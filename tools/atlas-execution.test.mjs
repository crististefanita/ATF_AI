import test from 'node:test';
import assert from 'node:assert/strict';
import {validateFlowExecutionBindings} from './atlas-execution.mjs';

function fixture(){
  return {analysis:{projectId:'generic-project',claims:[{id:'BEHAVIOR-1',status:'EXECUTED',verdict:'PASSED',sourceRefs:[{path:'src/request.any'}],governanceRefs:[{path:'checks/request.any'}],evidenceIds:['run'],
    executionSubjects:[{evidenceId:'run',testClass:'sample.RequestCheck',testName:'observed response',sourcePath:'checks/request.any'}]}],
    behaviorFlows:[{claimIds:['BEHAVIOR-1'],oracleClaimId:'BEHAVIOR-1'}]},
    evidence:[{id:'run',projectId:'generic-project',format:'SUREFIRE_XML',status:'PASSED',text:'<testsuite><testcase classname="sample.RequestCheck" name="observed response"/></testsuite>'}]};
}
const fails=(f,pattern)=>assert.throws(()=>validateFlowExecutionBindings(f),error=>error.code==='ATLAS-EVIDENCE'&&pattern.test(error.observed)&&error.hint&&error.owner==='atf-atlas/Report_Contract.md');

test('an executed behavior binds its exact retained testcase and quoted source owner',()=>{
  const f=fixture(),before=structuredClone(f),result=validateFlowExecutionBindings(f);
  assert.equal(result.status,'PASSED');assert.equal(result.claims,1);assert.equal(result.subjects,1);assert.deepEqual(f,before);
});

test('source-only and missing-oracle projects do not acquire execution requirements',()=>{
  const f=fixture();f.analysis.claims[0].status='SOURCE_REVIEWED';delete f.analysis.claims[0].executionSubjects;f.evidence=[];
  f.analysis.claims.push({id:'UNKNOWN-ORACLE',status:'UNKNOWN'});f.analysis.behaviorFlows[0].oracleClaimId='UNKNOWN-ORACLE';
  assert.equal(validateFlowExecutionBindings(f).claims,0);
  delete f.analysis.behaviorFlows;f.analysis.claims[0].status='EXECUTED';assert.equal(validateFlowExecutionBindings(f).claims,0);
});

test('an executed oracle needs bindings even when the behavior itself is source-reviewed',()=>{
  const f=fixture();f.analysis.claims.push({id:'SOURCE-ONLY',status:'SOURCE_REVIEWED'});f.analysis.behaviorFlows[0].claimIds=['SOURCE-ONLY'];
  delete f.analysis.claims[0].executionSubjects;fails(f,/needs retained testcase bindings/);
});

test('a retained unrelated testcase cannot establish behavior execution despite matching source hashes',()=>{
  const f=fixture();f.evidence[0].text='<testsuite><testcase classname="another.UnrelatedCheck" name="observed response"/></testsuite>';
  fails(f,/absent, ambiguous or skipped/);f.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="another observation"/></testsuite>';fails(f,/absent/);
});

test('bindings cannot use another claim source path, evidence ID or target',()=>{
  const f=fixture();f.analysis.claims[0].executionSubjects[0].sourcePath='checks/unquoted.any';fails(f,/quoted source path/);
  const g=fixture();g.analysis.claims[0].executionSubjects[0].evidenceId='another-run';fails(g,/claim evidence/);
  const h=fixture();h.evidence[0].projectId='another-project';fails(h,/this target/);
});

test('every evidence reference needs a testcase and duplicate bindings fail',()=>{
  const f=fixture();f.analysis.claims[0].evidenceIds.push('unbound-run');fails(f,/Every behavior execution evidence/);
  const g=fixture();g.analysis.claims[0].executionSubjects.push({...g.analysis.claims[0].executionSubjects[0]});fails(g,/duplicated/);
});

test('ambiguous duplicated testcase identities and skipped or todo cases cannot establish execution',()=>{
  const f=fixture();f.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response"/><testcase classname="sample.RequestCheck" name="observed response"/></testsuite>';fails(f,/ambiguous/);
  const g=fixture();g.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response"><skipped/></testcase></testsuite>';fails(g,/skipped/);
  const h=fixture();Object.assign(h.evidence[0],{format:'NODE_TAP',text:'ok 1 - observed response # TODO remaining\n'});delete h.analysis.claims[0].executionSubjects[0].testClass;fails(h,/skipped/);
});

test('selected failure and error outcomes remain failed and cannot be relabeled passed',()=>{
  for(const tag of ['failure','error']){
    const f=fixture();f.evidence[0].status='FAILED';f.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response"><'+tag+'/></testcase></testsuite>';
    fails(f,/verdict differs/);f.analysis.claims[0].verdict='FAILED';assert.equal(validateFlowExecutionBindings(f).status,'PASSED');
  }
});

test('an unrelated suite failure cannot be substituted for the selected testcase outcome',()=>{
  const f=fixture();f.evidence[0].status='FAILED';f.analysis.claims[0].verdict='FAILED';
  f.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response"/><testcase classname="another.Check" name="unrelated"><failure/></testcase></testsuite>';fails(f,/verdict differs/);
});

