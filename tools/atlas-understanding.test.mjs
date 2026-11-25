import test from 'node:test';
import assert from 'node:assert/strict';
import {validateAtlasUnderstanding,understandingFacets} from './atlas-understanding.mjs';

function fixture(){
  const claim={id:'CONTRACT-1',status:'SOURCE_REVIEWED',sourceRefs:[{path:'component/source.any'}]},oracle={id:'ORACLE-1',status:'SOURCE_REVIEWED',sourceRefs:[{path:'checks/oracle.any'}]};
  const unit={id:'component',label:'Owned component',kind:'COMPONENT',scopePaths:['component'],claimIds:['CONTRACT-1'],sharedClaimIds:['ORACLE-1'],flowIds:['observed-flow'],limits:'Only the declared source and supplied oracle were reviewed.',facets:Object.fromEntries(understandingFacets.map(name=>[name,{status:name==='behavior'?'REVIEWED':'PARTIAL',claimIds:['CONTRACT-1'],explanation:'An explicitly scoped source review, not universal semantic coverage.'}]))};
  return {analysis:{claims:[claim,oracle],behaviorFlows:[{id:'observed-flow',unitIds:['component']}],dependencyGraphs:[{nodes:[{id:'declared-owner',unitId:'component'}]}],understanding:{schemaVersion:1,scope:'PROJECT',scopeStatement:'Inspect the declared component, keeping gaps visible.',inventoryClaimIds:['CONTRACT-1'],units:[unit]},evidence:[]},index:{files:[{path:'component/source.any'},{path:'checks/oracle.any'}]}};
}
const validate=f=>validateAtlasUnderstanding(f.analysis,f.index,{required:true});
const fails=(f,code,needle)=>assert.throws(()=>validate(f),error=>error.code===code&&error.observed.includes(needle));

test('generic review inventory retains all seven facets and explicit partial scope',()=>{
  const f=fixture(),result=validate(f);assert.equal(result.units,1);assert.equal(result.facets.REVIEWED,1);assert.equal(result.facets.PARTIAL,6);assert.match(result.limitation,/not code coverage/);
});

test('new publications require declared breadth while historical input can remain unassessed',()=>{
  const f=fixture();delete f.analysis.understanding;fails(f,'ATLAS-UNDERSTANDING','requires an explicit');
  assert.equal(validateAtlasUnderstanding(f.analysis,f.index).status,'NOT_ASSESSED');
});

test('a missing facet cannot be hidden by a complete HTML or another owner',()=>{
  const f=fixture();delete f.analysis.understanding.units[0].facets.failure;fails(f,'ATLAS-UNDERSTANDING','all seven');
});

test('a module absent from understanding cannot be hidden inside the architecture graph',()=>{
  const f=fixture();f.analysis.dependencyGraphs[0].nodes.push({id:'unreviewed-module',unitId:'missing'});fails(f,'ATLAS-UNDERSTANDING','lacks a declared understanding unit');
});

test('reviewed owners need real indexed paths and source-bound claims',()=>{
  const f=fixture();f.analysis.understanding.units[0].scopePaths=['different'];fails(f,'ATLAS-UNDERSTANDING','actual source scope');
  f.analysis.understanding.units[0].scopePaths=['checks'];fails(f,'ATLAS-UNDERSTANDING','no source in its declared owner');
});

test('related cross-owner oracles require explicit shared references',()=>{
  const f=fixture(),unit=f.analysis.understanding.units[0];unit.claimIds.push('ORACLE-1');fails(f,'ATLAS-UNDERSTANDING','no source in its declared owner');
  unit.claimIds.pop();unit.facets.verification.claimIds=['ORACLE-1'];assert.equal(validate(f).status,'ASSESSED_DECLARATIONS');
});

test('shared-only prose cannot mark another owner facets as reviewed or partial',()=>{
  for(const status of ['REVIEWED','PARTIAL']){
    const f=fixture(),unit=f.analysis.understanding.units[0];unit.claimIds=[];
    for(const facet of Object.values(unit.facets)){facet.status=status;facet.claimIds=['ORACLE-1'];}
    fails(f,'ATLAS-UNDERSTANDING','evidence owned by this unit');
  }
  const f=fixture(),unit=f.analysis.understanding.units[0];unit.claimIds=[];unit.flowIds=[];f.analysis.behaviorFlows=[];
  for(const facet of Object.values(unit.facets)){facet.status='UNKNOWN';facet.claimIds=[];}
  assert.equal(validate(f).facets.UNKNOWN,7);
});

