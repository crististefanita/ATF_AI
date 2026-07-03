package com.endava.ai.skills.mcp.rules.source;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpProtectedRootSpec;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ProtectedSourceSurface {
    private ProtectedSourceSurface() {
    }

    public static Optional<Entry> match(String path) {
        String normalized = normalize(path);
        return entries().stream()
                .filter(entry -> normalized.startsWith(entry.prefix()))
                .findFirst();
    }

    public static List<Entry> entries() {
        return ContractCatalog.protectedRootContracts().stream()
                .map(ProtectedSourceSurface::entry)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Entry entry(McpProtectedRootSpec contract) {
        return new Entry(
                normalize(contract.path()),
                contract.reason(),
                contract.id(),
                contract.owner(),
                contract.allowedAgentAction());
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }

    public static final class Entry {
        private final String prefix;
        private final String reason;
        private final String id;
        private final String owner;
        private final String allowedAgentAction;

        private Entry(String prefix,
                      String reason,
                      String id,
                      String owner,
                      String allowedAgentAction) {
            this.prefix = prefix;
            this.reason = reason;
            this.id = id;
            this.owner = owner;
            this.allowedAgentAction = allowedAgentAction;
        }

        public String prefix() {
            return prefix;
        }

        public String reason() {
            return reason;
        }

        public String id() {
            return id;
        }

        public String owner() {
            return owner;
        }

        public String allowedAgentAction() {
            return allowedAgentAction;
        }
    }
}
