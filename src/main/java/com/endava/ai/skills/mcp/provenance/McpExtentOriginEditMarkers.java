package com.endava.ai.skills.mcp.provenance;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class McpExtentOriginEditMarkers {
    private McpExtentOriginEditMarkers() {
    }

    static List<String> forbiddenTokensIn(String lower) {
        return ContractCatalog.extentProvenanceTokens("origin-edit-token").stream()
                .filter(lower::contains)
                .collect(Collectors.toList());
    }

    static boolean declaresAgentEdit(String lower) {
        return Pattern.compile("\"edited_by_agent\"\\s*:\\s*true").matcher(lower).find()
                || Pattern.compile("\"edited\"\\s*:\\s*true").matcher(lower).find()
                || Pattern.compile("\"post_processed\"\\s*:\\s*true").matcher(lower).find()
                || Pattern.compile("\"postprocessed\"\\s*:\\s*true").matcher(lower).find();
    }
}
