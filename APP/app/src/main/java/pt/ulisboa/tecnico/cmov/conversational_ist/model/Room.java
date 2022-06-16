package pt.ulisboa.tecnico.cmov.conversational_ist.model;

import java.util.List;

public class Room {

    private String roomName;
    public String roomId;
    //private String roomDescription;
    //private String roomVisibility;



    public Room() {
        // Does nothing
    }

    public Room(String roomName, String roomId) {
        this.roomName = roomName;
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
