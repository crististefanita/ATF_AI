package com.ai.atf.governance;

import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bounded language signals and source-bound review declarations, not a semantic language oracle. */
final class DocumentationTranslationRules {
    static final String REVIEW = "docs/governance/translation-review.xml";
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
    private static final Pattern WORD = Pattern.compile("\\p{L}{2,}");

    private DocumentationTranslationRules() { }

    static List<String> validateLanguage(Path root) throws Exception {
        List<String> failures = new ArrayList<>();
        if (!MarkdownDocumentationRules.readmeEdition(root, failures).equals("ro")) return failures;
        for (Path file : GovernanceFiles.files(root, ".md")) {
            String prose = String.join(" ", proseBlocks(Files.readString(file)));
            if (wordCount(prose) >= 12 && !MarkdownDocumentationRules.languageMarkers().matcher(prose).find()) {
                failures.add(GovernanceFiles.relative(root, file)
                        + ":1 DOC-EDITION-LANGUAGE no Romanian prose signal in a declared Romanian document; review the complete file");
            }
        }
        return failures;
    }

    static List<String> validatePair(Path englishRoot, Path romanianRoot) throws Exception {
        List<String> failures = new ArrayList<>();
        for (Path file : GovernanceFiles.files(englishRoot, ".md")) {
            String relative = GovernanceFiles.relative(englishRoot, file);
            Path translated = romanianRoot.resolve(relative);
            if (!Files.isRegularFile(translated)) continue; // The inventory owner reports the missing path.
            Set<String> englishBlocks = new HashSet<>();
            for (String block : proseBlocks(Files.readString(file))) if (wordCount(block) >= 12) englishBlocks.add(block);
            int ordinal = 0;
            for (String block : proseBlocks(Files.readString(translated))) {
                ordinal++;
                if (englishBlocks.contains(block)) failures.add(relative + ":1 DOC-EDITION-UNTRANSLATED unchanged meaningful source prose block="
                        + ordinal + "; inspect the entire translation, not only its title");
            }
        }
        failures.addAll(validateReview(englishRoot));
        failures.addAll(validateReview(romanianRoot));
        return failures;
    }

    static List<String> validateReview(Path root) throws Exception {
        List<String> failures = new ArrayList<>();
        String edition = MarkdownDocumentationRules.readmeEdition(root, failures);
        Path review = root.resolve(REVIEW);
        if (!Files.isRegularFile(review)) return List.of(REVIEW + ":1 DOC-TRANSLATION-REVIEW missing complete source-bound bilingual review declaration");
        Map<String, Path> documents = new LinkedHashMap<>();
        for (Path file : GovernanceFiles.files(root, ".md")) documents.put(GovernanceFiles.relative(root, file), file);
        String living = "docs/Living_Architecture_UI_API_doc_v1_0.html";
        if (Files.isRegularFile(root.resolve(living))) documents.put(living, root.resolve(living));
        try {
            Element contract = GovernanceFiles.pom(review);
            if (!contract.getTagName().equals("translation-review") || !contract.getAttribute("version").equals("1")
                    || !contract.getAttribute("method").equals("source-grounded-full-text")
                    || contract.getAttribute("reviewer").isBlank()
                    || !contract.getAttribute("retrieval-sha256").matches("[a-f0-9]{64}")) {
                failures.add(REVIEW + ":1 DOC-TRANSLATION-REVIEW declare the full-text method, schema and accountable reviewer");
            }
            Set<String> seen = new HashSet<>();
            org.w3c.dom.NodeList entries = contract.getElementsByTagName("document");
            for (int index = 0; index < entries.getLength(); index++) {
                Element entry = (Element) entries.item(index);
                String path = entry.getAttribute("path");
                if (!seen.add(path) || !documents.containsKey(path)) {
                    failures.add(REVIEW + ":1 DOC-TRANSLATION-REVIEW duplicate or unowned review path=" + path);
                    continue;
                }
                if (!entry.getAttribute("status").equals("REVIEWED")
                        || !entry.getAttribute("checks").equals("language meaning conditions facts links diagrams disclosures")) {
                    failures.add(REVIEW + ":1 DOC-TRANSLATION-REVIEW incomplete review dimensions for " + path);
                }
                for (String language : List.of("en", "ro")) {
                    if (!entry.getAttribute(language + "-sha256").matches("[a-f0-9]{64}")) {
                        failures.add(REVIEW + ":1 DOC-TRANSLATION-REVIEW missing exact " + language + " source binding for " + path);
                    }
                }
                if (!digest(Files.readString(documents.get(path))).equals(entry.getAttribute(edition + "-sha256"))) {
                    failures.add(path + ":1 DOC-TRANSLATION-STALE content changed after the declared full-text " + edition + " review");
                }
            }
            for (String path : documents.keySet()) if (!seen.contains(path)) {
                failures.add(path + ":1 DOC-TRANSLATION-REVIEW missing whole-document review entry");
            }
        } catch (Exception invalid) {
            failures.add(REVIEW + ":1 DOC-TRANSLATION-REVIEW unreadable review XML; preserve the original diagnostic and repair its schema");
        }
        return failures;
    }

