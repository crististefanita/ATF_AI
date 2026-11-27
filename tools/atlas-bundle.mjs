import {createHash} from 'node:crypto';
import {isDeepStrictEqual} from 'node:util';
import {requireAtlas,parseAtlasJson} from './atlas-diagnostics.mjs';
import {surefireSummary,nodeTestSummary} from './verify-atlas.mjs';
import {validateFlowExecutionBindings} from './atlas-execution.mjs';
import {validateAtlasUnderstanding} from './atlas-understanding.mjs';
import {validateAtlasBehavior} from './atlas-behavior.mjs';
import {validateBusinessReader} from './atlas-reader.mjs';

const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const nonempty=value=>typeof value==='string'&&value.trim().length>0;
const sha=value=>typeof value==='string'&&/^[a-f0-9]{64}$/.test(value);
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const relative=value=>nonempty(value)&&!value.includes('\\')&&!value.includes(':')&&!value.startsWith('/')&&value.split('/').every(part=>part&&part!=='.'&&part!=='..');
const textAllowed=path=>!/^lib\//i.test(path)&&/\.(?:java|mjs|cjs|js|md|xml|json|properties|ya?ml|txt|csv|html|css|ps1)$/.test(path);
const requiredDocuments=['README.md','project-map.md','architecture.md','testing-and-governance.md','open-questions.md'];
const list=(value,code,name)=>{requireAtlas(Array.isArray(value),code,'Expected '+name+' array');return value;};

function parseJson(text,code,name){
  return parseAtlasJson(text,code,name);
}

function payloads(entries,code,name){
  const found=new Map();
  for(const entry of list(entries,code,name)){
    requireAtlas(object(entry)&&relative(entry.path)&&!found.has(entry.path)&&sha(entry.sha256)&&typeof entry.text==='string',code,'Invalid or duplicate '+name+' entry');
    let bytes;
    if(entry.encoding==='UTF8'){
      bytes=Buffer.from(entry.text,'utf8');requireAtlas(bytes.toString('utf8')===entry.text,code,'Invalid UTF-8 text representation: '+entry.path);
    }else{
      requireAtlas(entry.encoding==='BASE64',code,'Unsupported byte representation: '+entry.path);
      bytes=Buffer.from(entry.text,'base64');requireAtlas(bytes.toString('base64')===entry.text,code,'Noncanonical base64 representation: '+entry.path);
    }
    requireAtlas(hash(bytes)===entry.sha256,code,'Embedded bytes differ from their SHA-256: '+entry.path);
    found.set(entry.path,{...entry,bytes});
  }
  return found;
}

function inventory(entries,code,name){
  const found=new Map();
  for(const entry of list(entries,code,name)){
    requireAtlas(object(entry)&&relative(entry.path)&&sha(entry.sha256)&&!found.has(entry.path),code,'Invalid or duplicate '+name+' identity');
    found.set(entry.path,entry);
  }
  return found;
}

function exactInventory(actual,expected,code,name){
  requireAtlas(actual.size===expected.size&&[...expected.keys()].every(path=>actual.has(path)),code,'Embedded '+name+' inventory is incomplete or includes undeclared files');
  for(const [path,entry]of expected)requireAtlas(actual.get(path).sha256===entry.sha256,code,'Embedded '+name+' identity differs from its declaration: '+path);
}

