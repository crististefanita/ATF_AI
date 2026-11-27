import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
import {presentationTable} from '../../tools/atlas-presentation.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message),unique=a=>Array.isArray(a)&&new Set(a).size===a.length,words=s=>typeof s==='string'?s.trim().split(/\s+/).length:Infinity;
const escape=v=>String(v).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;');
export const conciseProfile='CONCISE_READING_V1';

/** Concise answers retain actual source relationships; schema checks do not establish prose truth. */
export function readingProjection(analysis){
  const r=analysis.reader;if(r.readingProfile===undefined){need(r.guide===undefined,'Declare concise reading before adding its guide');return null;}
  need(r.readingProfile===conciseProfile&&r.presentationProfile==='BUSINESS_REFERENCE_V2','Use the current concise reading contract with the fitted reference');
  need(Array.isArray(r.guide)&&r.guide.length>=2&&r.guide.length<=5,'Use two to five useful reader questions, not another exhaustive inventory');
  const claims=new Map(analysis.claims.map(c=>[c.id,c]));
  const rows=r.guide.map((g,index)=>{
    const context=' (reader.guide['+index+'])';
    need(g&&Object.keys(g).every(k=>['question','answer','limits','flowIds','claimIds','oracleClaimIds'].includes(k)),'Keep guide entries within their owned reading fields'+context);
    need([g.question,g.answer,g.limits].every(s=>typeof s==='string'&&s.trim().length>=20)&&words(g.question)<=16&&words(g.answer)<=55&&words(g.limits)<=35,'Keep each guide question concise: at most 16 question, 55 answer and 35 boundary words'+context);
    need(unique(g.flowIds)&&g.flowIds.length>0&&g.flowIds.every(id=>analysis.behaviorFlows.some(f=>f.id===id)),'Guide answers need their actual existing flow owners'+context);
    const flows=g.flowIds.map(id=>analysis.behaviorFlows.find(f=>f.id===id)),owned=new Set(flows.flatMap(f=>f.claimIds||[]));
    need(unique(g.claimIds)&&g.claimIds.length>0&&g.claimIds.every(id=>owned.has(id)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(id)?.status))&&flows.every(f=>(f.claimIds||[]).some(id=>g.claimIds.includes(id))),'Guide source claims must belong to every selected existing flow'+context);
    const oracles=new Set(flows.flatMap(f=>f.caseAnalysis.cases.map(c=>c.oracleClaimId)));
    need(unique(g.oracleClaimIds)&&g.oracleClaimIds.length>0&&g.oracleClaimIds.every(id=>oracles.has(id)&&['SOURCE_REVIEWED','EXECUTED','UNKNOWN'].includes(claims.get(id)?.status))&&flows.every(f=>f.caseAnalysis.cases.some(c=>g.oracleClaimIds.includes(c.oracleClaimId))),'Retain an actual independent oracle or declared gap for every selected guide flow'+context);
    return [g.question,g.answer,g.limits];
  });
  need(new Set(rows.map(r=>r[0].toLowerCase().trim())).size===rows.length,'Remove repeated reader questions instead of duplicating their answers');
  return {rows,scope:'DECLARED_FLOW_AND_ORACLE_LINKS; HUMAN_MEANING_REVIEW_REQUIRED'};
}

/** Only exactly shared boundaries are displayed once; distinct limits are never merged. */
export function referenceTable(rows){
  need(Array.isArray(rows)&&rows.length>0&&rows.every(r=>Array.isArray(r)&&r.length===3),'Local references need three original logical fields');
  const shared=rows.length>1&&rows.every(r=>r[2]===rows[0][2]);
  return presentationTable(shared?['Reference','Support for this view']:['Reference','Support for this view','Remaining boundary'],shared?rows.map(r=>r.slice(0,2)):rows)+(shared?'<p class="references-shared-limit"><strong>Shared boundary:</strong> <span class="shared-limit-text">'+escape(rows[0][2])+'</span></p>':'');
}

export function renderReadingGuide(projection){return '<div class="reader-guide"><h3>Reader questions</h3>'+presentationTable(['Reader question','Supported answer','Remaining boundary'],projection.rows)+'</div>';}

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
  if(!metrics||!Number.isFinite(metrics.width)||metrics.width<width*.9||metrics.width>width+2)add('READING_WIDTH','Use the available page width for meaningful comparisons; retain readable line lengths and a primary-map/context layout instead of empty fixed margins');
  if(!metrics||!same(metrics.guide,expected.guide)||!same(metrics.journeys,expected.journeys)||expected.brief&&!same(metrics.brief,expected.brief))add('READING_ORIENTATION','Retain the exact concise source-bound answers, project brief and complete behavior summary; do not substitute generic filler or erase an existing behavior');
  if(!metrics||metrics.openJourneys!==0||metrics.visibleCases!==0||metrics.openAxes!==0)add('READING_DISCLOSURE','Start with the useful summary: journey cases and individual score rationale open only on request');
  return findings;
}

export function reviewConciseReadingPath(path,targets,guideCount,journeyCount){
  const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
  return Array.isArray(path)&&same(path.map(p=>p.target),targets)&&path[0]?.briefVisible===true&&path[1]?.guideRows===guideCount&&path[2]?.summaryRows===journeyCount&&path[2]?.visibleCases===0?[]:[{code:'READING_PATH',observation:'Actual section navigation must reveal the project brief, source-bound guide and complete behavior summary while keeping detailed cases closed; inspect the normal reading path as well as the fully expanded audit.'}];
}
