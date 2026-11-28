import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
import {presentationTable} from '../../tools/atlas-presentation.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message),unique=a=>Array.isArray(a)&&new Set(a).size===a.length,words=s=>typeof s==='string'?s.trim().split(/\s+/).length:Infinity;
const escape=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
export const conciseProfile='CONCISE_READING_V1';

/** Exact repeated narrative is editable noise; similar meanings still require a reviewer. */
export function reviewNarrativeRepetition(reader){
  need(reader&&typeof reader.summary==='string'&&Array.isArray(reader.value)&&reader.value.every(v=>v&&typeof v.text==='string')&&Array.isArray(reader.guide)&&reader.guide.every(g=>g&&typeof g.answer==='string'),'Declare the actual summary, value statements and guide answers before reviewing repetition');
  const fields=[['summary',reader.summary],...reader.value.map((v,i)=>['value['+i+']',v.text]),...reader.guide.map((g,i)=>['guide['+i+']',g.answer])],seen=new Map();
  for(const [owner,text]of fields)for(const sentence of text.split(/(?<=[.!?])\s+/)){
    const key=sentence.toLowerCase().replace(/[.!?]+$/,'').replace(/\s+/g,' ').trim();if(words(key)<6)continue;
    need(!seen.has(key),'Remove the exact repeated business sentence in '+owner+'; explain its distinct contribution instead of repeating '+seen.get(key));seen.set(key,owner);
  }
  return {status:'PASSED',scope:'EXACT_NARRATIVE_SENTENCES; NO_FUZZY_DEDUPLICATION_OR_SEMANTIC_TRUTH'};
}

export function failureScopeLabel(status){
  const labels={REVIEWED:'Failure scenarios: reviewed within stated scope',PARTIAL:'Failure scenarios: partial review',UNKNOWN:'Failure scenarios: not established',NOT_APPLICABLE:'Failure scenarios: not applicable in reviewed scope'};
  need(Object.hasOwn(labels,status),'Keep the actual declared failure review status');return labels[status];
}

/** Concise answers retain actual source relationships; schema checks do not establish prose truth. */
export function readingProjection(analysis){
  const r=analysis.reader;if(r.readingProfile===undefined){need(r.guide===undefined,'Declare concise reading before adding its guide');return null;}
  need(r.readingProfile===conciseProfile&&r.presentationProfile==='BUSINESS_REFERENCE_V2','Use the current concise reading contract with the fitted reference');
  need(Array.isArray(r.guide)&&r.guide.length>=2&&r.guide.length<=5,'Use two to five useful reader questions, not another exhaustive inventory');
  need(words(r.review.scope)<=40&&words(r.review.limits)<=40,'Keep the confidence scope and boundary within 40 words each; move detailed inventories into their owned capability or case sections');
  const claims=new Map(analysis.claims.map(c=>[c.id,c]));
  const rows=r.guide.map((g,index)=>{
    const context=' (reader.guide['+index+'])';
    need(g&&Object.keys(g).every(k=>['question','answer','limits','flowIds','claimIds','oracleClaimIds'].includes(k)),'Keep guide entries within their owned reading fields'+context);
    need([g.question,g.answer,g.limits].every(s=>typeof s==='string'&&s.trim().length>=20)&&words(g.question)<=16&&words(g.answer)<=40&&words(g.limits)<=30,'Keep each guide question concise: at most 16 question, 40 answer and 30 boundary words'+context);
    need(unique(g.flowIds)&&g.flowIds.length>0&&g.flowIds.every(id=>analysis.behaviorFlows.some(f=>f.id===id)),'Guide answers need their actual existing flow owners'+context);
    const flows=g.flowIds.map(id=>analysis.behaviorFlows.find(f=>f.id===id)),owned=new Set(flows.flatMap(f=>f.claimIds||[]));
    need(unique(g.claimIds)&&g.claimIds.length>0&&g.claimIds.every(id=>owned.has(id)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(id)?.status))&&flows.every(f=>(f.claimIds||[]).some(id=>g.claimIds.includes(id))),'Guide source claims must belong to every selected existing flow'+context);
    const oracles=new Set(flows.flatMap(f=>f.caseAnalysis.cases.map(c=>c.oracleClaimId)));
    need(unique(g.oracleClaimIds)&&g.oracleClaimIds.length>0&&g.oracleClaimIds.every(id=>oracles.has(id)&&['SOURCE_REVIEWED','EXECUTED','UNKNOWN'].includes(claims.get(id)?.status))&&flows.every(f=>f.caseAnalysis.cases.some(c=>g.oracleClaimIds.includes(c.oracleClaimId))),'Retain an actual independent oracle or declared gap for every selected guide flow'+context);
    return [g.question,g.answer,g.limits];
  });
  need(new Set(rows.map(r=>r[0].toLowerCase().trim())).size===rows.length,'Remove repeated reader questions instead of duplicating their answers');
  reviewNarrativeRepetition(r);
  return {rows,scope:'DECLARED_FLOW_AND_ORACLE_LINKS; HUMAN_MEANING_REVIEW_REQUIRED'};
}

