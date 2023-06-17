package org.ent.webui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.nio.charset.StandardCharsets;

public class WebsocketAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    protected Encoder<ILoggingEvent> encoder;

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        byte[] byteArray = encoder.encode(event);

        System.err.println("hi there!");
        WebUI.broadcastLogMessage(new String(byteArray, StandardCharsets.UTF_8));
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
