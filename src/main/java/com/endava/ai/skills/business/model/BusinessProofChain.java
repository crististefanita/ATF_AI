package com.endava.ai.skills.business.model;

import com.endava.ai.skills.business.taxonomy.*;

import java.util.List;
import java.util.Objects;

public final class BusinessProofChain {
    private final BusinessClaim claim;
    private final List<String> supportLayers;
    private final List<String> evidenceReferences;
    private final String understandingLimit;

    public BusinessProofChain(BusinessClaim claim,
                              List<String> supportLayers,
                              List<String> evidenceReferences,
                              String understandingLimit) {
        this.claim = Objects.requireNonNull(claim, "claim");
        this.supportLayers = requireNonEmpty(supportLayers, "supportLayers");
        this.evidenceReferences = requireNonEmpty(evidenceReferences, "evidenceReferences");
        this.understandingLimit = requireText(understandingLimit, "understandingLimit");
    }

    public BusinessClaim claim() {
        return claim;
    }

    public List<String> supportLayers() {
        return supportLayers;
    }

    public List<String> evidenceReferences() {
        return evidenceReferences;
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
