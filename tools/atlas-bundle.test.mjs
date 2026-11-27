import test from 'node:test';
import assert from 'node:assert/strict';
import {createHash} from 'node:crypto';
import {validateAtlasBundle} from './atlas-bundle.mjs';
import {parseAtlasJson} from './atlas-diagnostics.mjs';

const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const encoded=(path,bytes)=>{const input=Buffer.isBuffer(bytes)?bytes:Buffer.from(bytes);return {path,sha256:hash(input),encoding:'BASE64',text:input.toString('base64')};};
const text=(path,value)=>({path,sha256:hash(value),encoding:'UTF8',text:value});
const source={path:'contract.txt',startLine:1,endLine:1,quote:'Return the observed response.'};
const guard={path:'oracle.txt',startLine:1,endLine:1,quote:'Assert the same response.'};

test('strict member identity preserves JSON values and independent object scopes',()=>{
  const values=[null,true,0,-1.5,'quoted " keys: {} [] \\ end',[{same:1},{same:2}],{empty:{},nested:[{},[],{same:'value'}],text:'"same":0, "same":1'},JSON.parse('{"__proto__":{"safe":true}}')];
  for(const value of values)assert.deepEqual(parseAtlasJson(JSON.stringify(value),'ATLAS-SOURCE','grammar probe'),value);
  for(const input of ['{"same":1,"same":1}','{"k":1,"\\u006b":2}','[{"a\\\"b":1,"a\\\"b":2}]','{"nested":{"__proto__":0,"__proto__":1}}']){
    assert.throws(()=>parseAtlasJson(input,'ATLAS-SOURCE','source-index.json'),error=>error.code==='ATLAS-SOURCE'&&/Duplicate JSON member/.test(error.observed)&&Boolean(error.hint&&error.owner&&error.recheck));
  }
  for(const input of ['{"a":1,}','{} trailing','{"a":NaN}','[1 2]'])assert.throws(()=>parseAtlasJson(input,'ATLAS-SCHEMA','malformed input'),error=>error.code==='ATLAS-SCHEMA');
});

function fixture(){
  const referencedSources=[text('contract.txt','Return the observed response.\n'),text('oracle.txt','Assert the same response.\n')];
  const sourceIndex={schemaVersion:1,projectId:'generic-project',revision:'UNVERSIONED',files:referencedSources.map(item=>({path:item.path,sha256:item.sha256,role:'Reviewed contract or oracle'}))};
  const xml='<testsuite tests="1" failures="0" errors="0" skipped="0"><testcase name="observed"/></testsuite>';
  const provenance=JSON.stringify({manifest:Object.fromEntries(sourceIndex.files.map(item=>[item.path,item.sha256]))},null,2)+'\n';
  const evidenceFiles=[text('execution.xml',xml),text('executed-source.json',provenance)];
  const analysis={schemaVersion:1,projectId:sourceIndex.projectId,revision:sourceIndex.revision,sourceIndexSha256:'',documents:[],claims:[{
    id:'OBSERVATION-1',statement:'The observed response is returned.',status:'EXECUTED',verdict:'PASSED',sourceRefs:[source],governanceStatus:'MAPPED',governanceRefs:[guard],evidenceIds:['observed-run']
  }],evidence:[{id:'observed-run',projectId:sourceIndex.projectId,path:'execution.xml',sha256:evidenceFiles[0].sha256,format:'SUREFIRE_XML',status:'PASSED',tests:1,failures:0,errors:0,skipped:0,selection:'Existing observation check',sourceManifest:'executed-source.json',sourceIdentity:evidenceFiles[1].sha256}]};
  const documents=[text('README.md','# Target\n\nThe reviewed target.\n'),text('project-map.md','# Map\n\nOBSERVATION-1: The observed response is returned.\n'),text('architecture.md','# Architecture\n\nOne contract boundary.\n'),text('testing-and-governance.md','# Tests\n\nExisting observation check.\n'),text('open-questions.md','# Limits\n\nNo external execution is implied.\n')];
  const bundle={schemaVersion:1,renderVersion:1,analysis,sourceIndex,analysisText:'',sourceIndexText:'',documents,referencedSources,sourceDocuments:[],evidenceFiles,
    knowledge:{corpusSha256:hash('Reviewed policy context'),retrievalScope:'DETERMINISTIC_LEXICAL_POLICY_CONTEXT',semanticRecall:'NOT_CLAIMED',mandatoryIds:['KB-TRACE'],selectedIds:['KB-TRACE'],records:[{id:'KB-TRACE',mandatory:true}]},consistency:{}};
  const save=()=>{bundle.sourceIndexText=JSON.stringify(sourceIndex,null,2)+'\n';analysis.sourceIndexSha256=hash(bundle.sourceIndexText);analysis.documents=documents.map(({path,sha256})=>({path,sha256}));bundle.analysisText=JSON.stringify(analysis,null,2)+'\n';bundle.consistency={status:'PASSED',projectId:analysis.projectId,revision:analysis.revision,sourceFiles:sourceIndex.files.length,documents:documents.length,claims:analysis.claims.length,evidence:analysis.evidence?.length||0,semanticCompleteness:'HUMAN_REVIEW_REQUIRED'};};
  save();return {bundle,save};
}

