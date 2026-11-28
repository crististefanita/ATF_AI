import {requireAtlas} from './atlas-diagnostics.mjs';
import {layoutFittedDiagram} from '../atf-atlas/governance/diagram-layout.mjs';
import {referencePresentationProjection} from './atlas-reference.mjs';
import {presentationTable} from './atlas-presentation.mjs';
import {referenceTable} from '../atf-atlas/governance/reading.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
const plain=value=>typeof value==='string'&&value.trim().length>0;
function lines(label,width=18){need(plain(label),'Give the diagram an actual readable label');const result=[''];for(const word of label.split(/\s+/)){if((result.at(-1)+' '+word).trim().length>width)result.push(word);else result[result.length-1]=(result.at(-1)+' '+word).trim();}need(result.length<=3&&label.split(/\s+/).every(word=>word.length<=width),'Shorten the source-bound diagram label instead of shrinking its type: '+label);return result;}

/** Relationship identities and direction survive layout, including cycles and disconnected owners. */
export function layoutReaderGraph(graph,{fit=false}={}){
  need(Array.isArray(graph?.nodes)&&graph.nodes.length>0&&Array.isArray(graph.edges),'A reader graph needs actual nodes and relationships');
  if(graph.kind==='PRIMARY'){
    const ids=new Set(graph.nodes.map(n=>n.id));need(ids.size===graph.nodes.length&&graph.edges.every(e=>ids.has(e.from)&&ids.has(e.to)),'Primary map relationships need their actual unique endpoints');
    need(graph.nodes.every(n=>Number.isInteger(n.band)&&n.band>=0&&n.band<=2&&Number.isInteger(n.column)&&n.column>=0&&n.column<=2),'Use the bounded primary reading bands');
    need(new Set(graph.nodes.map(n=>n.band+'/'+n.column)).size===graph.nodes.length,'Primary map frames cannot overlap');
    return {width:940,height:Math.max(...graph.nodes.map(n=>n.band))*210+290,nodes:graph.nodes.map(n=>({...n,x:40+n.column*320,y:120+n.band*210,width:220,height:90})),edges:graph.edges,cyclic:false,scope:'AUTHORED_BUSINESS_READING_BANDS; NOT_AN_EXHAUSTIVE_RUNTIME_SEQUENCE'};
  }
  if(fit)return layoutFittedDiagram(graph);
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
  if(reader.model.diagramMode!=='TEXT_ONLY')views.push({title:reader.model.title,legend:reader.model.legend,graph:{id:'reader-value-model',kind:'VALUE',nodes:reader.model.stages.map(n=>({...n,label:n.title,role:'stage'})),edges:reader.model.links}});
  if(reader.primaryGraph){const g=reader.primaryGraph;views.unshift({title:g.title,legend:g.legend,primary:true,graph:{...g,kind:'PRIMARY',nodes:g.nodes.map(n=>({...n,role:n.maturity!=='REVIEWED'?'boundary':n.id===g.focusNodeId?'action':'owner'}))}});}
  if(reader.presentationProfile==='BUSINESS_REFERENCE_V2')for(const view of views){
    view.fit=true;
    view.concise=reader.readingProfile==='CONCISE_READING_V1';
    const journey=reader.journeys.find(j=>j.flowId===view.flowId);
    const pathScope=journey?analysis.behaviorFlows.find(f=>f.id===view.flowId).caseAnalysis.pathScope:null;
    view.legendItems=view.primary?[view.legend,'Solid objects have a scoped review. Dashed objects retain partial or unestablished understanding; they are not promises of future functionality.','The emphasized object is the owned result. Arrows name actual relationships and limits; placement is a reading aid, not a complete runtime sequence.']:journey?['Shows: '+journey.operation,'Adds to the main map: '+journey.outcome,'Boundary: '+(pathScope==='ALL_DECLARED_EDGES'?'The selected cases witness every declared relationship, not every implementation branch or deployment condition.':'These are representative observations; other paths and deployment conditions remain outside this view.')+' Individual proof and limits stay with the references.']:['Shows: '+view.legend,'Adds to the main map: '+view.title+' connects '+view.graph.nodes.map(n=>n.label).join(', ')+'.','Boundary: '+reader.review.limits];
    if(view.primary)view.references=referencePresentationProjection(analysis).primary.nodes.map(n=>[n.label,n.meaning+' '+n.status+'. '+n.proof+'.',n.limit]);
    else if(journey){const flow=analysis.behaviorFlows.find(f=>f.id===view.flowId);view.references=journey.cases.map(c=>{const witness=flow.caseAnalysis.cases.find(w=>w.id===c.caseId),oracle=analysis.claims.find(q=>q.id===witness.oracleClaimId),basis=witness.oracleStatus==='EXECUTED'?(oracle?.verdict==='FAILED'?'A retained check failed':'A selected original check passed'):witness.oracleStatus==='NOT_FOUND'?'No existing check identified':'An independent check was reviewed; execution is not inferred';return [c.title,basis,c.limits];});}
    else view.references=view.graph.nodes.map(n=>{const capability=reader.capabilities.find(c=>c.unitId===n.unitId);return [capability?.title||n.label,capability?.benefit||view.legend,reader.review.limits];});
  }
  return views;
}

