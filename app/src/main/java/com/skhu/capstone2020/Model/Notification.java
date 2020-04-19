package com.skhu.capstone2020.Model;

public class Notification {
    private boolean isSeen = false;
    private int notificationType;
    private String id;

    Notification() {
    }

    Notification(String id, int notificationType) {
        this.id = id;
        this.notificationType = notificationType;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(int notificationType) {
        this.notificationType = notificationType;
    }
}
