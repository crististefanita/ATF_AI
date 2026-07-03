package com.endava.ai.core.reporting.adapters.extent.patch.model;

import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ExtentHtmlPatchOperation {
    private final String id;
    private final String marker;
    private final Function<String, String> patch;

    public ExtentHtmlPatchOperation(String id, String marker, Function<String, String> patch) {
        this.id = Objects.requireNonNull(id, "id");
        this.marker = marker == null ? "" : marker;
        this.patch = Objects.requireNonNull(patch, "patch");
    }

    public String apply(String html) {
        return patch.apply(html);
    }
}
