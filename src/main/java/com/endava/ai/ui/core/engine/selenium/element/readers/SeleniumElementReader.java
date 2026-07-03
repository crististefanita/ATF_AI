package com.endava.ai.ui.core.engine.selenium.element.readers;

import com.endava.ai.ui.core.engine.selenium.element.actions.SeleniumRetryPause;
import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;

import java.util.List;

public final class SeleniumElementReader {
    private final SeleniumTextReader textReader;
    private final SeleniumValueReader valueReader;
    private final SeleniumStateReader stateReader;

    public SeleniumElementReader(SeleniumSelectorResolver selectorResolver, SeleniumRetryPause retryPause) {
        this.textReader = new SeleniumTextReader(selectorResolver, retryPause);
        this.valueReader = new SeleniumValueReader(selectorResolver);
        this.stateReader = new SeleniumStateReader(selectorResolver);
    }

    public String getText(String cssSelector) {
        return textReader.getText(cssSelector);
    }

    public List<String> getTexts(String cssSelector) {
        return textReader.getTexts(cssSelector);
    }

    public List<String> getVisibleTexts(String cssSelector) {
        return textReader.getVisibleTexts(cssSelector);
    }

    public String getValue(String cssSelector) {
        return valueReader.getValue(cssSelector);
    }

    public String getAttribute(String cssSelector, String attributeName) {
        return valueReader.getAttribute(cssSelector, attributeName);
    }

    public boolean isVisible(String cssSelector) {
        return stateReader.isVisible(cssSelector);
    }

    public boolean isEnabled(String cssSelector) {
        return stateReader.isEnabled(cssSelector);
    }
}
