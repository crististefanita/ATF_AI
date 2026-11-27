import {requireAtlas} from './atlas-diagnostics.mjs';
const facets=['purpose','structure','behavior','data','configuration','failure','verification'];
const need=(ok,message)=>requireAtlas(ok,'ATLAS-SEMANTICS',message);
const substantive=value=>typeof value==='string'&&value.trim().length>=30;
const overlap=(a,b)=>a.path===b.path&&a.startLine<=b.endLine&&b.startLine<=a.endLine;
const owned=(path,unit)=>unit.scopePaths.some(scope=>path===scope||path.startsWith(scope+'/'));

export function semanticBaselineFacets(analysis){
  need(Array.isArray(analysis?.understanding?.units),'The previous dossier needs actual understanding owners');
  const claims=new Map((analysis.claims||[]).map(c=>[c.id,c])),sources=refs=>refs.flatMap(r=>r.refIndices.map(i=>{const source=claims.get(r.claimId)?.sourceRefs?.[i];need(source,'The previous dossier lacks its selected actual source fragment');return source;}));
  return analysis.understanding.units.flatMap(unit=>facets.map(facet=>{const value=unit.facets?.[facet];need(value?.review,'The previous dossier lacks its original facet scope');return {unitId:unit.id,facet,status:value.status,scope:value.review.scope,limits:value.review.limits,implementationRefs:value.review.implementationRefs,oracleRefs:value.review.oracleRefs,implementationSources:sources(value.review.implementationRefs),oracleSources:sources(value.review.oracleRefs)};}));
}

/** The publisher also compares these declared baseline values with the original adjacent audit bytes. */
export function validateSemanticBaseline(analysis,baseline){
  const review=analysis.understanding?.refinement;if(!review)return {status:'NOT_ASSESSED'};
  need(baseline?.analysis?.projectId===analysis.projectId&&baseline.analysis.revision===review.baselineRevision&&JSON.stringify(semanticBaselineFacets(baseline.analysis))===JSON.stringify(review.baselineFacets),'Refinement baseline differs from the actual original project, revision or facet dossier');
  return {status:'PASSED',scope:'ORIGINAL_BASELINE_DOSSIER_COMPARED; NO_AUTOMATIC_SEMANTIC_PROMOTION'};
}

