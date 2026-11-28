import test from 'node:test';import assert from 'node:assert/strict';
import {readingProjection,referenceTable,reviewConciseReading,renderCaseTable,reviewConciseReadingPath,reviewNarrativeRepetition,failureScopeLabel,reviewPrimaryBand,reviewReaderFrame,reviewConfidenceReview,renderConfidenceReview} from './reading.mjs';
import {referenceFixture} from '../../tools/fixtures/atlas-reference.mjs';
import {renderBusinessReader,validateBusinessReader,inspectBusinessReaderHtml} from '../../tools/atlas-reader.mjs';
const fixture=()=>{const a=referenceFixture();a.reader.presentationProfile='BUSINESS_REFERENCE_V2';a.reader.readingProfile='CONCISE_READING_V1';a.behaviorFlows[0].claimIds=['FACT'];a.reader.guide=['Which observation does the caller receive?','What remains outside this local check?'].map((question,i)=>({question,answer:i?'The available check describes a local boundary without establishing every deployed application behavior.':'The existing operation returns the observation supported by its independent local check.',limits:'The selected local observation does not establish every deployment or input.',flowIds:['operation'],claimIds:['FACT'],oracleClaimIds:['ORACLE']}));return a;};
const owned=fn=>assert.throws(fn,e=>e.code==='ATLAS-READER'&&Boolean(e.hint&&e.owner&&e.recheck));

