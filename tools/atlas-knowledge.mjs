import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {AtlasGovernanceError, requireAtlas} from './atlas-diagnostics.mjs';

const defaultRoot=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const corpusPath='atf-atlas/knowledge/atlas-contracts.json';
const archiveRevision='70a7279c2a9560d39e6bd2554cd2f96a88626692';
const hash=bytes=>createHash('sha256').update(bytes).digest('hex');
const need=(condition,observed)=>requireAtlas(condition,'ATLAS-KNOWLEDGE',observed);
const nonempty=value=>typeof value==='string'&&value.trim().length>0;
const canonical=value=>JSON.stringify(value,(_,item)=>item&&typeof item==='object'&&!Array.isArray(item)?Object.fromEntries(Object.keys(item).sort().map(key=>[key,item[key]])):item);
const payload=knowledge=>Object.fromEntries(['schemaVersion','id','language','archiveRevision','retrievalPolicy','records'].map(key=>[key,knowledge[key]]));
const normalized=bytes=>bytes.toString('utf8').replaceAll('\r\n','\n');
const tokens=value=>[...new Set((value.toLowerCase().match(/[\p{L}\p{N}]+/gu)||[]))];

export const ATLAS_MANDATORY_KNOWLEDGE_IDS=Object.freeze([
  'KB-BEHAVIOR','KB-BREADTH','KB-CASE-WITNESS','KB-COLLAPSE','KB-COMPLETE','KB-ENGLISH','KB-EVIDENCE','KB-EXECUTION-SUBJECT',
  'KB-ONE-TARGET','KB-REACHABILITY','KB-READONLY','KB-SCORES','KB-SEMANTICS','KB-STYLE','KB-TARGET-ADAPTATION','KB-TRACE'
]);

function relativeFile(relative){
  need(nonempty(relative)&&!relative.includes('\\')&&!relative.includes(':')&&!relative.startsWith('/')
    &&!relative.split('/').some(part=>!part||part==='.'||part==='..'),'Knowledge citation needs a normalized project-relative file path: '+relative);
}

function ownedRead(root,relative){
  relativeFile(relative);
  try{
    const selected=path.join(root,relative),actual=fs.realpathSync(selected),within=path.relative(root,actual);
    need(!path.isAbsolute(within)&&within!=='..'&&!within.startsWith('..'+path.sep)&&fs.lstatSync(selected).isFile(),'Knowledge source is missing or escaping: '+relative);
    return fs.readFileSync(selected);
  }catch(error){if(error instanceof AtlasGovernanceError)throw error;throw new AtlasGovernanceError('ATLAS-KNOWLEDGE','Cannot read knowledge source '+relative+': '+error.message);}
}

export function validateAtlasKnowledge(knowledge,{root=defaultRoot}={}){
  need(knowledge&&typeof knowledge==='object'&&!Array.isArray(knowledge),'Knowledge corpus must be an object');
  need(knowledge.schemaVersion===1&&knowledge.id==='atf-atlas-contracts'&&knowledge.language==='en','Unsupported knowledge schema, identity or language');
  need(knowledge.archiveRevision===archiveRevision,'Knowledge must retain the reviewed archive revision');
  need(knowledge.retrievalPolicy==='MANDATORY_PLUS_DETERMINISTIC_LEXICAL','Knowledge retrieval must retain mandatory requirements');
  need(Array.isArray(knowledge.records)&&knowledge.records.length>0,'Knowledge records are required');
  let sourceRoot;
  try{sourceRoot=fs.realpathSync(root);}catch(error){throw new AtlasGovernanceError('ATLAS-KNOWLEDGE','Knowledge project root is unavailable: '+error.message);}
  const ids=new Set();
  for(const record of knowledge.records){
    need(record&&typeof record.id==='string'&&/^KB-[A-Z0-9]+(?:-[A-Z0-9]+)*$/.test(record.id||'')&&!ids.has(record.id),'Knowledge IDs must be valid unique strings');ids.add(record.id);
    need(nonempty(record.title)&&nonempty(record.requirement)&&typeof record.mandatory==='boolean','Knowledge record needs title, requirement and mandatory status: '+record.id);
    need(Array.isArray(record.topics)&&record.topics.length>0&&record.topics.every(nonempty)&&new Set(record.topics).size===record.topics.length,'Knowledge topics must be nonempty and unique: '+record.id);
    need(Array.isArray(record.sources)&&record.sources.length>0,'Knowledge citations are required: '+record.id);
    for(const citation of record.sources){
      need(citation&&['ARCHIVE','CURRENT_CONTRACT'].includes(citation.kind),'Unsupported knowledge citation kind: '+record.id);
      relativeFile(citation.path);need(nonempty(citation.quote)&&citation.quote.trim().length>=20,'Knowledge citation needs a useful exact excerpt: '+record.id);
      if(citation.kind==='ARCHIVE'){
        need(citation.revision===archiveRevision&&typeof citation.sha256==='string'&&/^[a-f0-9]{64}$/.test(citation.sha256),'Archived citation needs its pinned revision and a string raw blob digest: '+record.id);
      }else{
        need(citation.path==='atf-atlas/Report_Contract.md','Current decisions belong to the canonical report contract: '+record.id);
        const source=normalized(ownedRead(sourceRoot,citation.path));
        need(source.includes(citation.quote.replaceAll('\r\n','\n')),'Current contract excerpt no longer matches its owner: '+record.id);
      }
    }
  }
  for(const id of ATLAS_MANDATORY_KNOWLEDGE_IDS)need(knowledge.records.some(record=>record.id===id&&record.mandatory),'Mandatory knowledge requirement cannot be removed or demoted: '+id);
  const computed=hash(canonical(payload(knowledge)));
  if(Object.hasOwn(knowledge,'corpusSha256'))need(knowledge.corpusSha256===computed,'Knowledge payload changed after its corpus identity was captured');
  if(Object.hasOwn(knowledge,'sourceSha256'))need(knowledge.sourceSha256===hash(ownedRead(sourceRoot,corpusPath)),'Knowledge raw source identity differs from the selected corpus file');
  if(Object.hasOwn(knowledge,'corpusIdentity'))need(knowledge.corpusIdentity==='CANONICAL_JSON_WITH_SORTED_OBJECT_KEYS','Knowledge canonical identity policy differs');
  return {status:'PASSED',records:ids.size,mandatoryIds:knowledge.records.filter(record=>record.mandatory).map(record=>record.id).sort(),corpusSha256:computed};
}

