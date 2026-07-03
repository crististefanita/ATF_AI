package com.ai.skills.business;

import com.ai.skills.business.taxonomy.BusinessUnderstandingConcept;
import com.ai.skills.business.taxonomy.BusinessUnderstandingInputType;
import com.ai.skills.business.taxonomy.BusinessUnderstandingStage;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Neutral facade for business-understanding semantics shared by MCP and Atlas.
 */
public final class BusinessUnderstandingKernel {
    private BusinessUnderstandingKernel() {
    }

    public static List<BusinessUnderstandingStage> semanticPipeline() {
        return Arrays.stream(BusinessUnderstandingStage.values())
                .sorted(Comparator.comparingInt(BusinessUnderstandingStage::order))
                .collect(Collectors.toUnmodifiableList());
    }

    public static Set<BusinessUnderstandingInputType> supportedInputTypes() {
        return Set.of(BusinessUnderstandingInputType.values());
    }

    public static Set<BusinessUnderstandingConcept> concepts() {
        return Set.of(BusinessUnderstandingConcept.values());
    }

    public static boolean supports(BusinessUnderstandingInputType inputType) {
        return supportedInputTypes().contains(inputType);
    }
}
