import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {publishAtlas,renderAtlasHtml,validateAtlasHtml,validateAtlasDelivery,atlasSections} from './atlas-report.mjs';
import {atlasFailure,AtlasGovernanceError} from './atlas-diagnostics.mjs';
import {validateAtlasBundle} from './atlas-bundle.mjs';

const repository=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const css=()=>fs.readFileSync(path.join(repository,'atf-atlas/assets/atlas.css'),'utf8').replaceAll('\r\n','\n');

function fixture(t){
  const temporary=fs.realpathSync(os.tmpdir()),directory=fs.mkdtempSync(path.join(temporary,'atf-complete-atlas-'));
  t.after(()=>{assert.equal(path.dirname(path.resolve(directory)),temporary);assert.ok(path.basename(directory).startsWith('atf-complete-atlas-'));fs.rmSync(directory,{recursive:true,force:true});});
  const sourceRoot=path.join(directory,'project with spaces'),artifactRoot=path.join(directory,'artifacts'),analysisRoot=path.join(artifactRoot,'analysis');
  fs.mkdirSync(sourceRoot);fs.mkdirSync(analysisRoot,{recursive:true});
  fs.writeFileSync(path.join(sourceRoot,'request.txt'),'Return the observed response.\n');
  fs.writeFileSync(path.join(sourceRoot,'request.test.txt'),'Assert the exact observed response.\n');
  const index={schemaVersion:1,projectId:'sample-project',revision:'UNVERSIONED',worktreeStatus:'',files:[
    {path:'request.txt',role:'Reviewed contract',sha256:hash('Return the observed response.\n')},
    {path:'request.test.txt',role:'Reviewed oracle',sha256:hash('Assert the exact observed response.\n')}
  ]};
  const source={path:'request.txt',startLine:1,endLine:1,quote:'Return the observed response.'},oracle={path:'request.test.txt',startLine:1,endLine:1,quote:'Assert the exact observed response.'};
  const xml='<testsuite tests="1" failures="0" errors="0" skipped="0"><testcase classname="sample.RequestContractTest" name="response"/></testsuite>';
  fs.writeFileSync(path.join(artifactRoot,'result.xml'),xml);
  fs.writeFileSync(path.join(artifactRoot,'execution-source.json'),JSON.stringify({manifest:Object.fromEntries(index.files.map(item=>[item.path,item.sha256]))}));
  const claims=[{id:'CONTRACT-1',statement:'The caller receives the observed response.',status:'EXECUTED',verdict:'PASSED',sourceRefs:[source],governanceStatus:'MAPPED',governanceRefs:[oracle],evidenceIds:['run'],executionSubjects:[{evidenceId:'run',testClass:'sample.RequestContractTest',testName:'response',sourcePath:'request.test.txt'}]},
    {id:'ORACLE-1',statement:'An existing oracle compares the exact response.',status:'SOURCE_REVIEWED',sourceRefs:[oracle],governanceStatus:'NOT_MAPPED',governanceRefs:[]}];
  const graph={id:'request-flow',kind:'BEHAVIOR',title:'Observed request behavior',nodes:[{id:'request',label:'Receive request',kind:'INTENT',x:450,y:40},{id:'response',label:'Return observation',kind:'OUTCOME',x:450,y:140}],edges:[{id:'observed-response',from:'request',to:'response',label:'observed response',claimId:'CONTRACT-1'}]};
  const analysis={schemaVersion:1,projectId:index.projectId,revision:index.revision,claims,documents:[],evidence:[{id:'run',projectId:index.projectId,path:'result.xml',sha256:hash(xml),format:'SUREFIRE_XML',status:'PASSED',tests:1,failures:0,errors:0,skipped:0,selection:'Existing isolated contract fixture',sourceManifest:'execution-source.json',sourceIdentity:hash(fs.readFileSync(path.join(artifactRoot,'execution-source.json')))}],
    behaviorFlows:[{id:'request-behavior',unitIds:['component'],title:'Return the observed response',actor:'Caller',intent:'Observe the request result',object:'Response',operation:'Execute the existing request contract',outcome:'The same observation reaches the caller',testOracle:'Compare the exact response',oracleStatus:'SOURCE_REVIEWED',oracleClaimId:'ORACLE-1',limits:'Fixture scope; no external execution',claimIds:['CONTRACT-1'],graph}],
    dependencyGraphs:[{id:'dependency-map',kind:'DEPENDENCY',title:'Single component boundary',nodes:[{id:'component',unitId:'component',label:'Request component',kind:'COMPONENT',x:450,y:40}],edges:[],limits:'No declared local dependency in this scoped fixture'}]};
  analysis.understanding={schemaVersion:1,scope:'PROJECT',scopeStatement:'Review the single declared fixture component and disclose unknown operational facets.',inventoryClaimIds:['CONTRACT-1'],units:[{id:'component',label:'Request component',kind:'COMPONENT',scopePaths:['request.txt','request.test.txt'],claimIds:['CONTRACT-1','ORACLE-1'],sharedClaimIds:[],flowIds:['request-behavior'],limits:'The fixture has no live integration or configuration evidence.',facets:Object.fromEntries(['purpose','structure','behavior','data','configuration','failure','verification'].map(name=>[name,{status:['purpose','behavior','verification'].includes(name)?'REVIEWED':'UNKNOWN',claimIds:['purpose','behavior','verification'].includes(name)?['CONTRACT-1']:[],explanation:'Bounded fixture evidence; unknown facets are not inferred.'}]))}]};
  analysis.behaviorFlows[0].caseAnalysis={schemaVersion:1,entryNodeId:'request',pathScope:'ALL_DECLARED_EDGES',negativeScope:{status:'UNKNOWN',claimIds:['CONTRACT-1'],explanation:'The fixture declares only the observed response; no failure behavior is inferred.'},cases:[{id:'observed-response',kind:'POSITIVE',title:'Return exact response',precondition:'Caller supplies one request.',trigger:'Invoke the existing operation.',expected:'The same response is observed.',nodeIds:['request','response'],edgeIds:['observed-response'],claimIds:['CONTRACT-1'],oracleClaimId:'ORACLE-1',oracleStatus:'SOURCE_REVIEWED',oracleRefIndices:[0],assertion:'The independent oracle compares the exact observed response.',limits:'Fixture only; negative behavior remains unknown.'}]};
  const docs={'README.md':'# Project\n\nReviewed fixture scope.\n','project-map.md':'# Map\n\n'+claims.map(item=>item.id+': '+item.statement).join('\n'),'architecture.md':'# Architecture\n\nOne component.\n','testing-and-governance.md':'# Tests\n\nOne existing oracle.\n','open-questions.md':'# Limits\n\nUnknown integrations remain unknown.\n'};
  const save=()=>{for(const [name,text]of Object.entries(docs))fs.writeFileSync(path.join(analysisRoot,name),text);fs.writeFileSync(path.join(analysisRoot,'source-index.json'),JSON.stringify(index));analysis.sourceIndexSha256=hash(fs.readFileSync(path.join(analysisRoot,'source-index.json')));analysis.documents=Object.keys(docs).map(name=>({path:name,sha256:hash(fs.readFileSync(path.join(analysisRoot,name)))}));fs.writeFileSync(path.join(analysisRoot,'analysis.json'),JSON.stringify(analysis));};
  save();return {directory,sourceRoot,artifactRoot,analysisRoot,index,analysis,docs,save,deliveryRoot:path.join(analysisRoot,'delivery')};
}

