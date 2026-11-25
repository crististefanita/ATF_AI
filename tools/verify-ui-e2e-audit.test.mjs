import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import {spawnSync} from 'node:child_process';
import {createHash} from 'node:crypto';
import {deflateSync} from 'node:zlib';
import {fileURLToPath} from 'node:url';
import {readSurefireUiResults,retainedUiE2eInventory,verifyUiE2eAudit} from './verify-ui-e2e-audit.mjs';

// These are synthetic component inputs, never a browser execution or application emulator.
const sha=value=>createHash('sha256').update(value).digest('hex');
const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&apos;');
const subject=entry=>entry.className+'#'+entry.methodName;
function crc(bytes){let value=0xffffffff;for(const byte of bytes){value^=byte;for(let bit=0;bit<8;bit++)value=value&1?(value>>>1)^0xedb88320:value>>>1;}return(value^0xffffffff)>>>0;}
function chunk(type,data){const bytes=Buffer.alloc(12+data.length);bytes.writeUInt32BE(data.length);bytes.write(type,4,'ascii');data.copy(bytes,8);bytes.writeUInt32BE(crc(bytes.subarray(4,8+data.length)),8+data.length);return bytes;}
function image(index){const header=Buffer.alloc(13);header.writeUInt32BE(1);header.writeUInt32BE(1,4);header[8]=8;header[9]=2;return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chunk('IHDR',header),chunk('IDAT',deflateSync(Buffer.from([0,index,90,140]))),chunk('IEND',Buffer.alloc(0))]);}
function fixture(options={}){
  const classes=[...new Set(retainedUiE2eInventory.map(entry=>entry.className))],expected={};
  for(const entry of retainedUiE2eInventory)(expected[entry.className]||=[]).push(entry.methodName);
  const setupClasses=['com.ai.ui.tests.AccountAuthenticatedAreaTests','com.ai.ui.tests.ProfileContactAddressMemoryTests'];
  const firstNative=Object.fromEntries(classes.map(name=>[name,expected[name].slice().sort((left,right)=>left.localeCompare(right))[0]]));
  const engine=options.engine||'playwright',source={
    schemaVersion:1,role:'RETAINED_PRODUCT_E2E_EXECUTION',runId:'component-native-record-'+engine,engine,targetUrl:'https://application.example.test',
    sourceCommit:'a'.repeat(40),harnessSha256:sha('component runner source'),
    args:['-Dui.engine='+engine,'-Dbase.url=https://application.example.test','-Dreporting.engine=extent',
      '-Dui.screenshots.enabled=true','-Dui.screenshots.on.failure.only=false','-Dui.screenshots.capture.final.state=true'],
    screenshotPolicy:{enabled:true,failureOnly:false,captureFinalState:true},selectedClasses:classes,expectedProductMethods:expected,expectedProductCases:16,manifest:{}
  };
  for(const name of classes)source.manifest['atf-tests/src/test/java/'+name.replaceAll('.','/')+'.java']=sha(name);
  for(const name of ['RegistrationValidation','ProfileAccountValidation'])source.manifest['atf-tests/src/test/java/com/ai/ui/support/validation/'+name+'.java']=sha(name);
  let next=10;
  const captures=[],cases=[],htmlByClass=new Map(classes.map(name=>[name,[]]));
  const card=(id,label,status,details,terminals,children='')=>'<div class="card"><div class="card-header"><div class="card-toolbar"><span class="badge '+status.toLowerCase()+'-bg log">'+status+'</span></div><div class="card-title"><a class="node" id="'+id+'"><span>'+escape(label)+'</span></a></div></div><div class="collapse"><div class="card-body"><table><tbody>'
    +details.map(text=>'<tr class="event-row"><td><span class="badge log info-bg">INFO</span></td><td>00:00</td><td>'+escape(text)+'</td></tr>').join('')
    +terminals.map(entry=>'<tr class="event-row"><td><span class="badge log '+entry.status.toLowerCase()+'-bg">'+entry.status+'</span></td><td>00:00</td><td>'+escape(entry.text)+'</td></tr>').join('')
    +'</tbody></table></div></div>'+children+'</div>';
  for(const [index,entry]of retainedUiE2eInventory.entries()){
    const key=subject(entry),status=options.statuses?.[key]||'PASS',early=options.earlyFailure===key;
    const nodeId=String(next++),classNodeId='test-id:class-'+classes.indexOf(entry.className),steps=[],chunks=[];
    const append=(label,status,details,terminals,parentId=null,children='')=>{
      const id=String(next++);steps.push({id,parentId,title:label,status,details,terminalMessages:terminals.map(term=>term.text)});
      return {id,html:card(id,label,status,details,terminals,children)};
    };
    if(status!=='SKIP'){
      const setupOwner=options.lateSetupFor===entry.className?expected[entry.className][0]:firstNative[entry.className];
      if(setupClasses.includes(entry.className)&&entry.methodName===setupOwner&&options.omitSetupFor!==entry.className){
        const addSetup=()=>{
          const setupId=String(next++),beforeId=String(next++),outside=options.setupOutside===entry.className;
          const phaseDefinitions=[
            {phase:'registration navigation',label:'Navigate to URL: '+source.targetUrl+'/auth/register',details:['base.url='+source.targetUrl,'relativePath=/auth/register'],terminal:'Navigated'},
            {phase:'registration email action',label:'Type into element: Email address',details:['locator=input[data-test=email]','value=component-user@example.test'],terminal:'Typed'},
            {phase:'registration password action',label:'Type into element: Password',details:['locator=input[data-test=password]','value=<redacted>'],terminal:'Typed'},
            {phase:'registration submission',label:'Click on: Register',details:['locator=button[data-test=register-submit]'],terminal:'Clicked'},
            {phase:'login email action',label:'Type into element: Email address',details:['locator=input[data-test=email]','value=component-user@example.test'],terminal:'Typed'},
            {phase:'login password action',label:'Type into element: Password',details:['locator=input[data-test=password]','value=<redacted>'],terminal:'Typed'},
            {phase:'login submission',label:'Click on: Login',details:['locator=button[data-test=login-submit]'],terminal:'Clicked'},
            {phase:'profile navigation',label:'Open URL: '+source.targetUrl+'/account/profile',details:['base.url='+source.targetUrl,'relativePath=/account/profile'],terminal:'Opened'}
          ].filter(phase=>options.missingSetupPhase?.className!==entry.className||options.missingSetupPhase.phase!==phase.phase)
            .map(phase=>options.omitNavigation?{...phase,details:phase.details.filter(detail=>!detail.startsWith('base.url='))}:phase);
          if(options.reorderedSetupFor===entry.className)phaseDefinitions.push(phaseDefinitions.shift());
          const actionHtml=phaseDefinitions.map(phase=>append(phase.label,'PASS',phase.details,[{status:'PASS',text:phase.terminal}],outside?null:beforeId).html).join('');
          const setupTitle=options.wrongSetupParentFor===entry.className?'TEST BODY':'SETUP';
          steps.push({id:setupId,parentId:null,title:setupTitle,status:'PASS',details:[],terminalMessages:[]});
          steps.push({id:beforeId,parentId:setupId,title:'@BeforeClass',status:'PASS',details:[],terminalMessages:[]});
          chunks.push(card(setupId,setupTitle,'PASS',[],[],card(beforeId,'@BeforeClass','PASS',[],[],outside?'':actionHtml)));
          if(outside)chunks.push(actionHtml);
        };
        addSetup();
        if(options.duplicateSetupFor===entry.className)addSetup();
      }
      if(!options.omitNavigation){
        const navigation=append('Navigate to URL: '+(options.navigationTarget||source.targetUrl)+'/auth/register','PASS',['base.url='+(options.navigationTarget||source.targetUrl),'relativePath=/auth/register'],[{status:'PASS',text:'Navigated'}]);chunks.push(navigation.html);
      }
      const actionStatus=early?'FAIL':'PASS',secret=options.secretValue||'<redacted>';
      const action=append('Type into element: Password',actionStatus,["locator=input[data-test='password']",'value='+secret],[{status:actionStatus,text:early?'Original action failed':'Typed'}]);chunks.push(action.html);
      if(!early&&options.omitValidations!==key){
        for(const [validationIndex,label]of entry.validations.entries()){
          const validationId=String(next++),failure=status==='FAIL'&&validationIndex===0,stepStatus=failure?'FAIL':'PASS';
          let details=['expected=accepted','actual=accepted'];
          if(options.omitExpected===key&&validationIndex===0)details=details.filter(value=>!value.startsWith('expected='));
          if(options.omitActual===key&&validationIndex===0)details=details.filter(value=>!value.startsWith('actual='));
          const terminals=options.omitTerminal===key&&validationIndex===0?[]:[{status:stepStatus,text:failure?'Original business assertion failed':options.wrongTerminal===key&&validationIndex===0?'Read succeeded':'Validated '+label.slice('Validate '.length)}];
          const read=append('Get text from: observed field','PASS',['locator=span[data-test=field]','text=accepted'],[{status:'PASS',text:'Text read'}],validationId);
          steps.push({id:validationId,parentId:null,title:label,status:stepStatus,details,terminalMessages:terminals.map(term=>term.text)});
          chunks.push(card(validationId,label,stepStatus,details,terminals,read.html));
        }
      }
    }
    let png=image(index+1);
    if(options.corruptPng===key){png=Buffer.from(png);png[45]^=1;}
    const screenshots=status==='PASS'&&options.omitScreenshot!==key?[{sha256:sha(png),mimeType:'image/png',bytes:png.length,width:1,height:1,label:'Final Screenshot'}]:[];
    const media=screenshots.length?'<div class="row mb-3"><div class="col-md-3"><a data-featherlight="image" href="data:image/png;base64,'+png.toString('base64')+'"><span>base64 img</span></a><div class="title">'+(options.wrongImageLabel===key?'Decorative icon':'Final Screenshot')+'</div></div></div>':'';
    htmlByClass.get(entry.className).push({methodName:entry.methodName,html:card(nodeId,entry.methodName,status,[],[],chunks.join('')+media)});
    captures.push({className:entry.className,methodName:entry.methodName,methodTitle:entry.methodName,nodeId,classNodeId,status,steps,screenshots,
      validationState:status==='SKIP'||early?'NOT_REACHED':'REACHED',...(status==='SKIP'?{notReachedReason:'SKIPPED_BY_TESTNG'}:early?{notReachedReason:'FAILED_BEFORE_VALIDATION'}:{})});
    const marker=status==='FAIL'?'<failure message="Original native failure"><![CDATA[Original native failure <preserved>]]></failure>':status==='SKIP'?'<skipped message="Skipped by native dependency"/>':'';
    cases.push({className:entry.className,methodName:entry.methodName,status,xml:'<testcase classname="'+entry.className+'" name="'+entry.methodName+'">'+marker+'</testcase>'});
  }
  const configurationFailures=[];
  if(options.configurationFailure){
    const entry={className:'com.ai.ui.tests.AccountAuthenticatedAreaTests',methodName:'createCustomerProfile',status:'FAIL'};configurationFailures.push(entry);
    cases.push({...entry,xml:'<testcase classname="'+entry.className+'" name="'+entry.methodName+'"><error message="Original setup failed"/></testcase>'});
  }
  const totals={total:cases.length,passed:cases.filter(entry=>entry.status==='PASS').length,failed:cases.filter(entry=>entry.status==='FAIL').length,skipped:cases.filter(entry=>entry.status==='SKIP').length};
  const errors=options.configurationFailure?1:0;
  const xml='<?xml version="1.0" encoding="UTF-8"?><testsuite tests="'+totals.total+'" failures="'+(totals.failed-errors)+'" errors="'+errors+'" skipped="'+totals.skipped+'">'+cases.map(entry=>entry.xml).join('')+'</testsuite>';
  const body=classes.map((name,index)=>'<li class="test-item" test-id="class-'+index+'"><p class="name">'+name.slice(name.lastIndexOf('.')+1)+'</p><div class="test-contents"><div class="detail-head">'+name+'</div>'+htmlByClass.get(name).sort((left,right)=>left.methodName.localeCompare(right.methodName)).map(entry=>entry.html).join('')+'</div></li>').join('');
  const ledger='parentCount:'+totals.total+',passParent:'+totals.passed+',failParent:'+totals.failed+',skipParent:'+totals.skipped+',warningParent:0,childCount:0,passChild:0,failChild:0,warningChild:0,skipChild:0,infoChild:0,grandChildCount:0,passGrandChild:0,failGrandChild:0,warningGrandChild:0,skipGrandChild:0,infoGrandChild:0,eventsCount:0,passEvents:0,failEvents:0,warningEvents:0,skipEvents:0,infoEvents:0';
  const html='<!doctype html><html><body data-atf-report-mode="runtime-audited" data-atf-runtime-audit="true"><h3 data-atf-test-cases-total="true">'+totals.total+'</h3><script>var statusGroup = {'+ledger+'};</script><ul class="test-list">'+body+'</ul></body></html>';
  const sourceManifestText=JSON.stringify(source,null,2)+'\n';
  return {sourceManifestText,surefireXml:xml,extentHtml:html,capture:{schemaVersion:1,captureMode:'NATIVE_CHROMIUM_DOM',runId:source.runId,sourceCommit:source.sourceCommit,uiEngine:source.engine,baseUrl:source.targetUrl,
    sourceManifestSha256:sha(sourceManifestText),surefireXmlSha256:sha(xml),htmlSha256:sha(html),probeSha256:sha('component probe source'),reportMode:'runtime-audited',totals,methods:captures,configurationFailures}};
}
const first=subject(retainedUiE2eInventory[0]),second=subject(retainedUiE2eInventory[1]);
const rebind=value=>{value.capture.sourceManifestSha256=sha(value.sourceManifestText);value.capture.surefireXmlSha256=sha(value.surefireXml);value.capture.htmlSha256=sha(value.extentHtml);return value;};
const sourceChange=(value,edit)=>{const source=JSON.parse(value.sourceManifestText);edit(source);value.sourceManifestText=JSON.stringify(source);return rebind(value);};
const rejected=(value,rule)=>assert.throws(()=>verifyUiE2eAudit(value),error=>error.message.startsWith('UI-E2E-AUDIT '+rule+'\n')&&error.message.includes('Owner: docs/Testing.md#existing-product-e2e-audit')&&error.message.includes('Repair hint:')&&error.message.includes('Recheck:'));

