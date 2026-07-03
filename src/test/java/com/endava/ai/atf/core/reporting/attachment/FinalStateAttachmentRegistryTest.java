package com.endava.ai.atf.core.reporting.attachment;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.attachment.FinalStateAttachmentHandler;
import com.endava.ai.core.reporting.attachment.FinalStateAttachmentRegistry;
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

    @AfterMethod
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
        FinalStateAttachmentRegistry.clearForTests();

        FinalStateAttachmentRegistry.onTestSuccess();

        Assert.assertTrue(true, "No handler must be a safe no-op.");
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
