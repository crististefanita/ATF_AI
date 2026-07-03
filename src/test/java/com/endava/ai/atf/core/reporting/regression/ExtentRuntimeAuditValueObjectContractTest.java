package com.endava.ai.atf.core.reporting.regression;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditSnapshot;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_RUNTIME_AUDIT_VALUE_OBJECT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_RUNTIME_AUDIT_VALUE_OBJECT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ExtentRuntimeAuditValueObjectContractTest {

    @Test
    public void runtimeInvocationNormalizesNullableTextFields() {
        ExtentRuntimeInvocation invocation = new ExtentRuntimeInvocation(
                " PASSED ",
                " com.example.Test ",
                " shouldPass ",
                123L,
                null,
                null
        );

        Assert.assertEquals(invocation.status(), "PASSED");
        Assert.assertEquals(invocation.className(), "com.example.Test");
        Assert.assertEquals(invocation.methodName(), "shouldPass");
        Assert.assertEquals(invocation.failureDetail(), "");
        Assert.assertEquals(invocation.annotationTags(), List.of());
    }

    @Test
    public void runtimeInvocationDoesNotRetainMutableAnnotationTags() {
        List<String> tags = new ArrayList<>();
        tags.add("@Smoke");

        ExtentRuntimeInvocation invocation = new ExtentRuntimeInvocation(
                "FAILED",
                "com.example.Test",
                "shouldFail",
                456L,
                "boom",
                tags
        );

        tags.add("@LateMutation");

        Assert.assertEquals(invocation.annotationTags(), List.of("@Smoke"));
        Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> invocation.annotationTags().add("@CannotMutate")
        );
    }

    @Test
    public void runtimeAuditSnapshotSortsAndDetachesFromMutableSource() {
        List<ExtentRuntimeInvocation> source = Collections.synchronizedList(new ArrayList<>());
        source.add(new ExtentRuntimeInvocation("PASSED", "b.Test", "second", 20L, "", List.of()));
        source.add(new ExtentRuntimeInvocation("FAILED", "a.Test", "first", 10L, "boom", List.of("@Smoke")));

        List<ExtentRuntimeInvocation> snapshot = RuntimeAuditSnapshot.sortedCopy(source);
        source.clear();

        Assert.assertEquals(snapshot.size(), 2);
        Assert.assertEquals(snapshot.get(0).line(), "FAILED | a.Test#first");
        Assert.assertEquals(snapshot.get(1).line(), "PASSED | b.Test#second");
    }
}
