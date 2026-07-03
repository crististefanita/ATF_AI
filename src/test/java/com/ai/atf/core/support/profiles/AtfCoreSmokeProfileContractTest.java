package com.ai.atf.core.support.profiles;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlProfileSpec;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_TEST_NG_PROFILE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_TEST_NG_PROFILE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class AtfCoreSmokeProfileContractTest {
    private static final String CORE_SMOKE = "atf-core-smoke";
    private static final String CORE_FAST = "atf-core-fast";
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";

    @Test
    public void coreSmokeProfileIsExecutableAndSmallerThanFast() throws Exception {
        AtfCoreYamlProfileSpec smoke = AtfCoreYamlContractCatalog.requireProfile(CORE_SMOKE);
        AtfCoreYamlProfileSpec fast = AtfCoreYamlContractCatalog.requireProfile(CORE_FAST);

        Assert.assertEquals(smoke.stability(), "smoke");
        Assert.assertFalse(smoke.finalArtifact(), "Core smoke must not become a final artifact profile.");
        Assert.assertEquals(smoke.browser(), Boolean.FALSE.toString());
        Assert.assertEquals(smoke.allowedSkipped(), 0);
        Assert.assertTrue(fast.expectedClasses().size() > smoke.expectedClasses().size(),
                "Core smoke must stay materially smaller than fast regression confidence.");
        Assert.assertTrue(Files.isRegularFile(Path.of(smoke.suiteXml())),
                "Core smoke TestNG suite must exist: " + smoke.suiteXml());
    }

    @Test
    public void coreSmokeTestNgSuiteMatchesYamlExactly() throws Exception {
        AtfCoreYamlProfileSpec smoke = AtfCoreYamlContractCatalog.requireProfile(CORE_SMOKE);
        Assert.assertEquals(classes(Path.of(smoke.suiteXml())), smoke.expectedClasses(),
                "Core smoke XML must stay tiny and YAML-driven.");
    }

    @Test
    public void coreSmokeFlowIsDocumentedBeforeCoreSanity() {
        List<String> orderedFlowIds = SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> "flow".equals(required(entry, "kind")))
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(required(entry, "order"))))
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toList());

        Assert.assertTrue(orderedFlowIds.indexOf("flow.core.smoke")
                        < orderedFlowIds.indexOf("flow.core.sanity"),
                "flow.core.smoke must stay before flow.core.sanity in skill-architecture.yml: "
                        + orderedFlowIds);
    }

    @Test
    public void coreSmokeDoesNotRouteToMcpAtlasOrPublishedPackageArtifacts() throws Exception {
        AtfCoreYamlProfileSpec smoke = AtfCoreYamlContractCatalog.requireProfile(CORE_SMOKE);
        String suiteXml = Files.readString(Path.of(smoke.suiteXml()));

        for (String forbidden : List.of("com.ai.skills.mcp.", "com.ai.skills.atlas.",
                "docs" + "/out", "docs" + "\\out",
                "docs" + "/atlas/validation", "docs" + "\\atlas\\validation")) {
            Assert.assertFalse(suiteXml.contains(forbidden), "Core smoke must not route through " + forbidden);
        }
    }

    private static List<String> classes(Path suiteXml) throws Exception {
        Assert.assertTrue(Files.isRegularFile(suiteXml), "Missing TestNG suite XML: " + suiteXml);
        Document document = parseXml(suiteXml);
        NodeList nodes = document.getElementsByTagName("class");
        List<String> values = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            values.add(nodes.item(index).getAttributes().getNamedItem("name").getNodeValue());
        }
        return values;
    }

    private static Document parseXml(Path suiteXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(suiteXml.toFile());
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, SKILL_ARCHITECTURE);
    }
}
