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
            if ("dot".equals(channel)) {
                message = event.getMessage();
            } else {
                message = event.getFormattedMessage();
            }
        }
        String storyId = event.getMDCPropertyMap().get("story");
        boolean isJump = hasMarker(event, "jump");
        boolean isMajorJump = hasMarker(event, "jump_major");
        if ("dot".equals(channel)) {
            WebUI.broadcast(storyId, LogEntryType.DOT, message, isJump, isMajorJump);
        } else if ("html".equals(channel)) {
            WebUI.broadcast(storyId, LogEntryType.HTML, message, isJump, isMajorJump);
        } else {
            WebUI.broadcast(storyId, LogEntryType.PLAIN, message, isJump, isMajorJump);
        }
    }

    private static boolean hasMarker(ILoggingEvent event, String marker) {
        return event.getMarkerList() != null
                && event.getMarkerList().stream().anyMatch(m -> marker.equals(m.getName()));
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
