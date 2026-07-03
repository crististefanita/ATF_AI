package com.endava.ai.skills.atlas.contract.catalog;

import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.AUDIT;
import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.SKILL_READINESS;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.endava.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.candidate;

public final class AtlasSkillAuditRuleCatalog {
    private AtlasSkillAuditRuleCatalog() {
    }

    public static List<AtlasRuleSpec> rules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.SKILL_MIGRATION_READINESS, SKILL_READINESS, MAJOR,
                        "A future Codex skill needs stable input/output and behavioral boundaries.",
                        "Keep the reference clear about source input, read-only behavior, and output HTML."),
                candidate(AtlasRuleIds.Candidate.SKILL_PARAMETERS_GENERIC, SKILL_READINESS, MAJOR,
                        "Skill parameters must remain reusable across applications.",
                        "Avoid domain-specific parameters and keep INPUT_TEST_PACKAGE_ROOT generic."),
                candidate(AtlasRuleIds.Candidate.TEST_AUDIT_EXTENT_LINKED, AUDIT, MAJOR,
                        "The atlas test audit makes the generated output reopenable.",
                        "Publish and link the scoped Atlas test Extent report for this atlas."),
                candidate(AtlasRuleIds.Candidate.TEST_AUDIT_TOTAL_SYNCHRONIZED, AUDIT, BLOCKER,
                        "The Atlas Test Audit count is a preflight truth gate, not a decorative number.",
                        "Before publishing, rerun the exact Atlas contract suite, copy its Extent report, and update the declared tests/failures from that report only."),
                candidate(AtlasRuleIds.Candidate.TEST_AUDIT_EXTENT_SUMMARY_COMMAND, AUDIT, BLOCKER,
                        "Every Extent report link in Atlas Test Audit must be independently understandable and reproducible.",
                        "For each linked Extent HTML, publish passed/failed totals, the exact Maven command, and the failure interpretation in the same audit row."),
                candidate(AtlasRuleIds.Candidate.TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY, AUDIT, BLOCKER,
                        "Playwright and Selenium Atlas runtime evidence must describe the same UI @Test scope.",
                        "Regenerate both UI runtime Extent reports from INPUT_TEST_PACKAGE_ROOT and update section 6 from those artifacts only."),
                candidate(AtlasRuleIds.Candidate.TEST_AUDIT_MANIFEST_COMMAND_SYNC, AUDIT, BLOCKER,
                        "Published aggregate evidence must be reproducible from a command and a manifest, not from prose.",
                        "Keep the atlas HTML, linked Extent report, linked README manifest, and Maven command synchronized.")
        );
    }
}
