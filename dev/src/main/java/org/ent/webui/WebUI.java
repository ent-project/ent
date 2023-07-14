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

public class WebUI {

    static {
        LogUtil.setUpLogback();
    }

    private static final Logger log = LoggerFactory.getLogger(WebUI.class);

    public static Set<WsContext> sessions = new HashSet<>();

    static Queue<Object> buffer = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        setUpJavalin();
        loopForever();
    }

    public static void setUpJavalin() {
        Javalin app = Javalin.create(config ->
                config.staticFiles.add("/public", Location.CLASSPATH)).start(7070);
        // fixme try with resource

        app.ws("/logs", ws -> {
            ws.onConnect(ctx -> {
                System.err.println("connect");
                sessions.add(ctx);
                ctx.session.setIdleTimeout(Duration.ofMinutes(15));
                for (Object message : buffer) {
                    ctx.send(message);
//                    sendToSession(ctx, message);
                }
            });
            ws.onClose(ctx -> {
                System.err.println("close");
                sessions.remove(ctx);
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

    public static void broadcastLogMessage(String message) {
        Object payload = Map.of(
                "type", "log",
                "message", message
        );
        buffer.add(payload);
        sessions.stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(payload);
        });
    }

    public static void broadcastDot(String dotString) {
        Object payload = Map.of(
                "type", "dot",
                "scale", 0.7,
                "dot", dotString
        );
        buffer.add(payload);
        sessions.stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(payload);
        });
    }

}