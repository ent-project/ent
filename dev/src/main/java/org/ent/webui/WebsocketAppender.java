package org.ent.webui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.nio.charset.StandardCharsets;

public class WebsocketAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    protected Encoder<ILoggingEvent> encoder;

    private boolean dot;

    public void setDot(boolean dot) {
        this.dot = dot;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        String message;
        if (encoder != null) {
            byte[] byteArray = encoder.encode(event);
            message = new String(byteArray, StandardCharsets.UTF_8);
        } else {
            message = event.getMessage();
        }
        if (dot) {
            WebUI.broadcastDot(message);
        } else {
            WebUI.broadcastLogMessage(message);
        }
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
