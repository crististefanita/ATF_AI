package com.endava.ai.skills.mcp.validation.run;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;

@RequiredArgsConstructor
public final class McpRunArchitectureAssertions {
    private final McpPackageAuditValidation audit;

    public void assertDiffTouchedUiTestsRespectLivingArchitecture() throws IOException {
            audit.assertNoBlockers("Diff-touched UI test architecture contract",
                    violation -> violation.code().startsWith("ui-test.architecture."),
                    audit.details(
                            "Asserts: UI files claimed by frozen diffs still exist and respect Living_Architecture_UI_API_doc_v1_0.html.",
                            "Discovery artifacts: review/root-source-thin-no-index.diff and runs/run_##/review/thin-no-index.diff.",
                            "Source truth: current project src/**, because frozen diffs cannot prove BaseTestUI/layer/validation behavior without the Java files.",
                            "Expected: UI test classes extend BaseTestUI and delegate listener/engine lifecycle to the framework.",
                            "Expected: assertions live in validation/assertion classes, with reusable messages for audit/reporting.",
                            "Expected: validation classes that assert must emit StepLogger details so Extent shows expected/actual evidence.",
                            "Expected: UI tests use a declared service/page/actions/validation/component layer, not monolithic inline scripting.",
                            "Forbidden in test classes: @Listeners, DriverManager/UIEngine direct control, StepLogger/ReportingManager direct reporting."));
        }
}
