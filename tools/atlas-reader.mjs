import {referencePresentationProjection,referenceBusinessText,renderReferenceScores,renderReferenceRationale,renderPrimaryProof} from './atlas-reference.mjs';
import {requireAtlas} from './atlas-diagnostics.mjs';
import {presentationTable} from './atlas-presentation.mjs';
import {renderReaderGraph,readerGraphSet,validateReaderDiagramProfile} from './atlas-reader-graphs.mjs';
import {facetProofProjection} from './atlas-semantics.mjs';
import {validateAtlasCaptureDeclaration} from './verify-atlas.mjs';
import {readingProjection,renderReadingGuide,renderCaseTable,failureScopeLabel,renderConfidenceReview} from '../atf-atlas/governance/reading.mjs';

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

export function readerComparisonProjection(witness){
  if(witness.comparison===undefined||witness.comparison===null)return null;
  const c=witness.comparison,rules={EQUALITY:'Matches the expected value',PREDICATE:'Meets the expected condition',FAILURE_IDENTITY:'Preserves the original failure',ORDER:'Preserves the required order',OWNERSHIP:'Preserves the owning context',CUSTOM:'Applies the reviewed rule'};
  need(object(c)&&typeof c.relation==='string'&&Object.hasOwn(rules,c.relation),'A visible comparison needs its supported source-reviewed rule: '+witness.id);
  for(const key of ['observedSubject','expectedCriterion','failureMeaning'])prose(c[key],'comparison '+witness.id+'/'+key);
  if(c.relation==='CUSTOM')prose(c.explanation,'comparison custom rule');
  return {observedSubject:c.observedSubject,expectedCriterion:c.expectedCriterion,rule:rules[c.relation],failureMeaning:c.failureMeaning,explanation:c.relation==='CUSTOM'?c.explanation:''};
}

export function readerCaptureProjection(index){
  const capture=validateAtlasCaptureDeclaration(index||{});if(capture.status!=='PASSED')return null;
  return {state:capture.state,meaning:capture.state==='DIRTY'?'The reviewed working copy contains unpublished local changes; conclusions belong to that captured state.':'The reviewed source was committed with no local source changes at capture time.'};
}

function comparisonMarkup(witness){
  const c=readerComparisonProjection(witness);if(!c)return '';
  return '<details class="reader-comparison"><summary>How this check decides</summary><dl><dt>What the check reads</dt><dd class="comparison-subject">'+escape(c.observedSubject)+'</dd><dt>Expected criterion</dt><dd class="comparison-criterion">'+escape(c.expectedCriterion)+'</dd><dt>Decision rule</dt><dd class="comparison-rule">'+escape(c.rule)+'</dd><dt>What a mismatch means</dt><dd class="comparison-failure">'+escape(c.failureMeaning)+'</dd>'+(c.explanation?'<dt>Reviewed comparison</dt><dd class="comparison-explanation">'+escape(c.explanation)+'</dd>':'')+'</dl></details>';
}

function diagramChoice(view){
  need(view.diagramMode===undefined||['SOURCE_GRAPH','TEXT_ONLY'].includes(view.diagramMode),'Use a source graph or an explicit prose explanation');
  if(view.diagramMode==='TEXT_ONLY')prose(view.diagramReason,'why this responsibility is clearer without a diagram');
}

