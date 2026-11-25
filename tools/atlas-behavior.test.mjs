import test from 'node:test';
import assert from 'node:assert/strict';
import {analyzeDirectedBehavior,validateAtlasBehavior} from './atlas-behavior.mjs';

function fixture(){
  const ref=(path,line)=>({path,startLine:line,endLine:line,quote:'An independently reviewed fragment.'});
  const claims=[{id:'SOURCE',status:'SOURCE_REVIEWED',sourceRefs:[ref('component.any',1)]},
    {id:'PASS-ORACLE',status:'SOURCE_REVIEWED',sourceRefs:[ref('checks.any',1)]},
    {id:'FAIL-ORACLE',status:'SOURCE_REVIEWED',sourceRefs:[ref('checks.any',2)]},
    {id:'MISSING',status:'UNKNOWN',sourceRefs:[]}];
  const nodes=[['entry','INTENT'],['decide','DECISION'],['pass','OUTCOME'],['fail','OUTCOME']].map(([id,kind])=>({id,kind}));
  const edges=[['begin','entry','decide'],['match','decide','pass'],['reject','decide','fail'],['retry','decide','entry']].map(([id,from,to])=>({id,from,to,claimId:'SOURCE'}));
  const witness=(id,kind,end,edge,oracle)=>({id,kind,title:'Observed '+id,precondition:'The caller supplies independent expected values.',trigger:'Evaluate the observed value.',expected:kind==='POSITIVE'?'Return an accepted observation.':'Preserve the rejected observation.',nodeIds:['entry','decide',end],edgeIds:['begin',edge],claimIds:['SOURCE'],oracleClaimId:oracle,oracleStatus:'SOURCE_REVIEWED',oracleRefIndices:[0],assertion:'The selected existing method asserts the exact observed decision.',limits:'Declared source evidence; no runtime or semantic completeness is inferred.'});
  return {claims,behaviorFlows:[{id:'comparison',unitIds:['component'],claimIds:['SOURCE','PASS-ORACLE','FAIL-ORACLE'],oracleClaimId:'PASS-ORACLE',graph:{kind:'BEHAVIOR',nodes,edges},caseAnalysis:{schemaVersion:1,entryNodeId:'entry',pathScope:'REPRESENTATIVE',negativeScope:{status:'REVIEWED',claimIds:['SOURCE'],explanation:'The positive and rejected input branches have selected independent assertion fragments.'},cases:[witness('match','POSITIVE','pass','match','PASS-ORACLE'),witness('reject','NEGATIVE','fail','reject','FAIL-ORACLE')]}}]};
}
const validate=analysis=>validateAtlasBehavior(analysis,{required:true});
const fails=(analysis,pattern)=>assert.throws(()=>validate(analysis),error=>error.code==='ATLAS-CASES'&&pattern.test(error.observed)&&error.hint&&error.recheck.includes('atlas-behavior.test.mjs'));

test('positive and negative witnesses retain exact directed paths and explicit representative gaps',()=>{
  const analysis=fixture(),before=structuredClone(analysis),result=validate(analysis);
  assert.equal(result.flows[0].positiveCases,1);assert.equal(result.flows[0].negativeCases,1);
  assert.deepEqual(result.flows[0].unwitnessedEdgeIds,['retry']);assert.deepEqual(result.flows[0].unwitnessedNodeIds,[]);assert.deepEqual(analysis,before);
});

test('reversed entry direction cannot fabricate an outcome reachable from intent',()=>{
  const analysis=fixture();Object.assign(analysis.behaviorFlows[0].graph.edges[0],{from:'decide',to:'entry'});fails(analysis,/unreachable/);
});

test('an orphan lobe and a reachable trap cycle fail distinct reachability checks',()=>{
  const a=fixture(),graph=a.behaviorFlows[0].graph;graph.nodes.push({id:'orphan',kind:'ACTION'});graph.edges.push({id:'orphan-edge',from:'orphan',to:'pass',claimId:'SOURCE'});fails(a,/unreachable.*orphan/);
  const b=fixture(),g=b.behaviorFlows[0].graph;g.nodes.push({id:'trap-a',kind:'ACTION'},{id:'trap-b',kind:'ACTION'});g.edges.push({id:'enter-trap',from:'decide',to:'trap-a',claimId:'SOURCE'},{id:'trap-forward',from:'trap-a',to:'trap-b',claimId:'SOURCE'},{id:'trap-back',from:'trap-b',to:'trap-a',claimId:'SOURCE'});fails(b,/cannot reach.*trap-a/);
});

test('a finite witness may traverse a real retry cycle with an exit',()=>{
  const analysis=fixture(),flow=analysis.behaviorFlows[0],witness=flow.caseAnalysis.cases[0];
  witness.nodeIds=['entry','decide','entry','decide','pass'];witness.edgeIds=['begin','retry','begin','match'];flow.caseAnalysis.pathScope='ALL_DECLARED_EDGES';
  assert.deepEqual(validate(analysis).flows[0].unwitnessedEdgeIds,[]);
});

