package com.demo.websocket_server_demo.entity;

import java.util.LinkedList;

public class OffLineMsg {
    private String fromUserId;
    private LinkedList<String> msgs;

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public LinkedList<String> getMsgs() {
        return msgs;
    }

    public void setMsg(LinkedList<String> msgs) {
        this.msgs = msgs;
    }
}
