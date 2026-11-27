import {requireAtlas} from './atlas-diagnostics.mjs';
import {presentationTable} from './atlas-presentation.mjs';
import {renderReaderGraph} from './atlas-reader-graphs.mjs';

export const businessSections=Object.freeze(['overview','capabilities','journeys','operating-model','confidence','limits']);
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;');
const technical=/\b(?:ssh|sha-?256|checksum|json|xml|tap|mvn|javac|source[- ]index)\b|\b(?:git\s+(?:show|status|diff)|node\s+--test)\b|\b[a-f0-9]{40,64}\b|(?:[a-z]:[\\/]|\.\.\/)|\b[\w./-]+\.(?:java|mjs|cjs|json|xml|log|md|css|ya?ml|properties|txt|html)\b/i;
function labelLines(title){const lines=[''];for(const word of title.split(' ')){if((lines.at(-1)+' '+word).trim().length>18)lines.push(word);else lines[lines.length-1]=(lines.at(-1)+' '+word).trim();}return lines;}
function prose(value,name){need(typeof value==='string'&&value.trim().length>=3&&value.length<=1800&&!/[<>`{}\[\]\x00-\x08\x0b\x0c\x0e-\x1f]/.test(value),'Reader prose is missing, excessive or contains markup: '+name);need(!technical.test(value),'Move technical construction detail out of reader prose: '+name);return value;}
function shape(value,keys,name){need(object(value)&&Object.keys(value).every(key=>keys.includes(key)),'Reader object has unknown or technical fields: '+name);}
function refs(ids,claims,name,allowUnknown=false){need(Array.isArray(ids)&&ids.length>0&&new Set(ids).size===ids.length&&ids.every(id=>claims.has(id)&&(allowUnknown||['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(id).status))),'Reader statement needs declared reviewed source claims: '+name);}
function statement(item,claims,name,allowUnknown=false){shape(item,['title','text','claimIds'],name);prose(item.title,name+' title');prose(item.text,name+' explanation');refs(item.claimIds,claims,name,allowUnknown);}
function exact(actual,expected,name){need(actual.length===expected.length&&new Set(actual).size===actual.length&&expected.every(id=>actual.includes(id)),'Reader coverage differs from declared '+name);}

/** Publication-envelope inspection is deliberately weaker than re-rendering with the original adjacent audit. */
export function inspectBusinessReaderHtml(html,{projectId}={}){
  need(typeof html==='string'&&/<html\b[^>]*data-atlas-reader="5"/.test(html),'Declare the current business reader version');
  need(!/<(?:template|script|pre|code|iframe|object)\b/i.test(html),'The business HTML contains hidden or technical payload elements');
  if(projectId!==undefined)requireAtlas(html.includes('data-project-id="'+escape(projectId)+'"'),'ATLAS-TARGET','The business reader belongs to another selected project');
  const sections=[...html.matchAll(/<section id="([^"]+)" data-atlas-section="([^"]+)" data-section-number="([^"]+)"/g)];
  need(sections.length===businessSections.length&&sections.every((m,i)=>m[1]===businessSections[i]&&m[2]===m[1]&&m[3]===String(i+1).padStart(2,'0')),'Reader section identities, order or numbering differ');
  const calibrated=html.includes('data-atlas-calibration="1"');
  need(!html.includes('data-atlas-calibration=')||calibrated,'Unsupported artifact calibration profile');
  const links=[...html.matchAll(/href="([^"]+)"/g)].map(m=>m[1]);
  need(links.length===businessSections.length&&links.every((href,i)=>href==='#'+businessSections[i]+(calibrated?'-content':'')),'Reader navigation must mirror the numbered local sections');
  if(calibrated)for(const id of businessSections)need((html.match(new RegExp('id="'+id+'-content"','g'))||[]).length===1,'Reader navigation needs one content target inside each disclosure: '+id);
  const prose=html.replace(/<style\b[^>]*>[\s\S]*?<\/style>/gi,'').replace(/<[^>]*>/g,' ');
  need(!technical.test(prose),'Technical construction detail leaked into the published reader');
  return {status:'PASSED',profile:calibrated?'CALIBRATED_V1':'HISTORICAL_V5_ENVELOPE',scope:'READER_ENVELOPE_ONLY; ORIGINAL_AUDIT_AND_SEMANTIC_REVIEW_STILL_REQUIRED'};
}

/** Source bindings and audience checks complement an explicit semantic review; they do not prove prose truth. */
export function validateBusinessReader(analysis){
  const r=analysis.reader,claims=new Map(analysis.claims.map(c=>[c.id,c]));
  shape(r,['schemaVersion','projectId','language','title','summary','reviewDate','review','value','capabilities','journeys','auditOnlyFlows','architecture','model','limits'],'project reader');
  need(r.schemaVersion===1&&r.projectId===analysis.projectId&&r.language==='en','Reader needs the same target, schemaVersion 1 and English authored prose');
  prose(r.title,'project title');prose(r.summary,'project summary');
  need(/^\d{4}-\d{2}-\d{2}$/.test(r.reviewDate)&&Number.isFinite(Date.parse(r.reviewDate+'T00:00:00Z'))&&new Date(r.reviewDate+'T00:00:00Z').toISOString().slice(0,10)===r.reviewDate,'Reader needs an actual review date');
  shape(r.review,['reviewer','scope','limits','claimIds'],'semantic review');
  for(const key of ['reviewer','scope','limits'])prose(r.review[key],'review '+key);
  refs(r.review.claimIds,claims,'semantic review');
  need(Array.isArray(r.value)&&r.value.length>0,'Explain the project value');
  r.value.forEach(item=>statement(item,claims,'project value'));
  const units=analysis.understanding?.units||[];
  need(Array.isArray(r.capabilities),'Declare reader capabilities');
  exact(r.capabilities.map(item=>item.unitId),units.map(item=>item.id),'owner units');
  for(const item of r.capabilities){shape(item,['unitId','title','benefit','claimIds'],'capability');prose(item.title,'capability title');prose(item.benefit,'capability benefit');refs(item.claimIds,claims,'capability');const unit=units.find(u=>u.id===item.unitId);need(item.claimIds.every(id=>[...unit.claimIds,...unit.sharedClaimIds].includes(id)),'Capability evidence belongs to another owner: '+item.unitId);}
  need(Array.isArray(r.journeys)&&r.journeys.length>0&&Array.isArray(r.auditOnlyFlows),'Declare reader journeys and explicit audit-only scope');
  const flows=analysis.behaviorFlows||[];
  exact([...r.journeys.map(j=>j.flowId),...r.auditOnlyFlows.map(j=>j.flowId)],flows.map(f=>f.id),'behavior flows');
  for(const omitted of r.auditOnlyFlows){shape(omitted,['flowId','reason','explanation'],'audit-only flow');need(['REPORT_CONSTRUCTION','IMPLEMENTATION_DETAIL'].includes(omitted.reason),'An audit-only flow needs a technical-scope reason');prose(omitted.explanation,'audit-only rationale');}
  for(const journey of r.journeys){
    shape(journey,['flowId','title','actor','intent','operation','outcome','cases'],'journey');
    for(const key of ['title','actor','intent','operation','outcome'])prose(journey[key],'journey '+key);
    const flow=flows.find(f=>f.id===journey.flowId);
    need(Array.isArray(journey.cases),'Explain reviewed cases');exact(journey.cases.map(c=>c.caseId),(flow.caseAnalysis?.cases||[]).map(c=>c.id),'journey cases');
    for(const item of journey.cases){shape(item,['caseId','title','situation','expectation','limits'],'journey case');for(const key of ['title','situation','expectation','limits'])prose(item[key],'case '+key);}
  }
  need(Array.isArray(r.architecture)&&r.architecture.length>0,'Explain the actual project architecture with source-bound views');
  exact(r.architecture.map(view=>view.graphId),(analysis.dependencyGraphs||[]).map(graph=>graph.id),'architecture graphs');
  for(const view of r.architecture){shape(view,['graphId','title','legend'],'architecture view');prose(view.title,'architecture title');prose(view.legend,'architecture legend');}
  shape(r.model,['title','stages','links','legend'],'operating model');prose(r.model.title,'model title');prose(r.model.legend,'model legend');
  need(Array.isArray(r.model.stages)&&r.model.stages.length>0&&r.model.stages.length<=3&&Array.isArray(r.model.links),'Use a small source-bound operating model');
  const stageIds=new Set();for(const stage of r.model.stages){shape(stage,['id','title','claimIds'],'model stage');need(/^[a-z][a-z0-9-]*$/.test(stage.id)&&!stageIds.has(stage.id),'Model stages need unique identities');stageIds.add(stage.id);prose(stage.title,'model stage');need(labelLines(stage.title).length<=2&&stage.title.split(' ').every(word=>word.length<=18),'Use a concise two-line diagram label');refs(stage.claimIds,claims,'model stage');}
  for(const link of r.model.links){shape(link,['from','to','label','claimIds'],'model relationship');need(stageIds.has(link.from)&&stageIds.has(link.to)&&link.from!==link.to,'Model relationship has an unknown or repeated endpoint');prose(link.label,'model relationship');need(link.label.length<=24,'Use a concise relationship label');refs(link.claimIds,claims,'model relationship');}
  need(r.model.links.length===r.model.stages.length-1&&r.model.links.every((link,i)=>link.from===r.model.stages[i].id&&link.to===r.model.stages[i+1].id),'The reader model must follow its declared adjacent reading stages; keep detailed branches in the audit');
  need(Array.isArray(r.limits)&&r.limits.length>0,'Keep remaining boundaries visible');r.limits.forEach(item=>statement(item,claims,'reader limit',true));
  const covered=new Set(r.limits.flatMap(l=>l.claimIds));for(const claim of claims.values())if(claim.status==='UNKNOWN'||claim.status==='EXECUTED'&&claim.verdict==='FAILED')need(covered.has(claim.id),'An unknown source claim is absent from the visible reader limits: '+claim.id);
  return {status:'PASSED',audience:'PROJECT_UNDERSTANDING',journeys:r.journeys.length,cases:r.journeys.reduce((n,j)=>n+j.cases.length,0),auditOnlyFlows:r.auditOnlyFlows.length,semanticTruth:'HUMAN_REVIEW_REQUIRED'};
}

function caseProof(source,claims){
  if(source.oracleStatus==='NOT_FOUND')return 'No existing check identified';
  if(source.oracleStatus!=='EXECUTED')return 'Check reviewed in source; no execution inferred';
  const oracle=claims.get(source.oracleClaimId);
  need(oracle?.status==='EXECUTED'&&['PASSED','FAILED'].includes(oracle.verdict),'An executed reader case needs its actual oracle verdict: '+source.id);
  return oracle.verdict==='FAILED'?'Recorded check failed':'Recorded check passed';
}
function reviewDepth(unit){
  const statuses=Object.values(unit.facets||{}).map(f=>f.status);
  if(!statuses.length)return 'Review depth not declared';
  if(statuses.includes('UNKNOWN'))return statuses.includes('PARTIAL')?'Partial review; some areas unestablished':'Some areas unestablished';
  return statuses.includes('PARTIAL')?'Partial review':'Declared aspects reviewed';
}
const paragraph=text=>'<p>'+escape(text)+'</p>';
const number=value=>String(value).padStart(2,'0');
const badge=value=>'<span class="section-number">'+value+'</span>';
const card=(title,text,label)=>'<article class="card"><h3>'+badge(label)+escape(title)+'</h3>'+paragraph(text)+'</article>';
function modelSvg(model){
  const count=model.stages.length,width=count*280,height=160;
  const at=new Map(model.stages.map((s,i)=>[s.id,20+i*280]));
  const edges=model.links.map(link=>{const from=at.get(link.from)+220,to=at.get(link.to),middle=(from+to)/2;return '<path class="edge" d="M'+from+',80 H'+to+'" marker-end="url(#arrow)"/><text class="edge-label" x="'+middle+'" y="58" text-anchor="middle">'+escape(link.label)+'</text>';}).join('');
  const nodes=model.stages.map(stage=>{const x=at.get(stage.id),lines=labelLines(stage.title);return '<g class="node"><rect x="'+x+'" y="40" width="220" height="80" rx="12"/><text x="'+(x+110)+'" y="'+(lines.length>1?70:86)+'" text-anchor="middle">'+lines.map((line,i)=>'<tspan x="'+(x+110)+'" dy="'+(i?22:0)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';}).join('');
  return '<figure class="atlas-graph"><svg class="flow" role="img" aria-label="'+escape(model.title)+'" viewBox="0 0 '+width+' '+height+'">'+edges+nodes+'</svg><figcaption class="atlas-legend">'+escape(model.legend)+'</figcaption></figure>';
}

/** Only the audience projection reaches HTML; raw source, commands and proof payloads stay in the adjacent audit. */
export function renderBusinessReader(bundle,css){
  const analysis=bundle.analysis,r=analysis.reader;validateBusinessReader(analysis);
  const flowById=new Map(analysis.behaviorFlows.map(f=>[f.id,f])),claims=new Map(analysis.claims.map(c=>[c.id,c]));
  const breadth='<h3>Review depth by capability</h3><div class="reader-breadth">'+presentationTable(['Capability','Source review'],analysis.understanding.units.map(unit=>[r.capabilities.find(c=>c.unitId===unit.id).title,reviewDepth(unit)]))+'</div>'+paragraph('Review depth concerns purpose, ownership, behavior, information, setup, failure handling and available checks. Partial review means selected aspects were inspected; passing checks do not complete unreviewed areas.');
  const unknownAreas=(analysis.understanding?.units||[]).flatMap(unit=>{const missing=Object.entries(unit.facets||{}).filter(([,facet])=>facet.status==='UNKNOWN').map(([name])=>({purpose:'purpose',structure:'ownership',behavior:'behavior',data:'information',configuration:'setup',failure:'failure handling',verification:'available checks'}[name]));return missing.length?[[r.capabilities.find(c=>c.unitId===unit.id).title,missing.join(', ')]]:[];});
  const architecture=r.architecture.map(view=>{const graph=analysis.dependencyGraphs.find(g=>g.id===view.graphId);return '<h3>'+escape(view.title)+'</h3>'+renderReaderGraph(graph,{...view,labels:new Map(graph.nodes.map(node=>[node.id,r.capabilities.find(c=>c.unitId===node.unitId).title]))});}).join('');
  const bodies=[paragraph(r.summary)+'<div class="reader-grid">'+r.value.map((item,i)=>card(item.title,item.text,'01.'+number(i+1))).join('')+'</div>',
    '<div class="reader-grid">'+r.capabilities.map((item,i)=>card(item.title,item.benefit,'02.'+number(i+1))).join('')+'</div>',
    r.journeys.map((journey,i)=>{const flow=flowById.get(journey.flowId),ordinal='03.'+number(i+1);return '<article class="card reader-journey"><h2>'+badge(ordinal)+escape(journey.title)+'</h2>'+'<details class="reader-flow"><summary>Behavior map and legend</summary>'+renderReaderGraph(flow.graph,{title:journey.title,legend:'Arrows show declared transitions between intent, operation and possible outcomes. The case cards explain the reviewed observations; other behavior remains outside this scope.'})+'</details>'+presentationTable(['Reader question','Project behavior'],[['Who',journey.actor],['Purpose',journey.intent],['What happens',journey.operation],['Expected result',journey.outcome]])+(flow.caseAnalysis.negativeScope.status!=='REVIEWED'?paragraph('The reviewed failure scenarios are partial; other failure behavior has not been established.'):'')+'<div class="reader-grid">'+journey.cases.map((item,j)=>{const source=flow.caseAnalysis.cases.find(c=>c.id===item.caseId),label=source.kind==='POSITIVE'?'Expected success':source.kind==='NEGATIVE'?'Expected refusal or failure':'Boundary case';const proof=caseProof(source,claims);return '<article class="reader-case"><p class="eyebrow">'+escape(label)+'</p><h3>'+badge(ordinal+'.'+number(j+1))+escape(item.title)+'</h3>'+paragraph(item.situation)+'<p class="reader-proof'+(proof==='Recorded check failed'?' reader-proof-failed':'')+'">'+escape(proof)+'</p>'+'<p><strong>What to observe:</strong> '+escape(item.expectation)+'</p><p class="muted"><strong>Boundary:</strong> '+escape(item.limits)+'</p></article>';}).join('')+'</div></article>';}).join(''),
    architecture+'<h2>'+escape(r.model.title)+'</h2>'+modelSvg(r.model),
    (analysis.claims.some(c=>c.status==='EXECUTED'&&c.verdict==='FAILED')?card('A retained check did not meet expectations','The available evidence includes a failure. Read the affected observations and boundaries before relying on the described behavior.','05.01'):'')+paragraph('This understanding combines source review and the existing checks that were actually available. It does not establish every possible behavior or every deployment.')+presentationTable(['Basis','What it means'],[['Checked behavior',analysis.claims.some(c=>c.status==='EXECUTED')?'Some described behavior is supported by retained execution; the stated boundaries still apply.':'This capture describes reviewed behavior; it does not claim a fresh execution.'],['Review scope',r.review.scope],['Review boundary',r.review.limits]])+breadth+(unknownAreas.length?'<h3>Areas not yet established</h3>'+presentationTable(['Capability','Remaining question'],unknownAreas):'')+paragraph(analysis.assessment?'Any separate authored quality assessment retains its own scope.':'This document does not assign a project-quality grade.')+paragraph('Technical construction and audit records are retained separately. Reading this document does not require them.'),
    r.limits.map((item,i)=>card(item.title,item.text,'06.'+number(i+1))).join('')];
  const titles=['Purpose and value','What the project enables','Behavior and meaningful cases','How the project works together','Confidence and review scope','Boundaries and remaining questions'];
  const sections=businessSections.map((id,i)=>'<section id="'+id+'" data-atlas-section="'+id+'" data-section-number="'+number(i+1)+'"><details'+(i===0?' open':'')+'><summary>'+badge(number(i+1))+titles[i]+'</summary><div class="section-body" id="'+id+'-content">'+bodies[i]+'</div></details></section>').join('');
  return '<!doctype html>\n<html lang="en" data-atlas-reader="5" data-atlas-calibration="1" data-project-id="'+escape(analysis.projectId)+'"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+escape(r.title)+' · project understanding</title><style id="atlas-style">'+css+'</style></head><body><div class="wrap reader"><header class="hero"><p class="eyebrow">Atlas · project understanding</p><h1>'+escape(r.title)+'</h1><p>Purpose, behavior, confidence and boundaries.</p><div class="pill-row"><span class="pill">Reviewed '+escape(r.reviewDate)+'</span><span class="pill">English</span></div></header><nav aria-label="Project understanding sections">'+businessSections.map((id,i)=>'<a href="#'+id+'-content">'+badge(number(i+1))+titles[i]+'</a>').join('')+'</nav><svg width="0" height="0" aria-hidden="true"><defs><marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto"><path d="M0,0 L10,5 L0,10 z"/></marker></defs></svg>'+sections+'<footer><p>Read this document offline. Conclusions retain the scope and limits stated above.</p></footer></div></body></html>\n';
}
