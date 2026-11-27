import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
import {collectArtifacts, artifactKinds, validateArtifactPublication} from './collect-artifacts.mjs';

function fixture(t) {
  const temporary = fs.realpathSync(os.tmpdir());
  const root = fs.mkdtempSync(path.join(temporary, 'atf-artifact-test-'));
  t.after(() => {
    const resolved = path.resolve(root);
    assert.equal(path.dirname(resolved), temporary);
    assert.ok(path.basename(resolved).startsWith('atf-artifact-test-'));
    fs.rmSync(resolved, {recursive:true, force:true});
  });
  const source = path.join(root,'source with spaces'), output = path.join(root,'archive');
  fs.mkdirSync(source);
  const write = (relative, content) => {const file=path.join(source,relative); fs.mkdirSync(path.dirname(file),{recursive:true}); fs.writeFileSync(file,content); return file;};
  return {root, source, output, write};
}

test('native collation cannot change ordinal evidence ordering or preserved bytes',t=>{
  const f=fixture(t),names=['A','Å','ä','z','😀'];f.write('pom.xml','<project/>');
  for(const name of names)f.write('target/reports/'+name+' report.html','<p>'+name+'</p>');
  const moduleUrl=new URL('./collect-artifacts.mjs',import.meta.url).href;
  const worker=`import {collectArtifacts} from ${JSON.stringify(moduleUrl)};
    const [source,output,locale]=process.argv.slice(1),original=String.prototype.localeCompare;
    String.prototype.localeCompare=function(other){return original.call(this,other,locale);};
    try{const {manifest}=collectArtifacts({sourceRoot:source,outputRoot:output,runId:'ordinal-input',revision:'c0ffee0'});
      console.log(JSON.stringify(manifest.files.map(({path,sha256})=>({path,sha256}))));
    }finally{String.prototype.localeCompare=original;}`;
  const expected=names.map(name=>({path:'files/target/reports/'+name+' report.html',sha256:createHash('sha256').update('<p>'+name+'</p>').digest('hex')})).sort((a,b)=>a.path<b.path?-1:a.path>b.path?1:0);
  for(const locale of ['en','sv']){
    const observed=spawnSync(process.execPath,['--input-type=module','-e',worker,f.source,path.join(f.output,locale),locale],{encoding:'utf8',windowsHide:true,timeout:30000});
    assert.equal(observed.status,0,observed.stderr);assert.deepEqual(JSON.parse(observed.stdout),expected);
  }
});

test('all report families retain their bytes, relative assets and source evidence', t => {
  const f=fixture(t);
  f.write('pom.xml','<project/>'); f.write('atf-tests/pom.xml','<project/>');
  f.write('atf-tests/target/classes/Ignore.class','compiled');
  f.write('atf-tests/target/reports/report #1.html','<link href="assets/theme.css"><img src="../screenshots/state.png">');
  f.write('atf-tests/target/reports/assets/theme.css','body{color:blue}');
  f.write('atf-tests/target/reports/assets/Diagnostic.java','original attached source evidence');
  f.write('atf-tests/target/screenshots/state.png',Buffer.from([0,1,2,255]));
  f.write('atf-tests/target/surefire-reports/TEST-Failed.xml','<testsuite failures="1"/>');
  f.write('atf-tests/allure-results/failed-result.json','{"status":"failed"}');
  f.write('atf-tests/target/atf-core-aggregate-report/index.html','original aggregate');
  f.write('atf-tests/target/library.jar',Buffer.from([80,75,1]));
  f.write('atf-tests/.codex-artifacts/unknown.data','keep unclassified evidence');
  const evidence=path.join(f.root,'execution.log'); fs.writeFileSync(evidence,'BUILD FAILURE');
  const result=collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId:'observed-failure',evidence:[evidence],revision:'abcdef0'});
  assert.equal(result.manifest.collectionStatus,'COMPLETE');
  assert.equal(result.manifest.runId,'observed-failure');
  assert.equal(result.manifest.executionVerdict,'NOT_INFERRED');
  assert.equal(result.manifest.sourceRevision,'abcdef0');
  for (const kind of artifactKinds) assert.equal(result.manifest.catalog[kind].status,'COLLECTED',kind);
  assert.equal(fs.readFileSync(path.join(result.destination,'files/atf-tests/target/reports/report #1.html'),'utf8'),'<link href="assets/theme.css"><img src="../screenshots/state.png">');
  assert.deepEqual(fs.readFileSync(path.join(result.destination,'files/atf-tests/target/screenshots/state.png')),Buffer.from([0,1,2,255]));
  assert.equal(fs.readFileSync(path.join(result.destination,'files/atf-tests/target/surefire-reports/TEST-Failed.xml'),'utf8'),'<testsuite failures="1"/>');
  assert.ok(fs.readFileSync(path.join(result.destination,'index.html'),'utf8').includes('report%20%231.html'));
  assert.ok(!result.manifest.files.some(file=>file.path.endsWith('Ignore.class')));
  assert.ok(result.manifest.files.every(file=>/^[a-f0-9]{64}$/.test(file.sha256)));
  assert.equal(fs.readFileSync(path.join(result.destination,'files/atf-tests/target/reports/assets/Diagnostic.java'),'utf8'),'original attached source evidence');
  assert.equal(result.manifest.files.length,10);
});

