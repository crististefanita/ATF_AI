const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
const finite=value=>typeof value==='number'&&Number.isFinite(value);

/** Compactness is measured in native pixels; it cannot replace source or clearance checks. */
export function reviewDiagramDensity(diagrams,expected){
  const findings=[],add=message=>findings.push({code:'DIAGRAM_DENSITY',observation:message});
  if(!Array.isArray(diagrams)||diagrams.length!==expected.length){add('Measure every actual compact canvas and its visible node, type and stroke geometry.');return findings;}
  for(const [i,graph]of expected.entries()){
    const row=diagrams[i],m=row?.density;
    if(row?.id!==graph.id||!m||!Array.isArray(m.nodes)||!Array.isArray(m.edges)||!same(m.nodes.map(n=>n?.id),graph.nodes.map(n=>n.id))||!same(m.edges.map(e=>[e?.from,e?.to]),graph.edges.map(e=>[e.from,e.to]))){add('Retain complete native density identities for '+graph.id+'; a compact class name is not measurement.');continue;}
    const ordinary=m.nodes.filter((_,i)=>graph.nodes[i].role!=='action').map(n=>n.stroke),baseStroke=Math.min(...ordinary);
    const primary=graph.kind==='PRIMARY',dependency=graph.kind==='DEPENDENCY',maxHeight=primary?560:dependency?480:420,maxPadding=primary?180:dependency?180:120;
    if(!finite(m.height)||m.height<=0||m.height>maxHeight||!finite(m.contentHeight)||m.contentHeight<=0||m.contentHeight>m.height+2||m.height-m.contentHeight>maxPadding||m.nodes.some(n=>!finite(n.height)||n.height<56||n.height>76||!finite(n.stroke)||n.stroke<1||n.stroke>2.1)||m.nodes.some((n,j)=>graph.nodes[j].role==='action'&&(!finite(baseStroke)||n.stroke<baseStroke*1.35))||m.edges.some(e=>!finite(e.stroke)||e.stroke<1||e.stroke>1.75)||!finite(m.minText)||!finite(m.maxText)||m.minText<13||m.maxText>18){add('Repair '+graph.id+': reduce empty vertical space, padding and oversized strokes while retaining 56–76 px nodes, readable 13–18 px type and every original relationship. Keep the main canvas at most 560 px, supporting canvases at most 420 px (480 for routed dependencies), and recheck label clearance and painted arrows. Do not shrink text, clip a view or narrow its full-width band.');}
  }
  return findings;
}
