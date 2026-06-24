package com.endava.ai.mcp.validation.profile;

import java.io.IOException;

@FunctionalInterface
public interface RunAudit {
    public void accept(String run) throws IOException;
}
