import {requireAtlas} from './atlas-diagnostics.mjs';

const escape=value=>String(value).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;').replaceAll('"','&quot;').replaceAll("'",'&#39;');

/** Allocate reading space by cell content, with bounded influence from long identifiers. */
export function tableColumns(headers,rows){
  requireAtlas(headers.length>0&&rows.every(row=>row.length===headers.length),'ATLAS-PRESENTATION','Table rows must match their declared headers');
  const columns=headers.map((header,index)=>{
    const values=rows.map(row=>String(row[index]??'')),prose=values.some(value=>value.length>80&&value.split(/\s+/).length>9);
    const numeric=values.length>0&&values.every(value=>/^[\d\s./%—–-]+$/.test(value));
    const typical=values.map(value=>Math.min(180,value.length)).sort((a,b)=>a-b)[Math.floor(Math.max(0,values.length-1)*0.8)]||0;
    const minimum=prose?300:numeric?Math.max(88,Math.min(160,String(header).length*7)):Math.max(144,Math.min(240,70+typical*3));
    return {kind:prose?'prose':numeric?'metric':'reference',minimum};
  });
  const minimum=columns.reduce((sum,column)=>sum+column.minimum,0);
  return {minimum,columns:columns.map(column=>({...column,percent:100*column.minimum/minimum}))};
}

export function presentationTable(headers,rows){
  const layout=tableColumns(headers,rows);
  if(headers[0]==='Case'&&headers.length===6)return '<div class="atlas-cases">'+rows.map(row=>'<article class="atlas-case"><h3>'+escape(row[0])+'</h3><dl>'+headers.slice(1).map((header,index)=>'<dt>'+escape(header)+'</dt><dd>'+escape(row[index+1])+'</dd>').join('')+'</dl></article>').join('')+'</div>';
  return '<div class="table-scroll atlas-table" role="region" aria-label="'+escape(headers.join(' / '))+'" tabindex="0"><table style="min-width:'+layout.minimum+'px"><colgroup>'+layout.columns.map(column=>'<col style="width:'+column.percent.toFixed(3)+'%">').join('')+'</colgroup><thead><tr>'+headers.map(header=>'<th scope="col">'+escape(header)+'</th>').join('')+'</tr></thead><tbody>'+rows.map(row=>'<tr>'+row.map((value,index)=>'<td data-column-kind="'+layout.columns[index].kind+'">'+escape(value??'')+'</td>').join('')+'</tr>').join('')+'</tbody></table></div>';
}

