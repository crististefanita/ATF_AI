# Lesson Learned

Refund request payload is anchored to a purchased invoice, charged amount, return category and registered customer.

Do not treat this as a generic contact-form smoke; its value comes from invoice-backed refund-support identity and the run-specific proof chain.

UI wrapper handoff: actor = customer; user intent = customer actor requests refund support for a purchased invoice; intent is invoice-backed refund ownership; visible proof = refund support flow evidence; business object = support message / invoice record.
