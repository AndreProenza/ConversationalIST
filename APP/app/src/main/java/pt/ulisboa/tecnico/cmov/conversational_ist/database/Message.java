package pt.ulisboa.tecnico.cmov.conversational_ist.database;

public class Message {

    private String id;
    private String sender;
    private String roomID;
    private String message;
    private String createdAt;
    private int isPhoto;

    public Message(String id, String sender, String roomID, String message, String createdAt, int isPhoto) {
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
        return isPhoto == 1;
    }

}