test('a portable complete bundle preserves raw original JSON, documents, sources and execution',()=>{
  const {bundle}=fixture(),before=structuredClone(bundle),result=validateAtlasBundle(bundle);
  assert.equal(result.status,'PASSED');assert.equal(result.documents,5);assert.equal(result.sources,2);assert.deepEqual(bundle,before);
});

test('portable raw analysis, inventory and provenance cannot hide duplicate members',()=>{
  for(const[field,code]of [['analysisText','ATLAS-SCHEMA'],['sourceIndexText','ATLAS-SOURCE']]){
    const {bundle}=fixture(),original=bundle[field];bundle[field]='{"projectId":"foreign",'+original.trim().slice(1);
    assert.throws(()=>validateAtlasBundle(bundle),error=>error.code===code&&/Duplicate JSON member/.test(error.observed));
    bundle[field]=original;assert.equal(validateAtlasBundle(bundle).status,'PASSED');
  }
  const f=fixture(),entry=f.bundle.evidenceFiles.find(item=>item.path==='executed-source.json'),original=entry.text;
  entry.text='{"manifest":{},'+original.trim().slice(1);entry.sha256=hash(entry.text);
  f.bundle.analysis.evidence[0].sourceIdentity=entry.sha256;f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&/Duplicate JSON member/.test(error.observed));
  entry.text=original;entry.sha256=hash(original);f.bundle.analysis.evidence[0].sourceIdentity=entry.sha256;f.save();
  assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
});

test('portable behavior execution retains the same exact testcase binding as live verification',()=>{
  const f=fixture();f.bundle.analysis.behaviorFlows=[{claimIds:['OBSERVATION-1'],oracleClaimId:'OBSERVATION-1'}];
  f.bundle.analysis.claims[0].executionSubjects=[{evidenceId:'observed-run',testClass:'',testName:'observed',sourcePath:'oracle.txt'}];f.save();
  assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
  f.bundle.analysis.claims[0].executionSubjects[0].testName='another observation';f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('absent, ambiguous or skipped'));
});

test('portable mixed-suite behavior retains selected outcomes and the separate failed suite',()=>{
  const f=fixture(),xml='<testsuite tests="2" failures="1" errors="0" skipped="0"><testcase name="observed"/><testcase name="rejected"><failure/></testcase></testsuite>';
  f.bundle.evidenceFiles[0]=text('execution.xml',xml);
  Object.assign(f.bundle.analysis.evidence[0],{sha256:hash(xml),status:'FAILED',tests:2,failures:1});
  f.bundle.analysis.behaviorFlows=[{claimIds:['OBSERVATION-1'],oracleClaimId:'OBSERVATION-1'}];
  f.bundle.analysis.claims[0].executionSubjects=[{evidenceId:'observed-run',testClass:'',testName:'observed',sourcePath:'oracle.txt'}];f.save();
  assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');assert.equal(f.bundle.analysis.evidence[0].status,'FAILED');
  f.bundle.analysis.claims[0].executionSubjects[0].testName='rejected';f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('selected testcase outcomes'));
  f.bundle.analysis.claims[0].verdict='FAILED';f.save();assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
  f.bundle.analysis.claims[0].executionSubjects[0].testName='observed';f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('selected testcase outcomes'));
});

test('portable unbound cohort claims retain the complete mixed-suite failure',()=>{
  const f=fixture(),xml='<testsuite tests="2" failures="1" errors="0" skipped="0"><testcase name="observed"/><testcase name="rejected"><failure/></testcase></testsuite>';
  f.bundle.evidenceFiles[0]=text('execution.xml',xml);Object.assign(f.bundle.analysis.evidence[0],{sha256:hash(xml),status:'FAILED',tests:2,failures:1});f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('Claim verdict differs'));
  f.bundle.analysis.claims[0].verdict='FAILED';f.save();assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
});

test('parsed metadata cannot diverge from original JSON or its original index bytes',()=>{
  const f=fixture();f.bundle.analysis.projectId='another-project';assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-SCHEMA');
  f.save();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-TARGET');
  const g=fixture();g.bundle.sourceIndexText+=' ';assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-SOURCE');
});

test('a deterministic rerender cannot hide omitted or altered original documents',()=>{
  const f=fixture();f.bundle.documents=f.bundle.documents.filter(item=>item.path!=='architecture.md');
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-DOCUMENT'&&error.observed.includes('inventory'));
  const g=fixture();g.bundle.documents[2].text+='Changed without updating the declared identity';
  assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-DOCUMENT'&&error.observed.includes('SHA-256'));
  const h=fixture();h.bundle.documents.push(text('undeclared.md','Undeclared content'));assert.throws(()=>validateAtlasBundle(h.bundle),error=>error.code==='ATLAS-DOCUMENT');
});

