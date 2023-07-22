package org.ent.util;

import org.ent.Ent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class Logging {
    private static final Logger dotLogger = LoggerFactory.getLogger("webui.dot");
    private static final Logger htmlLogger = LoggerFactory.getLogger("webui.html");

    public static void logDot(Ent ent) {
        dotLogger.atInfo().log(() -> new DotRenderer(ent).render());
    }

    public static void logHtml(Supplier<String> html) {
        htmlLogger.atInfo().log(html);
    }

    public static void logHtml(String html) {
        htmlLogger.info(html);
    }
}
