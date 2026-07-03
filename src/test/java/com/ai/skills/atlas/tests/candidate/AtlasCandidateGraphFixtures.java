package com.ai.skills.atlas.tests.candidate;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.io.IOException;
import java.nio.file.Path;

final class AtlasCandidateGraphFixtures {
    private AtlasCandidateGraphFixtures() {
    }
    static String genericSubgraph(String title) {
        return "<details class=\"subdetails\"><summary>" + title + "</summary>\n"
                + "<svg class=\"diag\">"
                + "<text class=\"nt\">Actor customer</text>"
                + "<text class=\"nt\">Intent</text>"
                + "<text class=\"nt\">Business object</text>"
                + "<text class=\"nt\">Observable result</text>"
                + "</svg>\n"
                + "</details>\n";
    }

    static String nodeTitle(String title) {
        return "<text class=\"nt\">" + title + "</text>\n";
    }
    static String nodeGroup(String title, String attributes) {
        return "<g " + attributes + "><rect class=\"node\"></rect><text class=\"nt\">" + title + "</text></g>\n";
    }
    static String nodeGroupWithSubtitle(String title, String attributes, String subtitle) {
        return "<g " + attributes + "><rect class=\"node\"></rect><text class=\"nt\">" + title
                + "</text><text class=\"ns\">" + subtitle + "</text></g>\n";
    }
    static String proofRow(String node, String maturity, String limit) {
        return "      <tr><td>" + node + "</td><td>accountOverviewIsVisibleAfterLogin()</td>"
                + "<td>test -> action -> page -> validation -> business object</td>"
                + "<td>" + maturity + "</td><td>Source-backed business evidence.</td><td>" + limit + "</td></tr>\n";
    }
    static String supportingStraightPathGraph() {
        return supportingGraph("path");
    }
    static String supportingGraphWithoutArrowheadsAndEdgeLabels() {
        return primaryGraph()
                + "<details><summary>3. Business Flow Understanding</summary>\n"
                + "  <details><summary>3.1 Account corridor</summary>\n"
                + "    <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1120 170\" aria-label=\"Account corridor flow\">\n"
                + "      <path class=\"edge\" d=\"M190 70 H305\"></path>\n"
                + "      <path class=\"edge\" d=\"M485 70 H600\"></path>\n"
                + "      <path class=\"edge\" d=\"M760 70 C830 92 830 122 760 145\"></path>\n"
                + nodeGroupWithSubtitle("Customer account", "transform=\"translate(30 35)\"", "authenticated user")
                + nodeGroupWithSubtitle("Registered identity", "transform=\"translate(305 35)\"", "stored customer facts")
                + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(600 35)\"", "phone and address")
                + nodeGroupWithSubtitle("Profile memory", "transform=\"translate(670 125)\"", "registered fields kept")
                + "    </svg></div>\n"
                + "  </details>\n"
                + "</details>\n"
                + "</body></html>\n";
    }
    static String invisibleSemanticsGraph() {
        return htmlWithMainGraph(
                "    <line class=\"edge\" x1=\"10\" y1=\"20\" x2=\"150\" y2=\"20\"></line>\n"
                        + nodeGroup("Customer account", "transform=\"translate(40 55)\" data-maturity=\"diagnostic\"")
                        + nodeGroup("Profile identity", "transform=\"translate(395 55)\" data-maturity=\"owned\"")
                        + nodeGroup("Contact address", "transform=\"translate(790 55)\" data-maturity=\"owned\"")
                        + nodeGroup("Messages area", "transform=\"translate(790 255)\" data-maturity=\"surface-only\"")
                        + nodeGroup("Invoice record", "transform=\"translate(395 255)\" data-maturity=\"partial\""));
    }
    static String terseLabelsApproximateMaturityGraph() {
        return htmlWithMainGraph(
                "    <text class=\"el\">opens</text><text class=\"el\">retains</text><text class=\"el\">exposes</text><text class=\"el\">bounds</text><text class=\"el\">reopens</text>\n"
                        + nodeGroupWithSubtitle("Customer account", "transform=\"translate(40 55)\" data-maturity=\"diagnostic\"", "diagnostic maturity")
                        + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(395 55)\" data-maturity=\"owned\"", "owned maturity")
                        + nodeGroupWithSubtitle("Contact address", "transform=\"translate(790 55)\" data-maturity=\"owned\"", "owned maturity")
                        + nodeGroupWithSubtitle("Messages area", "transform=\"translate(790 255)\" data-maturity=\"surface-only\"", "support surface reachable")
                        + nodeGroupWithSubtitle("Invoice record", "transform=\"translate(395 255)\" data-maturity=\"partial\"", "partial boundary"));
    }
    static String missingMaturityGraph() {
        return "<html><body>\n"
                + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                + "  <summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\">\n"
                + nodeTitle("Customer account")
                + nodeTitle("Profile identity")
                + nodeTitle("Contact address")
                + nodeTitle("Messages area")
                + nodeTitle("Invoice record")
                + "  </svg></div>\n"
                + legendAndProofRows("      <tr><td>Invoice record</td><td>accountInvoicesAreaIsReachable()</td>"
                        + "<td>test -> action -> page -> validation -> business object</td><td></td>"
                        + "<td>Invoice area reachable.</td><td>Limit: no accounting reconciliation.</td></tr>\n")
                + "</details>\n"
                + "<details data-atlas-section=\"3. business flow understanding\"><summary>3. Business Flow Understanding</summary></details>\n"
                + "</body></html>\n";
    }
    static String unmarkedMaturityGraph() {
        return "<html><body>\n"
                + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                + "  <summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\">\n"
                + nodeGroup("Customer account", "")
                + nodeGroup("Profile identity", "")
                + nodeGroup("Contact address", "")
                + nodeGroup("Messages area", "")
                + nodeGroup("Invoice record", "")
                + "  </svg></div>\n"
                + legendAndProofRows(proofRow("Invoice record", "partial boundary", "Limit: no accounting reconciliation."))
                + "</details>\n"
                + "<details data-atlas-section=\"3. business flow understanding\"><summary>3. Business Flow Understanding</summary></details>\n"
                + "</body></html>\n";
    }
    static String misorderedProofGraph() {
        return "<html><body>\n"
                + "<details open><summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\"></svg></div>\n"
                + "  <details class=\"subdetails\"><summary>Banda de dovada noduri</summary><table></table></details>\n"
                + "  <div class=\"legend\"><strong>Legend</strong></div>\n"
                + "</details>\n"
                + "<details><summary>3. Business Flow Understanding</summary></details>\n"
                + "<details><summary>4. Business Evidence Tables</summary></details>\n"
                + "</body></html>\n";
    }
    static String templateCloneGraph() {
        return "<html><body>\n"
                + "<details><summary>3. Business Flow Understanding</summary>\n"
                + genericSubgraph("3.1 Clone A")
                + genericSubgraph("3.2 Clone B")
                + genericSubgraph("3.3 Clone C")
                + genericSubgraph("3.4 Clone D")
                + "</details>\n"
                + "<details><summary>4. Business Evidence Tables</summary></details>\n"
                + "</body></html>\n";
    }
    static String metaVocabularyGraph() {
        return "<html><body>\n"
                + "<details open><summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\"></svg></div>\n"
                + "  <details class=\"subdetails\"><summary>Legend</summary>\n"
                + "    <ul><li>Shows package overlay: ATF6 UI Agentic tests currently cover profile memory.</li></ul>\n"
                + "  </details>\n"
                + "</details>\n"
                + "<details><summary>3. Business Flow Understanding</summary></details>\n"
                + "<details><summary>4. Business Evidence Tables</summary></details>\n"
                + "</body></html>\n";
    }
    static AtlasValidationResult validateFixture(Path atlas, AtlasValidationRule rule) throws IOException {
        String previous = System.getProperty("atlas.html");
        try {
            System.setProperty("atlas.html", atlas.toString());
            AtlasValidationResult result = new AtlasValidationResult();
            rule.validate(AtlasContext.fromWorkingDirectory(), result);
            return result;
        } finally {
            restore("atlas.html", previous);
        }
    }
    static void restore(String property, String previous) {
        if (previous == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, previous);
        }
    }
    private static String supportingGraph(String connectorTag) {
        String first = "path".equals(connectorTag)
                ? "      <path class=\"edge\" d=\"M170 90 H305\"></path>\n"
                : "      <line class=\"edge\" x1=\"170\" y1=\"90\" x2=\"305\" y2=\"90\"></line>\n";
        String second = "path".equals(connectorTag)
                ? "      <path class=\"edge\" d=\"M465 90 H600\"></path>\n"
                : "      <line class=\"edge\" x1=\"465\" y1=\"90\" x2=\"600\" y2=\"90\"></line>\n";
        String third = "path".equals(connectorTag)
                ? "      <path class=\"edge\" d=\"M760 90 V155\"></path>\n"
                : "      <line class=\"edge\" x1=\"760\" y1=\"90\" x2=\"760\" y2=\"155\"></line>\n";
        return primaryGraph()
                + "<details><summary>3. Business Flow Understanding</summary>\n"
                + "  <details><summary>3.1 Account corridor</summary>\n"
                + "    <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1120 230\" aria-label=\"Account corridor flow\">\n"
                + first + second + third
                + "      <text class=\"el\">opens account</text><text class=\"el\">retains profile</text><text class=\"el\">returns state</text>\n"
                + nodeGroupWithSubtitle("Actor customer", "transform=\"translate(40 50)\"", "registered user")
                + nodeGroupWithSubtitle("Intent account", "transform=\"translate(305 50)\"", "account intent")
                + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(600 50)\"", "business object")
                + nodeGroupWithSubtitle("Account workspace", "transform=\"translate(600 155)\"", "observable state")
                + "    </svg></div>\n"
                + "  </details>\n"
                + "</details>\n"
                + "</body></html>\n";
    }
    private static String primaryGraph() {
        return "<html><body>\n"
                + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                + "  <summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1180 560\" aria-label=\"Business Flow Graph\">\n"
                + "    <path class=\"edge\" d=\"M230 100 C295 82 330 82 395 100\"></path><text class=\"el\">opens account</text>\n"
                + "    <path class=\"edge\" d=\"M605 100 C680 78 725 78 790 100\"></path><text class=\"el\">retains profile</text>\n"
                + "    <path class=\"edge dashed\" d=\"M895 145 C960 190 970 230 900 255\"></path><text class=\"el\">exposes contact</text>\n"
                + "    <path class=\"edge\" d=\"M790 300 C710 338 650 338 605 300\"></path><text class=\"el\">bounds invoice</text>\n"
                + "    <path class=\"edge\" d=\"M500 345 C430 410 260 455 230 475\"></path><text class=\"el\">returns account state</text>\n"
                + nodeGroupWithSubtitle("Customer account", "transform=\"translate(40 55)\" data-maturity=\"diagnostic\"", "diagnostic maturity")
                + nodeGroupWithSubtitle("Profile identity", "transform=\"translate(395 55)\" data-maturity=\"owned\" data-primary=\"true\"", "primary owned object")
                + nodeGroupWithSubtitle("Contact address", "transform=\"translate(790 55)\" data-maturity=\"owned\"", "owned maturity")
                + nodeGroupWithSubtitle("Messages area", "class=\"frontier\" transform=\"translate(790 255)\" data-maturity=\"frontier\"", "frontier maturity")
                + nodeGroupWithSubtitle("Invoice record", "transform=\"translate(395 255)\" data-maturity=\"partial\"", "partial boundary")
                + nodeGroupWithSubtitle("Account state", "transform=\"translate(40 430)\" data-maturity=\"diagnostic\"", "diagnostic maturity")
                + "  </svg></div>\n"
                + "</details>\n";
    }
    private static String htmlWithMainGraph(String svgBody) {
        return "<html><body>\n"
                + "<details id=\"business-flow-graph\" data-atlas-section=\"2. business flow graph\" open>\n"
                + "  <summary>2. Business Flow Graph</summary>\n"
                + "  <div class=\"graph-panel\"><svg class=\"diag\" viewBox=\"0 0 1180 430\" aria-label=\"Business Flow Graph\">\n"
                + svgBody
                + "  </svg></div>\n"
                + "</details>\n"
                + "</body></html>\n";
    }
    private static String legendAndProofRows(String extraRow) {
        return "  <details class=\"subdetails\"><summary>Legend</summary><ul><li>Application map.</li></ul></details>\n"
                + "  <details class=\"subdetails\" data-atlas-role=\"business-proof-strip\"><summary>Banda de dovada noduri</summary>\n"
                + "    <table><tr><th>Node/flow</th><th>Test method</th><th>Layer path</th><th>Maturity</th><th>Business evidence</th><th>Limit</th></tr>\n"
                + proofRow("Customer account", "diagnostic surface", "Limit: UI-visible account state only.")
                + proofRow("Profile identity", "owned UI memory", "Limit: no backend identity audit.")
                + proofRow("Contact address", "owned UI memory", "Limit: no postal service proof.")
                + proofRow("Messages area", "surface-only", "Limit: no message lifecycle claim.")
                + extraRow
                + "    </table>\n"
                + "  </details>\n";
    }
}
