import {requireAtlas} from './atlas-diagnostics.mjs';
import {validateSemanticFacets} from './atlas-semantics.mjs';

export const understandingFacets=Object.freeze(['purpose','structure','behavior','data','configuration','failure','verification']);
const text=value=>typeof value==='string'&&value.trim().length>0;
const array=Array.isArray;
const id=value=>typeof value==='string'&&/^[a-z][a-z0-9-]*$/.test(value);
const relative=value=>text(value)&&!value.includes('\\')&&!value.includes(':')&&!value.startsWith('/')&&value.split('/').every(part=>part&&part!=='.'&&part!=='..');
const reviewed=claim=>claim&&['SOURCE_REVIEWED','EXECUTED'].includes(claim.status);
const inside=(file,scope)=>file===scope||file.startsWith(scope+'/');
const requireDepth=(condition,observed)=>requireAtlas(condition,'ATLAS-UNDERSTANDING',observed);
const requireScore=(condition,observed)=>requireAtlas(condition,'ATLAS-ASSESSMENT',observed);

/** Origin declarations distinguish reviewed local source from selected artifact contracts. */
export function validateAtlasOrigins(analysis,index){
  const review=analysis.understanding;
  if(review?.originProfile===undefined)return {status:'NOT_ASSESSED',scope:'LEGACY_ORIGIN_DECLARATIONS'};
  requireDepth(review.originProfile==='SOURCE_ORIGINS_V1'&&array(review.units)&&array(index?.files),'Declare the supported source-origin profile and indexed units');
  const indexed=new Set(index.files.map(file=>file.path)),claims=new Map((analysis.claims||[]).map(c=>[c.id,c]));let local=0,consumed=0,artifacts=0;
  for(const unit of review.units){
    requireDepth(['LOCAL_SOURCE','CONSUMED_ARTIFACT'].includes(unit.sourceRole),'Declare local source or consumed artifact for each unit: '+unit.id);
    if(unit.sourceRole==='LOCAL_SOURCE'){
      requireDepth(unit.artifacts===undefined||array(unit.artifacts)&&unit.artifacts.length===0,'Local source must not relabel a consumed artifact pair: '+unit.id);local++;continue;
    }
    requireDepth(unit.kind==='BOUNDARY'&&array(unit.artifacts)&&unit.artifacts.length>0,'Consumed artifacts are contract boundaries, not local source modules: '+unit.id);
    const paths=new Set(),owned=relative=>unit.scopePaths?.some(scope=>inside(relative,scope));
    for(const artifact of unit.artifacts){
      requireDepth(artifact&&typeof artifact.coordinate==='string'&&/^[^\s:]+:[^\s:]+:[^\s:]+$/.test(artifact.coordinate)&&relative(artifact.jarPath)&&relative(artifact.pomPath)&&artifact.jarPath.endsWith('.jar')&&artifact.pomPath.endsWith('.pom')&&artifact.jarPath!==artifact.pomPath&&indexed.has(artifact.jarPath)&&indexed.has(artifact.pomPath)&&owned(artifact.jarPath)&&owned(artifact.pomPath)&&!paths.has(artifact.jarPath)&&!paths.has(artifact.pomPath),'A consumed contract needs its distinct indexed, owned JAR/POM pair and selected coordinate: '+unit.id);
      requireDepth((unit.claimIds||[]).some(id=>reviewed(claims.get(id))&&claims.get(id).sourceRefs?.some(ref=>ref.path===artifact.pomPath)),'Cite the selected artifact declaration; an index entry alone is not a reviewed library contract: '+unit.id+'/'+artifact.pomPath);
      paths.add(artifact.jarPath);paths.add(artifact.pomPath);artifacts++;
    }
    consumed++;
  }
  return {status:'PASSED',profile:review.originProfile,localUnits:local,consumedBoundaries:consumed,selectedArtifacts:artifacts,scope:'DECLARED_ORIGIN_AND_SELECTED_PAIR_BINDINGS; NOT_PRODUCER_SOURCE_OR_AUTOMATIC_DEPENDENCY_RESOLUTION'};
}

/** The current authoring entry cannot silently downgrade to a historical profile. */
export function validateCurrentUnderstandingProfiles(analysis,index,{profile='CURRENT_UNDERSTANDING_V3'}={}){
  requireDepth(['CURRENT_UNDERSTANDING_V1','CURRENT_UNDERSTANDING_V2','CURRENT_UNDERSTANDING_V3'].includes(profile),'Unsupported understanding publication profile');
  const reference=analysis?.reader?.presentationProfile;
  requireDepth(analysis?.reader?.diagramProfile==='SOURCE_GRAPH_V2'&&analysis.reader.comparisonProfile==='SOURCE_BOUND_COMPARISONS_V1'&&analysis.reader.captureProfile==='GIT_WORKTREE_V1'&&index?.captureProfile==='GIT_WORKTREE_V1'&&analysis.understanding?.originProfile==='SOURCE_ORIGINS_V1'&&(profile==='CURRENT_UNDERSTANDING_V1'||profile==='CURRENT_UNDERSTANDING_V2'&&reference==='BUSINESS_REFERENCE_V1'||profile==='CURRENT_UNDERSTANDING_V3'&&reference==='BUSINESS_REFERENCE_V2'),'Current understanding requires a business map without horizontal scrolling, explained review scores, useful source diagrams, concrete case comparisons, source-capture disclosure and local/consumed origins; use the current owner contract instead of silently dropping a profile');
  return {status:'PASSED',profile,scope:profile==='CURRENT_UNDERSTANDING_V3'?'CURRENT_PROFILE_DECLARATIONS; LIVE_SOURCE_COMPONENT_AND_HUMAN_REVIEW_STILL_REQUIRED':'HISTORICAL_'+profile.slice(-2)+'_DECLARATIONS; NO_NEW_PRESENTATION_UPGRADE'};
}