test('invalid IDs and recursive output roots fail before a run is created', t => {
  const f=fixture(t);
  for (const runId of ['', '../escape', 'nested/run', 'run.', 'CON', 'nul.json', 'a:b']) {
    assert.throws(()=>collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId}),/portable nonempty run ID/);
  }
  assert.ok(!fs.existsSync(f.output));
  assert.throws(()=>collectArtifacts({sourceRoot:f.source,outputRoot:path.join(f.source,'archive'),runId:'run'}),/outside/);
  assert.ok(!fs.existsSync(path.join(f.source,'archive')));
  const alias=path.join(f.root,'source-alias');
  fs.symlinkSync(f.source,alias,process.platform==='win32'?'junction':'dir');
  assert.throws(()=>collectArtifacts({sourceRoot:f.source,outputRoot:path.join(alias,'archive'),runId:'alias'}),/outside/);
  assert.ok(!fs.existsSync(path.join(f.source,'archive')));
});

test('an existing archive is never overwritten', t => {
  const f=fixture(t); f.write('target/reports/index.html','first');
  const options={sourceRoot:f.source,outputRoot:f.output,runId:'retained'};
  const first=collectArtifacts(options);
  const before=fs.readFileSync(path.join(first.destination,'manifest.json'));
  f.write('target/reports/index.html','second');
  assert.throws(()=>collectArtifacts(options),{code:'EEXIST'});
  assert.deepEqual(fs.readFileSync(path.join(first.destination,'manifest.json')),before);
  assert.equal(fs.readFileSync(path.join(first.destination,'files/target/reports/index.html'),'utf8'),'first');
});

test('a linked report leaves an explicit failed collection and no complete manifest', t => {
  const f=fixture(t), outside=path.join(f.root,'outside');
  fs.mkdirSync(outside); fs.writeFileSync(path.join(outside,'sentinel.txt'),'untouched');
  fs.mkdirSync(path.join(f.source,'target/reports'),{recursive:true});
  fs.symlinkSync(outside,path.join(f.source,'target/reports/linked'),process.platform==='win32'?'junction':'dir');
  assert.throws(()=>collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId:'linked'}),/must not contain links/);
  const run=path.join(f.output,'linked');
  assert.equal(JSON.parse(fs.readFileSync(path.join(run,'collection-status.json'))).status,'FAILED');
  assert.ok(!fs.existsSync(path.join(run,'manifest.json')));
  assert.equal(fs.readFileSync(path.join(outside,'sentinel.txt'),'utf8'),'untouched');
});

test('source mutation during copying cannot produce a complete archive', t => {
  const f=fixture(t); f.write('target/reports/index.html','stable at start');
  const copy=fs.copyFileSync;
  t.mock.method(fs,'copyFileSync',(from,to,flags)=>{copy(from,to,flags); fs.appendFileSync(from,' changed');});
  assert.throws(()=>collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId:'changed'}),/Source changed/);
  assert.ok(!fs.existsSync(path.join(f.output,'changed/manifest.json')));
  assert.equal(JSON.parse(fs.readFileSync(path.join(f.output,'changed/collection-status.json'))).status,'FAILED');
});