test('malformed owner and assessment records retain actionable governance errors',()=>{
  const f=fixture();f.analysis.understanding.units=[null];fails(f,'ATLAS-UNDERSTANDING','unique identity');
  const g=assess(fixture());g.analysis.assessment.groups[0].axes=[null];fails(g,'ATLAS-ASSESSMENT','bounded integers');
  const h=fixture();h.analysis.behaviorFlows=[null];fails(h,'ATLAS-UNDERSTANDING','valid source claims');
});

test('unknown review facets remain valid and cannot fabricate reviewed source',()=>{
  const f=fixture(),facet=f.analysis.understanding.units[0].facets.configuration;facet.status='UNKNOWN';facet.claimIds=[];assert.equal(validate(f).facets.UNKNOWN,1);
  facet.status='REVIEWED';fails(f,'ATLAS-UNDERSTANDING','needs owned source evidence');
});

test('all seven facets cannot be dismissed without understanding the owner',()=>{
  const f=fixture();for(const facet of Object.values(f.analysis.understanding.units[0].facets)){facet.status='NOT_APPLICABLE';facet.claimIds=[];}fails(f,'ATLAS-UNDERSTANDING','All facets cannot');
});

test('behavior ownership must agree in both directions',()=>{
  const f=fixture();f.analysis.behaviorFlows[0].unitIds=['missing'];fails(f,'ATLAS-UNDERSTANDING','declared owner units');
  f.analysis.behaviorFlows[0].unitIds=['component'];f.analysis.understanding.units[0].flowIds=[];fails(f,'ATLAS-UNDERSTANDING','Reviewed behavior');
});

function assess(f){
  f.analysis.assessment={method:'EVIDENCE_BOUND_REVIEW',reviewer:'Source and controlled evidence review',scope:'Declared component architecture and Atlas traceability',limits:'No product acceptance is inferred.',groups:[{id:'atlas',label:'Atlas review',scope:'Declared inventory and trace',limits:'No automatic semantic certification.',axes:[{id:'trace',label:'Traceability',correctness:95,verification:90,clarity:95,score:94,weight:60,claimIds:['CONTRACT-1'],evidenceIds:[],reason:'Owned source fragments were inspected.',limits:'Source-only oracle remains source-only.'},{id:'breadth',label:'Breadth',correctness:80,verification:75,clarity:80,score:79,weight:40,claimIds:['CONTRACT-1'],evidenceIds:[],reason:'One declared component; gaps explicit.',limits:'Operational behavior remains partial.'}]}]};
  return f;
}

test('explicit 1–100 axis scores bind their rubric, source evidence and review limitations',()=>{
  const result=validate(assess(fixture()));assert.equal(result.assessment.groups[0].score,88);assert.equal(result.assessment.method,'EVIDENCE_BOUND_REVIEW');
});

test('test volume cannot silently replace the declared assessment rubric',()=>{
  const f=assess(fixture());f.analysis.assessment.groups[0].axes[0].score=100;fails(f,'ATLAS-ASSESSMENT','40/30/30');
});

test('out-of-range grades, unknown claims and fake execution receipts fail',()=>{
  const f=assess(fixture()),axis=f.analysis.assessment.groups[0].axes[0];axis.correctness=101;fails(f,'ATLAS-ASSESSMENT','bounded integers');
  axis.correctness=95;axis.claimIds=['INVENTED'];fails(f,'ATLAS-ASSESSMENT','reviewed claim references');
  axis.claimIds=['CONTRACT-1'];axis.evidenceIds=['fake'];fails(f,'ATLAS-ASSESSMENT','unknown execution evidence');
});

test('axis weights and supplied overall grades cannot disagree with visible arithmetic',()=>{
  const f=assess(fixture());f.analysis.assessment.groups[0].axes[0].weight=70;fails(f,'ATLAS-ASSESSMENT','total 100');
  f.analysis.assessment.groups[0].axes[0].weight=60;f.analysis.assessment.groups[0].score=99;fails(f,'ATLAS-ASSESSMENT','group total differs');
});
