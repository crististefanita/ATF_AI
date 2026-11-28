package com.ai.atf.governance;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verifies that diagnostic owners resolve and list the explicit documentation test owners.
 */
public class DocumentationGovernanceTest {
    @Test
    public void diagnosticOwnersResolveToRealContractsAndExistingGovernanceSelections() throws Exception {
        Path root = GovernanceFiles.root();
        Set<String> owners = new LinkedHashSet<>();
        Set<String> selections = new LinkedHashSet<>();
        for (String code : GovernanceFiles.diagnosticCodes()) {
            String report = GovernanceFiles.problems(List.of(code + " observed fixture evidence"));
            Matcher owner = Pattern.compile("(?m)^   Owner: (.+)$").matcher(report);
            Assert.assertTrue(owner.find(), "Diagnostic owner is missing for " + code);
            owners.add(owner.group(1));
            Matcher selection = Pattern.compile("-Dtest=([A-Za-z0-9,]+)\"").matcher(report);
            Assert.assertTrue(selection.find(), "Maintained recheck is missing for " + code);
            selections.addAll(Arrays.asList(selection.group(1).split(",")));
        }
        Assert.assertFalse(owners.isEmpty());
        for (String owner : owners) {
            String[] route = owner.split("#", 2);
            Path target = root.resolve(route[0]).normalize();
            Assert.assertTrue(target.startsWith(root) && Files.isRegularFile(target), "Diagnostic owner must resolve: " + owner);
            if (route.length == 2) Assert.assertTrue(MarkdownDocumentationRules.anchors(Files.readAllLines(target)).contains(route[1]),
                    "Diagnostic owner fragment must resolve: " + owner);
        }
        for (String selection : selections) {
            Class<?> owner = Class.forName("com.ai.atf.governance." + selection);
            Assert.assertTrue(Arrays.stream(owner.getMethods()).anyMatch(method -> method.isAnnotationPresent(Test.class)),
                    "Recheck must select an existing executable governance class: " + selection);
        }
    }

}
