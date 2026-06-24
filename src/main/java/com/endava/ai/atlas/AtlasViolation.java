package com.endava.ai.atlas;

import java.nio.file.Path;

public final class AtlasViolation {
    private final String code;
    private final String message;
    private final Path path;
    private final String repairHint;

    public AtlasViolation(String code, String message, Path path, String repairHint) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.repairHint = repairHint;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public Path path() {
        return path;
    }

    public String repairHint() {
        return repairHint;
    }

    @Override
    public String toString() {
        String location = path == null ? "" : " [" + path + "]";
        return code + ": " + message + location + " | repair: " + repairHint;
    }
}
