package com.endava.ai.ui.core.engine.selenium.element.scripts;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

public final class SeleniumScriptExecutor {
    private final JavascriptExecutor javascript;

    public SeleniumScriptExecutor(WebDriver driver) {
        this.javascript = (JavascriptExecutor) driver;
    }

    public Object execute(String script, Object... arguments) {
        return javascript.executeScript(script, arguments);
    }

    public void tryExecute(String script, Object... arguments) {
        try {
            execute(script, arguments);
        } catch (WebDriverException ignored) {
        }
    }
}
