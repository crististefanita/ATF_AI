package com.ai.atf.governance;

import java.net.URLDecoder;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/** Narrow, executable documentation contracts, without J0 workspace or style catalogs. */
final class MarkdownDocumentationRules {
    private static final Pattern FENCE = Pattern.compile("^ {0,3}(`{3,}|~{3,})(.*)$");
    private static final Pattern LINK = Pattern.compile("\\[[^]\\r\\n]+]\\((<[^>]+>|(?:[^()\\r\\n]|\\([^()\\r\\n]*\\))+)\\)");
    private static final Pattern REFERENCE_DEFINITION = Pattern.compile("^ {0,3}\\[([^]\\r\\n]+)]:\\s*(?:<([^>]+)>|(\\S+))(?:\\s+.*)?$");
    private static final Pattern REFERENCE = Pattern.compile("!?\\[([^]\\r\\n]+)](?:\\[([^]\\r\\n]*)])?");
    private static final Pattern HEADING = Pattern.compile("^ {0,3}#{1,6}\\s+(.+?)\\s*#*\\s*$");
    private static final Pattern EDITORIAL_ARTIFACT = Pattern.compile("@@CODE_\\d+@@|ZXQ\\d+ZQ|\\u2581");
    private static final Pattern TABLE_NUMBER = Pattern.compile("(?<![\\p{L}\\p{N}_])\\d+(?:[.,/-]\\d+)*(?:%)?");
    private static final Pattern EXPLICIT_ANCHOR = Pattern.compile("<(?:a|h[1-6])\\b[^>]*(?:id|name)=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    // A deterministic guard for known Romanian remnants, not a general language detector.
    private static final Pattern ROMANIAN_PROSE = Pattern.compile(
            "\\b(?:pentru|păstrează|pastreaza|fișiere|fisiere|rularea|rulare|verificările|verificarile|"
                    + "documentație|documentatie|schimbările|schimbarile|configurația|configuratia|"
                    + "proprietarul|responsabilități|responsabilitati|testele|raportului|următoarele|urmatoarele|"
                    + "ghid|pornire|acest|aceste|această|aceasta|pornește|porneste|validează|valideaza|facturile|"
                    + "utilizator|utilizatori|factura|raspuns|răspuns|eroare|incercari|încercări|rezultat|asteapta|așteaptă|reincearca|reîncearcă|validare|"
                    + "browserul|fiecare|după|dupa|fără|fara|înainte|inainte|între|verificare|configurare|"
                    + "configurație|configuratie|sunt|este|și|sau)\\b|[ășțĂȘȚ]",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern DIAGRAM_TYPE = Pattern.compile(
            "^(?:flowchart\\b|graph\\b|sequenceDiagram\\b|classDiagram\\b|stateDiagram(?:-v2)?\\b|"
                    + "erDiagram\\b|gantt\\b|journey\\b|mindmap\\b|timeline\\b|quadrantChart\\b|pie\\b).*");
    private static final String LIVING = "docs/Living_Architecture_UI_API_doc_v1_0";
    private static final List<String> EXAMPLE_READMES = List.of("README.md", "atf-playwright/README.md", "atf-selenium/README.md");
    private static final Pattern FEEDBACK_ID = Pattern.compile("[A-Z]+-\\d{3}");
    private static final String EDITION = "docs/governance/readme-edition.properties";
    private static final List<String> ARTIFACT_GUIDES = List.of(
            "artifacts/README.md", "artifacts/reports/README.md", "artifacts/atlas/README.md");

    private MarkdownDocumentationRules() { }

    static Pattern languageMarkers() { return ROMANIAN_PROSE; }

    static List<String> validate(Path root) throws Exception {
        List<String> violations = new ArrayList<>();
        String edition = readmeEdition(root, violations);
        Map<String, Set<String>> execution = executionInventory(root);
        Set<Path> diagramDocuments = new HashSet<>();
        List<String> required = new ArrayList<>(List.of("README.md", "docs/README.md", "docs/Architecture.md",
                "docs/Testing.md", "docs/Testing_Commands.md", "docs/Development.md", "docs/feedback/README.md",
                "docs/governance/README.md", "docs/governance/Code_And_Documentation_Policy.md", "docs/governance/Failure_Diagnostics.md",
                "atf-atlas/README.md", "atf-atlas/Coverage_Map.md", LIVING + ".md", LIVING + ".html"));
        for (String module : GovernanceFiles.MODULES) required.add(module + "/README.md");
        required.addAll(ARTIFACT_GUIDES);
        required.addAll(List.of("artifacts/atlas/Project_Map_Template.md", "tools/collect-artifacts.mjs", "tools/collect-artifacts.test.mjs",
                "tools/verify-atlas.mjs", "tools/verify-atlas.test.mjs",
                "atf-atlas/Report_Contract.md", "atf-atlas/Knowledge_Base.md", "atf-atlas/assets/atlas.css",
                "atf-atlas/knowledge/atlas-contracts.json", "tools/atlas-report.mjs", "tools/atlas-report.test.mjs",
                "tools/atlas-knowledge.mjs", "tools/atlas-knowledge.test.mjs", "tools/atlas-diagnostics.mjs",
                "tools/atlas-bundle.mjs", "tools/atlas-bundle.test.mjs",
                "tools/atlas-execution.mjs", "tools/atlas-execution.test.mjs",
                "tools/atlas-understanding.mjs", "tools/atlas-understanding.test.mjs",
                "tools/atlas-behavior.mjs", "tools/atlas-behavior.test.mjs"));
        for (String file : required) {
            if (!Files.isRegularFile(root.resolve(file))) violations.add(file + ":1 DOC-MISSING maintain the owning documentation");
        }
        for (Path file : GovernanceFiles.files(root, ".md")) {
            if (file.getFileName().toString().equalsIgnoreCase("AGENTS.md")) {
                violations.add(GovernanceFiles.relative(root, file) + ":1 DOC-AGENT-CONTROL keep project rules in owner documentation, not agent instruction files");
            }
            List<String> lines = Files.readAllLines(file);
            boolean english = !edition.equals("ro");
            List<String> visible = withoutFences(lines, root, file, violations, diagramDocuments, english);
            List<String> proseLines = withoutInlineCode(visible);
            validateLinks(root, file, proseLines, violations);
            if (!GovernanceFiles.relative(root, file).startsWith("docs/feedback/")) {
                validateDocumentedFences(root, file, lines, execution, violations);
                validateStableLifecycle(root, file, proseLines, violations);
            }
            for (int index = 0; index < proseLines.size(); index++) {
                if (REFERENCE_DEFINITION.matcher(proseLines.get(index)).matches()) continue;
                String prose = visibleProse(proseLines.get(index));
                if (english && ROMANIAN_PROSE.matcher(prose).find()) {
                    violations.add(location(root, file, index) + "DOC-LANGUAGE known Romanian prose marker; maintain English documentation");
                }
            }
            validateReadmeStyle(root, file, proseLines, violations);
            validateMarkdownHeadings(root, file, proseLines, violations);
            String relative = GovernanceFiles.relative(root, file);
            if (EXAMPLE_READMES.contains(relative)) {
                if (compilationExamples(lines).stream().noneMatch(example -> !example.isBlank())) {
                    violations.add(relative + ":1 DOC-README-EXAMPLE include a complete public consumer example marked java compile");
                }
                if (!diagramDocuments.contains(file)) {
                    violations.add(relative + ":1 DOC-README-ARCHITECTURE include the actual provider/lifecycle diagram");
                }
            }
            if (GovernanceFiles.MODULES.stream().anyMatch(module -> relative.equals(module + "/README.md"))
                    || relative.equals("atf-atlas/README.md") || ARTIFACT_GUIDES.contains(relative)) {
                for (String section : List.of("Overview", "Ownership", "Verification|Execution")) {
                    if (visible.stream().map(MarkdownDocumentationRules::canonicalReadmeLine)
                            .noneMatch(line -> line.matches("^## (?:" + section + ")(?:\\s.*)?$"))) {
                        violations.add(relative + ":1 DOC-OWNER missing owner section: " + section);
                    }
                }
            }
            if ((relative.equals("README.md") || relative.equals("docs/Architecture.md"))
                    && !diagramDocuments.contains(file)) {
                violations.add(relative + ":1 DOC-DIAGRAM include a maintained module or runtime diagram");
            }
            if (relative.equals("README.md")) {
                String document = String.join("\n", visible);
                for (String module : GovernanceFiles.MODULES) {
                    if (!document.contains(module)) violations.add(relative + ":1 DOC-MODULE missing " + module);
                }
            }
        }
        validateBuildFacts(root, violations);
        validateFeedback(root, violations);
        validateAtlasBoundary(root, violations);
        validateLivingPair(root, violations);
        Path englishPolicy = root.resolve("docs/governance/Code_And_Documentation_Policy.md");
        if (Files.isRegularFile(englishPolicy)
                && !Files.readString(englishPolicy).replaceAll("\\s+", " ").contains("MUST be written in English")) {
            violations.add("docs/governance/Code_And_Documentation_Policy.md:1 DOC-ENGLISH-REQUIREMENT retain the explicit mandatory English source/comment contract");
        }
        return violations;
    }

    static String readmeEdition(Path root, List<String> violations) {
        Properties policy = new Properties();
        try (java.io.Reader input = Files.newBufferedReader(root.resolve(EDITION))) {
            policy.load(input);
            String language = policy.getProperty("language", "");
            long settings = Files.readAllLines(root.resolve(EDITION)).stream().map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#") && !line.startsWith("!")).count();
            if (settings == 1 && policy.size() == 1 && Set.of("en", "ro").contains(language)) return language;
        } catch (java.io.IOException | IllegalArgumentException invalid) {
            // A missing or malformed policy fails closed; it never grants a language exception.
        }
        violations.add(EDITION + ":1 DOC-EDITION declare exactly language=en or language=ro");
        return "en";
    }

    private static String canonicalReadmeLine(String line) {
        return line.replace("## Prezentare generală", "## Overview")
                .replace("## Responsabilități", "## Ownership").replace("## Verificare", "## Verification")
                .replace("## Execuție", "## Execution").replace("## TODO-uri active", "## Active TODOs")
                .replace("## Corecții verificate", "## Verified corrections")
                .replace("Constatare", "Finding").replace("Responsabil", "Owner")
                .replace("Prioritate (1-100)", "Priority (1-100)").replace("Calitate (1-100)", "Quality (1-100)")
                .replace("Stare", "Status").replace("Dovada închiderii", "Closure evidence")
                .replace("Impact / risc / urgență", "Impact / risk / urgency")
                .replace("Corectitudine / verificare / claritate", "Correctness / verification / clarity")
                .replace("Limitele dovezilor", "Evidence scope");
    }

    private static void validateReadmeStyle(Path root, Path file, List<String> lines, List<String> violations) {
        java.util.Deque<Boolean> details = new java.util.ArrayDeque<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (EDITORIAL_ARTIFACT.matcher(line).find()) {
                violations.add(location(root, file, index) + "DOC-EDITORIAL remove unresolved translation or drafting markers from visible prose");
            }
            if (Pattern.compile("(?i)<(?:font|center|script|style)\\b|\\bstyle\\s*=").matcher(line).find()) {
                violations.add(location(root, file, index) + "DOC-README-STYLE use semantic Markdown rather than presentation markup");
            }
            Matcher tags = Pattern.compile("(?i)<(/?)(details|summary)\\b[^>]*>").matcher(line);
            while (tags.find()) {
                boolean closing = tags.group(1).equals("/");
                if (tags.group(2).equalsIgnoreCase("details")) {
                    if (!closing) details.push(false);
                    else if (details.isEmpty() || !details.pop()) {
                        violations.add(location(root, file, index) + "DOC-README-DETAILS balance details and provide a summary");
                    }
                } else if (!closing) {
                    if (details.isEmpty() || details.peek()) {
                        violations.add(location(root, file, index) + "DOC-README-DETAILS summary belongs to one open details block");
                    } else { details.pop(); details.push(true); }
                }
            }
        }
        if (!details.isEmpty()) violations.add(GovernanceFiles.relative(root, file) + ":1 DOC-README-DETAILS close every details block");
    }

    private static void validateMarkdownHeadings(Path root, Path file, List<String> lines, List<String> violations) {
        int titles = 0, previous = 0;
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (!HEADING.matcher(line).matches()) continue;
            String heading = line.stripLeading();
            int level = 0;
            while (level < heading.length() && heading.charAt(level) == '#') level++;
            if (level == 1) titles++;
            if ((previous == 0 && level != 1) || level > previous + 1) {
                violations.add(location(root, file, index) + "DOC-HEADING begin with H1 and descend one heading level at a time");
            }
            previous = level;
        }
        if (titles != 1) violations.add(GovernanceFiles.relative(root, file) + ":1 DOC-TITLE expected one H1, found " + titles);
    }

