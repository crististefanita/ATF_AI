import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {ATLAS_MANDATORY_KNOWLEDGE_IDS,loadAtlasKnowledge,retrieveAtlasKnowledge,validateAtlasKnowledge,verifyAtlasKnowledgeArchive} from './atlas-knowledge.mjs';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const raw=()=>JSON.parse(fs.readFileSync(path.join(root,'atf-atlas/knowledge/atlas-contracts.json'),'utf8'));
function fixture(t){
  const parent=fs.realpathSync(os.tmpdir()),directory=fs.mkdtempSync(path.join(parent,'atf-atlas-knowledge-'));
  t.after(()=>{const actual=fs.realpathSync(directory);assert.equal(path.dirname(actual),parent);assert.ok(path.basename(actual).startsWith('atf-atlas-knowledge-'));fs.rmSync(actual,{recursive:true,force:true});});
  for(const relative of ['atf-atlas/knowledge/atlas-contracts.json','atf-atlas/Report_Contract.md']){const destination=path.join(directory,relative);fs.mkdirSync(path.dirname(destination),{recursive:true});fs.copyFileSync(path.join(root,relative),destination);}
  return directory;
}
const expectKnowledgeFailure=fn=>assert.throws(fn,error=>{assert.equal(error.code,'ATLAS-KNOWLEDGE');assert.ok(error.observed);assert.ok(error.meaning);assert.ok(error.hint);assert.equal(error.owner,'atf-atlas/Report_Contract.md');assert.match(error.recheck,/node --test/);return true;});

test('the maintained knowledge corpus has complete mandatory policy and exact current citations',()=>{
  const corpus=loadAtlasKnowledge();assert.equal(validateAtlasKnowledge(corpus).status,'PASSED');
  assert.match(corpus.corpusSha256,/^[a-f0-9]{64}$/);assert.match(corpus.sourceSha256,/^[a-f0-9]{64}$/);
  assert.equal(corpus.corpusIdentity,'CANONICAL_JSON_WITH_SORTED_OBJECT_KEYS');
  assert.ok(ATLAS_MANDATORY_KNOWLEDGE_IDS.every(id=>corpus.records.some(record=>record.id===id&&record.mandatory)));
});

test('archived provenance verifies actual pinned Git bytes and useful excerpts without network access',()=>{
  const result=verifyAtlasKnowledgeArchive();assert.equal(result.status,'PASSED');assert.equal(result.archivedBlobs,16);
  assert.match(result.scope,/PINNED_ARCHIVE_BLOB_AND_EXCERPT_IDENTITY/);
});

test('one selected project always retrieves the single-HTML requirement even for an unrelated question',()=>{
  const result=retrieveAtlasKnowledge({query:'unrelated astronomy constellation',limit:0});
  assert.ok(result.selectedIds.includes('KB-ONE-TARGET'));assert.ok(result.mandatoryIds.includes('KB-ONE-TARGET'));
  const record=result.records.find(item=>item.id==='KB-ONE-TARGET');
  assert.match(record.requirement,/exactly one complete understanding HTML/);assert.match(record.requirement,/one project\/revision identity/);
  assert.deepEqual([...result.selectedIds].sort(),[...result.mandatoryIds].sort());
});

test('breadth, behavior cases and execution subjects remain mandatory while assessment stays scoped',()=>{
  const receipt=retrieveAtlasKnowledge({query:'unrelated astronomy constellation',limit:0});
  for(const id of ['KB-BREADTH','KB-EXECUTION-SUBJECT','KB-CASE-WITNESS','KB-REACHABILITY']){
    assert.ok(receipt.selectedIds.includes(id));assert.ok(receipt.mandatoryIds.includes(id));
    const changed=raw();changed.records=changed.records.filter(record=>record.id!==id);
    expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));
  }
  const assessment=raw().records.find(record=>record.id==='KB-ASSESSMENT');assert.equal(assessment.mandatory,false);
  assert.match(assessment.requirement,/NOT_ASSESSED/);assert.match(assessment.requirement,/separate project quality from Atlas quality/);
});

