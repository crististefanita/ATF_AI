import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {verifyAtlas, surefireSummary, nodeTestSummary} from './verify-atlas.mjs';

const hash = bytes => createHash('sha256').update(bytes).digest('hex');
function fixture(t) {
  const temporary = fs.realpathSync(os.tmpdir()), root = fs.mkdtempSync(path.join(temporary, 'atf-atlas-test-'));
  t.after(() => {
    const resolved = path.resolve(root);
    assert.equal(path.dirname(resolved), temporary);
    assert.ok(path.basename(resolved).startsWith('atf-atlas-test-'));
    fs.rmSync(resolved, {recursive:true, force:true});
  });
  const sourceRoot = path.join(root,'source with spaces'), artifactRoot = path.join(root,'artifacts'), analysisRoot = path.join(artifactRoot,'atlas/run');
  fs.mkdirSync(sourceRoot); fs.mkdirSync(analysisRoot,{recursive:true});
  fs.writeFileSync(path.join(sourceRoot,'contract.txt'),'Observed contract\n');
  fs.writeFileSync(path.join(sourceRoot,'guard.txt'),'Independent guard\n');
  const index = {schemaVersion:1,projectId:'sample',revision:'UNVERSIONED',files:[
    {path:'contract.txt',role:'Contract',sha256:hash('Observed contract\n')},
    {path:'guard.txt',role:'Existing guard',sha256:hash('Independent guard\n')}
  ]};
  const sourceRef = {path:'contract.txt',startLine:1,endLine:1,quote:'Observed contract'};
  const guardRef = {path:'guard.txt',startLine:1,endLine:1,quote:'Independent guard'};
  const map = '# Project map\n\nCONTRACT-1: The contract has an existing independent guard.\n';
  fs.writeFileSync(path.join(analysisRoot,'project-map.md'),map);
  fs.writeFileSync(path.join(analysisRoot,'open-questions.md'),'# Proof boundaries\n\nNo universal semantics are inferred.\n');
  const manifest = {schemaVersion:1,projectId:'sample',revision:'UNVERSIONED',claims:[{
    id:'CONTRACT-1',statement:'The contract has an existing independent guard.',status:'SOURCE_REVIEWED',
    sourceRefs:[sourceRef],governanceStatus:'MAPPED',governanceRefs:[guardRef]
  }],documents:[],evidence:[]};
  const save = () => {
    fs.writeFileSync(path.join(analysisRoot,'source-index.json'),JSON.stringify(index));
    manifest.sourceIndexSha256 = hash(fs.readFileSync(path.join(analysisRoot,'source-index.json')));
    manifest.documents = ['project-map.md','open-questions.md'].map(relative=>({path:relative,sha256:hash(fs.readFileSync(path.join(analysisRoot,relative)))}));
    fs.writeFileSync(path.join(analysisRoot,'analysis.json'),JSON.stringify(manifest));
  };
  const proof = () => {
    const xml = '<testsuite tests="2" failures="0" errors="0" skipped="0"><testcase name="first"/><testcase name="second"/></testsuite>';
    fs.writeFileSync(path.join(artifactRoot,'results.xml'),xml);
    fs.writeFileSync(path.join(artifactRoot,'source.json'),JSON.stringify({manifest:Object.fromEntries(index.files.map(entry=>[entry.path,entry.sha256]))}));
    return {id:'run',projectId:'sample',path:'results.xml',sha256:hash(xml),format:'SUREFIRE_XML',status:'PASSED',tests:2,failures:0,errors:0,skipped:0,
      selection:'Existing guard selection',sourceManifest:'source.json',sourceIdentity:hash(fs.readFileSync(path.join(artifactRoot,'source.json')))};
  };
  save(); return {sourceRoot,artifactRoot,analysisRoot,index,manifest,save,proof};
}

