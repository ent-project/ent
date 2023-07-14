package org.ent.util;

import org.ent.Ent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotLogger {
    private static final Logger logger = LoggerFactory.getLogger("webui.dot");

    public static void log(Ent ent) {
        logger.atInfo().log(() -> new DotRenderer(ent).render());
    }
}
