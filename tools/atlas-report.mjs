import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {isDeepStrictEqual} from 'node:util';
import {fileURLToPath} from 'node:url';
import {verifyAtlas} from './verify-atlas.mjs';
import {requireAtlas,parseAtlasJson} from './atlas-diagnostics.mjs';
import {retrieveAtlasKnowledge,validateAtlasKnowledge} from './atlas-knowledge.mjs';
import {validateAtlasBundle} from './atlas-bundle.mjs';
import {validateAtlasUnderstanding,understandingFacets,validateCurrentUnderstandingProfiles} from './atlas-understanding.mjs';
import {validateAtlasBehavior} from './atlas-behavior.mjs';
import {presentationTable,presentationMarkdown,graphLegend} from './atlas-presentation.mjs';
import {businessSections,renderBusinessReader,inspectBusinessReaderHtml} from './atlas-reader.mjs';

const repositoryRoot=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
export const atlasSections=['context','behavior','evidence','trace','architecture','audit','knowledge','limits','documents','provenance'];
const requiredDocuments=['README.md','project-map.md','architecture.md','testing-and-governance.md','open-questions.md'];
const digest=bytes=>createHash('sha256').update(bytes).digest('hex');
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;');
const decode=value=>value.replaceAll('&lt;','<').replaceAll('&gt;','>').replaceAll('&quot;','"').replaceAll('&#39;',"'").replaceAll('&amp;','&');
const within=(root,file)=>{const relative=path.relative(root,file);return !path.isAbsolute(relative)&&relative!=='..'&&!relative.startsWith('..'+path.sep);};
const nonempty=value=>typeof value==='string'&&value.trim().length>0;
const preservedText=bytes=>{const text=bytes.toString('utf8');return Buffer.from(text).equals(bytes)&&!/[\u0000-\u0008\u000b\u000c\u000e-\u001f]/.test(text)?{encoding:'UTF8',text}:{encoding:'BASE64',text:bytes.toString('base64')};};

function ownedFile(root,relative){
  requireAtlas(nonempty(relative)&&!relative.includes('\\')&&!relative.includes(':')&&!relative.startsWith('/')
    &&relative.split('/').every(part=>part&&part!=='.'&&part!=='..'),'ATLAS-PATH','Invalid relative package file: '+relative);
  const file=path.join(root,...relative.split('/'));
  requireAtlas(fs.existsSync(file)&&fs.lstatSync(file).isFile()&&within(root,fs.realpathSync(file)),'ATLAS-PATH','Missing or escaping package file: '+relative);
  return file;
}

