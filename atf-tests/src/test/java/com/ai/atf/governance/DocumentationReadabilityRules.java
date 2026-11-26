package com.ai.atf.governance;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Checks visible guide prose and command disclosure; historical issue prose retains its evidence form. */
final class DocumentationReadabilityRules {
    private static final int MAX_ORIENTATION_SENTENCES = 3;
    private static final int MAX_PARAGRAPH_WORDS = 100;
    private static final int MAX_SENTENCE_WORDS = 45;
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
    private static final Pattern LIST = Pattern.compile("^\\s*(?:[-+*]|\\d+[.)])\\s+.*$");
    private static final Pattern STRUCTURE = Pattern.compile(
            "^\\s*(?:#{1,6}\\s|\\||>|\\[[^]]+]:|(?:[-*_]\\s*){3,}$).*");
    private static final Pattern TAG = Pattern.compile("(?i)<(/?)(details|summary)\\b([^>]*)>");
    private static final Pattern ATTRIBUTE = Pattern.compile(
            "([^\\s=/>]+)(?:\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+))?");
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+(?:[-'][\\p{L}\\p{N}]+)*");
    private static final Pattern ABBREVIATION = Pattern.compile(
            "(?i)\\b(?:e\\.g|i\\.e|vs|Mr|Mrs|Ms|Dr|Prof|No)\\.");
    private static final Pattern INITIALISM = Pattern.compile("\\b(?:[A-Z]\\.){2,}");
    private static final Pattern MAVEN = Pattern.compile(
            "^\\s*(?:(?:PS[^>]*>|\\$|>)\\s*)?(?:&\\s*)?(?:\\.?(?:/|\\\\))?mvnw?(?:\\.cmd|\\.bat)?(?:\\s|$)",
            Pattern.CASE_INSENSITIVE);
    private static final Set<String> SHELL_LANGUAGES = Set.of(
            "powershell", "pwsh", "bash", "sh", "shell", "console", "cmd", "bat", "zsh");

    private DocumentationReadabilityRules() { }

    /** Strategy-specific presentation checks; source truth and semantic review remain separate obligations. */
    static List<String> validateTestingStrategy(List<String> lines) {
        List<String> findings = new ArrayList<>();
        String relative = "docs/Testing.md";
        String fence = null;
        boolean mermaid = false;
        boolean commentOpen = false;
        boolean matrix = false;
        boolean matrixHeadingSeen = false;
        int detailsDepth = 0;
        int flowcharts = 0;
        Map<String, Integer> lanes = new LinkedHashMap<>();
        for (String lane : List.of("API", "Playwright", "Selenium")) lanes.put(lane, 0);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).strip();
            Matcher marker = FENCE.matcher(line);
            if (fence != null) {
                if (marker.matches() && marker.group(1).charAt(0) == fence.charAt(0)
                        && marker.group(1).length() >= fence.length() && marker.group(2).isBlank()) {
                    fence = null;
                } else if (mermaid && !line.startsWith("%%")) {
                    Matcher direction = Pattern.compile("^(?:flowchart|graph)\\s+(LR|RL|TB|TD|BT)\\b").matcher(line);
                    if (direction.find()) {
                        flowcharts++;
                        if (!direction.group(1).equals("LR")) findings.add(relative + ":" + (index + 1)
                                + " DOC-STRATEGY-FLOW observed " + direction.group(1)
                                + "; use flowchart LR for the strategy's sequential or provider map; shorten labels and explain arrows in the adjacent legend");
                    } else if (Pattern.compile("^direction\\s+(?:TB|TD|BT|RL)\\b").matcher(line).find()) {
                        findings.add(relative + ":" + (index + 1)
                                + " DOC-STRATEGY-FLOW a nested direction reverses the horizontal strategy map; keep its flow LR");
                    }
                }
                continue;
            }
            CommentLine visible = withoutComments(line, commentOpen);
            commentOpen = visible.open();
            line = visible.text().strip();
            marker = FENCE.matcher(line);
            if (marker.matches()) {
                fence = marker.group(1);
                mermaid = marker.group(2).strip().equals("mermaid") && detailsDepth == 0;
                continue;
            }
            Matcher tags = TAG.matcher(withoutCodeSpans(line));
            boolean disclosureTag = false;
            while (tags.find()) {
                if (!tags.group(2).equalsIgnoreCase("details")) continue;
                disclosureTag = true;
                detailsDepth = tags.group(1).isEmpty() ? detailsDepth + 1 : Math.max(0, detailsDepth - 1);
            }
            if (disclosureTag || detailsDepth > 0) continue;
            if (line.equals("<a id=\"test-design-contracts\"></a>")) {
                matrix = true;
                matrixHeadingSeen = false;
            } else if (line.startsWith("## ") && matrix) {
                if (matrixHeadingSeen) matrix = false;
                else matrixHeadingSeen = true;
            } else if (matrix && line.startsWith("|")) {
                String[] cells = line.split("\\|", -1);
                String lane = cells.length > 1 ? cells[1].strip() : "";
                if (!lanes.containsKey(lane)) continue;
                lanes.put(lane, lanes.get(lane) + 1);
                // Four corresponding columns keep source ownership, observable oracle and proof limit together.
                if (cells.length != 6 || !cells[2].matches(".*\\[[^]]+][(][^)]+[)].*")
                        || cells[3].isBlank() || cells[4].isBlank()) {
                    findings.add(relative + ":" + (index + 1) + " DOC-STRATEGY-LANES " + lane
                            + " needs four columns: lane, linked design owner, observable oracle, proof limit; names in unrelated prose do not supply this comparison");
                }
            }
        }
        if (flowcharts == 0) findings.add(relative + ":1 DOC-STRATEGY-FLOW include a horizontal Mermaid strategy flow with an adjacent legend");
        for (Map.Entry<String, Integer> lane : lanes.entrySet()) {
            if (lane.getValue() != 1) findings.add(relative + ":1 DOC-STRATEGY-LANES observed " + lane.getValue()
                    + " rows for " + lane.getKey() + "; under test-design-contracts compare API, Playwright and Selenium exactly once with linked design owner, observable oracle and proof limit");
        }
        return findings;
    }

