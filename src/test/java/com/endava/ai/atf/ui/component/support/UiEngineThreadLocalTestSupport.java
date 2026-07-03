package com.endava.ai.atf.ui.component.support;

import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.core.engine.UIEngine;

import java.lang.reflect.Field;

public final class UiEngineThreadLocalTestSupport {
    private static final ThreadLocal<UIEngine> ENGINES = resolveEngineThreadLocal();

    private UiEngineThreadLocalTestSupport() {
    }

    public static void setEngineForCurrentThread(UIEngine engine) {
        if (engine == null) {
            ENGINES.remove();
        } else {
            ENGINES.set(engine);
        }
    }

    private static ThreadLocal<UIEngine> resolveEngineThreadLocal() {
        try {
            Field field = DriverManager.class.getDeclaredField("ENGINE");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            ThreadLocal<UIEngine> engines = (ThreadLocal<UIEngine>) field.get(null);
            return engines;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to access DriverManager ENGINE thread local", e);
        }
    }
}