function canonicalCss(root){
  const css=fs.readFileSync(path.join(root,'atf-atlas/assets/atlas.css'),'utf8').replaceAll('\r\n','\n');
  requireAtlas(css.includes(':root')&&!/<\/style|@import|expression\s*\(/i.test(css)
    &&![...css.matchAll(/url\(([^)]+)\)/gi)].some(match=>!/^['"]?#[\w-]+['"]?$/.test(match[1].trim())),
  'ATLAS-CSS','Canonical CSS needs local fragment resources only');
  return css;
}

function reviewGraph(graph,claims,dependency){
  requireAtlas(graph&&graph.kind===(dependency?'DEPENDENCY':'BEHAVIOR')&&/^[a-z][a-z0-9-]*$/.test(graph.id||'')&&nonempty(graph.title)
    &&Array.isArray(graph.nodes)&&graph.nodes.length>0&&Array.isArray(graph.edges)&&(graph.edges.length>0||nonempty(graph.limits)),
  'ATLAS-BEHAVIOR','A graph needs identity, title, nodes and traced edges');
  const ids=new Set();
  for(const node of graph.nodes){
    requireAtlas(/^[a-z][a-z0-9-]*$/.test(node.id||'')&&!ids.has(node.id)&&nonempty(node.label)&&node.label.length<=48
      &&Number.isFinite(node.x)&&node.x>=140&&node.x<=760&&Number.isFinite(node.y)&&node.y>=35&&node.y<=1800,
    'ATLAS-BEHAVIOR','Graph node identity, label or geometry is invalid: '+graph.id);
    requireAtlas(dependency?['MODULE','COMPONENT','SERVICE','BOUNDARY'].includes(node.kind):['INTENT','ACTION','DECISION','STATE','OUTCOME'].includes(node.kind),
      'ATLAS-BEHAVIOR','Dependency modules cannot substitute for behavior objects: '+graph.id+'/'+node.id);
    ids.add(node.id);
  }
  for(let first=0;first<graph.nodes.length;first++)for(let second=first+1;second<graph.nodes.length;second++)requireAtlas(
    Math.abs(graph.nodes[first].x-graph.nodes[second].x)>=240||Math.abs(graph.nodes[first].y-graph.nodes[second].y)>=62,
    'ATLAS-BEHAVIOR','Graph node frames overlap: '+graph.id);
  for(const edge of graph.edges)requireAtlas(ids.has(edge.from)&&ids.has(edge.to)&&(!dependency||edge.from!==edge.to)&&nonempty(edge.label)
    &&claims.has(edge.claimId)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(edge.claimId).status),
  'ATLAS-BEHAVIOR','Graph edge lacks a reviewed claim or endpoint: '+graph.id);
}

function reviewUnderstanding(analysis,{requiredCases=false,sourceIndex}={}){
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim]));
  requireAtlas(Array.isArray(analysis.behaviorFlows)&&analysis.behaviorFlows.length>0,'ATLAS-BEHAVIOR','A complete understanding report needs an existing behavior and its oracle');
  const ids=new Set();
  for(const flow of analysis.behaviorFlows){
    requireAtlas(/^[a-z][a-z0-9-]*$/.test(flow.id||'')&&!ids.has(flow.id)
      &&['title','actor','intent','object','operation','outcome','testOracle','limits'].every(key=>nonempty(flow[key]))
      &&Array.isArray(flow.claimIds)&&flow.claimIds.length>0&&flow.claimIds.every(id=>claims.has(id)&&['SOURCE_REVIEWED','EXECUTED'].includes(claims.get(id).status))
      &&claims.has(flow.oracleClaimId)&&['SOURCE_REVIEWED','EXECUTED','NOT_FOUND'].includes(flow.oracleStatus)
      &&(flow.oracleStatus==='NOT_FOUND'?claims.get(flow.oracleClaimId).status==='UNKNOWN':flow.oracleStatus===claims.get(flow.oracleClaimId).status),
    'ATLAS-BEHAVIOR','Behavior needs an actor, object, outcome, exact oracle claim and limits: '+flow.id);
    const allowed=new Set([...flow.claimIds,flow.oracleClaimId]);
    requireAtlas(flow.graph&&Array.isArray(flow.graph.edges)&&flow.graph.edges.every(edge=>allowed.has(edge.claimId)),'ATLAS-BEHAVIOR','A transition cites a claim outside its declared behavior: '+flow.id);
    ids.add(flow.id);reviewGraph(flow.graph,claims,false);
  }
  requireAtlas(Array.isArray(analysis.dependencyGraphs)&&analysis.dependencyGraphs.length>0,'ATLAS-BEHAVIOR','Declare dependency architecture separately from behavior');
  for(const graph of analysis.dependencyGraphs){requireAtlas(!ids.has(graph.id),'ATLAS-BEHAVIOR','Duplicate graph identity');ids.add(graph.id);reviewGraph(graph,claims,true);}
  return validateAtlasBehavior(analysis,{required:requiredCases,sourceIndex});
}