test('all-edge claims reject omitted transitions while representative review discloses them',()=>{
  const analysis=fixture();analysis.behaviorFlows[0].caseAnalysis.pathScope='ALL_DECLARED_EDGES';fails(analysis,/omits.*retry/);
});

test('witnesses cannot jump across nodes, reverse an edge or borrow a foreign transition claim',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases[0].edgeIds=['begin','reject'];fails(a,/walk, edge direction/);
  const b=fixture();b.behaviorFlows[0].caseAnalysis.cases[0].nodeIds=['entry','pass'];fails(b,/must start/);
  const c=fixture();c.claims.push({id:'FOREIGN',status:'SOURCE_REVIEWED',sourceRefs:[{path:'another.any',startLine:1,endLine:1}]});c.behaviorFlows[0].graph.edges[0].claimId='FOREIGN';fails(c,/selected transition source/);
});

test('declared outcomes cannot remain unwitnessed or be replaced by intermediate states',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases.pop();a.behaviorFlows[0].caseAnalysis.negativeScope.status='PARTIAL';fails(a,/outcome lacks/);
  const b=fixture();b.behaviorFlows[0].caseAnalysis.cases[0].nodeIds=['entry','decide'];b.behaviorFlows[0].caseAnalysis.cases[0].edgeIds=['begin'];fails(b,/observed outcome/);
});

test('a reviewed negative scope needs a negative case and inapplicability cannot hide one',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases[1].kind='POSITIVE';fails(a,/negative witness/);
  const b=fixture();b.behaviorFlows[0].caseAnalysis.negativeScope.status='NOT_APPLICABLE';fails(b,/contradicts/);
});

test('an implementation claim and copied source fragment cannot act as an independent oracle',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases[0].oracleClaimId='SOURCE';fails(a,/separate from its oracle/);
  const b=fixture();b.claims[1].sourceRefs=structuredClone(b.claims[0].sourceRefs);fails(b,/independent oracle/);
});

test('cases select actual oracle fragments and retain their original review or execution status',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases[0].oracleRefIndices=[2];fails(a,/exact existing/);
  const b=fixture();b.behaviorFlows[0].caseAnalysis.cases[0].oracleStatus='EXECUTED';fails(b,/status or declared flow ownership/);
});

test('overlapping trimmed oracle ranges cannot disguise the same implementation method',()=>{
  const a=fixture();Object.assign(a.claims[0].sourceRefs[0],{path:'same.any',startLine:10,endLine:30});Object.assign(a.claims[1].sourceRefs[0],{path:'same.any',startLine:11,endLine:29});fails(a,/independent oracle/);
  Object.assign(a.claims[1].sourceRefs[0],{startLine:31,endLine:38});assert.equal(validate(a).status,'ASSESSED_DECLARATIONS');
});

test('verified content identity rejects copied, hardlinked or case-aliased overlapping fragments',()=>{
  const a=fixture();a.claims[1].sourceRefs[0].path='COMPONENT.any';
  const sourceIndex={files:[{path:'component.any',sha256:'a'.repeat(64)},{path:'COMPONENT.any',sha256:'a'.repeat(64)},{path:'checks.any',sha256:'b'.repeat(64)}]};
  assert.throws(()=>validateAtlasBehavior(a,{required:true,sourceIndex}),error=>error.code==='ATLAS-CASES'&&/independent oracle/.test(error.observed));
  sourceIndex.files[1].sha256='c'.repeat(64);assert.equal(validateAtlasBehavior(a,{required:true,sourceIndex}).oracleIdentityScope,'CALLER_VERIFIED_SOURCE_CONTENT_IDENTITIES');
  assert.equal(validate(a).oracleIdentityScope,'DECLARED_PATHS_ONLY');
});

test('executed case fragments require the corresponding actual subject mapping',()=>{
  const a=fixture(),oracle=a.claims[1];oracle.status='EXECUTED';a.behaviorFlows[0].caseAnalysis.cases[0].oracleStatus='EXECUTED';
  oracle.executionSubjects=[{sourcePath:'checks.any',testClass:'ObservedCheck',testName:'different case'}];fails(a,/retained testcase subjects/);
  oracle.executionSubjects[0].sourceRefIndices=[1];fails(a,/retained testcase subjects/);
  oracle.executionSubjects[0].sourceRefIndices=[0];assert.equal(validate(a).status,'ASSESSED_DECLARATIONS');
  oracle.executionSubjects[0].sourceRefIndices.push(999);fails(a,/retained testcase subjects/);
});

test('a reviewed failure facet cannot hide behind positive-only cases and unknown negative scope',()=>{
  const a=fixture(),flow=a.behaviorFlows[0];flow.caseAnalysis.cases[1].kind='POSITIVE';flow.caseAnalysis.negativeScope.status='UNKNOWN';
  a.understanding={units:[{id:'component',claimIds:['SOURCE','PASS-ORACLE','FAIL-ORACLE'],sharedClaimIds:[],flowIds:['comparison'],facets:{failure:{status:'REVIEWED'}}}]};
  fails(a,/reviewed failure facet/);a.understanding.units[0].facets.failure.status='PARTIAL';assert.equal(validate(a).status,'ASSESSED_DECLARATIONS');
});

