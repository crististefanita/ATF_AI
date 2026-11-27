import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {atlasFailure,parseAtlasJson,requireAtlas} from './atlas-diagnostics.mjs';
import {validateFlowExecutionBindings} from './atlas-execution.mjs';
import {validateAtlasBehavior} from './atlas-behavior.mjs';
import {validateSemanticBaseline,validateSemanticFacets} from './atlas-semantics.mjs';

const digest = bytes => createHash('sha256').update(bytes).digest('hex');
const fail = message => {throw atlasFailure(message);};
const textNormalizationAllowed = relative => !/^lib\//i.test(relative) && /\.(?:java|mjs|cjs|js|md|xml|json|properties|ya?ml|txt|csv|html|css|ps1)$/.test(relative);
const within = (root, file) => {const relative = path.relative(root, file); return !path.isAbsolute(relative) && relative !== '..' && !relative.startsWith('..' + path.sep);};
const captureNeed=(condition,message)=>requireAtlas(condition,'ATLAS-SOURCE',message);
const canonicalWorktreeStatus=value=>value.replaceAll('\r\n','\n').trimEnd();

/** Validate captured declaration only; this function never queries or certifies live Git. */
export function validateAtlasCaptureDeclaration(index){
  captureNeed(index!==null&&typeof index==='object'&&!Array.isArray(index),'Source capture declaration needs an actual source-index object');
  if(index.captureProfile===undefined)return {status:'NOT_ASSESSED',profile:null,state:'UNKNOWN'};
  captureNeed(index.captureProfile==='GIT_WORKTREE_V1','Use the declared GIT_WORKTREE_V1 source-capture profile');
  captureNeed(['CLEAN','DIRTY'].includes(index.captureState)&&typeof index.worktreeStatus==='string',
    'Capture CLEAN or DIRTY together with the canonical Git worktree status');
  captureNeed(index.worktreeStatus===canonicalWorktreeStatus(index.worktreeStatus)
    &&!/[\u0000-\u0008\u000b\u000c\u000e-\u001f\u007f]/.test(index.worktreeStatus)
    &&(index.worktreeStatus===''||index.worktreeStatus.split('\n').every(line=>/^[ MADRCUT?!m]{2} .+$/.test(line)&&line.slice(0,2)!=='  ')),
    'Capture canonical porcelain-v1 status: normalize CRLF to LF and trim only the end; preserve leading status spaces');
  captureNeed(index.captureState===(index.worktreeStatus===''?'CLEAN':'DIRTY'),
    'Source capture state contradicts its declared Git worktree status; do not infer clean source from HEAD alone');
  return {status:'PASSED',profile:'GIT_WORKTREE_V1',state:index.captureState};
}

function ownedFile(root, relative) {
  if (typeof relative !== 'string' || !relative || relative.includes('\\') || relative.includes(':')
      || relative.startsWith('/') || relative.split('/').some(part => !part || part === '.' || part === '..')) fail('Use a normalized relative file path: ' + relative);
  const file = path.join(root, ...relative.split('/'));
  if (!fs.existsSync(file) || !fs.lstatSync(file).isFile() || !within(root, fs.realpathSync(file))) fail('Missing or escaping file: ' + relative);
  return file;
}

function readJson(root, relative) {
  const text=fs.readFileSync(ownedFile(root, relative), 'utf8');
  return parseAtlasJson(text,'ATLAS-SCHEMA',relative);
}

function records(value, name) {
  if (!Array.isArray(value) || value.length === 0) fail('Nonempty ' + name + ' is required');
  return value;
}

