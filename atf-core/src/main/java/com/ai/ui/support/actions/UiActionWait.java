package com.ai.ui.support.actions;

import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.support.wait.WaitUtils;

final class UiActionWait {
    private UiActionWait() {
    }

    static void visibleIfEngineRequires(String cssSelector) {
        if (!DriverManager.getEngine().supportsAutoWait()) {
            WaitUtils.waitForVisible(cssSelector);
        }
    }
}