const read=f=>fs.readFileSync(path.join(f.deliveryRoot,'atlas.html'),'utf8');

test('one target invocation delivers one complete offline English HTML with original evidence',t=>{
  const f=fixture(t),result=publishAtlas(f),html=read(f);
  assert.equal(result.status,'PASSED');assert.equal(result.sections,10);assert.deepEqual(fs.readdirSync(f.deliveryRoot),['atlas.html']);
  for(const id of atlasSections)assert.ok(html.includes('data-atlas-section="'+id+'"'),id);
  assert.ok(html.includes(css()));assert.ok(html.includes('&lt;testsuite tests='));assert.ok(html.includes('Return the observed response.'));
  assert.ok(html.includes('Existing isolated contract fixture'));assert.ok(html.includes('NOT_ASSESSED'));
  assert.ok(html.includes('request.test.txt:1–1'));assert.ok(html.includes('Return exact response'));
  assert.doesNotMatch(html,/<(?:script|link|iframe)\b|(?:src|href)="(?:https?:|file:|\.\.\/)/i);
});

test('a second HTML anywhere in delivery is rejected with a constructive recheck',t=>{
  const f=fixture(t);publishAtlas(f);fs.mkdirSync(path.join(f.deliveryRoot,'fragment'));fs.writeFileSync(path.join(f.deliveryRoot,'fragment','extra.HTML'),'extra');
  assert.throws(()=>validateAtlasDelivery({...f,projectId:'sample-project',css:css()}),error=>error.code==='ATLAS-HTML'&&error.message.includes('exactly one file')&&error.hint.includes('canonical renderer')&&error.recheck.includes('atlas-report.test.mjs'));
});

test('renderer 4 provides adjacent legends, semantic source lists and full case records',t=>{
  const f=fixture(t);f.docs['README.md']='# Reading order\n\n- Read source\n- Inspect the exact oracle\n\n| Owner | Limit |\n|---|---|\n| Request | Fixture only |\n';f.save();publishAtlas(f);
  const html=read(f),bundle=embedded(f);assert.equal(bundle.renderVersion,4);
  assert.equal((html.match(/<figcaption class="atlas-legend">/g)||[]).length,2);
  assert.match(html,/<ul><li>Read source<\/li><li>Inspect the exact oracle<\/li><\/ul>/);
  assert.match(html,/<article class="atlas-case">/);assert.match(html,/<th scope="col">Owner<\/th>/);
  const historical={...bundle,renderVersion:3},old=renderAtlasHtml(historical,css());
  assert.doesNotMatch(old,/<figcaption class="atlas-legend">|<article class="atlas-case">/);
  assert.equal(validateAtlasHtml({html:old,css:css(),projectId:'sample-project'}).status,'PASSED');
});

test('an exact LF/CRLF knowledge capture preserves its original raw digest portably',t=>{
  const f=fixture(t);publishAtlas(f);const bundle=embedded(f);
  const normalized=bundle.knowledgeSource.text.replaceAll('\r\n','\n');
  bundle.knowledgeSource.text=normalized.replaceAll('\n','\r\n');
  bundle.knowledgeSource.sha256=hash(bundle.knowledgeSource.text);bundle.knowledge.sourceSha256=bundle.knowledgeSource.sha256;
  const html=renderAtlasHtml(bundle,css());assert.equal(validateAtlasHtml({html,css:css(),projectId:'sample-project'}).status,'PASSED');
  bundle.knowledgeSource.text+=' ';bundle.knowledgeSource.sha256=hash(bundle.knowledgeSource.text);bundle.knowledge.sourceSha256=bundle.knowledgeSource.sha256;
  assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
});

test('new publication requires explicit cases and preserves historical v2 meaning separately',t=>{
  const f=fixture(t);delete f.analysis.behaviorFlows[0].caseAnalysis;f.save();
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('version 3 requires'));
  assert.equal(fs.existsSync(f.deliveryRoot),false);
});

