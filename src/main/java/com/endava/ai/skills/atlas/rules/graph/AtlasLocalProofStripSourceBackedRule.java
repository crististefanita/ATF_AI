package com.endava.ai.skills.atlas.rules.graph;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.report.AtlasReportModel;
import com.endava.ai.skills.atlas.rules.AtlasSourceInventory;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Set;

public final class AtlasLocalProofStripSourceBackedRule implements AtlasValidationRule {
    @Override
    public String id() {
        return AtlasRuleIds.Candidate.LOCAL_PROOF_STRIP_SOURCE_BACKED;
    }

    @Override
    public String expected() {
        return "Every local 3.x proof strip names existing source methods/classes so subgraph evidence is reopenable.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        AtlasReportModel report = AtlasReportModel.parse(context.atlasText());
        if (report.proofStrips().isEmpty()) {
            result.add(id(), "Cannot find Business Flow Understanding section", context.atlasHtml(),
                    "Keep local proof strips in the Business Flow Understanding section.");
            return;
        }

        AtlasSourceInventory source = AtlasSourceInventory.load(context);
        for (AtlasReportModel.ProofStrip proofStrip : report.proofStrips()) {
            String title = proofStrip.title().isBlank() ? "local proof strip" : proofStrip.title();
            Set<String> methods = proofStrip.methods();
            Set<String> classes = proofStrip.classes();

            if (methods.isEmpty() && classes.isEmpty()) {
                result.add(id(), "Local proof strip has no source method/class tokens: " + title,
                        context.atlasHtml(),
                        "Name at least one methodName() or concrete class in every local proof strip.");
                continue;
            }
            if (methods.stream().noneMatch(source::containsMethod)
                    && classes.stream().noneMatch(source::containsClass)) {
                result.add(id(), "Local proof strip tokens are not backed by source: " + title,
                        context.atlasHtml(),
                        "Replace generic or invented proof tokens with method/class names from analyzed source.");
            }
            methods.stream()
                    .filter(method -> !source.containsMethod(method))
                    .forEach(method -> result.add(id(), "Local proof strip method is not found in source: " + method + "()",
                            context.atlasHtml(),
                            "Every methodName() in a 3.x proof strip must exist in sourceRoot or traversed UI layers."));
            classes.stream()
                    .filter(className -> !source.containsClass(className))
                    .forEach(className -> result.add(id(), "Local proof strip class is not found in source: " + className,
                            context.atlasHtml(),
                            "Every class token in a 3.x proof strip must exist in sourceRoot or traversed UI layers."));
        }
        if (report.proofStrips().size() < 4) {
            result.add(id(), "Too few 3.x subgraphs for local proof source audit: " + report.proofStrips().size(),
                    context.atlasHtml(),
                    "Keep several source-backed 3.x subgraphs before claiming candidate atlas quality.");
        }
    }
}