test('concise orientation retains actual flow and independent oracle ownership without adding a semantic guarantee',()=>{
  const a=fixture(),before=structuredClone(a),r=readingProjection(a);assert.equal(r.rows.length,2);assert.match(r.scope,/HUMAN_MEANING/);assert.deepEqual(a,before);
  for(const change of [g=>g.flowIds=['foreign'],g=>g.claimIds=['ORACLE'],g=>g.oracleClaimIds=['FACT'],g=>g.oracleClaimIds=[]]){const b=fixture();change(b.reader.guide[0]);owned(()=>readingProjection(b));}
});
test('a short reader guide refuses verbose filler, duplicate questions and undeclared fields',()=>{
  for(const change of [r=>r.guide[0].answer='Words '.repeat(41),r=>r.guide[0].limits='Boundary '.repeat(31),r=>r.guide[1].question=r.guide[0].question,r=>r.guide[0].command='ignored',r=>r.guide.pop(),r=>delete r.readingProfile]){const a=fixture();change(a.reader);owned(()=>readingProjection(a));}
  const a=fixture();a.reader.guide[0].answer='Inspect source-index.json for this exact observation';owned(()=>validateBusinessReader(a));
});
test('exactly shared reference limits appear once while distinct boundaries remain separate and escaped',()=>{
  const limit='The original selected boundary remains explicit and has <limited> scope.',rows=[['First case','Original passing observation',limit],['Second case','Original failed observation',limit]],before=structuredClone(rows),html=referenceTable(rows);assert.equal((html.match(/has &lt;limited&gt; scope/g)||[]).length,1);assert.match(html,/Shared boundary/);assert.deepEqual(rows,before);
  rows[1][2]=limit+' A distinct condition remains.';const separate=referenceTable(rows);assert.doesNotMatch(separate,/references-shared-limit/);assert.match(separate,/A distinct condition remains/);
});
test('concise cases use comparison rows, retain real failed proof and deduplicate only an exact common limit',()=>{
  const a=fixture(),j=a.reader.journeys[0],flow=a.behaviorFlows[0];j.cases.push({...j.cases[0],caseId:'refused',title:'An independently refused observation'});flow.caseAnalysis.cases.push({...flow.caseAnalysis.cases[0],id:'refused',kind:'NEGATIVE',oracleStatus:'EXECUTED'});Object.assign(a.claims[1],{status:'EXECUTED',verdict:'FAILED'});
  const html=renderCaseTable(j,flow,'03.01',new Map(a.claims.map(c=>[c.id,c])),()=>'<details class="reader-comparison"><summary>Decision rule</summary></details>');assert.equal((html.match(/class="reader-case"/g)||[]).length,2);assert.equal((html.match(/reader-shared-limit/g)||[]).length,1);assert.match(html,/Recorded check failed/);assert.match(html,/colspan="3"/);assert.doesNotMatch(html,/<article|<details[^>]+ open/);
  owned(()=>renderCaseTable(j,flow,'03.01',new Map(),()=>''));const foreign=structuredClone(j);foreign.cases[0].caseId='foreign';owned(()=>renderCaseTable(foreign,flow,'03.01',new Map(a.claims.map(c=>[c.id,c])),()=>''));
});
test('the concise business reader opens its story and keeps cases and individual score rationale on request',()=>{
  const a=fixture(),html=renderBusinessReader({analysis:a},'body{}');assert.match(html,/data-atlas-reading="concise-1"/);assert.match(html,/reader-overview/);assert.match(html,/reader-journey-table/);assert.match(html,/reader-case-table/);assert.doesNotMatch(html,/<details class="reader-journey-details" open|<details class="reader-score-axis"[^>]* open/);assert.match(html,/<details class="reader-score-axis"[^>]*><summary>[\s\S]*?<div id="score-atlas-understanding">/);assert.ok(html.indexOf('class="reader-brief"')<html.indexOf('class="reader-primary"'));assert.doesNotMatch(html,/<aside|<footer|Open a score for its review basis/);assert.equal((html.match(/<details class="reader-review-depth">/g)||[]).length,1);assert.doesNotMatch(html,/<details class="reader-review-depth" open/);
});
test('measured reading refuses wasted width, missing behaviors and premature disclosure independently',()=>{
  const expected={guide:[['A reader question','Its actual supported answer','Its remaining boundary']],journeys:[['Existing behavior','Actual operation','Actual result']]},m={width:1680,...structuredClone(expected),openJourneys:0,visibleCases:0,openAxes:0,reviewDepthGroups:1,openReviewDepth:0};assert.deepEqual(reviewConciseReading(m,1920,expected),[]);
  for(const[code,change]of [['READING_WIDTH',x=>x.width=1920],['READING_WIDTH',x=>x.width=1200],['READING_ORIENTATION',x=>x.journeys=[]],['READING_ORIENTATION',x=>x.guide[0][1]='Generic filler'],['READING_DISCLOSURE',x=>x.visibleCases=1],['READING_DISCLOSURE',x=>x.openAxes=18],['READING_DISCLOSURE',x=>x.openReviewDepth=1],['READING_DISCLOSURE',x=>x.reviewDepthGroups=6]]){const bad=structuredClone(m);change(bad);assert.deepEqual(reviewConciseReading(bad,1920,expected).map(f=>f.code),[code]);}
});
test('normal reading navigation reveals useful summaries without exposing every case at once',()=>{
  const targets=['#purpose','#choices','#behaviors'],path=[{target:targets[0],briefVisible:true},{target:targets[1],guideRows:3},{target:targets[2],summaryRows:16,visibleCases:0}];assert.deepEqual(reviewConciseReadingPath(path,targets,3,16),[]);
  for(const change of [p=>p[0].briefVisible=false,p=>p[1].guideRows=0,p=>p[2].summaryRows=15,p=>p[2].visibleCases=51,p=>p[1].target='#foreign']){const bad=structuredClone(path);change(bad);assert.deepEqual(reviewConciseReadingPath(bad,targets,3,16).map(f=>f.code),['READING_PATH']);}
});

test('narrative repetition rejects exact long sentences without merging distinct claims or proof boundaries',()=>{
  const r=fixture().reader,before=structuredClone(r);assert.equal(reviewNarrativeRepetition(r).status,'PASSED');assert.deepEqual(r,before);
  owned(()=>reviewNarrativeRepetition({...r,summary:null}));owned(()=>reviewNarrativeRepetition({...r,value:[{text:null}]}));
  const repeated='A completed operation does not establish an accepted application result.';r.summary=repeated;r.guide[0].answer='  '+repeated.toUpperCase()+'  ';owned(()=>reviewNarrativeRepetition(r));
  r.guide[0].answer='A completed operation does not establish every possible application result.';assert.equal(reviewNarrativeRepetition(r).status,'PASSED');
});
test('concise failure labels preserve unknown, partial and not-applicable scope without claiming exhaustive coverage',()=>{
  assert.equal(failureScopeLabel('UNKNOWN'),'Failure scenarios: not established');assert.equal(failureScopeLabel('PARTIAL'),'Failure scenarios: partial review');assert.equal(failureScopeLabel('NOT_APPLICABLE'),'Failure scenarios: not applicable in reviewed scope');assert.equal(failureScopeLabel('REVIEWED'),'Failure scenarios: reviewed within stated scope');owned(()=>failureScopeLabel('PASSED'));
});

