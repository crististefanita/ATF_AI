const owner='atf-atlas/Report_Contract.md';
const recheck='node --test tools/atlas-reading.test.mjs tools/atlas-fit.test.mjs tools/atlas-semantics.test.mjs tools/atlas-calibration.test.mjs tools/atlas-reader.test.mjs tools/atlas-reader-graphs.test.mjs tools/atlas-reference.test.mjs tools/verify-atlas.test.mjs tools/atlas-report.test.mjs tools/atlas-presentation.test.mjs tools/atlas-knowledge.test.mjs tools/atlas-bundle.test.mjs tools/atlas-execution.test.mjs tools/atlas-understanding.test.mjs tools/atlas-behavior.test.mjs';
const rules={
  'ATLAS-SEMANTICS':['A semantic review promotion lacks a concrete scope, owned implementation or independent oracle.','Read the actual operation and existing check, select their separate source fragments, explain the facet-specific observation and remaining limits, and retain PARTIAL where the supported review is incomplete. Never upgrade a facet from test counts or repeated prose.'],
  'ATLAS-CALIBRATION':['The actual rendered artifact differs from its source-bound business understanding or native acceptance.','Compare live source and project documentation, preserve real screenshots and original verdicts, repair the owned projection or layout, and repeat the failing native interaction with an actual repaired artifact. Passing metadata does not prove semantic truth.'],
  'ATLAS-READER':['The project-understanding view mixes audiences or omits a reviewed behavior boundary.','Repair the source-bound business projection, retain meaningful negative cases and unknowns, and move commands, file checks and raw proof into the adjacent audit. Use at least three meaningful nodes and two distinct real relationships for each current rendered diagram; explain compact ideas in prose instead of padding a graph. Keep reader answers short and linked to their actual flows and independent oracles. Prefer comparative tables, exact shared boundaries and optional detail while retaining original case fields. Keep symmetric dark outer gutters while each diagram fills the inner frame. Present concise confidence answers in a table and clean provenance in its own closed detail; retain dirty-source and failed-check warnings. Recheck section numbering and all six sections through native navigation.'],
  'ATLAS-PATH':['An input or output escapes its declared owner.','Use project-relative evidence paths, regular files and an owned artifact output directory.'],
  'ATLAS-TARGET':['The analysis targets a different project or revision.','Capture the intended tree again and retain its actual revision and dirty state.'],
  'ATLAS-SOURCE':['Captured source no longer matches its reviewed identity.','Compare the declared capture with actual Git status, preserving leading status spaces and honest CLEAN/DIRTY state. Inspect changed source and regenerate the analysis; never repair this by replacing only a hash.'],
  'ATLAS-DOCUMENT':['Generated documentation differs from its declared content.','Regenerate the document from its reviewed inputs and verify its new manifest.'],
  'ATLAS-REFERENCE':['A quoted source or governance fragment is unsupported.','Read the real owner and record the exact complete line range and quote.'],
  'ATLAS-EVIDENCE':['An execution claim differs from retained outcomes or executed source.','Preserve XML/TAP, original verdicts and source provenance; separate historical and unexecuted claims.'],
  'ATLAS-KNOWLEDGE':['The retrieved contract has invalid identity, provenance or requirements.','Repair the reviewed knowledge record and its archived citation; rerun the provenance and retrieval controls.'],
  'ATLAS-HTML':['The complete HTML differs from its governed renderer.','Regenerate atlas.html with the canonical renderer; keep every section, local anchor and embedded input.'],
  'ATLAS-CSS':['The report visual contract differs from its canonical stylesheet.','Embed the exact atf-atlas/assets/atlas.css bytes and remove external styles or local overrides.'],
  'ATLAS-PRESENTATION':['Reader structure or measured table readability needs repair.','Preserve all facts, render semantic blocks and adjacent graph legends, allocate space to prose, and recheck desktop geometry. Current business readers use source-bound subviews, wrapped tables or case records without horizontal scrolling; never hide text or shrink it to fit.'],
  'ATLAS-BEHAVIOR':['Behavior, oracle or graph trace is missing or inconsistent.','Describe an existing observed contract and its test oracle; bind graph edges to reviewed claims and disclose source-only scope.'],
  'ATLAS-CASES':['A declared behavior case has invalid reachability, path or oracle binding.','Inspect the actual entry and outcomes, correct the directed witness walk, cite independent oracle fragments and describe the actual observed subject, expected criterion, comparison relation and failure meaning. Keep selected comparison references inside the case oracle; retain explicit negative-scope or missing-oracle gaps.'],
  'ATLAS-UNDERSTANDING':['The declared project breadth lacks owned source, a facet or an explicit gap.','Review actual component owners and record all seven facets. Bind flows to their owners; retain PARTIAL/UNKNOWN where source or oracle proof is incomplete.'],
  'ATLAS-ASSESSMENT':['A review score lacks a supported rubric, scope or source evidence.','Supply explicit 1–100 correctness/verification/clarity inputs, 40/30/30 arithmetic, axis weights and reviewed evidence. Do not infer scores from test counts.'],
  'ATLAS-SCHEMA':['Required input fields or supported structure are absent.','Read the report contract and repair the actual input; do not manufacture empty evidence.'],
  'ATLAS-UNKNOWN':['An unclassified validation failure occurred.','Preserve the original evidence and inspect its actual producer before assigning a known cause.']
};
export class AtlasGovernanceError extends Error {
  constructor(code,observed){const [meaning,hint]=rules[code]||rules['ATLAS-UNKNOWN'];super(code+'\nObserved: '+observed+'\nFailure meaning: '+meaning+'\nRepair hint: '+hint+'\nOwner: '+owner+'\nRecheck: '+recheck);Object.assign(this,{name:'AtlasGovernanceError',code,observed,meaning,hint,owner,recheck});}
}
export function atlasFailure(observed){
  if(/^(?:Invalid JSON input|Unsupported Atlas schema|Unsupported source inventory|Nonempty |Claim IDs|Evidence IDs|Duplicate or unowned source)/.test(observed))return new AtlasGovernanceError('ATLAS-SCHEMA',observed);
  const mappings=[[/^(?:Use a normalized|Missing or escaping|Analysis must belong)/,'ATLAS-PATH'],[/^(?:Atlas target|Declare the source revision|Live Git revision)/,'ATLAS-TARGET'],[/^(?:Live source drift|Live source inventory|Source index integrity|Invalid declared text normalization)/,'ATLAS-SOURCE'],[/^(?:Generated document integrity|Duplicate document|Required Atlas document)/,'ATLAS-DOCUMENT'],[/^(?:Reference is outside|Source quote differs|Invalid source line range)/,'ATLAS-REFERENCE'],[/^(?:Surefire|Node TAP|Missing\/ambiguous Node|Missing\/invalid Surefire|Unsafe Surefire|Contradictory Surefire|Declared execution|Execution |Claim source was not|Claim verdict)/,'ATLAS-EVIDENCE']];
  return new AtlasGovernanceError(mappings.find(([pattern])=>pattern.test(observed))?.[1]||'ATLAS-UNKNOWN',observed);
}
export function requireAtlas(condition,code,observed){if(!condition)throw new AtlasGovernanceError(code,observed);}

