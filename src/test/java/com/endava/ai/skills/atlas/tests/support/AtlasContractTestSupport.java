package com.endava.ai.skills.atlas.tests.support;

import com.endava.ai.skills.atlas.contract.AtlasContractProfile;
import com.endava.ai.skills.atlas.validation.AtlasAuditValidation;

import java.io.IOException;

public abstract class AtlasContractTestSupport {
    protected final void assertBaseRule(String ruleId) throws IOException {
        assertSuiteProfile(AtlasContractProfile.BASE);
        AtlasAuditValidation.generatedAtlas().assertRulePasses(ruleId);
    }

    protected final void assertCandidateRule(String ruleId) throws IOException {
        assertSuiteProfile(AtlasContractProfile.CANDIDATE);
        AtlasAuditValidation.generatedAtlas().assertCandidateRulePasses(ruleId);
    }

    protected final void publishCandidateRuleMatrix() throws IOException {
        assertSuiteProfile(AtlasContractProfile.CANDIDATE);
        AtlasAuditValidation.generatedAtlas().publishCandidateRuleMatrix();
    }

    private void assertSuiteProfile(AtlasContractProfile expectedProfile) {
        AtlasAuditValidation.assertSuiteProfile(getClass(), expectedProfile);
    }
}
