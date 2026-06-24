package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.ContactMessageActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.ContactMessageContext;
import com.endava.ai.ui.validation.ContactMessageValidation;
import org.testng.annotations.Test;

public class ContactMessageRun01SubmissionTests extends BaseTestUI {
    private final ContactMessageActions actions = new ContactMessageActions();
    private final ContactMessageValidation validation = new ContactMessageValidation();

    @Test
    public void authenticated_customer_creates_support_message_row() {
        ContactMessageContext context = actions.createMessageAndOpenMessages();
        validation.assertMessageRowCreated(context);
    }

    @Test
    public void support_message_row_carries_submitted_preview() {
        ContactMessageContext context = actions.createMessageAndOpenMessages();
        validation.assertMessagePreviewCarriesSubmittedText(context);
    }

    @Test
    public void support_message_row_starts_with_new_status() {
        ContactMessageContext context = actions.createMessageAndOpenMessages();
        validation.assertMessageStatusIsNew(context);
    }

    @Test
    public void support_message_row_has_a_business_timestamp() {
        ContactMessageContext context = actions.createMessageAndOpenMessages();
        validation.assertMessageDateIsVisible(context);
    }

    @Test
    public void support_message_replaces_messages_empty_state() {
        ContactMessageContext context = actions.createMessageAndOpenMessages();
        validation.assertMessagesEmptyStateIsGone(context);
    }
}
