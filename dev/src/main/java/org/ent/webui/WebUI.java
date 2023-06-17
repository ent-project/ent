package org.ent.webui;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import org.ent.dev.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static j2html.TagCreator.article;
import static j2html.TagCreator.p;

public class WebUI {

    static {
        LogUtil.setUpLogback();
    }

    private static final Logger log = LoggerFactory.getLogger(WebUI.class);

    public static Set<WsContext> sessions = new HashSet<>();

    static Queue<String> buffer = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config ->
                config.staticFiles.add("/public", Location.CLASSPATH)).start(7070);
        // fixme try with resource

        app.ws("/logs", ws -> {
            ws.onConnect(ctx -> {
                System.err.println("connect");
                sessions.add(ctx);
                ctx.session.setIdleTimeout(Duration.ofMinutes(15));
                for (String message : buffer) {
                    sendToSession(ctx, message);
                }
            });
            ws.onClose(ctx -> {
                System.err.println("close");
                sessions.remove(ctx);
            });
        });

        int i = 1;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("Msg {}", i);
            i++;
        }
    }

    public static void broadcastLogMessage(String message) {
        System.err.println("broadcast");
        buffer.add(message);
        sessions.stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            sendToSession(session, message);
        });
    }

    private static void sendToSession(WsContext session, String message) {
        session.send(
                Map.of(
                        "type", "log",
                        "message", createHtmlLogEntry(message)
                )
        );
    }

    private static String createHtmlLogEntry(String message) {
        return article(p(message)).render();
    }

}