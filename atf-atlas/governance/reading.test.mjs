import test from 'node:test';import assert from 'node:assert/strict';
import {readingProjection,referenceTable,reviewConciseReading,renderCaseTable,reviewConciseReadingPath} from './reading.mjs';
import {referenceFixture} from '../../tools/fixtures/atlas-reference.mjs';
import {renderBusinessReader,validateBusinessReader} from '../../tools/atlas-reader.mjs';
const fixture=()=>{const a=referenceFixture();a.reader.presentationProfile='BUSINESS_REFERENCE_V2';a.reader.readingProfile='CONCISE_READING_V1';a.behaviorFlows[0].claimIds=['FACT'];a.reader.guide=['Which observation does the caller receive?','What remains outside this local check?'].map(question=>({question,answer:'The existing operation returns the observation supported by its independent local check.',limits:'The selected local observation does not establish every deployment or input.',flowIds:['operation'],claimIds:['FACT'],oracleClaimIds:['ORACLE']}));return a;};
const owned=fn=>assert.throws(fn,e=>e.code==='ATLAS-READER'&&Boolean(e.hint&&e.owner&&e.recheck));

test('concise orientation retains actual flow and independent oracle ownership without adding a semantic guarantee',()=>{
  const a=fixture(),before=structuredClone(a),r=readingProjection(a);assert.equal(r.rows.length,2);assert.match(r.scope,/HUMAN_MEANING/);assert.deepEqual(a,before);
  for(const change of [g=>g.flowIds=['foreign'],g=>g.claimIds=['ORACLE'],g=>g.oracleClaimIds=['FACT'],g=>g.oracleClaimIds=[]]){const b=fixture();change(b.reader.guide[0]);owned(()=>readingProjection(b));}
});
test('a short reader guide refuses verbose filler, duplicate questions and undeclared fields',()=>{
  for(const change of [r=>r.guide[0].answer='Words '.repeat(56),r=>r.guide[1].question=r.guide[0].question,r=>r.guide[0].command='ignored',r=>r.guide.pop(),r=>delete r.readingProfile]){const a=fixture();change(a.reader);owned(()=>readingProjection(a));}
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
  const a=fixture(),html=renderBusinessReader({analysis:a},'body{}');assert.match(html,/data-atlas-reading="concise-1"/);assert.match(html,/reader-overview-grid/);assert.match(html,/reader-journey-table/);assert.match(html,/reader-case-table/);assert.doesNotMatch(html,/<details class="reader-journey-details" open|<details class="reader-score-axis"[^>]* open/);assert.match(html,/<details class="reader-score-axis"[^>]*><summary>[\s\S]*?<div id="score-atlas-understanding">/);assert.match(html,/The project in brief/);
});
test('measured reading refuses wasted width, missing behaviors and premature disclosure independently',()=>{
  const expected={guide:[['A reader question','Its actual supported answer','Its remaining boundary']],journeys:[['Existing behavior','Actual operation','Actual result']]},m={width:1860,...structuredClone(expected),openJourneys:0,visibleCases:0,openAxes:0};assert.deepEqual(reviewConciseReading(m,1920,expected),[]);
  for(const[code,change]of [['READING_WIDTH',x=>x.width=1200],['READING_ORIENTATION',x=>x.journeys=[]],['READING_ORIENTATION',x=>x.guide[0][1]='Generic filler'],['READING_DISCLOSURE',x=>x.visibleCases=1],['READING_DISCLOSURE',x=>x.openAxes=18]]){const bad=structuredClone(m);change(bad);assert.deepEqual(reviewConciseReading(bad,1920,expected).map(f=>f.code),[code]);}
});
test('normal reading navigation reveals useful summaries without exposing every case at once',()=>{
  const targets=['#purpose','#choices','#behaviors'],path=[{target:targets[0],briefVisible:true},{target:targets[1],guideRows:3},{target:targets[2],summaryRows:16,visibleCases:0}];assert.deepEqual(reviewConciseReadingPath(path,targets,3,16),[]);
  for(const change of [p=>p[0].briefVisible=false,p=>p[1].guideRows=0,p=>p[2].summaryRows=15,p=>p[2].visibleCases=51,p=>p[1].target='#foreign']){const bad=structuredClone(path);change(bad);assert.deepEqual(reviewConciseReadingPath(bad,targets,3,16).map(f=>f.code),['READING_PATH']);}
});
