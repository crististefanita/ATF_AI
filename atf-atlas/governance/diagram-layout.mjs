import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);

/** Preserve real relationships within a shared canvas; wider ideas need an authored split. */
export function layoutFittedDiagram(graph){
  const nodes=new Map(graph.nodes.map(n=>[n.id,n]));
  need(nodes.size===graph.nodes.length&&nodes.size>0,'Fitted diagrams need actual unique source objects');
  const degree=new Map([...nodes.keys()].map(id=>[id,0])),rank=new Map([...nodes.keys()].map(id=>[id,0]));
  for(const edge of graph.edges){need(nodes.has(edge.from)&&nodes.has(edge.to),'Fitted relationships need their actual source endpoints');degree.set(edge.to,degree.get(edge.to)+1);}
  const queue=[...degree].filter(([,n])=>n===0).map(([id])=>id);let visited=0;
  for(let i=0;i<queue.length;i++){const id=queue[i];visited++;for(const edge of graph.edges.filter(e=>e.from===id)){rank.set(edge.to,Math.max(rank.get(edge.to),rank.get(id)+1));degree.set(edge.to,degree.get(edge.to)-1);if(degree.get(edge.to)===0)queue.push(edge.to);}}
  const cyclic=visited!==nodes.size;if(cyclic)graph.nodes.forEach((n,i)=>rank.set(n.id,i));
  need(Math.max(...rank.values())<=2,'This view is too wide to read without horizontal scrolling. Split it into meaningful source-bound subviews or use the supported prose view; preserve original relationships in the audit instead of shrinking type or hiding overflow.');
  const columns=new Map();for(const n of graph.nodes){const column=rank.get(n.id);if(!columns.has(column))columns.set(column,[]);columns.get(column).push(n.id);}
  const rows=Math.max(...[...columns.values()].map(c=>c.length)),routed=graph.edges.filter(e=>rank.get(e.to)!==rank.get(e.from)+1).length,top=56+routed*44,offset=(960-(Math.max(...rank.values())*340+280))/2;
  return {width:960,height:rows*160+top+40,nodes:graph.nodes.map(n=>{const column=rank.get(n.id),siblings=columns.get(column);return {...n,x:offset+40+column*340,y:top+30+(rows-siblings.length)*80+siblings.indexOf(n.id)*160,width:200,height:90};}),edges:graph.edges,cyclic,scope:'BOUNDED_HORIZONTAL_READING; NO_HORIZONTAL_SCROLL'};
}

/** These observations come from the rendered page; source-specific meaning needs separate review. */
export function reviewFittedDiagramMetrics(structure,graphIds,openingSupport){
  const findings=[],add=(code,observation)=>findings.push({code,observation});
  if(!Array.isArray(structure.horizontalOverflow)||structure.horizontalOverflow.length)add('HORIZONTAL_SCROLL','The whole reader, including diagrams, tables and expanded support, must fit without horizontal scrolling; split a wide idea into meaningful source-bound views or prose instead of clipping or shrinking type');
  const scale=structure.diagramScale;
  if(!Array.isArray(scale)||JSON.stringify(scale.map(v=>v.id))!==JSON.stringify(graphIds)||scale.some(v=>!Number.isFinite(v.min)||!Number.isFinite(v.max)||v.min<13||v.max>24||v.min>v.max)||scale.length&&Math.max(...scale.map(v=>v.max))/Math.min(...scale.map(v=>v.max))>1.3)add('DIAGRAM_SCALE','Measure actual SVG text pixels after transforms; keep supporting views at a consistent readable scale instead of inflating sparse diagrams or shrinking multiline labels');
  if(!Array.isArray(openingSupport)||JSON.stringify(openingSupport.map(v=>v.id))!==JSON.stringify(graphIds)||openingSupport.some(v=>v.legends!==1||v.references!==1||v.details!==2||v.open!==0))add('DIAGRAM_DISCLOSURES','Every diagram needs exactly two initially closed local disclosures: a source-specific legend and references with original proof and remaining limits');
  return findings;
}

/** Native rectangles retain transformed CSS pixels; no graph meaning is inferred from distance. */
export function reviewDiagramSpacing(diagrams,expected){
  const findings=[],add=observation=>findings.push({code:'DIAGRAM_SPACING',observation});
  const rectangle=b=>b&&['x','y','width','height'].every(k=>Number.isFinite(b[k]))&&b.width>0&&b.height>0;
  if(!Array.isArray(diagrams)||diagrams.length!==expected.length){add('Retain every actual diagram and its measured node/label rectangles');return findings;}
  for(const [i,wanted]of expected.entries()){
    const row=diagrams[i],s=row?.spacing,identity=JSON.stringify;
    if(row?.id!==wanted.id||!s||!Array.isArray(s.nodes)||!Array.isArray(s.labels)||identity(s.nodes.map(n=>n.id))!==identity(wanted.nodes.map(n=>n.id))||identity(s.labels.map(e=>[e.from,e.to]))!==identity(wanted.edges.map(e=>[e.from,e.to]))||![...s.nodes,...s.labels].every(rectangle)){add('Measure the actual distinct source objects and relationship labels for '+wanted.id+'; do not substitute a declared padding value');continue;}
    const pairs=[...s.nodes.map((n,i)=>s.nodes.slice(i+1).map(m=>[n,m])),...s.labels.map((e,i)=>[...s.nodes.map(n=>[e,n]),...s.labels.slice(i+1).map(m=>[e,m])])].flat();
    for(const [a,b]of pairs){const dx=Math.max(a.x-b.x-b.width,b.x-a.x-a.width,0),dy=Math.max(a.y-b.y-b.height,b.y-a.y-a.height,0),gap=Math.hypot(dx,dy);if(gap<6)add('Give '+wanted.id+' at least 6 native CSS px between '+(a.id||a.from+' → '+a.to)+' and '+(b.id||b.from+' → '+b.to)+'; measured '+gap.toFixed(2)+' px. Move or shorten the source-bound label without shrinking type or deleting a relationship');}
  }
  return findings;
}
