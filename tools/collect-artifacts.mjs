import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {parseAtlasJson} from './atlas-diagnostics.mjs';
import {inspectBusinessReaderHtml} from './atlas-reader.mjs';

export const artifactKinds = Object.freeze(['test-results', 'extent', 'allure', 'aggregate', 'browser', 'build', 'execution', 'unclassified']);
const compiledDirectories = new Set(['classes', 'test-classes', 'generated-sources', 'generated-test-sources', 'surefire', 'dependency', '.tmp']);
const posix = value => value.split(path.sep).join('/');
const hash = file => createHash('sha256').update(fs.readFileSync(file)).digest('hex');
const inside = (parent, child) => {const relative = path.relative(parent, child); return relative === '' || (!relative.startsWith('..' + path.sep) && relative !== '..' && !path.isAbsolute(relative));};
const writeJson = (file, value) => fs.writeFileSync(file, JSON.stringify(value, null, 2) + '\n', {flag: 'wx'});

const publicationDocuments = Object.freeze(['atlas/current/README.md', 'atlas/current/index.html', 'reports/current/README.md', 'reports/current/index.html']);
const externalOnlyDirectories = new Set(['runs', 'history', 'archive', 'target', 'build', 'release', 'releases', 'tmp', 'temp', '.tmp', '.codex-artifacts', 'allure-results']);
const portableId = value => typeof value === 'string' && /^[A-Za-z0-9](?:[A-Za-z0-9._-]{0,98}[A-Za-z0-9_-])?$/.test(value)
  && !/^(?:con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\.|$)/i.test(value);

