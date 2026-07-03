package com.endava.ai.skills.atlas.tests.catalog;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class AtlasLombokArchitectureContractTest {
    private static final List<Path> ATLAS_ROOTS = List.of(
            AtfSourcePaths.MAIN_ATLAS_ROOT,
            AtfSourcePaths.TEST_ATLAS_ROOT
    );
    private static final List<Pattern> DISCIPLINED_LOMBOK_PATTERNS = List.of(
            Pattern.compile("@(?:Data|Setter|SneakyThrows)\\b"),
            Pattern.compile("@AllArgsConstructor\\s*\\(\\s*access\\s*=\\s*AccessLevel\\.PUBLIC\\s*\\)"),
            Pattern.compile("import\\s+lombok\\.(?!(?:AccessLevel|AllArgsConstructor|Builder|Getter|NoArgsConstructor|"
                    + "NonNull|RequiredArgsConstructor|Value|With|experimental\\.Accessors);)\\S+;")
    );

    @Test
    public void atlasLombokUsageStaysBroadDisciplinedAndImmutable() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path root : ATLAS_ROOTS) {
            collectViolations(root, violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "Atlas may use Lombok broadly for immutable report models, YAML specs, matrix rows and test audit DTOs. "
                        + "Avoid mutable shortcuts, hidden throws or public Lombok all-args constructors: " + violations);
    }

    private static void collectViolations(Path root, List<String> violations) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .forEach(path -> collectViolationsIn(path, violations));
        }
    }

    private static void collectViolationsIn(Path path, List<String> violations) {
        String source = read(path);
        for (Pattern pattern : DISCIPLINED_LOMBOK_PATTERNS) {
            Matcher matcher = pattern.matcher(source);
            while (matcher.find()) {
                violations.add(normalize(path) + ":" + lineNumber(source, matcher.start())
                        + " -> " + matcher.group());
            }
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Atlas source " + path, exception);
        }
    }

    private static long lineNumber(String source, int offset) {
        return source.substring(0, offset).chars().filter(ch -> ch == '\n').count() + 1;
    }

    private static String normalize(Path path) {
        return AtfSourcePaths.unixPath(path.normalize());
    }
}
