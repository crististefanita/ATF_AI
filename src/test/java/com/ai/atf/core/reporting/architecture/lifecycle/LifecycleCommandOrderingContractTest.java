package com.ai.atf.core.reporting.architecture.lifecycle;

import com.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.lifecycle.LifecycleCommand;
import com.ai.core.reporting.listener.internal.lifecycle.LifecycleCommandRunner;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.LIFECYCLE_COMMAND_ORDERING_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.LIFECYCLE_COMMAND_ORDERING_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class LifecycleCommandOrderingContractTest {
    private static final Path TEST_LISTENER_SOURCE = AtfSourcePaths.mainJava(TestListener.class);
    private static final String COMMAND_RUNNER_CALL = "LifecycleCommandRunner.executeAll(List.of(";
    private static final String BEGIN_SUITE_COMMAND = "\"reporting.beginSuite\"";
    private static final String SESSION_CLEAR_COMMAND = "\"session.clear\"";
    private static final String RUNTIME_AUDIT_COMMAND = "\"runtimeAudit.publishIfExtent\"";
    private static final String END_SUITE_COMMAND = "\"reporting.endSuite\"";

    @Test
    public void lifecycleCommandRunnerExecutesCommandsInDeclaredOrder() {
        List<String> sideEffects = new ArrayList<>();

        List<String> executed = LifecycleCommandRunner.executeAll(List.of(
                LifecycleCommand.named("first", () -> sideEffects.add("first")),
                LifecycleCommand.named("second", () -> sideEffects.add("second")),
                LifecycleCommand.named("third", () -> sideEffects.add("third"))
        ));

        Assert.assertEquals(executed, List.of("first", "second", "third"));
        Assert.assertEquals(sideEffects, executed);
    }

    @Test
    public void testListenerLifecycleCallbacksUseNamedCommandRunner() throws IOException {
        String source = ArchitectureSourceScanner.read(TEST_LISTENER_SOURCE);

        String onStart = methodBody(source, "onStart");
        String onFinish = methodBody(source, "onFinish");

        Assert.assertTrue(onStart.contains(COMMAND_RUNNER_CALL),
                "onStart must be command-driven.");
        Assert.assertTrue(onFinish.contains(COMMAND_RUNNER_CALL),
                "onFinish must be command-driven.");
        Assert.assertTrue(onStart.contains(BEGIN_SUITE_COMMAND));
        Assert.assertTrue(onStart.contains(SESSION_CLEAR_COMMAND));
        Assert.assertTrue(onFinish.contains(RUNTIME_AUDIT_COMMAND));
        Assert.assertTrue(onFinish.contains(END_SUITE_COMMAND));
    }

    private static String methodBody(String source, String methodName) {
        int methodIndex = source.indexOf("void " + methodName + "(");
        if (methodIndex < 0) {
            return "";
        }
        int openBrace = source.indexOf('{', methodIndex);
        int depth = 0;
        for (int i = openBrace; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(openBrace, i + 1);
                }
            }
        }
        return source.substring(openBrace);
    }
}
