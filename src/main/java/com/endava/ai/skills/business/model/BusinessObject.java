package com.endava.ai.skills.business.model;

import com.endava.ai.skills.business.taxonomy.*;

import java.util.Objects;

public final class BusinessObject {
    private final String name;
    private final String observableResult;

    public BusinessObject(String name, String observableResult) {
        this.name = requireText(name, "name");
        this.observableResult = requireText(observableResult, "observableResult");
    }

    public String name() {
        return name;
    }

    public String observableResult() {
        return observableResult;
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return text;
    }
}