function reviewAssessment(assessment,claims,evidence){
  if(assessment===undefined)return null;
  requireScore(assessment&&assessment.method==='EVIDENCE_BOUND_REVIEW'&&text(assessment.reviewer)&&text(assessment.scope)&&text(assessment.limits)
    &&array(assessment.groups)&&assessment.groups.length>0,'An assessment needs a declared reviewer, review scope, limitations and rubric groups');
  const groups=new Set(),result=[];
  for(const group of assessment.groups){
    requireScore(group&&id(group.id)&&!groups.has(group.id)&&text(group.label)&&text(group.scope)&&text(group.limits)&&array(group.axes)&&group.axes.length>0,'Assessment groups must be unique and scoped');groups.add(group.id);
    const axes=new Set();let weight=0,total=0;
    for(const axis of group.axes){
      requireScore(axis&&id(axis.id)&&!axes.has(axis.id)&&text(axis.label)&&text(axis.reason)&&text(axis.limits)
        &&['correctness','verification','clarity','score','weight'].every(key=>Number.isInteger(axis[key])&&axis[key]>=1&&axis[key]<=100),
      'Assessment axis needs bounded integers, rationale and limitations: '+group.id+'/'+axis?.id);axes.add(axis.id);
      requireScore(axis.score===Math.round((4*axis.correctness+3*axis.verification+3*axis.clarity)/10),'Assessment axis score differs from its 40/30/30 review rubric: '+axis.id);
      requireScore(array(axis.claimIds)&&axis.claimIds.length>0&&new Set(axis.claimIds).size===axis.claimIds.length&&axis.claimIds.every(key=>reviewed(claims.get(key))),
        'Assessment axis needs existing reviewed claim references: '+axis.id);
      requireScore(array(axis.evidenceIds)&&new Set(axis.evidenceIds).size===axis.evidenceIds.length&&axis.evidenceIds.every(key=>evidence.has(key)),
        'Assessment axis references unknown execution evidence: '+axis.id);
      weight+=axis.weight;total+=axis.weight*axis.score;
    }
    requireScore(weight===100,'Assessment axis weights must total 100: '+group.id);
    const score=Math.round(total/100);requireScore(group.score===undefined||group.score===score,'Assessment group total differs from its declared axis weights: '+group.id);
    result.push({id:group.id,label:group.label,score,scope:group.scope,limits:group.limits,axes:group.axes});
  }
  return {method:assessment.method,reviewer:assessment.reviewer,scope:assessment.scope,limits:assessment.limits,groups:result};
}

/** Reuse the bounded rubric independently of live inventory verification. */
export function reviewAtlasAssessment(analysis){
  return reviewAssessment(analysis.assessment,new Map(analysis.claims.map(c=>[c.id,c])),new Map((analysis.evidence||[]).map(e=>[e.id,e])));
}

