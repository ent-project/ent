package org.ent.dev;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.ent.Main;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class LogUtil {

    private static final String LOGBACK_CONFIG_FILE = "logback-webui.xml";

    private LogUtil() {
    }

    public static void setUpLogback() {
        // 1. prevent initial autoconfiguration with wrong default config
        // suppresses warnings, but may not work under all circumstances
        setUpLogbackBySystemProperty();
        // 2. reliable configuration for all run setups
        setUpLogbackProgrammatically();
    }

    public static void setUpLogbackBySystemProperty() {
        System.setProperty("logback.configurationFile", LOGBACK_CONFIG_FILE);
    }

    public static void setUpLogbackProgrammatically() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            URL resourceURL = Main.class.getClassLoader().getResource(LOGBACK_CONFIG_FILE);
            configurator.doConfigure(resourceURL);
        } catch (JoranException je) {
            // print errors and warnings with StatusPrinter
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

}
