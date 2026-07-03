package com.endava.ai.skills.atlas.report;

import com.endava.ai.skills.atlas.AtlasContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

public final class AtlasReportModel {
    private static final Pattern DECLARED_ATLAS_NAME =
            Pattern.compile("(?i)([a-z0-9-]+_)?(?:"
                    + Pattern.quote(AtlasContext.BUSINESS_ATLAS_FILE)
                    + "|"
                    + Pattern.quote(AtlasContext.FRAMEWORK_ATLAS_SUFFIX.substring(1))
                    + ")");

    private final AtlasHtmlDocument document;
    private final List<ScoreCard> scoreCards;
    private final List<AtlasSection> sections;
    private final List<BusinessGraph> businessGraphs;
    private final List<ProofStrip> proofStrips;
    private final List<SourceTraceRow> sourceTraceRows;
    private final List<AuditLink> auditLinks;

    private AtlasReportModel(String html) {
        this.document = AtlasHtmlDocument.parse(html);
        this.scoreCards = new AtlasScoreCardsParser().parse(document);
        this.sections = new AtlasSectionsParser().parse(document);
        this.businessGraphs = new AtlasBusinessGraphParser().parse(document, sections);
        this.proofStrips = new AtlasProofStripParser().parse(document);
        this.sourceTraceRows = new AtlasSourceTraceParser().parse(document);
        this.auditLinks = new AtlasAuditLinksParser().parse(document);
    }

    public static AtlasReportModel parse(String html) {
        return new AtlasReportModel(html);
    }

    public static boolean isDeclaredAtlasArtifactName(String fileName) {
        return fileName != null && DECLARED_ATLAS_NAME.matcher(fileName).matches();
    }

    public List<ScoreCard> scoreCards() {
        return scoreCards;
    }

    public Map<String, Integer> scoreCardScoresByLabel() {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (ScoreCard card : scoreCards) {
            scores.put(card.normalizedLabel(), card.score());
        }
        return scores;
    }

    public boolean hasScoreWallOrCards() {
        return document.lowerHtml().contains("score-wall") || !scoreCards.isEmpty();
    }

    public boolean scoreCardsOpenBeforeExecutiveContext() {
        int scoreIndex = AtlasHtmlDocument.firstIndexOf(document.lowerHtml(), "score-wall", "score-card");
        int executiveIndex = document.lowerHtml().indexOf("executive context");
        return scoreIndex < 0 || executiveIndex < 0 || scoreIndex < executiveIndex;
    }

    public boolean overallScoreCardIsLast() {
        return !scoreCards.isEmpty() && "overall".equals(scoreCards.get(scoreCards.size() - 1).normalizedLabel());
    }

    public List<AtlasSection> sections() {
        return sections;
    }

    public List<String> sectionAttributes() {
        List<String> values = new ArrayList<>();
        for (AtlasSection section : sections) {
            values.add(section.normalizedAttribute());
        }
        return Collections.unmodifiableList(values);
    }

    public String normalizedSummaryText() {
        return document.normalizedSummaryText();
    }

    public List<BusinessGraph> businessGraphs() {
        return businessGraphs;
    }

    public String businessGraphText() {
        return document.sectionText("business flow graph", "business evidence tables");
    }

    public List<ProofStrip> proofStrips() {
        return proofStrips;
    }

    public List<SourceTraceRow> sourceTraceRows() {
        return sourceTraceRows;
    }

    public List<AuditLink> auditLinks() {
        return auditLinks;
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ScoreCard {
        private final String label;
        private final int score;
        private final String description;

        public String normalizedLabel() {
            return AtlasHtmlDocument.normalize(label);
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class AtlasSection {
        private final String attribute;
        private final String summary;
        private final int index;

        public String normalizedAttribute() {
            return AtlasHtmlDocument.normalize(attribute);
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class BusinessGraph {
        private final String key;
        private final String summary;
        private final int index;
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class ProofStrip {
        private final String title;
        private final String html;
        private final Set<String> methods;
        private final Set<String> classes;
    }

    @Getter
    @Accessors(fluent = true)
    public static final class SourceTraceRow {
        private final List<String> cells;

        SourceTraceRow(List<String> cells) {
            this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        }

        public String joinedText() {
            return String.join(" ", cells);
        }
    }

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public static final class AuditLink {
        private final String href;
        private final String label;
    }
}