export function loadAtlasKnowledge({root=defaultRoot}={}){
  let selected;
  try{selected=fs.realpathSync(root);}catch(error){throw new AtlasGovernanceError('ATLAS-KNOWLEDGE','Knowledge project root is unavailable: '+error.message);}
  const bytes=ownedRead(selected,corpusPath);let knowledge;
  try{knowledge=JSON.parse(bytes.toString('utf8'));}catch(error){throw new AtlasGovernanceError('ATLAS-KNOWLEDGE','Knowledge JSON cannot be parsed: '+error.message);}
  const validated=validateAtlasKnowledge(knowledge,{root:selected});
  return {...knowledge,corpusSha256:validated.corpusSha256,sourceSha256:hash(bytes),corpusIdentity:'CANONICAL_JSON_WITH_SORTED_OBJECT_KEYS'};
}

export function retrieveAtlasKnowledge({query,root=defaultRoot,limit=4}={}){
  need(nonempty(query)&&query.length<=4096,'Provide a nonempty knowledge query up to 4096 characters');
  need(Number.isInteger(limit)&&limit>=0&&limit<=100,'Optional retrieval limit must be an integer from 0 to 100');
  const knowledge=loadAtlasKnowledge({root}),queryTerms=tokens(query);
  const ranking=knowledge.records.map(record=>{
    const topicTerms=new Set(tokens(record.topics.join(' '))),titleTerms=new Set(tokens(record.title)),bodyTerms=new Set(tokens(record.requirement));
    const matchedTerms=queryTerms.filter(term=>topicTerms.has(term)||titleTerms.has(term)||bodyTerms.has(term));
    const score=queryTerms.reduce((sum,term)=>sum+(topicTerms.has(term)?5:0)+(titleTerms.has(term)?3:0)+(bodyTerms.has(term)?1:0),0);
    return {id:record.id,score,mandatory:record.mandatory,matchedTerms};
  }).sort((left,right)=>right.score-left.score||(left.id<right.id?-1:left.id>right.id?1:0));
  const optional=ranking.filter(record=>!record.mandatory&&record.score>0).slice(0,limit),selected=new Set([...ranking.filter(record=>record.mandatory),...optional].map(record=>record.id));
  const selectedIds=ranking.filter(record=>selected.has(record.id)).map(record=>record.id),recordById=new Map(knowledge.records.map(record=>[record.id,record]));
  const records=selectedIds.map(id=>recordById.get(id));
  return {query,corpusSha256:knowledge.corpusSha256,sourceSha256:knowledge.sourceSha256,corpusIdentity:knowledge.corpusIdentity,archiveRevision:knowledge.archiveRevision,
    retrievalScope:'DETERMINISTIC_LEXICAL_POLICY_CONTEXT',semanticRecall:'NOT_CLAIMED',mandatoryIds:ranking.filter(record=>record.mandatory).map(record=>record.id).sort(),selectedIds,
    ranking:ranking.map(record=>({...record,selected:selected.has(record.id)})),records,citations:records.flatMap(record=>record.sources.map(source=>({recordId:record.id,...source})))};
}

export function verifyAtlasKnowledgeArchive({root=defaultRoot,knowledge=loadAtlasKnowledge({root})}={}){
  const validated=validateAtlasKnowledge(knowledge,{root}),checked=new Map();
  need(fs.existsSync(path.join(root,'.git')),'Archive verification needs the selected repository Git metadata. Restore the local archive reference explicitly; this check never fetches.');
  for(const record of knowledge.records)for(const citation of record.sources){
    if(citation.kind!=='ARCHIVE')continue;
    const identity=citation.revision+':'+citation.path;let bytes=checked.get(identity);
    if(!bytes){
      try{bytes=execFileSync('git',['-C',root,'show',identity],{windowsHide:true,maxBuffer:5*1024*1024,stdio:['ignore','pipe','pipe']});}
      catch(error){throw new AtlasGovernanceError('ATLAS-KNOWLEDGE','Retained archive source is unavailable: '+identity+'. Restore the local archive reference explicitly; this check never fetches. '+(error.stderr?.toString().trim()||error.message));}
      checked.set(identity,bytes);
    }
    need(hash(bytes)===citation.sha256,'Archived knowledge blob identity differs: '+record.id+' / '+citation.path);
    need(bytes.toString('utf8').includes(citation.quote),'Archived knowledge excerpt is unsupported: '+record.id+' / '+citation.path);
  }
  return {status:'PASSED',archiveRevision,corpusSha256:validated.corpusSha256,archivedBlobs:checked.size,records:knowledge.records.length,scope:'PINNED_ARCHIVE_BLOB_AND_EXCERPT_IDENTITY; retrieval is policy context, not project execution evidence'};
}