    /** Prose covers maintained operational guides except historical issue records; command checks cover both. */
    static List<String> validate(Path root, Path file, List<String> originalLines) {
        List<String> findings = new ArrayList<>();
        String relative = root.relativize(file).toString().replace('\\', '/');
        boolean inspectProse = !relative.startsWith("docs/feedback/") || relative.equals("docs/feedback/README.md");
        // The strategy owner is a rapid decision guide; detailed contracts retain concise multi-sentence explanations.
        boolean orientation = relative.equals("docs/Testing.md") || file.getFileName().toString().equalsIgnoreCase("README.md");
        Deque<Disclosure> disclosures = new ArrayDeque<>();
        Paragraph paragraph = new Paragraph();
        String fence = null;
        boolean shellFence = false;
        boolean commandChecked = false;
        boolean commentOpen = false;
        boolean listContinuation = false;
        for (int index = 0; index < originalLines.size(); index++) {
            String original = originalLines.get(index);
            Matcher fenceCandidate = FENCE.matcher(original);
            if (fence != null) {
                if (fenceCandidate.matches() && fenceCandidate.group(1).charAt(0) == fence.charAt(0)
                        && fenceCandidate.group(1).length() >= fence.length() && fenceCandidate.group(2).isBlank()) {
                    fence = null;
                } else if (shellFence && !commandChecked && MAVEN.matcher(original).find()) {
                    inspectCommand(relative, index + 1, disclosures.peek(), findings);
                    commandChecked = true;
                }
                continue;
            }
            CommentLine visible = withoutComments(withoutCodeSpans(original), commentOpen);
            commentOpen = visible.open();
            String line = visible.text();
            fenceCandidate = FENCE.matcher(line);
            if (fenceCandidate.matches()) {
                inspectParagraph(relative, paragraph, orientation, findings);
                fence = fenceCandidate.group(1);
                String language = fenceCandidate.group(2).strip().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
                shellFence = SHELL_LANGUAGES.contains(language);
                commandChecked = false;
                listContinuation = false;
                continue;
            }
            String markup = withoutCodeSpans(line);
            if (inspectDisclosures(markup, disclosures)) {
                inspectParagraph(relative, paragraph, orientation, findings);
                listContinuation = false;
                continue;
            }
            if (!disclosures.isEmpty()) {
                Disclosure current = disclosures.peek();
                if (current.summaryOpen) {
                    current.summary.append(' ').append(markup);
                    continue;
                }
                if (disclosures.stream().anyMatch(disclosure -> !disclosure.open)) continue;
            }
            if (!inspectProse) continue;
            if (line.isBlank()) {
                inspectParagraph(relative, paragraph, orientation, findings);
                listContinuation = false;
            } else if (LIST.matcher(line).matches()) {
                inspectParagraph(relative, paragraph, orientation, findings);
                listContinuation = true;
            } else if (STRUCTURE.matcher(line).matches() || line.stripLeading().startsWith("<")) {
                inspectParagraph(relative, paragraph, orientation, findings);
                listContinuation = false;
            } else if (!listContinuation) {
                if (paragraph.start == 0) paragraph.start = index + 1;
                paragraph.text.append(' ').append(line.strip());
            }
        }
        inspectParagraph(relative, paragraph, orientation, findings);
        return findings;
    }

