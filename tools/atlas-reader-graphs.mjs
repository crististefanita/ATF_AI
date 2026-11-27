import {requireAtlas} from './atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
const plain=value=>typeof value==='string'&&value.trim().length>0;
function lines(label){need(plain(label),'Give the diagram an actual readable label');const result=[''];for(const word of label.split(/\s+/)){if((result.at(-1)+' '+word).trim().length>18)result.push(word);else result[result.length-1]=(result.at(-1)+' '+word).trim();}need(result.length<=3&&label.split(/\s+/).every(word=>word.length<=18),'Shorten the source-bound diagram label instead of shrinking its type: '+label);return result;}

/** Relationship identities and direction survive layout, including cycles and disconnected owners. */
export function layoutReaderGraph(graph){
  need(Array.isArray(graph?.nodes)&&graph.nodes.length>0&&Array.isArray(graph.edges),'A reader graph needs actual nodes and relationships');
  const nodes=new Map(graph.nodes.map(node=>[node.id,node]));need(nodes.size===graph.nodes.length,'Diagram node identities must be unique');
  const degree=new Map([...nodes.keys()].map(id=>[id,0])),rank=new Map([...nodes.keys()].map(id=>[id,0]));
  for(const edge of graph.edges){need(nodes.has(edge.from)&&nodes.has(edge.to),'A reader diagram relationship has an absent endpoint');degree.set(edge.to,degree.get(edge.to)+1);}
  const queue=[...degree].filter(([,count])=>count===0).map(([id])=>id);let visited=0;
  for(let i=0;i<queue.length;i++){const id=queue[i];visited++;for(const edge of graph.edges.filter(edge=>edge.from===id)){rank.set(edge.to,Math.max(rank.get(edge.to),rank.get(id)+1));degree.set(edge.to,degree.get(edge.to)-1);if(degree.get(edge.to)===0)queue.push(edge.to);}}
  const cyclic=visited!==nodes.size;if(cyclic)graph.nodes.forEach((node,index)=>rank.set(node.id,index));
  const columns=new Map();for(const node of graph.nodes){const column=rank.get(node.id);if(!columns.has(column))columns.set(column,[]);columns.get(column).push(node.id);}
  const rows=Math.max(...[...columns.values()].map(column=>column.length)),routed=graph.edges.filter(e=>rank.get(e.to)!==rank.get(e.from)+1).length,top=56+routed*44;
  const positions=graph.nodes.map(node=>{const column=rank.get(node.id),siblings=columns.get(column);return {...node,x:60+column*380,y:top+30+(rows-siblings.length)*58+siblings.indexOf(node.id)*116,width:220,height:90};});
  return {width:(Math.max(...rank.values())+1)*380,height:rows*116+top+84,nodes:positions,edges:graph.edges,cyclic,scope:cyclic?'NEUTRAL_CYCLIC_LAYOUT':'HORIZONTAL_RELATIONSHIP_LAYOUT'};
}

export function readerGraphSet(analysis){
  const reader=analysis.reader,views=reader.journeys.filter(journey=>journey.diagramMode!=='TEXT_ONLY').map(journey=>{
    const flow=analysis.behaviorFlows.find(f=>f.id===journey.flowId),negative=new Set(flow.caseAnalysis.cases.filter(c=>c.kind==='NEGATIVE').map(c=>c.nodeIds?.at(-1))),positive=new Set(flow.caseAnalysis.cases.filter(c=>c.kind==='POSITIVE').map(c=>c.nodeIds?.at(-1)));
    return {flowId:flow.id,graph:{...flow.graph,nodes:flow.graph.nodes.map(n=>({...n,role:negative.has(n.id)&&!positive.has(n.id)?'boundary':n.kind==='INTENT'?'intent':n.kind==='ACTION'?'action':'outcome'}))},title:journey.title,legend:'Arrows show the named conditions between intent, operation and possible outcomes. Case cards retain their independent recorded verdicts. Dashed boxes identify refusal or boundary outcomes; they do not mean that an existing check failed.'};
  });
  for(const view of reader.architecture.filter(view=>view.diagramMode!=='TEXT_ONLY')){const graph=analysis.dependencyGraphs.find(g=>g.id===view.graphId);views.push({...view,graph:{...graph,nodes:graph.nodes.map(n=>({...n,role:analysis.understanding?.originProfile==='SOURCE_ORIGINS_V1'&&analysis.understanding.units.find(u=>u.id===n.unitId)?.sourceRole==='CONSUMED_ARTIFACT'?'consumed':'owner',label:reader.capabilities.find(c=>c.unitId===n.unitId).title}))}});}
  views.push({title:reader.model.title,legend:reader.model.legend,graph:{id:'reader-value-model',kind:'VALUE',nodes:reader.model.stages.map(n=>({...n,label:n.title,role:'stage'})),edges:reader.model.links}});return views;
}

export function readerDiagramProjection(analysis){return readerGraphSet(analysis).map(view=>({id:view.graph.id||'declared-graph',kind:view.graph.kind||'RELATIONSHIP',title:view.title,nodes:view.graph.nodes.map(n=>({id:n.id,role:n.role||'owner',label:n.label.replace(/\s+/g,' ').trim()})),edges:view.graph.edges.map(e=>({from:e.from,to:e.to,label:e.label.replace(/\s+/g,' ').trim()}))}));}

