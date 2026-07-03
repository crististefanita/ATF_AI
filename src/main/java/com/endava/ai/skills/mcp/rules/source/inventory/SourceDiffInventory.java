package com.endava.ai.skills.mcp.rules.source.inventory;

import com.endava.ai.skills.mcp.fs.DiffFileExtractor;
import com.endava.ai.skills.mcp.rules.source.ProtectedSourceSurface;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Cold facts extracted from the final root source diff.
 */
@Getter
@Accessors(fluent = true)
public final class SourceDiffInventory {
    private static final Pattern GIT_HEADER = Pattern.compile("(?im)^diff --git\\s+\"?a/(.+?)\"?\\s+\"?b/(.+?)\"?$");
    private static final Pattern TEST_METHOD_ADDED = Pattern.compile("(?m)^\\+\\s*@Test\\b");
    private static final Pattern DIRECT_UI_ENGINE = Pattern.compile("(?im)^\\+.*\\b(?:playwright|webdriver|selenium)\\b");

    private final int sourceFiles;
    private final int srcMainFiles;
    private final int srcTestFiles;
    private final int added;
    private final int modified;
    private final int deleted;
    private final int testMethodsAdded;
    private final int protectedFiles;
    private final int testClasses;
    private final int supportClasses;
    private final int pageComponentClasses;
    private final int directUiEngineLeakage;
    private final int testMethodsUsingSupport;
    private final int supportCohesionScore;
    private final int duplicateActionLineRatio;
    private final int duplicateAssertionLineRatio;
    private final int layeringViolationCount;
    private final int assertionLocalityScore;
    private final int refactorBurnDown;

    private SourceDiffInventory(int sourceFiles,
                                int srcMainFiles,
                                int srcTestFiles,
                                int added,
                                int modified,
                                int deleted,
                                int testMethodsAdded,
                                int protectedFiles,
                                int testClasses,
                                int supportClasses,
                                int pageComponentClasses,
                                int directUiEngineLeakage,
                                int testMethodsUsingSupport,
                                int supportCohesionScore,
                                int duplicateActionLineRatio,
                                int duplicateAssertionLineRatio,
                                int layeringViolationCount,
                                int assertionLocalityScore,
                                int refactorBurnDown) {
        this.sourceFiles = sourceFiles;
        this.srcMainFiles = srcMainFiles;
        this.srcTestFiles = srcTestFiles;
        this.added = added;
        this.modified = modified;
        this.deleted = deleted;
        this.testMethodsAdded = testMethodsAdded;
        this.protectedFiles = protectedFiles;
        this.testClasses = testClasses;
        this.supportClasses = supportClasses;
        this.pageComponentClasses = pageComponentClasses;
        this.directUiEngineLeakage = directUiEngineLeakage;
        this.testMethodsUsingSupport = testMethodsUsingSupport;
        this.supportCohesionScore = supportCohesionScore;
        this.duplicateActionLineRatio = duplicateActionLineRatio;
        this.duplicateAssertionLineRatio = duplicateAssertionLineRatio;
        this.layeringViolationCount = layeringViolationCount;
        this.assertionLocalityScore = assertionLocalityScore;
        this.refactorBurnDown = refactorBurnDown;
    }

    public static SourceDiffInventory from(String diff) {
        Set<String> sourceFiles = sourceFiles(diff);
        int added = 0;
        int deleted = 0;
        int modified = 0;
        Matcher matcher = GIT_HEADER.matcher(diff);
        while (matcher.find()) {
            String block = diff.substring(matcher.start(), nextHeaderStart(diff, matcher.end()));
            if (block.contains("\nnew file mode") || block.contains("\n--- /dev/null")) {
                added++;
            } else if (block.contains("\ndeleted file mode") || block.contains("\n+++ /dev/null")) {
                deleted++;
            } else {
                modified++;
            }
        }
        if (added + modified + deleted == 0 && !sourceFiles.isEmpty()) {
            modified = sourceFiles.size();
        }

        int testMethodsAdded = countMatches(diff, TEST_METHOD_ADDED);
        int supportClasses = (int) sourceFiles.stream()
                .filter(path -> path.endsWith(".java") && path.contains("/support/"))
                .count();
        int testClasses = (int) sourceFiles.stream()
                .filter(path -> path.startsWith("src/test/") && path.endsWith(".java"))
                .count();
        SupportQualityInventory supportQuality = SupportQualityInventory.from(
                diff, testMethodsAdded, supportClasses, testClasses);

        return new SourceDiffInventory(
                sourceFiles.size(),
                (int) sourceFiles.stream().filter(path -> path.startsWith("src/main/")).count(),
                (int) sourceFiles.stream().filter(path -> path.startsWith("src/test/")).count(),
                added,
                modified,
                deleted,
                testMethodsAdded,
                (int) sourceFiles.stream().filter(path -> ProtectedSourceSurface.match(path).isPresent()).count(),
                testClasses,
                supportClasses,
                (int) sourceFiles.stream()
                        .filter(path -> path.endsWith(".java"))
                        .filter(path -> path.contains("/page") || path.contains("/component"))
                        .count(),
                countMatches(diff, DIRECT_UI_ENGINE),
                supportQuality.testMethodsUsingSupport(),
                supportQuality.supportCohesionScore(),
                supportQuality.duplicateActionLineRatio(),
                supportQuality.duplicateAssertionLineRatio(),
                supportQuality.layeringViolationCount(),
                supportQuality.assertionLocalityScore(),
                supportQuality.refactorBurnDown()
        );
    }

    private static int nextHeaderStart(String diff, int from) {
        Matcher next = GIT_HEADER.matcher(diff);
        return next.find(from) ? next.start() : diff.length();
    }

    public static Set<String> sourceFiles(String diff) {
        return new DiffFileExtractor().referencedFiles(diff).stream()
                .map(path -> path.replace('\\', '/').toLowerCase(Locale.ROOT))
                .filter(path -> path.startsWith("src/"))
                .collect(Collectors.toSet());
    }

    private static int countMatches(String content, Pattern pattern) {
        int count = 0;
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

}
