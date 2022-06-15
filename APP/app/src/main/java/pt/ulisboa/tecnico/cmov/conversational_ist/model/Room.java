package pt.ulisboa.tecnico.cmov.conversational_ist.model;

import java.util.List;

public class Room {

    private String roomName;
    private String roomDescription;
    private String roomVisibility;

    public Room() {
        // Does nothing
    }

    public Room(String roomName, String roomDescription, String roomVisibility) {
        this.roomName = roomName;
        this.roomDescription = roomDescription;
        this.roomVisibility = roomVisibility;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getRoomVisibility() {
        return roomVisibility;
    }

    public void setRoomVisibility(String roomVisibility) {
        this.roomVisibility = roomVisibility;
    }
}