test('a missing oracle is valid only with an explicit actual searched scope',()=>{
  const analysis=fixture(),witness=analysis.behaviorFlows[0].caseAnalysis.cases[1];
  Object.assign(witness,{oracleClaimId:'MISSING',oracleStatus:'NOT_FOUND',oracleRefIndices:[],oracleGap:{searchedClaimIds:['SOURCE'],explanation:'The operation source was inspected; no corresponding existing oracle was located.'}});
  assert.equal(validate(analysis).flows[0].oracleGaps,1);witness.oracleGap.searchedClaimIds=['invented'];fails(analysis,/missing oracle/);
});

test('source and oracle ownership cannot be borrowed from another review unit',()=>{
  const a=fixture();a.understanding={units:[{id:'component',claimIds:['SOURCE'],sharedClaimIds:[]}]};fails(a,/oracle needs an owned/);
  a.understanding.units[0].sharedClaimIds=['PASS-ORACLE','FAIL-ORACLE'];assert.equal(validate(a).status,'ASSESSED_DECLARATIONS');
  a.understanding.units[0].claimIds=[];fails(a,/source is outside/);
});

test('unknown negative scope and a single bounded outcome remain valid for a source-only project',()=>{
  const a=fixture(),flow=a.behaviorFlows[0];flow.graph={kind:'BEHAVIOR',nodes:[{id:'observation',kind:'OUTCOME'}],edges:[]};
  flow.caseAnalysis.entryNodeId='observation';flow.caseAnalysis.negativeScope.status='UNKNOWN';flow.caseAnalysis.cases=[{...flow.caseAnalysis.cases[0],nodeIds:['observation'],edgeIds:[]}];
  assert.equal(validate(a).flows[0].cases,1);
});

test('legacy captures remain optional while v3 requires explicit case analysis',()=>{
  const a=fixture();delete a.behaviorFlows[0].caseAnalysis;assert.equal(validateAtlasBehavior(a).status,'NOT_ASSESSED');fails(a,/version 3 requires/);
  a.understanding={units:[{id:'historical',facets:{failure:{status:'REVIEWED'}}}]};assert.equal(validateAtlasBehavior(a).status,'NOT_ASSESSED');
  delete a.behaviorFlows;assert.equal(validateAtlasBehavior(a).status,'NOT_ASSESSED');fails(a,/declared flow records/);
});

test('malformed graphs and case records fail through the owned diagnostic',()=>{
  const a=fixture();a.behaviorFlows[0].caseAnalysis.cases=[null];fails(a,/unique identity/);
  const b=fixture();b.behaviorFlows[0].graph.edges[1].id='begin';fails(b,/unique identities/);
});

test('reachability is invariant under declaration order and consistent node renaming',()=>{
  for(let size=2;size<=90;size+=7){
    const nodes=Array.from({length:size},(_,index)=>({id:'node-'+index,kind:index===size-1?'OUTCOME':'ACTION'}));
    const edges=nodes.slice(1).map((_,index)=>({id:'edge-'+index,from:'node-'+index,to:'node-'+(index+1)}));
    const a=analyzeDirectedBehavior({nodes,edges,entryNodeId:'node-0'}),b=analyzeDirectedBehavior({nodes:[...nodes].reverse(),edges:[...edges].reverse(),entryNodeId:'node-0'});
    assert.deepEqual(a,b);assert.equal(a.reachableNodeIds.length,size);assert.deepEqual(a.deadEndNodeIds,[]);
    const rename=id=>'renamed-'+id;
    const c=analyzeDirectedBehavior({nodes:nodes.map(node=>({...node,id:rename(node.id)})),edges:edges.map(edge=>({...edge,from:rename(edge.from),to:rename(edge.to)})),entryNodeId:rename('node-0')});
    assert.deepEqual(c.unreachableNodeIds,a.unreachableNodeIds);assert.equal(c.productiveNodeIds.length,a.productiveNodeIds.length);
    const cut=Math.floor(edges.length/2),d=analyzeDirectedBehavior({nodes,edges:edges.filter((_,index)=>index!==cut),entryNodeId:'node-0'});
    assert.equal(d.unreachableNodeIds.length,size-cut-1);assert.equal(d.deadEndNodeIds.length,cut+1);
  }
});

test('iterative traversal handles a deep chain and does not invent an exit for a terminal cycle',()=>{
  const size=12000,nodes=Array.from({length:size},(_,index)=>({id:'n-'+index,kind:index===size-1?'OUTCOME':'ACTION'})),edges=nodes.slice(1).map((_,index)=>({id:'e-'+index,from:'n-'+index,to:'n-'+(index+1)}));
  assert.equal(analyzeDirectedBehavior({nodes,edges,entryNodeId:'n-0'}).productiveNodeIds.length,size);
  edges.pop();edges.push({id:'cycle',from:'n-'+(size-2),to:'n-'+(size-3)});
  assert.equal(analyzeDirectedBehavior({nodes,edges,entryNodeId:'n-0'}).deadEndNodeIds.length,size-1);
});
