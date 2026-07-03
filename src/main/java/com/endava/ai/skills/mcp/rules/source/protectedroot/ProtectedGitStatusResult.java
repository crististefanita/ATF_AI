package com.endava.ai.skills.mcp.rules.source.protectedroot;

import lombok.RequiredArgsConstructor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ProtectedGitStatusResult {
    private final boolean available;
    private final String output;
    private final String diagnostic;

    static ProtectedGitStatusResult available(String output) {
        return new ProtectedGitStatusResult(true, output, "");
    }

    static ProtectedGitStatusResult unavailable(String diagnostic) {
        return new ProtectedGitStatusResult(false, "", diagnostic);
    }
}
