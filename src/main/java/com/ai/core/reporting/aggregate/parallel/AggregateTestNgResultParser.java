package com.ai.core.reporting.aggregate.parallel;

import com.ai.core.reporting.aggregate.AggregateStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AggregateTestNgResultParser {
    private AggregateTestNgResultParser() {
    }

    public static List<AggregateCollectionTestCase> parse(Path testNgResults, AggregateCollectionSpec collection)
            throws IOException {
        Document document = document(testNgResults);
        NodeList classes = document.getElementsByTagName("class");
        List<AggregateCollectionTestCase> cases = new ArrayList<>();
        long syntheticClock = System.currentTimeMillis();

        for (int classIndex = 0; classIndex < classes.getLength(); classIndex++) {
            Element classElement = (Element) classes.item(classIndex);
            String className = classElement.getAttribute("name");
            NodeList children = classElement.getChildNodes();
            for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                Node child = children.item(childIndex);
                if (!(child instanceof Element)) {
                    continue;
                }
                Element method = (Element) child;
                if (!"test-method".equals(method.getTagName()) || isConfiguration(method)) {
                    continue;
                }
                long duration = longAttribute(method, "duration-ms");
                cases.add(new AggregateCollectionTestCase(
                        collection,
                        className,
                        method.getAttribute("name"),
                        status(method.getAttribute("status")),
                        syntheticClock,
                        syntheticClock + duration,
                        failureText(method)
                ));
                syntheticClock += Math.max(1L, duration);
            }
        }
        return cases;
    }

    private static boolean isConfiguration(Element method) {
        return "true".equalsIgnoreCase(method.getAttribute("is-config"));
    }

    private static AggregateStatus status(String raw) {
        String normalized = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if ("PASS".equals(normalized)) {
            return AggregateStatus.PASSED;
        }
        if ("SKIP".equals(normalized)) {
            return AggregateStatus.SKIPPED;
        }
        return AggregateStatus.FAILED;
    }

    private static long longAttribute(Element element, String attribute) {
        String value = element.getAttribute(attribute);
        if (value == null || value.isBlank()) {
            return 1L;
        }
        return Math.max(1L, Long.parseLong(value));
    }

    private static String failureText(Element method) {
        NodeList exceptions = method.getElementsByTagName("exception");
        if (exceptions.getLength() == 0) {
            return "";
        }
        Element exception = (Element) exceptions.item(0);
        String message = text(exception, "message");
        String stack = text(exception, "full-stacktrace");
        return (message + System.lineSeparator() + stack).trim();
    }

    private static String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    private static Document document(Path testNgResults) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder().parse(testNgResults.toFile());
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Unable to parse TestNG results: " + testNgResults, e);
        }
    }
}
