package com.endava.ai.skills.mcp.contracts.policy;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.strictCommaSeparated;
import static com.endava.ai.core.support.contract.yaml.YamlScalarLists.strictPipeSeparated;

@Getter
@Accessors(fluent = true)
public final class McpClaimActionContractSpec {
    private final String id;
    private final List<String> verbs;
    private final List<EvidenceGroup> requiredEvidence;
    private final String meaning;
    private final String repair;

    private McpClaimActionContractSpec(String id,
                                       List<String> verbs,
                                       List<EvidenceGroup> requiredEvidence,
                                       String meaning,
                                       String repair) {
        this.id = Objects.requireNonNull(id, "id");
        this.verbs = List.copyOf(verbs);
        this.requiredEvidence = List.copyOf(requiredEvidence);
        this.meaning = Objects.requireNonNull(meaning, "meaning");
        this.repair = Objects.requireNonNull(repair, "repair");
    }

    public static McpClaimActionContractSpec from(Map<String, String> entry, String resourcePath) {
        String id = required(entry, "id", resourcePath);
        return new McpClaimActionContractSpec(
                id,
                strictCommaSeparated(required(entry, "verbs", resourcePath), resourcePath + "#" + id + ".verbs"),
                strictCommaSeparated(required(entry, "requiredEvidence", resourcePath),
                        resourcePath + "#" + id + ".requiredEvidence").stream()
                        .map(value -> EvidenceGroup.from(value, resourcePath, id))
                        .collect(Collectors.toUnmodifiableList()),
                required(entry, "meaning", resourcePath),
                required(entry, "repair", resourcePath));
    }

    public boolean matchesVerb(String verb) {
        return verbs.contains(verb);
    }

    @Getter
    @Accessors(fluent = true)
    public static final class EvidenceGroup {
        private final String scope;
        private final List<String> tokens;

        private EvidenceGroup(String scope, List<String> tokens) {
            this.scope = Objects.requireNonNull(scope, "scope");
            this.tokens = List.copyOf(tokens);
        }

        private static EvidenceGroup from(String value, String resourcePath, String id) {
            int separator = value.indexOf(':');
            if (separator <= 0 || separator == value.length() - 1) {
                throw new IllegalArgumentException("Claim-action evidence group must use scope:token|token in "
                        + resourcePath + " for " + id + ": " + value);
            }
            String scope = value.substring(0, separator).trim();
            if (!List.of("action", "proof", "both").contains(scope)) {
                throw new IllegalArgumentException("Unsupported claim-action evidence scope in "
                        + resourcePath + " for " + id + ": " + scope);
            }
            return new EvidenceGroup(scope,
                    strictPipeSeparated(value.substring(separator + 1), resourcePath + "#" + id + ".requiredEvidence"));
        }
    }
}