test('generic projects without tests, Git or a multi-module build retain target-specific unknowns policy',t=>{
  const selected=fixture(t),result=retrieveAtlasKnowledge({root:selected,query:'standalone Python script without tests or Git',limit:0});
  assert.equal(fs.existsSync(path.join(selected,'.git')),false);
  const record=result.records.find(item=>item.id==='KB-TARGET-ADAPTATION');assert.ok(record.mandatory);
  assert.match(record.requirement,/actual project language/);assert.match(record.requirement,/no tests or Git/);
  assert.match(record.requirement,/single component and zero declared edges/);assert.match(record.requirement,/oracleStatus NOT_FOUND/);
  assert.match(record.requirement,/UNKNOWN oracle claim/);assert.equal(result.semanticRecall,'NOT_CLAIMED');
});

test('retrieval is deterministic, ranked and limits optional records without evicting mandatory ones',()=>{
  const first=retrieveAtlasKnowledge({query:'graph arrowheads relationship labels',limit:1}),second=retrieveAtlasKnowledge({query:'graph arrowheads relationship labels',limit:1});
  assert.deepEqual(first,second);assert.ok(first.selectedIds.includes('KB-GRAPH-READABILITY'));
  assert.equal(first.ranking.filter(item=>item.selected&&!item.mandatory).length,1);
  assert.ok(first.mandatoryIds.every(id=>first.selectedIds.includes(id)));
  assert.ok(first.citations.every(citation=>first.selectedIds.includes(citation.recordId)));
  assert.equal(first.retrievalScope,'DETERMINISTIC_LEXICAL_POLICY_CONTEXT');
});

test('query ties are resolved by stable ID rather than record file order',t=>{
  const selected=fixture(t),file=path.join(selected,'atf-atlas/knowledge/atlas-contracts.json'),corpus=raw();
  for(const record of corpus.records.filter(item=>!item.mandatory)){record.title='Shared optional detail';record.topics=['equalmatch'];record.requirement='A reviewed optional requirement.';}
  fs.writeFileSync(file,JSON.stringify(corpus));const first=retrieveAtlasKnowledge({root:selected,query:'equalmatch',limit:1});
  corpus.records.reverse();fs.writeFileSync(file,JSON.stringify(corpus));const second=retrieveAtlasKnowledge({root:selected,query:'equalmatch',limit:1});
  assert.deepEqual(first.ranking,second.ranking);assert.deepEqual(first.selectedIds,second.selectedIds);
  const firstOptional=corpus.records.filter(item=>!item.mandatory).map(item=>item.id).sort()[0];
  assert.equal(first.ranking.find(item=>!item.mandatory&&item.selected).id,firstOptional);
});

test('canonical corpus identity survives formatting and CRLF while raw file identity remains explicit',t=>{
  const selected=fixture(t),file=path.join(selected,'atf-atlas/knowledge/atlas-contracts.json'),first=loadAtlasKnowledge({root:selected});
  fs.writeFileSync(file,JSON.stringify(raw(),null,4).replaceAll('\n','\r\n')+'\r\n');
  const second=loadAtlasKnowledge({root:selected});assert.equal(first.corpusSha256,second.corpusSha256);assert.notEqual(first.sourceSha256,second.sourceSha256);
  const contract=path.join(selected,'atf-atlas/Report_Contract.md');fs.writeFileSync(contract,fs.readFileSync(contract,'utf8').replaceAll('\r\n','\n').replaceAll('\n','\r\n'));
  assert.equal(loadAtlasKnowledge({root:selected}).corpusSha256,first.corpusSha256);
});

test('mandatory rules cannot disappear, be demoted or hide behind duplicate IDs',()=>{
  for(const mutate of [corpus=>{corpus.records=corpus.records.filter(item=>item.id!=='KB-ONE-TARGET');},corpus=>{corpus.records.find(item=>item.id==='KB-TARGET-ADAPTATION').mandatory=false;},corpus=>{corpus.records.push(structuredClone(corpus.records[0]));}]){
    const corpus=raw();mutate(corpus);expectKnowledgeFailure(()=>validateAtlasKnowledge(corpus));
  }
});

