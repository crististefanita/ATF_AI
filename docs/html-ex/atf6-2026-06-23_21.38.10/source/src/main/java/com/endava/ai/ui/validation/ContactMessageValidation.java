package com.endava.ai.ui.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.model.ContactMessageContext;
import org.testng.Assert;

public final class ContactMessageValidation {
    public void assertMessageRowCreated(ContactMessageContext context) {
        StepLogger.startStep("Validate authenticated support message row is created");
        String body = context.getListBody();
        Assert.assertTrue(body.contains("Messages"), "Expected Messages page.");
        Assert.assertTrue(body.contains(context.getSubjectSlug()), "Expected message row subject slug.");
        Assert.assertTrue(body.contains("Details"), "Expected message row Details action.");
        StepLogger.pass("Authenticated support message row is present");
    }

    public void assertMessagePreviewCarriesSubmittedText(ContactMessageContext context) {
        StepLogger.startStep("Validate message row preview carries submitted text");
        Assert.assertTrue(context.getListBody().contains(context.getMessagePreview()),
                "Expected row preview to contain message prefix: " + context.getMessagePreview());
        StepLogger.pass("Message preview binds row to submitted payload");
    }

    public void assertMessageStatusIsNew(ContactMessageContext context) {
        StepLogger.startStep("Validate support message status is NEW");
        Assert.assertTrue(context.getListBody().contains("NEW"), "Expected message row to expose NEW status.");
        StepLogger.pass("Message row exposes NEW status");
    }

    public void assertMessageDateIsVisible(ContactMessageContext context) {
        StepLogger.startStep("Validate support message date is visible");
        Assert.assertTrue(context.getListBody().matches("(?is).*\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}.*"),
                "Expected message row to expose timestamp.");
        StepLogger.pass("Message row exposes timestamp");
    }

    public void assertMessagesEmptyStateIsGone(ContactMessageContext context) {
        StepLogger.startStep("Validate messages empty state disappears after submit");
        Assert.assertFalse(context.getListBody().contains("There are no messages yet"),
                "Expected created message to replace empty state.");
        StepLogger.pass("Messages empty state is replaced by row");
    }

    public void assertDetailsRouteOpensFromRow(ContactMessageContext context) {
        StepLogger.startStep("Validate message details route opens from row");
        Assert.assertTrue(context.getDetailsUrl().contains("/account/messages/"),
                "Expected account message details route, but was: " + context.getDetailsUrl());
        Assert.assertTrue(context.getDetailsBody().contains(context.getMessage()),
                "Expected details page to contain full submitted message.");
        StepLogger.pass("Message details route opens from the row");
    }

    public void assertDetailsPreserveSubjectAndStatus(ContactMessageContext context) {
        StepLogger.startStep("Validate details preserve subject and status");
        Assert.assertTrue(context.getDetailsBody().contains("Subject: " + context.getSubjectSlug()),
                "Expected details page to expose subject slug.");
        Assert.assertTrue(context.getDetailsBody().contains("NEW"),
                "Expected details page to preserve NEW status.");
        StepLogger.pass("Details page preserves subject and status");
    }

    public void assertDetailsPreserveCustomerIdentity(ContactMessageContext context) {
        StepLogger.startStep("Validate details preserve customer identity");
        String body = context.getDetailsBody();
        Assert.assertTrue(body.contains(context.getCustomer().getFirstName())
                        || body.contains(context.getCustomer().getLastName()),
                "Expected details page to expose customer identity.");
        StepLogger.pass("Details page exposes customer identity");
    }

    public void assertDetailsExposeReplySurface(ContactMessageContext context) {
        StepLogger.startStep("Validate details expose reply surface");
        String body = context.getDetailsBody();
        Assert.assertTrue(body.contains("Replies"), "Expected Replies section.");
        Assert.assertTrue(body.contains("Add Reply"), "Expected Add Reply action.");
        Assert.assertTrue(body.contains("Reply"), "Expected Reply label/action.");
        StepLogger.pass("Details page exposes reply surface");
    }

    public void assertDetailsPreserveDate(ContactMessageContext context) {
        StepLogger.startStep("Validate details preserve timestamp");
        Assert.assertTrue(context.getDetailsBody().matches("(?is).*\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}.*"),
                "Expected details page to expose timestamp.");
        StepLogger.pass("Details page preserves timestamp");
    }

    public void assertReopenedMessagesStillContainRow(ContactMessageContext context) {
        StepLogger.startStep("Validate reopened messages still contain support row");
        assertMessageRowCreated(context);
        assertMessagePreviewCarriesSubmittedText(context);
        StepLogger.pass("Reopened messages page keeps the support row");
    }

    public void assertReloginMessagesStillContainRow(ContactMessageContext context) {
        StepLogger.startStep("Validate relogin keeps support row");
        assertMessageRowCreated(context);
        assertMessageStatusIsNew(context);
        StepLogger.pass("Relogin keeps message row");
    }

    public void assertMessageBelongsToAccountSurface(ContactMessageContext context) {
        StepLogger.startStep("Validate support message is account-owned");
        Assert.assertTrue(context.getDetailsUrl().contains("/account/messages/"),
                "Expected message details to stay under account messages.");
        Assert.assertTrue(context.getDetailsBody().contains(context.getMessage()),
                "Expected account-owned details to expose the submitted message.");
        StepLogger.pass("Support message is account-owned");
    }
}