function refinement(analysis){
  const review=analysis.understanding.refinement;if(!review)return {status:'NOT_ASSESSED'};
  need(review.schemaVersion===1&&typeof review.baselineRevision==='string'&&/^[a-f0-9]{64}$/.test(review.baselineSha256||'')&&review.baselineDocument==='semantic-baseline.json'&&analysis.documents?.some(d=>d.path===review.baselineDocument&&d.sha256===review.baselineSha256)&&Array.isArray(review.baselineFacets)&&Array.isArray(review.decisions),'Bind refinement to its original baseline document, digest and revision');
  const baseline=new Map();for(const row of review.baselineFacets){const key=row?.unitId+'/'+row?.facet;need(row&&facets.includes(row.facet)&&!baseline.has(key)&&['REVIEWED','PARTIAL','UNKNOWN','NOT_APPLICABLE'].includes(row.status)&&substantive(row.scope)&&substantive(row.limits)&&Array.isArray(row.implementationRefs)&&Array.isArray(row.oracleRefs)&&Array.isArray(row.implementationSources)&&Array.isArray(row.oracleSources),'Preserve unique original baseline facet states, scopes, limits and actual fragments');baseline.set(key,row);}
  const partial=[...baseline].filter(([,row])=>row.status==='PARTIAL'),decisions=new Set();
  need(review.verificationProfile===undefined||review.verificationProfile==='BOUNDED_OBSERVATIONS_V1','Use the declared bounded-observation verification profile');
  need(review.verificationProfile==='BOUNDED_OBSERVATIONS_V1'||review.decisions.every(d=>d?.proofBoundary===undefined),'Declare the bounded-observation profile before displaying a component proof boundary');
  for(const decision of review.decisions){const key=decision?.unitId+'/'+decision?.facet,previous=baseline.get(key),unit=analysis.understanding.units.find(u=>u.id===decision?.unitId),current=unit?.facets?.[decision?.facet];
    need(previous?.status==='PARTIAL'&&current&&!decisions.has(key)&&substantive(decision.question)&&substantive(decision.answer),'Give every previous partial facet one actual investigated question and decision');decisions.add(key);
    need(decision.baselineScope===previous.scope&&decision.baselineLimits===previous.limits&&current.review.scope.includes(previous.scope)&&current.review.limits.includes(previous.limits),'Do not close a partial facet by deleting or narrowing its original scope or limits: '+key);
    if(review.verificationProfile==='BOUNDED_OBSERVATIONS_V1'){
      const boundary=decision.proofBoundary;
      need(boundary&&['COMPONENT','SOURCE_REVIEW'].includes(boundary.level)&&boundary.applicationAcceptance==='NOT_ESTABLISHED'&&Array.isArray(boundary.claimIds)&&boundary.claimIds.length>0&&new Set(boundary.claimIds).size===boundary.claimIds.length&&Array.isArray(boundary.nonClaims)&&boundary.nonClaims.length>0&&boundary.nonClaims.every(substantive),'Distinguish observed component contracts, source review and explicit application non-claims: '+key);
      for(const id of boundary.claimIds){const claim=analysis.claims.find(c=>c.id===id);need(claim&&current.review.oracleRefs.some(r=>r.claimId===id),'The proof boundary must cite an actual selected independent facet oracle: '+key);
        if(boundary.level==='COMPONENT')need(claim.status==='EXECUTED'&&claim.verdict==='PASSED','A component proof boundary needs its actual passing execution; source text or an unrelated green suite is insufficient: '+key);
        else need(claim.status==='SOURCE_REVIEWED','A source-only boundary must preserve its actual source-review status: '+key);
      }
      need(current.status!=='REVIEWED'||boundary.level==='COMPONENT','Source-only observation cannot silently become a component-qualified promotion: '+key);
    }
    for(const [field,oldRefs]of [['implementationRefs',previous.implementationRefs],['oracleRefs',previous.oracleRefs]]){
      const added=decision[field];need(Array.isArray(added)&&(field!=='oracleRefs'||current.status!=='REVIEWED'||added.length>0)&& (field!=='implementationRefs'||added.length>0),'A refinement needs additional owned source; promotion also needs an additional independent existing oracle: '+key);
      for(const item of added)need(item&&Array.isArray(item.refIndices)&&item.refIndices.length>0&&item.refIndices.every(index=>{const source=analysis.claims.find(c=>c.id===item.claimId)?.sourceRefs?.[index],oldSources=previous[field==='implementationRefs'?'implementationSources':'oracleSources'];return source&&current.review[field].some(r=>r.claimId===item.claimId&&r.refIndices.includes(index))&&!oldRefs.some(r=>r.claimId===item.claimId&&r.refIndices.includes(index))&&!oldSources.some(old=>old.path===source.path&&old.startLine===source.startLine&&old.endLine===source.endLine||old.quote&&source.quote&&old.quote.replaceAll('\r\n','\n')===source.quote.replaceAll('\r\n','\n'));}),'Refinement evidence must add actual selected fragments rather than recycle or rename the previous receipt: '+key);
    }
  }
  need(partial.length===decisions.size&&partial.every(([key])=>decisions.has(key)),'Investigate every original partial facet; do not drop inconvenient baseline questions');
  return {status:'PASSED',previousPartial:partial.length,decisions:decisions.size,remainingPartial:review.decisions.filter(d=>analysis.understanding.units.find(u=>u.id===d.unitId).facets[d.facet].status==='PARTIAL').length,scope:'DECLARED_PROGRESS_WITH_RETAINED_BOUNDARIES; ORIGINAL_BASELINE_CHECK_REQUIRED'};
}

export function facetProofProjection(analysis,unitId,facet){
  if(analysis.understanding?.refinement?.verificationProfile!=='BOUNDED_OBSERVATIONS_V1')return {proof:'',nonClaims:[]};
  const boundary=analysis.understanding?.refinement?.decisions?.find(d=>d.unitId===unitId&&d.facet===facet)?.proofBoundary;
  if(!boundary)return {proof:'',nonClaims:[]};
  return {proof:boundary.level==='COMPONENT'?'Local component checks; application acceptance is not established.':'Source review; no component execution inferred for the additional observation.',nonClaims:boundary.nonClaims};
}

