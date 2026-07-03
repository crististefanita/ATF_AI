package com.endava.ai.ui.support.actions;

import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.support.wait.WaitUtils;

final class UiActionWait {
    private UiActionWait() {
    }

    static void visibleIfEngineRequires(String cssSelector) {
        if (!DriverManager.getEngine().supportsAutoWait()) {
            WaitUtils.waitForVisible(cssSelector);
        }
    }
}