    /** Compare materialized editions without Git, browser execution or runtime changes. */
    static List<String> validateReadmeParity(Path englishRoot, Path romanianRoot) throws Exception {
        List<String> violations = new ArrayList<>();
        if (!readmeEdition(englishRoot, violations).equals("en") || !readmeEdition(romanianRoot, violations).equals("ro")) {
            violations.add("DOC-EDITION-PAIR expected English source and Romanian destination");
        }
        Map<String, Path> english = new java.util.TreeMap<>();
        Map<String, Path> romanian = new java.util.TreeMap<>();
        for (Path file : GovernanceFiles.files(englishRoot, "")) english.put(GovernanceFiles.relative(englishRoot, file), file);
        for (Path file : GovernanceFiles.files(romanianRoot, "")) romanian.put(GovernanceFiles.relative(romanianRoot, file), file);
        Set<String> inventory = new java.util.TreeSet<>(english.keySet());
        inventory.addAll(romanian.keySet());
        for (String relative : inventory) {
            if (!english.containsKey(relative) || !romanian.containsKey(relative)) {
                violations.add(relative + ": DOC-EDITION-INVENTORY both editions need the same project paths");
                continue;
            }
            if (relative.equals(EDITION)) continue;
            Path en = english.get(relative), ro = romanian.get(relative);
            if (relative.endsWith(".md")) {
                List<String> enLines = Files.readAllLines(en), roLines = Files.readAllLines(ro);
                if (!readmeContract(enLines).equals(readmeContract(roLines))) {
                    violations.add(relative + ": DOC-EDITION-PARITY preserve structure, commands, links and diagram topology");
                }
                List<String> enFacts = readmeProtectedFacts(enLines), roFacts = readmeProtectedFacts(roLines);
                if (!enFacts.equals(roFacts)) {
                    int mismatch = 0;
                    while (mismatch < Math.min(enFacts.size(), roFacts.size())
                            && enFacts.get(mismatch).equals(roFacts.get(mismatch))) mismatch++;
                    violations.add(relative + ": DOC-EDITION-FACTS preserve inline code and numeric table facts; first differing protected item="
                            + (mismatch + 1) + "; English items=" + enFacts.size() + "; Romanian items=" + roFacts.size());
                }
                if (!anchors(roLines).containsAll(anchors(enLines))) {
                    violations.add(relative + ": DOC-EDITION-ANCHOR retain English heading anchors as compatibility aliases");
                }
            } else if (relative.equals(LIVING + ".html")) {
                violations.addAll(DocumentationTranslationRules.validateHtmlParity(en, ro));
            } else {
                byte[] enBytes = Files.readAllBytes(en), roBytes = Files.readAllBytes(ro);
                if (!java.util.Arrays.equals(enBytes, roBytes)) {
                    boolean text = relative.matches(".*\\.(?:java|xml|properties|md|html|json|ya?ml|txt|css|js|mjs|cjs|svg)$")
                            || Set.of(".gitignore", ".gitattributes").contains(relative)
                            || relative.contains("/META-INF/services/");
                    if (!text || !Files.readString(en).replace("\r\n", "\n")
                            .equals(Files.readString(ro).replace("\r\n", "\n"))) {
                        violations.add(relative + ": DOC-EDITION-CONTENT only maintained documentation translations and the edition setting may differ");
                    }
                }
            }
        }
        return violations;
    }

