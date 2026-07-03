package com.endava.ai.skills.atlas.contract;

import com.endava.ai.skills.atlas.contract.catalog.AtlasBaseRuleCatalog;
import com.endava.ai.skills.atlas.contract.catalog.AtlasBusinessRuleCatalog;
import com.endava.ai.skills.atlas.contract.catalog.AtlasGraphRuleCatalog;
import com.endava.ai.skills.atlas.contract.catalog.AtlasScoreRuleCatalog;
import com.endava.ai.skills.atlas.contract.catalog.AtlasSkillAuditRuleCatalog;
import com.endava.ai.skills.atlas.contract.catalog.AtlasTraceRuleCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public Atlas contract catalog.
 * <p>
 * This class owns lookup and duplicate protection. Rule declarations are split
 * by responsibility under {@code atlas.contract.catalog}, so callers see a
 * small catalog API instead of a large rule declaration file.
 */
public final class AtlasRuleCatalog {
    private static final List<AtlasRuleSpec> BASE_RULES = Collections.unmodifiableList(AtlasBaseRuleCatalog.rules());
    private static final List<AtlasRuleSpec> CANDIDATE_RULES = candidateRulesInExecutionOrder();
    private static final Map<String, AtlasRuleSpec> BY_ID = index();

    private AtlasRuleCatalog() {
    }

    public static List<AtlasRuleSpec> baseRules() {
        return BASE_RULES;
    }

    public static List<AtlasRuleSpec> candidateRules() {
        return CANDIDATE_RULES;
    }

    public static List<AtlasRuleSpec> allRules() {
        List<AtlasRuleSpec> all = new ArrayList<>(BASE_RULES);
        all.addAll(CANDIDATE_RULES);
        return Collections.unmodifiableList(all);
    }

    public static AtlasRuleSpec require(String id) {
        AtlasRuleSpec spec = BY_ID.get(id);
        if (spec == null) {
            throw new IllegalArgumentException("Atlas rule is missing from AtlasRuleCatalog: " + id);
        }
        return spec;
    }

    private static Map<String, AtlasRuleSpec> index() {
        Map<String, AtlasRuleSpec> indexed = new LinkedHashMap<>();
        allRules().forEach(spec -> {
            AtlasRuleSpec previous = indexed.put(spec.id(), spec);
            if (previous != null) {
                throw new IllegalStateException("Duplicate atlas rule spec: " + spec.id());
            }
        });
        return Collections.unmodifiableMap(indexed);
    }

    private static List<AtlasRuleSpec> candidateRulesInExecutionOrder() {
        List<AtlasRuleSpec> rules = new ArrayList<>();
        rules.addAll(AtlasScoreRuleCatalog.openingRules());
        rules.addAll(AtlasGraphRuleCatalog.openingRules());
        rules.addAll(AtlasScoreRuleCatalog.auditRules());
        rules.addAll(AtlasTraceRuleCatalog.rules());
        rules.addAll(AtlasBusinessRuleCatalog.rules());
        rules.addAll(AtlasSkillAuditRuleCatalog.rules());
        return Collections.unmodifiableList(rules);
    }
}
