package com.endava.ai.core.reporting.adapters.extent.patch.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ExtentHtmlPatchStage {
    @Getter
    @Accessors(fluent = true)
    private final String id;
    private final Function<ExtentHtmlDocument, List<ExtentHtmlPatchPlan>> planner;

    private ExtentHtmlPatchStage(String id, Function<ExtentHtmlDocument, List<ExtentHtmlPatchPlan>> planner) {
        this.id = Objects.requireNonNull(id, "id");
        this.planner = Objects.requireNonNull(planner, "planner");
    }

    public static ExtentHtmlPatchStage named(
            String id,
            Function<ExtentHtmlDocument, List<ExtentHtmlPatchPlan>> planner
    ) {
        return new ExtentHtmlPatchStage(id, planner);
    }

    public List<ExtentHtmlPatchPlan> plans(ExtentHtmlDocument document) {
        return planner.apply(document);
    }
}
