package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.ContactMessageActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.ContactMessageContext;
import com.endava.ai.ui.validation.ContactMessageValidation;
import org.testng.annotations.Test;

public class ContactMessageRun02DetailsTests extends BaseTestUI {
    private final ContactMessageActions actions = new ContactMessageActions();
    private final ContactMessageValidation validation = new ContactMessageValidation();

    @Test
    public void support_message_details_open_from_row() {
        ContactMessageContext context = actions.createMessageAndOpenDetails();
        validation.assertDetailsRouteOpensFromRow(context);
    }

    @Test
    public void support_message_details_preserve_subject_and_status() {
        ContactMessageContext context = actions.createMessageAndOpenDetails();
        validation.assertDetailsPreserveSubjectAndStatus(context);
    }

    @Test
    public void support_message_details_preserve_customer_identity() {
        ContactMessageContext context = actions.createMessageAndOpenDetails();
        validation.assertDetailsPreserveCustomerIdentity(context);
    }

    @Test
    public void support_message_details_expose_reply_surface() {
        ContactMessageContext context = actions.createMessageAndOpenDetails();
        validation.assertDetailsExposeReplySurface(context);
    }

    @Test
    public void support_message_details_preserve_timestamp() {
        ContactMessageContext context = actions.createMessageAndOpenDetails();
        validation.assertDetailsPreserveDate(context);
    }
}
