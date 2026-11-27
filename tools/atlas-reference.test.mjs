import test from 'node:test';
import assert from 'node:assert/strict';
import {referencePresentationProjection,renderReferenceScores} from './atlas-reference.mjs';
import {renderBusinessReader,validateBusinessReader,inspectBusinessReaderHtml} from './atlas-reader.mjs';
import {readerDiagramProjection,layoutReaderGraph} from './atlas-reader-graphs.mjs';

function fixture(){
  const refs=['FACT'],names=['purpose','structure','behavior','data','configuration','failure','verification'];
  const facets=Object.fromEntries(names.map(name=>[name,{status:'REVIEWED',claimIds:['FACT','ORACLE'],explanation:'The selected local contract has a bounded independent source review.',review:{oracleRefs:[{claimId:'ORACLE',refIndices:[0]}]}}]));
  const a={projectId:'sample',claims:[{id:'FACT',status:'SOURCE_REVIEWED',sourceRefs:[{path:'component/contract'}]},{id:'ORACLE',status:'SOURCE_REVIEWED',sourceRefs:[{path:'checks/contract'}]}],evidence:[],understanding:{schemaVersion:1,scope:'SELECTED',scopeStatement:'The existing request contract is reviewed with its explicit limitations.',inventoryClaimIds:refs,units:[{id:'component',label:'Existing component',kind:'COMPONENT',scopePaths:['component'],claimIds:refs,sharedClaimIds:['ORACLE'],flowIds:['operation'],limits:'Only the supplied local contract and independent oracle are represented.',facets}]},dependencyGraphs:[{id:'owner',kind:'DEPENDENCY',nodes:[{id:'component',unitId:'component',label:'Existing component'}],edges:[]}],behaviorFlows:[{id:'operation',unitIds:['component'],graph:{id:'operation-map',kind:'BEHAVIOR',nodes:[{id:'request',kind:'INTENT',label:'Caller request'},{id:'invoke',kind:'ACTION',label:'Existing operation'},{id:'result',kind:'OUTCOME',label:'Observed result'}],edges:[{from:'request',to:'invoke',label:'requests observation'},{from:'invoke',to:'result',label:'returns observation'}]},caseAnalysis:{negativeScope:{status:'PARTIAL'},cases:[{id:'observed',kind:'POSITIVE',oracleStatus:'SOURCE_REVIEWED',oracleClaimId:'ORACLE',nodeIds:['request','invoke','result']}]}}]};
  const axes=['understanding','traceability','clarity'].map((id,i)=>({id,label:id,correctness:90,verification:90,clarity:90,score:90,weight:i===0?34:33,claimIds:refs,evidenceIds:[],reason:'The original component owner and its independent oracle were inspected.',limits:'The selected contract does not establish deployment or arbitrary failures.'}));
  a.assessment={method:'EVIDENCE_BOUND_REVIEW',reviewer:'A source-grounded assessment reviewer',scope:'Selected existing behavior and its source-understanding quality.',limits:'Scores are authored observations, not complete correctness or test coverage.',groups:[{id:'atlas',label:'Atlas quality',scope:'The local source understanding and its explicit evidence boundary.',limits:'No remote deployment or complete source semantics are certified.',axes}]};
  a.reader={schemaVersion:1,projectId:'sample',language:'en',presentationProfile:'BUSINESS_REFERENCE_V1',title:'An existing service',summary:'The project accepts an existing request and retains its actual observed result.',reviewDate:'2026-11-27',review:{reviewer:'Independent source-grounded reviewer',scope:'One existing bounded operation and its original independent oracle.',limits:'The observation does not establish deployment or arbitrary input behavior.',claimIds:refs},value:[{title:'Understand the result',text:'The caller can inspect the actual result and its declared acceptance boundary.',claimIds:refs}],capabilities:[{unitId:'component',title:'Existing operation',benefit:'The existing operation retains an independently checked observation.',claimIds:refs}],journeys:[{flowId:'operation',title:'Observe the existing operation',actor:'The calling team',intent:'Obtain the actual observed result',operation:'Invoke the existing bounded operation and retain its observation.',outcome:'The observation reaches the caller within the declared contract.',cases:[{caseId:'observed',title:'An existing observation',situation:'The caller supplies the supported request.',expectation:'The actual result remains available to its caller.',limits:'Only the selected existing request is represented.'}]}],auditOnlyFlows:[],architecture:[{graphId:'owner',title:'Existing source ownership',legend:'The selected component has no declared external dependency in this source map.',diagramMode:'TEXT_ONLY',diagramReason:'One local responsibility has no relationship to draw; prose retains the real source boundary.'}],model:{title:'Read the result',stages:[{id:'request',title:'Request result',claimIds:refs},{id:'result',title:'Observe result',claimIds:refs}],links:[{from:'request',to:'result',label:'retains result',claimIds:refs}],legend:'The reading sequence explains intent and observation, not a complete runtime trace.',diagramMode:'TEXT_ONLY',diagramReason:'The richer primary map explains the actual project objects; this short sequence remains supporting prose.'},limits:[{title:'Selected source scope',text:'The reviewed contract does not establish complete source or deployment acceptance.',claimIds:refs}]};
  a.reader.primaryGraph={id:'project-map',title:'From request to observation',summary:'An existing caller request reaches the operation and its actual retained observation.',legend:'Arrows name the selected source relationships. Scope labels describe reviewed meaning, not automatic application acceptance.',focusNodeId:'result',nodes:[['request','Caller request','purpose'],['invoke','Existing operation','behavior'],['result','Observed result','verification']].map(([id,label,facet],i)=>({id,label,meaning:'This object represents one inspected responsibility of the existing request contract.',limit:'The selected local observation does not establish every possible caller or failure.',band:i,column:1,facetRefs:[{unitId:'component',facet}],claimIds:refs,oracleClaimIds:['ORACLE'],maturity:'REVIEWED'})),edges:[{id:'invoke',from:'request',to:'invoke',label:'requests observation',kind:'BEHAVIOR',claimIds:refs},{id:'return',from:'invoke',to:'result',label:'returns observation',kind:'BEHAVIOR',claimIds:refs}]};
  a.reader.assessment={groups:[{groupId:'atlas',title:'Atlas understanding quality',scope:'The supplied existing behavior and its source-understanding explanation.',limits:'No score certifies every input, deployment or complete source semantics.',axes:axes.map(axis=>({axisId:axis.id,label:axis.label,goal:'Make the existing bounded behavior understandable to the project reader.',question:'Can the reader explain the observed behavior and its remaining boundary?',measure:'The selected source and independent oracle retain their declared meaning.',meaning:'The reviewed source and its declared limits support this authored judgment.',limits:'The selected fixture does not establish universal behavior or deployment acceptance.',ceiling:94,ceilingReason:'The scope is source-reviewed and does not contain fresh deployment acceptance.'}))}],featured:axes.map(axis=>({groupId:'atlas',axisId:axis.id}))};return a;
}
const owned=(run,pattern)=>assert.throws(run,e=>e.code==='ATLAS-READER'&&pattern.test(e.observed)&&Boolean(e.hint&&e.owner&&e.recheck));

