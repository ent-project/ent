package org.ent.util;

import org.ent.Ent;
import org.ent.net.Net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotLogger {
    private static final Logger logger = LoggerFactory.getLogger("webui.dot");

    public static void log(Net net) {
        logger.atInfo().log(() -> DotRenderer.render(net));
    }

    public static void log(Ent ent) {
        log(ent.getNet());
    }
}