/** Declared review breadth is auditable metadata, never inferred whole-project semantic coverage. */
export function validateAtlasUnderstanding(analysis,index,{required=false}={}){
  requireDepth(analysis&&index&&array(analysis.claims)&&analysis.claims.every(claim=>claim&&text(claim.id)),
    'Understanding review needs valid source claims and behavior flow records');
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim]));
  const assessment=reviewAssessment(analysis.assessment,claims,new Map((analysis.evidence||[]).map(item=>[item.id,item])));
  const understanding=analysis.understanding;
  if(understanding===undefined){requireDepth(!required,'Renderer version 2 requires an explicit source-bound understanding inventory');return {status:'NOT_ASSESSED',assessment};}
  requireDepth(analysis.behaviorFlows===undefined||array(analysis.behaviorFlows)&&analysis.behaviorFlows.every(flow=>flow&&text(flow.id)),
    'Understanding review needs valid source claims and behavior flow records');
  const flows=new Map((analysis.behaviorFlows||[]).map(flow=>[flow.id,flow]));
  requireDepth(understanding&&understanding.schemaVersion===1&&['PROJECT','SELECTED'].includes(understanding.scope)&&text(understanding.scopeStatement)
    &&array(understanding.inventoryClaimIds)&&understanding.inventoryClaimIds.length>0&&understanding.inventoryClaimIds.every(key=>reviewed(claims.get(key)))
    &&array(understanding.units)&&understanding.units.length>0,'Declare review scope, inventory-source claims and understanding units');
  const indexed=new Set((index.files||[]).map(entry=>entry.path)),units=new Map(),counts={REVIEWED:0,PARTIAL:0,UNKNOWN:0,NOT_APPLICABLE:0};
  for(const unit of understanding.units){
    requireDepth(unit&&id(unit.id)&&!units.has(unit.id)&&text(unit.label)&&['MODULE','COMPONENT','SERVICE','BOUNDARY'].includes(unit.kind)
      &&text(unit.limits)&&array(unit.scopePaths)&&unit.scopePaths.length>0&&new Set(unit.scopePaths).size===unit.scopePaths.length
      &&unit.scopePaths.every(scope=>relative(scope)&&[...indexed].some(file=>inside(file,scope))),
    'Understanding unit needs unique identity, actual source scope and limits: '+unit?.id);
    requireDepth(array(unit.claimIds)&&new Set(unit.claimIds).size===unit.claimIds.length&&unit.claimIds.every(key=>reviewed(claims.get(key)))
      &&array(unit.sharedClaimIds)&&new Set(unit.sharedClaimIds).size===unit.sharedClaimIds.length&&unit.sharedClaimIds.every(key=>reviewed(claims.get(key)))
      &&array(unit.flowIds)&&new Set(unit.flowIds).size===unit.flowIds.length&&unit.flowIds.every(key=>flows.has(key)),
    'Understanding unit contains unsupported or duplicate claim/flow references: '+unit.id);
    const allowed=new Set([...unit.claimIds,...unit.sharedClaimIds]);
    for(const key of unit.claimIds)requireDepth((claims.get(key).sourceRefs||[]).some(ref=>unit.scopePaths.some(scope=>inside(ref.path,scope))),
      'Unit claim has no source in its declared owner; use an explicit related shared reference after review: '+unit.id+'/'+key);
    requireDepth(unit.facets&&Object.keys(unit.facets).length===understandingFacets.length&&understandingFacets.every(key=>Object.hasOwn(unit.facets,key)),
      'Every understanding unit needs all seven review facets: '+unit.id);
    for(const name of understandingFacets){const facet=unit.facets[name];
      requireDepth(facet&&Object.hasOwn(counts,facet.status)&&text(facet.explanation)&&array(facet.claimIds)&&new Set(facet.claimIds).size===facet.claimIds.length
        &&facet.claimIds.every(key=>allowed.has(key)), 'Facet status, evidence or explanation is invalid: '+unit.id+'/'+name);
      requireDepth(!['REVIEWED','PARTIAL'].includes(facet.status)||facet.claimIds.length>0,'A reviewed or partial facet needs owned source evidence: '+unit.id+'/'+name);
      requireDepth(!['REVIEWED','PARTIAL'].includes(facet.status)||name==='verification'||facet.claimIds.some(key=>unit.claimIds.includes(key)),
        'A reviewed or partial facet needs evidence owned by this unit; only verification may use explicitly shared oracles: '+unit.id+'/'+name);
      requireDepth(name!=='behavior'||facet.status!=='REVIEWED'||unit.flowIds.length>0,'Reviewed behavior needs a traced existing flow: '+unit.id);
      counts[facet.status]++;
    }
    requireDepth(understandingFacets.some(name=>unit.facets[name].status!=='NOT_APPLICABLE'),'All facets cannot be dismissed as inapplicable: '+unit.id);
    units.set(unit.id,unit);
  }
  for(const flow of flows.values()){
    requireDepth(array(flow.unitIds)&&flow.unitIds.length>0&&new Set(flow.unitIds).size===flow.unitIds.length&&flow.unitIds.every(key=>units.has(key)),
      'Each behavior flow needs its declared owner units: '+flow.id);
    for(const key of flow.unitIds)requireDepth(units.get(key).flowIds.includes(flow.id),'Flow and owner-unit references disagree: '+flow.id+'/'+key);
  }
  for(const unit of units.values())for(const key of unit.flowIds)requireDepth(flows.get(key).unitIds.includes(unit.id),'Unit and flow ownership disagree: '+unit.id+'/'+key);
  const graphUnits=new Set();
  for(const graph of analysis.dependencyGraphs||[])for(const node of graph.nodes||[]){requireDepth(units.has(node.unitId),'Architecture node lacks a declared understanding unit: '+node.id);graphUnits.add(node.unitId);}
  if(understanding.scope==='PROJECT')requireDepth(graphUnits.size===units.size&&[...units.keys()].every(key=>graphUnits.has(key)),
    'Project review inventory and declared architecture units disagree; declare missing units or narrow the review scope');
  const semanticReview=validateSemanticFacets(analysis),origins=validateAtlasOrigins(analysis,index);
  return {semanticReview,origins,status:'ASSESSED_DECLARATIONS',scope:understanding.scope,units:units.size,facets:counts,assessment,
    limitation:'Facet counts describe declared review records, not code coverage or automatic semantic completeness'};
}
