package com.endava.ai.skills.mcp.contract.suites;

import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.suites.McpSuiteSpec;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * Runtime view over MCP suite metadata.
 */
@Getter
@Accessors(fluent = true)
public final class McpSuiteMetadata {
    private final String id;
    private final String className;
    private final McpProfile profile;
    private final McpRuleCategory category;
    private final String title;
    private final String htmlReq;
    private final String purpose;

    private McpSuiteMetadata(String id,
                             String className,
                             McpProfile profile,
                             McpRuleCategory category,
                             String title,
                             String htmlReq,
                             String purpose) {
        this.id = Objects.requireNonNull(id, "id");
        this.className = Objects.requireNonNull(className, "className");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.category = Objects.requireNonNull(category, "category");
        this.title = Objects.requireNonNull(title, "title");
        this.htmlReq = Objects.requireNonNull(htmlReq, "htmlReq");
        this.purpose = Objects.requireNonNull(purpose, "purpose");
    }

    public static McpSuiteMetadata fromClass(Class<?> suiteClass) {
        McpSuiteRef ref = suiteClass.getAnnotation(McpSuiteRef.class);
        if (ref != null) {
            McpSuiteSpec spec = ContractCatalog.requireMcpSuite(ref.value());
            if (!suiteClass.getName().equals(spec.className())) {
                throw new IllegalStateException("MCP suite ref " + ref.value()
                        + " points to " + spec.className() + " but is used on " + suiteClass.getName());
            }
            return fromSpec(spec);
        }

        throw new IllegalStateException("Missing @McpSuiteRef YAML metadata on " + suiteClass.getName());
    }

    private static McpSuiteMetadata fromSpec(McpSuiteSpec spec) {
        return new McpSuiteMetadata(
                spec.id(),
                spec.className(),
                spec.profile(),
                spec.category(),
                spec.title(),
                spec.htmlReq(),
                spec.purpose());
    }

}
