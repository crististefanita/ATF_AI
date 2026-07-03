package com.ai.skills.business.parse;

import com.ai.skills.business.taxonomy.*;

import com.ai.skills.business.model.*;

import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class BusinessConceptDetector {
    private static final String LEXICON_RESOURCE = "skills/business/contracts/semantic-lexicon.yml";
    private static final List<LexiconEntry> LEXICON = loadLexicon();

    private BusinessConceptDetector() {
    }

    static Set<BusinessUnderstandingConcept> detect(String visibleText) {
        String text = visibleText.toLowerCase(Locale.ROOT);
        EnumSet<BusinessUnderstandingConcept> concepts = EnumSet.noneOf(BusinessUnderstandingConcept.class);
        for (LexiconEntry entry : LEXICON) {
            if (entry.matches(text)) {
                concepts.add(entry.concept());
                concepts.addAll(entry.impliedConcepts());
            }
        }
        return Set.copyOf(concepts);
    }

    private static List<LexiconEntry> loadLexicon() {
        List<LexiconEntry> entries = new ArrayList<>();
        for (Map<String, String> entry : SimpleYamlContractLoader.load(LEXICON_RESOURCE)) {
            entries.add(new LexiconEntry(
                    BusinessUnderstandingConcept.valueOf(SimpleYamlContractLoader.required(
                            entry, "concept", LEXICON_RESOURCE)),
                    concepts(entry.get("impliedConcepts")),
                    tokens(SimpleYamlContractLoader.required(entry, "tokens", LEXICON_RESOURCE))
            ));
        }
        return List.copyOf(entries);
    }

    private static Set<BusinessUnderstandingConcept> concepts(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        EnumSet<BusinessUnderstandingConcept> concepts = EnumSet.noneOf(BusinessUnderstandingConcept.class);
        for (String token : csv.split(",")) {
            String value = token.trim();
            if (!value.isEmpty()) {
                concepts.add(BusinessUnderstandingConcept.valueOf(value));
            }
        }
        return Set.copyOf(concepts);
    }

    private static List<String> tokens(String csv) {
        List<String> tokens = new ArrayList<>();
        for (String token : csv.split(",")) {
            String value = token.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty()) {
                tokens.add(value);
            }
        }
        return List.copyOf(tokens);
    }

    private static final class LexiconEntry {
        private final BusinessUnderstandingConcept concept;
        private final Set<BusinessUnderstandingConcept> impliedConcepts;
        private final List<String> tokens;

        private LexiconEntry(BusinessUnderstandingConcept concept,
                             Set<BusinessUnderstandingConcept> impliedConcepts,
                             List<String> tokens) {
            this.concept = concept;
            this.impliedConcepts = Set.copyOf(impliedConcepts);
            this.tokens = List.copyOf(tokens);
        }

        BusinessUnderstandingConcept concept() {
            return concept;
        }

        Set<BusinessUnderstandingConcept> impliedConcepts() {
            return impliedConcepts;
        }

        boolean matches(String text) {
            return tokens.stream().anyMatch(text::contains);
        }
    }
}