const bandFixture=()=>({section:{x:20,y:400,width:1400,height:1000},brief:{x:28,y:410,width:1384,height:60},primary:{x:28,y:480,width:1384,height:800},figure:{x:28,y:520,width:1384,height:740},canvas:{x:28,y:520,width:1384,height:710}}),quiet=()=>({footers:0,scoreHints:0,unownedText:[]});
test('the primary map needs measured full-band geometry rather than a wide outer page or a narrow fixed canvas',()=>{
 const band=bandFixture(),before=structuredClone(band);assert.deepEqual(reviewPrimaryBand(band,quiet()),[]);assert.deepEqual(band,before);
 for(const change of [b=>b.primary.width=700,b=>b.figure.width=960,b=>b.canvas.width=960,b=>b.canvas.width=1500,b=>b.brief.y=500,b=>b.canvas.x=240,b=>b.canvas.height=0,b=>delete b.canvas]){const b=bandFixture();change(b);assert.deepEqual(reviewPrimaryBand(b,quiet()).map(f=>f.code),['PRIMARY_BAND']);}
});
test('reader noise governance refuses generic footers, score prompts and narrative outside its owning sections',()=>{
 for(const change of [n=>n.footers=1,n=>n.scoreHints=1,n=>n.unownedText=['Repeated global scope label']]){const n=quiet();change(n);assert.deepEqual(reviewPrimaryBand(bandFixture(),n).map(f=>f.code),['READER_NOISE']);}
 const a=fixture(),html=renderBusinessReader({analysis:a},'body{}');assert.equal(inspectBusinessReaderHtml(html).status,'PASSED');owned(()=>inspectBusinessReaderHtml(html.replace('</body>','<footer>Read this document offline.</footer></body>')));
 const historical=html.replace(' data-atlas-layout="primary-band-1"','').replace('</body>','<footer>Captured historical scope.</footer></body>');assert.match(inspectBusinessReaderHtml(historical).scope,/ORIGINAL_AUDIT_AND_SEMANTIC_REVIEW_STILL_REQUIRED/);
});
test('the full-width primary canvas scales only route geometry and retains every original object, label and legacy view',()=>{
 const a=fixture(),html=renderBusinessReader({analysis:a},'body{}');assert.match(html,/class="flow primary-fluid fluid-diagram"/);assert.match(html,/class="flow fluid-diagram"/);assert.equal((html.match(/class="primary-routing"/g)||[]).length,a.reader.primaryGraph.edges.length);assert.match(html,/<rect x="[\d.]+%"[^>]+width="[\d.]+%"/);
 const routes=[...html.matchAll(/<svg class="primary-routing"[\s\S]*?<\/svg>/g)];assert.ok(routes.every(r=>!r[0].includes('<text')&&r[0].includes('non-scaling-stroke')));for(const n of a.reader.primaryGraph.nodes)assert.ok(html.includes('data-node-id="'+n.id+'"'));
 delete a.reader.readingProfile;delete a.reader.guide;const legacy=renderBusinessReader({analysis:a},'body{}');assert.doesNotMatch(legacy,/primary-fluid/);assert.match(legacy,/viewBox="0 0 940/);assert.match(legacy,/<footer>/);
 const compact=fixture();compact.reader.primaryGraph.nodes.forEach((n,i)=>Object.assign(n,{band:0,column:i}));const short=renderBusinessReader({analysis:compact},'body{}');assert.match(short,/class="flow primary-fluid fluid-diagram"[^>]+height="290"/);assert.match(short,/primary-routing[^>]+viewBox="0 0 940 290"/);
});


test('measured reader frame refuses full bleed, asymmetry and bright gutters while preserving generous inner width',()=>{
 const frame={x:120,width:1680,right:1800,background:'rgb(11, 15, 20)',backgroundImage:'none'},before=structuredClone(frame);assert.deepEqual(reviewReaderFrame(frame,1920),[]);assert.deepEqual(frame,before);
 for(const change of [f=>Object.assign(f,{x:0,width:1920,right:1920}),f=>f.x=180,f=>Object.assign(f,{x:400,width:1120,right:1520}),f=>f.background='rgb(255, 255, 255)',f=>f.backgroundImage='linear-gradient(white, black)',f=>delete f.right]){const bad=structuredClone(frame);change(bad);assert.deepEqual(reviewReaderFrame(bad,1920).map(f=>f.code),['READER_FRAME']);assert.deepEqual(reviewReaderFrame(frame,1920),[]);}
 for(const width of [900,1440]){const inner=width*.9,x=(width-inner)/2;assert.deepEqual(reviewReaderFrame({...frame,x,width:inner,right:width-x},width),[]);}
});

test('confidence review preserves source-owned answers and refuses repeated prose or misplaced provenance',()=>{
 const expected={rows:[['What was reviewed','The selected source and checks explain the existing framework behavior.'],['What remains unproven','Application deployment and exhaustive behavior remain outside this review.']],date:'2026-11-28',capture:{state:'CLEAN',meaning:'The reviewed source was committed.',inBasis:true,visible:false}},metrics={...structuredClone(expected),basisCount:1,basisOpen:0,looseParagraphs:0,columns:[24,76]};assert.deepEqual(reviewConfidenceReview(metrics,expected),[]);
 for(const change of [m=>m.rows[0][1]='Generic summary',m=>m.date='2026-11-01',m=>m.columns=[40,60],m=>m.basisCount=0,m=>m.basisOpen=1,m=>m.looseParagraphs=1,m=>m.capture.inBasis=false,m=>m.capture.visible=true,m=>m.capture.meaning='An invented state']){const bad=structuredClone(metrics);change(bad);assert.deepEqual(reviewConfidenceReview(bad,expected).map(f=>f.code),['CONFIDENCE_REVIEW']);assert.deepEqual(reviewConfidenceReview(metrics,expected),[]);}
});

test('confidence rendering keeps clean provenance optional, dirty warnings outside detail and source-only evidence honest',()=>{
 const envelope=renderBusinessReader({analysis:fixture()},'body{}');owned(()=>inspectBusinessReaderHtml(envelope.replace('framed-1','unknown-surface')));
 const a=fixture(),review=a.reader.review,date=a.reader.reviewDate,clean={state:'CLEAN',meaning:'Original clean <source> capture.'},dirty={state:'DIRTY',meaning:'Original unpublished changes.'};
 const cleanHtml=renderConfidenceReview(review,date,clean),dirtyHtml=renderConfidenceReview(review,date,dirty),none=renderConfidenceReview(review,date,null);assert.match(cleanHtml,/<details class="reader-evidence-basis"><summary>Review basis/);assert.ok(cleanHtml.indexOf('reader-evidence-basis')<cleanHtml.indexOf('data-atlas-capture-state'));assert.match(cleanHtml,/Original clean &lt;source&gt; capture/);assert.ok(dirtyHtml.indexOf('data-atlas-capture-state')<dirtyHtml.indexOf('reader-evidence-basis'));assert.doesNotMatch(none,/data-atlas-capture-state|Recorded check passed/);
 Object.assign(a.claims[1],{status:'EXECUTED',verdict:'FAILED'});a.reader.limits[0].claimIds.push('ORACLE');a.behaviorFlows[0].caseAnalysis.cases[0].oracleStatus='EXECUTED';const html=renderBusinessReader({analysis:a},'body{}');assert.ok(html.indexOf('A retained check did not meet expectations')<html.indexOf('reader-evidence-basis'));assert.match(html,/Recorded check failed/);
 const verbose=fixture();verbose.reader.review.scope='Repeated '.repeat(41);owned(()=>validateBusinessReader(verbose));
});
