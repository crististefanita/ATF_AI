package com.ai.skills.business.model;

import com.ai.skills.business.taxonomy.*;

import java.util.Objects;

public final class BusinessClaim {
    private final BusinessObject businessObject;
    private final String claim;

    public BusinessClaim(BusinessObject businessObject, String claim) {
        this.businessObject = Objects.requireNonNull(businessObject, "businessObject");
        this.claim = requireText(claim, "claim");
    }

    public BusinessObject businessObject() {
        return businessObject;
    }

    public String claim() {
        return claim;
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return text;
    }
}