test('portable bundle independently rejects a corrupted v3 path witness',t=>{
  const f=fixture(t);publishAtlas(f);const bundle=embedded(f);bundle.analysis.behaviorFlows[0].caseAnalysis.cases[0].edgeIds=['invented'];bundle.analysisText=JSON.stringify(bundle.analysis);
  assert.throws(()=>validateAtlasBundle(bundle),error=>error.code==='ATLAS-CASES'&&error.observed.includes('selected transition source'));
});

test('live and portable v3 gates cannot treat a hardlink or identical source copy as an independent oracle',t=>{
  const f=fixture(t),alias='request-copy.txt',bytes=fs.readFileSync(path.join(f.sourceRoot,'request.txt'));
  fs.linkSync(path.join(f.sourceRoot,'request.txt'),path.join(f.sourceRoot,alias));
  f.analysis.claims[0].status='SOURCE_REVIEWED';delete f.analysis.claims[0].evidenceIds;delete f.analysis.claims[0].verdict;f.analysis.evidence=[];
  f.analysis.claims[1].sourceRefs=[{...f.analysis.claims[0].sourceRefs[0],path:alias}];
  f.index.files.push({path:alias,sha256:hash(bytes),role:'An aliased implementation cannot be an independent oracle'});f.analysis.understanding.units[0].scopePaths.push(alias);f.save();
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('independent oracle'));
  assert.equal(fs.existsSync(f.deliveryRoot),false);
});

