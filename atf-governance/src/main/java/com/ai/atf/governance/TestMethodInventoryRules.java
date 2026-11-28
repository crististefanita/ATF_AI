package com.ai.atf.governance;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;

import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Pins executable TestNG method identities independently of the selected class list. */
final class TestMethodInventoryRules {
    static final String INVENTORY = "atf-governance/src/test/resources/governance/test-method-inventory.txt";
    private static final String INVENTORY_SHA256 = "d28d55a021c10b8aa84750a5f989b6febba501083689b01737492523e8812099";

    private TestMethodInventoryRules() { }

    static List<String> validate(Path root) throws Exception {
        Path inventory = root.resolve(INVENTORY);
        if (!Files.isRegularFile(inventory)) {
            return List.of(INVENTORY + ": TEST-METHOD-INVENTORY missing the reviewed executable-method baseline");
        }
        if (!INVENTORY_SHA256.equals(sha256(inventory))) {
            return List.of(INVENTORY + ": TEST-METHOD-INVENTORY baseline digest changed; review the method inventory and its owner update together");
        }
        List<String> expected = Files.readAllLines(inventory, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .collect(Collectors.toList());
        List<String> violations = validateEntries(expected);
        Set<String> current = new TreeSet<>(inventory(root.resolve("atf-tests/src/test/java"), violations));
        current.addAll(inventory(root.resolve("atf-governance/src/test/java"), violations));
        violations.addAll(compare(expected, current));
        return violations.stream().distinct().collect(Collectors.toList());
    }

    static List<String> validateEntries(List<String> expected) {
        List<String> violations = new ArrayList<>();
        String previous = null;
        for (String entry : expected) {
            if (!entry.matches("[A-Za-z_$][\\w$]*(?:\\.[A-Za-z_$][\\w$]*)*#[A-Za-z_$][\\w$]*\\([^\\r\\n]*\\)")) {
                violations.add("TEST-METHOD-INVENTORY malformed executable-method identity: " + entry);
            }
            if (previous != null && previous.compareTo(entry) >= 0) {
                violations.add("TEST-METHOD-INVENTORY entries must be unique and sorted: " + entry);
            }
            previous = entry;
        }
        if (expected.isEmpty()) violations.add("TEST-METHOD-INVENTORY baseline must not be empty");
        return violations;
    }

    static List<String> compare(List<String> expected, Set<String> current) {
        Set<String> pinned = new TreeSet<>(expected);
        Set<String> removed = new TreeSet<>(pinned);
        removed.removeAll(current);
        Set<String> unregistered = new TreeSet<>(current);
        unregistered.removeAll(pinned);
        List<String> findings = new ArrayList<>();
        for (String method : removed) findings.add("TEST-METHOD-REMOVED " + method
                + " is absent from executable test source; confirm an intentional proof retirement and update the reviewed inventory");
        for (String method : unregistered) findings.add("TEST-METHOD-UNREGISTERED " + method
                + " needs an explicit baseline review; update the inventory only after reviewing the changed test contract");
        return findings;
    }

    static Set<String> inventory(Path sourceRoot, List<String> violations) throws IOException {
        List<Path> sources = GovernanceFiles.files(sourceRoot, ".java");
        if (sources.isEmpty()) {
            violations.add("TEST-METHOD-SOURCE source inventory must not be empty");
            return Set.of();
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Test method governance requires a JDK compiler");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        Set<String> methods = new TreeSet<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                    List.of("-proc:none", "--release", "11"), null, files.getJavaFileObjectsFromPaths(sources));
            for (CompilationUnitTree unit : task.parse()) {
                boolean imported = unit.getImports().stream().anyMatch(value -> !value.isStatic()
                        && Set.of("org.testng.annotations.Test", "org.testng.annotations.*")
                        .contains(value.getQualifiedIdentifier().toString()));
                for (Tree declaration : unit.getTypeDecls()) {
                    if (!(declaration instanceof ClassTree)) continue;
                    ClassTree type = (ClassTree) declaration;
                    if (type.getModifiers().getFlags().contains(Modifier.ABSTRACT)) continue;
                    boolean shadowed = unit.getTypeDecls().stream().anyMatch(TestMethodInventoryRules::declaresTestType)
                            || type.getMembers().stream().anyMatch(TestMethodInventoryRules::declaresTestType);
                    boolean classTest = testAnnotation(type.getModifiers(), imported && !shadowed);
                    for (Tree member : type.getMembers()) {
                        if (!(member instanceof MethodTree)) continue;
                        MethodTree method = (MethodTree) member;
                        if (method.getReturnType() == null) continue;
                        boolean methodTest = testAnnotation(method.getModifiers(), imported && !shadowed);
                        boolean publicClassTest = classTest && method.getModifiers().getFlags().contains(Modifier.PUBLIC);
                        if (!methodTest && !publicClassTest) continue;
                        String className = unit.getPackageName() == null ? type.getSimpleName().toString()
                                : unit.getPackageName() + "." + type.getSimpleName();
                        String parameters = method.getParameters().stream().map(parameter -> parameter.getType().toString())
                                .collect(Collectors.joining(","));
                        methods.add(className + "#" + method.getName() + "(" + parameters + ")");
                    }
                }
            }
        }
        diagnostics.getDiagnostics().stream().filter(value -> value.getKind() == Diagnostic.Kind.ERROR)
                .forEach(value -> violations.add("TEST-METHOD-SOURCE " + value));
        return methods;
    }

    /** Seals UTF-8 content with Git CRLF/LF equivalence; all other content remains protected. */
    static String sha256(Path path) throws Exception {
        byte[] canonical = Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n")
                .getBytes(StandardCharsets.UTF_8);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical);
        StringBuilder result = new StringBuilder();
        for (byte value : digest) result.append(String.format("%02x", value & 0xff));
        return result.toString();
    }

    private static boolean testAnnotation(com.sun.source.tree.ModifiersTree modifiers, boolean imported) {
        return modifiers.getAnnotations().stream().anyMatch(value -> value.getAnnotationType().toString().equals("org.testng.annotations.Test")
                || imported && value.getAnnotationType().toString().equals("Test"));
    }

    private static boolean declaresTestType(Tree declaration) {
        return declaration instanceof ClassTree && ((ClassTree) declaration).getSimpleName().contentEquals("Test");
    }
}
