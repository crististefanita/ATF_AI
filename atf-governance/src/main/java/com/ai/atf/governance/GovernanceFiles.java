package com.ai.atf.governance;

import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Filesystem and secure POM primitives shared by the governance validators. */
final class GovernanceFiles {
    static final List<String> MODULES = List.of(
            "atf-core", "atf-api", "atf-playwright", "atf-selenium", "atf-reporting", "atf-governance", "atf-tests");
    private static final Set<String> EXCLUDED = Set.of(
            ".git", ".idea", ".tmp", "target", "allure-results", ".codex-artifacts");
    private static final Set<String> PORTABLE_TEXT_SUFFIXES = Set.of(
            ".java", ".md", ".html", ".xml", ".properties", ".yml", ".yaml", ".json", ".txt", ".csv",
            ".svg", ".js", ".cjs", ".mjs", ".css", ".sh", ".cmd", ".bat", ".ps1", ".config", ".toml", ".ini");
    private static final Pattern EXTERNAL_URL = Pattern.compile("(?i)https?://[^\\s<>\"'`]+");
    // Bounded filesystem forms: API routes and dynamically resolved runtime roots are not local author paths.
    private static final Pattern MACHINE_PATH = Pattern.compile(
            "(?i)(?<![a-z0-9])[a-z]:[\\\\/]"
                    + "|\\bfile:[/]"
                    + "|(?<![\\\\/])\\\\\\\\[a-z0-9_.-]+\\\\[a-z0-9_$.-]+"
                    + "|(?<!xpath=)(?<![:/])//[a-z0-9_.-]+/[a-z0-9_$.-]+"
                    + "|(?<![a-z0-9_:/])/(?:home|Users)/[^\\s/<>\"'`]+"
                    + "|(?<![a-z0-9_:/])/root(?:[/]|(?=[\\s<>\"'`]|$))");

    private GovernanceFiles() { }

