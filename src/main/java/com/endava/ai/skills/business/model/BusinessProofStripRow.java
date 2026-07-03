package com.endava.ai.skills.business.model;

import com.endava.ai.skills.business.taxonomy.*;

import java.util.List;
import java.util.Objects;

/**
 * Local graph proof row: node/flow -> method -> layer path -> business evidence -> limit.
 */
public final class BusinessProofStripRow {
    private final String flowOrNode;
    private final String testMethod;
    private final List<String> layerPath;
    private final String businessEvidence;
    private final String understandingLimit;

    public BusinessProofStripRow(String flowOrNode,
                                 String testMethod,
                                 List<String> layerPath,
                                 String businessEvidence,
                                 String understandingLimit) {
        this.flowOrNode = requireText(flowOrNode, "flowOrNode");
        this.testMethod = requireText(testMethod, "testMethod");
        this.layerPath = requireNonEmpty(layerPath, "layerPath");
        this.businessEvidence = requireText(businessEvidence, "businessEvidence");
        this.understandingLimit = requireText(understandingLimit, "understandingLimit");
    }

    public String flowOrNode() {
        return flowOrNode;
    }

    public String testMethod() {
        return testMethod;
    }

    public List<String> layerPath() {
        return layerPath;
    }

    public String businessEvidence() {
        return businessEvidence;
    }

    public String understandingLimit() {
        return understandingLimit;
    }

    private static List<String> requireNonEmpty(List<String> values, String field) {
        Objects.requireNonNull(values, field);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        values.forEach(value -> requireText(value, field));
        return List.copyOf(values);
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return text;
    }
}
