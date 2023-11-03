package com.example.routerider;

// NO CHATGPT
public class TransitItem {
    private String id;
    private String type;
    private String time;

    public TransitItem(String id, String type, String time) {
        this.id = id;
        this.type = type;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }
}