test('reference opening has an open real primary map, closed support and scoped score links',()=>{
  const a=fixture(),before=structuredClone(a),html=renderBusinessReader({analysis:a},'body{}');
  assert.deepEqual(a,before);assert.equal(inspectBusinessReaderHtml(html).status,'PASSED');
  assert.match(html,/<details class="reader-primary" open>/);assert.doesNotMatch(html,/<details class="(?:reader-primary-proof|reader-score-rationale|reader-value|reader-flow)" open/);
  assert.equal((html.match(/class="score-card"/g)||[]).length,3);assert.match(html,/Goal/);assert.match(html,/Review ceiling/);assert.doesNotMatch(html,/class="axis-score">100|<script|<template/);
  assert.equal(readerDiagramProjection(a).filter(g=>g.kind==='PRIMARY').length,1);assert.ok(!readerDiagramProjection(a).some(g=>g.id==='reader-value-model'));
});
test('reference profiles cannot silently render unknown presentation fields or downgrade the projection',()=>{
  const a=fixture();delete a.reader.presentationProfile;owned(()=>validateBusinessReader(a),/Declare the reference/);
  a.reader.presentationProfile='invented';owned(()=>referencePresentationProjection(a),/supported business/);
  a.reader.presentationProfile='BUSINESS_REFERENCE_V1';a.reader.primaryGraph.command='ignored';owned(()=>referencePresentationProjection(a),/declared business reference/);
});
test('local proof selects actual facets and independent oracles rather than borrowed green claims',()=>{
  for(const mutate of [a=>a.reader.primaryGraph.nodes[0].facetRefs[0].unitId='foreign',a=>a.reader.primaryGraph.nodes[0].claimIds=['ORACLE-FOREIGN'],a=>a.reader.primaryGraph.nodes[0].oracleClaimIds=['FACT'],a=>a.reader.primaryGraph.nodes[0].oracleClaimIds=[]]){const a=fixture();mutate(a);owned(()=>referencePresentationProjection(a),/facet|claims|oracle/);}
});
test('object maturity preserves partial and unknown source meaning despite passing unrelated observations',()=>{
  const a=fixture();a.understanding.units[0].facets.behavior.status='PARTIAL';owned(()=>referencePresentationProjection(a),/least-established/);
  a.reader.primaryGraph.nodes[1].maturity='PARTIAL';assert.equal(referencePresentationProjection(a).primary.nodes[1].status,'Partial understanding');
  a.understanding.units[0].facets.behavior.status='UNKNOWN';a.reader.primaryGraph.nodes[1].maturity='UNKNOWN';a.reader.primaryGraph.nodes[1].oracleClaimIds=[];assert.equal(referencePresentationProjection(a).primary.nodes[1].proof,'No existing check established');
});
test('a real retained failure remains visible in local object proof',()=>{
  const a=fixture();Object.assign(a.claims[1],{status:'EXECUTED',verdict:'FAILED'});assert.equal(referencePresentationProjection(a).primary.nodes[0].proof,'A retained check failed');
});
test('primary relationships cannot invent endpoints, borrow source or masquerade as unsupported limits',()=>{
  for(const mutate of [g=>g.edges[0].to='foreign',g=>g.edges[0].claimIds=['ORACLE'],g=>g.edges[0].kind='LIMIT',g=>g.edges=[]]){const a=fixture();mutate(a.reader.primaryGraph);owned(()=>referencePresentationProjection(a),/endpoint|source claims|limitation arrow|distinct real/);}
});
test('three real objects and connected source relationships cannot be replaced by decorative padding',()=>{
  const a=fixture();a.reader.primaryGraph.nodes.pop();owned(()=>referencePresentationProjection(a),/three real/);
  const b=fixture();b.reader.primaryGraph.nodes.push({...b.reader.primaryGraph.nodes[0],id:'unused',band:0,column:0});owned(()=>referencePresentationProjection(b),/isolated/);
});
test('bounded layout rejects overlapping frames and keeps every actual source edge',()=>{
  const a=fixture(),graph={...a.reader.primaryGraph,kind:'PRIMARY'},before=structuredClone(graph),layout=layoutReaderGraph(graph);assert.equal(layout.nodes.length,3);assert.deepEqual(layout.edges,graph.edges);assert.deepEqual(graph,before);
  graph.nodes[1].band=graph.nodes[0].band;owned(()=>layoutReaderGraph(graph),/overlap/);
});
test('score values retain the actual rubric and cannot be manufactured from successful test counts',()=>{
  const a=fixture();a.assessment.groups[0].axes[0].score=99;assert.throws(()=>referencePresentationProjection(a),e=>e.code==='ATLAS-ASSESSMENT');
  const b=fixture();b.reader.assessment.groups[0].axes[0].ceiling=89;owned(()=>referencePresentationProjection(b),/reviewed ceiling/);
});
test('score goals, rationale, group separation and exact featured targets remain complete',()=>{
  for(const mutate of [r=>r.groups[0].axes[0].goal='OK',r=>r.groups[0].axes.pop(),r=>r.featured[0].axisId='foreign',r=>r.featured.push(r.featured[0]),r=>r.groups[0].groupId='other']){const a=fixture();mutate(a.reader.assessment);owned(()=>referencePresentationProjection(a),/goal|complete|featured|distinct|scope|group/);}
});
test('excellent scores require original execution evidence as well as a stated ceiling',()=>{
  const a=fixture(),axis=a.assessment.groups[0].axes[0];Object.assign(axis,{correctness:96,verification:96,clarity:96,score:96});a.reader.assessment.groups[0].axes[0].ceiling=97;owned(()=>referencePresentationProjection(a),/95\+/);
  a.evidence.push({id:'retained',status:'PASSED'});axis.evidenceIds=['retained'];assert.equal(referencePresentationProjection(a).axes[0].score,96);
});
test('business-only projection refuses technical score or object prose before output',()=>{
  const a=fixture();a.reader.assessment.groups[0].axes[0].measure='Inspect source-index.json and all XML files';owned(()=>validateBusinessReader(a),/technical construction/);
  const b=fixture();b.reader.primaryGraph.nodes[0].meaning='Use ssh remote-host to inspect the result';owned(()=>validateBusinessReader(b),/technical construction/);
});
test('native local score targets cannot escape the reader or disappear behind envelope changes',()=>{
  const html=renderBusinessReader({analysis:fixture()},'body{}');assert.match(renderReferenceScores(referencePresentationProjection(fixture())),/href="#score-atlas-understanding"/);
  owned(()=>inspectBusinessReaderHtml(html.replace('href="#score-atlas-understanding"','href="https://invalid/score"')),/local reviewed/);
  owned(()=>inspectBusinessReaderHtml(html.replace('id="score-atlas-understanding"','id="lost"')),/exactly once/);
});
