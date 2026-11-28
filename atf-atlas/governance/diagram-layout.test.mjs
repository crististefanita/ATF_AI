import test from 'node:test';import assert from 'node:assert/strict';
import {layoutFittedDiagram,reviewFittedDiagramMetrics,reviewDiagramSpacing} from './diagram-layout.mjs';
import {referenceFixture} from '../../tools/fixtures/atlas-reference.mjs';
import {renderBusinessReader} from '../../tools/atlas-reader.mjs';
import {readerGraphSet,readerDiagramProjection,renderReaderGraph} from '../../tools/atlas-reader-graphs.mjs';
import {validateCurrentUnderstandingProfiles} from '../../tools/atlas-understanding.mjs';
const fork=()=>({nodes:[{id:'intent',label:'Request service'},{id:'action',label:'Observe response'},{id:'accept',label:'Accepted result'},{id:'refuse',label:'Refusal retained'}],edges:[{from:'intent',to:'action',label:'sends request'},{from:'action',to:'accept',label:'accepts result'},{from:'action',to:'refuse',label:'retains refusal'}]});
const owned=fn=>assert.throws(fn,e=>e.code==='ATLAS-READER'&&Boolean(e.hint&&e.owner&&e.recheck));
const current=()=>{const a=referenceFixture();a.reader.presentationProfile='BUSINESS_REFERENCE_V2';return a;};

