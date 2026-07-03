package com.endava.ai.skills.atlas.rules.audit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

final class AtlasAuditTopLevelCards {
    private static final Pattern TEST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*class=\"[^\"]*test-item[^\"]*\"");
    private static final Pattern DIV_TOKEN_PATTERN = Pattern.compile("(?is)<div\\b([^>]*)>|</div>");

    private AtlasAuditTopLevelCards() {
    }

    static AtlasTestAuditSupport.AuditTotals count(String html) {
        int total = 0;
        int failures = 0;
        Matcher itemMatcher = TEST_ITEM_PATTERN.matcher(html);
        int itemStart = nextMatchStart(itemMatcher);
        while (itemStart >= 0) {
            int nextItemStart = nextMatchStart(itemMatcher);
            int itemEnd = nextItemStart >= 0 ? nextItemStart : html.length();
            String item = html.substring(itemStart, itemEnd);
            int bodyStart = item.indexOf("class=\"mt-4\"");
            if (bodyStart >= 0) {
                TopLevelCounts counts = countCards(item.substring(bodyStart));
                total += counts.total;
                failures += counts.failures;
            }
            itemStart = nextItemStart;
        }
        return total == 0
                ? AtlasTestAuditSupport.AuditTotals.missing()
                : AtlasTestAuditSupport.AuditTotals.present(total, failures);
    }

    private static int nextMatchStart(Matcher matcher) {
        return matcher.find() ? matcher.start() : -1;
    }

    private static TopLevelCounts countCards(String html) {
        int total = 0;
        int failures = 0;
        int cardDepth = 0;
        int topLevelStart = -1;
        boolean[] divStack = new boolean[128];
        int stackSize = 0;
        Matcher tokenMatcher = DIV_TOKEN_PATTERN.matcher(html);
        while (tokenMatcher.find()) {
            boolean openDiv = tokenMatcher.group(1) != null;
            if (openDiv) {
                boolean card = tokenMatcher.group(1).contains("class=\"card\"");
                divStack = growIfNeeded(divStack, stackSize);
                divStack[stackSize++] = card;
                if (card) {
                    if (cardDepth == 0) {
                        topLevelStart = tokenMatcher.start();
                    }
                    cardDepth++;
                }
            } else if (stackSize > 0) {
                boolean closedCard = divStack[--stackSize];
                if (closedCard) {
                    cardDepth--;
                    if (cardDepth == 0 && topLevelStart >= 0) {
                        String topLevelCard = html.substring(topLevelStart, tokenMatcher.end());
                        if (topLevelCard.contains("<a class=\"node")) {
                            total++;
                            failures += failed(topLevelCard) ? 1 : 0;
                        }
                        topLevelStart = -1;
                    }
                }
            }
        }
        return new TopLevelCounts(total, failures);
    }

    private static boolean[] growIfNeeded(boolean[] divStack, int stackSize) {
        if (stackSize < divStack.length) {
            return divStack;
        }
        boolean[] grown = new boolean[divStack.length * 2];
        System.arraycopy(divStack, 0, grown, 0, divStack.length);
        return grown;
    }

    private static boolean failed(String topLevelCard) {
        return topLevelCard.contains("fail-bg")
                || topLevelCard.contains("status=\"fail\"")
                || topLevelCard.contains("test-status text-fail");
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TopLevelCounts {
        private final int total;
        private final int failures;
    }
}
