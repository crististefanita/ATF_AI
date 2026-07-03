package com.endava.ai.skills.mcp.validation.diagnostics;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpRootCauseGroupSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

public final class McpBlockerRootCause {
    private McpBlockerRootCause() {
    }

    public static List<Summary> summarize(List<McpPackageViolation> blockers) {
        List<Summary> summaries = new ArrayList<>();
        for (Group group : groups()) {
            long count = blockers.stream().filter(group.matcher()).count();
            if (count > 0) {
                summaries.add(new Summary(group.key(), group.label(), count, group.repair()));
            }
        }
        return summaries.stream()
                .sorted(Comparator.comparingLong(Summary::count).reversed())
                .collect(Collectors.toList());
    }

    public static String primaryFocus(List<McpPackageViolation> blockers) {
        List<Summary> summaries = summarize(blockers);
        if (summaries.isEmpty()) {
            return "No dominant MCP root-cause bucket was detected; inspect the first blocker code and artifact path.";
        }
        Summary first = summaries.get(0);
        return first.label() + " (" + first.count() + " blocker(s)); " + first.repair() + ".";
    }

    public static List<String> executableGroupKeys() {
        return McpRootCauseViolationMatchers.executableGroupKeys();
    }

    private static List<Group> groups() {
        return ContractCatalog.rootCauseGroups().stream()
                .map(McpBlockerRootCause::groupFrom)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Group groupFrom(McpRootCauseGroupSpec spec) {
        Predicate<McpPackageViolation> matcher = McpRootCauseViolationMatchers.matcherFor(spec.id());
        if (matcher == null) {
            throw new IllegalStateException("Root-cause YAML group has no executable matcher: " + spec.id());
        }
        return new Group(spec.id(), spec.label(), spec.repair(), matcher);
    }

    @Getter
    @Accessors(fluent = true)
    public static final class Summary {
        private final String key;
        private final String label;
        private final long count;
        private final String repair;

        Summary(String key, String label, long count, String repair) {
            this.key = key;
            this.label = label;
            this.count = count;
            this.repair = repair;
        }
    }

    @Getter
    @Accessors(fluent = true)
    private static final class Group {
        private final String key;
        private final String label;
        private final String repair;
        private final Predicate<McpPackageViolation> matcher;

        private Group(String key,
                      String label,
                      String repair,
                      Predicate<McpPackageViolation> matcher) {
            this.key = key;
            this.label = label;
            this.repair = repair;
            this.matcher = matcher;
        }
    }
}