test('case-variant source aliases cannot bypass independent oracle overlap checks',t=>{
  const f=fixture(t),alias='REQUEST.txt',bytes=fs.readFileSync(path.join(f.sourceRoot,'request.txt'));
  if(!fs.existsSync(path.join(f.sourceRoot,alias)))fs.writeFileSync(path.join(f.sourceRoot,alias),bytes);
  f.analysis.claims[0].status='SOURCE_REVIEWED';delete f.analysis.claims[0].evidenceIds;delete f.analysis.claims[0].verdict;f.analysis.evidence=[];
  f.analysis.claims[1].sourceRefs=[{...f.analysis.claims[0].sourceRefs[0],path:alias}];
  f.index.files.push({path:alias,sha256:hash(bytes),role:'Case or copied-content alias'});f.analysis.understanding.units[0].scopePaths.push(alias);f.save();
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('independent oracle'));
});

test('portable source payload identity rejects an aliased oracle without filesystem access',t=>{
  const f=fixture(t);publishAtlas(f);const bundle=embedded(f),alias='request-copy.txt',original=bundle.referencedSources.find(item=>item.path==='request.txt');
  bundle.analysis.claims[1].sourceRefs=[{...bundle.analysis.claims[0].sourceRefs[0],path:alias}];bundle.analysis.understanding.units[0].scopePaths.push(alias);
  bundle.sourceIndex.files.push({path:alias,sha256:original.sha256,role:'Copied implementation source'});bundle.referencedSources.push({...original,path:alias});
  bundle.sourceIndexText=JSON.stringify(bundle.sourceIndex);bundle.analysis.sourceIndexSha256=hash(bundle.sourceIndexText);bundle.analysisText=JSON.stringify(bundle.analysis);bundle.consistency.sourceFiles++;
  assert.throws(()=>validateAtlasBundle(bundle),error=>error.code==='ATLAS-CASES'&&error.observed.includes('independent oracle'));
});

test('a real self-loop with an exit remains valid through live publication and the portable bundle',t=>{
  const f=fixture(t),flow=f.analysis.behaviorFlows[0];flow.graph.edges.push({id:'retry',from:'request',to:'request',label:'retry',claimId:'CONTRACT-1'});flow.caseAnalysis.pathScope='REPRESENTATIVE';f.save();
  publishAtlas(f);const html=read(f);assert.match(html,/M560,52 C650,40 650,88 560,76/);
  assert.ok(html.includes('unwitnessedEdgeIds'));assert.equal(validateAtlasHtml({html,css:css(),projectId:'sample-project'}).status,'PASSED');
});

test('sidecar resource delivery and another target cannot replace the one-file contract',t=>{
  const f=fixture(t);publishAtlas(f);
  assert.throws(()=>validateAtlasDelivery({...f,projectId:'other-project',css:css()}),error=>error.code==='ATLAS-TARGET');
  fs.writeFileSync(path.join(f.deliveryRoot,'atlas.css'),'sidecar');assert.throws(()=>validateAtlasDelivery({...f,projectId:'sample-project',css:css()}),/exactly one file/);
});

test('missing sections, appended executable resources and edited CSS are rejected independently',t=>{
  const f=fixture(t);publishAtlas(f);const html=read(f),validate=value=>validateAtlasHtml({html:value,css:css(),projectId:'sample-project'});
  assert.throws(()=>validate(html.replace('data-atlas-section="behavior"','data-atlas-section="missing"')),error=>error.code==='ATLAS-HTML');
  assert.throws(()=>validate(html+'<script src="https://example.invalid/run.js"></script>'),error=>error.code==='ATLAS-HTML');
  assert.throws(()=>validate(html.replace('--bg: #0b0f14;','--bg: red;')),error=>error.code==='ATLAS-CSS');
});

