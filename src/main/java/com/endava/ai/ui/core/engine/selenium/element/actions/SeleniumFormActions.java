package com.endava.ai.ui.core.engine.selenium.element.actions;

import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumDomScripts;
import com.endava.ai.ui.core.engine.selenium.element.scripts.SeleniumScriptExecutor;
import com.endava.ai.ui.core.engine.selenium.element.selectors.SeleniumSelectorResolver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.nio.file.Path;
import java.util.NoSuchElementException;

public final class SeleniumFormActions {
    private final SeleniumSelectorResolver selectorResolver;
    private final SeleniumClickActions clickActions;
    private final SeleniumScriptExecutor scripts;

    public SeleniumFormActions(
            SeleniumSelectorResolver selectorResolver,
            SeleniumClickActions clickActions,
            SeleniumScriptExecutor scripts
    ) {
        this.selectorResolver = selectorResolver;
        this.clickActions = clickActions;
        this.scripts = scripts;
    }

    public void select(String cssSelector, String valueOrText) {
        WebElement element = selectorResolver.first(cssSelector);
        clickActions.scrollIntoView(element);
        Select select = new Select(element);
        for (SeleniumSelectStrategy strategy : SeleniumSelectStrategy.values()) {
            if (strategy.select(select, valueOrText)) {
                dispatchSelectEvents(element);
                return;
            }
        }

        throw new NoSuchElementException("No option found for: " + valueOrText);
    }

    public void uploadFile(String cssSelector, String absolutePath) {
        selectorResolver.first(cssSelector).sendKeys(Path.of(absolutePath).toAbsolutePath().toString());
    }

    private void dispatchSelectEvents(WebElement element) {
        scripts.tryExecute(SeleniumDomScripts.DISPATCH_SELECT_EVENTS, element);
    }
}
