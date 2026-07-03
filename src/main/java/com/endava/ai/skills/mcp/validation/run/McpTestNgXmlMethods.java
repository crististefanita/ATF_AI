package com.endava.ai.skills.mcp.validation.run;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class McpTestNgXmlMethods {
    private static final ErrorHandler QUIET_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) {
            // Malformed negative fixtures should become zero countable XML methods, not console noise.
        }

        @Override
        public void error(SAXParseException exception) {
            // Malformed negative fixtures should become zero countable XML methods, not console noise.
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    private McpTestNgXmlMethods() {
    }

    public static Map<String, Set<String>> passedRealMethodsByClass(String xml) {
        Map<String, Set<String>> methodsByClass = new LinkedHashMap<>();
        Optional<Document> document = parse(xml);
        if (document.isEmpty()) {
            return methodsByClass;
        }
        NodeList classes = document.get().getElementsByTagName("class");
        for (int index = 0; index < classes.getLength(); index++) {
            Element testClass = (Element) classes.item(index);
            String className = testClass.getAttribute("name").trim();
            Set<String> methodNames = passedRealMethodNames(testClass.getElementsByTagName("test-method"));
            if (!methodNames.isEmpty()) {
                methodsByClass.put(className, methodNames);
            }
        }
        return methodsByClass;
    }

    public static Set<String> passedRealMethodNames(String xml) {
        Optional<Document> document = parse(xml);
        if (document.isEmpty()) {
            return Set.of();
        }
        return passedRealMethodNames(document.get().getElementsByTagName("test-method"));
    }

    public static int passedRealMethodCount(String xml) {
        Map<String, Set<String>> byClass = passedRealMethodsByClass(xml);
        if (!byClass.isEmpty()) {
            return byClass.values().stream().mapToInt(Set::size).sum();
        }
        return passedRealMethodNames(xml).size();
    }

    private static Set<String> passedRealMethodNames(NodeList methods) {
        Set<String> names = new LinkedHashSet<>();
        for (int index = 0; index < methods.getLength(); index++) {
            Element method = (Element) methods.item(index);
            if (isPassedRealTestMethod(method)) {
                names.add(method.getAttribute("name").trim());
            }
        }
        return names;
    }

    private static boolean isPassedRealTestMethod(Element method) {
        return "PASS".equalsIgnoreCase(method.getAttribute("status"))
                && !method.getAttribute("name").trim().isEmpty()
                && !"true".equalsIgnoreCase(method.getAttribute("is-config"));
    }

    private static Optional<Document> parse(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            DocumentBuilderFactory factory = secureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(QUIET_ERROR_HANDLER);
            return Optional.of(builder.parse(new InputSource(new StringReader(xml))));
        } catch (IOException | ParserConfigurationException | SAXException exception) {
            return Optional.empty();
        }
    }

    private static DocumentBuilderFactory secureDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeatureIfSupported(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static void setFeatureIfSupported(DocumentBuilderFactory factory,
                                              String feature,
                                              boolean enabled) throws ParserConfigurationException {
        try {
            factory.setFeature(feature, enabled);
        } catch (ParserConfigurationException exception) {
            // Keep XML parsing available on JDKs that do not expose an optional hardening feature.
        }
    }
}
