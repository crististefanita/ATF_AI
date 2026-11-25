import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {fileURLToPath} from 'node:url';

const registration = 'com.ai.atf.ui.RegistrationTests';
const account = 'com.ai.ui.tests.AccountAuthenticatedAreaTests';
const profile = 'com.ai.ui.tests.ProfileContactAddressMemoryTests';
const item = (className, methodName, validations) => Object.freeze({className, methodName, validations:Object.freeze(validations)});
const contact = ['Validate profile phone', 'Validate profile street'];
const address = ['Validate profile postal code', 'Validate profile city', 'Validate profile state', 'Validate profile country'];
export const retainedUiE2eInventory = Object.freeze([
  item(registration, 'positive_registration_redirects_to_login', ['Validate registration redirects to login']),
  item(registration, 'negative_missing_required_fields_shows_error', ['Validate registration form remains open', 'Validate registration error is present']),
  item(registration, 'negative_invalid_email_should_fail', ['Validate registration form remains open', 'Validate Any error message container']),
  item(registration, 'negative_duplicate_email_should_fail', ['Validate registration redirects to login', 'Validate registration form remains open', 'Validate duplicate email is rejected']),
  item(registration, 'negative_weak_password_should_show_validation_message', ['Validate registration form remains open', 'Validate Password validation message']),
  item(registration, 'negative_phone_with_letters_should_fail', ['Validate registration form remains open', 'Validate Phone validation message']),
  item(account, 'accountOverviewIsVisibleAfterLogin', ['Validate account page title']),
  item(account, 'accountProfileAreaIsReachable', ['Validate profile page title']),
  item(account, 'accountFavoritesAreaIsReachable', ['Validate account area page title']),
  item(account, 'accountInvoicesAreaIsReachable', ['Validate account area page title']),
  item(account, 'accountMessagesAreaIsReachable', ['Validate account area page title']),
  item(profile, 'profileKeepsRegisteredPhone', contact),
  item(profile, 'profileKeepsRegisteredStreet', contact),
  item(profile, 'profileKeepsRegisteredPostalCode', address),
  item(profile, 'profileKeepsRegisteredCityAndState', address),
  item(profile, 'profileKeepsRegisteredCountryCode', address)
]);
const key = value => value.className + '#' + value.methodName;
const digest = value => createHash('sha256').update(value).digest('hex');
const object = value => value !== null && typeof value === 'object' && !Array.isArray(value);
const norm = value => String(value ?? '').replace(/\s+/g, ' ').trim();
const title = value => norm(value).replace(/[▶▼►▸▾]/g, '').trim();
const same = (left, right) => JSON.stringify(left) === JSON.stringify(right);
const shaPattern = /^[a-f0-9]{64}$/;
const revisionPattern = /^[a-f0-9]{40}$/i;
function requireAudit(condition, rule, location, meaning) {
  if (!condition) throw new Error('UI-E2E-AUDIT ' + rule + '\nObserved: ' + location
    + '\nFailure meaning: ' + meaning
    + '\nRepair hint: Preserve original native results; repair the owned source, capture or reporting path, then recapture and reconcile the same retained subjects.'
    + '\nOwner: docs/Testing.md#existing-product-e2e-audit'
    + '\nRecheck: node tools/verify-ui-e2e-audit.mjs --source <source.json> --xml <TEST-TestSuite.xml> --html <extent.html> --capture <native-dom.json>');
}
function decode(value, xml = false) {
  return value.replace(/&(#x[0-9a-f]+|#\d+|[a-z][a-z0-9]+);/gi, (entity, name) => {
    const fixed = {amp:'&',lt:'<',gt:'>',quot:'"',apos:"'",nbsp:' ',middot:'·'};
    if (name.startsWith('#')) {
      const code = name[1].toLowerCase() === 'x' ? parseInt(name.slice(2),16) : Number(name.slice(1));
      requireAudit(Number.isInteger(code) && code > 0 && code <= 0x10ffff && !(code >= 0xd800 && code <= 0xdfff), 'E2E-XML', 'entity', 'Invalid numeric character reference.');
      return String.fromCodePoint(code);
    }
    if (Object.hasOwn(fixed,name.toLowerCase()) && (!xml || !['nbsp','middot'].includes(name.toLowerCase()))) return fixed[name.toLowerCase()];
    requireAudit(!xml, 'E2E-XML', 'entity', 'Custom XML entities are not accepted.');
    return entity;
  });
}
function markupEnd(text, start) {
  let quote = null;
  for (let index = start + 1; index < text.length; index++) {
    const char = text[index];
    if (quote) { if (char === quote) quote = null; }
    else if (char === '"' || char === "'") quote = char;
    else if (char === '>') return index + 1;
  }
  requireAudit(false, 'E2E-XML', 'markup', 'Unterminated markup.');
}
function attributes(source, xml) {
  const values = {};
  let cursor = 0;
  while (cursor < source.length) {
    const whitespace = /^\s+/.exec(source.slice(cursor));
    if (whitespace) cursor += whitespace[0].length;
    if (cursor === source.length) break;
    const name = /^[A-Za-z_:][A-Za-z0-9_.:-]*/.exec(source.slice(cursor));
    requireAudit(name, 'E2E-XML', 'attribute', 'Malformed attribute name.');
    cursor += name[0].length;
    const attr = xml ? name[0] : name[0].toLowerCase();
    requireAudit(!Object.hasOwn(values,attr), 'E2E-XML', 'attribute:' + attr, 'Duplicate attributes are ambiguous.');
    const gap = /^\s*/.exec(source.slice(cursor))[0]; cursor += gap.length;
    if (source[cursor] !== '=') {
      requireAudit(!xml, 'E2E-XML', 'attribute:' + attr, 'XML attributes require a quoted value.');
      values[attr] = ''; continue;
    }
    cursor++;
    cursor += /^\s*/.exec(source.slice(cursor))[0].length;
    const quote = source[cursor];
    if (quote === '"' || quote === "'") {
      const end = source.indexOf(quote,cursor+1);
      requireAudit(end >= 0, 'E2E-XML', 'attribute:' + attr, 'Unterminated attribute value.');
      values[attr] = decode(source.slice(cursor+1,end),xml); cursor = end+1;
    } else {
      requireAudit(!xml, 'E2E-XML', 'attribute:' + attr, 'XML attribute values must be quoted.');
      const value = /^[^\s>]+/.exec(source.slice(cursor));
      requireAudit(value, 'E2E-DOM', 'attribute:' + attr, 'Missing HTML attribute value.');
      values[attr] = decode(value[0]); cursor += value[0].length;
    }
  }
  return values;
}
/** Read native Surefire counters and subjects; no DTD, external entity or test execution. */
export function readSurefireUiResults(xml) {
  requireAudit(typeof xml === 'string' && xml.length > 0 && xml.length <= 64*1024*1024, 'E2E-XML', 'xml', 'Supply bounded original Surefire XML.');
  const stack = [], cases = [];
  let cursor = xml.charCodeAt(0) === 0xfeff ? 1 : 0, suite = null, active = null, rootClosed = false;
  while (cursor < xml.length) {
    if (xml[cursor] !== '<') {
      const next = xml.indexOf('<',cursor), end = next < 0 ? xml.length : next;
      const text = decode(xml.slice(cursor,end),true);
      if (active && stack.some(name => ['failure','error','skipped'].includes(name))) active.explanation += text;
      requireAudit(stack.length > 0 || !text.trim(), 'E2E-XML', 'outside-root', 'Unexpected text outside XML root.');
      cursor = end; continue;
    }
    if (xml.startsWith('<!--',cursor)) {
      const end = xml.indexOf('-->',cursor+4); requireAudit(end >= 0,'E2E-XML','comment','Unterminated XML comment.'); cursor = end+3; continue;
    }
    if (xml.startsWith('<![CDATA[',cursor)) {
      const end = xml.indexOf(']]>',cursor+9); requireAudit(end >= 0 && stack.length > 0,'E2E-XML','CDATA','Malformed CDATA.');
      if (active && stack.some(name => ['failure','error','skipped'].includes(name))) active.explanation += xml.slice(cursor+9,end);
      cursor = end+3; continue;
    }
    if (xml.startsWith('<?',cursor)) {
      const end = xml.indexOf('?>',cursor+2); requireAudit(end >= 0 && !suite,'E2E-XML','declaration','Unexpected processing instruction.'); cursor = end+2; continue;
    }
    requireAudit(!xml.startsWith('<!',cursor),'E2E-XML','declaration','DTD and entity declarations are forbidden.');
    const end = markupEnd(xml,cursor), token = xml.slice(cursor+1,end-1).trim(); cursor = end;
    if (token.startsWith('/')) {
      const name = token.slice(1).trim();
      requireAudit(stack.pop() === name,'E2E-XML',name,'Unbalanced XML element.');
      if (name === 'testcase') { cases.push(active); active = null; }
      if (name === 'testsuite' && stack.length === 0) rootClosed = true;
      continue;
    }
    const closing = token.endsWith('/'), contents = closing ? token.slice(0,-1).trimEnd() : token;
    const match = /^([A-Za-z_:][A-Za-z0-9_.:-]*)([\s\S]*)$/.exec(contents);
    requireAudit(match,'E2E-XML','element','Malformed XML element.');
    const name = match[1], attrs = attributes(match[2],true);
    if (!stack.length) {
      requireAudit(!suite && !rootClosed && name === 'testsuite','E2E-XML',name,'Expected exactly one native testsuite root.');
      const integer = field => {
        requireAudit(/^\d+$/.test(attrs[field] || '') && Number.isSafeInteger(Number(attrs[field])),'E2E-XML','testsuite@'+field,'Missing or invalid native suite counter.');
        return Number(attrs[field]);
      };
      suite = {total:integer('tests'),failed:integer('failures'),errors:integer('errors'),skipped:integer('skipped')};
    }
    if (name === 'testcase') {
      requireAudit(!active && attrs.classname && attrs.name && stack.length===1 && stack[0]==='testsuite','E2E-XML','testcase','A native testcase needs classname/name directly below its suite and cannot be nested.');
      active = {className:attrs.classname,methodName:attrs.name,status:'PASS',explanation:'',failureKind:null};
    }
    if (active && ['failure','error','skipped'].includes(name)) {
      requireAudit(active.failureKind === null && stack.at(-1)==='testcase','E2E-XML',key(active),'A testcase needs one direct native outcome marker, without nested or ambiguous outcomes.');
      active.failureKind = name; active.status = name === 'skipped' ? 'SKIP' : 'FAIL';
      active.explanation += (attrs.message || '') + '\n';
    }
    if (!closing) stack.push(name);
    else if (name === 'testcase') { cases.push(active); active = null; }
    else if (name === 'testsuite') rootClosed = true;
  }
  requireAudit(suite && rootClosed && !stack.length && !active,'E2E-XML','document','Incomplete native XML.');
  const found = new Set();
  for (const entry of cases) {
    requireAudit(!found.has(key(entry)),'E2E-SUBJECT',key(entry),'Duplicate native testcase subject.'); found.add(key(entry));
  }
  const failed = cases.filter(entry => entry.failureKind === 'failure').length;
  const errors = cases.filter(entry => entry.failureKind === 'error').length;
  const skipped = cases.filter(entry => entry.status === 'SKIP').length;
  requireAudit(suite.total === cases.length && suite.failed === failed && suite.errors === errors && suite.skipped === skipped,'E2E-XML','testsuite','Native counters do not match their testcase outcomes.');
  return {cases,totals:{total:cases.length,passed:cases.length-failed-errors-skipped,failed:failed+errors,skipped},nativeCounters:suite};
}
const hasClass = (node, value) => (node.attrs?.class || '').split(/\s+/).includes(value);
function nativeHtmlIndex(html) {
  const root = {tag:'#root',attrs:{},children:[],parent:null}, stack = [root], ids = new Map(), testIds = new Map(), all = [];
  const voids = new Set(['area','base','br','col','embed','hr','img','input','link','meta','param','source','track','wbr']);
  const append = node => { node.parent = stack.at(-1); node.parent.children.push(node); };
  let cursor = 0;
  while (cursor < html.length) {
    if (html[cursor] !== '<') {
      const next = html.indexOf('<',cursor), end = next < 0 ? html.length : next;
      append({tag:'#text',text:decode(html.slice(cursor,end)),children:[]}); cursor = end; continue;
    }
    if (html.startsWith('<!--',cursor)) { const end = html.indexOf('-->',cursor+4); requireAudit(end >= 0,'E2E-DOM','comment','Malformed native HTML.'); cursor=end+3;continue; }
    const end = markupEnd(html,cursor), token = html.slice(cursor+1,end-1).trim(); cursor=end;
    if (/^!doctype\b/i.test(token)) continue;
    if (token.startsWith('/')) {
      const name = token.slice(1).trim().toLowerCase();
      let index = stack.length-1; while (index > 0 && stack[index].tag !== name) index--;
      if (index > 0) stack.length = index;
      continue;
    }
    const match = /^([A-Za-z][A-Za-z0-9:-]*)([\s\S]*?)\/?$/.exec(token);
    requireAudit(match,'E2E-DOM','markup','Native HTML contains unsupported markup.');
    const node = {tag:match[1].toLowerCase(),attrs:attributes(match[2],false),children:[]}; append(node); all.push(node);
    if (node.attrs.id) { const values=ids.get(node.attrs.id)||[];values.push(node);ids.set(node.attrs.id,values); }
    if (node.attrs['test-id']) {const values=testIds.get(node.attrs['test-id'])||[];values.push(node);testIds.set(node.attrs['test-id'],values);}
    if (['script','style','textarea'].includes(node.tag)) {
      const regex = new RegExp('</'+node.tag+'\\s*>','ig');regex.lastIndex=cursor;const close=regex.exec(html);
      requireAudit(close,'E2E-DOM',node.tag,'Unclosed native raw-text element.');
      node.children.push({tag:'#text',text:node.tag==='textarea'?decode(html.slice(cursor,close.index)):'',children:[],parent:node});
      if(node.tag==='script')node.rawScript=html.slice(cursor,close.index);
      cursor=regex.lastIndex;continue;
    }
    if (!voids.has(node.tag) && !token.endsWith('/')) stack.push(node);
  }
  return {root,ids,testIds,all};
}
function descendants(node, predicate = () => true, excludeCards = false) {
  const result=[], pending=[...node.children].reverse();
  while(pending.length){const current=pending.pop();if(excludeCards&&hasClass(current,'card'))continue;if(predicate(current))result.push(current);for(let index=current.children.length-1;index>=0;index--)pending.push(current.children[index]);}
  return result;
}
const textOf = node => norm(descendants({children:[node]},part=>part.tag==='#text').map(part=>part.text).join(' '));
const ancestor = (node,predicate) => {for(let cursor=node;cursor;cursor=cursor.parent)if(predicate(cursor))return cursor;return null;};
function nodeBinding(index, id, location) {
  requireAudit(typeof id==='string'&&id.length>0,'E2E-DOM',location,'Native node ID is required.');
  const matches=index.ids.get(id)||[];
  requireAudit(matches.length===1,'E2E-DOM',location,'Node ID must identify one original native HTML element.');
  const anchor=matches[0],card=ancestor(anchor,node=>hasClass(node,'card'));
  requireAudit(anchor.tag==='a'&&hasClass(anchor,'node')&&card,'E2E-DOM',location,'Bind the original Spark a.node ID, not an invented or dashboard identifier.');
  const header=ancestor(anchor,node=>hasClass(node,'card-header'));
  requireAudit(header&&header.parent===card,'E2E-DOM',location,'The node must belong to its direct native card header.');
  const badges=descendants(header,node=>hasClass(node,'badge')&&hasClass(node,'log'));
  requireAudit(badges.length===1,'E2E-DOM',location,'One direct native card status badge is required.');
  return {anchor,card,title:title(textOf(anchor)),status:outcome(textOf(badges[0]),true),classNode:ancestor(card,node=>node.tag==='li'&&hasClass(node,'test-item'))};
}
function ownLogs(card) {
  return descendants(card,node=>node.tag==='tr'&&hasClass(node,'event-row'),true).map(row=>{
    const cells=row.children.filter(node=>node.tag==='td');
    requireAudit(cells.length===3,'E2E-DOM','event-row','Native log rows must expose status, timestamp and details.');
    return {status:outcome(textOf(cells[0]),true),text:textOf(cells[2])};
  });
}
function outcome(value, step=false) {
  const result={PASS:'PASS',PASSED:'PASS',FAIL:'FAIL',FAILED:'FAIL',ERROR:'FAIL',SKIP:'SKIP',SKIPPED:'SKIP',INFO:'INFO',WARNING:'WARNING'}[norm(value).toUpperCase()];
  requireAudit(result&&(step||['PASS','FAIL','SKIP'].includes(result)),'E2E-VERDICT','status','Unknown native outcome.');return result;
}
const crcTable = Array.from({length:256},(_,index)=>{let value=index;for(let bit=0;bit<8;bit++)value=value&1?0xedb88320^(value>>>1):value>>>1;return value>>>0;});
const crc32=bytes=>{let value=0xffffffff;for(const byte of bytes)value=crcTable[(value^byte)&255]^(value>>>8);return(value^0xffffffff)>>>0;};
function pngMetadata(uri) {
  requireAudit(/^data:image\/png;base64,[A-Za-z0-9+/]+={0,2}$/.test(uri),'E2E-SCREENSHOT','PNG','Expected an embedded native PNG screenshot.');
  const encoded=uri.slice(uri.indexOf(',')+1),bytes=Buffer.from(encoded,'base64');
  requireAudit(bytes.toString('base64')===encoded&&bytes.length>=45&&bytes.subarray(0,8).equals(Buffer.from([137,80,78,71,13,10,26,10])),'E2E-SCREENSHOT','PNG','Invalid screenshot encoding/signature.');
  let cursor=8,width=0,height=0,header=false,data=false,end=false;
  while(cursor<bytes.length){
    requireAudit(cursor+12<=bytes.length,'E2E-SCREENSHOT','PNG chunk','Truncated PNG chunk.');
    const length=bytes.readUInt32BE(cursor),type=bytes.toString('ascii',cursor+4,cursor+8),next=cursor+length+12;
    requireAudit(next<=bytes.length&&crc32(bytes.subarray(cursor+4,cursor+8+length))===bytes.readUInt32BE(cursor+8+length),'E2E-SCREENSHOT','PNG chunk','Invalid PNG chunk boundary or CRC.');
    if(!header){requireAudit(type==='IHDR'&&length===13,'E2E-SCREENSHOT','IHDR','PNG must start with its image header.');width=bytes.readUInt32BE(cursor+8);height=bytes.readUInt32BE(cursor+12);header=true;}
    else requireAudit(type!=='IHDR','E2E-SCREENSHOT','IHDR','Duplicate PNG header.');
    if(type==='IDAT')data=true;
    if(type==='IEND'){requireAudit(length===0&&next===bytes.length,'E2E-SCREENSHOT','IEND','PNG must end exactly after IEND.');end=true;}
    cursor=next;
  }
  requireAudit(header&&data&&end&&width>0&&height>0,'E2E-SCREENSHOT','PNG','Screenshot must contain a nonempty PNG image.');
  return {sha256:digest(bytes),mimeType:'image/png',bytes:bytes.length,width,height};
}
function expectedMap() {
  const result={};for(const entry of retainedUiE2eInventory)(result[entry.className]||=[]).push(entry.methodName);
  return result;
}
const normalizedMap=value=>Object.entries(value).sort(([left],[right])=>left.localeCompare(right)).map(([name,methods])=>[name,Array.isArray(methods)?methods.slice().sort():methods]);
function nativeConfiguration(entry) {
  const regular=[registration,account,profile].includes(entry.className)&&['setUp','tearDown'].includes(entry.methodName);
  return regular||entry.className==='com.ai.ui.core.runtime.BaseTestUI'&&['setUp','tearDown'].includes(entry.methodName)
    ||entry.className===registration&&entry.methodName==='before'
    ||[account,profile].includes(entry.className)&&entry.methodName==='createCustomerProfile';
}
/** Bounded consistency of declared native evidence; caller provenance is not execution attestation. */
export function verifyUiE2eAudit({sourceManifestText,surefireXml,extentHtml,capture}) {
  requireAudit(typeof sourceManifestText==='string'&&typeof surefireXml==='string'&&typeof extentHtml==='string'&&extentHtml.length>0&&extentHtml.length<=128*1024*1024,'E2E-SOURCE','inputs','Supply original source-manifest, native Surefire XML and native Extent HTML text.');
  let source;try{source=JSON.parse(sourceManifestText);}catch{requireAudit(false,'E2E-SOURCE','source manifest','Invalid original JSON.');}
  requireAudit(object(source)&&source.schemaVersion===1&&source.role==='RETAINED_PRODUCT_E2E_EXECUTION','E2E-SOURCE','source manifest','Use the original retained-product execution manifest.');
  requireAudit(revisionPattern.test(source.sourceCommit||'')&&typeof source.runId==='string'&&/^[A-Za-z0-9][A-Za-z0-9._-]*$/.test(source.runId),'E2E-SOURCE','source identity','A full captured revision and portable run ID are required.');
  requireAudit(['playwright','selenium'].includes(source.engine),'E2E-SOURCE','engine','Declare the actual selected browser adapter.');
  let target;try{target=new URL(source.targetUrl);}catch{requireAudit(false,'E2E-SOURCE','targetUrl','Explicit application URL is required.');}
  requireAudit(['http:','https:'].includes(target.protocol)&&!target.username&&!target.password,'E2E-SOURCE','targetUrl','Target must be an HTTP(S) application URL without embedded credentials.');
  requireAudit(Array.isArray(source.args)&&source.args.every(arg=>typeof arg==='string'),'E2E-SOURCE','args','Original invocation arguments are required.');
  const argument=(name,expected)=>{const values=source.args.filter(arg=>arg.startsWith('-D'+name+'='));requireAudit(values.length===1&&values[0]==='-D'+name+'='+expected,'E2E-SOURCE','args:'+name,'Invocation and declared runtime settings must agree exactly once.');};
  argument('ui.engine',source.engine);argument('base.url',source.targetUrl);argument('reporting.engine','extent');
  argument('ui.screenshots.enabled','true');argument('ui.screenshots.on.failure.only','false');argument('ui.screenshots.capture.final.state','true');
  requireAudit(object(source.screenshotPolicy)&&source.screenshotPolicy.enabled===true&&source.screenshotPolicy.failureOnly===false&&source.screenshotPolicy.captureFinalState===true,'E2E-SOURCE','screenshotPolicy','Record the explicit final-state screenshot policy before execution.');
  requireAudit(shaPattern.test(source.harnessSha256||''),'E2E-SOURCE','harnessSha256','The original runner harness needs its source hash.');
  const expected=expectedMap(),classes=Object.keys(expected).sort();
  requireAudit(object(source.expectedProductMethods)&&same(normalizedMap(source.expectedProductMethods),normalizedMap(expected))&&source.expectedProductCases===16,'E2E-SUBJECT','source selection','The exact same sixteen retained subjects must be declared.');
  requireAudit(Array.isArray(source.selectedClasses)&&same(source.selectedClasses.slice().sort(),classes),'E2E-SUBJECT','selectedClasses','Select exactly the three retained product classes.');
  requireAudit(object(source.manifest),'E2E-SOURCE','manifest','Retain the original source-file manifest.');
  for(const file of [...classes.map(name=>'atf-tests/src/test/java/'+name.replaceAll('.','/')+'.java'),
    'atf-tests/src/test/java/com/ai/ui/support/validation/RegistrationValidation.java',
    'atf-tests/src/test/java/com/ai/ui/support/validation/ProfileAccountValidation.java']){
    requireAudit(shaPattern.test(source.manifest[file]||''),'E2E-SOURCE',file,'The executed test and validation owners require captured source hashes.');
  }
  requireAudit(object(capture)&&capture.schemaVersion===1&&capture.captureMode==='NATIVE_CHROMIUM_DOM','E2E-DOM','capture','Use the native document capture contract.');
  for(const [field,actual]of Object.entries({runId:source.runId,sourceCommit:source.sourceCommit,uiEngine:source.engine,baseUrl:source.targetUrl})){
    requireAudit(capture[field]===actual,'E2E-ID',field,'Native capture and original execution identities disagree.');
  }
  for(const [field,actual]of Object.entries({sourceManifestSha256:digest(sourceManifestText),surefireXmlSha256:digest(surefireXml),htmlSha256:digest(extentHtml)})){
    requireAudit(capture[field]===actual,'E2E-ID',field,'Capture must bind the exact original source manifest, XML and HTML bytes.');
  }
  requireAudit(shaPattern.test(capture.probeSha256||'')&&capture.reportMode==='runtime-audited','E2E-DOM','capture identity','Capture the native probe hash and actual runtime-audited report mode.');
  const native=readSurefireUiResults(surefireXml),inventory=new Map(retainedUiE2eInventory.map(entry=>[key(entry),entry]));
  const product=native.cases.filter(entry=>inventory.has(key(entry))),configurations=native.cases.filter(entry=>!inventory.has(key(entry)));
  requireAudit(product.length===16&&product.every(entry=>inventory.has(key(entry))),'E2E-SUBJECT','native XML','Every retained product subject must appear exactly once.');
  requireAudit(configurations.every(nativeConfiguration),'E2E-SUBJECT','native XML','Unrelated tests cannot replace or extend the product acceptance selection.');
  const configurationCapture=capture.configurationFailures||[];
  requireAudit(Array.isArray(configurationCapture)&&configurationCapture.every(entry=>object(entry)&&typeof entry.className==='string'&&typeof entry.methodName==='string')&&same(configurationCapture.map(entry=>[key(entry),outcome(entry.status)]).sort(),configurations.map(entry=>[key(entry),entry.status]).sort()),'E2E-SUBJECT','configurationFailures','Preserve every additional native configuration outcome separately.');
  requireAudit(object(capture.totals)&&same(['total','passed','failed','skipped'].map(field=>capture.totals[field]),['total','passed','failed','skipped'].map(field=>native.totals[field])),'E2E-VERDICT','totals','Native Extent totals must agree with all XML outcomes, including configuration cases.');
  requireAudit(Array.isArray(capture.methods)&&capture.methods.length===16,'E2E-SUBJECT','capture methods','Sixteen native product method records are required.');
  const html=nativeHtmlIndex(extentHtml),methodIds=new Set(),stepIds=new Set(),subjects=new Set(),classBindings=new Map(),observations=[],navigationEvidence=[];
  const mode=html.all.find(node=>node.tag==='body');
  requireAudit(mode?.attrs['data-atf-report-mode']==='runtime-audited'&&mode.attrs['data-atf-runtime-audit']==='true','E2E-DOM','report mode','The original HTML must contain its native runtime-audit markers.');
  const totalNodes=html.all.filter(node=>node.tag==='h3'&&node.attrs['data-atf-test-cases-total']==='true');
  requireAudit(totalNodes.length===1&&textOf(totalNodes[0])===String(native.totals.total),'E2E-VERDICT','HTML total','The original runtime dashboard total must match native XML.');
  const groups=html.all.filter(node=>node.tag==='script').flatMap(node=>[...(node.rawScript||'').matchAll(/\b(?:var|let|const)\s+statusGroup\s*=\s*\{([^}]+)\}/g)].map(match=>match[1]));
  requireAudit(groups.length===1,'E2E-VERDICT','statusGroup','Exactly one original runtime status ledger is required.');
  const ledger={};
  const zeroLedger=['warningParent','childCount','passChild','failChild','warningChild','skipChild','infoChild','grandChildCount','passGrandChild','failGrandChild','warningGrandChild','skipGrandChild','infoGrandChild','eventsCount','passEvents','failEvents','warningEvents','skipEvents','infoEvents'];
  for(const field of ['parentCount','passParent','failParent','skipParent',...zeroLedger]){
    const values=[...groups[0].matchAll(new RegExp('\\b'+field+'\\s*:\\s*(\\d+)\\b','g'))];
    requireAudit(values.length===1,'E2E-VERDICT','statusGroup.'+field,'Runtime ledger counters must be present exactly once.');ledger[field]=Number(values[0][1]);
  }
  requireAudit(ledger.parentCount===native.totals.total&&ledger.passParent===native.totals.passed&&ledger.failParent===native.totals.failed&&ledger.skipParent===native.totals.skipped
    &&zeroLedger.every(field=>ledger[field]===0),'E2E-VERDICT','statusGroup','The original HTML ledger cannot duplicate nested steps or relabel native outcomes.');
  for(const method of capture.methods){
    requireAudit(object(method)&&typeof method.className==='string'&&typeof method.methodName==='string','E2E-SUBJECT','capture method','Each captured method needs a native subject identity.');
    const subject=key(method),expectedMethod=inventory.get(subject),xml=product.find(entry=>key(entry)===subject);
    requireAudit(expectedMethod&&!subjects.has(subject),'E2E-SUBJECT',subject,'Missing, duplicated or unrelated product subject.');subjects.add(subject);
    requireAudit(outcome(method.status)===xml.status,'E2E-VERDICT',subject,'The DOM outcome cannot relabel a failed or skipped native testcase.');
    requireAudit(!methodIds.has(method.nodeId)&&!stepIds.has(method.nodeId),'E2E-DOM',subject,'Native method nodes cannot be reused.');methodIds.add(method.nodeId);
    const bound=nodeBinding(html,method.nodeId,subject);
    requireAudit(bound.title===method.methodName&&title(method.methodTitle||method.methodName)===method.methodName&&bound.status===xml.status,'E2E-DOM',subject,'Bind the exact native method title and direct status badge.');
    requireAudit(bound.classNode&&method.classNodeId==='test-id:'+bound.classNode.attrs['test-id'],'E2E-DOM',subject,'Method must remain under its original native class parent.');
    const classTitle=method.className.slice(method.className.lastIndexOf('.')+1);
    const classNames=descendants(bound.classNode,node=>node.tag==='p'&&hasClass(node,'name')).map(textOf);
    requireAudit(classNames.includes(classTitle)&&textOf(bound.classNode).includes(method.className),'E2E-DOM',subject,'Native class title and full class description must identify the selected owner.');
    const previous=classBindings.get(method.className);
    requireAudit(!previous||previous===method.classNodeId,'E2E-DOM',subject,'One source class cannot be split across unrelated native parents.');
    requireAudit(![...classBindings].some(([name,id])=>name!==method.className&&id===method.classNodeId),'E2E-DOM',subject,'Different source classes cannot share a class parent.');classBindings.set(method.className,method.classNodeId);
    requireAudit(Array.isArray(method.steps)&&method.steps.every(object),'E2E-DOM',subject,'Capture all native nested steps as structured native records.');
    const expectedStepAnchors=descendants(bound.card,node=>node.tag==='a'&&hasClass(node,'node')&&node!==bound.anchor);
    requireAudit(same(method.steps.map(step=>step.id).sort(),expectedStepAnchors.map(node=>node.attrs.id).sort()),'E2E-DOM',subject,'Captured steps must cover the exact native descendant node inventory.');
    const stepMap=new Map(),validations=[],failedActions=[];
    let actionCount=0,redactedInputs=0;
    for(const step of method.steps){
      requireAudit(object(step)&&typeof step.id==='string'&&!stepIds.has(step.id)&&!methodIds.has(step.id),'E2E-DOM',subject,'Native step IDs must be unique and distinct from method nodes.');stepIds.add(step.id);
      const actual=nodeBinding(html,step.id,subject+'/'+step.id);
      requireAudit(ancestor(actual.card.parent,node=>node===bound.card),'E2E-DOM',subject,'A step cannot be borrowed from another method.');
      const parentAnchor=ancestor(actual.card.parent,node=>hasClass(node,'card'));
      const ownParent=parentAnchor?descendants(parentAnchor,node=>node.tag==='a'&&hasClass(node,'node')).find(node=>ancestor(node,part=>hasClass(part,'card'))===parentAnchor):null;
      const parentId=ownParent?.attrs.id===method.nodeId?null:ownParent?.attrs.id||null;
      requireAudit((step.parentId??null)===parentId&&title(step.title)===actual.title&&outcome(step.status,true)===actual.status,'E2E-DOM',subject+'/'+step.id,'Native step hierarchy, title and direct status must match the original HTML.');
      const logs=ownLogs(actual.card),details=logs.filter(log=>log.status==='INFO').map(log=>log.text),terminals=logs.filter(log=>['PASS','FAIL','SKIP'].includes(log.status));
      requireAudit(Array.isArray(step.details)&&step.details.every(value=>typeof value==='string')&&same(step.details.map(norm),details)&&Array.isArray(step.terminalMessages)&&step.terminalMessages.every(value=>typeof value==='string')&&same(step.terminalMessages.map(norm),terminals.map(log=>log.text)),'E2E-DOM',subject+'/'+step.id,'Capture original own-node details and terminal rows without omissions or fabricated content.');
      stepMap.set(step.id,{...step,title:actual.title,status:actual.status,details,terminals});
      if(actual.title.startsWith('Validate '))validations.push(stepMap.get(step.id));
      else if(/^(?:Click|Type|Select|Navigate|Open|Get|Read|Press|Upload)\b/.test(actual.title)){
        actionCount++;if(actual.status==='FAIL')failedActions.push(step.id);
      }
      if(/^(?:Navigate|Open)\b/.test(actual.title)){
        for(const detail of details){
          const value=/^(base\.url|baseUrl|fullUrl|url)=(.+)$/i.exec(detail);if(!value)continue;
          let observedUrl;try{observedUrl=new URL(value[2]);}catch{requireAudit(false,'E2E-ID',subject+'/'+step.id,'Native navigation URL evidence is malformed.');}
          const sameOrigin=observedUrl.origin===target.origin;
          const rootPath=target.pathname.replace(/\/$/,'');
          const pathMatches=value[1].toLowerCase().replaceAll('.','')==='baseurl'?observedUrl.href===target.href:!rootPath||observedUrl.pathname===rootPath||observedUrl.pathname.startsWith(rootPath+'/');
          requireAudit(sameOrigin&&pathMatches&&!observedUrl.username&&!observedUrl.password,'E2E-ID',subject+'/'+step.id,'Native navigation evidence disagrees with the configured application target.');
          navigationEvidence.push({subject,nodeId:step.id,field:value[1],url:observedUrl.href});
        }
      }
      if(/^Type\b/.test(actual.title)&&/password|passwd|secret|token/i.test(actual.title+' '+details.filter(detail=>/^(?:locator|inputType|type)=/i.test(detail)).join(' '))){
        const values=details.filter(detail=>/^value=/i.test(detail));
        requireAudit(values.length===1&&/^value=(?:\[redacted\]|<redacted>)$/i.test(values[0]),'E2E-SECRET',subject+'/'+step.id,'Typed credential details must use an explicit redacted placeholder.');redactedInputs++;
      }
    }
    const reached=validations.filter(step=>expectedMethod.validations.includes(step.title));
    const observed=[...new Set(reached.map(step=>step.title))],missing=expectedMethod.validations.filter(label=>!observed.includes(label));
    if(xml.status==='PASS'){
      requireAudit(method.validationState==='REACHED'&&actionCount>0&&missing.length===0,'E2E-VALIDATION',subject,'A passing product method must execute actions and every existing required business validation.');
      for(const label of expectedMethod.validations){
        const candidates=reached.filter(step=>step.title===label);
        requireAudit(candidates.every(step=>step.status==='PASS'&&step.details.some(detail=>/^expected(?:[ ._-][^=]*)?=/.test(detail))&&step.details.some(detail=>/^actual(?:[ ._-][^=]*)?=/.test(detail))&&step.terminals.some(log=>log.status==='PASS'&&log.text==='Validated '+step.title.slice('Validate '.length))&&!step.terminals.some(log=>log.status==='FAIL')),'E2E-VALIDATION',subject+'/'+label,'Action success or a default node status is not business acceptance: preserve expected/actual and its explicit validation PASS.');
      }
    }else if(reached.length===0){
      requireAudit(method.validationState==='NOT_REACHED'&&method.notReachedReason===(xml.status==='SKIP'?'SKIPPED_BY_TESTNG':'FAILED_BEFORE_VALIDATION'),'E2E-VALIDATION',subject,'Unreached validation must be explicit and cannot fabricate acceptance.');
      requireAudit(xml.explanation.trim().length>0,'E2E-VALIDATION',subject,'Unreached business proof requires its original native failure/skip explanation.');
    }else{
      requireAudit(method.validationState==='REACHED','E2E-VALIDATION',subject,'Reached validation cannot be relabeled as unreached.');
      requireAudit(xml.status!=='SKIP'||method.notReachedReason===undefined,'E2E-VALIDATION',subject,'Skipped partial execution must keep its reached observations without an invented unreached reason.');
    }
    const mediaNodes=descendants(bound.card,node=>node.tag==='img'&&/^data:image\/png;base64,/.test(node.attrs.src||'')
      ||node.tag==='a'&&node.attrs['data-featherlight']==='image'&&/^data:image\/png;base64,/.test(node.attrs.href||''));
    const imageUris=new Set(),images=[];
    for(const media of mediaNodes){
      const uri=media.tag==='img'?media.attrs.src:media.attrs.href;if(imageUris.has(uri))continue;imageUris.add(uri);
      const container=ancestor(media,node=>hasClass(node,'row')&&hasClass(node,'mb-3'));
      const labels=container?descendants(container,node=>hasClass(node,'title')).map(textOf):[];
      requireAudit(container&&labels.length===1&&['Final Screenshot','Failure Screenshot'].includes(labels[0]),'E2E-SCREENSHOT',subject,'Bind a native method attachment with its actual final/failure screenshot caption, not a decorative image.');
      images.push({...pngMetadata(uri),label:labels[0]});
    }
    requireAudit(Array.isArray(method.screenshots)&&method.screenshots.every(object)&&same(method.screenshots.map(image=>[image.sha256,image.mimeType,image.bytes,image.width,image.height]).sort(),images.map(image=>[image.sha256,image.mimeType,image.bytes,image.width,image.height]).sort()),'E2E-SCREENSHOT',subject,'Screenshot metadata must describe exactly the embedded PNGs belonging to this method.');
    requireAudit(xml.status!=='PASS'||images.some(image=>image.label==='Final Screenshot'),'E2E-SCREENSHOT',subject,'The configured passing final-state screenshot must be present in its native method.');
    observations.push({subject,nativeOutcome:xml.status,domOutcome:bound.status,validationState:method.validationState,validationCoverage:!reached.length?'NOT_REACHED':missing.length?'PARTIAL':'COMPLETE',observedValidations:observed,missingValidations:missing,actionCount,failedActions:failedActions.length,screenshots:images.length,redactedInputs});
  }
  requireAudit(subjects.size===16&&classBindings.size===3,'E2E-SUBJECT','native DOM','Capture every retained subject under exactly its three native class parents.');
  const productTotals={total:16,passed:product.filter(entry=>entry.status==='PASS').length,failed:product.filter(entry=>entry.status==='FAIL').length,skipped:product.filter(entry=>entry.status==='SKIP').length};
  requireAudit(productTotals.passed===0||navigationEvidence.length>0,'E2E-ID','navigation evidence','Passing product acceptance needs actual captured navigation configuration/URL evidence for its declared target.');
  return {status:'PASSED',scope:'DECLARED_NATIVE_E2E_EVIDENCE_CONSISTENCY',executionAttestation:'NOT_PROVIDED',engine:source.engine,runId:source.runId,sourceCommit:source.sourceCommit,targetUrl:source.targetUrl,
    hashes:{sourceManifestSha256:digest(sourceManifestText),surefireXmlSha256:digest(surefireXml),htmlSha256:digest(extentHtml),captureCanonicalSha256:digest(JSON.stringify(capture)),probeSha256:capture.probeSha256},
    nativeTotals:native.totals,productTotals,navigationEvidence:{state:navigationEvidence.length?'OBSERVED':'NOT_OBSERVED',records:navigationEvidence},configurationCases:configurations.map(entry=>({subject:key(entry),outcome:entry.status})),methods:observations,
    boundary:'Checks original declared identities, native counters, HTML-owned DOM structure, terminal validation and embedded PNG containers. It does not attest browser execution, pixel meaning, general secret detection or assertion completeness beyond the retained owner map.'};
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
  try{
    const args={};for(let index=2;index<process.argv.length;index+=2){const flag=process.argv[index],value=process.argv[index+1];requireAudit(['--source','--xml','--html','--capture'].includes(flag)&&value&&!args[flag],'E2E-SOURCE','CLI','Supply each supported input file exactly once.');args[flag]=value;}
    requireAudit(Object.keys(args).length===4,'E2E-SOURCE','CLI','Required inputs: --source --xml --html --capture.');
    const regular=file=>{try{const stat=fs.lstatSync(file);requireAudit(stat.isFile()&&!stat.isSymbolicLink(),'E2E-SOURCE','CLI input','Read only ordinary input files.');return fs.readFileSync(file,'utf8');}catch(error){if(error.message.startsWith('UI-E2E-AUDIT'))throw error;requireAudit(false,'E2E-SOURCE','CLI input','A declared ordinary input file cannot be read.');}};
    let capture;try{capture=JSON.parse(regular(args['--capture']));}catch(error){if(error.message.startsWith('UI-E2E-AUDIT'))throw error;requireAudit(false,'E2E-SOURCE','capture JSON','Native capture must be valid JSON.');}
    const result=verifyUiE2eAudit({sourceManifestText:regular(args['--source']),surefireXml:regular(args['--xml']),extentHtml:regular(args['--html']),capture});
    process.stdout.write(JSON.stringify(result,null,2)+'\n');
  }catch(error){process.stderr.write(error.message+'\n');process.exitCode=1;}
}
