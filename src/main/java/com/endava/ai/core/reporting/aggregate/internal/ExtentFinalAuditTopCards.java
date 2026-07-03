package com.endava.ai.core.reporting.aggregate.internal;

import com.endava.ai.core.reporting.adapters.extent.overview.ExtentOverviewTopCards;
import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;

final class ExtentFinalAuditTopCards {
    private ExtentFinalAuditTopCards() {
    }

    static String reconcileStatus(String html, long passed, long failed) {
        return ExtentOverviewTopCards.reconcileStatus(html, passed, failed);
    }

    static String reconcileTiming(String html, AggregateTimingSummary timing) {
        AggregateTimingSummary safeTiming = timing == null ? AggregateTimingSummary.empty() : timing;
        if (!safeTiming.observed()) {
            return html;
        }
        return ExtentOverviewTopCards.reconcileTiming(
                html,
                safeTiming.startedAt(),
                safeTiming.endedAt(),
                safeTiming.wallClockDuration()
        );
    }
}