test('source-reviewed claims retain exact source, governance and document identity', t => {
  const f=fixture(t), result=verifyAtlas(f);
  assert.equal(result.status,'PASSED'); assert.equal(result.sourceFiles,2); assert.equal(result.claims,1);
  assert.equal(result.semanticCompleteness,'HUMAN_REVIEW_REQUIRED'); assert.equal(result.scope,'DECLARED_SOURCE_AND_ARTIFACT_CONSISTENCY');
});
test('live source drift and edited generated prose are rejected independently', t => {
  const f=fixture(t);fs.appendFileSync(path.join(f.sourceRoot,'contract.txt'),'Changed\n');
  assert.throws(()=>verifyAtlas(f),/Live source drift/);
  fs.writeFileSync(path.join(f.sourceRoot,'contract.txt'),'Observed contract\n');
  fs.appendFileSync(path.join(f.analysisRoot,'project-map.md'),'An unsupported claim.');
  assert.throws(()=>verifyAtlas(f),/Generated document integrity/);
});
test('a source quote or line range cannot be replaced by a matching keyword', t => {
  const f=fixture(t);f.manifest.claims[0].sourceRefs[0].quote='contract';f.save();
  assert.throws(()=>verifyAtlas(f),/Source quote differs/);
  f.manifest.claims[0].sourceRefs[0].quote='Observed contract';f.manifest.claims[0].sourceRefs[0].endLine=900;f.save();
  assert.throws(()=>verifyAtlas(f),/Invalid source line range/);
});
test('target mismatches, unindexed references and undocumented governance gaps fail', t => {
  const f=fixture(t);f.manifest.projectId='other';f.save();assert.throws(()=>verifyAtlas(f),/target identity/);
  f.manifest.projectId='sample';f.manifest.claims[0].sourceRefs[0].path='missing.txt';f.save();assert.throws(()=>verifyAtlas(f),/outside the captured source/);
  f.manifest.claims[0].sourceRefs[0].path='contract.txt';f.manifest.claims[0].governanceRefs=[];f.save();assert.throws(()=>verifyAtlas(f),/governance references/);
});
test('executed claims require real XML counters and the same executed source bytes', t => {
  const f=fixture(t);f.manifest.evidence=[f.proof()];Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run']});f.save();
  assert.equal(verifyAtlas(f).status,'PASSED');
  const record=JSON.parse(fs.readFileSync(path.join(f.artifactRoot,'source.json'),'utf8'));record.manifest['guard.txt']=hash('A different test');
  fs.writeFileSync(path.join(f.artifactRoot,'source.json'),JSON.stringify(record));
  f.manifest.evidence[0].sourceIdentity=hash(fs.readFileSync(path.join(f.artifactRoot,'source.json')));f.save();
  assert.throws(()=>verifyAtlas(f),/not the executed source/);
});

test('live behavior execution rejects an unrelated testcase even with matching executed source bytes',t=>{
  const f=fixture(t);f.manifest.evidence=[f.proof()];
  Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run'],
    executionSubjects:[{evidenceId:'run',testClass:'',testName:'first',sourcePath:'guard.txt'}]});
  f.manifest.behaviorFlows=[{claimIds:['CONTRACT-1'],oracleClaimId:'CONTRACT-1'}];f.save();
  assert.equal(verifyAtlas(f).status,'PASSED');
  f.manifest.claims[0].executionSubjects[0].testName='an unrelated response oracle';f.save();
  assert.throws(()=>verifyAtlas(f),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('absent, ambiguous or skipped'));
});

test('mixed-suite behavior outcomes remain selected while the whole execution stays failed',t=>{
  const f=fixture(t),xml='<testsuite tests="2" failures="1" errors="0" skipped="0"><testcase name="first"/><testcase name="second"><failure/></testcase></testsuite>';
  f.manifest.evidence=[f.proof()];fs.writeFileSync(path.join(f.artifactRoot,'results.xml'),xml);
  Object.assign(f.manifest.evidence[0],{sha256:hash(xml),status:'FAILED',failures:1});
  Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run'],
    executionSubjects:[{evidenceId:'run',testClass:'',testName:'first',sourcePath:'guard.txt'}]});
  f.manifest.behaviorFlows=[{claimIds:['CONTRACT-1'],oracleClaimId:'CONTRACT-1'}];f.save();
  assert.equal(verifyAtlas(f).status,'PASSED');assert.equal(f.manifest.evidence[0].status,'FAILED');
  f.manifest.claims[0].executionSubjects[0].testName='second';f.save();
  assert.throws(()=>verifyAtlas(f),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('selected testcase outcomes'));
  f.manifest.claims[0].verdict='FAILED';f.save();assert.equal(verifyAtlas(f).status,'PASSED');
  f.manifest.claims[0].executionSubjects[0].testName='first';f.save();
  assert.throws(()=>verifyAtlas(f),error=>error.code==='ATLAS-EVIDENCE'&&error.observed.includes('selected testcase outcomes'));
});

