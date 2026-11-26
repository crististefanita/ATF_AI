package com.ai.atf.governance;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Checks diagram presentation in maintained Markdown; semantic truth and rendered geometry require review. */
final class DocumentationPresentationRules {
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
    private static final Pattern FLOW = Pattern.compile("^(?:flowchart|graph)\\s+(LR|RL|TB|TD|BT)\\b");
    private static final Pattern NESTED_DIRECTION = Pattern.compile("^direction\\s+(LR|RL|TB|TD|BT)\\b");
    private static final Pattern DETAILS = Pattern.compile("(?i)^<details(?:\\s[^>]*)?>\\s*$");
    private static final Pattern SUMMARY = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary\\s*>");

    private DocumentationPresentationRules() { }

    static List<String> validate(String relative, List<String> lines) {
        List<String> findings = new ArrayList<>();
        String fence = null;
        boolean mermaid = false;
        boolean comment = false;
        int opening = 0;
        List<String> body = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher marker = FENCE.matcher(line);
            if (fence != null) {
                if (marker.matches() && marker.group(1).charAt(0) == fence.charAt(0)
                        && marker.group(1).length() >= fence.length() && marker.group(2).isBlank()) {
                    if (mermaid) {
                        inspectDirection(relative, opening, body, findings);
                        inspectLegend(relative, lines, index + 1, findings);
                    }
                    fence = null;
                } else body.add(line);
                continue;
            }
            StringBuilder visible = new StringBuilder();
            int cursor = 0;
            while (cursor < line.length()) {
                int boundary = line.indexOf(comment ? "-->" : "<!--", cursor);
                if (boundary < 0) { if (!comment) visible.append(line.substring(cursor)); break; }
                if (!comment) visible.append(line, cursor, boundary);
                cursor = boundary + (comment ? 3 : 4);
                comment = !comment;
            }
            marker = FENCE.matcher(visible);
            if (marker.matches()) {
                fence = marker.group(1);
                mermaid = marker.group(2).strip().equals("mermaid");
                opening = index + 1;
                body.clear();
            }
        }
        return findings;
    }

    private static void inspectDirection(String relative, int line, List<String> body, List<String> findings) {
        boolean header = true;
        for (int offset = 0; offset < body.size(); offset++) {
            String statement = body.get(offset).strip();
            if (statement.isBlank() || statement.startsWith("%%")) continue;
            Matcher direction;
            String scope;
            if (header) {
                direction = FLOW.matcher(statement);
                // Sequence, state and class diagrams preserve their own semantic direction.
                if (!direction.find()) return;
                header = false;
                scope = "flow";
            } else {
                direction = NESTED_DIRECTION.matcher(statement);
                if (!direction.find()) continue;
                scope = "nested direction";
            }
            if (!direction.group(1).equals("LR")) {
                findings.add(relative + ":" + (line + offset + 1) + " DOC-DIAGRAM-ORIENTATION observed "
                        + direction.group(1) + " in " + scope
                        + "; use a compact LR flow for ownership, delivery or evidence; simplify the question before changing geometry");
            }
        }
    }

    private static void inspectLegend(String relative, List<String> lines, int start, List<String> findings) {
        int next = start;
        while (next < lines.size() && lines.get(next).isBlank()) next++;
        boolean valid = next < lines.size() && DETAILS.matcher(lines.get(next).strip()).matches()
                && !DocumentationReadabilityRules.hasOpenAttribute(lines.get(next).strip().substring("<details".length()));
        StringBuilder content = new StringBuilder();
        int depth = 0;
        boolean closed = false;
        if (valid) {
            for (int index = next; index < lines.size(); index++) {
                String value = lines.get(index).strip();
                if (value.matches("(?i)<details(?:\\s[^>]*)?>")) depth++;
                if (value.matches("(?i)</details\\s*>")) {
                    if (--depth == 0) { closed = true; break; }
                }
                content.append(value).append('\n');
            }
        }
        Matcher summary = SUMMARY.matcher(content);
        if (valid && closed && summary.find()) {
            String label = summary.group(1).replaceAll("<[^>]*>", "").strip();
            String explanation = content.substring(summary.end()).replaceAll("<[^>]*>", "").strip();
            valid = label.toLowerCase(java.util.Locale.ROOT).startsWith("legend") && label.length() > 9
                    && !explanation.isBlank();
        } else valid = false;
        if (!valid) findings.add(relative + ":" + (start + 1)
                + " DOC-DIAGRAM-DISCLOSURE the next block must be one closed legend with a descriptive summary and explanation; put arrows, responsibilities and omitted detail immediately below the diagram, before another paragraph or table");
    }
}
