package pt.ulisboa.tecnico.cmov.conversational_ist;

public class ModelChat {

    private String message;
    private String sender;
    private String timestamp;

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ModelChat(String message, String sender, String timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }
}
