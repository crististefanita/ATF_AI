package com.endava.ai.ui.core.engine.selenium.element.scripts;

public final class SeleniumDomScripts {
    private static final String EVENT_DISPATCH_BLOCK =
            "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                    + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));"
                    + "arguments[0].dispatchEvent(new Event('blur', {bubbles: true}));"
                    + "arguments[0].dispatchEvent(new Event('focusout', {bubbles: true}));";

    public static final String SCROLL_INTO_VIEW =
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});";
    public static final String CLICK = "arguments[0].click();";
    public static final String FOCUS = "arguments[0].focus();";
    public static final String SET_VALUE =
            "arguments[0].value = arguments[1];"
                    + EVENT_DISPATCH_BLOCK;
    public static final String DISPATCH_INPUT_EVENTS = EVENT_DISPATCH_BLOCK;
    public static final String DISPATCH_SELECT_EVENTS = EVENT_DISPATCH_BLOCK;

    private SeleniumDomScripts() {
    }
}