/** Publication-envelope inspection is deliberately weaker than re-rendering with the original adjacent audit. */
export function inspectBusinessReaderHtml(html,{projectId}={}){
  need(typeof html==='string'&&/<html\b[^>]*data-atlas-reader="5"/.test(html),'Declare the current business reader version');
  need(!/<(?:template|script|pre|code|iframe|object)\b/i.test(html),'The business HTML contains hidden or technical payload elements');
  if(projectId!==undefined)requireAtlas(html.includes('data-project-id="'+escape(projectId)+'"'),'ATLAS-TARGET','The business reader belongs to another selected project');
  const sections=[...html.matchAll(/<section id="([^"]+)" data-atlas-section="([^"]+)" data-section-number="([^"]+)"/g)];
  need(sections.length===businessSections.length&&sections.every((m,i)=>m[1]===businessSections[i]&&m[2]===m[1]&&m[3]===String(i+1).padStart(2,'0')),'Reader section identities, order or numbering differ');
  need(!html.includes('data-atlas-layout=')||html.includes('data-atlas-layout="primary-band-1"')&&html.includes('data-atlas-reading="concise-1"'),'Use the declared primary-band layout with its concise reader');
  need(!html.includes('data-atlas-surface=')||html.includes('data-atlas-surface="framed-1"')&&html.includes('data-atlas-reading="concise-1"'),'Use the declared framed surface with its concise reader; native review still measures the actual gutters');
  if(html.includes('data-atlas-layout="primary-band-1"'))need(!/<footer\b/.test(html)&&!html.includes('Open a score for its review basis and limits.'),'Remove generic footer and score instructions; use dedicated confidence and boundary sections');
  const calibrated=html.includes('data-atlas-calibration="1"');
  need(!html.includes('data-atlas-calibration=')||calibrated,'Unsupported artifact calibration profile');
  const navigation=/<nav\b[^>]*>[\s\S]*?<\/nav>/.exec(html)?.[0]||'';
  const links=[...(html.includes('data-atlas-presentation=')?navigation:html).matchAll(/href="([^"]+)"/g)].map(m=>m[1]);
  need(links.length===businessSections.length&&links.every((href,i)=>href==='#'+businessSections[i]+(calibrated?'-content':'')),'Reader navigation must mirror the numbered local sections');
  if(html.includes('data-atlas-presentation=')){need(/data-atlas-presentation="business-reference-[12]"/.test(html),'Use the declared reference presentation envelope');for(const match of html.matchAll(/href="([^"]+)"/g)){const href=match[1];need(links.includes(href)||/^#score-[a-z0-9-]+$/.test(href),'Reference links must remain local reviewed score or navigation targets');need((html.match(new RegExp('id="'+href.slice(1)+'"','g'))||[]).length===1,'A score/navigation target must exist exactly once');}}
  if(calibrated)for(const id of businessSections)need((html.match(new RegExp('id="'+id+'-content"','g'))||[]).length===1,'Reader navigation needs one content target inside each disclosure: '+id);
  const prose=html.replace(/<style\b[^>]*>[\s\S]*?<\/style>/gi,'').replace(/<[^>]*>/g,' ');
  need(!technical.test(prose),'Technical construction detail leaked into the published reader');
  return {status:'PASSED',profile:calibrated?'CALIBRATED_V1':'HISTORICAL_V5_ENVELOPE',scope:'READER_ENVELOPE_ONLY; ORIGINAL_AUDIT_AND_SEMANTIC_REVIEW_STILL_REQUIRED'};
}

/** Source bindings and audience checks complement an explicit semantic review; they do not prove prose truth. */
export function validateBusinessReader(analysis){
  const r=analysis.reader,claims=new Map(analysis.claims.map(c=>[c.id,c]));
  shape(r,['schemaVersion','projectId','language','title','summary','reviewDate','review','value','capabilities','journeys','auditOnlyFlows','architecture','model','limits','diagramProfile','comparisonProfile','captureProfile','presentationProfile','primaryGraph','assessment','readingProfile','guide'],'project reader');
  need(r.schemaVersion===1&&r.projectId===analysis.projectId&&r.language==='en','Reader needs the same target, schemaVersion 1 and English authored prose');
  need(r.comparisonProfile===undefined||r.comparisonProfile==='SOURCE_BOUND_COMPARISONS_V1','Use the declared source-bound reader comparison profile');
  need(r.captureProfile===undefined||r.captureProfile==='GIT_WORKTREE_V1','Use the declared source-capture profile');
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
    shape(journey,['flowId','title','actor','intent','operation','outcome','cases','diagramMode','diagramReason'],'journey');diagramChoice(journey);
    for(const key of ['title','actor','intent','operation','outcome'])prose(journey[key],'journey '+key);
    const flow=flows.find(f=>f.id===journey.flowId);
    if(r.comparisonProfile)need(flow.caseAnalysis.comparisonProfile===r.comparisonProfile,'The reader comparison promise needs the same actual case profile: '+flow.id);
    for(const witness of flow.caseAnalysis.cases){if(r.comparisonProfile)need(witness.oracleStatus==='NOT_FOUND'?witness.comparison===null:object(witness.comparison),'Keep the actual comparison or explicit oracle gap visible: '+flow.id+'/'+witness.id);readerComparisonProjection(witness);}
    need(Array.isArray(journey.cases),'Explain reviewed cases');exact(journey.cases.map(c=>c.caseId),(flow.caseAnalysis?.cases||[]).map(c=>c.id),'journey cases');
    for(const item of journey.cases){shape(item,['caseId','title','situation','expectation','limits'],'journey case');for(const key of ['title','situation','expectation','limits'])prose(item[key],'case '+key);}
  }
  need(Array.isArray(r.architecture)&&r.architecture.length>0,'Explain the actual project architecture with source-bound views');
  exact(r.architecture.map(view=>view.graphId),(analysis.dependencyGraphs||[]).map(graph=>graph.id),'architecture graphs');
  for(const view of r.architecture){shape(view,['graphId','title','legend','diagramMode','diagramReason'],'architecture view');prose(view.title,'architecture title');prose(view.legend,'architecture legend');diagramChoice(view);}
  shape(r.model,['title','stages','links','legend','diagramMode','diagramReason'],'operating model');diagramChoice(r.model);prose(r.model.title,'model title');prose(r.model.legend,'model legend');
  need(Array.isArray(r.model.stages)&&r.model.stages.length>0&&r.model.stages.length<=3&&Array.isArray(r.model.links),'Use a small source-bound operating model');
  const stageIds=new Set();for(const stage of r.model.stages){shape(stage,['id','title','claimIds'],'model stage');need(/^[a-z][a-z0-9-]*$/.test(stage.id)&&!stageIds.has(stage.id),'Model stages need unique identities');stageIds.add(stage.id);prose(stage.title,'model stage');need(labelLines(stage.title).length<=2&&stage.title.split(' ').every(word=>word.length<=18),'Use a concise two-line diagram label');refs(stage.claimIds,claims,'model stage');}
  for(const link of r.model.links){shape(link,['from','to','label','claimIds'],'model relationship');need(stageIds.has(link.from)&&stageIds.has(link.to)&&link.from!==link.to,'Model relationship has an unknown or repeated endpoint');prose(link.label,'model relationship');need(link.label.length<=24,'Use a concise relationship label');refs(link.claimIds,claims,'model relationship');}
  need(r.model.links.length===r.model.stages.length-1&&r.model.links.every((link,i)=>link.from===r.model.stages[i].id&&link.to===r.model.stages[i+1].id),'The reader model must follow its declared adjacent reading stages; keep detailed branches in the audit');
  need(Array.isArray(r.limits)&&r.limits.length>0,'Keep remaining boundaries visible');r.limits.forEach(item=>statement(item,claims,'reader limit',true));
  const covered=new Set(r.limits.flatMap(l=>l.claimIds));for(const claim of claims.values())if(claim.status==='UNKNOWN'||claim.status==='EXECUTED'&&claim.verdict==='FAILED')need(covered.has(claim.id),'An unknown source claim is absent from the visible reader limits: '+claim.id);
  const reference=referencePresentationProjection(analysis);if(!reference)need(r.primaryGraph===undefined&&r.assessment===undefined,'Declare the reference presentation before adding a primary map or visible score projection');
  for(const value of referenceBusinessText(reference))prose(value,'reference presentation');if(reference)for(const edge of r.primaryGraph.edges)prose(edge.label,'primary relationship');
  const reading=readingProjection(analysis);if(reading)for(const text of reading.rows.flat())prose(text,'concise reader guide');
  validateReaderDiagramProfile(analysis);
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
  return statuses.includes('PARTIAL')?'Partial review':unit.facets.purpose?.review?'Reviewed within stated scope':'Declared aspects reviewed';
}
const paragraph=text=>'<p>'+escape(text)+'</p>';
const number=value=>String(value).padStart(2,'0');
const badge=value=>'<span class="section-number">'+value+'</span>';
const card=(title,text,label)=>'<article class="card"><h3>'+(label===undefined?'':badge(label))+escape(title)+'</h3>'+paragraph(text)+'</article>';
/** Only the audience projection reaches HTML; raw source, commands and proof payloads stay in the adjacent audit. */
export function renderBusinessReader(bundle,css){
  const analysis=bundle.analysis,r=analysis.reader;validateBusinessReader(analysis);const reference=referencePresentationProjection(analysis),reading=readingProjection(analysis);
  const captureState=readerCaptureProjection(bundle.sourceIndex);if(r.captureProfile)need(bundle.sourceIndex?.captureProfile===r.captureProfile&&captureState,'The reader capture promise needs its original source-index declaration');
  const flowById=new Map(analysis.behaviorFlows.map(f=>[f.id,f])),claims=new Map(analysis.claims.map(c=>[c.id,c])),diagramViews=readerGraphSet(analysis);
  const breadth=reading?'':'<h3>Review depth by capability</h3><div class="reader-breadth">'+presentationTable(['Capability','Source review'],analysis.understanding.units.map(unit=>[r.capabilities.find(c=>c.unitId===unit.id).title,reviewDepth(unit)]))+'</div>'+paragraph('Review depth concerns purpose, ownership, behavior, information, setup, failure handling and available checks. Partial review means selected aspects were inspected; passing checks do not complete unreviewed areas.');
  const unknownAreas=(analysis.understanding?.units||[]).flatMap(unit=>{const missing=Object.entries(unit.facets||{}).filter(([,facet])=>facet.status==='UNKNOWN').map(([name])=>({purpose:'purpose',structure:'ownership',behavior:'behavior',data:'information',configuration:'setup',failure:'failure handling',verification:'available checks'}[name]));return missing.length?[[r.capabilities.find(c=>c.unitId===unit.id).title,missing.join(', ')]]:[];});
  const semantic=analysis.understanding.reviewProfile==='SEMANTIC_FACETS_V1',facetNames={purpose:'Purpose',structure:'Ownership',behavior:'Behavior',data:'Information',configuration:'Setup',failure:'Failure handling',verification:'Existing verification'};
  const facetDetails=item=>{
    if(!semantic)return '';const unit=analysis.understanding.units.find(u=>u.id===item.unitId);
    const rows=Object.entries(unit.facets).map(([key,facet])=>{
      const boundary=facetProofProjection(analysis,unit.id,key);
      for(const text of [facet.explanation,facet.review.scope,facet.review.limits,...boundary.nonClaims])prose(text,'business facet '+key);
      const status={REVIEWED:'Reviewed within stated scope',PARTIAL:'Partial review',UNKNOWN:'Not established',NOT_APPLICABLE:'Not applicable'}[facet.status];
      for(const q of boundary.openQuestions){prose(q.question);prose(q.evidenceNeeded);}
      const questions=boundary.openQuestions.length?'<details class="facet-questions"><summary>Remaining questions</summary><dl>'+boundary.openQuestions.map(q=>'<dt data-question-id="'+escape(q.id)+'">'+escape(q.question)+'</dt><dd>'+escape(q.evidenceNeeded)+'</dd>').join('')+'</dl></details>':'';
      const heading='<h4>'+facetNames[key]+'</h4><p class="facet-status">'+status+'</p>',meaning='<p class="facet-meaning">'+escape(facet.explanation)+'</p>',limit='<p class="facet-limit">'+escape(facet.review.limits)+'</p>'+(boundary.proof?'<p class="facet-proof">'+escape(boundary.proof)+'</p><ul class="facet-nonclaims">'+boundary.nonClaims.map(v=>'<li>'+escape(v)+'</li>').join('')+'</ul>':'')+questions,identity=' class="reader-facet" data-capability="'+escape(item.title)+'" data-facet="'+key+'"';
      return reading?'<tr'+identity+'><th scope="row">'+heading+'</th><td>'+meaning+'</td><td>'+limit+'</td></tr>':'<article'+identity+'>'+heading+meaning+limit+'</article>';
    }).join('');
    return '<details class="reader-facets"><summary>'+(reading?'Review detail: '+escape(item.title):'Understanding and boundaries')+'</summary>'+(reading?'<div class="table-scroll atlas-table"><table class="reader-facet-table"><colgroup><col style="width:19%"><col style="width:36%"><col style="width:45%"></colgroup><thead><tr><th>Aspect</th><th>What was understood</th><th>Evidence and remaining boundary</th></tr></thead><tbody>'+rows+'</tbody></table></div>':rows)+'</details>';
  };
  const architecture=r.architecture.map(view=>{if(view.diagramMode==='TEXT_ONLY')return card(view.title,view.diagramReason);const projected=diagramViews.find(v=>v.graph.id===view.graphId);return '<h3>'+escape(view.title)+'</h3>'+renderReaderGraph(projected.graph,projected);}).join('');
  const values='<div class="reader-grid">'+r.value.map((item,i)=>card(item.title,item.text,'01.'+number(i+(reference?2:1)))).join('')+'</div>';
  const main=reference?'<details class="reader-primary" open><summary>'+badge('01.01')+escape(reference.primary.title)+'</summary>'+(reading?'':paragraph(reference.primary.summary))+renderReaderGraph(diagramViews[0].graph,diagramViews[0])+(r.presentationProfile==='BUSINESS_REFERENCE_V2'?'':renderPrimaryProof(reference))+'</details>':'';
  const bodies=reading?[]:[(reference?'':paragraph(r.summary))+main+(reference?'<details class="reader-value"><summary>Value and context</summary>'+paragraph(r.summary)+values+'</details>':values),
    '<div class="reader-grid">'+r.capabilities.map((item,i)=>'<article class="card"><h3>'+badge('02.'+number(i+1))+escape(item.title)+'</h3>'+paragraph(item.benefit)+facetDetails(item)+'</article>').join('')+'</div>',
    r.journeys.map((journey,i)=>{const flow=flowById.get(journey.flowId),ordinal='03.'+number(i+1);return '<article class="card reader-journey"><h2>'+badge(ordinal)+escape(journey.title)+'</h2>'+(journey.diagramMode==='TEXT_ONLY'?paragraph(journey.diagramReason):'<details class="reader-flow"><summary>Behavior map and legend</summary>'+renderReaderGraph(diagramViews.find(view=>view.flowId===flow.id).graph,diagramViews.find(view=>view.flowId===flow.id))+'</details>')+presentationTable(['Reader question','Project behavior'],[['Who',journey.actor],['Purpose',journey.intent],['What happens',journey.operation],['Expected result',journey.outcome]])+(flow.caseAnalysis.negativeScope.status!=='REVIEWED'?paragraph('The reviewed failure scenarios are partial; other failure behavior has not been established.'):'')+'<div class="reader-grid">'+journey.cases.map((item,j)=>{const source=flow.caseAnalysis.cases.find(c=>c.id===item.caseId),label=source.kind==='POSITIVE'?'Expected success':source.kind==='NEGATIVE'?'Expected refusal or failure':'Boundary case';const proof=caseProof(source,claims);return '<article class="reader-case"><p class="eyebrow">'+escape(label)+'</p><h3>'+badge(ordinal+'.'+number(j+1))+escape(item.title)+'</h3>'+paragraph(item.situation)+'<p class="reader-proof'+(proof==='Recorded check failed'?' reader-proof-failed':'')+'">'+escape(proof)+'</p>'+'<p><strong>What to observe:</strong> '+escape(item.expectation)+'</p><p class="muted"><strong>Boundary:</strong> '+escape(item.limits)+'</p>'+comparisonMarkup(source)+'</article>'; }).join('')+'</div></article>';}).join(''),
    architecture+'<h2>'+escape(r.model.title)+'</h2>'+(r.model.diagramMode==='TEXT_ONLY'?paragraph(r.model.diagramReason):renderReaderGraph(diagramViews.find(v=>v.graph.id==='reader-value-model').graph,diagramViews.find(v=>v.graph.id==='reader-value-model'))),
    (analysis.claims.some(c=>c.status==='EXECUTED'&&c.verdict==='FAILED')?card('A retained check did not meet expectations','The available evidence includes a failure. Read the affected observations and boundaries before relying on the described behavior.','05.01'):'')+paragraph('This understanding combines source review and the existing checks that were actually available. It does not establish every possible behavior or every deployment.')+(captureState?'<p class="reader-source-state" data-atlas-capture-state="'+captureState.state+'">'+escape(captureState.meaning)+'</p>':'')+presentationTable(['Basis','What it means'],[['Review date',r.reviewDate],['Checked behavior',analysis.claims.some(c=>c.status==='EXECUTED')?'Some described behavior is supported by retained execution; the stated boundaries still apply.':'This capture describes reviewed behavior; it does not claim a fresh execution.'],['Review scope',r.review.scope],['Review boundary',r.review.limits]])+renderReferenceRationale(reference)+breadth+(unknownAreas.length?'<h3>Areas not yet established</h3>'+presentationTable(['Capability','Remaining question'],unknownAreas):'')+paragraph(analysis.assessment?'Any separate authored quality assessment retains its own scope.':'This document does not assign a project-quality grade.')+paragraph('Technical construction and audit records are retained separately. Reading this document does not require them.'),
    r.limits.map((item,i)=>card(item.title,item.text,'06.'+number(i+1))).join('')];
  if(reading){
    bodies[0]='<div class="reader-overview"><div class="reader-brief">'+paragraph(r.summary)+'</div>'+main+'<details class="reader-value"><summary>Value and context</summary>'+paragraph(reference.primary.summary)+presentationTable(['Value','Why it matters'],r.value.map(v=>[v.title,v.text]))+'</details></div>';
    bodies[1]=renderReadingGuide(reading)+'<h3>Responsibilities at a glance</h3><div class="reader-breadth">'+presentationTable(['Capability','Contribution','Review'],r.capabilities.map(c=>[c.title,c.benefit,reviewDepth(analysis.understanding.units.find(u=>u.id===c.unitId))]))+'</div><details class="reader-review-depth"><summary>Review detail and remaining questions</summary><div class="reader-capability-details">'+r.capabilities.map(facetDetails).join('')+'</div></details>';
    const journeyRows=r.journeys.map((j,i)=>{const f=flowById.get(j.flowId),ordinal='03.'+number(i+1),view=diagramViews.find(v=>v.flowId===f.id),graph=j.diagramMode==='TEXT_ONLY'?paragraph(j.diagramReason):renderReaderGraph(view.graph,view),context='<p class="reader-caller"><strong>Caller:</strong> '+escape(j.actor)+' · <strong>Purpose:</strong> '+escape(j.intent)+'</p>';
      return '<tbody class="reader-journey"><tr class="reader-journey-summary"><th scope="row">'+badge(ordinal)+escape(j.title)+'</th><td>'+escape(j.operation)+'</td><td>'+escape(j.outcome)+'</td></tr><tr><td colspan="3"><details class="reader-journey-details"><summary>Cases, map and context</summary>'+context+graph+'<p class="reader-negative-scope" data-negative-scope="'+escape(f.caseAnalysis.negativeScope.status)+'">'+escape(failureScopeLabel(f.caseAnalysis.negativeScope.status))+'</p>'+renderCaseTable(j,f,ordinal,claims,comparisonMarkup)+'</details></td></tr></tbody>';
    }).join('');
    bodies[2]='<p>Compare the existing behaviors below. Open only the cases or map needed for the current question.</p><div class="table-scroll atlas-table"><table class="reader-journey-table"><colgroup><col style="width:24%"><col style="width:38%"><col style="width:38%"></colgroup><thead><tr><th scope="col">Behavior</th><th scope="col">What happens</th><th scope="col">Expected result</th></tr></thead>'+journeyRows+'</table></div>';
    bodies[3]=architecture;
    bodies[4]=(analysis.claims.some(c=>c.status==='EXECUTED'&&c.verdict==='FAILED')?card('A retained check did not meet expectations','The original evidence includes a failure; the affected boundary remains explicit.'): '')+renderConfidenceReview(r.review,r.reviewDate,captureState)+renderReferenceRationale(reference,{concise:true})+(unknownAreas.length?presentationTable(['Capability','Not yet established'],unknownAreas):'');
    bodies[5]='<div class="reader-boundaries">'+presentationTable(['Boundary','What remains outside'],r.limits.map(l=>[l.title,l.text]))+'</div>';
  }
  const titles=['Purpose and value','What the project enables','Behavior and meaningful cases','How the project works together','Confidence and review scope','Boundaries and remaining questions'];
  const sections=businessSections.map((id,i)=>'<section id="'+id+'" data-atlas-section="'+id+'" data-section-number="'+number(i+1)+'"><details'+(i===0?' open':'')+'><summary>'+badge(number(i+1))+titles[i]+'</summary><div class="section-body" id="'+id+'-content">'+bodies[i]+'</div></details></section>').join('');
  const html='<!doctype html>\n<html lang="en"'+(reference?' data-atlas-presentation="business-reference-'+(r.presentationProfile==='BUSINESS_REFERENCE_V2'?'2':'1')+'"':'')+(r.diagramProfile?' data-atlas-diagrams="'+(r.diagramProfile==='SOURCE_GRAPH_V2'?'source-2':'source-1')+'"':'')+(reading?' data-atlas-reading="concise-1" data-atlas-layout="primary-band-1" data-atlas-surface="framed-1"':'')+' data-atlas-identity="compact-1" data-atlas-reader="5" data-atlas-calibration="1" data-project-id="'+escape(analysis.projectId)+'"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+escape(r.title)+' · project understanding</title><style id="atlas-style">'+css+'</style></head><body><div class="wrap reader'+(reference?' reference-reader'+(r.presentationProfile==='BUSINESS_REFERENCE_V2'?' fit-reader'+(reading?' concise-reader':''):''):'')+'"><header class="hero reader-identity"><h1><span class="reader-brand">Atlas</span><span aria-hidden="true"> · </span>'+escape(r.title)+'</h1></header>'+renderReferenceScores(reference,{concise:Boolean(reading)})+'<nav aria-label="Project understanding sections">'+businessSections.map((id,i)=>'<a href="#'+id+'-content">'+badge(number(i+1))+titles[i]+'</a>').join('')+'</nav><svg width="0" height="0" aria-hidden="true"><defs><marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto"><path d="M0,0 L10,5 L0,10 z"/></marker></defs></svg>'+sections+(reading?'':'<footer><p>Read this document offline. Conclusions retain the scope and limits stated above.</p></footer>')+'</div></body></html>\n';
  return r.presentationProfile==='BUSINESS_REFERENCE_V2'?html.replace(/<table style="min-width:\d+px">/g,'<table>'):html;
}