    private static void inspectParagraph(String relative, Paragraph paragraph, boolean orientation, List<String> findings) {
        if (paragraph.start == 0) return;
        String prose = visibleText(paragraph.text.toString());
        String sentenceText = ABBREVIATION.matcher(prose).replaceAll(match -> match.group().replace(".", ""));
        sentenceText = INITIALISM.matcher(sentenceText).replaceAll(match -> match.group().replace(".", ""));
        List<String> sentences = Pattern.compile("(?<=[.!?])[\"'”’)]*\\s+").splitAsStream(sentenceText.strip())
                .filter(sentence -> words(sentence) > 0).collect(Collectors.toList());
        int count = words(prose);
        if (orientation && sentences.size() > MAX_ORIENTATION_SENTENCES || count > MAX_PARAGRAPH_WORDS) {
            findings.add(relative + ":" + paragraph.start + " DOC-PROSE-DENSE observed " + sentences.size()
                    + " sentences and " + count + " visible words; "
                    + (orientation ? "README orientation and the testing strategy allow at most " + MAX_ORIENTATION_SENTENCES + " sentences and " : "guide paragraphs allow at most ")
                    + MAX_PARAGRAPH_WORDS + " words; separate decisions into bullets, comparisons into a table, or relationships into a diagram with a legend; keep secondary rationale in closed details");
        }
        int longest = sentences.stream().mapToInt(DocumentationReadabilityRules::words).max().orElse(0);
        if (longest > MAX_SENTENCE_WORDS) {
            findings.add(relative + ":" + paragraph.start + " DOC-PROSE-SENTENCE observed " + longest
                    + " visible words in one sentence; limit " + MAX_SENTENCE_WORDS
                    + "; separate the decision, precondition and consequence; wrapping the same sentence does not repair it");
        }
        paragraph.text.setLength(0);
        paragraph.start = 0;
    }

    private static void inspectCommand(String relative, int line, Disclosure disclosure, List<String> findings) {
        if (disclosure != null && !disclosure.open && disclosure.summaryClosed && meaningfulSummary(disclosure.summary.toString())) return;
        findings.add(relative + ":" + line + " DOC-COMMAND-DISCLOSURE runnable Maven command is not in closed details with a descriptive summary; wrap the complete shell fence in <details> without any open attribute and name its selection or purpose in <summary>; keep the visible proof boundary outside");
    }

