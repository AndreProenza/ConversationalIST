package pt.ulisboa.tecnico.cmov.conversational_ist.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Room implements Parcelable {

    private String roomId;
    private String roomName;
    private boolean isGeoFenced;
    private double lat;
    private double lng;
    private int radius;
    private int unread;


    //TODO change notification received

    public Room() {
        // Does nothing
    }

    public Room(String roomId, String roomName, boolean isGeoFenced, double lat, double lng, int radius, int unread) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.isGeoFenced = isGeoFenced;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.unread = unread;
    }

    protected Room(Parcel in) {
        roomId = in.readString();
        roomName = in.readString();
        isGeoFenced = in.readByte() != 0;
        lat = in.readDouble();
        lng = in.readDouble();
        radius = in.readInt();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getRoomName() {
        return roomName;
    }

    public int getUnreadNum() {return unread;}

    public void setUnreadNum(int num) {unread = num;}

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomId);
        dest.writeString(roomName);
        dest.writeByte((byte) (isGeoFenced ? 1 : 0));
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeInt(radius);
    }
}
