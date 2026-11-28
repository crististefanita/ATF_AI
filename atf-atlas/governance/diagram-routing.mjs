import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);

/** Soften actual orthogonal vertices, preserving ports, order and direction. */
export function roundedOrthogonalPath(vertices,radius=5){
  need(Array.isArray(vertices)&&vertices.length>=2&&Number.isFinite(radius)&&radius>0,'Supply finite orthogonal vertices and a positive corner radius; retain actual graph ports');
  const points=[];
  for(const point of vertices){
    need(Array.isArray(point)&&point.length===2&&point.every(Number.isFinite),'Diagram route coordinates must be finite pairs');
    if(!points.length||point.some((v,i)=>v!==points.at(-1)[i]))points.push([...point]);
  }
  need(points.length>=2,'A diagram relationship must connect distinct actual ports');
  for(let i=1;i<points.length;i++)need(points[i][0]===points[i-1][0]||points[i][1]===points[i-1][1],'Round only orthogonal routes; do not silently change an authored diagonal');
  const out=['M'+points[0].join(',')];
  for(let i=1;i<points.length-1;i++){
    const a=points[i-1],b=points[i],c=points[i+1],incoming=Math.hypot(b[0]-a[0],b[1]-a[1]),outgoing=Math.hypot(c[0]-b[0],c[1]-b[1]);
    if((a[0]===b[0])===(b[0]===c[0])){out.push('L'+b.join(','));continue;}
    const r=Math.min(radius,incoming/2,outgoing/2),entry=b.map((v,j)=>v+(a[j]-v)*r/incoming),exit=b.map((v,j)=>v+(c[j]-v)*r/outgoing);
    out.push('L'+entry.join(',')+' Q'+b.join(',')+' '+exit.join(','));
  }
  out.push('L'+points.at(-1).join(','));return out.join(' ');
}