/** Only exactly shared boundaries are displayed once; distinct limits are never merged. */
export function referenceTable(rows){
  need(Array.isArray(rows)&&rows.length>0&&rows.every(r=>Array.isArray(r)&&r.length===3),'Local references need three original logical fields');
  const shared=rows.length>1&&rows.every(r=>r[2]===rows[0][2]);
  return presentationTable(shared?['Reference','Support for this view']:['Reference','Support for this view','Remaining boundary'],shared?rows.map(r=>r.slice(0,2)):rows)+(shared?'<p class="references-shared-limit"><strong>Shared boundary:</strong> <span class="shared-limit-text">'+escape(rows[0][2])+'</span></p>':'');
}

export function renderReadingGuide(projection){return '<div class="reader-guide"><h3>Reader questions</h3>'+presentationTable(['Reader question','Supported answer','Remaining boundary'],projection.rows)+'</div>';}

/** Keep review meaning visible and clean capture provenance in its own optional disclosure. */
export function renderConfidenceReview(review,date,capture){
  const state=capture?'<p class="reader-source-state" data-atlas-capture-state="'+escape(capture.state)+'">'+escape(capture.meaning)+'</p>':'';
  return '<div class="reader-confidence">'+presentationTable(['Review question','Answer'],[['What was reviewed',review.scope],['What remains unproven',review.limits]])+'</div>'+(capture?.state==='DIRTY'?state:'')+'<details class="reader-evidence-basis"><summary>Review basis</summary>'+presentationTable(['Basis','Recorded value'],[['Review date',date]])+(capture?.state==='CLEAN'?state:'')+'</details>';
}

export function reviewConfidenceReview(metrics,expected){
  const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
  return metrics&&same(metrics.rows,expected.rows)&&metrics.date===expected.date&&metrics.basisCount===1&&metrics.basisOpen===0&&metrics.looseParagraphs===0&&Array.isArray(metrics.columns)&&metrics.columns.length===2&&metrics.columns.every(Number.isFinite)&&metrics.columns[0]>=18&&metrics.columns[0]<=30&&Math.abs(metrics.columns[0]+metrics.columns[1]-100)<=1&&same(metrics.capture,expected.capture)?[]:[{code:'CONFIDENCE_REVIEW',observation:'Present a short review answer and its unproven boundary in the confidence table. Keep the original date and clean-source provenance inside one initially closed Review basis; a dirty-source warning stays outside it. Preserve the exact source state and visible failed checks.'}];
}

/** Outer gutters are intentional; each diagram still fills the inner reading frame. */
export function reviewReaderFrame(frame,viewport){
  const finite=frame&&['x','width','right'].every(k=>Number.isFinite(frame[k]));
  return finite&&Math.abs(frame.right-frame.x-frame.width)<=2&&frame.x>=24&&viewport-frame.right>=24&&Math.abs(frame.x-(viewport-frame.right))<=2&&frame.width>=Math.min(viewport*.84,1680)&&frame.width<=Math.min(viewport*.94,1680)+2&&frame.background==='rgb(11, 15, 20)'&&frame.backgroundImage==='none'?[]:[{code:'READER_FRAME',observation:'Center the reading frame with small symmetric dark gutters (90% width, maximum 1680 CSS px). Measure both edges and the actual solid background; do not stretch prose to the viewport or narrow an individual diagram inside its panel.'}];
}

