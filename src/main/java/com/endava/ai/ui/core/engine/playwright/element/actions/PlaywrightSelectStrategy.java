package com.endava.ai.ui.core.engine.playwright.element.actions;

import com.endava.ai.ui.core.engine.syntax.UiOptionText;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.SelectOption;

import java.util.Collections;
import java.util.List;

enum PlaywrightSelectStrategy {
    RAW {
        @Override
        List<String> selectedOptions(Locator locator, String valueOrText) {
            return locator.selectOption(valueOrText);
        }
    },
    LABEL {
        @Override
        List<String> selectedOptions(Locator locator, String valueOrText) {
            return locator.selectOption(new SelectOption().setLabel(valueOrText));
        }
    },
    VALUE {
        @Override
        List<String> selectedOptions(Locator locator, String valueOrText) {
            return locator.selectOption(new SelectOption().setValue(valueOrText));
        }
    },
    NORMALIZED_VALUE_OR_TEXT {
        @Override
        List<String> selectedOptions(Locator locator, String valueOrText) {
            String expected = UiOptionText.normalized(valueOrText);
            Locator options = locator.locator("option");
            for (int index = 0; index < options.count(); index++) {
                Locator option = options.nth(index);
                String value = option.getAttribute("value");
                String text = option.innerText();
                if (expected.equals(UiOptionText.normalized(value))
                        || expected.equals(UiOptionText.normalized(text))) {
                    if (value != null && !value.isBlank()) {
                        return locator.selectOption(new SelectOption().setValue(value));
                    }
                    return locator.selectOption(new SelectOption().setLabel(text));
                }
            }
            return Collections.emptyList();
        }
    };

    boolean select(Locator locator, String valueOrText) {
        try {
            return !selectedOptions(locator, valueOrText).isEmpty();
        } catch (PlaywrightException ignored) {
            return false;
        }
    }

    abstract List<String> selectedOptions(Locator locator, String valueOrText);
}