test('full long documents and hostile source text remain exact inert input',t=>{
  const f=fixture(t),tail='UNIQUE_FINAL_DOCUMENT_SENTINEL';f.docs['architecture.md']='# Details\n\n'+'Long evidence. '.repeat(15000)+tail+'\n';
  f.docs['open-questions.md']='# Limits\n\n</template><script>alert("not executable")</script> & input\n';f.save();publishAtlas(f);const html=read(f);
  assert.ok(html.includes(tail));assert.ok(html.includes('&lt;/template&gt;&lt;script&gt;alert('));assert.doesNotMatch(html,/<script\b/);
  assert.equal(validateAtlasHtml({html,css:css(),projectId:'sample-project'}).status,'PASSED');
});

test('a project without Git, Maven or tests can disclose one component and a missing oracle',t=>{
  const f=fixture(t);f.analysis.claims.push({id:'ORACLE-MISSING',statement:'No test oracle was found in the declared scope.',status:'UNKNOWN',sourceRefs:[],governanceStatus:'NOT_MAPPED',governanceRefs:[]});
  f.docs['project-map.md']+='\nORACLE-MISSING: No test oracle was found in the declared scope.';
  Object.assign(f.analysis.behaviorFlows[0],{oracleStatus:'NOT_FOUND',oracleClaimId:'ORACLE-MISSING',testOracle:'No tests found in the declared source-only scope; behavior remains source-reviewed.'});
  Object.assign(f.analysis.behaviorFlows[0].caseAnalysis.cases[0],{oracleStatus:'NOT_FOUND',oracleClaimId:'ORACLE-MISSING',oracleRefIndices:[],oracleGap:{searchedClaimIds:['CONTRACT-1'],explanation:'The fixture source contains no test oracle.'}});
  f.analysis.claims[0].status='SOURCE_REVIEWED';delete f.analysis.claims[0].verdict;delete f.analysis.claims[0].evidenceIds;f.analysis.evidence=[];f.save();
  const result=publishAtlas(f);assert.equal(result.status,'PASSED');assert.ok(read(f).includes('NOT_FOUND'));assert.ok(read(f).includes('UNVERSIONED'));
});

test('a module inventory cannot masquerade as behavior and a missing oracle cannot be hidden',t=>{
  const f=fixture(t);f.analysis.behaviorFlows[0].graph.nodes[0].kind='MODULE';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('behavior node kinds'));
  f.analysis.behaviorFlows[0].graph.nodes[0].kind='INTENT';delete f.analysis.behaviorFlows[0].oracleClaimId;f.save();assert.throws(()=>publishAtlas(f),/exact oracle claim/);
});

test('untraced transitions, unknown edge claims and dangling graph endpoints fail',t=>{
  const f=fixture(t),edge=f.analysis.behaviorFlows[0].graph.edges[0];edge.claimId='ABSENT';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('selected transition source'));
  edge.claimId='CONTRACT-1';edge.to='missing';f.save();assert.throws(()=>publishAtlas(f),/endpoint/);
  edge.to='response';f.analysis.behaviorFlows[0].graph.nodes[1].y=40;f.save();assert.throws(()=>publishAtlas(f),/node frames overlap/);
});

test('source drift aborts publication before any delivery exists',t=>{
  const f=fixture(t);fs.appendFileSync(path.join(f.sourceRoot,'request.txt'),'Changed source\n');
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-SOURCE'&&error.observed.includes('request.txt')&&error.owner==='atf-atlas/Report_Contract.md');
  assert.equal(fs.existsSync(f.deliveryRoot),false);
});

test('a failed execution cannot be promoted while rebuilding a complete report',t=>{
  const f=fixture(t);const xml='<testsuite tests="1" failures="1" errors="0" skipped="0"><testcase name="response"><failure/></testcase></testsuite>';
  fs.writeFileSync(path.join(f.artifactRoot,'result.xml'),xml);f.analysis.evidence[0].sha256=hash(xml);f.save();
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-EVIDENCE');assert.equal(fs.existsSync(f.deliveryRoot),false);
});

test('publication preserves existing deliveries and target source bytes',t=>{
  const f=fixture(t),before=hash(fs.readFileSync(path.join(f.sourceRoot,'request.txt')));publishAtlas(f);const output=hash(read(f));
  assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-PATH'&&error.observed.includes('existing output'));
  assert.equal(hash(read(f)),output);assert.equal(hash(fs.readFileSync(path.join(f.sourceRoot,'request.txt'))),before);
});