test('source evidence must match its indexed bytes and exact complete line fragments',()=>{
  const f=fixture();f.bundle.referencedSources.pop();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-REFERENCE');
  const g=fixture();g.bundle.analysis.claims[0].sourceRefs=[{...source,quote:'observed response'}];g.save();assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-REFERENCE');
  const h=fixture();h.bundle.referencedSources[0]=text('contract.txt','Return the observed response.\nChanged tail\n');assert.throws(()=>validateAtlasBundle(h.bundle),error=>error.code==='ATLAS-SOURCE');
});

test('requested full source documents preserve exact opaque bytes without inventing a quote',()=>{
  const f=fixture(),attachment=encoded('source.bin',Buffer.from([0,255,44,128]));
  f.bundle.sourceIndex.files.push({path:attachment.path,sha256:attachment.sha256,role:'Explicit source attachment'});f.bundle.referencedSources.push(attachment);f.bundle.sourceDocuments.push({...attachment});f.bundle.analysis.sourceDocuments=['source.bin'];f.save();
  assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
  f.bundle.sourceDocuments=[];assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-SOURCE');
});

test('execution proof needs all original files and its declared raw byte identities',()=>{
  const f=fixture();f.bundle.evidenceFiles.pop();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE');
  const g=fixture();g.bundle.evidenceFiles[0].text=g.bundle.evidenceFiles[0].text.replace('failures="0"','failures="1"');assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('SHA-256'));
  const h=fixture();h.bundle.evidenceFiles.push(text('unlisted.log','Unlisted outcome'));assert.throws(()=>validateAtlasBundle(h.bundle),error=>error.code==='ATLAS-EVIDENCE');
});

test('failed XML cannot keep passed labels after correctly rehashing its bytes',()=>{
  const f=fixture(),xml='<testsuite tests="1" failures="1" errors="0" skipped="0"><testcase name="observed"><failure/></testcase></testsuite>';
  f.bundle.evidenceFiles[0]=text('execution.xml',xml);f.bundle.analysis.evidence[0].sha256=hash(xml);f.save();
  assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('counters'));
});

test('unsupported byte formats retain historical scope and cannot establish an executed verdict',()=>{
  const f=fixture(),attachment=encoded('attachment.bin',Buffer.from([0,255,44,128]));f.bundle.evidenceFiles.push(attachment);
  f.bundle.analysis.evidence.push({id:'retained',projectId:'generic-project',path:attachment.path,sha256:attachment.sha256,format:'BINARY',status:'HISTORICAL',selection:'Original retained attachment',sourceIdentity:'Historical identity only'});f.save();
  assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');f.bundle.analysis.evidence[1].status='PASSED';f.save();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE');
});

test('a matching counter set cannot substitute another target or different executed source',()=>{
  const f=fixture();f.bundle.analysis.evidence[0].projectId='other-project';f.save();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE');
  const g=fixture(),provenance={manifest:{'contract.txt':hash('Different code'),'oracle.txt':g.bundle.sourceIndex.files[1].sha256}};
  g.bundle.evidenceFiles[1]=text('executed-source.json',JSON.stringify(provenance));g.bundle.analysis.evidence[0].sourceIdentity=g.bundle.evidenceFiles[1].sha256;g.save();assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('not the executed source'));
});

test('optional source-only unknown fields remain valid without fabricated execution',()=>{
  const f=fixture();Object.assign(f.bundle.analysis.claims[0],{status:'UNKNOWN',governanceStatus:'NOT_MAPPED'});delete f.bundle.analysis.claims[0].sourceRefs;delete f.bundle.analysis.claims[0].governanceRefs;delete f.bundle.analysis.claims[0].evidenceIds;delete f.bundle.analysis.claims[0].verdict;delete f.bundle.analysis.evidence;
  f.bundle.referencedSources=[];f.bundle.evidenceFiles=[];f.save();assert.equal(validateAtlasBundle(f.bundle).status,'PASSED');
});

test('Node TAP individual outcomes cannot be replaced by an altered passing summary',()=>{
  const f=fixture(),tap='TAP version 13\nnot ok 1 - rejected\n# tests 1\n# pass 1\n# fail 0\n# cancelled 0\n# skipped 0\n# todo 0\n';
  f.bundle.evidenceFiles[0]=text('execution.tap',tap);Object.assign(f.bundle.analysis.evidence[0],{path:'execution.tap',sha256:hash(tap),format:'NODE_TAP'});f.save();assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-EVIDENCE');
});

test('missing selected mandatory policy and a false semantic-completeness declaration fail',()=>{
  const f=fixture();f.bundle.knowledge.records=[];f.bundle.knowledge.selectedIds=[];assert.throws(()=>validateAtlasBundle(f.bundle),error=>error.code==='ATLAS-KNOWLEDGE');
  const g=fixture();g.bundle.consistency.semanticCompleteness='AUTOMATICALLY_COMPLETE';assert.throws(()=>validateAtlasBundle(g.bundle),error=>error.code==='ATLAS-SCHEMA');
});
