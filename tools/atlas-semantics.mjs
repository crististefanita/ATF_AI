import {requireAtlas} from './atlas-diagnostics.mjs';
const facets=['purpose','structure','behavior','data','configuration','failure','verification'];
const need=(ok,message)=>requireAtlas(ok,'ATLAS-SEMANTICS',message);
const substantive=value=>typeof value==='string'&&value.trim().length>=30;
const overlap=(a,b)=>a.path===b.path&&a.startLine<=b.endLine&&b.startLine<=a.endLine;
const owned=(path,unit)=>unit.scopePaths.some(scope=>path===scope||path.startsWith(scope+'/'));

/** Reviewed means the declared facet scope was investigated, never that a whole source tree is understood. */
export function validateSemanticFacets(analysis,{required=false}={}){
  const understanding=analysis?.understanding;
  if(!understanding?.reviewProfile){need(!required,'Declare SEMANTIC_FACETS_V1 for a semantic qualification');return {status:'NOT_ASSESSED',scope:'LEGACY_DECLARATIONS_ONLY'};}
  need(understanding.reviewProfile==='SEMANTIC_FACETS_V1'&&Array.isArray(understanding.units)&&understanding.units.length>0&&Array.isArray(analysis.claims)&&analysis.claims.every(claim=>claim&&typeof claim.id==='string'),'Unsupported semantic facet profile or missing actual owners');
  const claims=new Map(analysis.claims.map(claim=>[claim.id,claim])),counts={REVIEWED:0,PARTIAL:0,UNKNOWN:0,NOT_APPLICABLE:0};let bindings=0;
  for(const unit of understanding.units){
    need(unit&&Array.isArray(unit.claimIds)&&Array.isArray(unit.sharedClaimIds)&&Array.isArray(unit.scopePaths)&&unit.scopePaths.length>0,'Declare actual owner scopes and owned/shared claims before semantic qualification');
    const explanations=new Set(),allowed=new Set([...unit.claimIds,...unit.sharedClaimIds]);
    for(const name of facets){
      const facet=unit.facets?.[name],review=facet?.review;
      need(facet&&typeof facet.status==='string'&&Object.hasOwn(counts,facet.status)&&substantive(facet.explanation)&&!explanations.has(facet.explanation.trim()),'Explain each semantic facet distinctly instead of repeating a template: '+unit.id+'/'+name);explanations.add(facet.explanation.trim());
      need(review&&substantive(review.scope)&&substantive(review.limits)&&Array.isArray(review.implementationRefs)&&Array.isArray(review.oracleRefs),'Record concrete reviewed scope, remaining limits and source selections: '+unit.id+'/'+name);
      const select=(items,oracle)=>{
        const selected=[],ids=new Set();
        for(const item of items){const claim=claims.get(item.claimId);need(claim&&allowed.has(claim.id)&&facet.claimIds.includes(claim.id)&&['SOURCE_REVIEWED','EXECUTED'].includes(claim.status)&&Array.isArray(item.refIndices)&&item.refIndices.length>0&&new Set(item.refIndices).size===item.refIndices.length,'Facet evidence must use this owner and facet claims: '+unit.id+'/'+name);
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
  return {status:'PASSED',profile:understanding.reviewProfile,units:understanding.units.length,facets:counts,sourceSelections:bindings,scope:'DECLARED_SCOPED_SEMANTIC_REVIEW; HUMAN_MEANING_REVIEW_REQUIRED; NO_AUTOMATIC_COMPLETENESS_OR_SCORE'};
}