/** Cases compare along rows; detailed comparison remains local and initially closed. */
export function renderCaseTable(journey,flow,ordinal,claims,comparisonMarkup){
  const shared=journey.cases.length>1&&journey.cases.every(c=>c.limits===journey.cases[0].limits);
  const body=journey.cases.map((c,i)=>{const source=flow.caseAnalysis.cases.find(w=>w.id===c.caseId);need(source,'A concise case must retain its actual original witness: '+c.caseId);const oracle=claims.get(source.oracleClaimId);need(source.oracleStatus!=='EXECUTED'||oracle?.status==='EXECUTED'&&['PASSED','FAILED'].includes(oracle.verdict),'A concise executed case needs its original recorded oracle verdict: '+c.caseId);const proof=source.oracleStatus==='EXECUTED'?(oracle.verdict==='FAILED'?'Recorded check failed':'Recorded check passed'):source.oracleStatus==='NOT_FOUND'?'No existing check identified':'Check reviewed in source; no execution inferred',kind=source.kind==='POSITIVE'?'Expected success':source.kind==='NEGATIVE'?'Expected refusal or failure':'Boundary case',comparison=comparisonMarkup(source);
    return '<tbody class="reader-case"><tr><td><p class="eyebrow">'+escape(kind)+'</p><h3><span class="section-number">'+ordinal+'.'+String(i+1).padStart(2,'0')+'</span>'+escape(c.title)+'</h3></td><td><p class="case-situation">'+escape(c.situation)+'</p><p><strong>Observe:</strong> <span class="case-expectation">'+escape(c.expectation)+'</span></p></td><td><p class="reader-proof'+(proof==='Recorded check failed'?' reader-proof-failed':'')+'">'+escape(proof)+'</p>'+(!shared?'<p class="muted"><strong>Boundary:</strong> <span class="case-limit">'+escape(c.limits)+'</span></p>':'')+'</td></tr>'+(comparison?'<tr class="reader-comparison-row"><td colspan="3">'+comparison+'</td></tr>':'')+'</tbody>';
  }).join('');
  return '<div class="table-scroll atlas-table"><table class="reader-case-table"><colgroup><col style="width:29%"><col style="width:39%"><col style="width:32%"></colgroup><thead><tr><th scope="col">Case</th><th scope="col">Situation and expected result</th><th scope="col">Evidence and boundary</th></tr></thead>'+body+'</table></div>'+(shared?'<p class="reader-shared-limit"><strong>Shared boundary:</strong> <span class="shared-limit-text">'+escape(journey.cases[0].limits)+'</span></p>':'');
}

export function reviewConciseReading(metrics,width,expected){
  const findings=[],add=(code,observation)=>findings.push({code,observation}),same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
  if(!metrics||!Number.isFinite(metrics.width)||metrics.width<Math.min(width*.84,1680)||metrics.width>Math.min(width*.94,1680)+2)add('READING_WIDTH','Use a generous centered reading frame with small dark side gutters; keep every primary and supporting map full width within that frame, with readable native type');
  if(!metrics||!same(metrics.guide,expected.guide)||!same(metrics.journeys,expected.journeys)||expected.brief&&!same(metrics.brief,expected.brief))add('READING_ORIENTATION','Retain the exact concise source-bound answers, project brief and complete behavior summary; do not substitute generic filler or erase an existing behavior');
  if(!metrics||metrics.openJourneys!==0||metrics.visibleCases!==0||metrics.openAxes!==0||metrics.openReviewDepth!==0||metrics.reviewDepthGroups!==1)add('READING_DISCLOSURE','Start with the useful summary: group detailed capability review once and keep it, journey cases and individual score rationale closed until requested');
  return findings;
}

export function reviewConciseReadingPath(path,targets,guideCount,journeyCount){
  const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
  return Array.isArray(path)&&same(path.map(p=>p.target),targets)&&path[0]?.briefVisible===true&&path[1]?.guideRows===guideCount&&path[2]?.summaryRows===journeyCount&&path[2]?.visibleCases===0?[]:[{code:'READING_PATH',observation:'Actual section navigation must reveal the project brief, source-bound guide and complete behavior summary while keeping detailed cases closed; inspect the normal reading path as well as the fully expanded audit.'}];
}

/** A wide page is insufficient: the actual main canvas must own the full content band. */
export function reviewPrimaryBand(band,noise){
  const findings=[],rectangle=b=>b&&['x','y','width','height'].every(k=>Number.isFinite(b[k]))&&b.width>0&&b.height>0;
  if(!band||!['section','brief','primary','figure','canvas'].every(k=>rectangle(band[k]))||band.brief.y+band.brief.height>band.primary.y+2||['primary','figure','canvas'].some(k=>band[k].width<band.section.width*.94||band[k].x+band[k].width>band.section.x+band.section.width+2||Math.abs(band[k].x-band.section.x)>band.section.width*.04))findings.push({code:'PRIMARY_BAND',observation:'Give the open main map its own full-width content band with a short brief above it. Measure the actual canvas as well as its container; remove sidebars and fixed narrow caps without stretching type.'});
  if(!noise||noise.footers!==0||noise.scoreHints!==0||!Array.isArray(noise.unownedText)||noise.unownedText.length)findings.push({code:'READER_NOISE',observation:'Remove generic footers, redundant score instructions and unowned narrative outside dedicated sections. Keep confidence and scope in their existing owned sections rather than repeating global labels.'});
  return findings;
}
