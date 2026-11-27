import test from 'node:test';import assert from 'node:assert/strict';
import {validateSemanticFacets} from './atlas-semantics.mjs';
function fixture(){
  const names=['purpose','structure','behavior','data','configuration','failure','verification'];
  return {claims:[{id:'implementation',status:'SOURCE_REVIEWED',sourceRefs:[{path:'service/operation.any',startLine:1,endLine:12}]},{id:'oracle',status:'SOURCE_REVIEWED',sourceRefs:[{path:'checks/operation.any',startLine:5,endLine:20}]}],understanding:{reviewProfile:'SEMANTIC_FACETS_V1',units:[{id:'service',scopePaths:['service'],claimIds:['implementation'],sharedClaimIds:['oracle'],facets:Object.fromEntries(names.map(name=>[name,{status:'REVIEWED',explanation:'The reviewed '+name+' responsibility has a distinct supported observation.',claimIds:['implementation','oracle'],review:{scope:'The declared '+name+' behavior is examined within this existing service boundary.',limits:'Other deployments, inputs and unreviewed responsibilities remain outside this scope.',implementationRefs:[{claimId:'implementation',refIndices:[0]}],oracleRefs:['purpose','structure'].includes(name)?[]:[{claimId:'oracle',refIndices:[0]}]}}]))}]}};
}
const owned=fn=>assert.throws(fn,e=>e.code==='ATLAS-SEMANTICS'&&Boolean(e.hint&&e.owner&&e.recheck));
test('specific scoped facets pass without inferring semantic completeness or changing the input',()=>{
  const a=fixture(),before=structuredClone(a),result=validateSemanticFacets(a,{required:true});assert.equal(result.facets.REVIEWED,7);assert.match(result.scope,/NO_AUTOMATIC_COMPLETENESS_OR_SCORE/);assert.deepEqual(a,before);
});
test('an optional legacy declaration is not silently certified as a semantic qualification',()=>{
  const a=fixture();delete a.understanding.reviewProfile;assert.equal(validateSemanticFacets(a).status,'NOT_ASSESSED');owned(()=>validateSemanticFacets(a,{required:true}));
});
test('repeated generic prose and empty review boundaries cannot justify a promotion',()=>{
  for(const change of [u=>u.facets.data.explanation=u.facets.behavior.explanation,u=>u.facets.failure.review.limits='',u=>u.facets.configuration.review.scope='Reviewed']){const a=fixture();change(a.understanding.units[0]);owned(()=>validateSemanticFacets(a));}assert.equal(validateSemanticFacets(fixture()).status,'PASSED');
});
test('foreign owners, absent fragments and duplicate source selections retain owned failures',()=>{
  for(const change of [u=>u.facets.data.status=['REVIEWED'],u=>u.scopePaths=['another-owner'],u=>u.facets.data.review.implementationRefs[0].refIndices=[999],u=>u.facets.data.review.implementationRefs[0].refIndices=[0,0],u=>u.facets.data.review.oracleRefs[0].claimId='foreign']){const a=fixture();change(a.understanding.units[0]);owned(()=>validateSemanticFacets(a));}
});
test('self-validation cannot substitute for an independent existing oracle',()=>{
  const a=fixture();a.claims[1].sourceRefs=[{path:'service/operation.any',startLine:10,endLine:15}];owned(()=>validateSemanticFacets(a));a.claims[1].sourceRefs=[{path:'checks/operation.any',startLine:10,endLine:15}];assert.equal(validateSemanticFacets(a).status,'PASSED');
});
test('missing verification preserves partial scope rather than a reviewed badge',()=>{
  const a=fixture(),facet=a.understanding.units[0].facets.configuration;facet.review.oracleRefs=[];owned(()=>validateSemanticFacets(a));facet.status='PARTIAL';assert.equal(validateSemanticFacets(a).facets.PARTIAL,1);
});
test('executed semantic oracles need their actual fragment and testcase identity',()=>{
  const a=fixture(),oracle=a.claims[1];oracle.status='EXECUTED';oracle.verdict='PASSED';owned(()=>validateSemanticFacets(a));oracle.executionSubjects=[{sourcePath:'checks/operation.any',sourceRefIndices:[0]}];assert.equal(validateSemanticFacets(a).status,'PASSED');oracle.executionSubjects[0].sourceRefIndices=[99];owned(()=>validateSemanticFacets(a));
});
