import {requireAtlas} from './atlas-diagnostics.mjs';

const need=(condition,message)=>requireAtlas(condition,'ATLAS-EVIDENCE',message);
const nonempty=value=>typeof value==='string'&&value.trim().length>0;
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const xmlPoint=point=>(point===9||point===10||point===13||point>=32&&point<=0x10ffff)
  &&!(point>=0xd800&&point<=0xdfff)&&(point&0xffff)!==0xfffe&&(point&0xffff)!==0xffff;

function xmlText(value){
  return value.replace(/&([^;\s<&]+);|&/g,(entity,name)=>{
    need(name!==undefined,'Execution testcase contains a malformed XML entity');
    const predefined={amp:'&',lt:'<',gt:'>',quot:'"',apos:"'"};
    if(Object.hasOwn(predefined,name))return predefined[name];
    need(/^#(?:\d+|x[\da-fA-F]+)$/.test(name),'Execution testcase contains an unsupported XML entity: '+entity);
    const point=name[1]==='x'?Number.parseInt(name.slice(2),16):Number(name.slice(1));
    need(Number.isSafeInteger(point)&&xmlPoint(point),
    'Execution testcase contains an invalid XML character reference');
    return String.fromCodePoint(point);
  });
}

function xmlCases(text){
  for(const character of text)need(xmlPoint(character.codePointAt(0)),'Execution XML contains an invalid raw character');
  const stack=[],cases=[];let cursor=0,roots=0;
  const malformed=message=>need(false,'Execution XML structure is invalid: '+message);
  while(cursor<text.length){
    if(text[cursor]!=='<'){
      const end=text.indexOf('<',cursor),next=end<0?text.length:end;
      if(!stack.length)need(!text.slice(cursor,next).replace(/^\uFEFF/,'').trim(),'Execution XML has text outside its suite root');
      else xmlText(text.slice(cursor,next));
      cursor=next;continue;
    }
    const consume=(open,close)=>{const end=text.indexOf(close,cursor+open.length);if(end<0)malformed('unterminated '+open);cursor=end+close.length;};
    if(text.startsWith('<!--',cursor)){consume('<!--','-->');continue;}
    if(text.startsWith('<?',cursor)){consume('<?','?>');continue;}
    if(text.startsWith('<![CDATA[',cursor)){need(stack.length>0,'Execution XML CDATA needs a containing element');consume('<![CDATA[',']]>');continue;}
    need(!/^<!DOCTYPE\b|^<!ENTITY\b/i.test(text.slice(cursor)),'Execution testcase bindings cannot resolve XML document types or entities');
    if(text.startsWith('</',cursor)){
      const closing=/^<\/([A-Za-z_][\w:.-]*)\s*>/.exec(text.slice(cursor));
      need(closing&&stack.length&&stack.at(-1).name===closing[1],'Execution XML has unmatched closing elements');
      const node=stack.pop();if(node.testcase)cases.push(node.testcase);cursor+=closing[0].length;continue;
    }
    const opening=/^<([A-Za-z_][\w:.-]*)/.exec(text.slice(cursor));
    need(opening,'Execution XML contains an unsupported declaration or malformed element');
    const name=opening[1],attributes={};cursor+=opening[0].length;
    let empty=false;
    while(true){
      const whitespace=/^\s*/.exec(text.slice(cursor))[0];cursor+=whitespace.length;
      if(text.startsWith('/>',cursor)){empty=true;cursor+=2;break;}
      if(text[cursor]==='>'){cursor++;break;}
      need(whitespace.length>0,'Execution XML attributes must be separated');
      const attribute=/^([A-Za-z_][\w:.-]*)\s*=\s*(["'])/.exec(text.slice(cursor));
      need(attribute,'Execution XML contains a malformed attribute');cursor+=attribute[0].length;
      const end=text.indexOf(attribute[2],cursor);need(end>=0,'Execution XML attribute is unterminated');
      const value=text.slice(cursor,end);need(!value.includes('<'),'Execution XML attribute contains raw markup');
      need(!Object.hasOwn(attributes,attribute[1]),'Execution testcase has an ambiguous repeated attribute');
      attributes[attribute[1]]=xmlText(value);cursor=end+1;
    }
    if(!stack.length){roots++;need(roots===1&&['testsuite','testsuites'].includes(name),'Execution XML needs one suite root');}
    const parent=stack.at(-1),suitePath=stack.every(node=>['testsuite','testsuites'].includes(node.name));
    const node={name};
    if(name==='testcase'&&parent?.name==='testsuite'&&suitePath)node.testcase={testClass:attributes.classname||'',testName:attributes.name||'',status:'PASSED'};
    if(parent?.testcase){
      if(['failure','error'].includes(name))parent.testcase.status='FAILED';
      if(name==='skipped'&&parent.testcase.status!=='FAILED')parent.testcase.status='NOT_EXECUTED';
    }
    if(empty){if(node.testcase)cases.push(node.testcase);}else stack.push(node);
  }
  need(roots===1&&stack.length===0,'Execution XML suite root is absent or unclosed');
  return cases;
}

function tapCases(text){
  return [...text.matchAll(/^(ok|not ok) \d+([^\r\n]*)$/gm)].map(match=>{
    const directive=/#\s*(?:SKIP|TODO)\b/i.test(match[2]);
    return {testClass:'',testName:match[2].replace(/^\s*-\s?/,'').replace(/\s+#\s*(?:SKIP|TODO)\b.*$/i,'').trim(),
      status:directive?'NOT_EXECUTED':match[1]==='ok'?'PASSED':'FAILED'};
  });
}

/** Binds declared behavior execution to retained test identities; semantic correspondence still needs review. */
export function validateFlowExecutionBindings({analysis,evidence}){
  need(object(analysis)&&Array.isArray(analysis.claims)&&Array.isArray(evidence),'Behavior execution binding needs analysis claims and verified evidence');
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim])),proofs=new Map(evidence.map(item=>[item.id,item]));
  const selected=new Set(),parsed=new Map();
  need(analysis.behaviorFlows===undefined||Array.isArray(analysis.behaviorFlows),'Behavior execution scope must be an array');
  for(const flow of analysis.behaviorFlows||[]){
    need(object(flow)&&Array.isArray(flow.claimIds),'Behavior execution scope needs declared source claims');
    for(const id of [...flow.claimIds,flow.oracleClaimId])if(claims.get(id)?.status==='EXECUTED')selected.add(id);
  }
  let subjects=0;
  for(const id of selected){
    const claim=claims.get(id),refs=new Set([...(claim.sourceRefs||[]),...(claim.governanceRefs||[])].map(ref=>ref.path));
    need(Array.isArray(claim.evidenceIds)&&claim.evidenceIds.length>0&&Array.isArray(claim.executionSubjects)&&claim.executionSubjects.length>0,
      'Executed behavior claim needs retained testcase bindings: '+id);
    const covered=new Set(),identities=new Set(),outcomes=[];
    for(const subject of claim.executionSubjects){
      need(object(subject)&&nonempty(subject.evidenceId)&&claim.evidenceIds.includes(subject.evidenceId)
        &&nonempty(subject.testName)&&nonempty(subject.sourcePath)&&refs.has(subject.sourcePath),
      'Execution testcase binding must use this claim evidence and a quoted source path: '+id);
      const proof=proofs.get(subject.evidenceId);
      need(proof&&proof.projectId===analysis.projectId&&typeof proof.text==='string'&&['PASSED','FAILED'].includes(proof.status)
        &&['SUREFIRE_XML','NODE_TAP'].includes(proof.format),'Execution testcase binding lacks verified XML/TAP for this target: '+id);
      need(proof.format==='SUREFIRE_XML'?typeof subject.testClass==='string':subject.testClass===undefined||subject.testClass==='',
        'Execution testcase class must match XML exactly and remain empty for flat Node TAP: '+id);
      const identity=JSON.stringify([subject.evidenceId,subject.testClass||'',subject.testName,subject.sourcePath]);
      need(!identities.has(identity),'Execution testcase binding is duplicated: '+id);identities.add(identity);
      if(!parsed.has(proof.id))parsed.set(proof.id,proof.format==='SUREFIRE_XML'?xmlCases(proof.text):tapCases(proof.text));
      const matches=parsed.get(proof.id).filter(item=>item.testClass===(subject.testClass||'')&&item.testName===subject.testName);
      need(matches.length===1&&['PASSED','FAILED'].includes(matches[0].status),
        'Execution testcase binding is absent, ambiguous or skipped: '+id+'/'+subject.testName);
      covered.add(proof.id);outcomes.push(matches[0].status);subjects++;
    }
    need(claim.evidenceIds.every(proofId=>covered.has(proofId)),'Every behavior execution evidence needs its retained testcase binding: '+id);
    need(claim.verdict===(outcomes.includes('FAILED')?'FAILED':'PASSED'),'Behavior claim verdict differs from its selected testcase outcomes: '+id);
  }
  return {status:'PASSED',claims:selected.size,subjects,scope:'DECLARED_BEHAVIOR_TESTCASE_BINDINGS; semantic correspondence requires source review'};
}