function graphSvg(graph){
  const nodes=new Map(graph.nodes.map(node=>[node.id,node])),height=Math.max(...graph.nodes.map(node=>node.y))+100;
  const edges=graph.edges.map((edge,index)=>{
    const from=nodes.get(edge.from),to=nodes.get(edge.to),start=from.y+48,end=to.y;
    if(from.id===to.id){
      const clear=side=>{const near=from.x+side*110,far=from.x+side*200,left=Math.min(near,far),right=Math.max(near,far);
        return left>=10&&right<=890&&!graph.nodes.some(node=>node.id!==from.id&&node.x-110<right&&node.x+110>left&&node.y<from.y+40&&node.y+48>from.y+8);};
      const side=clear(1)?1:clear(-1)?-1:0;requireAtlas(side,'ATLAS-BEHAVIOR','Move nodes to provide a clear self-loop corridor: '+graph.id+'/'+from.id);
      const x=from.x+side*110,control=from.x+side*200;
      return '<path class="edge" d="M'+x+','+(from.y+12)+' C'+control+','+from.y+' '+control+','+(from.y+48)+' '+x+','+(from.y+36)+'"/><text class="el" x="'+(control+side*12)+'" y="'+(from.y+27)+'">'+escape(edge.label)+'</text>';
    }
    const obstructed=from.x===to.x&&graph.nodes.some(node=>node.id!==from.id&&node.id!==to.id
      &&Math.abs(node.x-from.x)<120&&node.y<end&&node.y+48>start);
    const route=to.y<=from.y||obstructed?35+(index%2)*12:null;
    const line=route===null?`M${from.x},${start} C${from.x},${start+35} ${to.x},${end-35} ${to.x},${end}`
      :`M${from.x-110},${from.y+24} H${route} V${to.y+24} H${to.x-110}`;
    const labelX=route===null?(from.x+to.x)/2+75:route+45,labelY=route===null?(start+end)/2:Math.min(from.y,to.y)+18;
    return '<path class="edge'+(edge.label==='test'?' dashed':'')+'" d="'+line+'"/><text class="el" x="'+labelX+'" y="'+labelY+'">'+escape(edge.label)+'</text>';
  }).join('');
  const boxes=graph.nodes.map(node=>{
    const label=node.label,spaces=[...label.matchAll(/ /g)].map(match=>match.index).filter(index=>index>=label.length-24&&index<=24);
    const boundary=spaces.sort((left,right)=>Math.abs(left-label.length/2)-Math.abs(right-label.length/2))[0]??Math.floor(label.length/2);
    const lines=label.length<=24?[label]:[label.slice(0,boundary),label.slice(boundary).trim()];
    return '<g><rect class="node owned" x="'+(node.x-110)+'" y="'+node.y+'" width="220" height="48" rx="11"/><text class="nt">'+lines.map((line,index)=>'<tspan x="'+node.x+'" y="'+(node.y+(lines.length===1?29:18)+index*17)+'">'+escape(line)+'</tspan>').join('')+'</text></g>';
  }).join('');
  return '<div class="diag"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 '+height+'" role="img" aria-label="'+escape(graph.title)+'"><title>'+escape(graph.title)+'</title>'+edges+boxes+'</svg></div>';
}

const legacyTable=(headers,rows)=>'<div class="table-scroll"><table><thead><tr>'+headers.map(value=>'<th>'+escape(value)+'</th>').join('')+'</tr></thead><tbody>'+rows.map(row=>'<tr>'+row.map(value=>'<td>'+escape(value)+'</td>').join('')+'</tr>').join('')+'</tbody></table></div>';
const details=(title,body)=>'<details><summary>'+escape(title)+'</summary><div class="content">'+body+'</div></details>';
const original=(title,value)=>details(title,'<pre>'+escape(typeof value==='string'?value:JSON.stringify(value,null,2))+'</pre>');
const section=(id,title,body,open=false)=>'<details id="'+id+'" data-atlas-section="'+id+'"'+(open?' open':'')+'><summary>'+escape(title)+'</summary><div class="content">'+body+'</div></details>';

