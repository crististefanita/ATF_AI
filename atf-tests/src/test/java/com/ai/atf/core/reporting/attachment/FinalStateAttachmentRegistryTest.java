package com.ai.atf.core.reporting.attachment;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.attachment.FinalStateAttachmentHandler;
import com.ai.core.reporting.attachment.FinalStateAttachmentRegistry;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.FINAL_STATE_ATTACHMENT_REGISTRY_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.FINAL_STATE_ATTACHMENT_REGISTRY_TEST_CLASS)
@Listeners(TestListener.class)
public class FinalStateAttachmentRegistryTest {

    private CountingFinalStateHandler handler;

    @BeforeMethod
    public void setUp() {
        handler = new CountingFinalStateHandler();
        FinalStateAttachmentRegistry.clearForTests();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        FinalStateAttachmentRegistry.clearForTests();
    }

    @Test
    public void success_callback_reaches_registered_final_state_handler() {
        FinalStateAttachmentRegistry.register(handler);

        FinalStateAttachmentRegistry.onTestSuccess();

        Assert.assertEquals(handler.calls, 1);
    }

    @Test
    public void success_callback_is_safe_without_handler() {
        FinalStateAttachmentRegistry.register(handler);
        FinalStateAttachmentRegistry.clearForTests();

        FinalStateAttachmentRegistry.onTestSuccess();

        Assert.assertEquals(handler.calls, 0, "A cleared optional handler must not receive a stale callback.");
        FinalStateAttachmentRegistry.register(handler);
        FinalStateAttachmentRegistry.onTestSuccess();
        Assert.assertEquals(handler.calls, 1, "A later registration must still receive the next success callback.");
    }

    @Test
    public void success_callback_reaches_multiple_handlers_once_each() {
        SecondCountingFinalStateHandler second = new SecondCountingFinalStateHandler();
        FinalStateAttachmentRegistry.register(handler);
        FinalStateAttachmentRegistry.register(second);
        FinalStateAttachmentRegistry.register(second);

        FinalStateAttachmentRegistry.onTestSuccess();

        Assert.assertEquals(handler.calls, 1);
        Assert.assertEquals(second.calls, 1);
    }

    private static final class CountingFinalStateHandler implements FinalStateAttachmentHandler {
        private int calls;

        @Override
        public void onTestSuccess() {
            calls++;
        }
    }

    private static final class SecondCountingFinalStateHandler implements FinalStateAttachmentHandler {
        private int calls;

        @Override
        public void onTestSuccess() {
            calls++;
        }
    }
}
