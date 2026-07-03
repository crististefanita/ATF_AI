package com.ai.skills.atlas;

import java.nio.file.Path;
import lombok.Value;
import lombok.experimental.Accessors;

@Value(staticConstructor = "of")
@Accessors(fluent = true)
public final class AtlasViolation {
    String code;
    String message;
    Path path;
    String repairHint;

    @Override
    public String toString() {
        String location = path == null ? "" : " [" + path + "]";
        return code + ": " + message + location + " | repair: " + repairHint;
    }
}
