package com.ai.skills.business.model;

import com.ai.skills.business.taxonomy.*;

import java.util.List;
import java.util.Objects;

/**
 * Source row that reopens a business claim from class/method and layer evidence.
 */
public final class BusinessSourceTraceRow {
    private final String sourceReference;
    private final BusinessObject businessObject;
    private final List<String> evidenceLayerPath;
    private final BusinessMaturity maturity;
    private final String comprehensionLimit;

    public BusinessSourceTraceRow(String sourceReference,
                                  BusinessObject businessObject,
                                  List<String> evidenceLayerPath,
                                  BusinessMaturity maturity,
                                  String comprehensionLimit) {
        this.sourceReference = requireText(sourceReference, "sourceReference");
        this.businessObject = Objects.requireNonNull(businessObject, "businessObject");
        this.evidenceLayerPath = requireNonEmpty(evidenceLayerPath, "evidenceLayerPath");
        this.maturity = Objects.requireNonNull(maturity, "maturity");
        this.comprehensionLimit = requireText(comprehensionLimit, "comprehensionLimit");
    }

    public String sourceReference() {
        return sourceReference;
    }

    public BusinessObject businessObject() {
        return businessObject;
    }

    public List<String> evidenceLayerPath() {
        return evidenceLayerPath;
    }

    public BusinessMaturity maturity() {
        return maturity;
    }

    public String comprehensionLimit() {
        return comprehensionLimit;
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