test('explicit custom output roots preserve their full asset subtree', t => {
  const f=fixture(t), custom=path.join(f.root,'custom report');
  fs.mkdirSync(path.join(custom,'assets'),{recursive:true});
  fs.writeFileSync(path.join(custom,'index.html'),'<img src="assets/image.bin">');
  fs.writeFileSync(path.join(custom,'assets/image.bin'),Buffer.from([8,9]));
  const result=collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId:'custom',includes:[{kind:'allure',root:custom}]});
  assert.equal(result.manifest.catalog.allure.files.length,2);
  assert.ok(result.manifest.files.every(file=>file.kind==='allure'));
  assert.deepEqual(fs.readFileSync(path.join(result.destination,'files/_included/0/custom report/assets/image.bin')),Buffer.from([8,9]));
});

test('an empty producer remains NOT_FOUND and has no inferred execution result', t => {
  const f=fixture(t);
  const result=collectArtifacts({sourceRoot:f.source,outputRoot:f.output,runId:'empty'});
  assert.equal(result.manifest.files.length,0);
  assert.equal(result.manifest.executionVerdict,'NOT_INFERRED');
  assert.ok(Object.values(result.manifest.catalog).every(kind=>kind.status==='NOT_FOUND'));
});

test('the CLI handles space-containing paths without a shell and rejects unknown options', t => {
  const f=fixture(t); f.write('target/reports/index.html','report');
  const script=fileURLToPath(new URL('./collect-artifacts.mjs',import.meta.url));
  const args=[script,'--source',f.source,'--output',f.output,'--run-id','cli'];
  const bad=spawnSync(process.execPath,[...args,'--unknown','value'],{encoding:'utf8',windowsHide:true,timeout:10000});
  assert.notEqual(bad.status,0); assert.match(bad.stderr,/Unknown or repeated option/);
  assert.ok(!fs.existsSync(f.output));
  const good=spawnSync(process.execPath,args,{encoding:'utf8',windowsHide:true,timeout:10000});
  assert.equal(good.status,0,good.stderr);
  assert.equal(JSON.parse(good.stdout).executionVerdict,'NOT_INFERRED');
  assert.ok(fs.existsSync(path.join(f.output,'cli/manifest.json')));
});

function publicationFixture(t, {published = true} = {}) {
  const base = fixture(t), artifactRoot = path.join(base.root, 'selected publication');
  const write = (relative, content) => {
    const file = path.join(artifactRoot, ...relative.split('/'));
    fs.mkdirSync(path.dirname(file), {recursive:true}); fs.writeFileSync(file, content);
    return {path:relative, bytes:Buffer.byteLength(content), sha256:createHash('sha256').update(content).digest('hex')};
  };
  for (const relative of ['README.md', 'atlas/README.md', 'atlas/Project_Map_Template.md', 'reports/README.md']) write(relative, '# Maintained owner\n');
  const manifest = {schemaVersion:1, role:'CURRENT_PUBLICATION', history:{id:'preserved-01', sourceRevision:'a'.repeat(40), inventorySha256:'b'.repeat(64)}, atlases:[], reports:[], documents:[]};
  const save = () => write('publication.json', JSON.stringify(manifest, null, 2) + '\n');
  if (published) {
    for (const projectId of ['atf-ai', 'other-project']) manifest.atlases.push({projectId, sourceRevision:projectId === 'atf-ai' ? 'a'.repeat(40) : 'UNVERSIONED',
      ...write('atlas/current/' + projectId + '/atlas.html', '<!doctype html><style>body{color:#123}</style><h1>' + projectId + '</h1>')});
    manifest.reports.push({id:'native-xml', kind:'test-results', sourceRevision:'a'.repeat(40), executionScope:'Existing selected cases; retained original failure',
      ...write('reports/current/java/TEST-Failed.xml', '<testsuite tests="1" failures="1" errors="0" skipped="0"><testcase name="original failure"><failure/></testcase></testsuite>')});
    manifest.reports.push({id:'native-tap', kind:'test-results', ...write('reports/current/node/original result.tap', 'TAP version 13\nnot ok 1 - original failure\n1..1\n# fail 1\n')});
    for (const relative of ['atlas/current/README.md', 'atlas/current/index.html', 'reports/current/README.md', 'reports/current/index.html']) {
      manifest.documents.push(write(relative, relative.endsWith('.md') ? '# Selected original evidence\n' : '<!doctype html><h1>Selected original evidence</h1>'));
    }
    save();
  }
  return {...base, artifactRoot, write, manifest, save};
}

