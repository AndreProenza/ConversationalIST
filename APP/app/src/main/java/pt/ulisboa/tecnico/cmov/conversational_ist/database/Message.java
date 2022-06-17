package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import java.io.Serializable;

public class Message implements Serializable {

    private String id;
    private String sender;
    private String roomID;
    private String message;
    private String createdAt;
    private boolean isPhoto;

    public Message(String id, String sender, String roomID, String message, String createdAt, boolean isPhoto) {
        this.id = id;
        this.sender = sender;
        this.roomID = roomID;
        this.message = message;
        this.createdAt = createdAt;
        this.isPhoto = isPhoto;
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isPhoto() {
        return isPhoto;
    }

}
