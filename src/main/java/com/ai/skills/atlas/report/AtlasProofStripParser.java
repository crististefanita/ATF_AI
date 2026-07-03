package com.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasProofStripParser {
    private static final Pattern SUMMARY = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern LOCAL_PROOF_STRIP = Pattern.compile(
            "(?is)<details\\b(?<attributes>[^>]*data-atlas-role\\s*=\\s*['\"]local-proof-strip['\"][^>]*)>(?<body>.*?)</details>");
    private static final Pattern METHOD_TOKEN = Pattern.compile("\\b([a-z][A-Za-z0-9_]*)\\(\\)");
    private static final Pattern CODE_TOKEN = Pattern.compile("(?is)<code\\b[^>]*>(.*?)</code>");
    private static final Pattern CLASS_TOKEN = Pattern.compile("^[A-Z][A-Za-z0-9_]+$");

    List<AtlasReportModel.ProofStrip> parse(AtlasHtmlDocument document) {
        List<AtlasReportModel.ProofStrip> strips = new ArrayList<>();
        Matcher matcher = LOCAL_PROOF_STRIP.matcher(document.html());
        while (matcher.find()) {
            String body = matcher.group("body");
            strips.add(new AtlasReportModel.ProofStrip(
                    AtlasHtmlDocument.firstPlain(SUMMARY, body),
                    body,
                    tokens(METHOD_TOKEN, body),
                    classTokens(body)));
        }
        return Collections.unmodifiableList(strips);
    }

    private static Set<String> tokens(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        Set<String> tokens = new LinkedHashSet<>();
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return Collections.unmodifiableSet(tokens);
    }

    private static Set<String> classTokens(String html) {
        Matcher matcher = CODE_TOKEN.matcher(html);
        Set<String> tokens = new LinkedHashSet<>();
        while (matcher.find()) {
            String token = AtlasHtmlDocument.plain(matcher.group(1));
            if (CLASS_TOKEN.matcher(token).matches()) {
                tokens.add(token);
            }
        }
        return Collections.unmodifiableSet(tokens);
    }
}
