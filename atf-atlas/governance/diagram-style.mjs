const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
const color=value=>{const m=typeof value==='string'&&value.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)$/);return m?[...m.slice(1,4).map(Number),m[4]===undefined?1:Number(m[4])]:null;};
const neutral=value=>{const c=color(value);return c&&c.every(Number.isFinite)&&c.slice(0,3).every(v=>v>=0&&v<=80)&&(Math.max(...c.slice(0,3))-Math.min(...c.slice(0,3))<=32||c[2]>=c[0]&&c[2]>=c[1]&&Math.max(...c.slice(0,3))-Math.min(...c.slice(0,3))<=48)&&c[3]>=.8&&c[3]<=1;};

/** Inspect native visible primitives, not a class-name convention or a declared CSS token. */
export function reviewDiagramStyle(diagrams,expected){
  const findings=[],add=message=>findings.push({code:'DIAGRAM_STYLE',observation:message});
  if(!Array.isArray(diagrams)||diagrams.length!==expected.length){add('Measure every source diagram and its actual neutral shapes, centered labels and visible arrowheads.');return findings;}
  for(const [i,graph]of expected.entries()){
    const row=diagrams[i],style=row?.appearance;
    if(row?.id!==graph.id||!style||!Array.isArray(style.nodes)||!Array.isArray(style.edges)||!same(style.nodes.map(n=>n.id),graph.nodes.map(n=>n.id))||!same(style.edges.map(e=>[e.from,e.to]),graph.edges.map(e=>[e.from,e.to]))){add('Retain complete actual style measurements for '+graph.id+'; a CSS declaration or renamed object is not native proof.');continue;}
    for(const node of style.nodes)if(!['rect','circle','polygon'].includes(node.shape)||!neutral(node.fill)||!Number.isFinite(node.opacity)||node.opacity<.95||node.opacity>1||node.sharedFont!==true||node.anchor!=='middle'||!Number.isFinite(node.centerOffset)||node.centerOffset>2||node.centerOffset<0)add('Repair '+graph.id+'/'+node.id+': keep the actual node shape visible with a subdued neutral fill, use the report font family, and center every label line within 2 native CSS px. Put maturity in explicit text and subtle outlines instead of traffic-light fills; retain readable type and source meaning.');
    for(const edge of style.edges)if(edge.visible!==true||edge.arrowVisible!==true)add('Repair '+graph.id+'/'+edge.from+' → '+edge.to+': retain a visible connector and its resolved, painted arrowhead. A marker attribute with a hidden or missing arrow does not establish readable direction.');
  }
  return findings;
}
