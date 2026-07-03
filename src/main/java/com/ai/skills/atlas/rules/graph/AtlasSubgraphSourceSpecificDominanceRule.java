package com.ai.skills.atlas.rules.graph;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.rules.AtlasSourceInventory;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AtlasSubgraphSourceSpecificDominanceRule implements AtlasValidationRule {
    private static final Pattern RUN_SUFFIX = Pattern.compile("Run\\d{2}.*$");
    private static final Pattern TEST_SUFFIX = Pattern.compile("(Tests?|TestSupport|Support)$");

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.SUBGRAPHS_SOURCE_SPECIFIC_DOMINANCE;
    }

    @Override
    public String expected() {
        return "Business subgraphs are selected from dominant source families in the analyzed test package, not from generic atlas templates.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.atlasText(), "Business Flow Understanding", "Business Evidence Tables");
        if (section.isBlank()) {
            result.add(id(), "Cannot find Business Flow Understanding section", context.atlasHtml(),
                    "Keep source-specific 3.x subgraphs in the Business Flow Understanding section.");
            return;
        }

        AtlasSourceInventory source = AtlasSourceInventory.load(context);
        Set<String> dominantFamilies = dominantFamilies(source);
        if (dominantFamilies.size() < 3) {
            result.add(id(), "Too few dominant source families detected: " + dominantFamilies,
                    context.atlasHtml(),
                    "Analyze a concrete test package with multiple business families before claiming candidate atlas quality.");
            return;
        }

        String lowerSection = section.toLowerCase(Locale.ROOT);
        Set<String> represented = dominantFamilies.stream()
                .filter(family -> lowerSection.contains(family.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        int required = Math.min(5, dominantFamilies.size());
        if (represented.size() < required) {
            result.add(id(), "Business subgraphs are not source-specific enough: represented="
                            + represented + ", dominant=" + dominantFamilies,
                    context.atlasHtml(),
                    "Choose 3.x subgraphs from dominant source families and name their concrete method/class evidence in local proof strips.");
        }
    }

    private static Set<String> dominantFamilies(AtlasSourceInventory source) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String className : source.testClassNames()) {
            String family = sourceFamily(className);
            if (family.length() < 4 || "Example".equals(family)) {
                continue;
            }
            counts.merge(family, 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(8)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String sourceFamily(String className) {
        String family = RUN_SUFFIX.matcher(className).replaceFirst("");
        family = TEST_SUFFIX.matcher(family).replaceFirst("");
        return family;
    }
}
