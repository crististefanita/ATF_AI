package com.endava.ai.skills.mcp.validation.run.extent.html;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class McpHtmlTagSnapshot {
    private final String raw;
    private final String element;
    private final int start;
    private final int end;
    private final Map<String, String> attributes;

    public String attribute(String name) {
        return attributes.getOrDefault(name, "");
    }
}
