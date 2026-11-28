import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
import {presentationTable} from '../../tools/atlas-presentation.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-READER',message);
const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
const unique=a=>Array.isArray(a)&&new Set(a).size===a.length;

/** Mask comments and literals before recognizing a named declaration; this is not a call-graph parser. */
export function declaredSymbol(ref,symbol){
  if(!ref||typeof ref.quote!=='string'||typeof symbol!=='string')return false;
  const code=ref.quote.replace(/\/\*[\s\S]*?\*\/|\/\/[^\n]*|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|`(?:\\.|[^`\\])*`/g,m=>m.replace(/[^\n]/g,' '));
  if(ref.path.endsWith('.java')){
    const owner=ref.path.split('/').at(-1).replace(/\.java$/,''),parts=symbol.split('.');
    if(parts.length!==2||parts[0]!==owner||!/^\w+$/.test(parts[1]))return false;
    return new RegExp('(?:^|\\n)\\s*(?:(?:public|protected|private|static|final|synchronized|abstract|native|default)\\s+)*(?:[\\w<>,?.\\[\\]]+\\s+)+'+parts[1]+'\\s*\\([^;{}]*\\)\\s*(?:throws[^{}]+)?\\{').test(code);
  }
  return /\.(?:mjs|cjs|js)$/.test(ref.path)&&/^[$\w]+$/.test(symbol)&&new RegExp('\\b(?:function\\s+'+symbol+'\\s*\\(|(?:const|let)\\s+'+symbol+'\\s*=)').test(code);
}

/** Link a selected business decision to actual flow-local implementation and independent original cases. */
export function businessTraceProjection(analysis){
  const trace=analysis.reader.sourceTrace;if(trace===undefined)return null;
  need(trace&&trace.profile==='BUSINESS_IMPLEMENTATION_V1'&&Object.keys(trace).every(k=>['profile','rows'].includes(k)),'Declare the supported business-to-implementation trace profile');
  need(Array.isArray(trace.rows)&&trace.rows.length>0&&trace.rows.length<=12&&trace.rows.every(r=>r&&typeof r==='object'&&!Array.isArray(r))&&unique(trace.rows.map(r=>r.flowId)),'Choose distinct dominant business decisions; do not replace the story with a class inventory');
  const claims=new Map(analysis.claims.map(c=>[c.id,c]));
  return trace.rows.map(row=>{
    need(row&&Object.keys(row).every(k=>['flowId','subject','decision','limit','implementations','caseIds'].includes(k)),'Keep trace rows within their declared source-owned fields');
    const flow=analysis.behaviorFlows.find(f=>f.id===row.flowId),journey=analysis.reader.journeys.find(j=>j.flowId===row.flowId);
    need(flow&&journey,'A source trace must belong to a visible existing behavior');
    for(const key of ['subject','decision','limit'])need(typeof row[key]==='string'&&row[key].trim().length>=15&&row[key].trim().split(/\s+/).length<=35,'Keep each trace explanation concise and business-readable: '+key);
    need(unique(row.caseIds)&&same(row.caseIds,flow.caseAnalysis.cases.map(c=>c.id)),'Retain every original positive, negative and boundary case for each selected trace flow');
    need(Array.isArray(row.implementations)&&row.implementations.length>0&&row.implementations.length<=4,'Name the concrete collaboration behind the decision, not an exhaustive inventory');
    const implementations=row.implementations.map(item=>{
      need(item&&Object.keys(item).every(k=>['claimId','refIndex','symbol','meaning'].includes(k)),'Keep implementation trace references within their owned fields');
      const claim=claims.get(item.claimId),ref=claim?.sourceRefs?.[item.refIndex];
      need(flow.claimIds.includes(item.claimId)&&['SOURCE_REVIEWED','EXECUTED'].includes(claim?.status)&&Number.isInteger(item.refIndex)&&item.refIndex>=0&&ref,'Implementation evidence must belong to the selected existing flow');
      need(declaredSymbol(ref,item.symbol),'Name a real declaration in the selected source fragment; comments, strings, calls and unrelated owners do not establish a method: '+item.symbol);
      need(typeof item.meaning==='string'&&item.meaning.trim().split(/\s+/).length<=24&&item.meaning.length>=15,'Explain why the concrete responsibility matters to this business decision');
      return {symbol:item.symbol,meaning:item.meaning};
    });
    need(unique(implementations.map(i=>i.symbol)),'Do not repeat a concrete symbol to inflate mapping depth');
    const cases=flow.caseAnalysis.cases.map(c=>{const oracle=claims.get(c.oracleClaimId);need(oracle&&['SOURCE_REVIEWED','EXECUTED','UNKNOWN'].includes(oracle.status),'Retain each selected independent oracle or explicit gap');need(oracle.status!=='EXECUTED'||['PASSED','FAILED'].includes(oracle.verdict),'Keep the actual recorded oracle verdict for this trace');return {kind:c.kind,status:oracle.status,verdict:oracle.status==='EXECUTED'?oracle.verdict:null};});
    const proof=cases.some(c=>c.verdict==='FAILED')?'A retained check failed':cases.every(c=>c.status==='EXECUTED'&&c.verdict==='PASSED')?'Retained checks passed':cases.some(c=>c.status==='UNKNOWN')?'Some checks remain unestablished':'Checks reviewed in source; execution is not inferred';
    return {flowId:row.flowId,subject:row.subject,decision:row.decision,implementations,caseKinds:cases.map(c=>c.kind),proof,limit:row.limit};
  });
}

export function businessTraceRows(rows){return rows?.map(r=>[r.subject+' '+r.decision,r.implementations.map(i=>i.symbol+' — '+i.meaning).join('\n'),r.proof+'. '+r.caseKinds.length+' existing cases. '+r.limit])||null;}

export function renderBusinessTrace(rows){
  if(!rows)return '';
  let table=presentationTable(['Business decision','Concrete responsibilities','Evidence and boundary'],businessTraceRows(rows));for(const row of rows)for(const item of row.implementations)table=table.replaceAll(item.symbol+' —','<span class="reader-symbol">'+item.symbol.replaceAll('.','.<wbr>')+'</span> —');
  return '<details class="reader-source-trace"><summary>Business decisions and implementation evidence</summary><p>Selected decisions are traced to named source responsibilities and their original cases. Listed responsibilities do not imply an automatically established call sequence.</p>'+table+'</details>';
}

export function reviewBusinessTrace(actual,expected){
  return same(actual,expected)?[]:[{code:'BUSINESS_SOURCE_TRACE',observation:'Restore the original business decision, concrete source responsibilities, case kinds, actual oracle verdict and remaining boundary. Bind declarations to the selected flow; a class name or passing count alone is insufficient.'}];
}
