import test from 'node:test';import assert from 'node:assert/strict';
import {validateSemanticFacets,semanticBaselineFacets,validateSemanticBaseline,facetProofProjection} from './atlas-semantics.mjs';
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

function refinementFixture(){
 const a=fixture();a.projectId='service';a.revision='new';a.understanding.units[0].facets.data.status='PARTIAL';const baseline={analysis:structuredClone(a)};baseline.analysis.revision='old';
 const old=baseline.analysis.understanding.units[0].facets.data,facet=a.understanding.units[0].facets.data;
 a.documents=[{path:'semantic-baseline.json',sha256:'c'.repeat(64)}];
 a.claims[0].sourceRefs.push({path:'service/operation.any',startLine:30,endLine:42});a.claims[1].sourceRefs.push({path:'checks/operation.any',startLine:50,endLine:70});
 facet.status='REVIEWED';facet.review.implementationRefs.push({claimId:'implementation',refIndices:[1]});facet.review.oracleRefs.push({claimId:'oracle',refIndices:[1]});
 a.understanding.refinement={schemaVersion:1,baselineDocument:'semantic-baseline.json',baselineSha256:'c'.repeat(64),baselineRevision:'old',baselineFacets:semanticBaselineFacets(baseline.analysis),decisions:[{unitId:'service',facet:'data',question:'Which existing data relationships and limits were not explained by the previous review?',answer:'The independently checked additional operation establishes its stated relationship within the retained boundary.',baselineScope:old.review.scope,baselineLimits:old.review.limits,implementationRefs:[{claimId:'implementation',refIndices:[1]}],oracleRefs:[{claimId:'oracle',refIndices:[1]}]}]};return {a,baseline};
}
test('refinement preserves the original boundary and requires genuinely additional selected evidence',()=>{const {a,baseline}=refinementFixture(),before=structuredClone(a);assert.equal(validateSemanticBaseline(a,baseline).status,'PASSED');assert.deepEqual(validateSemanticFacets(a).refinement,{status:'PASSED',previousPartial:1,decisions:1,remainingPartial:0,scope:'DECLARED_PROGRESS_WITH_RETAINED_BOUNDARIES; ORIGINAL_BASELINE_CHECK_REQUIRED'});assert.deepEqual(a,before);});
test('scope deletion, omitted decisions, receipt recycling and unsupported promotion retain owned hints',()=>{
 for(const change of [a=>a.understanding.refinement.decisions=[],a=>a.understanding.refinement.decisions.push(a.understanding.refinement.decisions[0]),a=>a.understanding.units[0].facets.data.review.limits='A replacement boundary silently discards the actual previous limits.',a=>a.understanding.refinement.decisions[0].implementationRefs[0].refIndices=[0],a=>a.understanding.refinement.decisions[0].oracleRefs=[],a=>a.understanding.refinement.baselineSha256='f'.repeat(64)]){const {a}=refinementFixture();change(a);owned(()=>validateSemanticFacets(a));}assert.equal(validateSemanticFacets(refinementFixture().a).status,'PASSED');
});
test('a forged baseline projection or foreign original audit cannot certify refinement',()=>{for(const change of [b=>b.analysis.projectId='foreign',b=>b.analysis.revision='foreign',b=>b.analysis.understanding.units[0].facets.data.review.limits='An altered historical boundary does not match the original dossier.']){const {a,baseline}=refinementFixture();change(baseline);owned(()=>validateSemanticBaseline(a,baseline));}});
test('malformed semantic references and owner scopes fail with repair guidance rather than a raw type error',()=>{for(const change of [u=>u.facets.data.review.implementationRefs=[null],u=>delete u.facets.data.claimIds,u=>u.scopePaths=[null]]){const a=fixture();change(a.understanding.units[0]);owned(()=>validateSemanticFacets(a));}});

test('renaming an old claim cannot disguise recycled baseline source as additional progress',()=>{const {a}=refinementFixture(),unit=a.understanding.units[0],facet=unit.facets.data;a.claims.push({id:'renamed',status:'SOURCE_REVIEWED',sourceRefs:[structuredClone(a.claims[0].sourceRefs[0])]});unit.claimIds.push('renamed');facet.claimIds.push('renamed');facet.review.implementationRefs.push({claimId:'renamed',refIndices:[0]});a.understanding.refinement.decisions[0].implementationRefs=[{claimId:'renamed',refIndices:[0]}];owned(()=>validateSemanticFacets(a));});

function boundedFixture(){const {a}=refinementFixture();a.understanding.refinement.verificationProfile='BOUNDED_OBSERVATIONS_V1';a.claims[1].status='EXECUTED';a.claims[1].verdict='PASSED';a.claims[1].executionSubjects=[{sourcePath:'checks/operation.any',sourceRefIndices:[0,1]}];a.understanding.refinement.decisions[0].proofBoundary={level:'COMPONENT',applicationAcceptance:'NOT_ESTABLISHED',claimIds:['oracle'],nonClaims:['Local values and SDK operations do not establish external application persistence.']};return a;}
test('bounded component observation retains actual execution and visible application non-claims',()=>{const a=boundedFixture();assert.equal(validateSemanticFacets(a).status,'PASSED');assert.deepEqual(facetProofProjection(a,'service','data'),{proof:'Local component checks; application acceptance is not established.',nonClaims:a.understanding.refinement.decisions[0].proofBoundary.nonClaims});});
test('source-only or failed evidence and unsupported acceptance cannot become component qualification',()=>{for(const change of [a=>a.claims[1].status='SOURCE_REVIEWED',a=>a.claims[1].verdict='FAILED',a=>a.understanding.refinement.decisions[0].proofBoundary.applicationAcceptance='ESTABLISHED',a=>a.understanding.refinement.decisions[0].proofBoundary.nonClaims=[],a=>a.understanding.refinement.decisions[0].proofBoundary.claimIds=['implementation'],a=>delete a.understanding.refinement.verificationProfile]){const a=boundedFixture();change(a);owned(()=>validateSemanticFacets(a));}});
test('a source observation stays partial until a real component boundary is deliberately established',()=>{const a=boundedFixture(),d=a.understanding.refinement.decisions[0];d.proofBoundary.level='SOURCE_REVIEW';owned(()=>validateSemanticFacets(a));a.understanding.units[0].facets.data.status='PARTIAL';a.claims[1].status='SOURCE_REVIEWED';assert.equal(validateSemanticFacets(a).refinement.remainingPartial,1);assert.match(facetProofProjection(a,'service','data').proof,/no component execution inferred/);});
