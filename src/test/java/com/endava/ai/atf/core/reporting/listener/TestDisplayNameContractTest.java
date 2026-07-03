package com.endava.ai.atf.core.reporting.listener;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.TestResult;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.LISTENER_INTERNAL_FORMATTING_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.TEST_DISPLAY_NAME_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class TestDisplayNameContractTest {

    @Test
    public void normalMethodsKeepSimpleRuntimeDisplayName() {
        Assert.assertEquals(
                displayName(result(TestDisplayNameContractTest.class, "plain_contract")),
                "plain_contract"
        );
    }

    @Test
    public void parameterizedMethodsExposeParametersInRuntimeDisplayName() {
        TestResult result = result(TestDisplayNameContractTest.class, "window_size_contract");
        result.setParameters(new Object[]{"desktop", 1366, 768});

        Assert.assertEquals(
                displayName(result),
                "window_size_contract [desktop, 1366, 768]"
        );
    }

    @Test
    public void nestedClassesExposeClassDiscriminatorInRuntimeDisplayName() {
        Assert.assertEquals(
                displayName(result(ExpectedFailingTestA.class, "fails_as_expected")),
                "fails_as_expected [TestDisplayNameContractTest$ExpectedFailingTestA]"
        );
    }

    private static String displayName(ITestResult result) {
        try {
            Class<?> type = Class.forName("com.endava.ai.core.reporting.listener.internal.TestDisplayName");
            Method from = type.getDeclaredMethod("from", ITestResult.class);
            from.setAccessible(true);
            return (String) from.invoke(null, result);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("TestDisplayName internal contract is no longer reachable.", exception);
        }
    }

    private static TestResult result(Class<?> realClass, String methodName) {
        TestResult result = TestResult.newEmptyTestResult();
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn(methodName);
        when(method.getRealClass()).thenReturn((Class) realClass);
        result.setMethod(method);
        return result;
    }

    public static final class ExpectedFailingTestA {
        private ExpectedFailingTestA() {
        }
    }
}