test('unbound cohort claims retain the complete mixed-suite failure verdict',t=>{
  const f=fixture(t),xml='<testsuite tests="2" failures="1" errors="0" skipped="0"><testcase name="first"/><testcase name="second"><failure/></testcase></testsuite>';
  f.manifest.evidence=[f.proof()];fs.writeFileSync(path.join(f.artifactRoot,'results.xml'),xml);
  Object.assign(f.manifest.evidence[0],{sha256:hash(xml),status:'FAILED',failures:1});
  Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run']});f.save();
  assert.throws(()=>verifyAtlas(f),/Claim verdict differs from executed evidence/);
  f.manifest.claims[0].verdict='FAILED';f.save();assert.equal(verifyAtlas(f).status,'PASSED');
});
test('a failed XML report cannot be relabeled passed or replaced by HTML proof', t => {
  const f=fixture(t);f.manifest.evidence=[f.proof()];Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run']});
  const xml='<testsuite tests="2" failures="1" errors="0" skipped="0"><testcase name="first"><failure/></testcase><testcase name="second"/></testsuite>';fs.writeFileSync(path.join(f.artifactRoot,'results.xml'),xml);
  f.manifest.evidence[0].sha256=hash(xml);f.save();assert.throws(()=>verifyAtlas(f),/verdict\/counters/);
  Object.assign(f.manifest.evidence[0],{status:'FAILED',failures:1});f.save();assert.throws(()=>verifyAtlas(f),/Claim verdict/);
  f.manifest.claims[0].verdict='FAILED';f.save();assert.equal(verifyAtlas(f).status,'PASSED');
  Object.assign(f.manifest.evidence[0],{format:'HTML',status:'HISTORICAL'});f.save();assert.throws(()=>verifyAtlas(f),/unexecuted/);
});
test('execution evidence from another target cannot establish this project claim', t => {
  const f=fixture(t);f.manifest.evidence=[{...f.proof(),projectId:'other'}];Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run']});f.save();
  assert.throws(()=>verifyAtlas(f),/another project/);
});
test('normalized paths and real roots prevent source and artifact escape', t => {
  const f=fixture(t);f.index.files[0].path='../outside.txt';f.save();assert.throws(()=>verifyAtlas(f),/normalized relative/);
  f.index.files[0].path='contract.txt';f.save();assert.throws(()=>verifyAtlas({...f,artifactRoot:f.sourceRoot}),/selected artifact root/);
});
test('the CLI supports space-containing paths without shell expansion', t => {
  const f=fixture(t), cli=fileURLToPath(new URL('./verify-atlas.mjs',import.meta.url));
  const result=spawnSync(process.execPath,[cli,'--source',f.sourceRoot,'--analysis',f.analysisRoot,'--artifacts',f.artifactRoot],{encoding:'utf8',windowsHide:true});
  assert.equal(result.status,0,result.stderr);assert.equal(JSON.parse(result.stdout).projectId,'sample');
  assert.throws(()=>surefireSummary('<testsuite tests="0" failures="1" errors="0" skipped="0"/>'),/Contradictory/);
  assert.throws(()=>surefireSummary('<testsuite tests="2" failures="0" errors="0" skipped="0"/>'),/testcase outcomes/);
  assert.equal(surefireSummary('<testsuite tests="2" failures="0" errors="0" skipped="1"><testcase name="first"><skipped/></testcase><testcase name="second"/></testsuite>').status,'NOT_EXECUTED');
});

test('flat Node TAP evidence preserves failure and refuses counter-only claims', () => {
  const body='TAP version 13\nok 1 - observed\nnot ok 2 - rejected\n# tests 2\n# pass 1\n# fail 1\n# cancelled 0\n# skipped 0\n# todo 0\n';
  assert.deepEqual(nodeTestSummary(body),{tests:2,failures:1,errors:0,skipped:0,status:'FAILED'});
  assert.throws(()=>nodeTestSummary(body.replace('not ok 2 - rejected\n','')),/flat testcase outcomes/);
  assert.throws(()=>nodeTestSummary(body.replace('# fail 1','# fail 0')),/flat testcase outcomes/);
  assert.throws(()=>nodeTestSummary(body.replace('# fail 1','# fail 0').replace('# pass 1','# pass 2')),/flat testcase outcomes/);
});

