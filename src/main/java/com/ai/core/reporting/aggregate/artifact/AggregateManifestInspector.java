package com.ai.core.reporting.aggregate.artifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AggregateManifestInspector {
    private final String manifest;

    private AggregateManifestInspector(String manifest) {
        this.manifest = manifest == null ? "" : manifest;
    }

    public static AggregateManifestInspector inspect(String manifest) {
        return new AggregateManifestInspector(manifest);
    }

    public String field(String field) {
        Matcher matcher = Pattern.compile("\\| " + Pattern.quote(field) + " \\| `([^`]+)` \\|")
                .matcher(manifest);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Aggregate manifest field must exist: " + field);
        }
        return matcher.group(1);
    }
}
