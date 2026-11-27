import test from 'node:test';
import assert from 'node:assert/strict';
import {referencePresentationProjection,renderReferenceScores} from './atlas-reference.mjs';
import {renderBusinessReader,validateBusinessReader,inspectBusinessReaderHtml} from './atlas-reader.mjs';
import {readerDiagramProjection,layoutReaderGraph} from './atlas-reader-graphs.mjs';

import {referenceFixture as fixture} from './fixtures/atlas-reference.mjs';

const owned=(run,pattern)=>assert.throws(run,e=>e.code==='ATLAS-READER'&&pattern.test(e.observed)&&Boolean(e.hint&&e.owner&&e.recheck));

test('reference opening has an open real primary map, closed support and scoped score links',()=>{
  const a=fixture(),before=structuredClone(a),html=renderBusinessReader({analysis:a},'body{}');
  assert.deepEqual(a,before);assert.equal(inspectBusinessReaderHtml(html).status,'PASSED');
  assert.match(html,/<details class="reader-primary" open>/);assert.doesNotMatch(html,/<details class="(?:reader-primary-proof|reader-score-rationale|reader-value|reader-flow)" open/);
  assert.equal((html.match(/class="score-card"/g)||[]).length,3);assert.match(html,/Goal/);assert.match(html,/Review ceiling/);assert.doesNotMatch(html,/class="axis-score">100|<script|<template/);
  assert.equal(readerDiagramProjection(a).filter(g=>g.kind==='PRIMARY').length,1);assert.ok(!readerDiagramProjection(a).some(g=>g.id==='reader-value-model'));
});
test('reference profiles cannot silently render unknown presentation fields or downgrade the projection',()=>{
  const a=fixture();delete a.reader.presentationProfile;owned(()=>validateBusinessReader(a),/Declare the reference/);
  a.reader.presentationProfile='invented';owned(()=>referencePresentationProjection(a),/supported business/);
  a.reader.presentationProfile='BUSINESS_REFERENCE_V1';a.reader.primaryGraph.command='ignored';owned(()=>referencePresentationProjection(a),/declared business reference/);
});
test('local proof selects actual facets and independent oracles rather than borrowed green claims',()=>{
  for(const mutate of [a=>a.reader.primaryGraph.nodes[0].facetRefs[0].unitId='foreign',a=>a.reader.primaryGraph.nodes[0].claimIds=['ORACLE-FOREIGN'],a=>a.reader.primaryGraph.nodes[0].oracleClaimIds=['FACT'],a=>a.reader.primaryGraph.nodes[0].oracleClaimIds=[]]){const a=fixture();mutate(a);owned(()=>referencePresentationProjection(a),/facet|claims|oracle/);}
});
test('object maturity preserves partial and unknown source meaning despite passing unrelated observations',()=>{
  const a=fixture();a.understanding.units[0].facets.behavior.status='PARTIAL';owned(()=>referencePresentationProjection(a),/least-established/);
  a.reader.primaryGraph.nodes[1].maturity='PARTIAL';assert.equal(referencePresentationProjection(a).primary.nodes[1].status,'Partial understanding');
  a.understanding.units[0].facets.behavior.status='UNKNOWN';a.reader.primaryGraph.nodes[1].maturity='UNKNOWN';a.reader.primaryGraph.nodes[1].oracleClaimIds=[];assert.equal(referencePresentationProjection(a).primary.nodes[1].proof,'No existing check established');
});
test('a real retained failure remains visible in local object proof',()=>{
  const a=fixture();Object.assign(a.claims[1],{status:'EXECUTED',verdict:'FAILED'});assert.equal(referencePresentationProjection(a).primary.nodes[0].proof,'A retained check failed');
});
test('primary relationships cannot invent endpoints, borrow source or masquerade as unsupported limits',()=>{
  for(const mutate of [g=>g.edges[0].to='foreign',g=>g.edges[0].claimIds=['ORACLE'],g=>g.edges[0].kind='LIMIT',g=>g.edges=[]]){const a=fixture();mutate(a.reader.primaryGraph);owned(()=>referencePresentationProjection(a),/endpoint|source claims|limitation arrow|distinct real/);}
});
test('three real objects and connected source relationships cannot be replaced by decorative padding',()=>{
  const a=fixture();a.reader.primaryGraph.nodes.pop();owned(()=>referencePresentationProjection(a),/three real/);
  const b=fixture();b.reader.primaryGraph.nodes.push({...b.reader.primaryGraph.nodes[0],id:'unused',band:0,column:0});owned(()=>referencePresentationProjection(b),/isolated/);
});
test('bounded layout rejects overlapping frames and keeps every actual source edge',()=>{
  const a=fixture(),graph={...a.reader.primaryGraph,kind:'PRIMARY'},before=structuredClone(graph),layout=layoutReaderGraph(graph);assert.equal(layout.nodes.length,3);assert.deepEqual(layout.edges,graph.edges);assert.deepEqual(graph,before);
  graph.nodes[1].band=graph.nodes[0].band;owned(()=>layoutReaderGraph(graph),/overlap/);
});
test('score values retain the actual rubric and cannot be manufactured from successful test counts',()=>{
  const a=fixture();a.assessment.groups[0].axes[0].score=99;assert.throws(()=>referencePresentationProjection(a),e=>e.code==='ATLAS-ASSESSMENT');
  const b=fixture();b.reader.assessment.groups[0].axes[0].ceiling=89;owned(()=>referencePresentationProjection(b),/reviewed ceiling/);
});
test('score goals, rationale, group separation and exact featured targets remain complete',()=>{
  for(const mutate of [r=>r.groups[0].axes[0].goal='OK',r=>r.groups[0].axes.pop(),r=>r.featured[0].axisId='foreign',r=>r.featured.push(r.featured[0]),r=>r.groups[0].groupId='other']){const a=fixture();mutate(a.reader.assessment);owned(()=>referencePresentationProjection(a),/goal|complete|featured|distinct|scope|group/);}
});
test('excellent scores require original execution evidence as well as a stated ceiling',()=>{
  const a=fixture(),axis=a.assessment.groups[0].axes[0];Object.assign(axis,{correctness:96,verification:96,clarity:96,score:96});a.reader.assessment.groups[0].axes[0].ceiling=97;owned(()=>referencePresentationProjection(a),/95\+/);
  a.evidence.push({id:'retained',status:'PASSED'});axis.evidenceIds=['retained'];assert.equal(referencePresentationProjection(a).axes[0].score,96);
});
test('business-only projection refuses technical score or object prose before output',()=>{
  const a=fixture();a.reader.assessment.groups[0].axes[0].measure='Inspect source-index.json and all XML files';owned(()=>validateBusinessReader(a),/technical construction/);
  const b=fixture();b.reader.primaryGraph.nodes[0].meaning='Use ssh remote-host to inspect the result';owned(()=>validateBusinessReader(b),/technical construction/);
});
test('native local score targets cannot escape the reader or disappear behind envelope changes',()=>{
  const html=renderBusinessReader({analysis:fixture()},'body{}');assert.match(renderReferenceScores(referencePresentationProjection(fixture())),/href="#score-atlas-understanding"/);
  owned(()=>inspectBusinessReaderHtml(html.replace('href="#score-atlas-understanding"','href="https://invalid/score"')),/local reviewed/);
  owned(()=>inspectBusinessReaderHtml(html.replace('id="score-atlas-understanding"','id="lost"')),/exactly once/);
});