    private static List<String> readmeContract(List<String> lines) {
        List<String> result = new ArrayList<>();
        String fence = null, language = "";
        boolean paragraph = false;
        for (String line : lines) {
            Matcher candidate = FENCE.matcher(line);
            if (candidate.matches() && (fence == null || (candidate.group(1).charAt(0) == fence.charAt(0)
                    && candidate.group(1).length() >= fence.length() && candidate.group(2).isBlank()))) {
                if (fence == null) { fence = candidate.group(1); language = candidate.group(2).strip(); result.add("fence:" + language); }
                else { fence = null; result.add("end-fence"); }
                paragraph = false;
            } else if (fence != null) {
                String content = line;
                if (language.equals("mermaid") && !line.strip().startsWith("%%")) {
                    content = content.replaceAll("(\\w+)\\[[^\\[\\]]*]", "$1[label]").replaceAll("\\|[^|]+\\|", "|label|")
                            .replaceAll("-\\. [^.]+ \\.->", "-. label .->")
                            .replaceAll("(participant \\w+ as ).+", "$1label").replaceAll("(->>|-->>)(\\w+):.*", "$1$2:label");
                }
                result.add("code:" + content);
            } else if (line.strip().matches("<a\\s+(?:id|name)=.*</a>")) {
                // Compatibility aliases preserve incoming links without adding another prose section.
            } else {
                Matcher heading = HEADING.matcher(line);
                String shape = heading.matches() ? "heading:" + line.strip().indexOf(' ')
                        : line.strip().startsWith("|") ? "table:" + line.chars().filter(value -> value == '|').count()
                        : line.strip().matches("(?:[-+*]|\\d+[.)])\\s+.*") ? "list:" + line.replaceAll("\\S.*", "")
                                + line.strip().split("\\s+", 2)[0].replaceAll("\\d+", "n")
                        : line.strip().matches("(?i)</?(?:details|summary)\\b.*") ? line.replaceAll(">.*<", "><")
                        : line.isBlank() ? "" : "paragraph";
                if (!shape.equals("paragraph") || !paragraph) result.add(shape);
                paragraph = shape.equals("paragraph");
                Matcher link = LINK.matcher(line);
                while (link.find()) result.add("link:" + link.group(1));
                Matcher reference = REFERENCE_DEFINITION.matcher(line);
                if (reference.matches()) result.add("reference:" + (reference.group(2) == null ? reference.group(3) : reference.group(2)));
            }
        }
        return result;
    }

    /** Translation changes prose, not technical code spans or quantities in corresponding table rows. */
    private static List<String> readmeProtectedFacts(List<String> lines) {
        List<String> visible = new ArrayList<>();
        String fence = null;
        for (String line : lines) {
            Matcher marker = FENCE.matcher(line);
            if (fence == null && marker.matches()) { fence = marker.group(1); visible.add(""); }
            else if (fence != null && marker.matches() && marker.group(1).charAt(0) == fence.charAt(0)
                    && marker.group(1).length() >= fence.length() && marker.group(2).isBlank()) { fence = null; visible.add(""); }
            else visible.add(fence == null ? line : "");
        }
        String source = String.join("\n", visible);
        List<String> facts = new ArrayList<>();
        for (int[] span : inlineCodeRanges(source)) {
            String payload = source.substring(span[1], span[2]).replace('\n', ' ');
            if (payload.startsWith(" ") && payload.endsWith(" ") && !payload.isBlank()) {
                payload = payload.substring(1, payload.length() - 1);
            }
            facts.add("inline:" + payload);
        }
        int row = 0;
        for (String line : withoutInlineCode(visible)) {
            if (!line.stripLeading().startsWith("|")) continue;
            row++;
            Matcher number = TABLE_NUMBER.matcher(line);
            while (number.find()) facts.add("table-row:" + row + ":number:" + number.group());
        }
        return facts;
    }