function publicationState(root) {
  const state = [];
  const visit = relative => {
    const file = path.join(root, relative), value = fs.lstatSync(file);
    state.push({path:relative, mtimeMs:value.mtimeMs, bytes:value.isFile() ? fs.readFileSync(file).toString('base64') : null});
    if (value.isDirectory() && !value.isSymbolicLink()) for (const name of fs.readdirSync(file).sort()) visit(path.join(relative, name));
  };
  visit(''); return state;
}

function publicationFailure(f, observed, suppliedPath) {
  const before = publicationState(f.artifactRoot);
  assert.throws(() => validateArtifactPublication({artifactRoot:f.artifactRoot}), error => {
    assert.match(error.message, /^ARTIFACT-PUBLICATION\nObserved: /);
    assert.ok(error.message.includes(observed), error.message);
    if (suppliedPath !== undefined) assert.ok(error.message.includes('path=' + JSON.stringify(suppliedPath)), error.message);
    assert.match(error.message, /Failure meaning: /);
    assert.match(error.message, /Repair hint: /);
    assert.match(error.message, /Owner: artifacts\/README\.md#ownership/);
    assert.match(error.message, /Recheck: node tools\/collect-artifacts\.mjs --check-layout <artifact-root>/);
    return true;
  });
  assert.deepEqual(publicationState(f.artifactRoot), before, 'A rejected layout must not change file bytes or modification times.');
}

test('publication validation accepts a clean source-only tree without generating directories or files', t => {
  const f = publicationFixture(t, {published:false}), before = publicationState(f.artifactRoot);
  assert.deepEqual(validateArtifactPublication({artifactRoot:f.artifactRoot}), {status:'SOURCE_ONLY', atlases:0, reports:0, files:0});
  assert.deepEqual(publicationState(f.artifactRoot), before);
  fs.unlinkSync(path.join(f.artifactRoot, 'atlas/Project_Map_Template.md'));
  publicationFailure(f, 'atlas/Project_Map_Template.md');
});

test('current readers bind selected projects and failed native proof without inferring execution success', t => {
  const f = publicationFixture(t), before = publicationState(f.artifactRoot);
  assert.deepEqual(validateArtifactPublication({artifactRoot:f.artifactRoot}), {status:'COMPLETE', role:'CURRENT_PUBLICATION', atlases:2, reports:2, files:8});
  assert.deepEqual(publicationState(f.artifactRoot), before);
  assert.match(fs.readFileSync(path.join(f.artifactRoot, f.manifest.reports[0].path), 'utf8'), /failures="1"/);
  assert.match(fs.readFileSync(path.join(f.artifactRoot, f.manifest.reports[1].path), 'utf8'), /not ok 1/);
});

test('an explicitly empty current selection still has four bound reader entry points', t => {
  const f = publicationFixture(t);
  for (const entry of [...f.manifest.atlases, ...f.manifest.reports]) fs.unlinkSync(path.join(f.artifactRoot, entry.path));
  for (const relative of ['atlas/current/atf-ai', 'atlas/current/other-project', 'reports/current/java', 'reports/current/node']) fs.rmdirSync(path.join(f.artifactRoot, relative));
  f.manifest.atlases = []; f.manifest.reports = []; f.save();
  assert.deepEqual(validateArtifactPublication({artifactRoot:f.artifactRoot}), {status:'COMPLETE', role:'CURRENT_PUBLICATION', atlases:0, reports:0, files:4});
});

test('legacy runs, releases and temporary output are rejected even without a publication manifest', t => {
  for (const relative of ['atlas/runs', 'reports/runs', 'release', 'releases', 'build', 'target', 'tmp', 'temp', 'unowned']) {
    const f = publicationFixture(t, {published:false});
    fs.mkdirSync(path.join(f.artifactRoot, relative), {recursive:true});
    publicationFailure(f, relative);
  }
});

test('current files cannot bypass their manifest and required current documents', t => {
  const missingManifest = publicationFixture(t);
  fs.unlinkSync(path.join(missingManifest.artifactRoot, 'publication.json'));
  publicationFailure(missingManifest, 'publication.json');
  const missingDocument = publicationFixture(t);
  missingDocument.manifest.documents.pop(); missingDocument.save();
  publicationFailure(missingDocument, 'reports/current/index.html');
  const missingProof = publicationFixture(t);
  fs.unlinkSync(path.join(missingProof.artifactRoot, missingProof.manifest.reports[0].path));
  publicationFailure(missingProof, missingProof.manifest.reports[0].path);
});

test('a selected project cannot acquire another HTML, stylesheet or orphan directory', t => {
  for (const relative of ['atlas/current/atf-ai/second.html', 'atlas/current/atf-ai/theme.css', 'reports/current/unselected.log']) {
    const f = publicationFixture(t); f.write(relative, 'unselected bytes'); publicationFailure(f, relative);
  }
  const f = publicationFixture(t);
  fs.mkdirSync(path.join(f.artifactRoot, 'atlas/current/undeclared-project'));
  publicationFailure(f, 'atlas/current/undeclared-project');
});

test('manifest-bound history, temporary folders, build payloads and HTML sites cannot bypass the current perimeter', t => {
  for (const directory of ['runs', 'history', 'archive', 'target', 'build', 'release', 'releases', 'tmp', 'temp', '.tmp', '.codex-artifacts', 'allure-results']) {
    const f = publicationFixture(t), relative = 'reports/current/selected/' + directory + '/original.json';
    f.manifest.reports.push({id:'additional-proof', kind:'execution', ...f.write(relative, '{"original":true}')});
    f.save(); publicationFailure(f, relative);
  }
  for (const extension of ['jar', 'ZIP', 'war', 'class', 'html', 'HTM', 'xhtml']) {
    const f = publicationFixture(t), relative = 'reports/current/renamed-build.' + extension;
    f.manifest.reports.push({id:'renamed-build', kind:'execution', ...f.write(relative, 'original build bytes')});
    f.save(); publicationFailure(f, relative);
  }
  const buildCategory = publicationFixture(t), relative = 'reports/current/build-proof.json';
  buildCategory.manifest.reports.push({id:'build-category', kind:'build', ...buildCategory.write(relative, '{}')});
  buildCategory.save(); publicationFailure(buildCategory, relative);
  const legitimate = publicationFixture(t);
  legitimate.manifest.reports.push({id:'release-check', kind:'execution', ...legitimate.write('reports/current/release-check.json', '{"scope":"native proof"}')});
  legitimate.save(); assert.equal(validateArtifactPublication({artifactRoot:legitimate.artifactRoot}).reports, 3);
});

test('changed bytes, wrong sizes and malformed metadata cannot pass publication integrity', t => {
  const changed = publicationFixture(t), selected = changed.manifest.atlases[0];
  changed.write(selected.path, 'x'.repeat(selected.bytes));
  publicationFailure(changed, selected.path);
  const wrongSize = publicationFixture(t); wrongSize.manifest.reports[0].bytes++; wrongSize.save();
  publicationFailure(wrongSize, wrongSize.manifest.reports[0].path);
  for (const change of [m => m.role = 'EXECUTION_PASSED', m => m.schemaVersion = 2,
    m => m.history.inventorySha256 = 'not-a-digest', m => m.history.sourceRevision = 'main', m => m.atlases[0].sourceRevision = 'HEAD',
    m => m.reports[0].kind = 'SUREFIRE_XML', m => m.reports[0].sourceRevision = 'abcdef0',
    m => m.reports[0].executionScope = '', m => m.documents = {}]) {
    const f = publicationFixture(t); change(f.manifest); f.save(); publicationFailure(f, 'publication.json');
  }
  const invalidBytes = publicationFixture(t); invalidBytes.manifest.reports[0].bytes = -1; invalidBytes.save(); publicationFailure(invalidBytes, invalidBytes.manifest.reports[0].path);
  const invalidJson = publicationFixture(t); invalidJson.write('publication.json', '{invalid'); publicationFailure(invalidJson, 'publication.json');
});

test('case-colliding identifiers and duplicate paths cannot select ambiguous evidence', t => {
  const projects = publicationFixture(t); projects.manifest.atlases[1].projectId = 'ATF-AI'; projects.save(); publicationFailure(projects, 'publication.json#atlases/1');
  const ids = publicationFixture(t); ids.manifest.reports[1].id = 'NATIVE-XML'; ids.save(); publicationFailure(ids, 'publication.json#reports/1');
  const paths = publicationFixture(t); paths.manifest.reports[1].path = paths.manifest.reports[0].path.toUpperCase().replace('REPORTS/CURRENT/', 'reports/current/');
  paths.save(); publicationFailure(paths, paths.manifest.reports[1].path);
  const docs = publicationFixture(t); docs.manifest.documents.push({...docs.manifest.documents[0]}); docs.save(); publicationFailure(docs, 'atlas/current/README.md');
});

test('publication paths cannot escape, use unsafe filenames or masquerade as another project reader', t => {
  for (const relative of ['reports/current/../outside.xml', 'reports/current//empty.xml', 'reports/current/' + 'C:' + '/outside.xml',
    'reports/current/\\outside.xml', '/reports/current/absolute.xml', 'reports/current/CON.xml', 'reports/current/trailing.']) {
    const f = publicationFixture(t); f.manifest.reports[0].path = relative; f.save(); publicationFailure(f, 'publication.json#reports/0', relative);
  }
  const f = publicationFixture(t); f.manifest.atlases[0].path = 'atlas/current/other-project/atlas.html'; f.save(); publicationFailure(f, 'publication.json#atlases/0', f.manifest.atlases[0].path);
});

test('linked publication directories cannot expose or modify outside evidence', t => {
  const f = publicationFixture(t), outside = path.join(f.root, 'outside publication');
  fs.mkdirSync(outside); fs.writeFileSync(path.join(outside, 'sentinel.txt'), 'untouched');
  fs.symlinkSync(outside, path.join(f.artifactRoot, 'reports/current/linked'), process.platform === 'win32' ? 'junction' : 'dir');
  publicationFailure(f, 'reports/current/linked');
  assert.equal(fs.readFileSync(path.join(outside, 'sentinel.txt'), 'utf8'), 'untouched');
});

test('the layout CLI is read-only and rejects mixed collection arguments', t => {
  const f = publicationFixture(t), before = publicationState(f.artifactRoot);
  const script = fileURLToPath(new URL('./collect-artifacts.mjs', import.meta.url));
  const accepted = spawnSync(process.execPath, [script, '--check-layout', f.artifactRoot], {encoding:'utf8', windowsHide:true, timeout:30000});
  assert.equal(accepted.status, 0, accepted.stderr); assert.equal(JSON.parse(accepted.stdout).status, 'COMPLETE');
  const rejected = spawnSync(process.execPath, [script, '--check-layout', f.artifactRoot, '--source', f.source], {encoding:'utf8', windowsHide:true, timeout:30000});
  assert.notEqual(rejected.status, 0); assert.match(rejected.stderr, /without collection options/);
  assert.deepEqual(publicationState(f.artifactRoot), before);
});

function retainedE2ePublication(f, engine, {runId = 'retained-' + engine, sourceRevision = 'c'.repeat(40), skipped = false} = {}) {
  const prefix = 'reports/current/e2e/' + engine + '/';
  const identity = {uiEngine:engine, runId, sourceRevision};
  const proof = (name, kind, content) => {
    const entry = {id:engine + '-' + name.replaceAll('.', '-'), kind, ...identity, ...f.write(prefix + name, content)};
    f.manifest.reports.push(entry); return entry;
  };
  const native = proof('native.xml', 'test-results', skipped
    ? '<testsuite tests="1" failures="0" errors="0" skipped="1"><testcase name="original skip"><skipped/></testcase></testsuite>'
    : '<testsuite tests="1" failures="1" errors="0" skipped="0"><testcase name="original failure"><failure/></testcase></testsuite>');
  const source = proof('source.json', 'execution', JSON.stringify({sourceCommit:sourceRevision, runId, engine}));
  const audit = proof('audit.json', 'execution', JSON.stringify({status:skipped ? 'SKIPPED' : 'FAILED', runId, engine}));
  const reader = {id:engine + '-extent', kind:'extent', ...identity,
    executionScope:'Original retained application methods; execution outcome remains in native proof',
    nativeResultsPath:native.path, executedSourcePath:source.path, auditPath:audit.path,
    ...f.write(prefix + 'ExtentReport.html', '<!doctype html><h1>Original ' + engine + ' E2E report</h1>')};
  f.manifest.reports.push(reader); f.save();
  return {reader, native, source, audit};
}

test('two engine-specific E2E readers bind original failed or skipped proof without inferring a passing execution', t => {
  const f = publicationFixture(t), playwright = retainedE2ePublication(f, 'playwright'), selenium = retainedE2ePublication(f, 'selenium', {skipped:true});
  const before = publicationState(f.artifactRoot), result = validateArtifactPublication({artifactRoot:f.artifactRoot});
  assert.deepEqual(result, {status:'COMPLETE', role:'CURRENT_PUBLICATION', atlases:2, reports:10, files:16});
  assert.equal(result.executionVerdict, undefined, 'A valid publication is not a passing test verdict.');
  assert.match(fs.readFileSync(path.join(f.artifactRoot, playwright.native.path), 'utf8'), /failures="1"/);
  assert.match(fs.readFileSync(path.join(f.artifactRoot, selenium.native.path), 'utf8'), /skipped="1"/);
  assert.deepEqual(publicationState(f.artifactRoot), before);
});

test('E2E readers require an exact engine, portable run identity, captured revision and explicit execution scope', t => {
  for (const [field, value, observed] of [
    ['uiEngine', undefined, 'ExtentReport.html'], ['uiEngine', 'selenium', 'ExtentReport.html'], ['uiEngine', 'Playwright', 'ExtentReport.html'],
    ['runId', undefined, 'ExtentReport.html'], ['runId', '../escape', 'ExtentReport.html'], ['runId', 'CON', 'ExtentReport.html'],
    ['sourceRevision', undefined, 'ExtentReport.html'], ['sourceRevision', 'UNVERSIONED', 'ExtentReport.html'], ['sourceRevision', 'HEAD', 'publication.json'],
    ['executionScope', undefined, 'ExtentReport.html'], ['executionScope', ' ', 'publication.json']
  ]) {
    const f = publicationFixture(t), {reader} = retainedE2ePublication(f, 'playwright');
    reader[field] = value; f.save(); publicationFailure(f, observed);
  }
});

test('each E2E proof reference is declared, distinct, correctly classified and bound to the same engine run and source', t => {
  for (const proofName of ['native', 'source', 'audit']) {
    for (const [field, value] of [['kind', 'browser'], ['runId', 'another-run'], ['sourceRevision', 'd'.repeat(40)], ['uiEngine', 'selenium']]) {
      const f = publicationFixture(t), selected = retainedE2ePublication(f, 'playwright');
      selected[proofName][field] = value; f.save(); publicationFailure(f, 'reports/current/e2e/playwright/');
    }
    const f = publicationFixture(t), selected = retainedE2ePublication(f, 'playwright');
    f.manifest.reports = f.manifest.reports.filter(entry => entry !== selected[proofName]); f.save();
    publicationFailure(f, 'ExtentReport.html');
  }
  for (const field of ['nativeResultsPath', 'executedSourcePath', 'auditPath']) {
    for (const reference of [undefined, 'reports/current/e2e/selenium/other.json', 'reports/current/native.xml',
      'reports/current/e2e/playwright/../escape.json', 'reports/current/e2e/playwright/missing.json', 'reports/current/e2e/playwright/ExtentReport.html']) {
      const f = publicationFixture(t), {reader} = retainedE2ePublication(f, 'playwright');
      reader[field] = reference; f.save(); publicationFailure(f, 'ExtentReport.html#' + field, reference);
    }
  }
  const duplicate = publicationFixture(t), selected = retainedE2ePublication(duplicate, 'playwright');
  selected.reader.auditPath = selected.reader.executedSourcePath; duplicate.save(); publicationFailure(duplicate, 'ExtentReport.html#auditPath');
  const wrongCase = publicationFixture(t), original = retainedE2ePublication(wrongCase, 'playwright');
  original.reader.executedSourcePath = original.source.path.replace('source.json', 'SOURCE.json'); wrongCase.save();
  publicationFailure(wrongCase, 'ExtentReport.html#executedSourcePath');
});

test('an E2E proof subtree cannot outlive its selected reader or accumulate unreferenced evidence', t => {
  const orphan = publicationFixture(t), selected = retainedE2ePublication(orphan, 'playwright');
  orphan.manifest.reports = orphan.manifest.reports.filter(entry => entry !== selected.reader); orphan.save();
  publicationFailure(orphan, selected.native.path);
  const extra = publicationFixture(t), owned = retainedE2ePublication(extra, 'playwright');
  extra.manifest.reports.push({id:'unreferenced', kind:'execution', uiEngine:'playwright', runId:owned.reader.runId,
    sourceRevision:owned.reader.sourceRevision, ...extra.write('reports/current/e2e/playwright/unreferenced.json', '{}')});
  extra.save(); publicationFailure(extra, 'unreferenced.json');
  const duplicate = publicationFixture(t), original = retainedE2ePublication(duplicate, 'selenium');
  duplicate.manifest.reports.push({...original.reader, id:'second-selenium-reader'}); duplicate.save();
  publicationFailure(duplicate, original.reader.path);
  for (const relative of ['reports/current/E2E/playwright/proof.json', 'reports/current/e2e/Playwright/proof.json',
    'reports/current/e2e/firefox/proof.json', 'reports/current/e2e/orphan.json']) {
    const f = publicationFixture(t);
    f.manifest.reports.push({id:'orphan-proof', kind:'execution', uiEngine:'playwright', runId:'retained', sourceRevision:'c'.repeat(40), ...f.write(relative, '{}')});
    f.save(); publicationFailure(f, relative);
  }
});

test('only the canonical extent E2E reader route can use the HTML exception', t => {
  for (const [relative, kind] of [
    ['reports/current/e2e/playwright/ExtentReport.html', 'execution'],
    ['reports/current/e2e/playwright/ExtentReport.html', 'allure'],
    ['reports/current/e2e/playwright/ExtentReport.html', 'aggregate'],
    ['reports/current/e2e/playwright/ExtentReport.html', 'test-results'],
    ['reports/current/e2e/playwright/second.html', 'extent'],
    ['reports/current/e2e/playwright/ExtentReport.HTML', 'extent'],
    ['reports/current/e2e/playwright/ExtentReport.htm', 'extent'],
    ['reports/current/e2e/playwright/ExtentReport.xhtml', 'extent'],
    ['reports/current/generic/ExtentReport.html', 'extent'],
    ['reports/current/ExtentReport.html', 'extent']
  ]) {
    const f = publicationFixture(t);
    f.manifest.reports.push({id:'disguised-html', kind, uiEngine:'playwright', runId:'retained', sourceRevision:'c'.repeat(40),
      executionScope:'Retained execution', ...f.write(relative, '<!doctype html><h1>Unowned HTML</h1>')});
    f.save(); publicationFailure(f, relative);
  }
});

function currentBusinessEnvelope(projectId){const ids=['overview','capabilities','journeys','operating-model','confidence','limits'];return '<!doctype html><html lang="en" data-atlas-reader="5" data-project-id="'+projectId+'"><head><style>body{color:#123}</style></head><body><nav>'+ids.map(id=>'<a href="#'+id+'">Read the section</a>').join('')+'</nav>'+ids.map((id,i)=>'<section id="'+id+'" data-atlas-section="'+id+'" data-section-number="0'+(i+1)+'"><h2>Project understanding</h2></section>').join('')+'</body></html>';}
test('current reader declarations and numbered envelopes are checked without claiming a full audit',t=>{
  const f=publicationFixture(t),entry=f.manifest.atlases[0];Object.assign(entry,{readerVersion:5,audience:'BUSINESS_PROJECT_UNDERSTANDING',...f.write(entry.path,currentBusinessEnvelope(entry.projectId))});f.save();const before=publicationState(f.artifactRoot);assert.equal(validateArtifactPublication({artifactRoot:f.artifactRoot}).atlases,2);assert.deepEqual(publicationState(f.artifactRoot),before);
});
test('business reader selection refuses hidden payloads, numbering drift and mixed target or audience',t=>{
  for(const change of [html=>html.replace('</body>','<template>raw proof</template></body>'),html=>html.replace('data-section-number="02"','data-section-number="01"'),html=>html.replace('data-project-id="atf-ai"','data-project-id="foreign-project"'),html=>html.replace('href="#journeys"','href="#limits"')]){
    const f=publicationFixture(t),entry=f.manifest.atlases[0];Object.assign(entry,{readerVersion:5,audience:'BUSINESS_PROJECT_UNDERSTANDING',...f.write(entry.path,change(currentBusinessEnvelope(entry.projectId)))});f.save();publicationFailure(f,entry.path);
  }
  const f=publicationFixture(t),entry=f.manifest.atlases[0];Object.assign(entry,{readerVersion:5,audience:'TECHNICAL',...f.write(entry.path,currentBusinessEnvelope(entry.projectId))});f.save();publicationFailure(f,entry.path);
});