test('citation escapes and keyword-only current excerpts cannot authorize requirements',()=>{
  const escaped=raw();escaped.records[0].sources[0].path='../foreign.css';expectKnowledgeFailure(()=>validateAtlasKnowledge(escaped));
  const altered=raw();altered.records.find(item=>item.id==='KB-ENGLISH').sources[0].quote='The unrelated owner says authored output is acceptable.';expectKnowledgeFailure(()=>validateAtlasKnowledge(altered));
  const terse=raw();terse.records[0].sources[0].quote='stylesheet';expectKnowledgeFailure(()=>validateAtlasKnowledge(terse));
});

test('an edited archive digest or unsupported excerpt fails against the real retained object',()=>{
  const digest=raw();digest.records[0].sources[0].sha256='0'.repeat(64);expectKnowledgeFailure(()=>verifyAtlasKnowledgeArchive({knowledge:digest}));
  const quote=raw();quote.records[0].sources[0].quote='A sentence that never belonged to the archived stylesheet.';expectKnowledgeFailure(()=>verifyAtlasKnowledgeArchive({knowledge:quote}));
});

test('missing local archive metadata is a clear failure rather than a skip or parent Git fallback',t=>{
  const selected=fixture(t);expectKnowledgeFailure(()=>verifyAtlasKnowledgeArchive({root:selected}));
});

test('changed loaded payload, malformed JSON and invalid retrieval requests retain constructive failures',t=>{
  const changed=loadAtlasKnowledge();changed.records[0].requirement+=' An unauthorized alteration.';expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));
  const sourceIdentity=loadAtlasKnowledge();sourceIdentity.sourceSha256='0'.repeat(64);expectKnowledgeFailure(()=>validateAtlasKnowledge(sourceIdentity));
  const identityPolicy=loadAtlasKnowledge();identityPolicy.corpusIdentity='UNDECLARED_POLICY';expectKnowledgeFailure(()=>validateAtlasKnowledge(identityPolicy));
  const selected=fixture(t);fs.writeFileSync(path.join(selected,'atf-atlas/knowledge/atlas-contracts.json'),'{invalid');expectKnowledgeFailure(()=>loadAtlasKnowledge({root:selected}));
  for(const options of [{query:''},{query:'valid',limit:-1},{query:'valid',limit:1.5},{query:'x'.repeat(4097)}])expectKnowledgeFailure(()=>retrieveAtlasKnowledge(options));
});

test('coercible JSON identifiers cannot enter retrieval or conceal duplicate identities',t=>{
  for(const duplicate of [false,true]){
    const selected=fixture(t),corpus=raw(),record=corpus.records.find(item=>!item.mandatory);
    const original=record.id;record.id=[original];if(duplicate)corpus.records.push(structuredClone(record));
    const file=path.join(selected,'atf-atlas/knowledge/atlas-contracts.json');fs.writeFileSync(file,JSON.stringify(corpus));
    expectKnowledgeFailure(()=>loadAtlasKnowledge({root:selected}));
    expectKnowledgeFailure(()=>retrieveAtlasKnowledge({root:selected,query:'graph arrowheads labels'}));
    record.id=original;if(duplicate)corpus.records.pop();fs.writeFileSync(file,JSON.stringify(corpus));
    assert.equal(validateAtlasKnowledge(loadAtlasKnowledge({root:selected}),{root:selected}).status,'PASSED');
    assert.ok(retrieveAtlasKnowledge({root:selected,query:'graph arrowheads labels'}).selectedIds.every(id=>typeof id==='string'));
  }
});