/** Verifies portable internal consistency; live source and semantic review remain separate gates. */
export function validateAtlasBundle(bundle){
  requireAtlas(object(bundle)&&bundle.schemaVersion===1&&[1,2,3,4,5].includes(bundle.renderVersion),'ATLAS-SCHEMA','Unsupported complete bundle schema or renderer version');
  const analysis=parseJson(bundle.analysisText,'ATLAS-SCHEMA','analysis'),index=parseJson(bundle.sourceIndexText,'ATLAS-SOURCE','source index');
  requireAtlas(isDeepStrictEqual(analysis,bundle.analysis)&&isDeepStrictEqual(index,bundle.sourceIndex),'ATLAS-SCHEMA','Original JSON and embedded parsed objects disagree');
  requireAtlas(object(analysis)&&object(index)&&analysis.schemaVersion===1&&index.schemaVersion===1
    &&/^[a-z0-9][a-z0-9-]*$/.test(analysis.projectId||'')&&analysis.projectId===index.projectId
    &&analysis.revision===index.revision&&/^(?:[a-f0-9]{40}|UNVERSIONED)$/.test(analysis.revision||''),'ATLAS-TARGET','Embedded target identity or source revision differs');
  requireAtlas(analysis.sourceIndexSha256===hash(Buffer.from(bundle.sourceIndexText,'utf8')),'ATLAS-SOURCE','Original source index digest differs');
  requireAtlas(!analysis.sourceNormalization||['EXACT','CRLF_LF'].includes(analysis.sourceNormalization),'ATLAS-SOURCE','Unsupported source normalization policy');
  const indexed=inventory(index.files,'ATLAS-SOURCE','source index');
  requireAtlas(indexed.size>0&&[...indexed.values()].every(entry=>nonempty(entry.role)&&(!entry.normalizedSha256||(sha(entry.normalizedSha256)&&textAllowed(entry.path)))),'ATLAS-SOURCE','Source index needs owned identities and valid text normalization');
  const declaredDocuments=inventory(analysis.documents,'ATLAS-DOCUMENT','declared documents'),documents=payloads(bundle.documents,'ATLAS-DOCUMENT','documents');
  exactInventory(documents,declaredDocuments,'ATLAS-DOCUMENT','documents');
  requireAtlas(requiredDocuments.every(path=>documents.has(path)),'ATLAS-DOCUMENT','Complete bundle is missing a required analysis document');
  const claims=list(analysis.claims,'ATLAS-SCHEMA','claims'),claimIds=new Set(),neededSources=new Set();
  requireAtlas(claims.length>0,'ATLAS-SCHEMA','The bundle needs declared claims');
  const sourceDocuments=list(analysis.sourceDocuments||[],'ATLAS-SOURCE','requested source documents');
  requireAtlas(new Set(sourceDocuments).size===sourceDocuments.length&&sourceDocuments.every(relative),'ATLAS-SOURCE','Requested source document paths must be unique and relative');
  sourceDocuments.forEach(path=>neededSources.add(path));
  const sources=payloads(bundle.referencedSources,'ATLAS-SOURCE','referenced sources');
  const reference=ref=>{
    requireAtlas(object(ref)&&sources.has(ref.path),'ATLAS-REFERENCE','A source or governance reference is missing its full source payload');
    const bytes=sources.get(ref.path).bytes,text=bytes.toString('utf8');
    requireAtlas(Buffer.from(text,'utf8').equals(bytes),'ATLAS-REFERENCE','Quoted source is not UTF-8: '+ref.path);
    const lines=text.replaceAll('\r\n','\n').split('\n');
    requireAtlas(Number.isInteger(ref.startLine)&&Number.isInteger(ref.endLine)&&ref.startLine>=1&&ref.endLine>=ref.startLine&&ref.endLine<=lines.length
      &&nonempty(ref.quote)&&lines.slice(ref.startLine-1,ref.endLine).join('\n')===ref.quote,'ATLAS-REFERENCE','Embedded quote or complete line range differs: '+ref.path);
    neededSources.add(ref.path);
  };
  const map=documents.get('project-map.md').bytes.toString('utf8');
  for(const claim of claims){
    requireAtlas(object(claim)&&/^[A-Z][A-Z0-9-]+$/.test(claim.id||'')&&!claimIds.has(claim.id)
      &&['SOURCE_REVIEWED','EXECUTED','NOT_EXECUTED','UNKNOWN'].includes(claim.status)&&nonempty(claim.statement)
      &&map.includes(claim.id)&&map.includes(claim.statement),'ATLAS-SCHEMA','Claim identity, status or documented statement differs');
    claimIds.add(claim.id);
    const refs=list(claim.sourceRefs||[],'ATLAS-REFERENCE','source references');
    requireAtlas(!['SOURCE_REVIEWED','EXECUTED'].includes(claim.status)||refs.length>0,'ATLAS-REFERENCE','A reviewed claim needs source references: '+claim.id);
    refs.forEach(reference);
    const guards=list(claim.governanceRefs||[],'ATLAS-REFERENCE','governance references');
    requireAtlas(['MAPPED','NOT_MAPPED'].includes(claim.governanceStatus)&&(claim.governanceStatus!=='MAPPED'||guards.length>0),'ATLAS-REFERENCE','A mapped claim needs governance references: '+claim.id);
    guards.forEach(reference);
  }
  requireAtlas(sources.size===neededSources.size&&[...neededSources].every(path=>sources.has(path)),'ATLAS-SOURCE','Referenced source payload inventory is incomplete or undeclared');
  for(const [path,entry]of sources){
    const identity=indexed.get(path);requireAtlas(identity&&identity.sha256===entry.sha256,'ATLAS-SOURCE','Embedded source does not match the original index: '+path);
    if(identity.normalizedSha256)requireAtlas(identity.normalizedSha256===hash(Buffer.from(entry.bytes.toString('utf8').replaceAll('\r\n','\n'))),'ATLAS-SOURCE','Embedded source normalization differs: '+path);
  }
  const sourceCopies=payloads(bundle.sourceDocuments||[],'ATLAS-SOURCE','source documents');
  exactInventory(sourceCopies,new Map(sourceDocuments.map(path=>[path,indexed.get(path)||{}])),'ATLAS-SOURCE','source documents');
  const evidence=list(analysis.evidence||[],'ATLAS-EVIDENCE','evidence'),files=payloads(bundle.evidenceFiles,'ATLAS-EVIDENCE','evidence files'),expectedFiles=new Map(),proofs=new Map();
  const expectFile=(path,sha256)=>{requireAtlas(relative(path)&&sha(sha256)&&(!expectedFiles.has(path)||expectedFiles.get(path).sha256===sha256),'ATLAS-EVIDENCE','Declared evidence paths or identities conflict');expectedFiles.set(path,{sha256});};
  for(const item of evidence){
    requireAtlas(object(item)&&nonempty(item.id)&&!proofs.has(item.id)&&nonempty(item.selection)&&nonempty(item.sourceIdentity)
      &&['PASSED','FAILED','NOT_EXECUTED','HISTORICAL'].includes(item.status),'ATLAS-EVIDENCE','Execution evidence lacks a unique identity, scope or original verdict');
    expectFile(item.path,item.sha256);
    if(item.sourceManifest)expectFile(item.sourceManifest,item.sourceIdentity);
    const supported=['SUREFIRE_XML','NODE_TAP'].includes(item.format);
    requireAtlas(supported||!['PASSED','FAILED'].includes(item.status),'ATLAS-EVIDENCE','Unsupported formats cannot establish executed verdicts: '+item.id);
    if(supported){
      requireAtlas(files.has(item.path)&&files.has(item.sourceManifest),'ATLAS-EVIDENCE','Supported execution proof or its original source manifest is missing: '+item.id);
      const observed=item.format==='SUREFIRE_XML'?surefireSummary(files.get(item.path).bytes.toString('utf8')):nodeTestSummary(files.get(item.path).bytes.toString('utf8'));
      requireAtlas(observed.status===item.status&&['tests','failures','errors','skipped'].every(key=>observed[key]===item[key]),'ATLAS-EVIDENCE','Embedded execution verdict or counters differ: '+item.id);
      const provenance=parseJson(files.get(item.sourceManifest).bytes.toString('utf8'),'ATLAS-EVIDENCE','executed-source manifest');
      requireAtlas(object(provenance.manifest),'ATLAS-EVIDENCE','Executed-source identities are missing: '+item.id);
      proofs.set(item.id,{...item,executed:provenance.manifest,normalized:provenance.normalizedManifest,text:files.get(item.path).bytes.toString('utf8')});
    }else proofs.set(item.id,item);
  }
  exactInventory(files,expectedFiles,'ATLAS-EVIDENCE','evidence files');
  const behaviorClaims=new Set((Array.isArray(analysis.behaviorFlows)?analysis.behaviorFlows:[])
    .flatMap(flow=>Array.isArray(flow?.claimIds)?[...flow.claimIds,flow.oracleClaimId]:[]));
  for(const claim of claims.filter(item=>item.status==='EXECUTED')){
    const ids=list(claim.evidenceIds,'ATLAS-EVIDENCE','executed claim evidence');
    requireAtlas(ids.length>0&&new Set(ids).size===ids.length,'ATLAS-EVIDENCE','Executed claims need unique evidence references: '+claim.id);
    for(const id of ids){
      const proof=proofs.get(id);requireAtlas(proof&&proof.projectId===analysis.projectId&&proof.executed&&['PASSED','FAILED'].includes(proof.status),'ATLAS-EVIDENCE','Execution evidence belongs to another target or is unexecuted: '+id);
      for(const ref of [...(claim.sourceRefs||[]),...(claim.governanceRefs||[])]){
        const entry=indexed.get(ref.path),normalized=analysis.sourceNormalization==='CRLF_LF'&&textAllowed(ref.path)&&entry.normalizedSha256&&proof.normalized?.[ref.path]===entry.normalizedSha256;
        requireAtlas(proof.executed[ref.path]===entry.sha256||normalized,'ATLAS-EVIDENCE','Claim source was not the executed source: '+claim.id+'/'+ref.path);
      }
    }
    // Keep the complete suite result in evidence; exact selected testcase outcomes own bound behavior verdicts.
    requireAtlas(behaviorClaims.has(claim.id)||claim.verdict===(ids.some(id=>proofs.get(id).status==='FAILED')?'FAILED':'PASSED'),'ATLAS-EVIDENCE','Claim verdict differs from the retained execution: '+claim.id);
  }
  validateFlowExecutionBindings({analysis,evidence:[...proofs.values()]});
  validateAtlasUnderstanding(analysis,index,{required:bundle.renderVersion>=2});
  validateAtlasBehavior(analysis,{required:bundle.renderVersion>=3,sourceIndex:index});
  const result=bundle.consistency;
  requireAtlas(object(result)&&result.status==='PASSED'&&result.projectId===analysis.projectId&&result.revision===analysis.revision
    &&result.sourceFiles===indexed.size&&result.documents===documents.size&&result.claims===claims.length&&result.evidence===evidence.length
    &&result.semanticCompleteness==='HUMAN_REVIEW_REQUIRED','ATLAS-SCHEMA','Recorded source consistency result disagrees with the embedded package');
  const knowledge=bundle.knowledge;
  requireAtlas(object(knowledge)&&sha(knowledge.corpusSha256)&&knowledge.retrievalScope==='DETERMINISTIC_LEXICAL_POLICY_CONTEXT'
    &&knowledge.semanticRecall==='NOT_CLAIMED'&&Array.isArray(knowledge.records)&&knowledge.records.length>0&&Array.isArray(knowledge.selectedIds)&&Array.isArray(knowledge.mandatoryIds)&&knowledge.mandatoryIds.length>0
    &&new Set(knowledge.selectedIds).size===knowledge.selectedIds.length&&knowledge.records.length===knowledge.selectedIds.length
    &&knowledge.records.every(record=>object(record)&&nonempty(record.id)&&knowledge.selectedIds.includes(record.id))
    &&new Set(knowledge.records.map(record=>record.id)).size===knowledge.records.length
    &&knowledge.mandatoryIds.every(id=>knowledge.selectedIds.includes(id)),'ATLAS-KNOWLEDGE','Retrieved mandatory policy or its declared scope is incomplete');
  if(bundle.renderVersion>=5)validateBusinessReader(analysis);
  return {status:'PASSED',projectId:analysis.projectId,documents:documents.size,sources:sources.size,evidenceFiles:files.size,scope:'PORTABLE_BUNDLE_INTERNAL_CONSISTENCY'};
}