test('the CLI publishes a generic project under space-containing roots',t=>{
  const f=fixture(t),script=fileURLToPath(new URL('./atlas-report.mjs',import.meta.url));
  const result=spawnSync(process.execPath,[script,'understand','--source',f.sourceRoot,'--analysis',f.analysisRoot,'--artifacts',f.artifactRoot],{encoding:'utf8',windowsHide:true});
  assert.equal(result.status,0,result.stderr);assert.equal(JSON.parse(result.stdout).projectId,'sample-project');assert.deepEqual(fs.readdirSync(f.deliveryRoot),['atlas.html']);
});

test('binary historical evidence remains byte-recoverable in the complete artifact',t=>{
  const f=fixture(t),bytes=Buffer.from([0,255,17,128,42]);fs.writeFileSync(path.join(f.artifactRoot,'binary.dat'),bytes);
  f.analysis.evidence.push({id:'binary-history',projectId:'sample-project',path:'binary.dat',sha256:hash(bytes),format:'BINARY',status:'HISTORICAL',selection:'Retained unexecuted fixture attachment',sourceIdentity:'Historical byte identity only'});f.save();publishAtlas(f);
  assert.ok(read(f).includes(bytes.toString('base64')));assert.ok(read(f).includes('BASE64'));
});

test('missing complete input and cross-project execution cannot be hidden by presentation',t=>{
  const f=fixture(t);delete f.docs['architecture.md'];f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-DOCUMENT');
  f.docs['architecture.md']='# Architecture\n\nOne component.';f.analysis.evidence[0].projectId='another-project';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-EVIDENCE');
});

test('unknown diagnostic payload mentions cannot impersonate an observed rule',()=>{
  const error=atlasFailure('Unexpected payload: Live source drift: guessed');
  assert.equal(error.code,'ATLAS-UNKNOWN');assert.equal(error.observed,'Unexpected payload: Live source drift: guessed');
  assert.ok(error.message.includes('Observed:'));assert.ok(error.message.includes('Repair hint:'));assert.ok(error.message.includes('Recheck:'));
  assert.ok(error instanceof AtlasGovernanceError);
});

test('source-reviewed oracle status cannot be promoted to executed in the visible report',t=>{
  const f=fixture(t);f.analysis.behaviorFlows[0].oracleStatus='EXECUTED';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-BEHAVIOR');
});

test('a self-rebuilt presentation cannot omit a declared input document',t=>{
  const f=fixture(t);publishAtlas(f);const content=/<template id="atlas-bundle">([\s\S]*?)<\/template>/.exec(read(f))[1];
  const bundle=JSON.parse(content.replaceAll('&lt;','<').replaceAll('&gt;','>').replaceAll('&quot;','"').replaceAll('&#39;',"'").replaceAll('&amp;','&'));
  bundle.documents=bundle.documents.filter(item=>item.path!=='architecture.md');assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-DOCUMENT');
});

test('target documentation is embedded with its full indexed source identity',t=>{
  const f=fixture(t),text='# Project use\n\nExisting request example.\n';fs.writeFileSync(path.join(f.sourceRoot,'README.md'),text);
  f.index.files.push({path:'README.md',role:'Reviewed owner guide',sha256:hash(text)});f.analysis.sourceDocuments=['README.md'];f.save();publishAtlas(f);
  assert.ok(read(f).includes('Existing request example.'));assert.ok(read(f).includes('Target source: README.md'));
});

test('optional unknown references and evidence remain honest empty scopes',t=>{
  const f=fixture(t);f.analysis.claims[0].status='SOURCE_REVIEWED';delete f.analysis.claims[0].evidenceIds;delete f.analysis.claims[0].verdict;delete f.analysis.evidence;
  f.analysis.claims.push({id:'UNKNOWN-1',statement:'An integration has not been inspected.',status:'UNKNOWN',governanceStatus:'NOT_MAPPED'});f.docs['project-map.md']+='\nUNKNOWN-1: An integration has not been inspected.';f.save();
  assert.equal(publishAtlas(f).status,'PASSED');assert.ok(read(f).includes('UNKNOWN-1'));
});

