import {createHash} from 'node:crypto';
import {businessSections} from './atlas-reader.mjs';
import {reviewAtlasLayout} from './atlas-presentation.mjs';
import {requireAtlas} from './atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-CALIBRATION',message);
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const hash=value=>createHash('sha256').update(value).digest('hex');
const meaningful=value=>typeof value==='string'&&value.trim().length>=30;
const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);

/** Run in real document QA only. Nothing from this probe is embedded in the business reader. */
export function atlasCalibrationProbe(){
  const visible=node=>{const box=node.getBoundingClientRect();return box.width>0&&box.height>0&&getComputedStyle(node).visibility!=='hidden';};
  return {
    cases:[...document.querySelectorAll('.reader-case')].filter(visible).map(node=>({title:node.querySelector('h3').textContent.replace(/^\d{2}(?:\.\d{2})+\s*/,'').trim(),kind:node.querySelector('.eyebrow').textContent.trim(),proof:node.querySelector('.reader-proof')?.textContent.trim()||node.querySelectorAll('p')[2]?.textContent.trim()||''})),
    breadth:[...document.querySelectorAll('.reader-breadth tbody tr')].filter(visible).map(node=>[...node.cells].map(cell=>cell.textContent.trim())),
    hiddenPayloads:document.querySelectorAll('template,script,pre,code,iframe,object').length,
    smallText:[...document.querySelectorAll('p,td,summary')].filter(node=>visible(node)&&parseFloat(getComputedStyle(node).fontSize)<13).map(node=>node.textContent.slice(0,80)),
    graphClipping:[...document.querySelectorAll('.flow .node')].flatMap(node=>{const box=node.querySelector('rect').getBoundingClientRect();return [...node.querySelectorAll('tspan')].filter(text=>{const area=text.getBoundingClientRect();return area.left<box.left-2||area.right>box.right+2||area.top<box.top-2||area.bottom>box.bottom+2;}).map(text=>text.textContent);}),
    invisibleLabels:[...document.querySelectorAll('.flow text')].filter(node=>!visible(node)||['transparent','rgba(0, 0, 0, 0)'].includes(getComputedStyle(node).fill)).map(node=>node.textContent),
    graphs:[...document.querySelectorAll('.atlas-graph')].map(node=>({legend:node.querySelector('.atlas-legend')?.textContent.trim()||'',contained:node.scrollWidth>node.clientWidth+2,named:!!node.getAttribute('aria-label'),focusable:node.tabIndex===0,smallLabels:[...node.querySelectorAll('text')].filter(text=>text.getBoundingClientRect().height<13).map(text=>text.textContent)})),
    touch:navigator.maxTouchPoints,ratio:devicePixelRatio
  };
}