const inline=text=>{
  // Authored HTML and resource URLs stay inert. This is a documented Markdown subset.
  return text.split(/(`[^`\n]+`)/g).map((part,index)=>index%2===1
    ?'<code>'+escape(part.slice(1,-1))+'</code>':escape(part).replace(/\*\*([^*\n]+)\*\*/g,'<strong>$1</strong>')).join('');
};
const splitRow=line=>line.trim().replace(/^\|/,'').replace(/\|$/,'').split(/(?<!\\)\|/).map(cell=>cell.trim().replaceAll('\\|','|'));
const separator=line=>/^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(line);

/** Render readable blocks without interpreting untrusted HTML, scripts or network resources. */
export function presentationMarkdown(text){
  const lines=text.replaceAll('\r\n','\n').split('\n'),output=[];let index=0,disclosures=0;
  while(index<lines.length){
    const line=lines[index];if(!line.trim()){index++;continue;}
    const fence=/^\s*(`{3,}|~{3,})([\w-]*)\s*$/.exec(line);
    if(fence){
      const code=[];index++;while(index<lines.length&&!new RegExp('^\\s*'+fence[1][0]+'{'+fence[1].length+',}\\s*$').test(lines[index]))code.push(lines[index++]);
      if(index<lines.length)index++;
      output.push((fence[2]==='mermaid'?'<p class="note">Retained Mermaid source; the reviewed analysis diagrams appear in Behavior and Architecture.</p>':'')+'<pre><code'+(fence[2]?' class="language-'+escape(fence[2])+'"':'')+'>'+escape(code.join('\n'))+'</code></pre>');continue;
    }
    const heading=/^(#{1,6})\s+(.+)$/.exec(line);
    if(heading){const level=Math.min(6,heading[1].length+2);output.push('<h'+level+'>'+inline(heading[2])+'</h'+level+'>');index++;continue;}
    if(index+1<lines.length&&line.includes('|')&&separator(lines[index+1])){
      const headers=splitRow(line),rows=[];index+=2;
      while(index<lines.length&&lines[index].trim()&&lines[index].includes('|')){
        const row=splitRow(lines[index]);if(row.length!==headers.length)break;rows.push(row);index++;
      }
      output.push(presentationTable(headers,rows));continue;
    }
    const list=/^\s*(?:([-+*])|(\d+)\.)\s+(.+)$/.exec(line);
    if(list){
      const ordered=!!list[2],items=[];
      while(index<lines.length){const item=/^\s*(?:([-+*])|(\d+)\.)\s+(.+)$/.exec(lines[index]);if(!item||!!item[2]!==ordered)break;
        const parts=[item[3]];index++;while(index<lines.length&&/^\s{2,}\S/.test(lines[index])&&!/^\s*(?:[-+*]|\d+\.)\s/.test(lines[index]))parts.push(lines[index++].trim());items.push('<li>'+inline(parts.join(' '))+'</li>');}
      const tag=ordered?'ol':'ul';output.push('<'+tag+'>'+items.join('')+'</'+tag+'>');continue;
    }
    if(/^\s*<details(?:\s+open)?>\s*$/.test(line)){output.push('<details>');disclosures++;index++;continue;}
    if(/^\s*<\/details>\s*$/.test(line)){output.push(disclosures>0?'</details>':'<p>'+escape(line)+'</p>');if(disclosures>0)disclosures--;index++;continue;}
    const summary=/^\s*<summary>([^<>]+)<\/summary>\s*$/.exec(line);
    if(summary&&disclosures>0){output.push('<summary>'+inline(summary[1])+'</summary>');index++;continue;}
    const paragraph=[line.trim()];index++;
    while(index<lines.length&&lines[index].trim()&&!/^\s*(?:#{1,6}\s|`{3,}|~{3,}|[-+*]\s|\d+\.\s|<\/?(?:details|summary)\b)/.test(lines[index])&&!(index+1<lines.length&&separator(lines[index+1])))paragraph.push(lines[index++].trim());
    output.push('<p>'+inline(paragraph.join(' '))+'</p>');
  }
  return output.join('')+'</details>'.repeat(disclosures);
}

export function graphLegend(graph){
  const dependency=graph.kind==='DEPENDENCY';
  return '<figcaption class="atlas-legend"><strong>Legend · '+(dependency?'Dependency':'Behavior')+'</strong><ul><li>Boxes: '+escape([...new Set(graph.nodes.map(node=>node.kind))].join(', '))+'. Labels identify '+(dependency?'declared owners and boundaries':'declared intents, actions, states or outcomes')+'.</li><li>Arrows: '+(dependency?'a declared dependency from its consumer to its provider':'a directed, claim-backed transition; arrow labels name the observation')+'. A dashed arrow has the literal label <code>test</code>; it does not imply execution.</li><li>Evidence: source review and retained execution remain separate. Read the adjacent claim and oracle scope before treating an outcome as executed.</li><li>Limit: '+escape(graph.limits||'Only the declared nodes and edges are represented; no omitted transition or full coverage is inferred.')+'</li></ul></figcaption>';
}

/** Browser-side metric producer. Used only by document QA, never embedded in a reader. */
export function atlasLayoutProbe(){
  return {viewport:innerWidth,pageOverflow:document.documentElement.scrollWidth>innerWidth+2,tables:[...document.querySelectorAll('table')].filter(table=>table.getBoundingClientRect().width>0).map((table,index)=>({index,headers:[...table.querySelectorAll('thead th')].map(cell=>cell.textContent),scrollLabel:table.parentElement.getAttribute('aria-label'),focusable:table.parentElement.tabIndex===0,contained:table.parentElement.scrollWidth>table.parentElement.clientWidth+2,rows:[...table.querySelectorAll('tbody tr')].map(row=>[...row.cells].map(cell=>{const style=getComputedStyle(cell),width=cell.clientWidth-parseFloat(style.paddingLeft)-parseFloat(style.paddingRight),range=document.createRange();range.selectNodeContents(cell);const boxes=[...range.getClientRects()],lines=new Set(boxes.filter(box=>box.width>0).map(box=>Math.round(box.top))).size;return {text:cell.textContent,width,lines,font:parseFloat(style.fontSize),clipped:cell.scrollWidth>cell.clientWidth+2||['hidden','clip'].includes(style.overflow),whiteSpace:style.whiteSpace};}))}))};
}

/** A bounded readability signal, not a semantic or aesthetic completeness oracle. */
export function reviewAtlasLayout(metrics){
  const findings=[];
  if(metrics.pageOverflow)findings.push('Document has horizontal overflow; contain wide tables locally.');
  for(const table of metrics.tables){
    if(table.contained&&(!table.focusable||!table.scrollLabel))findings.push('Table '+table.index+' needs a named, keyboard-focusable scroll region.');
    for(const [rowIndex,row]of table.rows.entries())for(const [column,cell]of row.entries()){
      if(cell.clipped)findings.push('Table '+table.index+' row '+rowIndex+' column '+column+' clips or hides content.');
      if(cell.font<13)findings.push('Table '+table.index+' uses text smaller than 13 CSS px.');
      const prose=cell.text.length>100&&cell.text.trim().split(/\s+/).length>12;
      if(prose&&cell.width<180&&cell.lines>=6&&row.some(other=>other.width>cell.width*1.8&&other.text.length<60&&other.lines<=3))findings.push('Table '+table.index+' row '+rowIndex+' column '+column+' starves long prose while a short cell receives much more width.');
    }
  }
  return {status:findings.length?'REPAIR_REQUIRED':'PASSED',findings,scope:'MEASURED_RENDERED_TABLE_READABILITY_SIGNALS; HUMAN_SEMANTIC_AND_VISUAL_REVIEW_REQUIRED'};
}

export function validateAtlasLayout(metrics){
  const review=reviewAtlasLayout(metrics);
  requireAtlas(review.status==='PASSED','ATLAS-PRESENTATION',review.findings.join('\n'));
  return review;
}
