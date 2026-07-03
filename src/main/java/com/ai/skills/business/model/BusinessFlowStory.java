package com.ai.skills.business.model;

import com.ai.skills.business.taxonomy.*;

import java.util.Objects;

/**
 * Actor -> intent -> visible action -> business object -> observable result.
 */
public final class BusinessFlowStory {
    private final String actor;
    private final String intent;
    private final String visibleAction;
    private final BusinessObject businessObject;
    private final String observableResult;

    public BusinessFlowStory(String actor,
                             String intent,
                             String visibleAction,
                             BusinessObject businessObject,
                             String observableResult) {
        this.actor = requireText(actor, "actor");
        this.intent = requireText(intent, "intent");
        this.visibleAction = requireText(visibleAction, "visibleAction");
        this.businessObject = Objects.requireNonNull(businessObject, "businessObject");
        this.observableResult = requireText(observableResult, "observableResult");
    }

    public String actor() {
        return actor;
    }

    public String intent() {
        return intent;
    }

    public String visibleAction() {
        return visibleAction;
    }

    public BusinessObject businessObject() {
        return businessObject;
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
