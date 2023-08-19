package org.ent.webui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.function.Supplier;

public class WebUiStoryOutput {
    public static final String MDC_KEY_STORY = "story";

    private static final Logger htmlLogger = LoggerFactory.getLogger("webui.html");


    public static void addStory(String storyId, Runnable run) {
        WebUI.addStoryHook(storyId, run);
    }

    public static void addStoryWithAnnouncement(String storyId, Runnable run) {
        addStory(storyId, run);
        htmlLogger.info("<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
    }

    public static void startStory(String storyId) {
        MDC.put(MDC_KEY_STORY, storyId);
    }

    public static void endStory() {
        MDC.remove(MDC_KEY_STORY);
    }

    public static <T> T runStoryWithAnnouncement(String storyId, Supplier<T> run) {
        htmlLogger.info("<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
        return runStory(storyId, run);
    }

    public static <T> T runStory(String storyId, Supplier<T> run) {
        WebUI.Story story = WebUI.addStoryHook(storyId, () -> {});
        startStory(storyId);
        T result = run.get();
        story.setExecuted(true);
        endStory();
        return result;
    }
}