/** A shared visual grammar cannot replace source-specific meaning with generic template labels. */
export function validateReaderDiagramProfile(analysis){
  if(analysis.reader?.diagramProfile===undefined)return {status:'NOT_ASSESSED'};
  need(['SOURCE_GRAPH_V1','SOURCE_GRAPH_V2'].includes(analysis.reader.diagramProfile),'Use the declared source-graph profile');const views=readerGraphSet(analysis),signatures=new Set();
  for(const view of views){need(plain(view.title)&&plain(view.legend)&&view.legend.length>=30,'Explain actual graph direction and boundaries beside it');layoutReaderGraph(view.graph);
    if(analysis.reader.diagramProfile==='SOURCE_GRAPH_V2')need(view.graph.nodes.length>=3&&new Set(view.graph.edges.map(e=>e.from+'\u0000'+e.to)).size>=2,'A useful reader diagram needs at least three meaningful nodes and two distinct source relationships: '+(view.graph.id||view.title)+'. Move a smaller idea to prose, a card or a list; never pad nodes or invent dependencies.');
    for(const node of view.graph.nodes)lines(node.label);
    for(const edge of view.graph.edges){need(plain(edge.label),'Do not invent or silently replace a missing relationship label');need(lines(edge.label).length<=2,'Move long relationship detail to the local legend instead of dropping meaning');}
    if(view.graph.kind==='BEHAVIOR'){
      need(view.graph.nodes.filter(n=>!/^(actor|observable result|business object|visible action|source evidence|test method)$/i.test(n.label.trim())).length>=2,'Replace template-only nodes with the actual actor, intent, object or outcome');
      need(view.graph.edges.every(e=>!/^(evaluate|observe|observe expected result|preserve refusal or failure)$/i.test(e.label.trim())&&e.label.trim().split(/\s+/).length>=2),'Name the domain condition on each behavior relationship instead of repeating generic template verbs');
      signatures.add(view.graph.nodes.map(n=>n.label.toLowerCase().trim()).sort().join('|'));
    }
  }
  need(views.filter(v=>v.graph.kind==='BEHAVIOR').length<4||signatures.size>=4,'Distinct source behaviors need distinct diagram signatures, not cloned labels');
  return {status:'PASSED',profile:analysis.reader.diagramProfile,graphs:views.length,scope:'SOURCE_RELATIONSHIPS_AND_DECLARED_DOMAIN_LABELS; NATIVE_GEOMETRY_AND_HUMAN_MEANING_REVIEW_REQUIRED'};
}

/** Full labels are wrapped or rejected, never silently replaced by a generic relationship. */
export function renderReaderGraph(graph,{title,legend,labels}){
  const layout=layoutReaderGraph(graph),byId=new Map(layout.nodes.map(node=>[node.id,node]));let lane=0;
  const paths=layout.edges.map(edge=>{const from=byId.get(edge.from),to=byId.get(edge.to),start=from.x+from.width,end=to.x,sy=from.y+45,ty=to.y+45,skip=end-start>200||end<start,y=skip?52+lane++*44:0;
    const route=skip?'M'+start+','+sy+' C'+(start+28)+','+sy+' '+(start+28)+','+y+' '+(start+42)+','+y+' H'+(end-36)+' C'+(end-12)+','+y+' '+(end-24)+','+ty+' '+end+','+ty:'M'+start+','+sy+' C'+(start+30)+','+sy+' '+(end-30)+','+ty+' '+end+','+ty;
    const label=edge.label||(graph.kind==='DEPENDENCY'?'uses':'relates'),text=lines(label),cx=(start+end)/2,baseline=skip?y-26:(sy+ty)/2-14-(text.length-1)*18;
    need(text.length<=2,'Keep relationship labels to two readable lines and explain detail in the legend');
    return '<g class="relationship" data-from="'+escape(edge.from)+'" data-to="'+escape(edge.to)+'"><path class="edge" d="'+route+'" marker-end="url(#arrow)"/><text class="edge-label" x="'+cx+'" y="'+baseline+'" text-anchor="middle">'+text.map((line,i)=>'<tspan x="'+cx+'" dy="'+(i?18:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  const boxes=layout.nodes.map(node=>{const text=lines(labels?.get(node.id)||node.label),center=node.x+110,top=node.y+45-(text.length-1)*11,role=node.role||'owner';return '<g class="node" data-node-id="'+escape(node.id)+'" data-graph-role="'+escape(role)+'"><rect x="'+node.x+'" y="'+node.y+'" width="220" height="90" rx="12"/><text x="'+center+'" y="'+top+'" text-anchor="middle">'+text.map((line,i)=>'<tspan x="'+center+'" dy="'+(i?22:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  return '<figure class="atlas-graph reader-map" data-graph-id="'+escape(graph.id||'declared-graph')+'" data-graph-kind="'+escape(graph.kind||'RELATIONSHIP')+'" role="region" tabindex="0" aria-label="'+escape(title)+'"><svg class="flow" role="img" aria-label="'+escape(title)+'" style="min-width:'+Math.ceil(layout.width*0.86)+'px" viewBox="0 0 '+layout.width+' '+layout.height+'">'+paths+boxes+'</svg><figcaption class="atlas-legend"><details><summary>Legend and boundaries</summary><ul><li>'+escape(legend)+(layout.cyclic?' A neutral row preserves the declared cycle; position does not imply execution order.':'')+'</li><li>Labels retain the declared relationship direction. Position and visual emphasis do not establish complete verification.</li></ul></details></figcaption></figure>';
}
