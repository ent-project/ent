package org.ent.webui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebUI {

    public static final String STORY_ID_MAIN = "main";
    public static final String QUERY_KEY_STORY = "story";

    static {
        LogUtil.setUpLogback();
    }

    private static final Logger log = LoggerFactory.getLogger(WebUI.class);

    public static class Story {
        final Runnable runnable;
        boolean executed;

        private Story(Runnable runnable) {
            this.runnable = runnable;
        }

        public void run() {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.error("Exception in story", e);
            }
        }

        public boolean isExecuted() {
            return executed;
        }

        public void setExecuted(boolean executed) {
            this.executed = executed;
        }
    }

    private static final Multimap<String, WsConnectContext> sessionsByStory = ArrayListMultimap.create();
    private static final Map<String, Queue<Object>> buffersByStory = new HashMap<>();
    private static final Map<String, Story> storyHooks = new HashMap<>();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(3);

    private static boolean started;

    public static void main(String[] args) {
        WebUiStoryOutput.addStory("my", () -> {
            log.info("story time...");
        });
        setUpJavalin();
    }

    public static void setUpJavalin() {
        if (started) {
            return;
        }
        started = true;

        Javalin app = Javalin.create(config ->
                config.staticFiles.add("/public", Location.CLASSPATH)).start(7070);

        app.ws("/logs", ws -> {
            ws.onConnect(ctx -> {
                ctx.session.setIdleTimeout(Duration.ofMinutes(15));
                String storyId = ctx.queryParam(QUERY_KEY_STORY);
                storyId = Optional.ofNullable(storyId).orElse(STORY_ID_MAIN);

                sessionsByStory.put(storyId, ctx);
                Queue<Object> buffer = buffersByStory.get(storyId);
                if (buffer != null) {
                    for (Object message : buffer) {
                        ctx.send(message);
                    }
                }
                if (!STORY_ID_MAIN.equals(storyId)) {
                    Story story = storyHooks.get(storyId);
                    if (story != null) {
                        if (!story.isExecuted()) {
                            String storyIdFinal = storyId;
                            story.setExecuted(true);
                            EXECUTOR_SERVICE.submit(() -> {
                                WebUiStoryOutput.startStory(storyIdFinal);
                                story.run();
                                WebUiStoryOutput.endStory();
                            });
                        }
                    } else if (buffer == null) {
                        WebUiStoryOutput.startStory(storyId);
                        log.warn("No content found.");
                        WebUiStoryOutput.endStory();
                    }
                }
            });
            ws.onClose(ctx -> {
                String storyId = ctx.queryParam(QUERY_KEY_STORY);
                storyId = Optional.ofNullable(storyId).orElse(STORY_ID_MAIN);
                sessionsByStory.remove(storyId, ctx);
            });
        });
    }

    public static void loopForever() {
        int i = 1;
        while (true) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            log.info("Msg {}", i);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            broadcastDot("digraph { a -> c%s }".formatted(i));
            i++;
        }
    }

    private static Queue<Object> getBuffer(String storyId) {
        Queue<Object> buffer = buffersByStory.get(storyId);
        if (buffer == null) {
            buffer = new ConcurrentLinkedQueue<>();
            buffersByStory.put(storyId, buffer);
        }
        return buffer;
    }

    public static void broadcast(String storyId, LogEntryType type, String input,
                                 boolean jumpPoint, boolean jumpPointMajor) {
        Map<String, Object> payload = switch (type) {
            case PLAIN -> buildPayloadForLogEntry(input);
            case HTML -> buildPayloadForHtmlEntry(input);
            case DOT -> buildPayloadForDotEntry(input);
        };
        if (jumpPoint) {
            payload.put("jump", true);
        }
        if (jumpPointMajor) {
            payload.put("jump_major", true);
        }
        storyId = Optional.ofNullable(storyId).orElse(STORY_ID_MAIN);
        Queue<Object> buffer = getBuffer(storyId);
        buffer.add(payload);
        sessionsByStory.get(storyId).stream().filter(ctx -> ctx.session.isOpen())
                .forEach(session -> session.send(payload));
    }

    public static Map<String, Object> buildPayloadForLogEntry(String message) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", "log");
        result.put("message", message);
        return result;
    }

    private static Map<String, Object> buildPayloadForHtmlEntry(String html) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", "html");
        result.put("html", html);
        return result;
    }

    public static Map<String, Object> buildPayloadForDotEntry(String dotString) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", "dot");
        result.put("scale", 0.7);
        result.put("dot", dotString);
        return result;
    }

    public static Story addStoryHook(String storyId, Runnable runnable) {
        Story story = new Story(runnable);
        storyHooks.put(storyId, story);
        return story;
    }
}