/** Preserve JSON syntax semantics while refusing ambiguous member identities at every object depth. */
export function parseAtlasJson(text,code,name){
  requireAtlas(typeof text==='string',code,'Original '+name+' text is missing');
  let value;
  try{value=JSON.parse(text);}catch(error){throw new AtlasGovernanceError(code,'Invalid JSON input: '+name+' / '+error.message);}
  const scopes=[];
  // Syntax is already validated. Walk strings iteratively so large escaped audits
  // do not consume the regular-expression engine's tokenization stack.
  for(let index=0;index<text.length;index++){
    const character=text[index];
    if(character==='{'){scopes.push(new Set());continue;}
    if(character==='['){scopes.push(null);continue;}
    if(character==='}'||character===']'){scopes.pop();continue;}
    if(character!=='"')continue;
    const start=index;
    for(index++;index<text.length;index++){
      if(text[index]==='\\'){index++;continue;}
      if(text[index]==='"')break;
    }
    let next=index+1;
    while(next<text.length&&' \t\r\n'.includes(text[next]))next++;
    if(text[next]!==':')continue;
    const spelling=text.slice(start,index+1);
    const keys=scopes.at(-1),key=JSON.parse(spelling);
    requireAtlas(!keys.has(key),code,'Duplicate JSON member '+spelling+' in '+name+' at character '+start+'; remove the duplicate and review the intended value');
    keys.add(key);
  }
  return value;
}
