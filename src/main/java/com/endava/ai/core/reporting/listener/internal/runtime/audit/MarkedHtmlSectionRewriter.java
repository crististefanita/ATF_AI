package com.endava.ai.core.reporting.listener.internal.runtime.audit;

import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;

import java.util.List;
import java.util.Objects;

public final class MarkedHtmlSectionRewriter {

    private static final List<String> CATEGORY_SIBLINGS = List.of(
            ExtentHtmlStructure.DASHBOARD_CONTAINER,
            ExtentHtmlStructure.TEST_CONTAINER,
            "<script"
    );

    private MarkedHtmlSectionRewriter() {
    }

    public static String removeMarkedBlock(String html, String startMarker, String endMarker) {
        if (html == null || startMarker == null || endMarker == null
                || startMarker.isBlank() || endMarker.isBlank()) {
            return html == null ? "" : html;
        }

        String updated = html;
        int start = updated.indexOf(startMarker);
        int end = updated.indexOf(endMarker);
        while (start >= 0 && end > start) {
            updated = updated.substring(0, start) + updated.substring(end + endMarker.length());
            start = updated.indexOf(startMarker);
            end = updated.indexOf(endMarker);
        }
        return updated;
    }

    public static String replaceCategoryViewContent(String html, String section) {
        if (html == null) {
            return "";
        }
        int categoryStart = html.indexOf(ExtentHtmlStructure.CATEGORY_CONTAINER);
        int contentStart = categoryStart < 0 ? -1 : html.indexOf('>', categoryStart);
        int contentEnd = categoryStart < 0 ? -1 : categoryContentEnd(html, categoryStart);
        if (contentStart >= 0 && contentEnd > contentStart) {
            return html.substring(0, contentStart + 1)
                    + (section == null ? "" : section)
                    + html.substring(contentEnd);
        }
        return html;
    }

    public static String replaceRange(String html, int startInclusive, int endExclusive, String replacement) {
        if (html == null) {
            return "";
        }
        if (startInclusive < 0 || endExclusive < startInclusive || endExclusive > html.length()) {
            return html;
        }
        return html.substring(0, startInclusive)
                + nullToEmpty(replacement)
                + html.substring(endExclusive);
    }

    public static String replaceBetweenMarkersAfter(
            String html,
            int afterIndex,
            List<String> startMarkers,
            List<String> endMarkers,
            String replacement
    ) {
        if (html == null) {
            return "";
        }
        int start = firstMarkerAtOrAfter(html, startMarkers, Math.max(0, afterIndex));
        int end = firstMarkerAtOrAfter(html, endMarkers, start + 1);
        return replaceRange(html, start, end, replacement);
    }

    public static String replaceFromPreviousMarkerToEndMarkerAfterAnchor(
            String html,
            String anchor,
            List<String> startMarkers,
            List<String> endMarkers,
            String replacement
    ) {
        if (html == null) {
            return "";
        }
        String safeAnchor = Objects.requireNonNullElse(anchor, "");
        if (safeAnchor.isBlank()) {
            return html;
        }
        int anchorIndex = html.indexOf(safeAnchor);
        if (anchorIndex < 0) {
            return html;
        }
        int start = lastMarkerBefore(html, startMarkers, anchorIndex);
        MarkerMatch end = firstMarkerMatchAtOrAfter(html, endMarkers, anchorIndex + 1);
        if (start < 0 || !end.found()) {
            return html;
        }
        return replaceRange(html, start, end.endExclusive(), replacement);
    }

    public static String replaceScriptContaining(String html, String token, String replacement) {
        if (html == null) {
            return "";
        }
        String safeToken = Objects.requireNonNullElse(token, "");
        if (safeToken.isBlank()) {
            return html;
        }
        int tokenIndex = html.indexOf(safeToken);
        if (tokenIndex < 0) {
            return html;
        }
        int scriptStart = html.lastIndexOf("<script", tokenIndex);
        MarkerMatch scriptEnd = firstMarkerMatchAtOrAfter(html, List.of("</script>"), tokenIndex + 1);
        if (scriptStart < 0 || !scriptEnd.found()) {
            return html;
        }
        return replaceRange(html, scriptStart, scriptEnd.endExclusive(), replacement);
    }

    private static int categoryContentEnd(String html, int categoryStart) {
        int nextViewStart = nextCategorySiblingStart(html, categoryStart);
        if (nextViewStart < 0) {
            return -1;
        }
        return html.lastIndexOf("</div>", nextViewStart);
    }

    private static int nextCategorySiblingStart(String html, int categoryStart) {
        return CATEGORY_SIBLINGS.stream()
                .mapToInt(marker -> html.indexOf(marker, categoryStart + 1))
                .filter(index -> index > categoryStart)
                .min()
                .orElse(-1);
    }

    private static int firstMarkerAtOrAfter(String html, List<String> markers, int fromIndex) {
        MarkerMatch match = firstMarkerMatchAtOrAfter(html, markers, fromIndex);
        return match.index;
    }

    private static MarkerMatch firstMarkerMatchAtOrAfter(String html, List<String> markers, int fromIndex) {
        if (markers == null || markers.isEmpty()) {
            return MarkerMatch.missing();
        }
        return markers.stream()
                .filter(marker -> marker != null && !marker.isBlank())
                .map(marker -> MarkerMatch.of(html.indexOf(marker, Math.max(0, fromIndex)), marker))
                .filter(MarkerMatch::found)
                .min((left, right) -> Integer.compare(left.index, right.index))
                .orElse(MarkerMatch.missing());
    }

    private static int lastMarkerBefore(String html, List<String> markers, int beforeIndex) {
        if (markers == null || markers.isEmpty()) {
            return -1;
        }
        return markers.stream()
                .filter(marker -> marker != null && !marker.isBlank())
                .mapToInt(marker -> html.lastIndexOf(marker, Math.max(0, beforeIndex)))
                .filter(index -> index >= 0)
                .max()
                .orElse(-1);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static final class MarkerMatch {
        private final int index;
        private final String marker;

        private MarkerMatch(int index, String marker) {
            this.index = index;
            this.marker = marker == null ? "" : marker;
        }

        private static MarkerMatch of(int index, String marker) {
            return new MarkerMatch(index, marker);
        }

        private static MarkerMatch missing() {
            return new MarkerMatch(-1, "");
        }

        private boolean found() {
            return index >= 0;
        }

        private int endExclusive() {
            return index + marker.length();
        }
    }
}
