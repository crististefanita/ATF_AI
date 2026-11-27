import {requireAtlas} from './atlas-diagnostics.mjs';
import {reviewAtlasAssessment} from './atlas-understanding.mjs';
import {presentationTable} from './atlas-presentation.mjs';

export const referenceProfile='BUSINESS_REFERENCE_V2';
const need=(condition,message)=>requireAtlas(condition,'ATLAS-READER',message);
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const text=value=>typeof value==='string'&&value.trim().length>=30;
const unique=values=>new Set(values).size===values.length;
const id=value=>typeof value==='string'&&/^[a-z][a-z0-9-]*$/.test(value);
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;');
const shape=(value,keys,name)=>need(object(value)&&Object.keys(value).every(key=>keys.includes(key)),'Use only the declared business reference fields: '+name);
const key=(group,axis)=>group+'/'+axis;
const maturity=facets=>facets.some(f=>f.status==='UNKNOWN')?'UNKNOWN':facets.some(f=>f.status==='PARTIAL')?'PARTIAL':'REVIEWED';
const maturityLabel={REVIEWED:'Reviewed within scope',PARTIAL:'Partial understanding',UNKNOWN:'Not established'};

/** Source and assessment bindings protect the authored projection, not automatic semantic truth. */
export function referencePresentationProjection(analysis){
  if(analysis.reader?.presentationProfile===undefined)return null;
  need(['BUSINESS_REFERENCE_V1',referenceProfile].includes(analysis.reader.presentationProfile),'Use the supported business reference presentation profile');
  const {primaryGraph:graph,assessment:reader}=analysis.reader;
  shape(graph,['id','title','summary','legend','focusNodeId','nodes','edges'],'primary map');
  need(id(graph.id)&&text(graph.summary)&&text(graph.legend),'Explain the actual primary map and its relationship boundary');
  need(Array.isArray(graph.nodes)&&graph.nodes.length>=3&&unique(graph.nodes.map(n=>n.id))&&Array.isArray(graph.edges),'A primary map needs at least three real source-bound objects');
  const claims=new Map(analysis.claims.map(c=>[c.id,c])),units=new Map(analysis.understanding.units.map(u=>[u.id,u]));
  const positions=new Set(),nodes=graph.nodes.map(node=>{
    shape(node,['id','label','meaning','limit','band','column','facetRefs','claimIds','oracleClaimIds','maturity'],'primary object');
    need(id(node.id)&&text(node.meaning)&&text(node.limit),'Explain each primary object and its own remaining limit: '+node.id);
    need(Number.isInteger(node.band)&&node.band>=0&&node.band<=2&&Number.isInteger(node.column)&&node.column>=0&&node.column<=2,'Place the primary object in the bounded three-band map: '+node.id);
    const position=node.band+'/'+node.column;need(!positions.has(position),'Two primary objects occupy the same source-reading position: '+node.id);positions.add(position);
    need(Array.isArray(node.facetRefs)&&node.facetRefs.length>0&&unique(node.facetRefs.map(r=>r.unitId+'/'+r.facet)),'Select distinct actual owner facets for each primary object: '+node.id);
    const facets=node.facetRefs.map(ref=>{shape(ref,['unitId','facet'],'primary facet');const facet=units.get(ref.unitId)?.facets?.[ref.facet];need(facet&&['REVIEWED','PARTIAL','UNKNOWN'].includes(facet.status),'The primary object borrows a missing or inapplicable facet: '+node.id);return facet;});
    need(node.maturity===maturity(facets),'Primary maturity must retain the least-established selected source facet: '+node.id);
    const owned=new Set(facets.flatMap(f=>f.claimIds)),oracles=new Set(facets.flatMap(f=>f.review?.oracleRefs?.map(r=>r.claimId)||[]));
    need(Array.isArray(node.claimIds)&&node.claimIds.length>0&&unique(node.claimIds)&&node.claimIds.every(c=>owned.has(c)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(c)?.status)),'Bind the primary object to its actual selected facet claims: '+node.id);
    need(Array.isArray(node.oracleClaimIds)&&unique(node.oracleClaimIds)&&node.oracleClaimIds.every(c=>oracles.has(c)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(c)?.status)),'Bind local object proof to independent selected facet oracles: '+node.id);
    need(node.maturity==='UNKNOWN'||node.oracleClaimIds.length>0,'A reviewed or partial primary object needs its independent existing oracle: '+node.id);
    const selected=node.oracleClaimIds.map(c=>claims.get(c));
    const proof=selected.some(c=>c.status==='EXECUTED'&&c.verdict==='FAILED')?'A retained check failed':selected.some(c=>c.status==='EXECUTED')?'Retained component checks':selected.length?'Existing checks reviewed in source':'No existing check established';
    return {id:node.id,label:node.label,meaning:node.meaning,limit:node.limit,maturity:node.maturity,status:maturityLabel[node.maturity],proof};
  });
  need(nodes.some(n=>n.id===graph.focusNodeId),'Highlight one actual owned primary object');
  const nodeMap=new Map(graph.nodes.map(n=>[n.id,n]));
  need(unique(graph.edges.map(e=>e.id))&&new Set(graph.edges.map(e=>e.from+'/'+e.to)).size>=2,'Retain distinct real primary relationships');
  for(const edge of graph.edges){
    shape(edge,['id','from','to','label','claimIds','kind'],'primary relationship');
    need(id(edge.id)&&nodeMap.has(edge.from)&&nodeMap.has(edge.to)&&edge.from!==edge.to&&['BEHAVIOR','ALTERNATIVE','LIMIT'].includes(edge.kind),'A primary relationship needs actual endpoints and declared meaning: '+edge.id);
    const owners=new Set([...nodeMap.get(edge.from).claimIds,...nodeMap.get(edge.to).claimIds]);
    need(Array.isArray(edge.claimIds)&&edge.claimIds.length>0&&unique(edge.claimIds)&&edge.claimIds.every(c=>owners.has(c)),'Bind the primary relationship to its actual endpoint source claims: '+edge.id);
    need(edge.kind!=='LIMIT'||[nodeMap.get(edge.from),nodeMap.get(edge.to)].some(n=>n.maturity!=='REVIEWED'),'A limitation arrow needs an actually partial or unknown endpoint: '+edge.id);
  }
  const visited=new Set([graph.nodes[0].id]);for(let pass=0;pass<graph.nodes.length;pass++)for(const e of graph.edges)if(visited.has(e.from)||visited.has(e.to)){visited.add(e.from);visited.add(e.to);}
  need(visited.size===graph.nodes.length,'Every primary object needs a real relationship; do not pad the map with isolated objects');
  need(graph.nodes.length<6||new Set(graph.nodes.map(n=>n.band)).size===3,'A rich primary map needs context, operation and outcome bands instead of a compressed row');
  need(analysis.reader.model.diagramMode==='TEXT_ONLY','Keep the earlier generic reading sequence in prose instead of duplicating the primary map');
  const assessment=reviewAtlasAssessment(analysis);
  need(assessment,'Visible review scores need the original scoped authored assessment');
  shape(reader,['groups','featured'],'visible assessment');
  need(Array.isArray(reader.groups)&&reader.groups.length===assessment.groups.length&&unique(reader.groups.map(g=>g.groupId)),'Represent every declared assessment group without mixing project and Atlas quality');
  const axes=[];
  for(const view of reader.groups){
    shape(view,['groupId','title','scope','limits','axes'],'score group');const group=assessment.groups.find(g=>g.id===view.groupId);
    need(group&&text(view.scope)&&text(view.limits)&&Array.isArray(view.axes)&&view.axes.length===group.axes.length&&unique(view.axes.map(a=>a.axisId)),'Retain the complete scoped assessment group: '+view.groupId);
    for(const item of view.axes){
      shape(item,['axisId','label','goal','question','measure','meaning','limits','ceiling','ceilingReason'],'score rationale');const axis=group.axes.find(a=>a.id===item.axisId);
      need(axis&&['goal','question','measure','meaning','limits','ceilingReason'].every(k=>text(item[k])),'Explain goal, question, measure and limits of the actual authored score: '+view.groupId+'/'+item.axisId);
      need(Number.isInteger(item.ceiling)&&item.ceiling>=1&&item.ceiling<=100&&axis.score<=item.ceiling,'A visible score exceeds its explicitly reviewed ceiling: '+item.axisId);
      need(axis.score<95||axis.evidenceIds.some(id=>(analysis.evidence||[]).some(e=>e.id===id&&['PASSED','FAILED'].includes(e.status))),'A 95+ axis needs original execution evidence as well as its declared limit: '+item.axisId);
      axes.push({groupId:view.groupId,axisId:item.axisId,groupTitle:view.title,...item,score:axis.score,correctness:axis.correctness,verification:axis.verification,clarity:axis.clarity,weight:axis.weight});
    }
  }
  need(Array.isArray(reader.featured)&&reader.featured.length>=3&&reader.featured.length<=6&&unique(reader.featured.map(r=>key(r.groupId,r.axisId))),'Feature three to six distinct scoped axes; do not invent an automatic Overall');
  const featured=reader.featured.map(ref=>{shape(ref,['groupId','axisId'],'featured score');const axis=axes.find(a=>key(a.groupId,a.axisId)===key(ref.groupId,ref.axisId));need(axis,'A featured score must reopen to its original assessment axis');return axis;});
  need(featured.every(a=>a.groupId===featured[0].groupId),'Feature one visibly identified assessment group; keep project and Atlas judgments separate');
  return {profile:referenceProfile,primary:{id:graph.id,title:graph.title,summary:graph.summary,legend:graph.legend,focusNodeId:graph.focusNodeId,nodes},axes,featured,groups:reader.groups};
}