test('Surefire mixed self-closing and body-bearing testcases retain independent outcomes', () => {
  const xml='<testsuite tests="4" failures="1" errors="1" skipped="0"><testcase name="passed"/><testcase name="failed"><failure message="observed"/></testcase><testcase name="error"><error/></testcase><testcase name="output"><system-out><![CDATA[<failure/>]]></system-out></testcase></testsuite>';
  assert.deepEqual(surefireSummary(xml),{tests:4,failures:1,errors:1,skipped:0,status:'FAILED'});
  assert.throws(()=>surefireSummary(xml.replace('tests="4"','tests="3"')),/testcase outcomes/);
});

test('explicit CRLF/LF equivalence cannot hide changed code or normalize library artifacts', t => {
  const f=fixture(t), proof=f.proof();f.manifest.evidence=[proof];Object.assign(f.manifest.claims[0],{status:'EXECUTED',verdict:'PASSED',evidenceIds:['run']});
  const provenance=JSON.parse(fs.readFileSync(path.join(f.artifactRoot,'source.json'),'utf8'));
  provenance.normalizedManifest={...provenance.manifest};
  fs.writeFileSync(path.join(f.artifactRoot,'source.json'),JSON.stringify(provenance));proof.sourceIdentity=hash(fs.readFileSync(path.join(f.artifactRoot,'source.json')));
  fs.writeFileSync(path.join(f.sourceRoot,'contract.txt'),'Observed contract\r\n');
  f.index.files[0].sha256=hash('Observed contract\r\n');f.index.files[0].normalizedSha256=hash('Observed contract\n');f.save();
  assert.throws(()=>verifyAtlas(f),/not the executed source/);
  f.manifest.sourceNormalization='CRLF_LF';f.save();assert.equal(verifyAtlas(f).sourceNormalization,'CRLF_LF');
  fs.writeFileSync(path.join(f.sourceRoot,'guard.txt'),'Different guard\n');f.index.files[1].sha256=hash('Different guard\n');f.index.files[1].normalizedSha256=hash('Different guard\n');
  f.manifest.claims[0].governanceRefs[0].quote='Different guard';f.save();assert.throws(()=>verifyAtlas(f),/not the executed source/);
  fs.mkdirSync(path.join(f.sourceRoot,'lib'));fs.writeFileSync(path.join(f.sourceRoot,'lib/library.pom'),'library');f.index.files.push({path:'lib/library.pom',role:'Published library POM',sha256:hash('library'),normalizedSha256:hash('library')});f.save();
  assert.throws(()=>verifyAtlas(f),/Invalid declared text normalization/);
  fs.mkdirSync(path.join(f.sourceRoot,'LIB'),{recursive:true});fs.writeFileSync(path.join(f.sourceRoot,'LIB/library.pom'),'library');f.index.files.at(-1).path='LIB/library.pom';f.save();
  assert.throws(()=>verifyAtlas(f),/Invalid declared text normalization/);
});

test('malformed intake JSON retains a stable schema finding and the actual input owner',t=>{
  const f=fixture(t);fs.writeFileSync(path.join(f.analysisRoot,'analysis.json'),'{invalid');
  assert.throws(()=>verifyAtlas(f),error=>error.code==='ATLAS-SCHEMA'&&error.observed.startsWith('Invalid JSON input: analysis.json')&&error.message.includes('Repair hint:'));
});

test('declared Git inventory catches added files without hiding source packages named target', t => {
  const f=fixture(t),run=args=>{const result=spawnSync('git',args,{cwd:f.sourceRoot,encoding:'utf8',windowsHide:true});assert.equal(result.status,0,result.stderr);return result.stdout.trim();};
  run(['init','--quiet']);run(['add','.']);run(['-c','user.name=Atlas Fixture','-c','user.email=atlas-fixture@example.invalid','commit','--quiet','-m','Fixture source']);
  f.index.revision=run(['rev-parse','HEAD']);f.manifest.revision=f.index.revision;f.index.inventory={kind:'GIT_RELEVANT',excludedRoots:['generated']};f.save();
  assert.equal(verifyAtlas(f).status,'PASSED');fs.mkdirSync(path.join(f.sourceRoot,'generated'));fs.writeFileSync(path.join(f.sourceRoot,'generated','receipt.txt'),'Generated only');
  assert.equal(verifyAtlas(f).status,'PASSED');fs.mkdirSync(path.join(f.sourceRoot,'src/target'),{recursive:true});fs.writeFileSync(path.join(f.sourceRoot,'src/target','Owned.java'),'class Owned {}');
  assert.throws(()=>verifyAtlas(f),error=>error.code==='ATLAS-SOURCE'&&error.observed.includes('src/target/Owned.java'));
});