test('fitted fork preserves every source edge and readable fixed-scale room without overflow lanes',()=>{
  const g=fork(),before=structuredClone(g),l=layoutFittedDiagram(g);assert.equal(l.width,960);assert.deepEqual(l.edges,g.edges);assert.deepEqual(g,before);
  for(const n of l.nodes){assert.ok(n.x>=0&&n.x+n.width<=l.width&&n.y>=0&&n.y+n.height<=l.height);assert.equal(n.width,200);}
  assert.ok(l.nodes.find(n=>n.id==='accept').x>l.nodes.find(n=>n.id==='action').x);assert.notEqual(l.nodes[2].y,l.nodes[3].y);
});
test('fitted layout is invariant to consistent identities and edge reordering while retaining real direction',()=>{
  const a=fork(),b=structuredClone(a);b.nodes.forEach(n=>n.id='renamed-'+n.id);b.edges=b.edges.reverse().map(e=>({...e,from:'renamed-'+e.from,to:'renamed-'+e.to}));
  const la=layoutFittedDiagram(a),lb=layoutFittedDiagram(b);assert.deepEqual(la.nodes.map(({id,...n})=>n),lb.nodes.map(({id,...n})=>n));assert.equal(lb.edges.length,3);
});
test('wide source stories fail with an owned split or prose repair rather than silently scrolling',()=>{
  const g=fork();g.nodes.push({id:'later',label:'Later owned result'});g.edges.push({from:'accept',to:'later',label:'retains result'});
  assert.throws(()=>layoutFittedDiagram(g),e=>e.code==='ATLAS-READER'&&/horizontal scrolling/.test(e.observed)&&/subviews or use the supported prose/.test(e.observed)&&Boolean(e.hint&&e.owner&&e.recheck));
  const cycle=fork();cycle.edges.push({from:'accept',to:'intent',label:'returns context'});owned(()=>layoutFittedDiagram(cycle));
});
test('fitted graphs retain disconnected actual owners and refuse foreign endpoints without invented links',()=>{
  const g=fork();g.nodes.push({id:'independent',label:'Independent owner'});assert.equal(layoutFittedDiagram(g).edges.length,3);g.edges.push({from:'intent',to:'absent',label:'invented endpoint'});owned(()=>layoutFittedDiagram(g));
});
test('every current diagram has two closed local disclosures with original proof and source-specific meaning',()=>{
  const a=current(),before=structuredClone(a),views=readerGraphSet(a),html=renderBusinessReader({analysis:a},'body{}');assert.deepEqual(a,before);
  assert.match(html,/business-reference-2/);assert.equal((html.match(/class="atlas-references/g)||[]).length,views.length);assert.equal((html.match(/<summary>References and review scope<\/summary>/g)||[]).length,views.length);assert.doesNotMatch(html,/style="min-width:|overflow-x:auto/);
  const detail=readerDiagramProjection(a).find(v=>v.kind==='BEHAVIOR');assert.equal(detail.reading.length,3);assert.ok(detail.reading[0].includes(a.reader.journeys[0].operation));assert.ok(detail.reading[1].includes(a.reader.journeys[0].outcome));assert.equal(detail.references[0][1],'An independent check was reviewed; execution is not inferred');
  Object.assign(a.claims.find(c=>c.id==='ORACLE'),{status:'EXECUTED',verdict:'FAILED'});a.behaviorFlows[0].caseAnalysis.cases[0].oracleStatus='EXECUTED';assert.equal(readerDiagramProjection(a).find(v=>v.kind==='BEHAVIOR').references[0][1],'A retained check failed');
});
test('an empty references shell cannot satisfy local diagram support',()=>{
  const a=current(),view=readerGraphSet(a).find(v=>v.flowId);view.references=[];owned(()=>renderReaderGraph(view.graph,view));
});
test('actual fitted metrics reject horizontal overflow, tiny transformed text and missing measurements independently',()=>{
  const ids=['primary','detail'],structure={horizontalOverflow:[],diagramScale:ids.map(id=>({id,min:14,max:19}))},support=ids.map(id=>({id,legends:1,references:1,details:2,open:0})),before=structuredClone({structure,support});assert.deepEqual(reviewFittedDiagramMetrics(structure,ids,support),[]);
  for(const[code,change]of [['HORIZONTAL_SCROLL',s=>s.horizontalOverflow=['detail']],['HORIZONTAL_SCROLL',s=>delete s.horizontalOverflow],['DIAGRAM_SCALE',s=>s.diagramScale[0].min=9],['DIAGRAM_SCALE',s=>s.diagramScale[0].max=26],['DIAGRAM_SCALE',s=>s.diagramScale[0].min=21],['DIAGRAM_SCALE',s=>s.diagramScale[1].id='primary'],['DIAGRAM_SCALE',s=>delete s.diagramScale]]){const s=structuredClone(structure);change(s);assert.deepEqual(reviewFittedDiagramMetrics(s,ids,support).map(f=>f.code),[code]);}
  assert.deepEqual({structure,support},before);
});
test('legend and references must both start closed and retain exactly one owned disclosure per graph',()=>{
  const ids=['primary'],structure={horizontalOverflow:[],diagramScale:[{id:'primary',min:14,max:19}]},support=[{id:'primary',legends:1,references:1,details:2,open:0}];
  for(const change of [s=>s[0].references=0,s=>s[0].details=3,s=>s[0].open=1,s=>s[0].legends=0,s=>s[0].id='foreign']){const s=structuredClone(support);change(s);assert.deepEqual(reviewFittedDiagramMetrics(structure,ids,s).map(f=>f.code),['DIAGRAM_DISCLOSURES']);}
});
test('historical reference captures do not gain current no-scroll qualification by compatibility alone',()=>{
  const a=current(),index={captureProfile:'GIT_WORKTREE_V1'};Object.assign(a.reader,{diagramProfile:'SOURCE_GRAPH_V2',comparisonProfile:'SOURCE_BOUND_COMPARISONS_V1',captureProfile:'GIT_WORKTREE_V1'});a.understanding.originProfile='SOURCE_ORIGINS_V1';assert.equal(validateCurrentUnderstandingProfiles(a,index,{profile:'CURRENT_UNDERSTANDING_V3'}).profile,'CURRENT_UNDERSTANDING_V3');a.reader.presentationProfile='BUSINESS_REFERENCE_V1';assert.throws(()=>validateCurrentUnderstandingProfiles(a,index),e=>e.code==='ATLAS-UNDERSTANDING');assert.match(validateCurrentUnderstandingProfiles(a,index,{profile:'CURRENT_UNDERSTANDING_V2'}).scope,/HISTORICAL_V2/);
});

test('native diagram spacing distinguishes readable gaps from touching labels without changing source relationships',()=>{
 const expected=[{id:'owned',nodes:[{id:'request'},{id:'result'}],edges:[{from:'request',to:'result'}]}],d=[{id:'owned',spacing:{nodes:[{id:'request',x:0,y:0,width:100,height:60},{id:'result',x:200,y:0,width:100,height:60}],labels:[{from:'request',to:'result',x:106,y:0,width:88,height:20}]}}],before=structuredClone(d);
 assert.deepEqual(reviewDiagramSpacing(d,expected),[]);assert.deepEqual(d,before);d[0].spacing.labels[0].x=103;const findings=reviewDiagramSpacing(d,expected);assert.equal(findings[0].code,'DIAGRAM_SPACING');assert.match(findings[0].observation,/3.00 px/);assert.match(findings[0].observation,/without shrinking type or deleting/);
});
test('diagram breathing room requires actual complete finite native rectangles rather than padding declarations',()=>{
 const expected=[{id:'owned',nodes:[{id:'request'}],edges:[]}],d=[{id:'owned',spacing:{nodes:[{id:'request',x:0,y:0,width:100,height:60}],labels:[]}}];assert.deepEqual(reviewDiagramSpacing(d,expected),[]);
 for(const change of [r=>delete r[0].spacing,r=>r[0].spacing.nodes[0].width=0,r=>r[0].spacing.nodes[0].x=NaN,r=>r[0].spacing.nodes[0].id='foreign',r=>r.push(structuredClone(r[0]))]){const r=structuredClone(d);change(r);assert.ok(reviewDiagramSpacing(r,expected).some(f=>f.code==='DIAGRAM_SPACING'));}
});