export function surefireSummary(xml) {
  const visible = xml.replace(/<!\[CDATA\[[\s\S]*?\]\]>|<!--[\s\S]*?-->/g, '');
  const opening = /<testsuite\b([^>]*)>/.exec(visible);
  if (!opening) fail('Surefire XML needs a testsuite root');
  const attributes = Object.fromEntries([...opening[1].matchAll(/([\w-]+)="([^"]*)"/g)].map(match => [match[1], match[2]]));
  const result = {};
  for (const name of ['tests','failures','errors','skipped']) {
    if (!/^\d+$/.test(attributes[name] || '')) fail('Missing/invalid Surefire counter: ' + name);
    result[name] = Number(attributes[name]);
    if (!Number.isSafeInteger(result[name])) fail('Unsafe Surefire counter');
  }
  if (result.failures + result.errors + result.skipped > result.tests) fail('Contradictory Surefire counters');
  const cases = [...visible.matchAll(/<testcase\b[^>]*?\/>|<testcase\b[^>]*>[\s\S]*?<\/testcase\s*>/g)].map(match => match[0]);
  if (cases.length !== result.tests || ['failure','error','skipped'].some((tag, index) =>
    cases.filter(value => new RegExp('<' + tag + '\\b').test(value)).length !== result[['failures','errors','skipped'][index]])) fail('Surefire counters differ from testcase outcomes');
  result.status = result.failures || result.errors ? 'FAILED' : result.tests && !result.skipped ? 'PASSED' : 'NOT_EXECUTED';
  return result;
}

export function nodeTestSummary(tap) {
  const number = name => {
    const values = [...tap.matchAll(new RegExp('^# ' + name + ' (\\d+)\\s*$', 'gm'))];
    if (values.length !== 1 || !Number.isSafeInteger(Number(values[0][1]))) fail('Missing/ambiguous Node TAP counter: ' + name);
    return Number(values[0][1]);
  };
  const result = {tests:number('tests'),failures:number('fail'),errors:number('cancelled'),skipped:number('skipped') + number('todo')};
  const passed = number('pass');
  const points = [...tap.matchAll(/^(ok|not ok) (\d+)([^\r\n]*)$/gm)];
  const ordinary = points.filter(point => !/#\s*(?:SKIP|TODO)\b/i.test(point[3]));
  if (passed + result.failures + result.errors + result.skipped !== result.tests
      || points.length !== result.tests || new Set(points.map(point => point[2])).size !== points.length
      || ordinary.filter(point => point[1] === 'ok').length !== passed
      || ordinary.filter(point => point[1] === 'not ok').length !== result.failures + result.errors
      || points.length - ordinary.length !== result.skipped) fail('Node TAP counters differ from flat testcase outcomes');
  result.status = result.failures || result.errors ? 'FAILED' : result.tests && !result.skipped ? 'PASSED' : 'NOT_EXECUTED';
  return result;
}

/** Validate declared evidence against bytes and source; semantic completeness remains a human review. */
export function verifyAtlas({sourceRoot, analysisRoot, artifactRoot}) {
  const source = fs.realpathSync(sourceRoot), analysis = fs.realpathSync(analysisRoot), artifacts = fs.realpathSync(artifactRoot);
  if (!within(artifacts, analysis)) fail('Analysis must belong to the selected artifact root');
  const manifest = readJson(analysis, 'analysis.json'), index = readJson(analysis, 'source-index.json');
  if (manifest.schemaVersion !== 1 || index.schemaVersion !== 1) fail('Unsupported Atlas schema');
  if (!/^[a-z0-9][a-z0-9-]*$/.test(manifest.projectId || '') || manifest.projectId !== index.projectId
      || manifest.revision !== index.revision) fail('Atlas target identity differs');
  if (!/^(?:[a-f0-9]{40}|UNVERSIONED)$/.test(manifest.revision || '')) fail('Declare the source revision');
  const declaration=validateAtlasCaptureDeclaration(index),captureProfile=declaration.status==='PASSED';
  let captureStateReview=declaration;
  const git=args=>execFileSync('git',['-C',source,...args],{encoding:'utf8',timeout:60000,windowsHide:true,stdio:['ignore','pipe','pipe']});
  if(captureProfile){
    let inside=false;try{inside=git(['rev-parse','--is-inside-work-tree']).trim()==='true';}catch{}
    captureNeed(inside,'GIT_WORKTREE_V1 needs a real Git worktree at the selected source; keep unversioned or unavailable capture identity explicit');
  }
  if (captureProfile||fs.existsSync(path.join(source, '.git'))) {
    let actual;
    try{actual=git(['rev-parse','HEAD']).trim();}catch(error){
      if(!captureProfile)throw error;
      captureNeed(false,'Cannot read the actual Git HEAD for GIT_WORKTREE_V1; capture source bytes and existing revision together');
    }
    if (actual !== manifest.revision) fail('Live Git revision differs from the Atlas target');
    if(captureProfile){
      let status;try{status=canonicalWorktreeStatus(git(['status','--porcelain=v1','--untracked-files=all']));}catch{
        captureNeed(false,'Cannot read the actual Git worktree status; recapture GIT_WORKTREE_V1 identity without relabeling the source');
      }
      const state=status===''?'CLEAN':'DIRTY';
      captureNeed(index.captureState===state&&index.worktreeStatus===status,
        'Declared Git capture state/status differs from the actual worktree; recapture its exact porcelain-v1 status with the reviewed bytes');
      captureStateReview={status:'PASSED',profile:'GIT_WORKTREE_V1',state,verification:'LIVE_GIT_STATUS_COMPARED',worktreeStatus:status};
    }
  }
  if (manifest.sourceIndexSha256 !== digest(fs.readFileSync(ownedFile(analysis, 'source-index.json')))) fail('Source index integrity differs');
  const sources = new Map();
  for (const entry of records(index.files, 'source files')) {
    if (sources.has(entry.path) || typeof entry.role !== 'string' || !entry.role.trim()) fail('Duplicate or unowned source entry');
    const bytes = fs.readFileSync(ownedFile(source, entry.path));
    if (entry.sha256 !== digest(bytes)) fail('Live source drift: ' + entry.path);
    if (entry.normalizedSha256 && (!textNormalizationAllowed(entry.path)
        || entry.normalizedSha256 !== digest(Buffer.from(bytes.toString('utf8').replaceAll('\r\n','\n'))))) fail('Invalid declared text normalization: ' + entry.path);
    sources.set(entry.path, bytes.toString('utf8').replaceAll('\r\n', '\n'));
  }
  if(index.inventory){
    if(index.inventory.kind!=='GIT_RELEVANT'||!Array.isArray(index.inventory.excludedRoots))fail('Unsupported source inventory declaration');
    for(const relative of index.inventory.excludedRoots)if(typeof relative!=='string'||!relative||relative.includes('\\')||relative.includes(':')||relative.split('/').some(part=>!part||part==='.'||part==='..'))fail('Use a normalized relative inventory exclusion');
    const current=execFileSync('git',['-C',source,'ls-files','--cached','--others','--exclude-standard','-z'],{encoding:'utf8',timeout:60000,windowsHide:true}).split('\0').filter(Boolean)
      .filter(relative=>!index.inventory.excludedRoots.some(excluded=>relative===excluded||relative.startsWith(excluded+'/')))
      .filter(relative=>fs.existsSync(path.join(source,relative))&&fs.statSync(path.join(source,relative)).isFile());
    for(const relative of current)if(!sources.has(relative))fail('Live source inventory added an unindexed file: '+relative);
  }
  const documents = new Map();
  for (const entry of records(manifest.documents, 'documents')) {
    if (documents.has(entry.path)) fail('Duplicate document: ' + entry.path);
    const bytes = fs.readFileSync(ownedFile(analysis, entry.path));
    if (entry.sha256 !== digest(bytes)) fail('Generated document integrity differs: ' + entry.path);
    documents.set(entry.path, bytes.toString('utf8'));
  }
  for (const required of ['project-map.md', 'open-questions.md']) if (!documents.has(required)) fail('Required Atlas document missing: ' + required);
  if(manifest.understanding?.refinement){
    validateSemanticFacets(manifest);
    const name=manifest.understanding.refinement.baselineDocument;
    validateSemanticBaseline(manifest,parseAtlasJson(documents.get(name),'ATLAS-SEMANTICS','original semantic baseline'));
  }
  const reference = ref => {
    if (!ref || !sources.has(ref.path)) fail('Reference is outside the captured source index');
    const lines = sources.get(ref.path).split('\n');
    if (!Number.isInteger(ref.startLine) || !Number.isInteger(ref.endLine) || ref.startLine < 1
        || ref.endLine < ref.startLine || ref.endLine > lines.length) fail('Invalid source line range: ' + ref.path);
    const actual = lines.slice(ref.startLine - 1, ref.endLine).join('\n');
    if (typeof ref.quote !== 'string' || !ref.quote.trim() || actual !== ref.quote) fail('Source quote differs: ' + ref.path);
  };
  const evidence = new Map();
  for (const item of manifest.evidence || []) {
    if (!item.id || evidence.has(item.id)) fail('Evidence IDs must be unique');
    const bytes = fs.readFileSync(ownedFile(artifacts, item.path));
    if (digest(bytes) !== item.sha256) fail('Execution evidence integrity differs: ' + item.id);
    if (!['PASSED','FAILED','NOT_EXECUTED','HISTORICAL'].includes(item.status)
        || typeof item.selection !== 'string' || !item.selection.trim()
        || typeof item.sourceIdentity !== 'string' || !item.sourceIdentity.trim()) fail('Execution evidence needs status, selection and its original source identity');
    let executedSources = null, normalizedSources = null;
    if(!['SUREFIRE_XML','NODE_TAP'].includes(item.format)&&['PASSED','FAILED'].includes(item.status))fail('Execution verdict requires a supported XML/TAP outcome parser: '+item.id);
    if (['SUREFIRE_XML','NODE_TAP'].includes(item.format)) {
      const observed = item.format === 'SUREFIRE_XML' ? surefireSummary(bytes.toString('utf8')) : nodeTestSummary(bytes.toString('utf8'));
      if (observed.status !== item.status || ['tests','failures','errors','skipped'].some(name => observed[name] !== item[name])) fail('Declared execution verdict/counters differ from XML/TAP: ' + item.id);
      const provenance = fs.readFileSync(ownedFile(artifacts, item.sourceManifest));
      if (digest(provenance) !== item.sourceIdentity) fail('Execution source identity differs: ' + item.id);
      const captured = parseAtlasJson(provenance.toString('utf8'),'ATLAS-EVIDENCE',item.sourceManifest);
      executedSources = captured.manifest;
      normalizedSources = captured.normalizedManifest;
      if (!executedSources || typeof executedSources !== 'object') fail('Execution source manifest is required');
    }
    evidence.set(item.id, {...item, executedSources, normalizedSources,text:bytes.toString('utf8')});
  }
  const claims = new Set();
  const behaviorClaims = new Set((Array.isArray(manifest.behaviorFlows) ? manifest.behaviorFlows : [])
    .flatMap(flow => Array.isArray(flow?.claimIds) ? [...flow.claimIds, flow.oracleClaimId] : []));
  const map = documents.get('project-map.md');
  for (const claim of records(manifest.claims, 'claims')) {
    if (!/^[A-Z][A-Z0-9-]+$/.test(claim.id || '') || claims.has(claim.id)) fail('Claim IDs must be unique');
    claims.add(claim.id);
    if (!['SOURCE_REVIEWED','EXECUTED','NOT_EXECUTED','UNKNOWN'].includes(claim.status)) fail('Unsupported claim status');
    if (typeof claim.statement !== 'string' || !claim.statement.trim() || !map.includes(claim.id)
        || !map.includes(claim.statement)) fail('Project map and declared claim differ: ' + claim.id);
    if (['SOURCE_REVIEWED','EXECUTED'].includes(claim.status)) records(claim.sourceRefs, 'source references for ' + claim.id).forEach(reference);
    else (claim.sourceRefs || []).forEach(reference);
    if (!['MAPPED','NOT_MAPPED'].includes(claim.governanceStatus)) fail('Declare governance mapping for ' + claim.id);
    if (claim.governanceStatus === 'MAPPED') records(claim.governanceRefs, 'governance references for ' + claim.id);
    (claim.governanceRefs || []).forEach(reference);
    if (claim.status === 'EXECUTED') {
      const ids = records(claim.evidenceIds, 'execution evidence for ' + claim.id);
      for (const id of ids) {
        const proof = evidence.get(id);
        if (!proof || proof.projectId !== manifest.projectId || !['PASSED','FAILED'].includes(proof.status)
            || !['SUREFIRE_XML','NODE_TAP'].includes(proof.format)) fail('Execution proof belongs to another project/source or is unexecuted: ' + id);
        for (const ref of [...claim.sourceRefs, ...(claim.governanceRefs || [])]) {
          const entry = index.files.find(value => value.path === ref.path);
          const normalizedSources = proof.normalizedSources;
          const normalizedMatch = manifest.sourceNormalization === 'CRLF_LF' && textNormalizationAllowed(ref.path)
            && entry.normalizedSha256 && normalizedSources && normalizedSources[ref.path] === entry.normalizedSha256;
          if (proof.executedSources[ref.path] !== entry.sha256 && !normalizedMatch) fail('Claim source was not the executed source: ' + claim.id + '/' + ref.path);
        }
      }
      const verdict = ids.some(id => evidence.get(id).status === 'FAILED') ? 'FAILED' : 'PASSED';
      // The original suite verdict stays in evidence; bound behavior uses its selected testcase outcomes below.
      if (!behaviorClaims.has(claim.id) && claim.verdict !== verdict) fail('Claim verdict differs from executed evidence: ' + claim.id);
    }
  }
  validateFlowExecutionBindings({analysis:manifest,evidence:[...evidence.values()]});
  validateAtlasBehavior(manifest,{sourceIndex:index});
  return {status:'PASSED', projectId:manifest.projectId, revision:manifest.revision,
    captureStateReview,
    sourceFiles:sources.size, documents:documents.size, claims:claims.size, evidence:evidence.size,
    scope:'DECLARED_SOURCE_AND_ARTIFACT_CONSISTENCY', sourceNormalization:manifest.sourceNormalization || 'EXACT', semanticCompleteness:'HUMAN_REVIEW_REQUIRED'};
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const names = {'--source':'sourceRoot','--analysis':'analysisRoot','--artifacts':'artifactRoot'}, options = {};
    for (let i = 2; i < process.argv.length; i += 2) {
      const name = names[process.argv[i]], value = process.argv[i + 1];
      if (!name || !value || options[name]) fail('Use --source, --analysis and --artifacts once each');
      options[name] = value;
    }
    if (Object.keys(options).length !== 3) fail('Source, analysis and artifact roots are required');
    process.stdout.write(JSON.stringify(verifyAtlas(options)) + '\n');
  } catch (error) {process.stderr.write(error.message + '\n'); process.exitCode = 1;}
}
