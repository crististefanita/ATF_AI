package com.ai.ui.core.engine.selenium.element.actions;

import com.ai.ui.core.engine.syntax.UiOptionText;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

enum SeleniumSelectStrategy {
    VALUE {
        @Override
        void selectOption(Select select, String valueOrText) {
            select.selectByValue(valueOrText);
        }
    },
    VISIBLE_TEXT {
        @Override
        void selectOption(Select select, String valueOrText) {
            select.selectByVisibleText(valueOrText);
        }
    },
    CASE_INSENSITIVE_TEXT {
        @Override
        void selectOption(Select select, String valueOrText) {
            for (WebElement option : select.getOptions()) {
                if (valueOrText.equalsIgnoreCase(option.getText().trim())) {
                    option.click();
                    return;
                }
            }
            throw new NoSuchElementException("No option text found for: " + valueOrText);
        }
    },
    NORMALIZED_VALUE_OR_TEXT {
        @Override
        void selectOption(Select select, String valueOrText) {
            String expected = UiOptionText.normalized(valueOrText);
            for (WebElement option : select.getOptions()) {
                if (expected.equals(UiOptionText.normalized(option.getAttribute("value")))
                        || expected.equals(UiOptionText.normalized(option.getText()))) {
                    option.click();
                    return;
                }
            }
            throw new NoSuchElementException("No normalized option found for: " + valueOrText);
        }
    };

    boolean select(Select select, String valueOrText) {
        try {
            selectOption(select, valueOrText);
            return true;
        } catch (NoSuchElementException ignored) {
            return false;
        } catch (UnsupportedOperationException ignored) {
            return false;
        }
    }

    abstract void selectOption(Select select, String valueOrText);
}
