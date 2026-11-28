import {requireAtlas} from '../../tools/atlas-diagnostics.mjs';
const need=(ok,message)=>requireAtlas(ok,'ATLAS-SEMANTICS',message);
const text=value=>typeof value==='string'&&value.trim().length>=30;
export const questionDispositions=Object.freeze({SOURCE_BOUND_LIMIT:'Source-bound limit',UNVERIFIED_ENVIRONMENT:'Environment not requalified'});

/** A source answer explains an open question; it does not establish application acceptance. */
export function reviewQuestionSources(analysis){
  const refinement=analysis.understanding?.refinement,rows=[...(refinement?.decisions||[]),...(refinement?.retainedProofs||[])];let questions=0;const identities=new Set();
  for(const row of rows){
    const boundary=row.proofBoundary;if(!boundary)continue;
    const declared=boundary.questionReviewProfile!==undefined,items=boundary.openQuestions||[];
    need(Array.isArray(items),'Keep remaining question records as a structured array');
    need(declared||items.every(q=>q?.review===undefined),'Declare SOURCE_BOUND_QUESTIONS_V1 before adding source answers to remaining questions');
    if(!declared)continue;
    const facet=analysis.understanding.units.find(u=>u.id===row.unitId)?.facets?.[row.facet];
    need(boundary.questionReviewProfile==='SOURCE_BOUND_QUESTIONS_V1'&&facet?.status==='PARTIAL'&&boundary.applicationAcceptance==='NOT_ESTABLISHED'&&items.length>0,'Source question review must retain the actual partial facet, application non-acceptance and every remaining question');
    for(const q of items){
      need(q&&typeof q.id==='string'&&!identities.has(q.id)&&text(q.question)&&text(q.evidenceNeeded),'Keep distinct question identities and original evidence requirements across the reviewed partial facets');identities.add(q.id);
      const review=q.review;need(review&&Object.hasOwn(questionDispositions,review.disposition)&&text(review.answer),'Every declared remaining question needs a bounded source answer and an explicit disposition: '+q.id);
      for(const field of ['implementationRefs','oracleRefs']){
        const selected=review[field],seen=new Set();need(Array.isArray(selected)&&selected.length>0,'A source answer needs actual facet-owned implementation and independent oracle selections: '+q.id);
        for(const ref of selected){const allowed=facet.review[field].find(r=>r.claimId===ref?.claimId),claim=analysis.claims.find(c=>c.id===ref?.claimId);need(allowed&&claim&&['SOURCE_REVIEWED','EXECUTED'].includes(claim.status)&&Array.isArray(ref.refIndices)&&ref.refIndices.length>0,'Do not borrow a source answer from another facet or an unknown claim: '+q.id);
          for(const index of ref.refIndices){const key=ref.claimId+'/'+index;need(Number.isInteger(index)&&allowed.refIndices.includes(index)&&claim.sourceRefs?.[index]&&!seen.has(key),'Select each actual facet fragment once; a green unrelated suite does not answer this question: '+q.id);seen.add(key);}
        }
      }
      questions++;
    }
  }
  return {status:questions?'PASSED':'NOT_ASSESSED',questions,scope:'SOURCE_BOUND_ANSWERS; ORIGINAL_QUESTIONS_AND_PARTIAL_APPLICATION_BOUNDARIES_RETAINED'};
}

export function questionProjection(question){
  const {id,question:meaning,evidenceNeeded,review}=question;
  return {id,question:meaning,evidenceNeeded,...(review?{disposition:questionDispositions[review.disposition],answer:review.answer}:{})};
}