/** Identity and declared source comparisons are prerequisites, not automatic semantic attestation. */
export function reviewAtlasCalibration({analysis,html,auditSha256,capture,sourceReview}){
  need(object(analysis)&&object(analysis.reader)&&typeof html==='string'&&object(capture)&&object(sourceReview),'Supply the original analysis, actual HTML, native capture and authored live-source review');
  for(const item of [capture,sourceReview])need(item.projectId===analysis.projectId&&item.sourceRevision===analysis.revision&&item.sourceIndexSha256===analysis.sourceIndexSha256,'Capture or source review belongs to another project, source revision or inventory');
  need(typeof auditSha256==='string'&&/^[a-f0-9]{64}$/.test(auditSha256)&&capture.auditSha256===auditSha256,'Native observations must retain the exact original adjacent audit identity');
  need(capture.schemaVersion===1&&capture.htmlSha256===hash(html)&&capture.native===true&&capture.emulated===false&&capture.browser==='CHROMIUM','Bind native desktop metrics to these actual HTML bytes; emulation or stale screenshots cannot qualify them');
  need(sourceReview.schemaVersion===1&&meaningful(sourceReview.reviewer)&&meaningful(sourceReview.limits)&&sourceReview.coverageDeclaration===analysis.understanding.scope,'Declare the actual reviewer, semantic limits and selected/full review scope');
  const claims=new Set(analysis.claims.map(claim=>claim.id)),comparisons=sourceReview.comparisons;
  need(Array.isArray(comparisons)&&same(comparisons.map(item=>item.sectionId),businessSections),'Compare every reader section with actual source and project documentation in reading order');
  for(const row of comparisons){
    need(['SUPPORTED','PARTIAL'].includes(row.verdict)&&meaningful(row.observation)&&meaningful(row.limits)&&Array.isArray(row.claimIds)&&row.claimIds.length>0&&new Set(row.claimIds).size===row.claimIds.length&&row.claimIds.every(id=>claims.has(id)),'A source comparison needs real claim owners, a reasoned observation and limits: '+row.sectionId);
    need(Array.isArray(row.documentPaths)&&new Set(row.documentPaths).size===row.documentPaths.length&&row.documentPaths.every(file=>(analysis.sourceDocuments||[]).includes(file))&&(!(analysis.sourceDocuments||[]).length||row.documentPaths.length>0),'Compare declared live project documents, or preserve their actual absence: '+row.sectionId);
  }
  need(Array.isArray(capture.records)&&capture.records.length===2&&same(capture.records.map(row=>row.width).sort((a,b)=>a-b),[900,1440]),'Retain both distinct native desktop widths: 1440 and 900 CSS px');
  need(Array.isArray(capture.networkRequests)&&Array.isArray(capture.pageErrors),'Retain original resource and page-error observations');
  const findings=[],add=(width,code,observation)=>findings.push({width,code,observation});
  if(capture.networkRequests.length||capture.pageErrors.length)add(0,'OFFLINE','Unexpected external requests or document errors');
  const claimsById=new Map(analysis.claims.map(claim=>[claim.id,claim]));
  const expectedCases=analysis.reader.journeys.flatMap(journey=>journey.cases.map(item=>{const source=analysis.behaviorFlows.find(flow=>flow.id===journey.flowId).caseAnalysis.cases.find(row=>row.id===item.caseId),oracle=claimsById.get(source.oracleClaimId);if(source.oracleStatus==='EXECUTED')need(oracle?.status==='EXECUTED'&&['PASSED','FAILED'].includes(oracle.verdict),'An executed case lacks its actual recorded oracle verdict');return {title:item.title,kind:source.kind==='POSITIVE'?'Expected success':source.kind==='NEGATIVE'?'Expected refusal or failure':'Boundary case',proof:source.oracleStatus==='EXECUTED'?(oracle?.verdict==='FAILED'?'Recorded check failed':'Recorded check passed'):source.oracleStatus==='NOT_FOUND'?'No existing check identified':'Check reviewed in source; no execution inferred'};}));
  const expectedBreadth=analysis.understanding.units.map(unit=>{const status=Object.values(unit.facets||{}).map(facet=>facet.status),depth=!status.length?'Review depth not declared':status.includes('UNKNOWN')?(status.includes('PARTIAL')?'Partial review; some areas unestablished':'Some areas unestablished'):status.includes('PARTIAL')?'Partial review':'Declared aspects reviewed';return [analysis.reader.capabilities.find(item=>item.unitId===unit.id).title,depth];});
  const expectedGraphs=analysis.reader.journeys.length+(analysis.reader.architecture||[]).length+1;
  for(const record of capture.records){
    need(object(record.metrics)&&object(record.structure)&&Array.isArray(record.navigation),'Retain actual layout, structure and interaction observations');
    const {structure,width}=record;
    need(record.metrics.viewport===width&&structure.touch===0&&structure.ratio===1,'Native viewport and device observations differ from the declared desktop run');
    for(const key of ['cases','breadth','smallText','graphClipping','invisibleLabels','graphs'])need(Array.isArray(structure[key]),'Missing native observation: '+key);
    for(const finding of reviewAtlasLayout(record.metrics).findings)add(width,'LAYOUT',finding);
    if(!same(structure.cases,expectedCases))add(width,'CASE_VERDICT','Visible case identities, expected kinds or actual recorded verdicts differ from their source-bound oracles');
    if(!same(structure.breadth,expectedBreadth))add(width,'REVIEW_DEPTH','Visible capability review depth hides or promotes partial/unknown source scope');
    if(record.navigation.length!==6||!record.navigation.every((row,i)=>row.target==='#'+businessSections[i]+'-content'&&row.opened===true)||record.keyboardDisclosure!==true)add(width,'NAVIGATION','Navigation must open the intended closed content, and native keyboard disclosure must work');
    if(structure.hiddenPayloads||structure.smallText.length||structure.graphClipping.length||structure.invisibleLabels.length)add(width,'READABILITY','Hidden payload, tiny text or clipped/invisible graph content is present');
    if(structure.graphs.length!==expectedGraphs||structure.graphs.some(graph=>!meaningful(graph.legend)||!Array.isArray(graph.smallLabels)||graph.smallLabels.length||graph.contained&&(!graph.named||!graph.focusable)))add(width,'DIAGRAMS','Retain source-bound architecture and behavior diagrams, adjacent legends, legible labels and accessible local scrolling');
  }
  return {status:findings.length?'REPAIR_REQUIRED':'PASSED',findings,scope:'BOUND_NATIVE_RENDER_AND_AUTHORED_SOURCE_COMPARISONS; LIVE_SOURCE_VERIFICATION_AND_HUMAN_SEMANTIC_REVIEW_REQUIRED',cases:expectedCases.length,capabilities:expectedBreadth.length,graphs:expectedGraphs};
}

export function validateAtlasCalibration(options){const result=reviewAtlasCalibration(options);need(result.status==='PASSED',result.findings.map(finding=>finding.code+' at '+finding.width+': '+finding.observation).join('\n'));return result;}
