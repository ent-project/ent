package org.ent.webui;

public class WebUiStoryOutput {
    public static void addStory(String id, Runnable run) {
        WebUI.addStoryHook(id, run);
    }
}
