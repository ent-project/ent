package org.ent.webui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;

import java.nio.charset.StandardCharsets;

public class WebsocketAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    protected Encoder<ILoggingEvent> encoder;

    private String channel;

    public void setChannel(String channel) {
        this.channel = channel;
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
        String storyId = event.getMDCPropertyMap().get("story");
        if ("dot".equals(channel)) {
            WebUI.broadcast(storyId, LogEntryType.DOT, message);
        } else if ("html".equals(channel)) {
            WebUI.broadcast(storyId, LogEntryType.HTML, message);
        } else {
            WebUI.broadcast(storyId, LogEntryType.PLAIN, message);
        }
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