    static Path root() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))
                    && Files.isDirectory(current.resolve("atf-core"))
                    && Files.isDirectory(current.resolve("atf-tests"))) return current;
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate the seven-module ATF reactor from user.dir");
    }

    static List<Path> files(Path root, String suffix) throws IOException {
        return files(root, suffix, directory -> { });
    }

    /** The observer exposes visited directory boundaries for deterministic traversal controls. */
    static List<Path> files(Path root, String suffix, Consumer<Path> directoryObserver) throws IOException {
        if (!Files.exists(root)) return List.of();
        Path canonicalRoot = root.toRealPath();
        List<Path> selected = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                directoryObserver.accept(directory);
                return included(root, directory) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (!included(root, file) || !file.toString().endsWith(suffix) || !Files.isRegularFile(file)) {
                    return FileVisitResult.CONTINUE;
                }
                try {
                    if (file.toRealPath().startsWith(canonicalRoot)) selected.add(file);
                } catch (IOException error) {
                    throw new IllegalStateException("Unreadable source: " + file, error);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException failure) throws IOException {
                if (included(root, file)) throw failure;
                return FileVisitResult.CONTINUE;
            }
        });
        return selected.stream().sorted().collect(Collectors.toList());
    }

    /** Project text remains relocatable, including code fences and historical feedback receipts. */
    static List<String> validatePortablePaths(Path root) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path file : files(root, "")) {
            String relative = relative(root, file);
            String name = file.getFileName().toString();
            boolean maintainedText = PORTABLE_TEXT_SUFFIXES.stream().anyMatch(name::endsWith)
                    || Set.of(".gitignore", ".gitattributes", "Dockerfile").contains(name)
                    || relative.contains("/META-INF/services/");
            if (!maintainedText) continue;
            String source = Files.readString(file);
            boolean machinePath;
            if (name.endsWith(".java")) {
                machinePath = containsMachinePath(JavaSourceScanner.commentsOnly(source));
                // Most files need no additional compiler parse; decode only possible literal matches.
                String literals = JavaSourceScanner.literalsOnly(source);
                String collapsedEscapes = literals.replace("\\\\", "\\");
                String possibleValues = collapsedEscapes.replaceAll("\\\\[nrtfb]", " ");
                if (!machinePath && (MACHINE_PATH.matcher(literals).find()
                        || MACHINE_PATH.matcher(collapsedEscapes).find()
                        || MACHINE_PATH.matcher(possibleValues).find())) {
                    machinePath = JavaTypeReferences.stringLiterals(source).stream().anyMatch(GovernanceFiles::containsMachinePath);
                }
            } else machinePath = containsMachinePath(source);
            if (machinePath) violations.add(relative + ": PATH-PORTABILITY use a project-relative path or an explicit configurable root placeholder");
        }
        return violations;
    }

    private static boolean containsMachinePath(String source) {
        return MACHINE_PATH.matcher(EXTERNAL_URL.matcher(source).replaceAll(" ")).find();
    }

    /** Output names are not exclusions once they are part of an owned source/resource tree. */
    private static boolean included(Path root, Path file) {
        boolean ownedSource = withinStandardSourceRoot(root.toAbsolutePath().normalize());
        List<String> parts = new ArrayList<>();
        for (Path part : root.relativize(file)) parts.add(part.toString());
        if (!ownedSource && parts.equals(List.of("artifacts", "publication.json"))) return false;
        for (int index = 0; index < parts.size(); index++) {
            String part = parts.get(index);
            if (!ownedSource && EXCLUDED.contains(part)) return false;
            if (!ownedSource && index == 2 && Set.of("runs", "current").contains(part)
                    && parts.get(0).equals("artifacts")
                    && Set.of("reports", "atlas").contains(parts.get(1))) return false;
            if (index >= 2 && parts.get(index - 2).equals("src")
                    && Set.of("main", "test").contains(parts.get(index - 1))
                    && Set.of("java", "resources").contains(part)) ownedSource = true;
        }
        return true;
    }

    private static boolean withinStandardSourceRoot(Path root) {
        for (int index = 2; index < root.getNameCount(); index++) {
            if (root.getName(index - 2).toString().equals("src")
                    && Set.of("main", "test").contains(root.getName(index - 1).toString())
                    && Set.of("java", "resources").contains(root.getName(index).toString())) return true;
        }
        return false;
    }

    static Element pom(Path path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(path.toFile()).getDocumentElement();
    }

    static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        if (parent == null) return result;
        for (org.w3c.dom.Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && name.equals(((Element) node).getTagName())) result.add((Element) node);
        }
        return result;
    }

    static Element child(Element parent, String name) {
        List<Element> values = children(parent, name);
        return values.isEmpty() ? null : values.get(0);
    }

    static String text(Element parent, String name, String fallback) {
        Element value = child(parent, name);
        return value == null ? fallback : value.getTextContent().strip();
    }

    static String relative(Path root, Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }

    static String problems(List<String> violations) {
        if (violations.isEmpty()) return "No governance findings supplied.";
        StringBuilder message = new StringBuilder("Governance failed with ")
                .append(violations.size()).append(" finding(s).\n");
        Set<String> selections = new LinkedHashSet<>();
        for (int index = 0; index < violations.size(); index++) {
            String finding = violations.get(index);
            DiagnosticHelp help = DIAGNOSTIC_HELP.get(findingCode(finding));
            message.append('\n').append(index + 1).append(") Observed: ").append(finding).append('\n');
            if (help == null) {
                message.append("   Failure meaning: Unclassified finding; no rule-specific cause is inferred.\n")
                        .append("   Repair hint: Inspect the emitting validator and preserve its evidence before choosing a correction.\n")
                        .append("   Owner: docs/governance/Failure_Diagnostics.md#unknown-findings\n");
            } else {
                message.append("   Failure meaning: ").append(help.meaning).append('\n')
                        .append("   Repair hint: ").append(help.repair).append('\n')
                        .append("   Owner: ").append(help.owner).append('\n');
                selections.add(help.selection);
            }
        }
        message.append("\nRecheck from an isolated reactor root; keep the original failure evidence.\n");
        for (String selection : selections) {
            message.append("  mvn -B -ntp -pl atf-governance -am \"-Dtest=").append(selection)
                    .append("\" \"-Dsurefire.failIfNoSpecifiedTests=false\" test\n");
        }
        message.append("For paired documentation edits, repeat with the explicit peer input described in docs/governance/Readme_Editions.md.\n")
                .append("A suggested correction is not an observed root cause or a passing verification.");
        return message.toString();
    }

    private static final Pattern LEADING_FINDING_CODE = Pattern.compile("^([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*)(?=\\s|$)");
    private static final Pattern LOCATED_FINDING_CODE = Pattern.compile("^.+?:(?:\\d+:?)?\\s+([A-Z][A-Z0-9]*(?:-[A-Z0-9]+)*)(?=\\s|$)");
    private static final Map<String, DiagnosticHelp> DIAGNOSTIC_HELP = diagnosticHelp();

    static String findingCode(String finding) {
        Matcher leading = LEADING_FINDING_CODE.matcher(finding);
        if (leading.find()) return leading.group(1);
        Matcher located = LOCATED_FINDING_CODE.matcher(finding);
        return located.find() ? located.group(1) : "";
    }

    static boolean matchesFinding(String finding, String expected) {
        String code = findingCode(expected);
        if (code.isEmpty()) throw new IllegalArgumentException("A finding expectation must name an emitted rule code");
        return code.equals(findingCode(finding)) && finding.contains(expected);
    }

    static boolean hasDiagnosticHelp(String finding) {
        return DIAGNOSTIC_HELP.containsKey(findingCode(finding));
    }

    static Set<String> diagnosticCodes() {
        return DIAGNOSTIC_HELP.keySet();
    }

    private static Map<String, DiagnosticHelp> diagnosticHelp() {
        Map<String, DiagnosticHelp> result = new LinkedHashMap<>();
        String architecture = "FrameworkArchitectureGovernanceTest";
        String documentation = String.join(",", "DocumentationGovernanceTest",
                "DocumentationRootGovernanceTest", "DocumentationEditionGovernanceTest",
                "LivingPrinciplesGovernanceTest", "DocumentationStructureGovernanceTest",
                "FeedbackGovernanceTest", "DocumentationPublicationGovernanceTest");
        String policy = "docs/governance/Code_And_Documentation_Policy.md";
        String structure = "docs/Architecture.md";
        help(result, "SUITE-DISABLED",
                "A maintained TestNG source annotation can suppress an existing regression.",
                "Remove enabled indirection or disabling from the reported maintained annotation; omit enabled or use literal true. Preserve reviewed manual fixtures and run the selected owner.",
                policy + "#existing-test-quality", architecture);
        help(result, "TEST-METHOD-INVENTORY TEST-METHOD-SOURCE TEST-METHOD-REMOVED TEST-METHOD-UNREGISTERED",
                "The reviewed executable TestNG method inventory is missing, malformed or differs from current test source.",
                "Inspect each reported signature and source path. Confirm the deletion was intentional and preserves the required proof; then update the sorted inventory and its pinned digest with the owning test change. Additions also require an explicit inventory update so a later deletion cannot pass unnoticed.",
                policy + "#existing-test-quality", architecture);
        help(result, "DOC-PROSE-DENSE DOC-PROSE-SENTENCE",
                "Visible orientation prose combines too many decisions or observations in one reading unit.",
                "Separate decisions into short paragraphs; use bullets for parallel checks, a table for alternatives, or a diagram with an adjacent legend for a real flow. Preserve prerequisites and proof limits; changing line wrapping does not repair density.",
                policy + "#reader-oriented-guides", "DocumentationReadabilityRulesTest");
        help(result, "DOC-COMMAND-DISCLOSURE",
                "A runnable Maven example is exposed or its disclosure opens by default without useful orientation.",
                "Keep essential strategy visible and place the shell command in balanced details without an open attribute. Give its summary the actual selection or purpose; retain prerequisites and expected proof outside it.",
                policy + "#reader-oriented-guides", "DocumentationReadabilityRulesTest");
        help(result, "DOC-STRATEGY-FLOW DOC-STRATEGY-LANES",
                "The testing strategy loses horizontal flow or a comparable API, Playwright or Selenium proof boundary.",
                "Use compact flowchart LR maps with an adjacent legend. Under test-design-contracts restore one four-column row per lane: linked design owner, observable oracle and proof limit. Move setup details to Testing_Commands.md; inspect the actual suite and assertion before writing the claim.",
                policy + "#reader-oriented-guides", "DocumentationReadabilityRulesTest");
        help(result, "DOC-DIAGRAM-ORIENTATION DOC-DIAGRAM-DISCLOSURE",
                "A maintained diagram loses the agreed horizontal reading or its immediately adjacent explained legend.",
                "Reduce the diagram to one question and use a compact LR flow. Put one balanced details block immediately after the fence, without open, with a descriptive Legend summary and the arrow meaning, actors and omitted detail. Keep sequence/state/class semantics and verify rendered geometry; a short source label is not visual proof.",
                policy + "#reader-orientation-and-diagrams", "DocumentationReadabilityRulesTest");
        help(result, "RETIRED-REPORTING",
                "A dependency restores a reporting backend outside the maintained Extent scope.",
                "Remove the retired SDK from direct, profile and managed dependencies. Keep reporting.engine=extent and the Extent aggregate selection; recheck dependency and executable-source ownership. Historical evidence remains historical.",
                policy + "#reporting-scope", architecture);
        help(result, "DOC-EDITION-LANGUAGE DOC-EDITION-UNTRANSLATED",
                "A Romanian documentation edition lacks a supported Romanian signal or retains meaningful unchanged English prose.",
                "Retrieve the canonical policy and read the complete EN/RO pair; translate the whole passage while preserving commands, facts, conditions and legends. Recheck with the explicit peer and record full-text review; a marker alone is not semantic proof.",
                "docs/governance/Readme_Editions.md#whole-document-review", documentation);
        help(result, "DOC-TRANSLATION-REVIEW DOC-TRANSLATION-STALE",
                "The full-document bilingual review declaration is missing, incomplete or bound to different content.",
                "Review the complete source and translation using the documented retrieval context, repair meaning and refresh only reviewed EN/RO digests. Keep every maintained document in the shared review inventory; do not stamp an unread file.",
                "docs/governance/Readme_Editions.md#whole-document-review", documentation);
        help(result, "MODULES ARTIFACT PARENT-COORDINATE PARENT-PACKAGING MODULE-COORDINATE MISSING-MODULE",
                "The declared seven-module reactor and its coordinates disagree with the maintained contract.",
                "Restore the existing parent/module declarations and compare the reported expected and observed values; do not add a module to silence the guard.",
                structure + "#module-ownership", architecture);
        help(result, "DEPENDENCY-COORDINATE DEPENDENCY-CYCLE DEPENDENCY-DIRECTION DUPLICATE-DEPENDENCY INHERITED-DEPENDENCIES",
                "Dependency declarations hide ownership, duplicate a dependency or violate the supported direction.",
                "Correct the reported POM/profile at its owning module and preserve test-only consumer dependencies; inspect the cycle before moving code.",
                structure + "#module-ownership", architecture);
        help(result, "CORE-SDK SDK-OWNERSHIP IMPORT-OWNERSHIP SIMPLE-TYPE-OWNERSHIP",
                "A concrete SDK or source reference crosses its module boundary.",
                "Move the vendor conversion/reference to its existing adapter or use the neutral Core contract; inspect the reported type and dependency together.",
                structure + "#provider-composition", architecture);
        help(result, "IMPORT-WILDCARD DUPLICATE-IMPORT",
                "Imports obscure the concrete dependencies or repeat an existing declaration.",
                "Name the used Java types/static members explicitly and remove duplicates; compile the affected owner without changing implementation behavior.",
                policy + "#explicit-imports-and-visible-dependencies", architecture);
        help(result, "PACKAGE PACKAGE-OWNER PACKAGE-PATH DUPLICATE-TYPE SOURCE-ROOTS ROOT-SOURCES TESTS-ANNOTATION TESTS-HELPER TESTS-MAIN TEST-PRODUCT-LEAK TEST-RESOURCE-LEAK",
                "A source, resource or type is outside its declared owner or appears more than once.",
                "Reconcile the reported package, standard source root and owner; keep helpers in main and annotated classes in test; package product fixture data from atf-tests/src/main/resources, retain suites/inventory as test resources and remove shadow copies.",
                structure + "#module-ownership", architecture);
        help(result, "SERVICE SERVICE-OWNER SPI-OWNER",
                "A provider descriptor or implementation belongs to the wrong library or cannot satisfy its neutral SPI.",
                "Inspect the owning adapter, public provider type and matching META-INF/services descriptor together; keep Core independent of concrete providers.",
                structure + "#provider-composition", architecture);
        help(result, "RETIRED-GENERATION",
                "Retired infrastructure has re-entered the maintained framework surface.",
                "Remove the unsupported reference from the current owner; preserve historical material only on the archive branch.",
                policy + "#ownership-and-change-scope", architecture);
        help(result, "EXECUTION-CONSOLE EXECUTION-PAUSE EXECUTION-INTERRUPT EXECUTION-OWNER-STALE INTERRUPT-RESTORE",
                "Execution bypasses its diagnostic/wait owner or loses the caller's interrupt state.",
                "Use the existing ConsoleLogger or wait owner, preserve bounded waits and current-thread interruption, and reconcile stale exact ownership entries.",
                policy + "#visibility-and-execution", architecture);
        help(result, "PATH-PORTABILITY",
                "Maintained content embeds a machine-specific author path.",
                "Replace the authored path with a correct project-relative reference or a declared caller-supplied root; preserve runtime-resolved paths and exact negative fixture data.",
                policy + "#portable-paths-and-executable-facts", architecture);
        help(result, "SUITE-MISSING SUITE-EMPTY SUITE-UNDECLARED SUITE-XML SUITE-FILTER SUITE-DUPLICATE SUITE-OWNER SUITE-CLASS",
                "A maintained selection is absent, malformed, filtered or points to an invalid class.",
                "Inspect the reported suite XML and existing class; restore explicit supported wiring and retain separate opt-in application selections.",
                "docs/Testing.md", architecture);
        help(result, "SUITE-INVOCATIONS",
                "A selected source test can be suppressed or its execution count is not explicit.",
                "Repair the owned test annotation: omit enabled or use true, and omit invocationCount or use a positive integer literal. Preserve the method, assertions and maintained selection; parse-only governance does not resolve constants.",
                policy + "#existing-test-quality", architecture);
        help(result, "SUITE-EXEMPTION-STALE SUITE-OPT-IN SUITE-OMISSION SUITE-GOVERNANCE SUITE-AGGREGATE SUITE-SOURCE",
                "The existing verification inventory and maintained selections disagree.",
                "Restore the affected existing regression to its owned selection or reconcile a reviewed exclusion; keep governance in the local gate and aggregate inventory aligned.",
                policy + "#existing-test-quality", architecture);
        help(result, "DOC-MISSING DOC-MODULE DOC-OWNER DOC-AGENT-CONTROL DOC-ATLAS-BOUNDARY",
                "A maintained documentation owner is missing or contains material outside its declared purpose.",
                "Restore the reported owner guide and required structure; keep Atlas documentation-only and project rules in the canonical owner documentation.",
                "docs/governance/README.md", documentation);
        help(result, "DOC-LINK DOC-ANCHOR",
                "A documented local route or fragment has no observed target.",
                "Correct the source-relative destination or restore the intended real anchor; verify the incoming route without adding a dummy target.",
                policy + "#portable-paths-and-executable-facts", documentation);
        help(result, "DOC-MERMAID DOC-DIAGRAM DOC-DIAGRAM-LEGEND DOC-README-ARCHITECTURE",
                "Markdown structure or a diagram no longer explains the owned contract in its supported form.",
                "Repair the reported fence/section/disclosure, diagram declaration or adjacent legend, then render the affected diagram and preserve its actual responsibilities.",
                "docs/governance/Failure_Diagnostics.md#documentation-and-rendering", documentation);
        help(result, "DOC-TABLE-LONG-CELL DOC-TABLE-SHAPE",
                "A normative Markdown table cell is too dense for scanning or a row no longer matches its declared columns.",
                "Keep explanatory cells within 140 visible characters and 22 words, move detail into nearby prose or a linked owner, and recheck the rendered table; align every row with its header.",
                policy + "#reader-orientation-and-diagrams", documentation);
        help(result, "DOC-README-EDITION",
                "The root project guide exposes translated-branch administration instead of reader decisions.",
                "Move translated-branch names and routes to docs/governance/Readme_Editions.md; keep the root guide focused on project purpose, composition and owner routes. Repair both maintained editions without hiding branch references in comments, encoded URLs or examples.",
                policy + "#reader-orientation-and-diagrams", documentation);
        help(result, "DOC-README-DIAGRAM DOC-README-MODULE-TABLE DOC-README-ROUTE DOC-README-EXAMPLE-PLACEMENT",
                "The root README no longer serves as a compact map to module owners, consumer routes and detailed guides.",
                "Restore the two-column seven-module map, dependency diagram under the design section and API/UI flow under check execution; restore required reader routes and section anchors, and keep compilable examples in the Playwright/Selenium owner guides.",
                "README.md#the-design-in-one-view", documentation);
        help(result, "DOC-TITLE DOC-HEADING DOC-FENCE DOC-FENCE-LANGUAGE DOC-README-DETAILS DOC-README-STYLE DOC-EDITORIAL",
                "Maintained Markdown has invalid structure, presentation markup or unresolved editorial artifacts.",
                "Keep one H1, descend one heading level at a time, label fenced examples, balance details/summary and repair the actual prose or diagram label; literal code remains data. Recheck the affected document and review its rendering and translation meaning.",
                policy + "#reader-orientation-and-diagrams", documentation);
        help(result, "DOC-BUILD-FACT DOC-COMMAND DOC-DEFAULT",
                "Documented execution/configuration differs from the source-owned declaration.",
                "Compare the reported command/default with the actual POM, suite and resource; correct the stale owner instead of changing runtime defaults to fit prose.",
                policy + "#portable-paths-and-executable-facts", documentation);
        help(result, "DOC-README-EXAMPLE DOC-JAVA-COMPILE",
                "A required consumer example is absent or does not compile against the current public API.",
                "Inspect the compiler evidence and repair the example at its README owner; retain a useful consumer assertion and the maintained compile fence.",
                "docs/Testing.md", documentation);
        help(result, "DOC-PAIR",
                "The maintained living architecture HTML and Markdown no longer describe the same source contract.",
                "Reconcile both formats, source digest, real headings and local navigation; retain the essential document in both formats without adding executable scripts.",
                "docs/Living_Architecture_UI_API_doc_v1_0.md", documentation);
        help(result, "DOC-EDITION DOC-EDITION-ANCHOR DOC-EDITION-CONTENT DOC-EDITION-INVENTORY DOC-EDITION-PAIR DOC-EDITION-PARITY DOC-EDITION-FACTS",
                "The declared documentation edition or its source/translation identity is inconsistent.",
                "Restore the exact en/ro policy or reconcile the reported pair; compare the reported protected-item ordinal and preserve fenced/inline code, numeric table facts, commands, links, anchors, topology and non-document source, then run the explicit peer check. Review prose meaning separately.",
                "docs/governance/Readme_Editions.md", documentation);
        help(result, "DOC-LIVING-PRINCIPLES",
                "A retained living-architecture principle, real owner/check route or declared proof limit drifted.",
                "Restore LD-01 through LD-12 exactly once and resolve the real source/check owners. Keep LD-02 UIActions ownership, LD-03 Selenium/Playwright waits and LD-04 WaitUtils/FAIL limits in their own rows; inspect the source conditions, not just matching names. Retain MAINTAINED/PARTIAL boundaries and keep cleanup PARTIAL until independent account-deletion evidence exists; synchronize the paired HTML.",
                "docs/Living_Architecture_UI_API_doc_v1_0.md#retained-principles", documentation);
        help(result, "DOC-FEEDBACK-EMPTY",
                "The active backlog is empty without a deliberate declaration, or its declaration contradicts active work.",
                "For zero accepted ATF_AI tasks, keep the register header and one standalone <!-- feedback:empty --> in Active TODOs, outside fences. For active rows, remove that marker. Preserve closed evidence and classify foreign or withdrawn scope separately; an empty backlog is not proof of defect-free code.",
                "docs/feedback/README.md#recording-and-closing-feedback", documentation);
        help(result, "DOC-FEEDBACK DOC-FEEDBACK-PRIORITY DOC-FEEDBACK-QUALITY DOC-FEEDBACK-SCHEMA DOC-LIFECYCLE",
                "Feedback ownership, assessment or evidence lifecycle no longer matches its declared scope.",
                "Correct the specific row/owner/state and retain baseline scores and original failures; close only demonstrated criteria and keep stable usage at its canonical owner.",
                "docs/feedback/README.md", documentation);
        help(result, "DOC-CODE-LANGUAGE DOC-LANGUAGE DOC-ENGLISH-REQUIREMENT",
                "Maintained prose or example code violates the English requirement or its explicit README-only edition exception.",
                "Restore the mandatory English contract and translate authored prose/code at its owner; keep executable examples unchanged in the Romanian README edition.",
                policy + "#english-source-and-comments", documentation);
        help(result, "ENG-COMMENT ENG-DIAGNOSTIC ENG-EXCEPTION-SCOPE ENG-IDENTIFIER ENG-STALE-EXCEPTION",
                "Authored Java or an exact language-data exception violates the English source contract.",
                "Use English names, comments and messages; preserve external payload data only through an exact reviewed fixture owner, and remove stale exceptions.",
                policy + "#english-source-and-comments", "JavaSourceScannerTest");
        return Map.copyOf(result);
    }

    private static void help(Map<String, DiagnosticHelp> result, String codes, String meaning,
                             String repair, String owner, String selection) {
        DiagnosticHelp help = new DiagnosticHelp(meaning, repair, owner, selection);
        for (String code : codes.split(" ")) {
            if (result.put(code, help) != null) throw new IllegalArgumentException("Duplicate diagnostic help: " + code);
        }
    }

    private static final class DiagnosticHelp {
        final String meaning;
        final String repair;
        final String owner;
        final String selection;

        DiagnosticHelp(String meaning, String repair, String owner, String selection) {
            this.meaning = meaning;
            this.repair = repair;
            this.owner = owner;
            this.selection = selection;
        }
    }

    static final class Fixture implements AutoCloseable {
        final Path root;

        Fixture() throws IOException {
            root = Files.createTempDirectory("atf-governance-").toAbsolutePath().normalize();
        }

        Fixture(Path parent) throws IOException {
            root = Files.createTempDirectory(parent, "atf-governance-").toAbsolutePath().normalize();
        }

        void write(String path, String source) throws IOException {
            Path target = root.resolve(path).normalize();
            if (!target.startsWith(root)) throw new IllegalArgumentException("Fixture path escapes root");
            Files.createDirectories(target.getParent());
            Files.writeString(target, source);
        }

        @Override
        public void close() throws IOException {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(path);
            }
        }
    }
}