test('archive digests must be strings before retrieval, with an actual repaired corpus accepted',t=>{
  const selected=fixture(t),corpus=raw(),citation=corpus.records.flatMap(record=>record.sources).find(source=>source.kind==='ARCHIVE');
  const original=citation.sha256;citation.sha256=[original];
  const file=path.join(selected,'atf-atlas/knowledge/atlas-contracts.json');fs.writeFileSync(file,JSON.stringify(corpus));
  expectKnowledgeFailure(()=>loadAtlasKnowledge({root:selected}));
  expectKnowledgeFailure(()=>retrieveAtlasKnowledge({root:selected,query:'complete reader'}));
  citation.sha256=original;fs.writeFileSync(file,JSON.stringify(corpus));
  assert.equal(validateAtlasKnowledge(loadAtlasKnowledge({root:selected}),{root:selected}).status,'PASSED');
  assert.ok(retrieveAtlasKnowledge({root:selected,query:'complete reader'}).mandatoryIds.every(id=>typeof id==='string'));
});

test('duplicate corpus members are refused before retrieval, including escaped and nested identities',t=>{
  const selected=fixture(t),file=path.join(selected,'atf-atlas/knowledge/atlas-contracts.json'),original=JSON.stringify(raw());
  for(const changed of ['{"schemaVersion":0,'+original.slice(1),original.replace('"mandatory":true','"mandatory":false,"mandatory":true'),original.replace('"id":','"\\u0069d":null,"id":')]){
    assert.notEqual(changed,original);fs.writeFileSync(file,changed);
    expectKnowledgeFailure(()=>loadAtlasKnowledge({root:selected}));
    expectKnowledgeFailure(()=>retrieveAtlasKnowledge({root:selected,query:'complete reader'}));
    assert.throws(()=>loadAtlasKnowledge({root:selected}),error=>/Duplicate JSON member/.test(error.observed));
  }
  fs.writeFileSync(file,original);
  assert.equal(loadAtlasKnowledge({root:selected}).corpusSha256,loadAtlasKnowledge().corpusSha256);
  assert.ok(retrieveAtlasKnowledge({root:selected,query:'complete reader'}).selectedIds.includes('KB-ONE-TARGET'));
});

test('business audience, adjacent audit and numbering remain mandatory for every retrieval question',()=>{
  const receipt=retrieveAtlasKnowledge({query:'unrelated question',limit:0}),rule=receipt.records.find(r=>r.id==='KB-BUSINESS-READER');
  assert.ok(rule?.mandatory);assert.match(rule.requirement,/hierarchical section numbering/);assert.match(rule.requirement,/hidden templates/);assert.match(rule.requirement,/adjacent audit/);
  const altered=raw();altered.records.find(r=>r.id===rule.id).mandatory=false;expectKnowledgeFailure(()=>validateAtlasKnowledge(altered));
  const complete=receipt.records.find(r=>r.id==='KB-COMPLETE');assert.match(complete.requirement,/does not embed raw technical inputs/);
});

// Active authoring guides must not reintroduce the retired reader audience contract.
test('active Atlas guides agree with the retrieved business reader generation',()=>{
  for(const relative of ['atf-atlas/README.md','artifacts/atlas/README.md','artifacts/atlas/Project_Map_Template.md']){
    const text=fs.readFileSync(path.join(root,relative),'utf8');
    assert.match(text,/renderVersion 5/,'Repair '+relative+' to explain the current business reader and adjacent audit.');
    assert.doesNotMatch(text,/renderVersion [1-4]\b/,'Move old publication instructions to explicitly scoped historical contract material: '+relative);
  }
});

test('every retrieval keeps native artifact calibration and actual source comparison policy',()=>{
 const receipt=retrieveAtlasKnowledge({query:'unrelated question',limit:0}),rule=receipt.records.find(r=>r.id==='KB-BUSINESS-READER');
 for(const term of ['live source','expected cases','partial review depth','native navigation','architecture/behavior diagrams','masking','exact repeated business sentences','one initially closed disclosure','Unknown failure scope'])assert.ok(rule.requirement.includes(term),'Repair the mandatory artifact-calibration policy: '+term);
 for(const id of ['ATLAS-CALIBRATION-001:','ATLAS-CLARITY-001:','ATLAS-SPACING-001:','ATLAS-PRIMARY-BAND-001:','ATLAS-READER-NOISE-001:','ATLAS-DIAGRAM-WIDTH-001:'])assert.ok(rule.sources.some(s=>s.quote.startsWith(id)),'Restore the exact current audience/layout citation: '+id);
 for(const term of ['full-width band','artificial canvas caps','No generic footer','dedicated sections','symmetric solid dark gutters','Clean provenance','all six sections','flow-owned concrete declarations','separate derived Extent construction report'])assert.ok(rule.requirement.includes(term),'Retain the independent main-map and quiet-surface policy: '+term);
 const graph=receipt.records.find(r=>r.id==='KB-GRAPH-READABILITY');assert.match(graph.requirement,/six CSS pixels/);assert.match(graph.requirement,/complete source identities/);
});