export function referenceBusinessText(projection){
  if(!projection)return [];
  return [projection.primary.title,projection.primary.summary,projection.primary.legend,...projection.primary.nodes.flatMap(n=>[n.label,n.meaning,n.limit]),...projection.groups.flatMap(g=>[g.title,g.scope,g.limits]),...projection.axes.flatMap(a=>[a.label,a.goal,a.question,a.measure,a.meaning,a.limits,a.ceilingReason])];
}

export function renderReferenceScores(projection){
  if(!projection)return '';
  return '<div class="reader-score-opening"><p class="eyebrow">'+escape(projection.featured[0].groupTitle)+' · 1–100</p><div class="score-wall reader-score-wall">'+projection.featured.map(a=>'<a class="score-card" data-score-group="'+escape(a.groupId)+'" data-score-axis="'+escape(a.axisId)+'" href="#score-'+escape(a.groupId)+'-'+escape(a.axisId)+'"><span class="label">'+escape(a.label)+'</span><strong class="score">'+a.score+'<small> / 100</small></strong><span class="note">'+escape(a.meaning)+'</span></a>').join('')+'</div><p class="muted">Open a score for its review basis and limits.</p></div>';
}

export function renderReferenceRationale(projection){
  if(!projection)return '';
  return '<details class="reader-score-rationale"><summary>Review scores, goals and limits</summary><p>Each score rounds 40% correctness, 30% verification and 30% clarity. Group weights organize the assessment; this reader does not invent an Overall or turn a review into application acceptance.</p>'+projection.groups.map(g=>'<h3>'+escape(g.title)+'</h3><p>'+escape(g.scope)+' '+escape(g.limits)+'</p>'+projection.axes.filter(a=>a.groupId===g.groupId).map(a=>'<article class="reader-score-axis" id="score-'+escape(a.groupId)+'-'+escape(a.axisId)+'" data-score-group="'+escape(a.groupId)+'" data-score-axis="'+escape(a.axisId)+'"><h4>'+escape(a.label)+' · <span class="axis-score">'+a.score+'</span>/100</h4>'+presentationTable(['Review question','Basis and boundary'],[['Goal',a.goal],['Question',a.question],['Measure',a.measure],['Meaning',a.meaning],['Rubric inputs','Correctness '+a.correctness+' · verification '+a.verification+' · clarity '+a.clarity],['Review ceiling',a.ceiling+' — '+a.ceilingReason],['Remaining limit',a.limits]])+'</article>').join('')).join('')+'</details>';
}

export function renderPrimaryProof(projection){
  const graph=projection.primary;
  return '<details class="reader-primary-proof"><summary>What each object owns and what supports it</summary>'+presentationTable(['Project object','Meaning and support','Remaining boundary'],graph.nodes.map(n=>[n.label,n.meaning+' '+n.status+'. '+n.proof+'.',n.limit]))+'</details>';
}
