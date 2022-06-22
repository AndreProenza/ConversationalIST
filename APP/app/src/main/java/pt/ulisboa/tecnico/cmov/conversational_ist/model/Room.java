package pt.ulisboa.tecnico.cmov.conversational_ist.model;

public class Room {

    private String roomId;
    private String roomName;
    private boolean isGeoFenced;
    private double lat;
    private double lng;
    private int radius;


    //TODO change notification received

    public Room() {
        // Does nothing
    }

    public Room(String roomId, String roomName, boolean isGeoFenced, double lat, double lng, int radius) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.isGeoFenced = isGeoFenced;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
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

    public boolean isGeoFenced() {
        return isGeoFenced;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getRadius() {
        return radius;
    }
}
