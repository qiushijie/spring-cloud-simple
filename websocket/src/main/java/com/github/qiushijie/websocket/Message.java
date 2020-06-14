package com.github.qiushijie.websocket;

public class Message {

    private Integer to;

    private String text;

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "to=" + to +
                ", text='" + text + '\'' +
                '}';
    }
}