test('XML predefined and numeric entities resolve only to exact retained identities',()=>{
  const f=fixture();Object.assign(f.analysis.claims[0].executionSubjects[0],{testClass:'sample.RequestCheck',testName:'response & status 1'});
  f.evidence[0].text="<testsuite><testcase classname='sample&#x2e;RequestCheck' name='response &amp; status &#49;'/></testsuite>";
  assert.equal(validateFlowExecutionBindings(f).status,'PASSED');
  f.evidence[0].text=f.evidence[0].text.replace('&amp;','&unsupported;');fails(f,/unsupported XML entity/);
});

test('XML declarations cannot introduce external entity resolution or forged test identities',()=>{
  const f=fixture();f.evidence[0].text='<!DOCTYPE testsuite [<!ENTITY external SYSTEM "https://example.invalid/evidence">]>'+f.evidence[0].text;fails(f,/cannot resolve XML/);
  const g=fixture();g.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response" name="another"/></testsuite>';fails(g,/ambiguous repeated attribute/);
  const h=fixture();h.evidence[0].text='<testsuite><!-- <testcase classname="sample.RequestCheck" name="observed response"/> --></testsuite>';fails(h,/absent/);
});

test('flat Node TAP binds the exact test name without inventing a Java class',()=>{
  const f=fixture();Object.assign(f.evidence[0],{format:'NODE_TAP',text:'TAP version 13\nok 1 - observed response\n'});delete f.analysis.claims[0].executionSubjects[0].testClass;
  assert.equal(validateFlowExecutionBindings(f).status,'PASSED');f.analysis.claims[0].executionSubjects[0].testName='another response';fails(f,/absent/);
  f.analysis.claims[0].executionSubjects[0].testName='observed response';f.analysis.claims[0].executionSubjects[0].testClass='sample.RequestCheck';fails(f,/empty for flat Node TAP/);
});

test('XML processing instructions, CDATA and output containers cannot manufacture testcase identities',()=>{
  for(const payload of ['<?audit <testcase classname="sample.RequestCheck" name="observed response"/> ?>',
    '<![CDATA[<testcase classname="sample.RequestCheck" name="observed response"/>]]>',
    '<system-out><testcase classname="sample.RequestCheck" name="observed response"/></system-out>',
    '<system-out><testsuite><testcase classname="sample.RequestCheck" name="observed response"/></testsuite></system-out>']){
    const f=fixture();f.evidence[0].text='<testsuite>'+payload+'<testcase classname="another.Check" name="unrelated"/></testsuite>';fails(f,/absent/);
  }
});

test('only actual direct testcase failure children decide the selected outcome',()=>{
  const f=fixture();f.evidence[0].text='<testsuites><testsuite><testcase classname="sample.RequestCheck" name="observed response"><system-out><![CDATA[<failure/>]]><?audit <error/> ?><failure/></system-out></testcase></testsuite></testsuites>';
  assert.equal(validateFlowExecutionBindings(f).status,'PASSED');
});

test('malformed XML cannot supply a selected outcome through unmatched roots or attributes',()=>{
  for(const xml of ['<testsuite><testcase classname="sample.RequestCheck" name="observed response"/></another>',
    '<testsuite><testcase classname="sample.RequestCheck"name="observed response"/></testsuite>',
    '<testsuite><testcase classname="sample.RequestCheck" name="observed response"/>',
    '<testsuite/><testsuite><testcase classname="sample.RequestCheck" name="observed response"/></testsuite>']){
    const f=fixture();f.evidence[0].text=xml;fails(f,/XML/);
  }
});

test('unescaped XML entities and illegal raw characters cannot establish retained identities',()=>{
  for(const suffix of ['&broken','\u0000','\ud800']){
    const f=fixture();f.analysis.claims[0].executionSubjects[0].testClass='sample'+suffix;
    f.evidence[0].text='<testsuite><testcase classname="sample'+suffix+'" name="observed response"/></testsuite>';fails(f,/XML/);
  }
});

test('inert output can retain observed HTML document types without introducing an XML DTD',()=>{
  const f=fixture();f.evidence[0].text='<testsuite><testcase classname="sample.RequestCheck" name="observed response"><system-out><![CDATA[<!DOCTYPE html><html>observed body</html>]]></system-out><!-- <!DOCTYPE html> --></testcase></testsuite>';
  assert.equal(validateFlowExecutionBindings(f).status,'PASSED');
});

test('unsupported HTML and historical proof cannot satisfy an execution binding',()=>{
  const f=fixture();Object.assign(f.evidence[0],{format:'HTML',text:'<p>Passed</p>'});fails(f,/verified XML\/TAP/);
  const g=fixture();g.evidence[0].status='HISTORICAL';fails(g,/verified XML\/TAP/);
});

test('executed facet oracles keep exact testcase bindings even without a declared behavior flow',()=>{
 const f=fixture();delete f.analysis.behaviorFlows;f.analysis.understanding={reviewProfile:'SEMANTIC_FACETS_V1',units:[{facets:{verification:{review:{oracleRefs:[{claimId:'BEHAVIOR-1',refIndices:[0]}]}}}}]};assert.equal(validateFlowExecutionBindings(f).claims,1);f.analysis.claims[0].executionSubjects[0].testName='unrelated case';fails(f,/absent, ambiguous or skipped/);f.analysis.claims[0].executionSubjects[0].testName='observed response';assert.equal(validateFlowExecutionBindings(f).subjects,1);
});