/** Validate the selected live publication without collecting, rewriting or deleting evidence. */
export function validateArtifactPublication({artifactRoot}) {
  const fail = (observed, meaning) => {
    throw new Error('ARTIFACT-PUBLICATION\nObserved: ' + observed + '\nFailure meaning: ' + meaning
      + '\nRepair hint: Keep selected, manifest-bound readers and native proof under current; preserve immutable history outside the checkout.'
      + '\nOwner: artifacts/README.md#ownership\nRecheck: node tools/collect-artifacts.mjs --check-layout <artifact-root>');
  };
  if (typeof artifactRoot !== 'string' || !artifactRoot) fail('<artifact-root>', 'Supply an existing artifact root.');
  const root = path.resolve(artifactRoot);
  const local = relative => path.join(root, ...relative.split('/'));
  const observe = relative => relative || '<artifact-root>';
  const stat = relative => {
    try { return fs.lstatSync(local(relative)); }
    catch (error) { fail(observe(relative), 'Cannot inspect the declared path: ' + error.message); }
  };
  const directory = relative => {
    const value = stat(relative);
    if (value.isSymbolicLink() || !value.isDirectory()) fail(observe(relative), 'Publication directories must be ordinary directories, without links.');
  };
  const regular = relative => {
    const value = stat(relative);
    if (value.isSymbolicLink() || !value.isFile()) fail(observe(relative), 'Publication files must be ordinary files, without links.');
    return value;
  };
  const inspectOwner = (relative, allowed) => {
    directory(relative);
    for (const item of fs.readdirSync(local(relative), {withFileTypes:true})) {
      const candidate = relative ? relative + '/' + item.name : item.name;
      if (!allowed.includes(item.name)) fail(candidate, 'Unowned output, legacy runs, releases and temporary data do not belong in the current publication.');
      if (item.isSymbolicLink()) fail(candidate, 'Publication paths must not contain links.');
    }
  };
  inspectOwner('', ['README.md', 'publication.json', 'atlas', 'reports']);
  inspectOwner('atlas', ['README.md', 'Project_Map_Template.md', 'current']);
  inspectOwner('reports', ['README.md', 'current']);
  for (const relative of ['README.md', 'atlas/README.md', 'atlas/Project_Map_Template.md', 'reports/README.md']) regular(relative);
  const has = relative => fs.readdirSync(path.dirname(local(relative))).includes(path.basename(relative));
  if (!has('publication.json')) {
    if (has('atlas/current') || has('reports/current')) fail('publication.json', 'Current readers or proof require their publication manifest.');
    return {status:'SOURCE_ONLY', atlases:0, reports:0, files:0};
  }
  regular('publication.json');
  let manifest;
  try { manifest = JSON.parse(fs.readFileSync(local('publication.json'), 'utf8')); }
  catch (error) { fail('publication.json', 'The publication manifest must be valid JSON: ' + error.message); }
  const object = value => value !== null && typeof value === 'object' && !Array.isArray(value);
  const revision = value => value === 'UNVERSIONED' || typeof value === 'string' && /^[a-f0-9]{40}$/i.test(value);
  if (!object(manifest) || manifest.schemaVersion !== 1 || manifest.role !== 'CURRENT_PUBLICATION') {
    fail('publication.json', 'Expected schemaVersion 1 and role CURRENT_PUBLICATION.');
  }
  if (!object(manifest.history) || !portableId(manifest.history.id) || !revision(manifest.history.sourceRevision)
      || typeof manifest.history.inventorySha256 !== 'string' || !/^[a-f0-9]{64}$/.test(manifest.history.inventorySha256)) {
    fail('publication.json#history', 'History must declare a portable ID, captured revision and inventory SHA-256.');
  }
  for (const key of ['atlases', 'reports', 'documents']) if (!Array.isArray(manifest[key])) fail('publication.json#' + key, 'Expected an array of declared publication files.');
  const expected = new Map(), parents = new Set(['atlas/current', 'reports/current']);
  const safePath = value => typeof value === 'string' && value.length > 0 && !/[\\<>:"|?*\x00-\x1f]/.test(value)
    && value.split('/').every(part => part && part !== '.' && part !== '..' && !/[. ]$/.test(part)
      && !/^(?:con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\.|$)/i.test(part));
  const declare = (entry, location) => {
    if (!object(entry) || !safePath(entry.path)) fail(location + (object(entry) ? ' path=' + JSON.stringify(entry.path) : ''), 'Every file needs a safe project-relative POSIX path.');
    if (entry.path.split('/').slice(2, -1).some(part => externalOnlyDirectories.has(part.toLowerCase()))) {
      fail(entry.path, 'Historical, build and temporary directories stay outside the current publication, even when manifest-bound.');
    }
    const key = entry.path.normalize('NFC').toLowerCase();
    if (expected.has(key)) fail(entry.path, 'Declared publication paths must be unique, including case collisions.');
    if (!Number.isSafeInteger(entry.bytes) || entry.bytes < 0 || typeof entry.sha256 !== 'string' || !/^[a-f0-9]{64}$/.test(entry.sha256)) {
      fail(entry.path, 'Declare the original nonnegative byte count and SHA-256.');
    }
    expected.set(key, entry);
    let parent = path.posix.dirname(entry.path);
    while (parent !== '.') { parents.add(parent); parent = path.posix.dirname(parent); }
  };
  const identifiers = (entries, field, domain) => {
    const seen = new Set();
    entries.forEach((entry, index) => {
      const location = 'publication.json#' + domain + '/' + index;
      if (!object(entry) || !portableId(entry[field])) fail(location, 'Declare a portable ' + field + '.');
      const key = entry[field].toLowerCase();
      if (seen.has(key)) fail(location, 'Identifiers must be unique, including case collisions.');
      seen.add(key);
    });
  };
  identifiers(manifest.atlases, 'projectId', 'atlases');
  identifiers(manifest.reports, 'id', 'reports');
  manifest.atlases.forEach((entry, index) => {
    if (!revision(entry.sourceRevision)) fail('publication.json#atlases/' + index, 'Declare the captured source revision; current selection does not imply current HEAD.');
    if (entry.path !== 'atlas/current/' + entry.projectId + '/atlas.html') fail('publication.json#atlases/' + index + ' path=' + JSON.stringify(entry.path), 'Each project has exactly one selected atlas/current/<project>/atlas.html reader.');
    declare(entry, 'publication.json#atlases/' + index);
  });
  const e2ePrefix = 'reports/current/e2e/';
  const e2eReaders = new Map(), e2eEntries = [];
  manifest.reports.forEach((entry, index) => {
    const location = 'publication.json#reports/' + index;
    if (!artifactKinds.includes(entry.kind)) fail(location, 'Use an existing artifact category.');
    if (typeof entry.path !== 'string' || !entry.path.startsWith('reports/current/')) fail(location + ' path=' + JSON.stringify(entry.path), 'Selected native proof belongs below reports/current.');
    const e2ePath = entry.path.toLowerCase() === e2ePrefix.slice(0, -1) || entry.path.toLowerCase().startsWith(e2ePrefix);
    const e2eRoute = /^reports\/current\/e2e\/(playwright|selenium)\/(.+)$/.exec(entry.path);
    const e2eReader = e2eRoute && e2eRoute[2] === 'ExtentReport.html' && entry.kind === 'extent';
    const constructionReader=entry.id==='atlas-construction-extent'&&entry.kind==='extent'&&entry.path==='reports/current/atlas-construction/ExtentReport.html';
    if (entry.kind === 'build' || /\.(?:jar|zip|war|class)$/i.test(entry.path)
        || /\.(?:html?|xhtml)$/i.test(entry.path) && !e2eReader && !constructionReader) {
      fail(entry.path, 'Build payloads and generic HTML report sites belong in the external immutable archive; only governed per-engine E2E or source-bound Atlas construction Extent readers may be selected.');
    }
    if (entry.sourceRevision !== undefined && !revision(entry.sourceRevision)) fail(location, 'Source revision must identify the captured source or UNVERSIONED.');
    if (entry.executionScope !== undefined && (typeof entry.executionScope !== 'string' || !entry.executionScope.trim())) fail(location, 'Execution scope must describe the preserved selection.');
    if (e2ePath) {
      if (!e2eRoute) fail(entry.path, 'Use the exact reports/current/e2e/<playwright|selenium>/ publication namespace.');
      if (entry.uiEngine !== e2eRoute[1] || !portableId(entry.runId)
          || typeof entry.sourceRevision !== 'string' || !/^[a-f0-9]{40}$/i.test(entry.sourceRevision)) {
        fail(entry.path, 'Every E2E file needs its exact owning uiEngine, portable runId and captured Git sourceRevision.');
      }
      e2eEntries.push(entry);
      if (e2eReader) {
        if (typeof entry.executionScope !== 'string' || !entry.executionScope.trim()) fail(entry.path, 'The selected E2E reader must state its actual executionScope.');
        if (e2eReaders.has(entry.uiEngine)) fail(entry.path, 'Select only one E2E Extent reader per engine.');
        e2eReaders.set(entry.uiEngine, entry);
      }
    }
    declare(entry, location);
  });
  const e2eOwnedPaths = new Set();
  for (const [engine, reader] of e2eReaders) {
    const prefix = e2ePrefix + engine + '/';
    const references = [['nativeResultsPath', 'test-results'], ['executedSourcePath', 'execution'], ['auditPath', 'execution']];
    const selected = new Set([reader.path]);
    for (const [field, kind] of references) {
      const reference = reader[field];
      if (!safePath(reference) || !reference.startsWith(prefix) || selected.has(reference)) {
        fail(reader.path + '#' + field + ' path=' + JSON.stringify(reference), 'Declare a distinct native proof path under the same engine prefix.');
      }
      const proof = expected.get(reference.normalize('NFC').toLowerCase());
      if (!proof || proof.path !== reference || proof.kind !== kind || proof.uiEngine !== engine
          || proof.runId !== reader.runId || proof.sourceRevision !== reader.sourceRevision) {
        fail(reader.path + '#' + field + ' path=' + JSON.stringify(reference), 'The referenced declared proof must have the required category and match uiEngine, runId and sourceRevision.');
      }
      selected.add(reference);
    }
    for (const reference of selected) e2eOwnedPaths.add(reference);
  }
  for (const entry of e2eEntries) if (!e2eOwnedPaths.has(entry.path)) {
    fail(entry.path, 'Every E2E proof file must belong to its declared per-engine reader and three explicit proof references.');
  }
  manifest.documents.forEach((entry, index) => {
    if (!object(entry) || !publicationDocuments.includes(entry.path)) fail('publication.json#documents/' + index, 'Declare only the four current README and index entry points.');
    declare(entry, 'publication.json#documents/' + index);
  });
  for (const relative of publicationDocuments) if (!expected.has(relative.toLowerCase())) fail(relative, 'Both current domains require an explicit README and index, including an empty selection.');
  const observed = new Set();
  const walk = relative => {
    directory(relative);
    for (const item of fs.readdirSync(local(relative), {withFileTypes:true})) {
      const candidate = relative + '/' + item.name;
      if (item.isSymbolicLink()) fail(candidate, 'Publication paths must not contain links.');
      if (item.isDirectory()) {
        if (!parents.has(candidate)) fail(candidate, 'An undeclared directory is not part of the current selection.');
        walk(candidate);
      } else {
        const key = candidate.normalize('NFC').toLowerCase(), entry = expected.get(key);
        if (!entry || entry.path !== candidate) fail(candidate, 'An orphan or differently cased file is not part of the declared current selection.');
        const value = regular(candidate);
        if (value.size !== entry.bytes || hash(local(candidate)) !== entry.sha256) fail(candidate, 'Current bytes differ from the declared original evidence.');
        observed.add(key);
      }
    }
  };
  walk('atlas/current'); walk('reports/current');
  for (const [key, entry] of expected) if (!observed.has(key)) fail(entry.path, 'A declared current reader, document or proof file is missing.');
  for(const entry of manifest.reports.filter(r=>r.id==='atlas-construction-extent')){
    const provenance=entry.construction,reader=manifest.atlases.find(a=>a.projectId===provenance?.projectId),receipt=manifest.reports.find(r=>r.id==='business-atlas-acceptance');
    if(!provenance||provenance.role!=='DERIVED_ATLAS_CONSTRUCTION_V1'||!reader||provenance.readerSha256!==reader.sha256||entry.sourceRevision!==reader.sourceRevision||!receipt||receipt.sourceRevision!==reader.sourceRevision)fail(entry.path,'Bind derived Atlas construction evidence to its exact selected reader and original acceptance receipt.');
    let captured;try{captured=parseAtlasJson(fs.readFileSync(local(receipt.path),'utf8'),'ATLAS-EVIDENCE','Atlas construction receipt').constructionEvidence;}catch(error){fail(entry.path,'Keep valid unique-member original receipt data: '+error.message);}
    if(!object(captured)||captured.role!==provenance.role||captured.projectId!==reader.projectId||captured.sourceRevision!==reader.sourceRevision||captured.readerSha256!==reader.sha256||captured.reportPath!==entry.path||captured.reportSha256!==entry.sha256||!Array.isArray(captured.proofs)||captured.proofs.length!==3||!captured.proofs.every(p=>object(p))||new Set(captured.proofs.map(p=>p.kind)).size!==3||!['NODE_TAP','NATIVE_RENDER','PUBLISHER_CONTROLS'].every(k=>captured.proofs.some(p=>p.kind===k&&p.status==='PASSED'&&/^[a-f0-9]{64}$/.test(p.sha256))))fail(entry.path,'Retain the exact reader/report digests and three distinct original passing construction proof families. A derived report is not a fresh MCP or application execution.');
    if(!fs.readFileSync(local(entry.path),'utf8').includes('name="atlas-construction-role" content="DERIVED_ATLAS_CONSTRUCTION_V1"'))fail(entry.path,'Mark this report explicitly as derived Atlas construction evidence.');
  }
  for (const entry of manifest.atlases) {
    const html=fs.readFileSync(local(entry.path),'utf8'),current=html.includes('data-atlas-reader="5"');
    if (current || Object.hasOwn(entry,'readerVersion')) {
      if (!current || entry.readerVersion!==5 || entry.audience!=='BUSINESS_PROJECT_UNDERSTANDING') {
        fail(entry.path,'Declare renderer 5 and its business audience together; historical readers are not automatically certified under the current reader contract.');
      }
      if (html.includes('data-atlas-calibration=') || Object.hasOwn(entry,'calibrationProfile')) {
        if (!html.includes('data-atlas-calibration="1"') || entry.calibrationProfile!==1) fail(entry.path,'Declare calibrationProfile 1 with the actual calibrated HTML; historical readers retain their captured profile.');
      }
      try { inspectBusinessReaderHtml(html,{projectId:entry.projectId}); }
      catch (error) { fail(entry.path,error.message+' The original adjacent audit and native render acceptance remain required before selection.'); }
    }
  }
  return {status:'COMPLETE', role:'CURRENT_PUBLICATION', atlases:manifest.atlases.length, reports:manifest.reports.length, files:observed.size};
}

function futureRealPath(input) {
  let current = path.resolve(input); const missing = [];
  while (!fs.existsSync(current)) {const parent = path.dirname(current); if (parent === current) throw new Error('No existing destination ancestor'); missing.unshift(path.basename(current)); current = parent;}
  return path.join(fs.realpathSync(current), ...missing);
}

function classify(relative) {
  const name = relative.toLowerCase();
  if (/\/(?:surefire-reports|failsafe-reports)\//.test('/' + name)) return 'test-results';
  if (name.includes('/atf-core-aggregate-report/')) return 'aggregate';
  if (/\/(?:allure-results|allure-report)\//.test('/' + name)) return 'allure';
  if (/\/(?:screenshots?|videos?|traces?|browser|attachments)\//.test('/' + name) || /\.(?:png|jpe?g|webm|mp4|har)$/.test(name)) return 'browser';
  if (name.includes('/reports/')) return 'extent';
  if (/\.(?:jar|zip)$/.test(name) || /\/maven-(?:archiver|status)\//.test('/' + name)) return 'build';
  if (/\.(?:log|dump|dumpstream)$/.test(name)) return 'execution';
  return 'unclassified';
}

/** Copy completed evidence. Classification never fabricates an execution verdict. */
export function collectArtifacts({sourceRoot, outputRoot, runId, evidence = [], includes = [], revision = null}) {
  if (!sourceRoot || !outputRoot) throw new Error('Source and output roots are required');
  if (!/^[A-Za-z0-9](?:[A-Za-z0-9._-]{0,98}[A-Za-z0-9_-])?$/.test(runId || '')
      || /^(?:con|prn|aux|nul|com[1-9]|lpt[1-9])(?:\.|$)/i.test(runId)) throw new Error('Use a portable nonempty run ID');
  if (revision !== null && !/^[a-f0-9]{7,40}$/i.test(revision)) throw new Error('Source revision must be a Git commit identifier');
  const source = fs.realpathSync(path.resolve(sourceRoot));
  if (!fs.statSync(source).isDirectory()) throw new Error('Source root must be a directory');
  const output = futureRealPath(outputRoot);
  const extra = includes.map(item => {
    if (!artifactKinds.includes(item.kind)) throw new Error('Unknown artifact kind: ' + item.kind);
    return {...item, root: fs.realpathSync(path.resolve(item.root))};
  });
  for (const input of [source, ...extra.map(item => item.root)]) {
    if (inside(input, output)) throw new Error('Output must be outside every collected source root');
  }
  const evidenceFiles = evidence.map(file => fs.realpathSync(path.resolve(file)));
  if (evidenceFiles.some(file => !fs.statSync(file).isFile())) throw new Error('Evidence inputs must be existing files');
  fs.mkdirSync(output, {recursive: true});
  const destination = path.join(output, runId);
  fs.mkdirSync(destination); // EEXIST preserves an earlier run without overwriting it.
  const statusFile = path.join(destination, 'collection-status.json');
  fs.writeFileSync(statusFile, JSON.stringify({status: 'COLLECTING'}) + '\n');
  const files = [], ignored = [], seen = new Set();
  const copy = (input, relative, module, kind) => {
    if (seen.has(relative)) throw new Error('Duplicate destination: ' + relative);
    seen.add(relative);
    const stat = fs.lstatSync(input);
    if (stat.isSymbolicLink() || !stat.isFile()) throw new Error('Report evidence must be a regular file: ' + input);
    const before = hash(input), target = path.join(destination, ...relative.split('/'));
    fs.mkdirSync(path.dirname(target), {recursive: true});
    fs.copyFileSync(input, target, fs.constants.COPYFILE_EXCL);
    if (hash(target) !== before || hash(input) !== before) throw new Error('Source changed during collection: ' + input);
    files.push({path: relative, sourcePath: input, kind, module, bytes: stat.size, sha256: before});
  };
  const walk = (input, relative, module, forcedKind, targetRoot = false, depth = 0) => {
    const stat = fs.lstatSync(input);
    if (stat.isSymbolicLink()) throw new Error('Report evidence must not contain links: ' + input);
    if (!stat.isDirectory()) {copy(input, relative, module, forcedKind || classify(relative)); return;}
    for (const entry of fs.readdirSync(input, {withFileTypes: true}).sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0)) {
      const next = relative + '/' + entry.name;
      if (!forcedKind && targetRoot && depth === 0 && compiledDirectories.has(entry.name)) {ignored.push({path: next, reason: 'compiled-or-temporary-output'}); continue;}
      const reportTree = /\/(?:reports|surefire-reports|failsafe-reports|atf-core-aggregate-report|allure-results|allure-report|screenshots?|videos?|traces?|attachments|browser)\//.test(relative + '/');
      if (!forcedKind && !reportTree && /\.(?:class|java)$/.test(entry.name)) {ignored.push({path: next, reason: 'compiled-or-generated-source'}); continue;}
      walk(path.join(input, entry.name), next, module, forcedKind, targetRoot, depth + 1);
    }
  };
  try {
    const modules = [{root: source, name: 'root', relative: ''}];
    for (const item of fs.readdirSync(source, {withFileTypes: true}).sort((a,b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0)) {
      if (item.isDirectory() && fs.existsSync(path.join(source, item.name, 'pom.xml'))) modules.push({root:path.join(source,item.name), name:item.name, relative:item.name + '/'});
    }
    for (const module of modules) for (const producer of ['target', 'allure-results', '.codex-artifacts']) {
      const directory = path.join(module.root, producer);
      if (fs.existsSync(directory)) walk(directory, 'files/' + module.relative + producer, module.name, null, producer === 'target');
    }
    extra.forEach((item,index) => walk(item.root, 'files/_included/' + index + '/' + path.basename(item.root), 'explicit-input', item.kind));
    evidenceFiles.forEach((file,index) => copy(file, 'evidence/' + index + '/' + path.basename(file), 'execution-evidence', 'execution'));
    files.sort((a,b) => a.path < b.path ? -1 : a.path > b.path ? 1 : 0);
    const catalog = Object.fromEntries(artifactKinds.map(kind => [kind, {status:files.some(file=>file.kind===kind)?'COLLECTED':'NOT_FOUND', files:files.filter(file=>file.kind===kind).map(file=>file.path)}]));
    fs.mkdirSync(path.join(destination, 'catalog'));
    for (const kind of artifactKinds) writeJson(path.join(destination,'catalog',kind+'.json'),catalog[kind]);
    const manifest = {schemaVersion:1, runId, collectionStatus:'COMPLETE', collectedAt:new Date().toISOString(), sourceRoot:source,
      sourceRevision:revision, revisionAuthority:revision?'CALLER_DECLARED':'NOT_SUPPLIED', executionVerdict:'NOT_INFERRED', files, ignored, catalog};
    const escape = value => String(value).replace(/[&<>"']/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    const href = value => value.split('/').map(encodeURIComponent).join('/');
    const sections = artifactKinds.map(kind=>'<details><summary>'+escape(kind)+' — '+catalog[kind].status+' ('+catalog[kind].files.length+' files)</summary><ul>'
      + files.filter(file=>file.kind===kind).map(file=>'<li><a href="'+href(file.path)+'">'+escape(file.path)+'</a></li>').join('')+'</ul></details>').join('');
    const style='body{max-width:1080px;margin:32px auto;padding:0 20px;font:16px/1.5 system-ui,sans-serif;color:#172b4d;background:#f7f9fc}'
      +'h1{overflow-wrap:anywhere}details{margin:14px 0;padding:16px;background:white;border:1px solid #dbe2ee;border-radius:10px}'
      +'summary{cursor:pointer;font-weight:600}li{padding:4px 0;overflow-wrap:anywhere}a{color:#145dbf}nav{display:flex;gap:18px}';
    fs.writeFileSync(path.join(destination,'index.html'),'<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">'
      +'<title>ATF artifact archive</title><style>'+style+'</style></head><body><h1>'+escape(runId)+'</h1><p>Inspect collection status and original execution evidence.</p>'
      +'<nav><a href="manifest.json">Integrity manifest</a><a href="collection-status.json">Collection status</a></nav>'+sections+'</body></html>',{flag:'wx'});
    writeJson(path.join(destination,'manifest.pending.json'),manifest);
    fs.renameSync(path.join(destination,'manifest.pending.json'),path.join(destination,'manifest.json'));
    fs.writeFileSync(statusFile,JSON.stringify({status:'COMPLETE', files:files.length})+'\n');
    return {destination, manifest};
  } catch (error) {
    fs.writeFileSync(statusFile,JSON.stringify({status:'FAILED', message:error.message, filesCopied:files.length})+'\n');
    throw error;
  }
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    if (process.argv[2] === '--check-layout') {
      if (process.argv.length !== 4) throw new Error('Use --check-layout <artifact-root> without collection options');
      process.stdout.write(JSON.stringify(validateArtifactPublication({artifactRoot:process.argv[3]})) + '\n');
    } else {
    const options = {evidence:[], includes:[]}, names = {'--source':'sourceRoot','--output':'outputRoot','--run-id':'runId','--revision':'revision'};
    for (let i=2;i<process.argv.length;i+=2) {
      const flag=process.argv[i], value=process.argv[i+1];
      if (!value) throw new Error('Missing value for '+flag);
      if (flag==='--evidence') options.evidence.push(value);
      else if (flag==='--include') {const split=value.indexOf('='); if(split<1) throw new Error('Use --include kind=path'); options.includes.push({kind:value.slice(0,split),root:value.slice(split+1)});}
      else if (names[flag] && options[names[flag]]===undefined) options[names[flag]]=value;
      else throw new Error('Unknown or repeated option: '+flag);
    }
    const result=collectArtifacts(options);
    process.stdout.write(JSON.stringify({destination:result.destination,files:result.manifest.files.length,executionVerdict:'NOT_INFERRED'})+'\n');
    }
  } catch(error) {process.stderr.write(error.message+'\n'); process.exitCode=1;}
}