    /** The Markdown topology owns diagrams; this projection additionally preserves dedicated CSS and linked owners. */
    static List<String> validateHtmlParity(Path english, Path romanian) throws Exception {
        List<String> failures = new ArrayList<>();
        String en = Files.readString(english), ro = Files.readString(romanian);
        String style = "(?is)<style\\b[^>]*>(.*?)</style\\s*>";
        List<String> enStyles = matches(en, style), roStyles = matches(ro, style);
        if (!enStyles.equals(roStyles) || !htmlRoutes(en).equals(htmlRoutes(ro))) {
            failures.add("docs/Living_Architecture_UI_API_doc_v1_0.html:1 DOC-EDITION-PARITY preserve dedicated styles and owner/link routes");
        }
        for (String html : List.of(en, ro)) {
            List<String> styles = matches(html, style);
            Matcher opening = Pattern.compile("(?is)<style\\b([^>]*)>").matcher(html);
            boolean attributes = false;
            while (opening.find()) if (!opening.group(1).isBlank()) attributes = true;
            if (styles.size() != 1 || styles.get(0).isBlank() || attributes) {
                failures.add("docs/Living_Architecture_UI_API_doc_v1_0.html:1 DOC-EDITION-PARITY require one nonempty dedicated style with no attributes");
            }
            Matcher link = Pattern.compile("(?is)<link\\b[^>]*>").matcher(html);
            while (link.find()) {
                Matcher relation = Pattern.compile("(?is)\\brel\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+))").matcher(link.group());
                if (relation.find() && List.of(attributeValue(relation).toLowerCase(java.util.Locale.ROOT).split("\\s+"))
                        .contains("stylesheet")) {
                    failures.add("docs/Living_Architecture_UI_API_doc_v1_0.html:1 DOC-EDITION-PARITY keep dedicated CSS inline without linked stylesheets");
                }
            }
        }
        if (Pattern.compile("(?is)<script\\b").matcher(en).find() || Pattern.compile("(?is)<script\\b").matcher(ro).find()) {
            failures.add("docs/Living_Architecture_UI_API_doc_v1_0.html:1 DOC-EDITION-PARITY keep both Living projections self-contained and script-free");
        }
        return failures;
    }

    private static List<String> htmlRoutes(String source) {
        List<String> routes = new ArrayList<>();
        Matcher attributes = Pattern.compile("(?is)\\b(?:href|src)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'=<>`]+))").matcher(source);
        while (attributes.find()) routes.add(attributeValue(attributes));
        return routes;
    }

    private static String attributeValue(Matcher attribute) {
        for (int group = 1; group <= 3; group++) if (attribute.group(group) != null) return attribute.group(group);
        throw new IllegalStateException("HTML attribute has no parsed value");
    }

    private static List<String> matches(String source, String expression) {
        List<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile(expression).matcher(source);
        while (matcher.find()) result.add(matcher.group(1).replace("\r\n", "\n"));
        return result;
    }

    /** Paragraph/table/list units remain independent of physical wrapping. Literal code is outside translation prose. */
    static List<String> proseBlocks(String source) {
        List<String> outsideFences = new ArrayList<>();
        String fence = null;
        for (String line : source.replace("\r\n", "\n").split("\n", -1)) {
            Matcher marker = FENCE.matcher(line);
            if (fence == null && marker.matches()) { fence = marker.group(1); outsideFences.add(""); continue; }
            if (fence != null) {
                if (marker.matches() && marker.group(1).charAt(0) == fence.charAt(0)
                        && marker.group(1).length() >= fence.length() && marker.group(2).isBlank()) fence = null;
                outsideFences.add("");
                continue;
            }
            outsideFences.add(line);
        }
        List<String> visibleLines = MarkdownDocumentationRules.withoutInlineCode(outsideFences);
        List<String> blocks = new ArrayList<>();
        StringBuilder paragraph = new StringBuilder();
        for (int index = 0; index < outsideFences.size(); index++) {
            String line = outsideFences.get(index);
            String visible = visibleLines.get(index).replaceAll("\\]\\([^)]+\\)", "]")
                    .replaceAll("https?://\\S+", " ").replaceAll("<[^>]+>", " ").strip();
            if (line.isBlank() || line.stripLeading().matches("(?:#{1,6}\\s|\\|).*")
                    || line.stripLeading().startsWith("<")) {
                flush(paragraph, blocks);
                if (!visible.isEmpty()) blocks.add(visible.replaceAll("\\s+", " "));
            } else {
                if (line.stripLeading().matches("(?:[-+*]\\s|\\d+[.)]\\s).*")) flush(paragraph, blocks);
                if (!visible.isEmpty()) paragraph.append(' ').append(visible);
            }
        }
        flush(paragraph, blocks);
        return blocks;
    }

    private static void flush(StringBuilder paragraph, List<String> blocks) {
        if (paragraph.length() > 0) blocks.add(paragraph.toString().strip().replaceAll("\\s+", " "));
        paragraph.setLength(0);
    }

    private static int wordCount(String text) {
        Matcher words = WORD.matcher(text);
        int count = 0;
        while (words.find()) count++;
        return count;
    }

    static String digest(String source) throws Exception {
        StringBuilder result = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(source.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8))) {
            result.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
        }
        return result.toString();
    }
}
