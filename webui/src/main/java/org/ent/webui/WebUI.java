package org.ent.webui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
    public static final String MDC_KEY_STORY = "story";
    public static final String QUERY_KEY_STORY = "story";

    static {
        LogUtil.setUpLogback();
    }

    private static final Logger log = LoggerFactory.getLogger(WebUI.class);

    private static final Multimap<String, WsConnectContext> sessionsByStory = ArrayListMultimap.create();
    private static final Map<String, Queue<Object>> buffersByStory = new HashMap<>();
    private static final Map<String, Runnable> storyHooks = new HashMap<>();

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
        // fixme try with resource

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
                    Runnable runnable = storyHooks.get(storyId);
                    if (runnable != null) {
                        String storyIdFinal = storyId;
                        EXECUTOR_SERVICE.submit(() -> {
                            MDC.put(MDC_KEY_STORY, storyIdFinal);
                            runnable.run();
                            MDC.remove(MDC_KEY_STORY);
                        });
                    } else {
                        MDC.put(MDC_KEY_STORY, storyId);
                        log.warn("No content found.");
                        MDC.remove(MDC_KEY_STORY);
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

    public static void broadcast(String storyId, LogEntryType type, String input) {
        Object payload = switch (type) {
            case PLAIN -> buildPayloadForLogEntry(input);
            case HTML -> buildPayloadForHtmlEntry(input);
            case DOT -> buildPayloadForDotEntry(input);
        };
        storyId = Optional.ofNullable(storyId).orElse(STORY_ID_MAIN);
        Queue<Object> buffer = getBuffer(storyId);
        buffer.add(payload);
        sessionsByStory.get(storyId).stream().filter(ctx -> ctx.session.isOpen())
                .forEach(session -> session.send(payload));
    }

    public static Object buildPayloadForLogEntry(String message) {
        return Map.of(
                "type", "log",
                "message", message
        );
    }

    private static Object buildPayloadForHtmlEntry(String html) {
        return Map.of(
                "type", "html",
                "html", html
        );
    }

    public static Object buildPayloadForDotEntry(String dotString) {
        return Map.of(
                "type", "dot",
                "scale", 0.7,
                "dot", dotString
        );
    }

    public static void addStoryHook(String storyId, Runnable runnable) {
        storyHooks.put(storyId, runnable);
    }
}