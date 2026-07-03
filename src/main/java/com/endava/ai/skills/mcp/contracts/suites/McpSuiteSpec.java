package com.endava.ai.skills.mcp.contracts.suites;

import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlEnums.enumValue;

/**
 * Declarative metadata for an MCP TestNG suite class.
 */
@Getter
@Accessors(fluent = true)
public final class McpSuiteSpec {
    private final String id;
    private final String className;
    private final McpProfile profile;
    private final McpRuleCategory category;
    private final String title;
    private final String htmlReq;
    private final String purpose;

    private McpSuiteSpec(String id,
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

    public static McpSuiteSpec from(Map<String, String> entry, String resourcePath) {
        return new McpSuiteSpec(
                required(entry, "id", resourcePath),
                required(entry, "className", resourcePath),
                enumValue(McpProfile.class, required(entry, "profile", resourcePath), "profile", resourcePath, entry),
                enumValue(McpRuleCategory.class, required(entry, "category", resourcePath), "category", resourcePath, entry),
                required(entry, "title", resourcePath),
                required(entry, "htmlReq", resourcePath),
                required(entry, "purpose", resourcePath));
    }

}