/** Reviewed means the declared facet scope was investigated, never that a whole source tree is understood. */
export function validateSemanticFacets(analysis,{required=false}={}){
  const understanding=analysis?.understanding;
  if(!understanding?.reviewProfile){need(!required,'Declare SEMANTIC_FACETS_V1 for a semantic qualification');return {status:'NOT_ASSESSED',scope:'LEGACY_DECLARATIONS_ONLY'};}
  need(understanding.reviewProfile==='SEMANTIC_FACETS_V1'&&Array.isArray(understanding.units)&&understanding.units.length>0&&Array.isArray(analysis.claims)&&analysis.claims.every(claim=>claim&&typeof claim.id==='string'),'Unsupported semantic facet profile or missing actual owners');
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim])),counts={REVIEWED:0,PARTIAL:0,UNKNOWN:0,NOT_APPLICABLE:0};let bindings=0;
  for(const unit of understanding.units){
    need(unit&&Array.isArray(unit.claimIds)&&Array.isArray(unit.sharedClaimIds)&&Array.isArray(unit.scopePaths)&&unit.scopePaths.length>0&&unit.scopePaths.every(p=>typeof p==='string'&&p.length>0),'Declare actual owner scopes and owned/shared claims before semantic qualification');
    const explanations=new Set(),allowed=new Set([...unit.claimIds,...unit.sharedClaimIds]);
    for(const name of facets){
      const facet=unit.facets?.[name],review=facet?.review;
      need(facet&&typeof facet.status==='string'&&Object.hasOwn(counts,facet.status)&&substantive(facet.explanation)&&!explanations.has(facet.explanation.trim()),'Explain each semantic facet distinctly instead of repeating a template: '+unit.id+'/'+name);explanations.add(facet.explanation.trim());
      need(review&&substantive(review.scope)&&substantive(review.limits)&&Array.isArray(review.implementationRefs)&&Array.isArray(review.oracleRefs),'Record concrete reviewed scope, remaining limits and source selections: '+unit.id+'/'+name);
      const select=(items,oracle)=>{
        const selected=[],ids=new Set();
        for(const item of items){need(item&&typeof item.claimId==='string'&&Array.isArray(facet.claimIds),'Select structured facet evidence with actual claim identities');const claim=claims.get(item.claimId);need(claim&&allowed.has(claim.id)&&facet.claimIds.includes(claim.id)&&['SOURCE_REVIEWED','EXECUTED'].includes(claim.status)&&Array.isArray(item.refIndices)&&item.refIndices.length>0&&new Set(item.refIndices).size===item.refIndices.length,'Facet evidence must use this owner and facet claims: '+unit.id+'/'+name);
          for(const index of item.refIndices){const ref=claim.sourceRefs?.[index],key=item.claimId+'/'+index;need(Number.isInteger(index)&&ref&&!ids.has(key),'Select unique actual source fragments for the semantic facet');ids.add(key);need(oracle||owned(ref.path,unit),'Implementation evidence belongs to another owner; keep it explicitly shared instead of promoting this facet');selected.push(ref);bindings++;
            if(oracle&&claim.status==='EXECUTED')need(['PASSED','FAILED'].includes(claim.verdict)&&claim.executionSubjects?.some(subject=>subject.sourcePath===ref.path&&subject.sourceRefIndices?.includes(index)),'An executed facet oracle needs its original testcase-to-fragment binding');
          }
        }return selected;
      };
      const implementation=select(review.implementationRefs,false),oracles=select(review.oracleRefs,true);
      need(!implementation.some(ref=>oracles.some(oracle=>overlap(ref,oracle))),'An implementation cannot be its own independent semantic oracle: '+unit.id+'/'+name);
      if(facet.status==='REVIEWED'){need(implementation.length>0,'Reviewed scope needs owned implementation or declaration evidence');if(!['purpose','structure'].includes(name))need(oracles.length>0,'Reviewed behavioral/data/configuration/failure/verification scope needs an independent existing oracle');}
      counts[facet.status]++;
    }
  }
  return {status:'PASSED',profile:understanding.reviewProfile,units:understanding.units.length,facets:counts,sourceSelections:bindings,refinement:refinement(analysis),scope:'DECLARED_SCOPED_SEMANTIC_REVIEW; HUMAN_MEANING_REVIEW_REQUIRED; NO_AUTOMATIC_COMPLETENESS_OR_SCORE'};
}
