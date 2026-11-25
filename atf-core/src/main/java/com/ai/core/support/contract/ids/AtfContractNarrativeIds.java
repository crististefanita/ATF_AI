package com.ai.core.support.contract.ids;

import com.ai.core.support.contract.ids.narrative.AtfContractNarrativeContractIds;
import com.ai.core.support.contract.ids.narrative.AtfContractNarrativeSuiteIds;
import com.ai.core.support.contract.ids.narrative.AtfExpectedFailureIds;

/**
 * Backward-compatible facade for stable ATF contract narrative ids.
 *
 * <p>The constants live in small thematic classes so Core support remains
 * navigable, while existing usages such as {@code AtfContractNarrativeIds.Suite.X}
 * continue to compile.</p>
 */
public final class AtfContractNarrativeIds {
    private AtfContractNarrativeIds() {
    }

    public static final class Suite extends AtfContractNarrativeSuiteIds {
        private Suite() {
        }
    }

    public static final class Contract extends AtfContractNarrativeContractIds {
        private Contract() {
        }
    }

    public static final class ExpectedFailure extends AtfExpectedFailureIds {
        private ExpectedFailure() {
        }
    }
}
