package com.ai.skills.business.model;

import com.ai.skills.business.taxonomy.*;

import java.util.Objects;

/**
 * Score row: business goal -> flow question -> source/layer evidence -> atlas score.
 */
public final class BusinessScoreRationale {
    private final String axis;
    private final int score;
    private final String businessGoal;
    private final String flowQuestion;
    private final String sourceLayerEvidence;
    private final String capRationale;

    public BusinessScoreRationale(String axis,
                                  int score,
                                  String businessGoal,
                                  String flowQuestion,
                                  String sourceLayerEvidence,
                                  String capRationale) {
        this.axis = requireText(axis, "axis");
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        this.score = score;
        this.businessGoal = requireText(businessGoal, "businessGoal");
        this.flowQuestion = requireText(flowQuestion, "flowQuestion");
        this.sourceLayerEvidence = requireText(sourceLayerEvidence, "sourceLayerEvidence");
        this.capRationale = requireText(capRationale, "capRationale");
    }

    public String axis() {
        return axis;
    }

    public int score() {
        return score;
    }

    public String businessGoal() {
        return businessGoal;
    }

    public String flowQuestion() {
        return flowQuestion;
    }

    public String sourceLayerEvidence() {
        return sourceLayerEvidence;
    }

    public String capRationale() {
        return capRationale;
    }

    private static String requireText(String value, String field) {
        String text = Objects.requireNonNull(value, field).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return text;
    }
}