for(const engine of ['playwright','selenium']){
  test('complete original '+engine+' subject/step/PNG evidence reconciles without claiming execution attestation',()=>{
    const value=fixture({engine}),before=JSON.stringify(value),result=verifyUiE2eAudit(value);
    assert.equal(result.status,'PASSED');assert.equal(result.executionAttestation,'NOT_PROVIDED');
    assert.deepEqual(result.productTotals,{total:16,passed:16,failed:0,skipped:0});
    assert.equal(result.methods.length,16);assert.equal(result.methods.filter(entry=>entry.screenshots===1).length,16);
    assert.ok(result.methods.every(entry=>entry.redactedInputs>=1&&entry.validationCoverage==='COMPLETE'));
    assert.deepEqual(result.classSetup.map(entry=>entry.state),['COMPLETE','COMPLETE']);
    assert.equal(result.classSetup[0].setupSubject,'com.ai.ui.tests.AccountAuthenticatedAreaTests#accountFavoritesAreaIsReachable');
    assert.equal(result.classSetup[1].setupSubject,'com.ai.ui.tests.ProfileContactAddressMemoryTests#profileKeepsRegisteredCityAndState');
    assert.ok(result.classSetup.every(entry=>entry.orderedActions.length===8));
    assert.equal(JSON.stringify(value),before,'Pure reconciliation must leave every input unchanged.');
  });
}
test('native FAIL and SKIP remain failed/skipped while evidence consistency can pass',()=>{
  const value=fixture({statuses:{[first]:'FAIL',[second]:'SKIP'}}),result=verifyUiE2eAudit(value);
  assert.deepEqual(result.productTotals,{total:16,passed:14,failed:1,skipped:1});
  assert.equal(result.methods[0].nativeOutcome,'FAIL');assert.equal(result.methods[1].validationCoverage,'NOT_REACHED');
});
test('an actual failed action can retain an explicit unreached business oracle',()=>{
  const value=fixture({statuses:{[first]:'FAIL'},earlyFailure:first}),result=verifyUiE2eAudit(value);
  assert.equal(result.methods[0].validationState,'NOT_REACHED');assert.equal(result.methods[0].failedActions,1);
});
test('extra original configuration failures are separated from the sixteen product subjects',()=>{
  const result=verifyUiE2eAudit(fixture({configurationFailure:true}));
  assert.equal(result.nativeTotals.total,17);assert.equal(result.productTotals.total,16);
  assert.equal(result.configurationCases[0].outcome,'FAIL');
});
test('engine, target, explicit screenshot policy and selected source owners cannot drift',()=>{
  for(const edit of [
    source=>source.args.push('-Dui.engine=selenium'),
    source=>source.args[source.args.indexOf('-Dbase.url=https://application.example.test')]='-Dbase.url=https://other.example.test',
    source=>source.screenshotPolicy.captureFinalState=false,
    source=>delete source.harnessSha256,
    source=>delete source.manifest['atf-tests/src/test/java/com/ai/ui/support/validation/RegistrationValidation.java']
  ])rejected(sourceChange(fixture(),edit),'E2E-SOURCE');
  rejected(sourceChange(fixture(),source=>source.selectedClasses=['com.ai.atf.governance.DocumentationGovernanceTest']),'E2E-SUBJECT');
});
test('capture identities and exact original HTML/XML/source bytes are mandatory',()=>{
  for(const field of ['sourceManifestSha256','surefireXmlSha256','htmlSha256','sourceCommit','uiEngine','baseUrl','runId']){
    const value=fixture();value.capture[field]='mismatched identity';rejected(value,'E2E-ID');
  }
});
test('XML outcome and counters cannot be relabeled by matching dashboard totals alone',()=>{
  const value=fixture({statuses:{[first]:'FAIL'}});value.capture.methods[0].status='PASS';rejected(value,'E2E-VERDICT');
  const wrong=fixture();wrong.surefireXml=wrong.surefireXml.replace('tests="16"','tests="17"');rejected(rebind(wrong),'E2E-XML');
});
test('runtime HTML counters and required report markers remain independent of capture claims',()=>{
  const value=fixture();value.extentHtml=value.extentHtml.replace('passParent:16','passParent:15');rejected(rebind(value),'E2E-VERDICT');
  const mode=fixture();mode.extentHtml=mode.extentHtml.replace('data-atf-runtime-audit="true"','data-atf-runtime-audit="false"');rejected(rebind(mode),'E2E-DOM');
  const nested=fixture();nested.extentHtml=nested.extentHtml.replace('passEvents:0','passEvents:16');rejected(rebind(nested),'E2E-VERDICT');
});
test('captured navigation evidence must identify the declared application instead of trusting manifest text alone',()=>{
  rejected(fixture({navigationTarget:'https://different.example.test'}),'E2E-ID');
  rejected(fixture({omitNavigation:true}),'E2E-ID');
  const statuses=Object.fromEntries(retainedUiE2eInventory.map(entry=>[subject(entry),'SKIP']));
  const result=verifyUiE2eAudit(fixture({statuses,omitNavigation:true}));
  assert.equal(result.navigationEvidence.state,'NOT_OBSERVED');
  assert.ok(result.classSetup.every(entry=>entry.state==='NOT_REACHED'));
});
test('class setup missing from both original HTML and capture cannot be hidden by successful product bodies',()=>{
  for(const className of ['com.ai.ui.tests.AccountAuthenticatedAreaTests','com.ai.ui.tests.ProfileContactAddressMemoryTests']){
    rejected(fixture({omitSetupFor:className}),'E2E-SETUP');
  }
});
test('class setup must remain exactly once under the first native serialized method rather than capture inventory order',()=>{
  const className='com.ai.ui.tests.AccountAuthenticatedAreaTests';
  rejected(fixture({duplicateSetupFor:className}),'E2E-SETUP');
  rejected(fixture({lateSetupFor:className}),'E2E-SETUP');
  rejected(fixture({wrongSetupParentFor:className}),'E2E-SETUP');
});
test('registration login and profile precondition observations cannot be borrowed from test-body actions',()=>{
  const className='com.ai.ui.tests.ProfileContactAddressMemoryTests';
  rejected(fixture({setupOutside:className}),'E2E-SETUP');
  rejected(fixture({reorderedSetupFor:className}),'E2E-SETUP');
});
test('each meaningful setup phase remains independently required without demanding a fixed action count',()=>{
  const className='com.ai.ui.tests.AccountAuthenticatedAreaTests';
  for(const phase of ['registration navigation','registration submission','login password action','login submission','profile navigation']){
    rejected(fixture({missingSetupPhase:{className,phase}}),'E2E-SETUP');
  }
});
test('duplicate, missing and unrelated native subjects cannot replace the retained inventory',()=>{
  const missing=fixture();missing.capture.methods.pop();rejected(missing,'E2E-SUBJECT');
  const duplicate=fixture();duplicate.capture.methods[1]=structuredClone(duplicate.capture.methods[0]);rejected(duplicate,'E2E-SUBJECT');
  const unrelated=fixture();unrelated.capture.methods[0].className='com.ai.atf.ui.component.UiEngineActionParityContractTest';rejected(unrelated,'E2E-SUBJECT');
  rejected(sourceChange(fixture(),source=>source.expectedProductMethods[retainedUiE2eInventory[0].className].pop()),'E2E-SUBJECT');
});
test('class collisions, borrowed method nodes and invented native IDs cannot prove ownership',()=>{
  const collision=fixture();collision.capture.methods[0].classNodeId=collision.capture.methods[6].classNodeId;rejected(collision,'E2E-DOM');
  const borrowed=fixture();borrowed.capture.methods[1].nodeId=borrowed.capture.methods[0].nodeId;rejected(borrowed,'E2E-DOM');
  const invented=fixture();invented.capture.methods[0].nodeId='unrelated-dashboard-id';rejected(invented,'E2E-DOM');
});
test('all captured step hierarchy and original own-node details must resolve back to HTML',()=>{
  const parent=fixture();parent.capture.methods[0].steps[0].parentId='unrelated';rejected(parent,'E2E-DOM');
  const missing=fixture();missing.capture.methods[0].steps.pop();rejected(missing,'E2E-DOM');
  const invented=fixture();invented.capture.methods[0].steps[0].details.push('expected=fabricated acceptance');rejected(invented,'E2E-DOM');
  const verdict=fixture();verdict.capture.methods[0].steps[0].status='FAIL';rejected(verdict,'E2E-DOM');
});
test('action PASS and a default passing validation node are not business acceptance',()=>{
  rejected(fixture({omitValidations:first}),'E2E-VALIDATION');
  rejected(fixture({omitTerminal:first}),'E2E-VALIDATION');
  rejected(fixture({wrongTerminal:first}),'E2E-VALIDATION');
});
test('each passing business validation needs independent expected and actual observations',()=>{
  rejected(fixture({omitExpected:first}),'E2E-VALIDATION');
  rejected(fixture({omitActual:first}),'E2E-VALIDATION');
});
test('unreached validation is allowed only with its actual native nonpassing outcome and reason',()=>{
  const passing=fixture();passing.capture.methods[0].validationState='NOT_REACHED';passing.capture.methods[0].notReachedReason='FAILED_BEFORE_VALIDATION';rejected(passing,'E2E-VALIDATION');
  const skipped=fixture({statuses:{[first]:'SKIP'}});delete skipped.capture.methods[0].notReachedReason;rejected(skipped,'E2E-VALIDATION');
});
test('passing final-state screenshots are real method-owned PNG containers rather than labels',()=>{
  rejected(fixture({omitScreenshot:first}),'E2E-SCREENSHOT');
  rejected(fixture({corruptPng:first}),'E2E-SCREENSHOT');
  rejected(fixture({wrongImageLabel:first}),'E2E-SCREENSHOT');
  const borrowed=fixture();borrowed.capture.methods[0].screenshots=structuredClone(borrowed.capture.methods[1].screenshots);rejected(borrowed,'E2E-SCREENSHOT');
});
test('typed credential values are redacted and failure diagnostics never print leaked values',()=>{
  const secret='component-only-private-value',value=fixture({secretValue:secret});
  assert.throws(()=>verifyUiE2eAudit(value),error=>error.message.includes('E2E-SECRET')&&!error.message.includes(secret));
});
test('native XML supports quoted attributes, preserved CDATA and explicit error counters but not DTDs',()=>{
  const value=fixture({statuses:{[first]:'FAIL'},configurationFailure:true}),parsed=readSurefireUiResults(value.surefireXml);
  assert.ok(parsed.cases[0].explanation.includes('<preserved>'));assert.equal(parsed.nativeCounters.errors,1);
  const externalEntity='<!DOCTYPE testsuite [<!ENTITY unsafe SYSTEM "'+escape(import.meta.url)+'">]>';
  assert.throws(()=>readSurefireUiResults(value.surefireXml.replace('<?xml version="1.0" encoding="UTF-8"?>',externalEntity)),/E2E-XML/);
  assert.throws(()=>readSurefireUiResults(value.surefireXml.replace('</testsuite>','</different>')),/E2E-XML/);
});
test('malformed native capture shapes fail with constructive owner guidance rather than incidental exceptions',()=>{
  for(const [edit,rule]of [
    [value=>value.capture.methods[0]=null,'E2E-SUBJECT'],
    [value=>value.capture.methods[0].steps[0]=null,'E2E-DOM'],
    [value=>value.capture.methods[0].steps[0].details=[null],'E2E-DOM'],
    [value=>value.capture.methods[0].screenshots=[null],'E2E-SCREENSHOT'],
    [value=>value.capture.configurationFailures=[null],'E2E-SUBJECT'],
    [value=>value.surefireXml=null,'E2E-SOURCE']
  ]){const value=fixture();edit(value);rejected(value,rule);}
});
test('XML status tags and testcases cannot be hidden inside unrelated nested elements',()=>{
  const direct=fixture().surefireXml;
  const nestedCase=direct.replace('<testcase ','<properties><testcase ').replace('</testcase>','</testcase></properties>');
  assert.throws(()=>readSurefireUiResults(nestedCase),/E2E-XML/);
  const failed=fixture({statuses:{[first]:'FAIL'}}).surefireXml;
  assert.throws(()=>readSurefireUiResults(failed.replace('<failure ','<system-out><failure ').replace('</failure>','</failure></system-out>')),/E2E-XML/);
});
test('CLI reads space-containing input paths without changing the original evidence',t=>{
  const directory=fs.mkdtempSync(path.join(os.tmpdir(),'atf-ui-e2e audit-'));
  t.after(()=>{assert.ok(fs.realpathSync(directory).startsWith(fs.realpathSync(os.tmpdir())+path.sep));assert.ok(path.basename(directory).startsWith('atf-ui-e2e audit-'));fs.rmSync(directory,{recursive:true});});
  const value=fixture(),inputs={source:value.sourceManifestText,xml:value.surefireXml,html:value.extentHtml,capture:JSON.stringify(value.capture)};
  const args=[],before={};
  for(const [kind,text]of Object.entries(inputs)){const file=path.join(directory,kind+' native.txt');fs.writeFileSync(file,text);args.push('--'+kind,file);before[file]={sha:sha(fs.readFileSync(file)),mtime:fs.statSync(file).mtimeMs};}
  const result=spawnSync(process.execPath,[fileURLToPath(new URL('./verify-ui-e2e-audit.mjs',import.meta.url)),...args],{encoding:'utf8',windowsHide:true,timeout:30000});
  assert.equal(result.status,0,result.stderr);assert.equal(JSON.parse(result.stdout).productTotals.total,16);
  for(const [file,expected]of Object.entries(before)){assert.equal(sha(fs.readFileSync(file)),expected.sha);assert.equal(fs.statSync(file).mtimeMs,expected.mtime);}
  const invalid=spawnSync(process.execPath,[fileURLToPath(new URL('./verify-ui-e2e-audit.mjs',import.meta.url)),'--output',path.join(directory,'not-created.json')],{encoding:'utf8',windowsHide:true,timeout:30000});
  assert.equal(invalid.status,1);assert.match(invalid.stderr,/Repair hint:/);assert.equal(fs.existsSync(path.join(directory,'not-created.json')),false);
});

