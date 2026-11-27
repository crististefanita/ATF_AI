import {requireAtlas} from './atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
function lines(label){const result=[''];for(const word of label.split(/\s+/)){if((result.at(-1)+' '+word).trim().length>18)result.push(word);else result[result.length-1]=(result.at(-1)+' '+word).trim();}need(result.length<=3&&label.split(/\s+/).every(word=>word.length<=18),'Shorten the source-bound diagram label instead of shrinking its type: '+label);return result;}

/** Dependency direction is preserved. Cycles use a finite neutral row, never an invented ordering. */
export function layoutReaderGraph(graph){
  need(Array.isArray(graph.nodes)&&graph.nodes.length>0&&Array.isArray(graph.edges),'A reader graph needs actual nodes and relationships');
  const nodes=new Map(graph.nodes.map(node=>[node.id,node]));need(nodes.size===graph.nodes.length,'Diagram node identities must be unique');
  const degree=new Map([...nodes.keys()].map(id=>[id,0])),rank=new Map([...nodes.keys()].map(id=>[id,0]));
  for(const edge of graph.edges){need(nodes.has(edge.from)&&nodes.has(edge.to),'A reader diagram relationship has an absent endpoint');degree.set(edge.to,degree.get(edge.to)+1);}
  const queue=[...degree].filter(([,count])=>count===0).map(([id])=>id);let visited=0;
  for(let i=0;i<queue.length;i++){const id=queue[i];visited++;for(const edge of graph.edges.filter(edge=>edge.from===id)){rank.set(edge.to,Math.max(rank.get(edge.to),rank.get(id)+1));degree.set(edge.to,degree.get(edge.to)-1);if(degree.get(edge.to)===0)queue.push(edge.to);}}
  const cyclic=visited!==nodes.size;if(cyclic)graph.nodes.forEach((node,index)=>rank.set(node.id,index));
  const columns=new Map();for(const node of graph.nodes){const column=rank.get(node.id);if(!columns.has(column))columns.set(column,[]);columns.get(column).push(node.id);}
  const rows=Math.max(...[...columns.values()].map(column=>column.length)),height=rows*116+110;
  const positions=graph.nodes.map(node=>{const column=rank.get(node.id),siblings=columns.get(column);return {...node,x:20+column*280,y:70+(rows-siblings.length)*58+siblings.indexOf(node.id)*116,width:220,height:90};});
  return {width:(Math.max(...rank.values())+1)*280,height,nodes:positions,edges:graph.edges,cyclic,scope:cyclic?'NEUTRAL_CYCLIC_LAYOUT':'HORIZONTAL_RELATIONSHIP_LAYOUT'};
}

/** Rendering does not add, remove or reverse a reviewed source relationship. */
export function renderReaderGraph(graph,{title,legend,labels}){
  const layout=layoutReaderGraph(graph),byId=new Map(layout.nodes.map(node=>[node.id,node]));
  const paths=layout.edges.map(edge=>{const from=byId.get(edge.from),to=byId.get(edge.to),start=from.x+from.width,end=to.x,sy=from.y+45,ty=to.y+45,skip=end-start>100||end<start;
    const route=skip?'M'+start+','+sy+' C'+(start+28)+','+sy+' '+(start+28)+',24 '+(start+42)+',24 H'+(end-36)+' C'+(end-12)+',24 '+(end-24)+','+ty+' '+end+','+ty:'M'+start+','+sy+' C'+(start+30)+','+sy+' '+(end-30)+','+ty+' '+end+','+ty;
    const label=graph.kind==='DEPENDENCY'?'uses':edge.label&&edge.label.length<=16?edge.label:'observe';
    return '<path class="edge" d="'+route+'" marker-end="url(#arrow)"/><text class="edge-label" x="'+((start+end)/2)+'" y="'+(skip?18:(sy+ty)/2-12)+'" text-anchor="middle">'+escape(label)+'</text>';}).join('');
  const boxes=layout.nodes.map(node=>{const text=lines(labels?.get(node.id)||node.label),center=node.x+110,top=node.y+45-(text.length-1)*11;return '<g class="node"><rect x="'+node.x+'" y="'+node.y+'" width="220" height="90" rx="12"/><text x="'+center+'" y="'+top+'" text-anchor="middle">'+text.map((line,i)=>'<tspan x="'+center+'" dy="'+(i?22:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  return '<figure class="atlas-graph reader-map" role="region" tabindex="0" aria-label="'+escape(title)+'"><svg class="flow" role="img" aria-label="'+escape(title)+'" style="min-width:'+Math.ceil(layout.width*0.86)+'px" viewBox="0 0 '+layout.width+' '+layout.height+'">'+paths+boxes+'</svg><figcaption class="atlas-legend">'+escape(legend)+(layout.cyclic?' A neutral row preserves the declared cycle; position does not imply execution order.':'')+'</figcaption></figure>';
}
