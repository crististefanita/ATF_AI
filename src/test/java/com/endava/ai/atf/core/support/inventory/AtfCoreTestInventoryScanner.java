package com.endava.ai.atf.core.support.inventory;

import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.capability.AtfCapabilityCatalog;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AtfCoreTestInventoryScanner {

    private static final Pattern TEST_ANNOTATION = Pattern.compile("(?m)^\\s*@Test\\b");
    private static final Pattern ATF_CONTRACT_ANNOTATION = Pattern.compile("(?m)^\\s*@AtfContract(?:Ref)?\\b");
    private static final Pattern ATF_CONTRACT_SUITE_ANNOTATION = Pattern.compile("(?m)^\\s*@AtfContractSuite(?:Ref)?\\b");
    private static final Pattern ATF_CONTRACT_PROFILE = Pattern.compile("profile\\s*=\\s*AtfContractProfile\\.([A-Z_]+)");
    private static final Pattern ATF_CONTRACT_SUITE_REF = Pattern.compile("@AtfContractSuiteRef\\(\"([^\"]+)\"\\)");
    private static final Pattern ATF_CONTRACT_SUITE_CONSTANT_REF = Pattern.compile(
            "@AtfContractSuiteRef\\(AtfContractNarrativeIds\\.Suite\\.([A-Z0-9_]+)\\)"
    );
    private static final Pattern TEST_LISTENER_ANNOTATION = Pattern.compile("(?m)^\\s*@Listeners\\(TestListener\\.class\\)");
    private static final Pattern NO_FRAMEWORK_REPORTING_ANNOTATION = Pattern.compile("(?m)^\\s*@NoFrameworkReporting\\b");
    private static final Pattern ALLURE_ONLY_GROUP = Pattern.compile(
            "\"(?:" + AtfTestGroups.ALLURE_ONLY + ")\"|AtfTestGroups\\.ALLURE_ONLY"
    );
    private static final Pattern EXTENT_ONLY_GROUP = Pattern.compile(
            "\"(?:" + AtfTestGroups.EXTENT_ONLY + ")\"|AtfTestGroups\\.EXTENT_ONLY"
    );

    private AtfCoreTestInventoryScanner() {
    }

    static List<AtfCoreTestSource> testSources(Path root) throws IOException {
        List<AtfCoreTestSource> sources = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .sorted()
                    .collect(Collectors.toList())) {
                String text = Files.readString(path);
                sources.add(new AtfCoreTestSource(
                        AtfSourcePaths.unixPath(root.relativize(path)),
                        countTestMethods(text),
                        contains(ATF_CONTRACT_ANNOTATION, text),
                        contains(ATF_CONTRACT_SUITE_ANNOTATION, text),
                        profile(text),
                        contains(TEST_LISTENER_ANNOTATION, text),
                        contains(NO_FRAMEWORK_REPORTING_ANNOTATION, text),
                        contains(ALLURE_ONLY_GROUP, text),
                        contains(EXTENT_ONLY_GROUP, text)
                ));
            }
        }
        return sources;
    }

    static Map<String, List<AtfCoreTestSource>> profileMatrix(List<AtfCoreTestSource> sources) {
        Map<String, List<AtfCoreTestSource>> profiles = new LinkedHashMap<>();
        sources.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::suiteProfile)
                        .thenComparing(AtfCoreTestSource::relativePath))
                .forEach(source -> profiles
                        .computeIfAbsent(source.suiteProfile(), ignored -> new ArrayList<>())
                        .add(source));
        return profiles;
    }

    static Map<String, Long> catalogProfileMatrix() {
        return AtfCapabilityCatalog.all().stream()
                .collect(Collectors.groupingBy(
                        spec -> spec.profile().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    static Set<String> catalogProfiles() {
        return AtfCapabilityCatalog.all().stream()
                .map(spec -> spec.profile().name())
                .collect(Collectors.toSet());
    }

    static Set<String> suiteProfiles(List<AtfCoreTestSource> sources) {
        return sources.stream()
                .map(AtfCoreTestSource::suiteProfile)
                .collect(Collectors.toSet());
    }

    private static int countTestMethods(String text) {
        int classIndex = text.indexOf("class ");
        if (classIndex < 0) {
            return count(TEST_ANNOTATION, text);
        }
        int classBodyIndex = text.indexOf('{', classIndex);
        if (classBodyIndex < 0 || classBodyIndex + 1 >= text.length()) {
            return 0;
        }
        return count(TEST_ANNOTATION, text.substring(classBodyIndex + 1));
    }

    private static int count(Pattern pattern, String text) {
        return (int) pattern.matcher(text).results().count();
    }

    private static boolean contains(Pattern pattern, String text) {
        return pattern.matcher(text).find();
    }

    private static String profile(String text) {
        java.util.regex.Matcher matcher = ATF_CONTRACT_PROFILE.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        java.util.regex.Matcher suiteRefMatcher = ATF_CONTRACT_SUITE_REF.matcher(text);
        if (suiteRefMatcher.find()) {
            return AtfCoreYamlContractCatalog.requireContractNarrativeSuite(suiteRefMatcher.group(1)).suiteProfile();
        }
        java.util.regex.Matcher suiteConstantRefMatcher = ATF_CONTRACT_SUITE_CONSTANT_REF.matcher(text);
        if (suiteConstantRefMatcher.find()) {
            return AtfCoreYamlContractCatalog.requireContractNarrativeSuite(
                    suiteIdConstant(suiteConstantRefMatcher.group(1))).suiteProfile();
        }
        return "UNDECLARED";
    }

    private static String suiteIdConstant(String fieldName) {
        try {
            Field field = AtfContractNarrativeIds.Suite.class.getField(fieldName);
            return (String) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unknown AtfContractNarrativeIds.Suite constant: " + fieldName, e);
        }
    }
}