function legacyMarkdown(text){
  const output=[];let fence=null,code=[];
  for(const line of text.split(/\r?\n/)){
    const marker=/^\s*(`{3,}|~{3,})(.*)$/.exec(line);
    if(marker&&!fence){fence=marker[1];code=[];continue;}
    if(marker&&fence&&marker[1][0]===fence[0]&&marker[1].length>=fence.length){output.push('<pre><code>'+escape(code.join('\n'))+'</code></pre>');fence=null;continue;}
    if(fence){code.push(line);continue;}
    if(/^#{1,6}\s/.test(line)){output.push('<h3>'+escape(line.replace(/^#{1,6}\s+/,''))+'</h3>');continue;}
    if(line.trim())output.push('<p>'+escape(line)+'</p>');
  }
  if(fence)output.push('<pre><code>'+escape(code.join('\n'))+'</code></pre>');
  return output.join('');
}

function reviewKnowledgeReceipt(receipt,source,required){
  requireAtlas(receipt&&nonempty(receipt.query),'ATLAS-KNOWLEDGE','A cited retrieval query is required');
  let expected=retrieveAtlasKnowledge({query:receipt.query});
  // A portable capture retains its original raw digest even across an LF/CRLF checkout.
  const currentKnowledge=fs.readFileSync(path.join(repositoryRoot,'atf-atlas/knowledge/atlas-contracts.json'),'utf8');
  if(source?.encoding==='UTF8'&&typeof source.text==='string'&&digest(Buffer.from(source.text))===source.sha256
    &&source.text.replaceAll('\r\n','\n')===currentKnowledge.replaceAll('\r\n','\n'))expected={...expected,sourceSha256:source.sha256};
  requireAtlas(isDeepStrictEqual(receipt,expected),'ATLAS-KNOWLEDGE','The complete retrieval receipt differs from canonical retrieval');
  requireAtlas(receipt.corpusSha256===expected.corpusSha256&&receipt.sourceSha256===expected.sourceSha256&&receipt.retrievalScope===expected.retrievalScope
    &&receipt.semanticRecall===expected.semanticRecall&&Array.isArray(receipt.records)&&Array.isArray(receipt.selectedIds)&&Array.isArray(receipt.mandatoryIds),
  'ATLAS-KNOWLEDGE','Embedded retrieval must match the canonical knowledge contract');
  requireAtlas(receipt.archiveRevision===expected.archiveRevision
    &&JSON.stringify(receipt.records)===JSON.stringify(expected.records)&&JSON.stringify(receipt.citations)===JSON.stringify(expected.citations)
    &&JSON.stringify(receipt.ranking)===JSON.stringify(expected.ranking)&&JSON.stringify(receipt.selectedIds)===JSON.stringify(expected.selectedIds)
    &&JSON.stringify(receipt.mandatoryIds)===JSON.stringify(expected.mandatoryIds),
    'ATLAS-KNOWLEDGE','Retrieved optional records, citations, ranking or mandatory policy differ from canonical retrieval');
  if(source){
    requireAtlas(source.path==='atf-atlas/knowledge/atlas-contracts.json'&&source.encoding==='UTF8'&&typeof source.text==='string'
      &&digest(Buffer.from(source.text))===source.sha256&&receipt.sourceSha256===source.sha256,
      'ATLAS-KNOWLEDGE','The retrieval receipt does not match its complete original corpus bytes');
    const corpus=parseAtlasJson(source.text,'ATLAS-KNOWLEDGE','original corpus payload');
    requireAtlas(validateAtlasKnowledge(corpus).corpusSha256===receipt.corpusSha256,'ATLAS-KNOWLEDGE','Original corpus and retrieved canonical identity differ');
  }else requireAtlas(!required&&receipt.sourceSha256===expected.sourceSha256,'ATLAS-KNOWLEDGE','Complete original knowledge source is required for renderer version 2');
}

export function renderAtlasHtml(bundle,css){
  validateAtlasBundle(bundle);reviewKnowledgeReceipt(bundle.knowledge,bundle.knowledgeSource,bundle.renderVersion>=2);
  const caseReview=reviewUnderstanding(bundle.analysis,{requiredCases:bundle.renderVersion>=3,sourceIndex:bundle.sourceIndex});
  if(bundle.renderVersion>=5)return renderBusinessReader(bundle,css);
  const {analysis,sourceIndex,knowledge,documents,evidenceFiles,consistency}=bundle;
  const table=bundle.renderVersion>=4?presentationTable:legacyTable;
  const markdown=bundle.renderVersion>=4?presentationMarkdown:legacyMarkdown;
  const diagram=graph=>bundle.renderVersion>=4?'<figure class="atlas-graph">'+graphSvg(graph)+graphLegend(graph)+'</figure>':graphSvg(graph);
  const claims=analysis.claims;
  const breadth=validateAtlasUnderstanding(analysis,sourceIndex,{required:bundle.renderVersion>=2});
  const matrix=analysis.understanding?details('Declared review breadth and remaining gaps', '<p>'+escape(analysis.understanding.scopeStatement)+'</p>'+table(['Owner / scope',...understandingFacets],analysis.understanding.units.map(unit=>[unit.label+' / '+unit.scopePaths.join(', '),...understandingFacets.map(name=>unit.facets[name].status)]))
    +analysis.understanding.units.map(unit=>details(unit.label+' — source evidence and limits',table(['Facet','Review status','Declared evidence','Explanation'],understandingFacets.map(name=>[name,unit.facets[name].status,unit.facets[name].claimIds.join(', '),unit.facets[name].explanation]))+'<p>'+escape(unit.limits)+'</p>')).join('')+'<p>'+escape(breadth.limitation)+'</p>'):'';
  const scores=breadth.assessment?'<h2>Evidence-bound review scores</h2><p>'+escape(breadth.assessment.scope)+' '+escape(breadth.assessment.limits)+'</p>'+breadth.assessment.groups.map(group=>details(group.label+' — weighted review '+group.score+'/100',table(['Axis','Score','Weight','Correctness / verification / clarity','Source claims','Review basis and limits'],group.axes.map(axis=>[axis.label,axis.score,axis.weight,[axis.correctness,axis.verification,axis.clarity].join(' / '),axis.claimIds.join(', '),axis.reason+' '+axis.limits]))+'<p>'+escape(group.scope+' '+group.limits)+'</p>')).join(''):'';
  const context='<p>'+escape(analysis.projectId)+' · revision <code>'+escape(analysis.revision)+'</code></p><p>Source fingerprint: <code>'+escape(sourceIndex.fingerprint||digest(JSON.stringify(sourceIndex.files)))+'</code></p>'
    +'<p>Worktree: '+escape(sourceIndex.worktreeStatus?'DIRTY; exact state retained below':'No dirty state declared')+'. Consistency: '+escape(consistency.status)+'. Semantic completeness: HUMAN_REVIEW_REQUIRED. Assessment: '+(breadth.assessment?'EXPLICIT_SCOPED_REVIEW':'NOT_ASSESSED')+'.</p>'
    +'<p>Source review, executed checks and historical observations have separate scopes. A later edit requires a new capture and verification.</p>';
  const behavior=analysis.behaviorFlows.map(flow=>'<article class="card"><h2>'+escape(flow.title)+'</h2>'+diagram(flow.graph)
    +(flow.caseAnalysis?table(['Case','Kind / exact oracle fragment','Precondition and trigger','Expected observation','Directed witness','Independent assertion and limits'],flow.caseAnalysis.cases.map(witness=>[
      witness.title,witness.kind+' / '+witness.oracleStatus+' / '+witness.oracleClaimId+' / '+(witness.oracleStatus==='NOT_FOUND'
        ?'Searched: '+witness.oracleGap.searchedClaimIds.join(', ')+'. '+witness.oracleGap.explanation
        :witness.oracleRefIndices.map(index=>{const ref=claims.find(claim=>claim.id===witness.oracleClaimId).sourceRefs[index];return ref.path+':'+ref.startLine+'–'+ref.endLine;}).join('; ')),witness.precondition+' '+witness.trigger,witness.expected,
      witness.nodeIds.join(' → ')+'; edges: '+witness.edgeIds.join(' → '),witness.assertion+' '+witness.limits]))+'<p>'+escape('Negative review: '+flow.caseAnalysis.negativeScope.status+'. '+flow.caseAnalysis.negativeScope.explanation)+'</p>'
      +original('Declared path coverage and explicit gaps',caseReview.flows.find(item=>item.id===flow.id)):'')
    +table(['Behavior field','Observed understanding'],[['Actor',flow.actor],['Intent',flow.intent],['Object',flow.object],['Operation',flow.operation],['Outcome',flow.outcome],['Existing test oracle',flow.testOracle],['Oracle scope',flow.oracleStatus],['Oracle claim',flow.oracleClaimId],['Proof limit',flow.limits],['Source claims',flow.claimIds.join(', ')]])+'</article>').join('');
  const evidence=table(['Claim','Statement','Scope / verdict','Related governance','Execution IDs'],claims.map(claim=>[claim.id,claim.statement,claim.status+(claim.verdict?' / '+claim.verdict:''),claim.governanceStatus,(claim.evidenceIds||[]).join(', ')]))
    +'<p>Related mappings are traceability, not complete coverage of every clause. NOT_MAPPED remains explicit.</p>';
  const trace=claims.map(claim=>details(claim.id+' · '+claim.statement,[...(claim.sourceRefs||[]).map(ref=>({...ref,owner:'Source'})),...(claim.governanceRefs||[]).map(ref=>({...ref,owner:'Related governance'}))].map(ref=>'<p class="source-path">'+escape(ref.owner+' · '+ref.path+':'+ref.startLine+'–'+ref.endLine)+'</p><pre>'+escape(ref.quote)+'</pre>').join(''))).join('');
  const architecture=analysis.dependencyGraphs.map(graph=>'<h2>'+escape(graph.title)+'</h2>'+diagram(graph)+table(['From','To','Declared relation','Source claim'],graph.edges.map(edge=>[edge.from,edge.to,edge.label,edge.claimId]))).join('')
    +'<p>These are declared module dependencies. Behavior diagrams above describe observations and outcomes separately.</p>';
  const audit=table(['Evidence','Project','Original status','Tests / failures / errors / skipped','Selection','Executed source identity'],(analysis.evidence||[]).map(item=>[item.id,item.projectId,item.status,[item.tests,item.failures,item.errors,item.skipped].map(value=>value??'—').join(' / '),item.selection,item.sourceIdentity]))
    +'<p>Execution verdicts come from original supported XML/TAP. Consistency success does not change a failed or historical execution into a passing result.</p>'+original('Verification result',consistency);
  const knowledgeView='<p>Retrieval: '+escape(knowledge.retrievalScope)+'. Semantic recall: '+escape(knowledge.semanticRecall)+'. Corpus SHA-256: <code>'+escape(knowledge.corpusSha256)+'</code>.</p>'
    +'<p>All mandatory requirements apply, independently of query ranking.</p>'+original('Complete cited retrieval receipt',knowledge);
  const limits='<p>Unknowns and contrary observations remain visible. No new product journey, E2E test or emulator is introduced by this publication.</p>'
    +original('Complete proof boundaries',documents.find(item=>item.path==='open-questions.md').text)
    +original('Recorded execution versus captured source',documents.find(item=>item.path==='execution-source-comparison.json')?.text||'No whole-tree comparison supplied; individual executed claim references remain checked.');
  const allDocuments=[...documents,...(bundle.sourceDocuments||[]).map(item=>({...item,path:'Target source: '+item.path}))].map(item=>details(item.path+' · SHA-256 '+item.sha256,
    item.path.endsWith('.md')?markdown(item.text):'<pre>'+escape(item.text)+'</pre>')).join('');
  const provenance=original('Complete analysis contract',analysis)+original('Complete source index',sourceIndex)
    +evidenceFiles.map(item=>original(item.path+' · '+item.encoding+' · SHA-256 '+item.sha256,item.text)).join('')
    +(bundle.referencedSources||[]).map(item=>original('Complete referenced source: '+item.path+' · '+item.encoding+' · SHA-256 '+item.sha256,item.text)).join('');
  const titles=['Executive context','Behavior and existing test oracles','Business / contract evidence tables','Source-to-behavior trace','Architecture and dependencies','Atlas governance and execution audit','Retrieved knowledge and requirements','Limits, findings and ownership','Complete input documentation','Complete machine and execution provenance'];
  const bodies=[context+matrix+scores,behavior,evidence,trace,architecture,audit,knowledgeView,limits,allDocuments,provenance];
  const navigation=atlasSections.map((id,index)=>'<a href="#'+id+'">'+escape(titles[index])+'</a>').join('');
  const body=atlasSections.map((id,index)=>section(id,titles[index],bodies[index],index===0)).join('');
  return '<!doctype html>\n<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>'+escape(analysis.projectId)+' · complete understanding Atlas</title><style id="atlas-style" data-sha256="'+digest(css)+'">'+css+'</style></head><body><div class="wrap"><header class="hero"><p class="eyebrow">Atlas · governed source understanding</p><h1>'+escape(analysis.projectId)+' — understanding Atlas</h1><p>One target. One complete document. Dedicated embedded design and auditable evidence.</p><div class="pill-row"><span class="pill">'+escape(analysis.revision.slice(0,12))+'</span><span class="pill">'+claims.length+' source-bound claims</span><span class="pill">'+sourceIndex.files.length+' indexed files</span><span class="pill">English</span></div></header><nav aria-label="Atlas sections">'+navigation+'</nav><svg width="0" height="0" aria-hidden="true"><defs><marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto"><path d="M0,0 L10,5 L0,10 z"/></marker></defs></svg>'+body+'<template id="atlas-bundle">'+escape(JSON.stringify(bundle))+'</template><footer><p>Generated from declared source and preserved evidence. No network resource or companion page is required. Review scope and source identity remain explicit.</p></footer></div></body></html>\n';
}

export function validateAtlasHtml({html,css,projectId,auditBundle}){
  if(html.includes('data-atlas-reader="5"')){
    inspectBusinessReaderHtml(html,{projectId});
    requireAtlas(auditBundle&&auditBundle.renderVersion===5,'ATLAS-READER','The reader needs its separate original audit bundle for verification; reading remains offline and independent');
    requireAtlas(auditBundle.analysis.projectId===projectId,'ATLAS-TARGET','The reader audit belongs to another target');
    requireAtlas(html.includes('<style id="atlas-style">'+css+'</style>'),'ATLAS-CSS','The reader does not embed canonical CSS');
    requireAtlas(html===renderAtlasHtml(auditBundle,css),'ATLAS-READER','The business reader differs from its audited projection, numbered sections or preserved limits');
    return {status:'PASSED',projectId,sections:businessSections.length,offline:true,scope:'BUSINESS_READER_WITH_ADJACENT_AUDIT'};
  }
  const payload=/<template id="atlas-bundle">([\s\S]*?)<\/template>/.exec(html);
  requireAtlas(payload,'ATLAS-HTML','Complete machine bundle is missing');
  const bundle=parseAtlasJson(decode(payload[1]),'ATLAS-HTML','embedded analysis bundle');
  requireAtlas(bundle.analysis.projectId===projectId,'ATLAS-TARGET','The complete HTML belongs to another target');
  requireAtlas(html.includes('<style id="atlas-style" data-sha256="'+digest(css)+'">'+css+'</style>'),'ATLAS-CSS','The complete HTML does not embed canonical CSS');
  requireAtlas(html===renderAtlasHtml(bundle,css),'ATLAS-HTML','The complete HTML differs from its governed sections, content or local resources');
  return {status:'PASSED',projectId,sections:atlasSections.length,offline:true,scope:'DETERMINISTIC_COMPLETE_DOCUMENT_CONSISTENCY'};
}

export function validateAtlasDelivery({deliveryRoot,projectId,css}){
  const root=fs.realpathSync(deliveryRoot),files=[];
  const walk=directory=>{for(const entry of fs.readdirSync(directory,{withFileTypes:true})){const file=path.join(directory,entry.name);requireAtlas(!entry.isSymbolicLink(),'ATLAS-PATH','Delivery contains a symbolic link');if(entry.isDirectory())walk(file);else if(entry.isFile())files.push(file);}};
  walk(root);
  requireAtlas(files.length===1&&path.relative(root,files[0])==='atlas.html','ATLAS-HTML','A target invocation must deliver exactly one file: delivery/atlas.html');
  const html=fs.readFileSync(files[0],'utf8');
  const auditBundle=html.includes('data-atlas-reader="5"')?parseAtlasJson(fs.readFileSync(ownedFile(path.dirname(root),'atlas-audit.json'),'utf8'),'ATLAS-EVIDENCE','adjacent Atlas audit'):undefined;
  return validateAtlasHtml({html,projectId,css,auditBundle});
}

export function prepareAtlasBundle({sourceRoot,analysisRoot,artifactRoot,root=repositoryRoot,query='business project understanding audience source evidence styling numbered sections'}){
  requireAtlas([sourceRoot,analysisRoot,artifactRoot].every(nonempty),'ATLAS-SCHEMA','Declare source, analysis and artifact roots for one target');
  const source=fs.realpathSync(sourceRoot),analysisDirectory=fs.realpathSync(analysisRoot),artifacts=fs.realpathSync(artifactRoot);
  const consistency=verifyAtlas({sourceRoot:source,analysisRoot:analysisDirectory,artifactRoot:artifacts});
  const analysisText=fs.readFileSync(ownedFile(analysisDirectory,'analysis.json'),'utf8'),analysis=parseAtlasJson(analysisText,'ATLAS-SCHEMA','analysis.json');
  const sourceIndexText=fs.readFileSync(ownedFile(analysisDirectory,'source-index.json'),'utf8'),sourceIndex=parseAtlasJson(sourceIndexText,'ATLAS-SOURCE','source-index.json');
  reviewUnderstanding(analysis,{requiredCases:true,sourceIndex});
  const documents=analysis.documents.map(item=>{
    const bytes=fs.readFileSync(ownedFile(analysisDirectory,item.path));requireAtlas(digest(bytes)===item.sha256,'ATLAS-DOCUMENT','Input document changed before publication: '+item.path);return {...item,...preservedText(bytes)};
  });
  requireAtlas(requiredDocuments.every(required=>documents.some(item=>item.path===required)),'ATLAS-DOCUMENT','A complete Atlas needs all five maintained analysis inputs');
  const evidenceFiles=[],seen=new Set();
  for(const item of analysis.evidence||[])for(const relative of [item.path,item.sourceManifest].filter(Boolean)){
    if(seen.has(relative))continue;seen.add(relative);
    const bytes=fs.readFileSync(ownedFile(artifacts,relative)),expected=relative===item.path?item.sha256:item.sourceIdentity;
    requireAtlas(digest(bytes)===expected,'ATLAS-EVIDENCE','Evidence changed before publication: '+relative);
    evidenceFiles.push({path:relative,sha256:expected,...preservedText(bytes)});
  }
  const referencedPaths=new Set(analysis.claims.flatMap(claim=>[...(claim.sourceRefs||[]),...(claim.governanceRefs||[])].map(ref=>ref.path)));
  requireAtlas(!analysis.sourceDocuments||Array.isArray(analysis.sourceDocuments)&&analysis.sourceDocuments.every(nonempty),'ATLAS-SCHEMA','sourceDocuments must contain target-relative document paths');
  for(const relative of analysis.sourceDocuments||[])referencedPaths.add(relative);
  const referencedSources=[...referencedPaths].sort().map(relative=>{
    const entry=sourceIndex.files.find(item=>item.path===relative),bytes=fs.readFileSync(ownedFile(source,relative));
    requireAtlas(entry&&digest(bytes)===entry.sha256,'ATLAS-SOURCE','Referenced target source changed before publication: '+relative);
    return {path:relative,sha256:entry.sha256,...preservedText(bytes)};
  });
  const sourceDocuments=referencedSources.filter(item=>(analysis.sourceDocuments||[]).includes(item.path));
  const knowledge=retrieveAtlasKnowledge({query,root}),css=canonicalCss(root),knowledgeBytes=fs.readFileSync(path.join(root,'atf-atlas/knowledge/atlas-contracts.json'));
  const knowledgeSource={path:'atf-atlas/knowledge/atlas-contracts.json',sha256:digest(knowledgeBytes),...preservedText(knowledgeBytes)};
  const bundle={schemaVersion:1,renderVersion:4,analysis,analysisText,sourceIndex,sourceIndexText,documents,sourceDocuments,referencedSources,evidenceFiles,knowledge,knowledgeSource,consistency};
  return bundle;
}

export function publishAtlas(options){
  const bundle={...prepareAtlasBundle(options),renderVersion:5},css=canonicalCss(options.root||repositoryRoot);
  if(options.requireCurrentUnderstanding){bundle.publicationProfile='CURRENT_UNDERSTANDING_V2';bundle.profileReview=validateCurrentUnderstandingProfiles(bundle.analysis,bundle.sourceIndex);}
  const {analysis}=bundle,artifacts=fs.realpathSync(options.artifactRoot),analysisDirectory=fs.realpathSync(options.analysisRoot);
  const html=renderAtlasHtml(bundle,css);validateAtlasHtml({html,css,projectId:analysis.projectId,auditBundle:bundle});
  const delivery=path.join(analysisDirectory,'delivery');
  const audit=path.join(analysisDirectory,'atlas-audit.json');
  requireAtlas(within(artifacts,delivery)&&!fs.existsSync(delivery)&&!fs.existsSync(audit),'ATLAS-PATH','Use a new analysis delivery directory and adjacent audit; existing output is preserved');
  fs.writeFileSync(audit,JSON.stringify(bundle,null,2)+'\n',{flag:'wx'});
  fs.mkdirSync(delivery);fs.writeFileSync(path.join(delivery,'atlas.html'),html,{flag:'wx'});
  const verification=validateAtlasDelivery({deliveryRoot:delivery,projectId:analysis.projectId,css});
  return {...verification,output:path.join(delivery,'atlas.html'),bytes:Buffer.byteLength(html),sha256:digest(html),audit,auditSha256:digest(fs.readFileSync(audit)),sourceRevision:analysis.revision,knowledgeSha256:bundle.knowledge.corpusSha256};
}

if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
  try{
    const command=process.argv[2],options={},names={'--source':'sourceRoot','--analysis':'analysisRoot','--artifacts':'artifactRoot','--query':'query'};
    for(let index=3;index<process.argv.length;index+=2){const flag=process.argv[index],value=process.argv[index+1];requireAtlas(Object.hasOwn(names,flag)&&value&&!Object.hasOwn(options,names[flag]),'ATLAS-SCHEMA','Unknown, duplicate or missing argument: '+flag);options[names[flag]]=value;}
    const result=command==='retrieve'?retrieveAtlasKnowledge({query:options.query}):command==='publish'||command==='understand'?publishAtlas({...options,requireCurrentUnderstanding:command==='understand'}):null;
    requireAtlas(result,'ATLAS-SCHEMA','Use retrieve, understand or publish with the declared input paths');
    process.stdout.write(JSON.stringify(result)+'\n');
  }catch(error){process.stderr.write(error.message+'\n');process.exitCode=1;}
}
