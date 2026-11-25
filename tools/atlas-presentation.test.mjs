import test from 'node:test';
import assert from 'node:assert/strict';
import {tableColumns,presentationTable,presentationMarkdown,graphLegend,reviewAtlasLayout,validateAtlasLayout} from './atlas-presentation.mjs';

const explanation='The original response remains unchanged after the bounded retry and its exact observation is supported by the retained independent assertion. ';

test('prose receives more reading space than short numeric values',()=>{
  const result=tableColumns(['Explanation','Count'],[[explanation,2]]);
  assert.ok(result.columns[0].minimum>=300);assert.ok(result.columns[0].percent>result.columns[1].percent*2);
  assert.ok(Math.abs(result.columns.reduce((sum,column)=>sum+column.percent,0)-100)<0.001);
});
test('a very long identifier cannot dominate the prose column',()=>{
  const result=tableColumns(['Identity','Reason'],[['a'.repeat(4000),explanation]]);
  assert.ok(result.columns[0].minimum<=240);assert.ok(result.columns[1].minimum>result.columns[0].minimum);
});
test('sparse and empty comparison tables remain valid',()=>{
  assert.equal(tableColumns(['From','To'],[]).columns.length,2);
  assert.match(presentationTable(['Owner','Observation'],[['Single owner','No dependency declared']]),/scope="col"/);
});
test('ragged rows fail with owned constructive feedback',()=>{
  assert.throws(()=>presentationTable(['Owner','Meaning'],[['Lost cell']]),error=>error.code==='ATLAS-PRESENTATION'&&error.hint.includes('never hide text')&&error.owner.endsWith('Report_Contract.md')&&error.recheck.includes('atlas-presentation.test.mjs'));
});
test('dense comparisons remain complete in a named keyboard scroll region',()=>{
  const html=presentationTable(['Owner','A','B','C','D','E','F','G'],[['scope',...Array(7).fill('SOURCE_REVIEWED')]]);
  assert.match(html,/role="region"[^>]+tabindex="0"/);assert.equal((html.match(/<col style=/g)||[]).length,8);
  assert.equal((html.match(/SOURCE_REVIEWED/g)||[]).length,7);assert.match(html,/<colgroup>/);
});
test('narrative case witnesses become readable records without losing a field',()=>{
  const headers=['Case','Kind / exact oracle fragment','Precondition and trigger','Expected observation','Directed witness','Independent assertion and limits'];
  const values=['Case one','NEGATIVE / source.txt:1',explanation,'Exact result','start → end',explanation+'Explicit limit.'];
  const html=presentationTable(headers,[values]);assert.match(html,/<article class="atlas-case">/);assert.doesNotMatch(html,/<table/);
  for(const value of values)assert.ok(html.includes(value));assert.equal((html.match(/<dt>/g)||[]).length,5);
});
test('wrapped Markdown prose becomes one paragraph with preserved heading hierarchy',()=>{
  assert.equal(presentationMarkdown('# Scope\n\nOne physical line\nand its continuation.\n\n## Limits\n\nUnknown.'),'<h3>Scope</h3><p>One physical line and its continuation.</p><h4>Limits</h4><p>Unknown.</p>');
});
test('bullets and ordered steps are semantic lists, including wrapped items',()=>{
  assert.equal(presentationMarkdown('- Observe\n  the original value.\n- Retain evidence\n\n1. Read\n2. Verify'),'<ul><li>Observe the original value.</li><li>Retain evidence</li></ul><ol><li>Read</li><li>Verify</li></ol>');
});
test('Markdown comparison tables preserve all rows and escaped separators',()=>{
  const html=presentationMarkdown('| Owner | Limit |\n|---|---|\n| Core | A \\| B |\n| UI | Unknown |');
  assert.match(html,/<table/);assert.match(html,/A \| B/);assert.match(html,/Unknown/);assert.equal((html.match(/<tbody><tr>|<\/tr><tr>/g)||[]).length,2);
});
test('authored resources, executable HTML and CSS remain inert text',()=>{
  const html=presentationMarkdown('<script>alert(1)</script>\n\n<img src="https://invalid/image">\n\n[Reference](https://invalid)\n\n`<style>body{display:none}</style>`');
  assert.doesNotMatch(html,/<(?:script|img|style|iframe|a)\b/);assert.match(html,/&lt;script&gt;/);assert.match(html,/<code>&lt;style&gt;/);
});
test('code is protected from emphasis parsing',()=>{
  assert.equal(presentationMarkdown('**Observed** `**literal**`'),'<p><strong>Observed</strong> <code>**literal**</code></p>');
  assert.equal(presentationMarkdown('`'),'<p>`</p>');
});
test('command recipes remain closed native disclosures',()=>{
  const html=presentationMarkdown('<details open>\n<summary>Local verification</summary>\n\n```powershell\nmvn test\n```\n\n</details>');
  assert.match(html,/^<details><summary>Local verification<\/summary>/);assert.doesNotMatch(html,/<details open/);assert.match(html,/<code class="language-powershell">mvn test/);
});
test('unbalanced authored disclosures cannot escape their source document',()=>{
  assert.match(presentationMarkdown('</details>\n\nText'),/^<p>&lt;\/details&gt;<\/p>/);
  assert.equal(presentationMarkdown('<details>\n<summary>Scope</summary>\n\nText'),'<details><summary>Scope</summary><p>Text</p></details>');
});
test('Mermaid source is retained honestly without scripts or invented graphs',()=>{
  const html=presentationMarkdown('```mermaid\nflowchart TB\n A --> B\n```');
  assert.match(html,/Retained Mermaid source/);assert.match(html,/A --&gt; B/);assert.doesNotMatch(html,/<svg|<script/);
});
test('behavior and dependency legends disclose different arrow meanings and evidence limits',()=>{
  const base={nodes:[{kind:'ACTION'},{kind:'OUTCOME'}],limits:'Selected source only.'};
  const behavior=graphLegend({...base,kind:'BEHAVIOR'}),dependency=graphLegend({...base,kind:'DEPENDENCY'});
  assert.match(behavior,/directed, claim-backed transition/);assert.match(dependency,/consumer to its provider/);
  for(const html of [behavior,dependency]){assert.match(html,/does not imply execution/);assert.match(html,/Selected source only\./);assert.match(html,/<figcaption/);}
});
const cell=(text,width,lines,extra={})=>({text,width,lines,font:14,clipped:false,...extra});
const metrics=row=>({pageOverflow:false,tables:[{index:0,contained:false,rows:[row]}]});
test('measured narrow prose versus wide short text fails, then passes after width repair',()=>{
  const bad=metrics([cell(explanation,100,12),cell('PASSED',450,1)]);
  assert.equal(reviewAtlasLayout(bad).status,'REPAIR_REQUIRED');assert.match(reviewAtlasLayout(bad).findings[0],/starves long prose/);
  assert.throws(()=>validateAtlasLayout(bad),error=>error.code==='ATLAS-PRESENTATION'&&error.message.includes('Observed:')&&error.message.includes('Repair hint:')&&error.hint.includes('allocate space to prose'));
  const repaired=metrics([cell(explanation,450,3),cell('PASSED',100,1)]);
  assert.equal(reviewAtlasLayout(repaired).status,'PASSED');
});
test('a wrapped identifier or two equally long explanations do not trigger the imbalance heuristic',()=>{
  assert.equal(reviewAtlasLayout(metrics([cell('x'.repeat(500),100,20),cell('ID',400,1)])).status,'PASSED');
  assert.equal(reviewAtlasLayout(metrics([cell(explanation,160,7),cell(explanation,160,7)])).status,'PASSED');
});
test('clipping, tiny type and document overflow are explicit failures',()=>{
  const observation=metrics([cell('Hidden',90,1,{clipped:true,font:10})]);observation.pageOverflow=true;
  const result=reviewAtlasLayout(observation);assert.equal(result.status,'REPAIR_REQUIRED');assert.equal(result.findings.length,3);
});
test('contained table scrolling needs a name and keyboard focus',()=>{
  const observation=metrics([cell('Full value',300,1)]);observation.tables[0].contained=true;
  assert.equal(reviewAtlasLayout(observation).status,'REPAIR_REQUIRED');Object.assign(observation.tables[0],{focusable:true,scrollLabel:'Evidence comparison'});
  assert.equal(reviewAtlasLayout(observation).status,'PASSED');
});
