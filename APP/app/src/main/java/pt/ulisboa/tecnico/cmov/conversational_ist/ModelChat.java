package pt.ulisboa.tecnico.cmov.conversational_ist;

public class ModelChat {

    private String message;
    private String sender;
    private String timestamp;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ModelChat(String message, String sender, String receiver, String timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }
}