test('dependency edges follow direct vertical ownership without passing through an unrelated neighbor',t=>{
  const f=fixture(t);f.analysis.dependencyGraphs=[{id:'module-graph',kind:'DEPENDENCY',title:'Declared fixture dependencies',nodes:[
    {id:'caller',unitId:'component',label:'Caller module',kind:'MODULE',x:450,y:40},
    {id:'neighbor',unitId:'component',label:'Independent neighbor',kind:'MODULE',x:170,y:210},
    {id:'adapter',unitId:'component',label:'Selected adapter',kind:'MODULE',x:450,y:210},
    {id:'core',unitId:'component',label:'Required core',kind:'MODULE',x:450,y:380}],edges:[
    {from:'caller',to:'adapter',label:'uses adapter',claimId:'CONTRACT-1'},
    {from:'adapter',to:'core',label:'uses core',claimId:'CONTRACT-1'},
    {from:'caller',to:'core',label:'direct core access',claimId:'CONTRACT-1'}]}];f.save();publishAtlas(f);
  const graph=/<h2>Declared fixture dependencies<\/h2>([\s\S]*?)<\/svg>/.exec(read(f))[1];
  const paths=[...graph.matchAll(/<path class="edge[^\"]*" d="([^\"]+)"/g)].map(match=>match[1]);
  assert.match(paths[0],/^M450,88 C450,/);assert.match(paths[1],/^M450,258 C450,/);
  assert.match(paths[2],/^M340,64 H(?:35|47) V404 H340$/);
});

function embedded(f){const content=/<template id="atlas-bundle">([\s\S]*?)<\/template>/.exec(read(f))[1];return JSON.parse(content.replaceAll('&lt;','<').replaceAll('&gt;','>').replaceAll('&quot;','"').replaceAll('&#39;',"'").replaceAll('&amp;','&'));}

test('a behavior edge cannot borrow an unrelated reviewed architecture claim',t=>{
  const f=fixture(t);f.analysis.claims.push({...f.analysis.claims[1],id:'STRUCTURE-1',statement:'A separate declaration exists.'});f.docs['project-map.md']+='\nSTRUCTURE-1: A separate declaration exists.';
  f.analysis.behaviorFlows[0].graph.edges[0].claimId='STRUCTURE-1';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('selected transition source'));
});

test('declared behavior and dependency graph kinds cannot be swapped',t=>{
  const f=fixture(t);f.analysis.behaviorFlows[0].graph.kind='DEPENDENCY';f.save();assert.throws(()=>publishAtlas(f),error=>error.code==='ATLAS-CASES'&&error.observed.includes('actual behavior graph'));
});

test('a true corpus digest cannot authorize counterfeit optional retrieval and citations',t=>{
  const f=fixture(t);publishAtlas(f);const bundle=embedded(f);bundle.knowledge.records.push({id:'KB-COUNTERFEIT',mandatory:false,requirement:'Unsupported optional policy.'});bundle.knowledge.selectedIds.push('KB-COUNTERFEIT');
  assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
});

test('retrieval source bytes and rank cannot be fabricated under a real corpus identity',t=>{
  const f=fixture(t);publishAtlas(f);let bundle=embedded(f);bundle.knowledge.sourceSha256='f'.repeat(64);assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
  bundle=embedded(f);bundle.knowledge.ranking=[];assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
  bundle=embedded(f);bundle.knowledgeSource.text+=' ';bundle.knowledgeSource.sha256=createHash('sha256').update(bundle.knowledgeSource.text).digest('hex');bundle.knowledge.sourceSha256=bundle.knowledgeSource.sha256;
  assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
});

test('retrieval metadata and undeclared attestations cannot extend the canonical receipt',t=>{
  const f=fixture(t);publishAtlas(f);let bundle=embedded(f);bundle.knowledge.corpusIdentity='UNSUPPORTED_CERTIFICATION';
  assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
  bundle=embedded(f);bundle.knowledge.automaticSemanticCertification=true;
  assert.throws(()=>renderAtlasHtml(bundle,css()),error=>error.code==='ATLAS-KNOWLEDGE');
});
