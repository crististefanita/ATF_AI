package com.endava.ai.skills.atlas.rules.graph.style;

import com.endava.ai.skills.atlas.rules.AtlasText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasGraphRelationshipLabels {
    private static final Pattern EDGE_LABEL_ATTRIBUTE_PATTERN = Pattern.compile(
            "(?is)data-edge-label\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern EDGE_LABEL_ANY_TEXT_PATTERN = Pattern.compile(
            "(?is)<text\\b([^>]*)>(.*?)</text>");
    private static final Pattern EXPLICIT_EDGE_LABEL_PATTERN = Pattern.compile(
            "(?is)(class=[\"'][^\"']*\\b(?:el|edge-label)\\b[^\"']*[\"']|data-edge-label=[\"'][^\"']+[\"'])");
    private static final Pattern NS_CLASS_PATTERN = Pattern.compile("(?is)class=[\"'][^\"']*\\bns\\b[^\"']*[\"']");
    private static final Pattern RELATIONSHIP_VERB_PATTERN = Pattern.compile(
            "(?i)^(owns|anchors|classifies|opens|submits|confirms|reopens|returns|retains|exposes|bounds|limits|keeps|logs|reach|reviews|verify|change|next)\\b");

    private AtlasGraphRelationshipLabels() {
    }

    static int count(String body) {
        return relationshipLabels(body).size();
    }

    static List<String> semantic(String body) {
        List<String> labels = new ArrayList<>();
        relationshipLabels(body).forEach(label -> addSemanticLabel(labels, label));
        Matcher attributeMatcher = EDGE_LABEL_ATTRIBUTE_PATTERN.matcher(body);
        while (attributeMatcher.find()) {
            addSemanticLabel(labels, AtlasText.normalizeLabel(attributeMatcher.group(1)));
        }
        return labels;
    }

    private static List<String> relationshipLabels(String body) {
        List<String> labels = new ArrayList<>();
        Matcher textMatcher = EDGE_LABEL_ANY_TEXT_PATTERN.matcher(body);
        while (textMatcher.find()) {
            String attributes = textMatcher.group(1);
            String label = AtlasText.normalizeLabel(AtlasText.stripTags(textMatcher.group(2)));
            if (isExplicitEdgeLabel(attributes) || isNsRelationshipLabel(attributes, label)) {
                labels.add(label);
            }
        }
        Matcher attributeMatcher = EDGE_LABEL_ATTRIBUTE_PATTERN.matcher(body);
        while (attributeMatcher.find()) {
            labels.add(AtlasText.normalizeLabel(attributeMatcher.group(1)));
        }
        return labels;
    }

    private static boolean isExplicitEdgeLabel(String attributes) {
        return EXPLICIT_EDGE_LABEL_PATTERN.matcher(attributes).find();
    }

    private static boolean isNsRelationshipLabel(String attributes, String label) {
        if (!NS_CLASS_PATTERN.matcher(attributes).find()) {
            return false;
        }
        String normalized = label.toLowerCase(Locale.ROOT);
        if (normalized.contains("maturity") || normalized.contains("boundary")
                || normalized.contains("owned") || normalized.contains("workspace")
                || normalized.contains("visible") || normalized.contains("memory")
                || normalized.contains("surface")) {
            return false;
        }
        return RELATIONSHIP_VERB_PATTERN.matcher(label).find();
    }

    private static void addSemanticLabel(List<String> labels, String label) {
        if (label.isBlank()) {
            return;
        }
        String[] words = label.split("\\s+");
        if (words.length >= 2) {
            labels.add(label);
        }
    }
}