export function readerDiagramProjection(analysis){return readerGraphSet(analysis).map(view=>({id:view.graph.id||'declared-graph',kind:view.graph.kind||'RELATIONSHIP',title:view.title,...(view.fit?{fit:true,reading:view.legendItems,references:view.references}:{}),nodes:view.graph.nodes.map(n=>({id:n.id,role:n.role||'owner',label:n.label.replace(/\s+/g,' ').trim(),...(view.primary?{maturity:n.maturity,maturityLabel:{REVIEWED:'Scoped review',PARTIAL:'Partial',UNKNOWN:'Not established'}[n.maturity],focus:n.id===view.graph.focusNodeId}:{})})),edges:view.graph.edges.map(e=>({from:e.from,to:e.to,label:e.label.replace(/\s+/g,' ').trim()}))}));}

/** A shared visual grammar cannot replace source-specific meaning with generic template labels. */
export function validateReaderDiagramProfile(analysis){
  if(analysis.reader?.diagramProfile===undefined)return {status:'NOT_ASSESSED'};
  need(['SOURCE_GRAPH_V1','SOURCE_GRAPH_V2'].includes(analysis.reader.diagramProfile),'Use the declared source-graph profile');const views=readerGraphSet(analysis),signatures=new Set();
  for(const view of views){need(plain(view.title)&&plain(view.legend)&&view.legend.length>=30,'Explain actual graph direction and boundaries beside it');layoutReaderGraph(view.graph,{fit:view.fit});
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
export function renderReaderGraph(graph,{title,legend,labels,fit=false,legendItems,references,concise=false}){
  const layout=layoutReaderGraph(graph,{fit}),byId=new Map(layout.nodes.map(node=>[node.id,node]));const responsive=fit&&concise,xPosition=value=>responsive?(value/layout.width*100).toFixed(6)+'%':value;let lane=0;
  if(graph.kind==='PRIMARY')return renderPrimaryMap(graph,layout,{title,legend,fit,legendItems,references,concise});
  const paths=layout.edges.map(edge=>{const from=byId.get(edge.from),to=byId.get(edge.to),start=from.x+from.width,end=to.x,sy=from.y+45,ty=to.y+45,skip=end-start>200||end<start,y=skip?52+lane++*44:0;
    const route=skip?'M'+start+','+sy+' C'+(start+28)+','+sy+' '+(start+28)+','+y+' '+(start+42)+','+y+' H'+(end-36)+' C'+(end-12)+','+y+' '+(end-24)+','+ty+' '+end+','+ty:'M'+start+','+sy+' C'+(start+30)+','+sy+' '+(end-30)+','+ty+' '+end+','+ty;
    const label=edge.label||(graph.kind==='DEPENDENCY'?'uses':'relates'),text=lines(label,responsive?12:fit?14:18),cx=(start+end)/2,baseline=skip?y-26:(sy+ty)/2-14-(text.length-1)*18;
    need(text.length<=(fit?3:2),'Keep relationship labels concise and explain detail in the legend');
    const path='<path class="edge" d="'+route+'"'+(responsive?' vector-effect="non-scaling-stroke"':'')+' marker-end="url(#arrow)"/>';
    const geometry=responsive?'<svg class="diagram-routing" width="100%" height="100%" viewBox="0 0 '+layout.width+' '+layout.height+'" preserveAspectRatio="none" aria-hidden="true">'+path+'</svg>':path;
    return '<g class="relationship" data-from="'+escape(edge.from)+'" data-to="'+escape(edge.to)+'">'+geometry+'<text class="edge-label" x="'+xPosition(cx)+'" y="'+baseline+'" text-anchor="middle">'+text.map((line,i)=>'<tspan x="'+xPosition(cx)+'" dy="'+(i?18:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  const boxes=layout.nodes.map(node=>{const text=lines(labels?.get(node.id)||node.label),center=node.x+node.width/2,top=node.y+45-(text.length-1)*11,role=node.role||'owner';return '<g class="node" data-node-id="'+escape(node.id)+'" data-graph-role="'+escape(role)+'"><rect x="'+xPosition(node.x)+'" y="'+node.y+'" width="'+xPosition(node.width)+'" height="90" rx="12"/><text x="'+xPosition(center)+'" y="'+top+'" text-anchor="middle">'+text.map((line,i)=>'<tspan x="'+xPosition(center)+'" dy="'+(i?22:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  const reading=fit?legendItems:[legend+(layout.cyclic?' A neutral row preserves the declared cycle; position does not imply execution order.':''),'Labels retain the declared relationship direction. Position and visual emphasis do not establish complete verification.'];
  need(!fit||Array.isArray(reading)&&reading.length===3&&reading.every(v=>plain(v)&&v.length>=30),'A fitted diagram needs three source-specific reading bullets: purpose, contribution to the main map and remaining boundary');
  return '<figure class="atlas-graph reader-map" data-graph-id="'+escape(graph.id||'declared-graph')+'" data-graph-kind="'+escape(graph.kind||'RELATIONSHIP')+'"'+(fit?' data-graph-fit="true"':'')+' role="region" tabindex="0" aria-label="'+escape(title)+'"><svg class="flow'+(responsive?' fluid-diagram':'')+'" role="img" aria-label="'+escape(title)+'"'+(fit?'':' style="min-width:'+Math.ceil(layout.width*0.86)+'px"')+' '+(responsive?'height="'+layout.height+'"':'viewBox="0 0 '+layout.width+' '+layout.height+'"')+'>'+paths+boxes+'</svg><figcaption class="atlas-legend"><details><summary>Legend and boundaries</summary><ul>'+reading.map(v=>'<li>'+escape(v)+'</li>').join('')+'</ul></details>'+(fit?renderDiagramReferences(references,false,concise):'')+'</figcaption></figure>';
}

function renderDiagramReferences(rows,primary=false,concise=false){
  need(Array.isArray(rows)&&rows.length>0&&rows.every(r=>Array.isArray(r)&&r.length===3&&r.every(plain)),'Diagram references need actual owned subjects, original proof and remaining limits; do not add an empty references disclosure');
  return '<details class="atlas-references'+(primary?' reader-primary-proof':'')+'"><summary>References and review scope</summary>'+(concise?referenceTable(rows):presentationTable(['Reference','Support for this view','Remaining boundary'],rows))+'</details>';
}

function renderPrimaryMap(graph,layout,{title,legend,fit,legendItems,references,concise}){
  const byId=new Map(layout.nodes.map(n=>[n.id,n])),responsive=fit&&concise,xPosition=value=>responsive?(value/layout.width*100).toFixed(6)+'%':value;
  const relationshipLines=value=>{const result=[''];for(const word of value.split(/\s+/)){if((result.at(-1)+' '+word).trim().length>11)result.push(word);else result[result.length-1]=(result.at(-1)+' '+word).trim();}return result;};
  const paths=layout.edges.map(edge=>{
    const from=byId.get(edge.from),to=byId.get(edge.to),wrapped=relationshipLines(edge.label),sameBand=from.band===to.band;
    need(wrapped.length<=2,'Keep primary relationship labels concise and explain detail in the legend');
    let route,cx,baseline;
    if(sameBand){const forward=to.x>from.x,sx=from.x+(forward?220:0),ex=to.x+(forward?0:220),y=from.y+45;cx=(sx+ex)/2;
      if(Math.abs(from.column-to.column)>1){route='M'+sx+','+y+' C'+sx+',40 '+(sx+30)+',40 '+(sx+40)+',40 H'+(ex-30)+' C'+ex+',40 '+ex+','+y+' '+ex+','+y;baseline=18;}
      else{route='M'+sx+','+y+' H'+ex;baseline=y-18-(wrapped.length-1)*18;}
    }else{const down=to.band>from.band,sx=from.x+110,sy=from.y+(down?90:0),ex=to.x+110,ey=to.y+(down?0:90),mid=(sy+ey)/2;route='M'+sx+','+sy+' C'+sx+','+mid+' '+ex+','+mid+' '+ex+','+ey;cx=(sx+ex)/2+(sx===ex?62:0);baseline=mid-10-(wrapped.length-1)*18;}
    const path='<path class="edge'+(edge.kind==='LIMIT'?' primary-limit':'')+'" d="'+route+'"'+(responsive?' vector-effect="non-scaling-stroke"':'')+' marker-end="url(#arrow)"/>';
    const geometry=responsive?'<svg class="primary-routing" width="100%" height="100%" viewBox="0 0 '+layout.width+' '+layout.height+'" preserveAspectRatio="none" aria-hidden="true">'+path+'</svg>':path;
    return '<g class="relationship" data-from="'+escape(edge.from)+'" data-to="'+escape(edge.to)+'">'+geometry+'<text class="edge-label" x="'+xPosition(cx)+'" y="'+baseline+'" text-anchor="middle">'+wrapped.map((line,i)=>'<tspan x="'+xPosition(cx)+'" dy="'+(i?18:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';
  }).join('');
  const boxes=layout.nodes.map(node=>{const wrapped=lines(node.label);need(wrapped.length<=2,'Use concise primary object labels; put their full meaning in the local proof');const x=node.x+110,top=node.y+32-(wrapped.length-1)*11;return '<g class="node" data-node-id="'+escape(node.id)+'" data-graph-role="'+escape(node.role)+'" data-maturity="'+escape(node.maturity)+'" data-focus="'+(node.id===graph.focusNodeId)+'"><rect x="'+xPosition(node.x)+'" y="'+node.y+'" width="'+xPosition(220)+'" height="90" rx="10"/><text class="primary-object-label" x="'+xPosition(x)+'" y="'+top+'" text-anchor="middle">'+wrapped.map((line,i)=>'<tspan x="'+xPosition(x)+'" dy="'+(i?22:0)+'">'+escape(line)+'</tspan>').join('')+'</text><text class="maturity-label" x="'+xPosition(x)+'" y="'+(node.y+77)+'" text-anchor="middle">'+({REVIEWED:'Scoped review',PARTIAL:'Partial',UNKNOWN:'Not established'}[node.maturity])+'</text></g>';}).join('');
  return '<figure class="atlas-graph reader-map primary-map" data-graph-id="'+escape(graph.id)+'" data-graph-kind="PRIMARY"'+(fit?' data-graph-fit="true"':'')+' role="region" tabindex="0" aria-label="'+escape(title)+'"><svg class="flow'+(responsive?' primary-fluid fluid-diagram':'')+'" role="img" aria-label="'+escape(title)+'" '+(responsive?'height="'+layout.height+'"':'viewBox="0 0 '+layout.width+' '+layout.height+'"')+'>'+paths+boxes+'</svg><figcaption class="atlas-legend"><details><summary>Legend and boundaries</summary><ul><li>'+escape(legend)+'</li><li>Solid objects have a scoped review. Dashed objects retain partial or unestablished understanding; they are not promises of future functionality.</li><li>The emphasized object is the owned result. Arrows name actual relationships and limits; placement is a reading aid, not a complete runtime sequence.</li></ul></details>'+(fit?renderDiagramReferences(references,true,concise):'')+'</figcaption></figure>';
}