    private static boolean meaningfulSummary(String summary) {
        String text = visibleText(summary).strip();
        return words(text) >= 2 && !text.matches("(?i)(?:more\\s+details|more\\s+commands)");
    }

    private static boolean inspectDisclosures(String line, Deque<Disclosure> disclosures) {
        Matcher tag = TAG.matcher(line);
        int previous = 0;
        boolean matched = false;
        while (tag.find()) {
            if (!disclosures.isEmpty() && disclosures.peek().summaryOpen) {
                disclosures.peek().summary.append(' ').append(line, previous, tag.start());
            }
            boolean closing = !tag.group(1).isEmpty();
            if (tag.group(2).equalsIgnoreCase("details")) {
                if (closing) {
                    if (!disclosures.isEmpty()) disclosures.pop();
                } else {
                    disclosures.push(new Disclosure(hasOpenAttribute(tag.group(3))));
                }
            } else if (!disclosures.isEmpty()) {
                Disclosure current = disclosures.peek();
                current.summaryOpen = !closing;
                if (closing) current.summaryClosed = true;
            }
            previous = tag.end();
            matched = true;
        }
        if (matched && !disclosures.isEmpty() && disclosures.peek().summaryOpen) {
            disclosures.peek().summary.append(' ').append(line.substring(previous));
        }
        return matched;
    }

    static boolean hasOpenAttribute(String attributes) {
        Matcher attribute = ATTRIBUTE.matcher(attributes);
        while (attribute.find()) if (attribute.group(1).equalsIgnoreCase("open")) return true;
        return false;
    }

    private static String visibleText(String text) {
        String links = withoutCodeSpans(text).replaceAll("!?\\[([^]\\r\\n]+)]\\((?:[^()]|\\([^()]*\\))*\\)", "$1");
        String urls = Pattern.compile("https?://[^\\s<>]+").matcher(links).replaceAll(match -> {
            String value = match.group();
            int end = value.length();
            while (end > 0 && ".,!?;:".indexOf(value.charAt(end - 1)) >= 0) end--;
            return " " + value.substring(end);
        });
        return urls.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ").strip();
    }

    private static String withoutCodeSpans(String text) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < text.length();) {
            if (text.charAt(index) != '`') {
                result.append(text.charAt(index++));
                continue;
            }
            int end = index;
            while (end < text.length() && text.charAt(end) == '`') end++;
            int length = end - index;
            int closing = end;
            boolean found = false;
            while (closing < text.length()) {
                if (text.charAt(closing) != '`') { closing++; continue; }
                int candidate = closing;
                while (candidate < text.length() && text.charAt(candidate) == '`') candidate++;
                if (candidate - closing == length) {
                    result.append(" technical ");
                    index = candidate;
                    found = true;
                    break;
                }
                closing = candidate;
            }
            if (!found) { result.append(text, index, end); index = end; }
        }
        return result.toString();
    }

    private static int words(String text) { return (int) WORD.matcher(text).results().count(); }

    private static CommentLine withoutComments(String line, boolean open) {
        StringBuilder result = new StringBuilder();
        int cursor = 0;
        while (cursor < line.length()) {
            int boundary = line.indexOf(open ? "-->" : "<!--", cursor);
            if (boundary < 0) {
                if (!open) result.append(line.substring(cursor));
                break;
            }
            if (!open) result.append(line, cursor, boundary);
            cursor = boundary + (open ? 3 : 4);
            open = !open;
        }
        return new CommentLine(result.toString(), open);
    }

    private static final class CommentLine {
        private final String text;
        private final boolean open;

        private CommentLine(String text, boolean open) { this.text = text; this.open = open; }
        private String text() { return text; }
        private boolean open() { return open; }
    }
    private static final class Paragraph {
        private final StringBuilder text = new StringBuilder();
        private int start;
    }
    private static final class Disclosure {
        private final boolean open;
        private final StringBuilder summary = new StringBuilder();
        private boolean summaryOpen;
        private boolean summaryClosed;

        private Disclosure(boolean open) { this.open = open; }
    }
}
