package com.ai.core.reporting.adapters.extent.patch.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class ExtentHtmlPatchPlan {
    private final List<ExtentHtmlPatchOperation> operations;

    private ExtentHtmlPatchPlan(List<ExtentHtmlPatchOperation> operations) {
        this.operations = Collections.unmodifiableList(new ArrayList<>(operations));
    }

    public static ExtentHtmlPatchPlan of(List<ExtentHtmlPatchOperation> operations) {
        return new ExtentHtmlPatchPlan(operations == null ? List.of() : operations);
    }

}
