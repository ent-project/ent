package org.ent.util;

import org.ent.Ent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.function.Supplier;

public class Logging {
    public static final Marker JUMP_MARKER = MarkerFactory.getMarker("jump");
    public static final Marker MAJOR_JUMP_MARKER = MarkerFactory.getMarker("jump_major");

    public static final Logger dotLogger = LoggerFactory.getLogger("webui.dot");
    public static final Logger htmlLogger = LoggerFactory.getLogger("webui.html");

    private final static Logger log = LoggerFactory.getLogger(Logging.class);

    public static void logDot(Ent ent) {
        dotLogger.atInfo().log(() -> {
            try {
                return new DotRenderer(ent).render();
            } catch (Throwable e) {
                log.error("Exception during dot rendering", e);
                throw e;
            }
        });
    }

    public static void logHtml(Supplier<String> html) {
        htmlLogger.atInfo().log(html);
    }

    public static void logHtml(String html) {
        htmlLogger.info(html);
    }
}
