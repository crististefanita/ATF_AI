package com.ai.ui.support.validation;

import java.util.regex.Pattern;

/** Exact invoice identity in observed row text, including adjacent-number cases. */
public final class InvoiceEvidence {
    private InvoiceEvidence() {}

    public static boolean containsInvoice(String observedText, String invoiceNumber) {
        if (observedText == null || invoiceNumber == null || invoiceNumber.isBlank()) return false;
        return Pattern.compile("(?<![A-Za-z0-9-])" + Pattern.quote(invoiceNumber) + "(?![A-Za-z0-9-])")
                .matcher(observedText).find();
    }
}
