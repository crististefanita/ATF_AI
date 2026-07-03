package com.endava.ai.ui.core.engine;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface UIEngine {
    int DEFAULT_WINDOW_WIDTH = 2560;
    int DEFAULT_WINDOW_HEIGHT = 1440;

    boolean supportsAutoWait();

    default Capabilities capabilities() {
        return Capabilities.of(
                getClass().getSimpleName(),
                Capability.SESSION_CLEAR,
                Capability.SCREENSHOT_CAPTURE,
                Capability.WINDOW_RESIZE
        );
    }

    void open(String url);

    void click(String cssSelector);

    default void clickVisible(String cssSelector, int oneBasedIndex) {
        throw UiUnsupportedAction.VISIBLE_INDEX_CLICK.exception();
    }

    void type(String cssSelector, String text);

    default void pressKey(String cssSelector, String key) {
        throw UiUnsupportedAction.KEY_PRESS.exception();
    }

    void select(String cssSelector, String valueOrText);

    default void uploadFile(String cssSelector, String absolutePath) {
        throw UiUnsupportedAction.FILE_UPLOAD.exception();
    }

    String getText(String cssSelector);

    default List<String> getTexts(String cssSelector) {
        return List.of(getText(cssSelector));
    }

    default List<String> getVisibleTexts(String cssSelector) {
        return getTexts(cssSelector);
    }

    String getValue(String cssSelector);

    default String getAttribute(String cssSelector, String attributeName) {
        throw UiUnsupportedAction.ATTRIBUTE_LOOKUP.exception();
    }

    default UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
        throw UiUnsupportedAction.NETWORK_RESPONSE_WAIT.exception();
    }

    boolean isVisible(String cssSelector);

    default boolean isEnabled(String cssSelector) {
        return isVisible(cssSelector);
    }

    void waitForVisible(String cssSelector, int seconds);

    void waitForUrlContains(String fragment, int seconds);

    String getCurrentUrl();

    void clearSession();

    String captureScreenshotAsBase64();

    void quit();

    void setWindowSize(int width, int height);

    enum Capability {
        AUTO_WAIT,
        VISIBLE_INDEX_CLICK,
        KEY_PRESS,
        FILE_UPLOAD,
        ATTRIBUTE_LOOKUP,
        NETWORK_RESPONSE_WAIT,
        BROWSER_ZOOM,
        SESSION_CLEAR,
        SCREENSHOT_CAPTURE,
        WINDOW_RESIZE
    }

    final class Capabilities {
        private final String engineName;
        private final EnumSet<Capability> supported;

        private Capabilities(String engineName, EnumSet<Capability> supported) {
            this.engineName = engineName;
            this.supported = supported;
        }

        public static Capabilities of(String engineName, Capability... capabilities) {
            EnumSet<Capability> supported = EnumSet.noneOf(Capability.class);
            Collections.addAll(supported, capabilities);
            return new Capabilities(engineName, supported);
        }

        public String engineName() {
            return engineName;
        }

        public boolean supports(Capability capability) {
            return supported.contains(capability);
        }

        public Set<Capability> supported() {
            return Collections.unmodifiableSet(supported);
        }

        @Override
        public String toString() {
            return engineName + supported;
        }
    }
}
