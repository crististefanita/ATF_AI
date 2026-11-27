import {requireAtlas} from './atlas-diagnostics.mjs';

const need=(value,message)=>requireAtlas(value,'ATLAS-CASES',message);
const object=value=>value!==null&&typeof value==='object'&&!Array.isArray(value);
const text=value=>typeof value==='string'&&value.trim().length>0;
const identity=value=>typeof value==='string'&&/^[a-z][a-z0-9-]*$/.test(value);
const reviewed=claim=>claim&&['SOURCE_REVIEWED','EXECUTED'].includes(claim.status);
const unique=values=>new Set(values).size===values.length;
const textIdentityAllowed=file=>!/^lib\//i.test(file)&&/\.(?:java|mjs|cjs|js|md|xml|json|properties|ya?ml|txt|csv|html|css|ps1)$/.test(file);
const substantial=value=>typeof value==='string'&&value.trim().length>=30;
const comparisonRelations=new Set(['EQUALITY','PREDICATE','FAILURE_IDENTITY','ORDER','OWNERSHIP','CUSTOM']);

function validateCaseComparison(cases,witness,flowId){
  const context=flowId+'/'+witness.id;
  if(cases.comparisonProfile===undefined){
    need(witness.comparison===undefined,'Declare SOURCE_BOUND_COMPARISONS_V1 before adding a case comparison: '+context);
    return;
  }
  if(witness.oracleStatus==='NOT_FOUND'){
    need(witness.comparison===null,'A missing oracle needs an explicit null comparison, not a fabricated observed/expected assertion: '+context);
    return;
  }
  const comparison=witness.comparison;
  need(object(comparison)&&Object.keys(comparison).every(key=>['observedSubject','expectedCriterion','relation','failureMeaning','oracleRefIndices','explanation'].includes(key)),
    'Comparison needs only the declared source-review fields, not invented recorded values: '+context);
  need(['observedSubject','expectedCriterion','failureMeaning'].every(key=>substantial(comparison[key])),
    'Explain the observed subject, independent expected criterion and failure meaning of this exact oracle: '+context);
  need(comparisonRelations.has(comparison.relation),'Declare an existing comparison relation or explain CUSTOM: '+context);
  need(comparison.explanation===undefined||substantial(comparison.explanation),'A comparison explanation must describe its actual source-bound meaning: '+context);
  need(comparison.relation!=='CUSTOM'||substantial(comparison.explanation),'CUSTOM comparison needs its substantive source-bound explanation: '+context);
  need(Array.isArray(comparison.oracleRefIndices)&&comparison.oracleRefIndices.length>0&&unique(comparison.oracleRefIndices)
    &&comparison.oracleRefIndices.every(index=>Number.isInteger(index)&&index>=0&&witness.oracleRefIndices.includes(index)),
    'Bind comparison meaning to nonempty selected independent case-oracle fragments: '+context);
}

function visit(start,adjacency){
  const seen=new Set(start),queue=[...start];
  for(let index=0;index<queue.length;index++)for(const next of adjacency.get(queue[index])||[])if(!seen.has(next)){seen.add(next);queue.push(next);}
  return seen;
}

/** Linear directed reachability; cycles are valid when they can reach an observed outcome. */
export function analyzeDirectedBehavior({nodes,edges,entryNodeId}){
  need(Array.isArray(nodes)&&nodes.length>0&&nodes.every(node=>object(node)&&identity(node.id)&&['INTENT','ACTION','DECISION','STATE','OUTCOME'].includes(node.kind))&&unique(nodes.map(node=>node.id)),
    'A behavior graph needs unique source-declared node identities and behavior node kinds');
  const ids=new Set(nodes.map(node=>node.id));
  need(ids.has(entryNodeId),'Declare an existing behavior entry node');
  need(Array.isArray(edges)&&edges.every(edge=>object(edge)&&identity(edge.id)&&ids.has(edge.from)&&ids.has(edge.to))&&unique(edges.map(edge=>edge.id)),
    'Behavior edges need unique identities and existing endpoints');
  const forward=new Map(nodes.map(node=>[node.id,[]])),reverse=new Map(nodes.map(node=>[node.id,[]]));
  for(const edge of edges){forward.get(edge.from).push(edge.to);reverse.get(edge.to).push(edge.from);}
  const outcomes=nodes.filter(node=>node.kind==='OUTCOME').map(node=>node.id);
  need(outcomes.length>0,'A behavior graph needs an explicit observed outcome');
  const reachable=visit([entryNodeId],forward),productive=visit(outcomes,reverse),sorted=values=>[...values].sort();
  return {entryNodeId,outcomeNodeIds:sorted(outcomes),reachableNodeIds:sorted(reachable),productiveNodeIds:sorted(productive),
    unreachableNodeIds:sorted([...ids].filter(id=>!reachable.has(id))),deadEndNodeIds:sorted([...ids].filter(id=>!productive.has(id))),
    scope:'DIRECTED_DECLARED_GRAPH_REACHABILITY; not program execution or semantic completeness'};
}

