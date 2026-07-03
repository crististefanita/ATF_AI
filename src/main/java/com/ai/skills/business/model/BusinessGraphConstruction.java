package com.ai.skills.business.model;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * Business-first graph construction recipe shared by MCP and Atlas.
 */
@Value
@Accessors(fluent = true)
public class BusinessGraphConstruction {
    BusinessObject businessAnchor;
    String actorIntent;
    BusinessObject primaryOwnedObject;
    List<String> relationshipLabels;
    BusinessObject frontierObject;
    String proofBoundary;

    public BusinessGraphConstruction(BusinessObject businessAnchor,
                                     String actorIntent,
                                     BusinessObject primaryOwnedObject,
                                     List<String> relationshipLabels,
                                     BusinessObject frontierObject,
                                     String proofBoundary) {
        this.businessAnchor = Objects.requireNonNull(businessAnchor, "businessAnchor");
        this.actorIntent = requireText(actorIntent, "actorIntent");
        this.primaryOwnedObject = Objects.requireNonNull(primaryOwnedObject, "primaryOwnedObject");
        this.relationshipLabels = requireNonEmpty(relationshipLabels, "relationshipLabels");
        this.frontierObject = Objects.requireNonNull(frontierObject, "frontierObject");
        this.proofBoundary = requireText(proofBoundary, "proofBoundary");
    }

    public boolean startsFromBusinessObject() {
        return !technicalEvidenceToken(businessAnchor.name())
                && !technicalEvidenceToken(primaryOwnedObject.name())
                && !technicalEvidenceToken(frontierObject.name());
    }

    public boolean hasLabeledBusinessRelationships() {
        return relationshipLabels.stream().allMatch(label -> !technicalEvidenceToken(label));
    }

    public boolean separatesProofBoundary() {
        return technicalEvidenceToken(proofBoundary);
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

    private static boolean technicalEvidenceToken(String value) {
        String lower = value.toLowerCase();
        return lower.contains("run_")
                || lower.contains("testng")
                || lower.contains("extent")
                || lower.contains("xml")
                || lower.contains("mcp")
                || lower.contains("diff")
                || lower.contains("suite");
    }
}