test('semantic qualification and compact identity policy cannot be evicted by query ranking',()=>{
 const r=retrieveAtlasKnowledge({query:'unrelated topic',limit:0}),rule=r.records.find(x=>x.id==='KB-SEMANTIC-FACETS');assert.equal(rule.mandatory,true);for(const value of ['SEMANTIC_FACETS_V1','independent existing oracles','original testcase identities','partial scopes','compact Atlas/project identity'])assert.ok(rule.requirement.includes(value));const changed=raw();changed.records.find(x=>x.id===rule.id).mandatory=false;expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));
});

test('refinement and archived graph quality remain mandatory for unrelated retrieval',()=>{
 const receipt=retrieveAtlasKnowledge({query:'unrelated topic',limit:0});for(const id of ['KB-FACET-REFINEMENT','KB-GRAPH-READABILITY']){assert.ok(receipt.mandatoryIds.includes(id));assert.ok(receipt.selectedIds.includes(id));const changed=raw();changed.records.find(r=>r.id===id).mandatory=false;expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));}
 const graph=receipt.records.find(r=>r.id==='KB-GRAPH-READABILITY');assert.ok(graph.sources.some(s=>s.path.endsWith('graph-style-primitives.html')));assert.ok(graph.sources.some(s=>s.path.endsWith('AtlasGraphRuleCatalog.java')));assert.match(graph.requirement,/SOURCE_GRAPH_V1/);assert.match(receipt.records.find(r=>r.id==='KB-FACET-REFINEMENT').requirement,/recycled old receipts/);
});

test('observation policy retains preview and image limits plus root-guide ownership for any query',()=>{const r=retrieveAtlasKnowledge({query:'unrelated topic',limit:0}),rule=r.records.find(x=>x.id==='KB-OBSERVATION-BOUNDARIES');assert.ok(r.mandatoryIds.includes(rule.id));for(const term of ['BOUNDED_OBSERVATIONS_V1','source-only observations remain partial','complete raw body','pixel meaning','global uniqueness'])assert.ok(rule.requirement.includes(term));const changed=raw();changed.records.find(x=>x.id===rule.id).mandatory=false;expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));const language=r.records.find(x=>x.id==='KB-ENGLISH');assert.ok(language.sources.some(s=>s.quote.startsWith('DOC-ROOT-FOCUS-001:')));});

test('successive application review retrieves retained proof and remaining questions even without lexical overlap',()=>{
  const receipt=retrieveAtlasKnowledge({query:'unrelated topic',limit:0}),rule=receipt.records.find(r=>r.id==='KB-FACET-REFINEMENT');assert.ok(receipt.mandatoryIds.includes(rule.id));
  for(const term of ['BOUNDED_OBSERVATIONS_V2','actual prior audit','additional oracle fragments','remaining questions','collapsed by default','not new product-flow tasks'])assert.ok(rule.requirement.includes(term));
  assert.ok(rule.sources.some(s=>s.kind==='CURRENT_CONTRACT'&&s.quote.startsWith('ATLAS-SUCCESSIVE-001:')));
  assert.match(rule.requirement,/iterative tokenization/);assert.match(rule.requirement,/Never truncate history/);assert.ok(rule.sources.some(s=>s.quote.startsWith('ATLAS-AUDIT-SIZE-001:')));
  const changed=raw();changed.records.find(r=>r.id===rule.id).mandatory=false;expectKnowledgeFailure(()=>validateAtlasKnowledge(changed));
});