test('actual audit failures resolve to a live owner and a runnable four-input recheck',t=>{
  const root=path.resolve(fileURLToPath(new URL('../',import.meta.url)));
  const script=fileURLToPath(new URL('./verify-ui-e2e-audit.mjs',import.meta.url));
  const failed=spawnSync(process.execPath,[script],{encoding:'utf8',windowsHide:true,timeout:30000});
  assert.equal(failed.status,1,failed.stderr);assert.match(failed.stderr,/^UI-E2E-AUDIT E2E-SOURCE\r?\n/);
  const owners=[...failed.stderr.matchAll(/^Owner: (.+)$/gm)],rechecks=[...failed.stderr.matchAll(/^Recheck: (.+)$/gm)];
  assert.equal(owners.length,1,'An emitted failure needs one canonical owner.');
  assert.equal(rechecks.length,1,'An emitted failure needs one executable recheck.');
  const owner=owners[0][1].trim(),parts=owner.split('#');
  assert.equal(parts.length,2,'The owner must include its documentation fragment.');
  const [relative,encodedFragment]=parts,fragment=decodeURIComponent(encodedFragment),file=path.resolve(root,relative);
  assert.ok(!path.isAbsolute(relative)&&file.startsWith(root+path.sep)&&relative.endsWith('.md'));
  assert.ok(fs.statSync(file).isFile(),'The emitted owner must name an existing document.');
  const assertOwnerFragment=markdown=>{
    const visible=[];let fence=null;
    for(const line of markdown.split(/\r?\n/)){
      const marker=/^ {0,3}(`{3,}|~{3,})(.*)$/.exec(line);
      if(fence){
        if(marker&&marker[1][0]===fence[0]&&marker[1].length>=fence.length&&!marker[2].trim())fence=null;
      }else if(marker)fence=marker[1];
      else visible.push(line);
    }
    const prose=visible.join('\n'),anchors=new Set();
    for(const match of prose.matchAll(/^\s*<a\b[^>]*\s(?:id|name)=["']([^"']+)["'][^>]*>\s*<\/a>\s*$/gmi))anchors.add(match[1]);
    for(const line of visible){
      const heading=/^ {0,3}#{1,6}\s+(.+?)\s*#*\s*$/.exec(line);
      if(heading)anchors.add(heading[1].toLowerCase().replace(/[^\p{L}\p{N}_\-\s]/gu,'').replace(/\s/g,'-'));
    }
    assert.ok(anchors.has(fragment),'Missing emitted owner fragment: '+owner);
  };
  assertOwnerFragment(fs.readFileSync(file,'utf8'));
  assertOwnerFragment('# Component owner\n\n<a id="'+fragment+'"></a>\n');
  assertOwnerFragment('# '+fragment.replaceAll('-',' ')+'\n');
  for(const markdown of [
    '# Component owner\n\nThe original anchor is missing.\n',
    '# Component owner\n\n<a id="'+fragment+'-moved"></a>\n',
    '# Component owner\n\n````markdown\n<a id="'+fragment+'"></a>\n```\n## '+fragment+'\n````\n',
    '# Component owner\n\n~~~markdown\n<a id="'+fragment+'"></a>\n```\n## '+fragment+'\n~~~\n'
  ])assert.throws(()=>assertOwnerFragment(markdown),/Missing emitted owner fragment:/);
  const command=rechecks[0][1].trim().split(/\s+/);
  assert.equal(command.length,10,'The recheck names Node, its tool and four input pairs.');
  assert.equal(command[0],'node');assert.equal(path.resolve(root,command[1]),script);
  assert.ok(fs.statSync(path.resolve(root,command[1])).isFile());
  const flags=command.filter((_,index)=>index>=2&&index%2===0);
  assert.deepEqual(flags.slice().sort(),['--capture','--html','--source','--xml']);
  for(let index=3;index<command.length;index+=2)assert.match(command[index],/^<[^<>]+>$/);
  const directory=fs.mkdtempSync(path.join(os.tmpdir(),'atf-ui-audit-guidance-'));
  t.after(()=>{assert.ok(fs.realpathSync(directory).startsWith(fs.realpathSync(os.tmpdir())+path.sep));assert.ok(path.basename(directory).startsWith('atf-ui-audit-guidance-'));fs.rmSync(directory,{recursive:true});});
  const value=fixture(),inputs={'--source':value.sourceManifestText,'--xml':value.surefireXml,'--html':value.extentHtml,'--capture':JSON.stringify(value.capture)};
  const args=[path.resolve(root,command[1])];
  for(const flag of flags){const input=path.join(directory,flag.slice(2)+' native.txt');fs.writeFileSync(input,inputs[flag]);args.push(flag,input);}
  const checked=spawnSync(process.execPath,args,{cwd:root,encoding:'utf8',windowsHide:true,timeout:30000});
  assert.equal(checked.status,0,checked.stderr);
  const result=JSON.parse(checked.stdout);assert.equal(result.status,'PASSED');
  assert.deepEqual(result.productTotals,{total:16,passed:16,failed:0,skipped:0});
});