/** Validates authored path witnesses and independent oracle fragments without inferring prose truth. */
export function validateAtlasBehavior(analysis,{required=false,sourceIndex}={}){
  need(object(analysis)&&Array.isArray(analysis.claims)&&analysis.claims.every(claim=>object(claim)&&text(claim.id)),
    'Behavior cases need claims and flow records');
  const readerComparisonProfile=analysis.reader?.comparisonProfile;
  need(readerComparisonProfile===undefined||readerComparisonProfile==='SOURCE_BOUND_COMPARISONS_V1',
    'Use the declared SOURCE_BOUND_COMPARISONS_V1 reader comparison profile');
  if(analysis.behaviorFlows===undefined&&!required)return {status:'NOT_ASSESSED',flows:[],scope:'No behavior declaration was supplied by this historical or source-only input'};
  need(Array.isArray(analysis.behaviorFlows)&&(!required||analysis.behaviorFlows.length>0),'Behavior cases need declared flow records for a new complete report');
  let identities=null;
  if(sourceIndex!==undefined){
    need(object(sourceIndex)&&Array.isArray(sourceIndex.files)&&sourceIndex.files.every(file=>object(file)&&text(file.path)&&/^[a-f0-9]{64}$/.test(file.sha256)
      &&(!file.normalizedSha256||/^[a-f0-9]{64}$/.test(file.normalizedSha256))),'Case source identities need the caller-verified source index');
    identities=new Map(sourceIndex.files.map(file=>[file.path,file]));
  }
  const sameSource=(a,b)=>{
    if(a.path===b.path)return true;
    if(!identities)return false;
    const first=identities.get(a.path),second=identities.get(b.path);need(first&&second,'Case source identity is absent from its verified index');
    return first.sha256===second.sha256||analysis.sourceNormalization==='CRLF_LF'&&textIdentityAllowed(a.path)&&textIdentityAllowed(b.path)
      &&first.normalizedSha256&&first.normalizedSha256===second.normalizedSha256;
  };
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim])),flows=[];
  for(const flow of analysis.behaviorFlows){
    need(object(flow),'A behavior flow must be an object');
    const cases=flow.caseAnalysis;
    if(readerComparisonProfile==='SOURCE_BOUND_COMPARISONS_V1')need(object(cases)&&cases.comparisonProfile===readerComparisonProfile,
      'The reader comparison profile requires the same source-bound case profile on every flow: '+flow.id);
    if(cases===undefined){need(!required,'Renderer version 3 requires explicit caseAnalysis: '+flow.id);continue;}
    need(object(cases)&&cases.schemaVersion===1&&identity(cases.entryNodeId)&&['REPRESENTATIVE','ALL_DECLARED_EDGES'].includes(cases.pathScope)
      &&Array.isArray(cases.cases)&&cases.cases.length>0,'Declare case schema, entry, path scope and witnesses: '+flow.id);
    need(cases.comparisonProfile===undefined||cases.comparisonProfile==='SOURCE_BOUND_COMPARISONS_V1',
      'Use the declared SOURCE_BOUND_COMPARISONS_V1 comparison profile: '+flow.id);
    const graph=flow.graph;need(object(graph)&&graph.kind==='BEHAVIOR','Cases require their actual behavior graph: '+flow.id);
    const reach=analyzeDirectedBehavior({...graph,entryNodeId:cases.entryNodeId});
    need(reach.unreachableNodeIds.length===0,'Nodes are unreachable from the declared entry in '+flow.id+': '+reach.unreachableNodeIds.join(', '));
    need(reach.deadEndNodeIds.length===0,'Nodes cannot reach any declared outcome in '+flow.id+': '+reach.deadEndNodeIds.join(', '));
    const nodes=new Map(graph.nodes.map(node=>[node.id,node])),edges=new Map(graph.edges.map(edge=>[edge.id,edge]));
    need(text(flow.oracleClaimId)&&claims.has(flow.oracleClaimId),'Behavior needs an exact oracle claim before case analysis: '+flow.id);
    const allowed=new Set([...(flow.claimIds||[]),flow.oracleClaimId]);
    let ownerClaims=null;
    if(analysis.understanding){
      need(Array.isArray(analysis.understanding.units)&&Array.isArray(flow.unitIds),'Case witnesses need their declared owner units: '+flow.id);
      const owners=analysis.understanding.units.filter(unit=>flow.unitIds.includes(unit.id));
      need(owners.length===flow.unitIds.length,'Case witness references an unknown owner: '+flow.id);
      ownerClaims=new Set(owners.flatMap(unit=>[...(unit.claimIds||[]),...(unit.sharedClaimIds||[])]));
    }
    const sourceIds=values=>Array.isArray(values)&&values.length>0&&unique(values)&&values.every(id=>allowed.has(id)&&reviewed(claims.get(id)));
    const negative=cases.negativeScope;
    need(object(negative)&&['REVIEWED','PARTIAL','UNKNOWN','NOT_APPLICABLE'].includes(negative.status)&&sourceIds(negative.claimIds)&&text(negative.explanation),
      'Negative review scope needs a status, actual searched source claims and explanation: '+flow.id);
    const ids=new Set(),coveredEdges=new Set(),coveredNodes=new Set(),coveredOutcomes=new Set();let positiveCount=0,negativeCount=0,oracleGaps=0;
    for(const witness of cases.cases){
      need(object(witness)&&identity(witness.id)&&!ids.has(witness.id)&&['POSITIVE','NEGATIVE'].includes(witness.kind)
        &&['title','precondition','trigger','expected','assertion','limits'].every(key=>text(witness[key])),
      'Case needs a unique identity, kind, precondition, trigger, expected observation, assertion and limits: '+flow.id);ids.add(witness.id);
      need(sourceIds(witness.claimIds)&&!witness.claimIds.includes(witness.oracleClaimId),
        'Case source claims must be flow-local and separate from its oracle: '+flow.id+'/'+witness.id);
      need(!ownerClaims||witness.claimIds.every(id=>ownerClaims.has(id)),'Case source is outside its declared owner scope: '+witness.id);
      const oracle=claims.get(witness.oracleClaimId);
      need(oracle&&['SOURCE_REVIEWED','EXECUTED','NOT_FOUND'].includes(witness.oracleStatus)
        &&(witness.oracleStatus==='NOT_FOUND'?oracle.status==='UNKNOWN':allowed.has(oracle.id)&&oracle.status===witness.oracleStatus),
        'Case oracle status or declared flow ownership differs: '+flow.id+'/'+witness.id);
      need(Array.isArray(witness.oracleRefIndices)&&unique(witness.oracleRefIndices),'Case oracle fragment indices must be unique: '+witness.id);
      if(witness.oracleStatus==='NOT_FOUND'){
        need(witness.oracleRefIndices.length===0&&object(witness.oracleGap)&&sourceIds(witness.oracleGap.searchedClaimIds)&&text(witness.oracleGap.explanation),
          'A missing oracle needs an explicit searched scope and no invented assertion fragment: '+witness.id);oracleGaps++;
      }else{
        need(!ownerClaims||ownerClaims.has(oracle.id),'Case oracle needs an owned or explicitly shared owner reference: '+witness.id);
        need(witness.oracleRefIndices.length>0&&Array.isArray(oracle.sourceRefs)&&witness.oracleRefIndices.every(index=>Number.isInteger(index)&&index>=0&&index<oracle.sourceRefs.length),
          'Select exact existing oracle source fragments: '+witness.id);
        const implementation=witness.claimIds.flatMap(id=>claims.get(id).sourceRefs||[]);
        need(witness.oracleRefIndices.every(index=>!implementation.some(ref=>sameSource(ref,oracle.sourceRefs[index])&&ref.startLine<=oracle.sourceRefs[index].endLine&&oracle.sourceRefs[index].startLine<=ref.endLine)),
          'An implementation fragment cannot substitute for its own independent oracle: '+witness.id);
        if(witness.oracleStatus==='EXECUTED')need(Array.isArray(oracle.executionSubjects)&&oracle.executionSubjects.length>0
          &&oracle.executionSubjects.every(subject=>object(subject)&&Array.isArray(subject.sourceRefIndices)&&subject.sourceRefIndices.length>0&&unique(subject.sourceRefIndices)
            &&subject.sourceRefIndices.every(index=>Number.isInteger(index)&&index>=0&&index<oracle.sourceRefs.length&&oracle.sourceRefs[index].path===subject.sourcePath))
          &&witness.oracleRefIndices.every(index=>oracle.executionSubjects.some(subject=>subject.sourceRefIndices.includes(index))),
          'An executed case needs retained testcase subjects bound to each selected oracle fragment: '+witness.id);
      }
      validateCaseComparison(cases,witness,flow.id);
      need(Array.isArray(witness.nodeIds)&&witness.nodeIds.length>0&&witness.nodeIds.every(id=>nodes.has(id))
        &&Array.isArray(witness.edgeIds)&&witness.nodeIds.length===witness.edgeIds.length+1
        &&witness.nodeIds[0]===cases.entryNodeId&&nodes.get(witness.nodeIds.at(-1)).kind==='OUTCOME',
        'Case path must start at its declared entry and finish at an observed outcome: '+witness.id);
      for(let index=0;index<witness.edgeIds.length;index++){
        const edge=edges.get(witness.edgeIds[index]);
        need(edge&&edge.from===witness.nodeIds[index]&&edge.to===witness.nodeIds[index+1]
          &&(witness.claimIds.includes(edge.claimId)||witness.oracleStatus!=='NOT_FOUND'&&edge.claimId===witness.oracleClaimId),
          'Case walk, edge direction or selected transition source differs: '+witness.id+'/'+witness.edgeIds[index]);coveredEdges.add(edge.id);
      }
      for(const id of witness.nodeIds)coveredNodes.add(id);coveredOutcomes.add(witness.nodeIds.at(-1));
      if(witness.kind==='POSITIVE')positiveCount++;else negativeCount++;
    }
    need(positiveCount>0,'Retain at least one declared positive observation: '+flow.id);
    need(!['REVIEWED'].includes(negative.status)||negativeCount>0,'Reviewed negative behavior needs an explicit negative witness: '+flow.id);
    need(negative.status!=='NOT_APPLICABLE'||negativeCount===0,'Inapplicable negative scope contradicts a negative witness: '+flow.id);
    need(reach.outcomeNodeIds.every(id=>coveredOutcomes.has(id)),'A declared outcome lacks a case witness: '+flow.id);
    const unwitnessedEdgeIds=[...edges.keys()].filter(id=>!coveredEdges.has(id)).sort(),unwitnessedNodeIds=[...nodes.keys()].filter(id=>!coveredNodes.has(id)).sort();
    need(cases.pathScope!=='ALL_DECLARED_EDGES'||unwitnessedEdgeIds.length===0,'All-edge scope omits declared transitions in '+flow.id+': '+unwitnessedEdgeIds.join(', '));
    flows.push({id:flow.id,pathScope:cases.pathScope,negativeScope:negative.status,cases:cases.cases.length,positiveCases:positiveCount,negativeCases:negativeCount,oracleGaps,
      unwitnessedEdgeIds,unwitnessedNodeIds,reachability:reach});
  }
  for(const unit of analysis.understanding?.units||[])if(unit.facets?.failure?.status==='REVIEWED'&&(required||flows.some(flow=>unit.flowIds?.includes(flow.id))))need(
    flows.some(flow=>unit.flowIds?.includes(flow.id)&&flow.negativeCases>0&&['REVIEWED','PARTIAL'].includes(flow.negativeScope)),
    'A reviewed failure facet needs a declared negative behavior witness: '+unit.id);
  return {status:flows.length?'ASSESSED_DECLARATIONS':'NOT_ASSESSED',flows,oracleIdentityScope:identities?'CALLER_VERIFIED_SOURCE_CONTENT_IDENTITIES':'DECLARED_PATHS_ONLY',scope:'DECLARED_CASE_PATH_AND_ORACLE_BINDINGS; source-to-prose meaning still requires review'};
}