    /** Compile declared consumer examples once, separately from ordinary link/factual fixtures. */
    static List<String> validateCompilableExamples(Path root) throws Exception {
        List<String> failures = new ArrayList<>();
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Documentation examples require a JDK compiler");
        for (String relative : EXAMPLE_READMES) {
            Path file = root.resolve(relative);
            if (!Files.isRegularFile(file)) continue;
            for (String source : compilationExamples(Files.readAllLines(file))) {
                String identifiers = JavaSourceScanner.codeOnly(source)
                        .replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
                if (ROMANIAN_PROSE.matcher(identifiers).find()) {
                    failures.add(relative + ": DOC-CODE-LANGUAGE maintain English authored identifiers in examples");
                }
                if (ROMANIAN_PROSE.matcher(JavaSourceScanner.commentsOnly(source)).find()) {
                    failures.add(relative + ": DOC-CODE-LANGUAGE maintain English comments in authored examples");
                }
                if (ROMANIAN_PROSE.matcher(JavaSourceScanner.literalsOnly(source)).find()) {
                    failures.add(relative + ": DOC-CODE-LANGUAGE maintain English authored messages in canonical examples; review external-language data explicitly");
                }
                Matcher name = Pattern.compile("\\bpublic\\s+(?:(?:final|abstract)\\s+)?class\\s+([\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)")
                        .matcher(JavaSourceScanner.codeOnly(source));
                if (!name.find()) {
                    failures.add(relative + ": DOC-JAVA-COMPILE declare one complete public example class");
                    continue;
                }
                try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture();
                     javax.tools.StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.ROOT, StandardCharsets.UTF_8)) {
                    String className = name.group(1);
                    fixture.write(className + ".java", source);
                    Path classes = Files.createDirectory(fixture.root.resolve("classes"));
                    javax.tools.DiagnosticCollector<javax.tools.JavaFileObject> diagnostics = new javax.tools.DiagnosticCollector<>();
                    String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
                    boolean compiled = Boolean.TRUE.equals(compiler.getTask(null, manager, diagnostics,
                            List.of("--release", "11", "-proc:none", "-classpath", classpath, "-d", classes.toString()), null,
                            manager.getJavaFileObjects(fixture.root.resolve(className + ".java").toFile())).call());
                    if (!compiled) {
                        String details = diagnostics.getDiagnostics().stream()
                                .filter(item -> item.getKind() == javax.tools.Diagnostic.Kind.ERROR)
                                .map(item -> item.getMessage(Locale.ROOT)).collect(Collectors.joining("; "));
                        failures.add(relative + ": DOC-JAVA-COMPILE example does not compile against current APIs: " + details);
                    }
                }
            }
        }
        return failures;
    }

    private static List<String> compilationExamples(List<String> lines) {
        List<String> examples = new ArrayList<>();
        String fence = null;
        boolean compile = false;
        StringBuilder body = new StringBuilder();
        for (String line : lines) {
            Matcher candidate = FENCE.matcher(line);
            if (fence == null && candidate.matches()) {
                fence = candidate.group(1);
                compile = candidate.group(2).strip().equals("java compile");
                body.setLength(0);
            } else if (fence != null && candidate.matches()
                    && candidate.group(1).charAt(0) == fence.charAt(0)
                    && candidate.group(1).length() >= fence.length() && candidate.group(2).isBlank()) {
                if (compile) examples.add(body.toString());
                fence = null;
            } else if (fence != null && compile) body.append(line).append('\n');
        }
        return examples;
    }

    /** Resolves only this reactor's documented selection facts, never executes a command. */
    private static Map<String, Set<String>> executionInventory(Path root) throws Exception {
        Map<String, Set<String>> inventory = new HashMap<>();
        inventory.put("modules", new LinkedHashSet<>());
        if (!Files.isRegularFile(root.resolve("pom.xml"))) return inventory;
        org.w3c.dom.Element parent = GovernanceFiles.pom(root.resolve("pom.xml"));
        addPomFacts(inventory, "", parent);
        for (org.w3c.dom.Element entry : GovernanceFiles.children(GovernanceFiles.child(parent, "modules"), "module")) {
            String module = entry.getTextContent().strip();
            Path pom = root.resolve(module).resolve("pom.xml").normalize();
            if (!pom.startsWith(root.normalize()) || !Files.isRegularFile(pom)) continue;
            inventory.get("modules").add(module);
            org.w3c.dom.Element project = GovernanceFiles.pom(pom);
            addPomFacts(inventory, module, project);
            String artifact = GovernanceFiles.text(project, "artifactId", module);
            String group = GovernanceFiles.text(project, "groupId", GovernanceFiles.text(parent, "groupId", ""));
            for (String alias : List.of(module, artifact, ':' + artifact, group + ':' + artifact)) {
                inventory.computeIfAbsent("alias:" + alias, ignored -> new LinkedHashSet<>()).add(module);
            }
        }
        return inventory;
    }

    private static void addPomFacts(Map<String, Set<String>> inventory, String owner, org.w3c.dom.Element pom) {
        Set<String> profiles = inventory.computeIfAbsent("profiles:" + owner, ignored -> new LinkedHashSet<>());
        String suite = GovernanceFiles.text(GovernanceFiles.child(pom, "properties"), "atf.test.suite", "");
        if (!suite.isEmpty()) inventory.put("suite:" + owner, Set.of(suite));
        for (org.w3c.dom.Element profile : GovernanceFiles.children(GovernanceFiles.child(pom, "profiles"), "profile")) {
            String id = GovernanceFiles.text(profile, "id", "");
            profiles.add(id);
            String profileSuite = GovernanceFiles.text(GovernanceFiles.child(profile, "properties"), "atf.test.suite", "");
            if (!profileSuite.isEmpty()) inventory.put("suite:" + owner + ':' + id, Set.of(profileSuite));
        }
    }

    private static void validateDocumentedFences(Path root, Path file, List<String> lines,
                                                Map<String, Set<String>> execution, List<String> violations) throws Exception {
        String fence = null;
        String language = "";
        Set<String> keys = new HashSet<>();
        int defaults = 0;
        int declared = 0;
        Properties actual = new Properties();
        Path properties = root.resolve("atf-core/src/main/resources/framework.properties");
        if (Files.isRegularFile(properties)) {
            try (java.io.InputStream input = Files.newInputStream(properties)) { actual.load(input); }
        }
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher delimiter = FENCE.matcher(line);
            if (fence == null && delimiter.matches()) {
                fence = delimiter.group(1);
                language = delimiter.group(2).strip();
                keys.clear();
                if (language.equals("properties defaults")) defaults++;
            } else if (fence != null && delimiter.matches()
                    && delimiter.group(1).charAt(0) == fence.charAt(0)
                    && delimiter.group(1).length() >= fence.length() && delimiter.group(2).isBlank()) {
                if (language.equals("properties defaults") && keys.isEmpty()) {
                    violations.add(location(root, file, index) + "DOC-DEFAULT declare at least one source-backed default");
                }
                fence = null;
            } else if (fence != null && language.equals("properties defaults")) {
                String property = line.strip();
                if (property.isEmpty() || property.startsWith("#") || property.startsWith("!")) continue;
                Properties entry = new Properties();
                try { entry.load(new StringReader(property)); }
                catch (IllegalArgumentException invalid) {
                    violations.add(location(root, file, index) + "DOC-DEFAULT invalid literal property declaration");
                    continue;
                }
                for (String key : entry.stringPropertyNames()) {
                    declared++;
                    if (!keys.add(key)) violations.add(location(root, file, index) + "DOC-DEFAULT duplicate default key " + key);
                    if (!actual.containsKey(key) || !actual.getProperty(key).equals(entry.getProperty(key))) {
                        violations.add(location(root, file, index) + "DOC-DEFAULT " + key
                                + " differs from atf-core/src/main/resources/framework.properties");
                    }
                }
            } else if (fence != null && Set.of("powershell", "bash", "sh", "shell").contains(language)
                    && line.stripLeading().startsWith("mvn ")) {
                validateMavenSelection(root, file, index, line.strip(), execution, violations);
            }
        }
        if (GovernanceFiles.relative(root, file).equals(LIVING + ".md") && (defaults == 0 || declared == 0)) {
            violations.add(LIVING + ".md:1 DOC-DEFAULT retain explicitly declared configuration defaults");
        }
    }

    /** Single-line, quote-aware Maven arguments; this is not a general shell interpreter. */
    private static List<String> commandArguments(String command) {
        List<String> arguments = new ArrayList<>();
        StringBuilder argument = new StringBuilder();
        char quote = 0;
        for (int index = 0; index < command.length(); index++) {
            char value = command.charAt(index);
            if (quote != 0) {
                if (value == quote) quote = 0; else argument.append(value);
            } else if (value == '\'' || value == '"') quote = value;
            else if (value == '#' && argument.length() == 0) break;
            else if (value == ';' || value == '|' || value == '&' || value == '`') return List.of();
            else if (Character.isWhitespace(value)) {
                if (argument.length() != 0) { arguments.add(argument.toString()); argument.setLength(0); }
            } else argument.append(value);
        }
        if (quote != 0) return List.of();
        if (argument.length() != 0) arguments.add(argument.toString());
        return arguments;
    }

    private static void validateMavenSelection(Path root, Path file, int line, String command,
                                              Map<String, Set<String>> execution, List<String> violations) throws Exception {
        List<String> arguments = commandArguments(command);
        String location = location(root, file, line) + "DOC-COMMAND ";
        if (arguments.isEmpty()) {
            violations.add(location + "use a complete single-line command without shell chaining");
            return;
        }
        String owner = "";
        List<String> projects = new ArrayList<>();
        List<String> profiles = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();
        for (int index = 1; index < arguments.size(); index++) {
            String argument = arguments.get(index);
            if (argument.startsWith("-f") && argument.length() > 2
                    && !Set.of("-fae", "-ff", "-fn").contains(argument)) argument = "-f=" + argument.substring(2).replaceFirst("^=", "");
            if (Set.of("-f", "--file", "-pl", "--projects", "-P", "--activate-profiles").contains(argument)) {
                if (++index == arguments.size()) { violations.add(location + "missing value after " + argument); return; }
                argument += '=' + arguments.get(index);
            }
            if (argument.startsWith("-f=") || argument.startsWith("--file=")) {
                try {
                    Path pom = root.resolve(argument.substring(argument.indexOf('=') + 1)).normalize();
                    if (Files.isDirectory(pom)) pom = pom.resolve("pom.xml");
                    String relative = pom.startsWith(root.normalize()) ? GovernanceFiles.relative(root, pom) : "";
                    String module = relative.endsWith("/pom.xml") ? relative.substring(0, relative.length() - 8) : "";
                    if (!Files.isRegularFile(pom) || !(relative.equals("pom.xml") || execution.get("modules").contains(module))) {
                        violations.add(location + "selected POM is not a current reactor owner");
                        return;
                    }
                    owner = module;
                } catch (IllegalArgumentException invalid) {
                    violations.add(location + "invalid selected POM path");
                    return;
                }
            } else if (argument.startsWith("-pl") || argument.startsWith("--projects=")) {
                String value = argument.startsWith("-pl") ? argument.substring(3).replaceFirst("^=", "")
                        : argument.substring(argument.indexOf('=') + 1);
                projects.addAll(List.of(value.split(",", -1)));
            } else if (argument.startsWith("-P") || argument.startsWith("--activate-profiles=")) {
                String value = argument.startsWith("-P") ? argument.substring(2).replaceFirst("^=", "")
                        : argument.substring(argument.indexOf('=') + 1);
                profiles.addAll(List.of(value.split(",", -1)));
            } else if (argument.startsWith("-D")) {
                int equals = argument.indexOf('=');
                properties.put(equals < 0 ? argument.substring(2) : argument.substring(2, equals),
                        equals < 0 ? "true" : argument.substring(equals + 1));
            }
        }
        Set<String> selected = owner.isEmpty() ? new LinkedHashSet<>(execution.get("modules")) : new LinkedHashSet<>(Set.of(owner));
        if (!projects.isEmpty()) {
            boolean hasIncludes = projects.stream().anyMatch(value -> !value.startsWith("!"));
            if (hasIncludes) selected.clear();
            for (String project : projects) {
                boolean exclude = project.startsWith("!");
                String alias = exclude ? project.substring(1) : project;
                Set<String> matches = execution.getOrDefault("alias:" + alias, Set.of());
                if (matches.size() != 1 || !owner.isEmpty() && !matches.contains(owner)) {
                    violations.add(location + "selected project is not in this POM's reactor: " + project);
                } else if (exclude) selected.removeAll(matches); else selected.addAll(matches);
            }
        }
        Set<String> availableProfiles = new HashSet<>(execution.getOrDefault("profiles:", Set.of()));
        for (String module : selected) availableProfiles.addAll(execution.getOrDefault("profiles:" + module, Set.of()));
        for (String profile : profiles) {
            String id = profile.replaceFirst("^[!-]", "");
            if (!availableProfiles.contains(id)) violations.add(location + "profile is not declared for the selected reactor: " + profile);
        }
        if (properties.containsKey("test")) {
            validateTestSelection(root, properties.get("test"), selected, execution, location, violations);
        }
        if (selected.contains("atf-tests") && arguments.stream().anyMatch(Set.of("test", "verify", "package", "install")::contains)
                && !properties.containsKey("test") && !Boolean.parseBoolean(properties.getOrDefault("skipTests", "false"))
                && !Boolean.parseBoolean(properties.getOrDefault("maven.test.skip", "false"))) {
            String suite = properties.getOrDefault("surefire.suiteXmlFiles", properties.getOrDefault("atf.test.suite", ""));
            if (suite.isEmpty() && (properties.containsKey("surefire.suiteXmlFiles") || properties.containsKey("atf.test.suite"))) {
                violations.add(location + "explicit suite selection must not be empty");
                return;
            }
            if (suite.isEmpty()) {
                Set<String> candidates = new LinkedHashSet<>();
                for (String profile : profiles) if (!profile.startsWith("!") && !profile.startsWith("-")) {
                    candidates.addAll(execution.getOrDefault("suite:atf-tests:" + profile, Set.of()));
                }
                if (candidates.isEmpty()) candidates.addAll(execution.getOrDefault("suite:atf-tests", Set.of()));
                if (candidates.size() != 1) violations.add(location + "suite selection is missing or ambiguous; declare the intended suite");
                else suite = candidates.iterator().next();
            }
            for (String configured : suite.split(",", -1)) validateSuiteSelection(root, configured, location, violations);
        }
    }

    private static void validateTestSelection(Path root, String selection, Set<String> projects,
                                             Map<String, Set<String>> execution, String location, List<String> violations) throws Exception {
        if (!projects.contains("atf-tests")) {
            violations.add(location + "documented class filters must select their verified atf-tests owner");
            return;
        }
        if (!execution.containsKey("testClasses")) {
            execution.put("testClasses", FrameworkArchitectureRules.executableTests(root, violations));
        }
        for (String requested : selection.split(",", -1)) {
            String selector = requested.replaceFirst("^!", "");
            if (selector.isEmpty() || !selector.matches("[\\w$.*?]+")) {
                violations.add(location + "unsupported class selector; document a class name/glob rather than a method or regex expression: " + requested);
                continue;
            }
            String regex = Pattern.quote(selector).replace("*", "\\E.*\\Q").replace("?", "\\E.\\Q");
            boolean found = execution.get("testClasses").stream().anyMatch(name -> name.matches(regex)
                    || name.substring(name.lastIndexOf('.') + 1).matches(regex));
            if (!found) violations.add(location + "class selector matches no executable TestNG class: " + requested);
        }
    }

    private static void validateSuiteSelection(Path root, String configured, String location, List<String> violations) throws Exception {
        try {
            Path owner = root.resolve("atf-tests").normalize();
            Path suite = owner.resolve(configured).normalize();
            if (!configured.isEmpty() && suite.startsWith(owner) && configured.endsWith(".xml") && Files.isRegularFile(suite)
                    && suite.toRealPath().startsWith(owner.toRealPath())) return;
        } catch (IllegalArgumentException invalid) {
            violations.add(location + "invalid suite selection path: " + configured);
            return;
        }
        violations.add(location + "suite is not an existing atf-tests XML selection: " + configured);
    }

    /** Known delivery/scorecard markers belong to feedback or external reviews, not stable contracts. */
    private static void validateStableLifecycle(Path root, Path file, List<String> lines, List<String> violations) {
        StringBuilder paragraph = new StringBuilder();
        int start = 0;
        for (int index = 0; index <= lines.size(); index++) {
            String line = index == lines.size() ? "" : visibleProse(lines.get(index));
            boolean structural = line.matches("^\\s*(?:#{1,6}\\s|\\||<|[-+*]\\s|\\d+[.)]\\s).*");
            if (line.isBlank() || structural) {
                inspectStableParagraph(root, file, start, paragraph.toString(), violations);
                paragraph.setLength(0);
                if (structural) inspectStableParagraph(root, file, index, line, violations);
            } else {
                if (paragraph.length() == 0) start = index;
                else paragraph.append(' ');
                paragraph.append(line);
            }
        }
    }

    private static void inspectStableParagraph(Path root, Path file, int line, String text, List<String> violations) {
        String prose = text.replace("**", "");
        boolean datedResult = Pattern.compile("\\b20\\d{2}[-/]\\d{2}[-/]\\d{2}\\b").matcher(prose).find()
                && Pattern.compile("(?i)\\b(?:PASS(?:ED)?|verified|delivered|accepted)\\b").matcher(prose).find();
        boolean counts = Pattern.compile("(?i)\\b(?:\\d+\\s*/\\s*\\d+\\s+(?:tests?|checks?|cases?)|"
                + "\\d+\\s+(?:tests?|checks?|cases?)\\s+(?:passed|failed|skipped)|(?:passed|failed|skipped)\\s*[:=]\\s*\\d+)\\b").matcher(prose).find();
        boolean score = Pattern.compile("(?i)\\b(?:ATF[_ -]?(?:AI|J\\d+)|(?:review\\s+)?(?:score|rating|assessment))\\b"
                + ".{0,60}?\\b\\d{1,3}\\s*/\\s*100\\b").matcher(prose).find();
        if (datedResult || counts || score) {
            violations.add(location(root, file, line) + "DOC-LIFECYCLE keep execution receipts and review scores in feedback or external review");
        }
    }

    private static void validateBuildFacts(Path root, List<String> violations) throws Exception {
        if (!Files.isRegularFile(root.resolve("README.md")) || !Files.isRegularFile(root.resolve("pom.xml"))) return;
        org.w3c.dom.Element pom = GovernanceFiles.pom(root.resolve("pom.xml"));
        String coordinate = GovernanceFiles.text(pom, "groupId", "") + ':'
                + GovernanceFiles.text(pom, "artifactId", "") + ':' + GovernanceFiles.text(pom, "version", "");
        String release = GovernanceFiles.text(GovernanceFiles.child(pom, "properties"), "maven.compiler.release", "");
        List<String> visible = withoutFences(Files.readAllLines(root.resolve("README.md")), root,
                root.resolve("README.md"), new ArrayList<>(), new HashSet<>());
        String overview = String.join("\n", visible);
        if (coordinate.contains("::") || !overview.contains(coordinate)) {
            violations.add("README.md:1 DOC-BUILD-FACT document the current parent coordinate " + coordinate);
        }
        if (release.isEmpty() || !Pattern.compile("\\bJava\\s+" + Pattern.quote(release) + "\\b").matcher(overview).find()) {
            violations.add("README.md:1 DOC-BUILD-FACT document the compiler target Java " + release);
        }
    }

    /** The register is the lifecycle authority; historical receipts are kept at their original scope. */
    private static void validateFeedback(Path root, List<String> violations) throws Exception {
        Path register = root.resolve("docs/feedback/README.md");
        if (!Files.isRegularFile(register)) return;
        List<String> lines = withoutFences(Files.readAllLines(register), root, register,
                new ArrayList<>(), new HashSet<>());
        Set<String> seen = new HashSet<>();
        boolean active = false;
        boolean verified = false;
        boolean hasActive = false;
        boolean hasVerified = false;
        boolean hasActiveHeader = false;
        boolean baseline = false;
        Set<String> baselineIds = new HashSet<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = canonicalReadmeLine(lines.get(index).strip());
            if (line.startsWith("|")) {
                String[] baselineCells = line.split("\\|", -1);
                if (baselineCells.length >= 3 && baselineCells[1].strip().equals("ID")
                        && baselineCells[2].strip().equals("Impact / risk / urgency")) {
                    baseline = true;
                    if (baselineCells.length != 6
                            || !baselineCells[3].strip().equals("Correctness / verification / clarity")
                            || !baselineCells[4].strip().equals("Evidence scope")) {
                        violations.add(location(root, register, index) + "DOC-FEEDBACK-SCHEMA baseline component table requires four declared columns");
                    }
                    continue;
                }
                if (baseline) {
                    if (baselineCells.length > 1 && baselineCells[1].strip().matches("[-:]+")) continue;
                    String component = "(?:[1-9][0-9]?|100)";
                    String triple = component + "\\s*/\\s*" + component + "\\s*/\\s*" + component;
                    if (baselineCells.length != 6 || !FEEDBACK_ID.matcher(baselineCells[1].strip()).matches()
                            || !baselineCells[2].strip().matches(triple)
                            || !baselineCells[3].strip().matches(triple) || baselineCells[4].isBlank()) {
                        violations.add(location(root, register, index) + "DOC-FEEDBACK-SCHEMA baseline component row requires an ID, two bounded numeric triples and evidence scope; keep verified summaries in their own table");
                    } else if (!baselineIds.add(baselineCells[1].strip())) {
                        violations.add(location(root, register, index) + "DOC-FEEDBACK duplicate baseline component ID " + baselineCells[1].strip());
                    }
                    continue;
                }
            } else baseline = false;
            if (line.startsWith("## ")) {
                active = line.equals("## Active TODOs");
                verified = line.equals("## Verified corrections");
                hasActive |= active;
                hasVerified |= verified;
            }
            if (!line.startsWith("|") || (!active && !verified)) continue;
            String[] cells = line.split("\\|", -1);
            if (cells.length < 3) continue;
            if (!FEEDBACK_ID.matcher(cells[1].strip()).matches()) {
                if (active && cells[1].strip().equals("ID")) {
                    hasActiveHeader = true;
                    List<String> headings = java.util.Arrays.stream(cells).map(String::strip)
                            .map(MarkdownDocumentationRules::canonicalReadmeLine).collect(Collectors.toList());
                    if (!headings.equals(List.of("", "ID", "Finding", "Owner", "Priority (1-100)", "Quality (1-100)", "Status", "Closure evidence", ""))) {
                        violations.add(location(root, register, index) + "DOC-FEEDBACK-SCHEMA active register requires separate Priority (1-100) and Quality (1-100) columns");
                    }
                }
                if (!cells[1].strip().equals("ID") && !cells[1].strip().matches("[-:]+")) {
                    violations.add(location(root, register, index) + "DOC-FEEDBACK invalid item ID: " + cells[1].strip());
                }
                continue;
            }
            String id = cells[1].strip();
            if (!seen.add(id)) violations.add(location(root, register, index) + "DOC-FEEDBACK duplicate ID " + id);
            int expected = active ? 9 : 5;
            if (cells.length != expected) {
                violations.add(location(root, register, index) + "DOC-FEEDBACK invalid register columns for " + id);
                continue;
            }
            for (int cell = 2; cell < cells.length - 1; cell++) {
                if (active && (cell == 4 || cell == 5)) continue;
                if (cells[cell].isBlank() || cells[cell].strip().matches("(?i)(?:tbd|todo|pending|done|passed|verified)")) {
                    if (!(active && cell == 6 && cells[cell].strip().equals("TODO"))) {
                        violations.add(location(root, register, index) + "DOC-FEEDBACK provide actionable owner/evidence fields for " + id);
                    }
                }
            }
            if (active) {
                if (!cells[4].strip().matches("(?:[1-9][0-9]?|100)")) {
                    violations.add(location(root, register, index) + "DOC-FEEDBACK-PRIORITY provide an integer from 1 to 100 for " + id);
                }
                if (!cells[5].strip().matches("(?:[1-9][0-9]?|100)")) {
                    violations.add(location(root, register, index) + "DOC-FEEDBACK-QUALITY provide an integer from 1 to 100 for " + id);
                }
            }
            if (active && !Set.of("TODO", "IN PROGRESS", "BLOCKED").contains(cells[6].strip())) {
                violations.add(location(root, register, index) + "DOC-FEEDBACK active item has invalid status: " + id);
            }
            if (verified && Pattern.compile("(?i)\\b(?:TODO|pending|unexecuted)\\b").matcher(cells[3]).find()) {
                violations.add(location(root, register, index) + "DOC-FEEDBACK verified item has pending closure evidence: " + id);
            }
        }
        if (!hasActive || !hasVerified) violations.add("docs/feedback/README.md:1 DOC-FEEDBACK retain active and verified lifecycle sections");
        if (hasActive && !hasActiveHeader) violations.add("docs/feedback/README.md:1 DOC-FEEDBACK-SCHEMA retain the active register header with separate Priority and Quality columns");
    }

    private static void validateAtlasBoundary(Path root, List<String> violations) throws Exception {
        for (String area : List.of("atf-atlas", "artifacts/atlas")) {
            Path atlas = root.resolve(area);
            if (Files.exists(atlas.resolve("pom.xml")) || !GovernanceFiles.files(atlas, ".java").isEmpty()
                    || !GovernanceFiles.files(atlas, ".class").isEmpty() || !GovernanceFiles.files(atlas, ".jar").isEmpty()) {
                violations.add(area + ":1 DOC-ATLAS-BOUNDARY Atlas is documentation and traceability, not a runtime or Maven module");
            }
        }
    }

    private static void validateLivingPair(Path root, List<String> violations) throws Exception {
        Path markdown = root.resolve(LIVING + ".md");
        Path htmlFile = root.resolve(LIVING + ".html");
        if (!Files.isRegularFile(markdown) || !Files.isRegularFile(htmlFile)) return;
        String canonical = Files.readString(markdown).replace("\r\n", "\n");
        String html = Files.readString(htmlFile);
        String edition = readmeEdition(root, new ArrayList<>());
        if (!html.matches("(?is).*<html\\b[^>]*\\blang=[\"']" + edition + "[\"'].*")) {
            violations.add(LIVING + ".html:1 DOC-PAIR HTML language must match the declared documentation edition");
        }
        if (!meta(html, "atf-source").equals(markdown.getFileName().toString())
                || !meta(html, "atf-source-sha256").equals(sha256(canonical))) {
            violations.add(LIVING + ".html:1 DOC-PAIR source metadata/hash is missing or stale");
        }
        List<String> mdHeadings = new ArrayList<>();
        List<String> visible = withoutFences(Files.readAllLines(markdown), root, markdown, new ArrayList<>(), new HashSet<>());
        for (String line : visible) {
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) mdHeadings.add(normalizeHeading(heading.group(1)));
        }
        List<String> htmlHeadings = new ArrayList<>();
        Matcher heading = Pattern.compile("(?is)<h[1-6]\\b[^>]*>(.*?)</h[1-6]\\s*>").matcher(html);
        while (heading.find()) htmlHeadings.add(normalizeHeading(heading.group(1)));
        if (!mdHeadings.equals(htmlHeadings)) violations.add(LIVING + ".html:1 DOC-PAIR keep the ordered section map synchronized with Markdown");
        Set<String> htmlAnchors = new HashSet<>();
        Matcher anchor = Pattern.compile("(?is)\\bid=[\"']([^\"']+)[\"']").matcher(html);
        while (anchor.find()) {
            if (!htmlAnchors.add(anchor.group(1))) violations.add(LIVING + ".html:1 DOC-PAIR duplicate HTML anchor " + anchor.group(1));
        }
        Matcher link = Pattern.compile("(?is)\\b(?:href|src)\\s*=\\s*[\"']([^\"']+)[\"']").matcher(html);
        while (link.find()) {
            String target = link.group(1);
            if (target.startsWith("#")) {
                if (!htmlAnchors.contains(target.substring(1))) violations.add(LIVING + ".html:1 DOC-PAIR missing HTML anchor " + target);
            } else validateTarget(root, htmlFile, 0, target, violations);
        }
    }

    static List<String> validateRetainedLivingPrinciples(Path root) throws Exception {
        String markdown = Files.readString(root.resolve(LIVING + ".md"));
        List<String> findings = validateRetainedLivingPrinciples(root, markdown);
        findings.addAll(validateRetainedLivingProjection(root, markdown, Files.readString(root.resolve(LIVING + ".html"))));
        return findings;
    }

    /** A bounded source/check map; MAINTAINED is not an inferred execution or coverage verdict. */
    static List<String> validateRetainedLivingPrinciples(Path root, String markdown) throws Exception {
        List<String> findings = new ArrayList<>();
        Path file = root.resolve(LIVING + ".md");
        List<String> lines = withoutFences(List.of(markdown.replace("\r\n", "\n").split("\n", -1)),
                root, file, new ArrayList<>(), new HashSet<>());
        List<Integer> markers = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).strip().equals("<a id=\"retained-principles\"></a>")) markers.add(index);
        }
        if (markers.size() != 1) {
            findings.add(LIVING + ".md:1 DOC-LIVING-PRINCIPLES require one retained-principles anchor, found " + markers.size());
            return findings;
        }
        Set<String> required = new LinkedHashSet<>();
        for (int index = 1; index <= 12; index++) required.add(String.format(Locale.ROOT, "LD-%02d", index));
        Set<String> seen = new HashSet<>();
        boolean headingSeen = false;
        for (int index = markers.get(0) + 1; index < lines.size(); index++) {
            String line = lines.get(index).strip();
            if (line.matches("^#{1,3}\\s+.*")) {
                if (headingSeen) break;
                headingSeen = true;
            }
            if (!line.matches("^\\|\\s*LD-[^|]*\\|.*")) continue;
            String[] cells = line.split("\\|", -1);
            String id = cells[1].strip();
            String prefix = location(root, file, index) + "DOC-LIVING-PRINCIPLES " + id + " ";
            if (!required.contains(id)) findings.add(prefix + "unknown principle " + id);
            if (!seen.add(id)) findings.add(prefix + "duplicate " + id);
            if (cells.length != 8) {
                findings.add(prefix + "requires six declared table columns");
                continue;
            }
            if (cells[2].isBlank() || cells[6].isBlank()) findings.add(prefix + "requires an intent and explicit proof boundary");
            String state = cells[5].strip();
            if (!Set.of("MAINTAINED", "PARTIAL").contains(state)) {
                findings.add(prefix + "unsupported state " + state + "; source/check routes cannot infer execution or complete coverage");
            }
            if (id.equals("LD-11") && (!state.equals("PARTIAL")
                    || !cells[6].contains("ACCOUNT_DELETION_NOT_IMPLEMENTED"))) {
                findings.add(prefix + "LD-11 cleanup remains PARTIAL with ACCOUNT_DELETION_NOT_IMPLEMENTED until its owner supplies independent deletion proof");
            }
            validatePrincipleRoutes(root, file, cells[3], false, prefix, findings);
            validatePrincipleRoutes(root, file, cells[4], true, prefix, findings);
        }
        for (String id : required) if (!seen.contains(id)) {
            findings.add(LIVING + ".md:1 DOC-LIVING-PRINCIPLES missing " + id + "; restore its source/check owners and proof boundary");
        }
        return findings;
    }

    private static void validatePrincipleRoutes(Path root, Path file, String cell,
                                                boolean check, String prefix, List<String> findings) throws Exception {
        Matcher links = LINK.matcher(cell);
        int count = 0;
        while (links.find()) {
            count++;
            String target = links.group(1).strip();
            boolean valid = target.endsWith(".java") && !target.contains("\\")
                    && !target.matches("^[A-Za-z][A-Za-z0-9+.-]*:.*") && !target.startsWith("/");
            Path destination = null;
            if (valid) {
                try {
                    destination = file.getParent().resolve(decode(target)).normalize();
                    valid = destination.toAbsolutePath().startsWith(root.toAbsolutePath().normalize())
                            && Files.isRegularFile(destination)
                            && destination.toRealPath().startsWith(root.toRealPath());
                } catch (IllegalArgumentException invalid) {
                    valid = false;
                }
            }
            if (valid) {
                String relative = GovernanceFiles.relative(root, destination);
                valid = relative.contains("/src/main/java/") || relative.contains("/src/test/java/");
                if (check) valid = relative.contains("/src/test/java/")
                        && destination.getFileName().toString().matches(".*Tests?\\.java")
                        && Files.readString(destination).contains("@Test");
            }
            if (!valid) findings.add(prefix + (check ? "check route must name an existing Java test source: "
                    : "implementation route is not an owned existing Java source: ") + target);
        }
        if (count == 0) findings.add(prefix + (check ? "check route must name an existing Java test source"
                : "implementation route is not an owned existing Java source"));
    }

    /** Compare declared table projection only; matching text/hrefs cannot establish application acceptance. */
    static List<String> validateRetainedLivingProjection(Path root, String markdown, String html) throws Exception {
        List<String> findings = new ArrayList<>();
        String prefix = LIVING + ".html:1 DOC-LIVING-PRINCIPLES HTML ";
        List<String> visible = withoutFences(List.of(markdown.replace("\r\n", "\n").split("\n", -1)),
                root, root.resolve(LIVING + ".md"), new ArrayList<>(), new HashSet<>());
        Map<String, String[]> expected = new LinkedHashMap<>();
        boolean section = false;
        boolean headingSeen = false;
        for (String sourceLine : visible) {
            String line = sourceLine.strip();
            if (line.equals("<a id=\"retained-principles\"></a>")) { section = true; continue; }
            if (!section) continue;
            if (line.matches("^#{1,3}\\s+.*")) { if (headingSeen) break; headingSeen = true; }
            if (!line.matches("^\\|\\s*LD-[^|]*\\|.*")) continue;
            String[] cells = line.split("\\|", -1);
            if (cells.length != 8 || expected.put(cells[1].strip(), cells) != null) {
                findings.add(prefix + "projection requires a unique six-column Markdown principle map");
                return findings;
            }
        }
        if (expected.size() != 12) {
            findings.add(prefix + "projection requires the complete twelve-row Markdown principle map");
            return findings;
        }
        LivingHtmlProjection projection = new LivingHtmlProjection();
        new ParserDelegator().parse(new StringReader(html), projection, true);
        if (projection.headings != 1) {
            findings.add(prefix + "require one retained-principles heading outside literal examples");
            return findings;
        }
        Set<String> seen = new HashSet<>();
        String[] fields = {"ID", "intent", "implementation", "check", "state", "proof boundary"};
        for (List<LivingHtmlCell> cells : projection.rows) {
                String id = cells.get(0).text.toString().strip();
                if (!expected.containsKey(id)) { findings.add(prefix + "unknown principle " + id); continue; }
                if (!seen.add(id)) findings.add(prefix + "duplicate " + id);
                if (cells.size() != 6) { findings.add(prefix + id + " requires six projected columns"); continue; }
                String[] sourceCells = expected.get(id);
                for (int column = 0; column < cells.size(); column++) {
                    String sourceText = normalizeHeading(sourceCells[column + 1]
                            .replaceAll("\\[([^\\]]+)]\\([^)]+\\)", "$1"));
                    String actualText = cells.get(column).text.toString().replaceAll("\\s+", " ").strip();
                    if (!sourceText.equals(actualText)) {
                        findings.add(prefix + id + " " + fields[column] + " differs from Markdown; expected="
                                + sourceText + "; observed=" + actualText);
                    }
                    if (column == 2 || column == 3) {
                        List<String> sourceLinks = new ArrayList<>();
                        Matcher links = LINK.matcher(sourceCells[column + 1]);
                        while (links.find()) sourceLinks.add(links.group(1).strip());
                        List<String> actualLinks = cells.get(column).links;
                        if (!sourceLinks.equals(actualLinks)) findings.add(prefix + id + " " + fields[column]
                                + " hrefs differ from Markdown; expected=" + sourceLinks + "; observed=" + actualLinks);
                    }
                }
        }
        for (String id : expected.keySet()) if (!seen.contains(id)) findings.add(prefix + "missing " + id);
        return findings;
    }

    private static final class LivingHtmlCell {
        private final StringBuilder text = new StringBuilder();
        private final List<String> links = new ArrayList<>();
    }

    /** Narrow JDK HTML callbacks for the rendered six-column table, not a general HTML5 validator. */
    private static final class LivingHtmlProjection extends HTMLEditorKit.ParserCallback {
        private static final Set<String> LITERAL = Set.of("pre", "code", "script", "style", "textarea");
        private final List<List<LivingHtmlCell>> rows = new ArrayList<>();
        private List<LivingHtmlCell> row;
        private LivingHtmlCell cell;
        private int headings;
        private int literalDepth;
        private boolean section;

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            String name = tag.toString();
            if (LITERAL.contains(name) && !(name.equals("code") && cell != null && literalDepth == 0)) {
                literalDepth++;
                return;
            }
            if (literalDepth > 0) return;
            if (name.matches("h[1-3]")) {
                boolean selected = name.equals("h3") && "retained-principles".equals(attributes.getAttribute(HTML.Attribute.ID));
                if (selected) headings++;
                section = selected;
            }
            if (!section) return;
            if (name.equals("tr")) { row = new ArrayList<>(); cell = null; }
            else if (name.equals("td") && row != null) { cell = new LivingHtmlCell(); row.add(cell); }
            else if (name.equals("a") && cell != null) {
                Object href = attributes.getAttribute(HTML.Attribute.HREF);
                if (href != null) cell.links.add(href.toString());
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            String name = tag.toString();
            if (LITERAL.contains(name) && literalDepth > 0) { literalDepth--; return; }
            if (literalDepth > 0 || !section) return;
            if (name.equals("td")) cell = null;
            else if (name.equals("tr")) {
                if (row != null && !row.isEmpty()) rows.add(row);
                row = null;
                cell = null;
            }
        }

        @Override
        public void handleText(char[] data, int position) {
            if (section && literalDepth == 0 && cell != null) cell.text.append(data);
        }
    }

    private static String meta(String html, String name) {
        Matcher meta = Pattern.compile("(?is)<meta\\b[^>]*\\bname=[\"']" + Pattern.quote(name)
                + "[\"'][^>]*\\bcontent=[\"']([^\"']*)[\"'][^>]*>").matcher(html);
        return meta.find() ? meta.group(1) : "";
    }

    private static String normalizeHeading(String value) {
        return value.replaceAll("<[^>]+>", "").replace("`", "")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&#39;", "'").replaceAll("\\s+", " ").strip();
    }

    private static String sha256(String source) throws Exception {
        StringBuilder hash = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8))) {
            hash.append(String.format("%02x", value & 0xff));
        }
        return hash.toString();
    }

    /** Keeps line numbers stable; executable Mermaid labels remain visible for language checks. */
    private static List<String> withoutFences(List<String> lines, Path root, Path file, List<String> violations,
                                               Set<Path> diagramDocuments) {
        boolean english = !readmeEdition(root, new ArrayList<>()).equals("ro");
        return withoutFences(lines, root, file, violations, diagramDocuments, english);
    }

    private static List<String> withoutFences(List<String> lines, Path root, Path file, List<String> violations,
                                               Set<Path> diagramDocuments, boolean english) {
        List<String> visible = new ArrayList<>();
        String fence = null;
        String language = "";
        int opening = -1;
        List<String> diagram = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            Matcher candidate = FENCE.matcher(line);
            if (fence == null && candidate.matches()) {
                fence = candidate.group(1);
                language = candidate.group(2).strip();
                opening = index;
                if (language.isBlank()) violations.add(location(root, file, index)
                        + "DOC-FENCE-LANGUAGE declare a language or text for a literal output block");
                diagram.clear();
                visible.add("");
            } else if (fence != null && candidate.matches()
                    && candidate.group(1).charAt(0) == fence.charAt(0)
                    && candidate.group(1).length() >= fence.length() && candidate.group(2).isBlank()) {
                if (language.equals("mermaid")) {
                    inspectDiagram(root, file, opening, diagram, violations);
                    diagramDocuments.add(file);
                    int next = index + 1;
                    while (next < lines.size() && lines.get(next).isBlank()) next++;
                    if (next == lines.size() || lines.get(next).strip().matches("^(?:#{1,6}\\s|```|~~~|</details>).*")) {
                        violations.add(location(root, file, index) + "DOC-DIAGRAM-LEGEND explain the diagram immediately after its fence");
                    }
                }
                fence = null;
                visible.add("");
            } else if (fence != null) {
                if (language.equals("mermaid")) {
                    diagram.add(line);
                    // Link validation still ignores all fenced examples.
                    if (english && ROMANIAN_PROSE.matcher(line).find()) {
                        violations.add(location(root, file, index) + "DOC-LANGUAGE known Romanian marker in diagram labels");
                    }
                    if (!line.stripLeading().startsWith("%%") && EDITORIAL_ARTIFACT.matcher(line).find()) {
                        violations.add(location(root, file, index) + "DOC-EDITORIAL remove unresolved translation or drafting markers from diagram labels");
                    }
                }
                visible.add("");
            } else visible.add(line);
        }
        if (fence != null) violations.add(location(root, file, opening) + "DOC-FENCE close the " + fence + " block");
        return visible;
    }

    private static void inspectDiagram(Path root, Path file, int opening, List<String> lines, List<String> violations) {
        List<String> executable = lines.stream().map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("%%")).collect(Collectors.toList());
        if (executable.size() < 2 || !DIAGRAM_TYPE.matcher(executable.get(0)).matches()) {
            violations.add(location(root, file, opening) + "DOC-MERMAID provide a supported type and a nonempty diagram body");
        }
    }

    private static void validateLinks(Path root, Path file, List<String> lines, List<String> violations) throws Exception {
        Set<String> definitions = new HashSet<>();
        for (int index = 0; index < lines.size(); index++) {
            Matcher definition = REFERENCE_DEFINITION.matcher(lines.get(index));
            if (definition.matches()) {
                definitions.add(referenceId(definition.group(1)));
                validateTarget(root, file, index, definition.group(2) == null ? definition.group(3) : definition.group(2), violations);
            }
        }
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            if (REFERENCE_DEFINITION.matcher(line).matches()) continue;
            Matcher link = LINK.matcher(line);
            List<int[]> inlineRanges = new ArrayList<>();
            while (link.find()) {
                if (escaped(line, link.start())) continue;
                inlineRanges.add(new int[]{link.start(), link.end()});
                String target = link.group(1).strip();
                if (target.startsWith("<")) target = target.substring(1, target.indexOf('>'));
                else target = target.replaceFirst("\\s+[\"'].*$", "");
                validateTarget(root, file, index, target, violations);
            }
            Matcher reference = REFERENCE.matcher(line);
            while (reference.find()) {
                if (escaped(line, reference.start())) continue;
                int start = reference.start();
                if (inlineRanges.stream().anyMatch(range -> start >= range[0] && start < range[1])) continue;
                if (reference.group(2) != null) {
                    String id = reference.group(2).isEmpty() ? reference.group(1) : reference.group(2);
                    if (!definitions.contains(referenceId(id))) {
                        violations.add(location(root, file, index) + "DOC-LINK missing reference definition: " + id);
                    }
                }
            }
        }
    }

    private static void validateTarget(Path root, Path file, int index, String target, List<String> violations) throws Exception {
        if (target.matches("(?i)^(?:https?|mailto):.*") || target.contains("${")) return;
        try {
            int fragment = target.indexOf('#');
            String path = decode(fragment < 0 ? target : target.substring(0, fragment));
            String anchor = fragment < 0 ? "" : decode(target.substring(fragment + 1));
            Path destination = path.isEmpty() ? file : file.getParent().resolve(path).normalize();
            if (!destination.toAbsolutePath().startsWith(root.toAbsolutePath().normalize())) {
                violations.add(location(root, file, index) + "DOC-LINK keep repository links inside the checkout: " + target);
            } else if (!Files.exists(destination)) {
                violations.add(location(root, file, index) + "DOC-LINK missing target: " + target);
            } else if (!anchor.isEmpty() && destination.toString().toLowerCase(Locale.ROOT).endsWith(".md")
                    && !anchors(Files.readAllLines(destination)).contains(anchor)) {
                violations.add(location(root, file, index) + "DOC-ANCHOR missing fragment: " + target);
            } else if (!anchor.isEmpty() && destination.toString().toLowerCase(Locale.ROOT).endsWith(".html")) {
                Matcher htmlAnchor = Pattern.compile("(?is)\\bid=[\"']([^\"']+)[\"']").matcher(Files.readString(destination));
                Set<String> ids = new HashSet<>();
                while (htmlAnchor.find()) ids.add(htmlAnchor.group(1));
                if (!ids.contains(anchor)) violations.add(location(root, file, index) + "DOC-ANCHOR missing HTML fragment: " + target);
            }
        } catch (IllegalArgumentException invalid) {
            violations.add(location(root, file, index) + "DOC-LINK invalid path or percent escape: " + target);
        }
    }

    private static String referenceId(String id) {
        return id.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Code spans may use any matching backtick run and may cross a line boundary. */
    static List<String> withoutInlineCode(List<String> lines) {
        String source = String.join("\n", lines);
        StringBuilder visible = new StringBuilder(source);
        for (int[] span : inlineCodeRanges(source)) {
            for (int index = span[0]; index < span[3]; index++) {
                if (source.charAt(index) != '\n') visible.setCharAt(index, ' ');
            }
        }
        return List.of(visible.toString().split("\n", -1));
    }

    private static List<int[]> inlineCodeRanges(String source) {
        List<int[]> spans = new ArrayList<>();
        for (int cursor = 0; cursor < source.length();) {
            if (source.charAt(cursor) != '`' || escaped(source, cursor)) { cursor++; continue; }
            int opening = cursor;
            while (cursor < source.length() && source.charAt(cursor) == '`') cursor++;
            int length = cursor - opening;
            int search = cursor;
            int end = -1, contentEnd = -1;
            while (search < source.length()) {
                if (source.charAt(search) != '`') { search++; continue; }
                int run = search;
                while (search < source.length() && source.charAt(search) == '`') search++;
                if (search - run == length) { end = search; contentEnd = run; break; }
            }
            if (end < 0) continue;
            spans.add(new int[] {opening, cursor, contentEnd, end});
            cursor = end;
        }
        return spans;
    }

    private static boolean escaped(String source, int index) {
        int backslashes = 0;
        while (index > 0 && source.charAt(--index) == '\\') backslashes++;
        return backslashes % 2 != 0;
    }

    static Set<String> anchors(List<String> lines) {
        Set<String> anchors = new HashSet<>();
        Map<String, Integer> duplicates = new HashMap<>();
        String fence = null;
        for (String line : lines) {
            Matcher candidate = FENCE.matcher(line);
            if (fence == null && candidate.matches()) {
                fence = candidate.group(1);
                continue;
            }
            if (fence != null) {
                if (candidate.matches() && candidate.group(1).charAt(0) == fence.charAt(0)
                        && candidate.group(1).length() >= fence.length() && candidate.group(2).isBlank()) fence = null;
                continue;
            }
            Matcher explicit = EXPLICIT_ANCHOR.matcher(line);
            while (explicit.find()) anchors.add(explicit.group(1));
            Matcher heading = HEADING.matcher(line);
            if (heading.matches()) {
                String slug = heading.group(1).toLowerCase(Locale.ROOT)
                        .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                        .replaceAll("<[^>]+>", "").replaceAll("[^\\p{L}\\p{N} _-]", "")
                        .strip().replace(' ', '-');
                int duplicate = duplicates.getOrDefault(slug, 0);
                String unique = duplicate == 0 ? slug : slug + '-' + duplicate;
                while (anchors.contains(unique)) unique = slug + '-' + (++duplicate);
                duplicates.put(slug, duplicate + 1);
                anchors.add(unique);
            }
        }
        return anchors;
    }

    private static String visibleProse(String line) {
        return line.replaceAll("`+[^`]*`+", "")
                .replaceAll("\\]\\([^)]+\\)", "]").replaceAll("https?://\\S+", "")
                .replaceAll("<[^>]+>", "");
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static String location(Path root, Path file, int zeroBasedLine) {
        return GovernanceFiles.relative(root, file) + ':' + (zeroBasedLine + 1) + ' ';
    }
}
