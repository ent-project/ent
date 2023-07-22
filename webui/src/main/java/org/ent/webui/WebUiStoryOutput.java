package org.ent.webui;

import org.slf4j.MDC;

public class WebUiStoryOutput {
    public static final String MDC_KEY_STORY = "story";

    public static void addStory(String id, Runnable run) {
        WebUI.addStoryHook(id, run);
    }

    public static void startStory(String storyId) {
        MDC.put(MDC_KEY_STORY, storyId);
    }

    public static void endStory() {
        MDC.remove(MDC_KEY_STORY);
    }
}
