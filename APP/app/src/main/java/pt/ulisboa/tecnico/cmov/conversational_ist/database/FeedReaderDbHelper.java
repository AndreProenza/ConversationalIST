package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.

    public static FeedReaderDbHelper sInstance;

    public static final int DATABASE_VERSION = 25;
    public static final String DATABASE_NAME = "FeedReader.db";
    private final Context dbContext;

    public static final String SQL_CREATE_CHANNELS = "CREATE TABLE channels ( channel_id TEXT PRIMARY KEY," +
            "channel_name TEXT NOT NULL, channel_isGeoFenced BOOLEAN NOT NULL, channel_lat REAL NOT NULL, channel_lng REAL NOT NULL, channel_radius INTEGER NOT NULL, channel_unread INTEGER NOT NULL);";

    public static final String SQL_CREATE_MESSAGES = "CREATE TABLE messages ( message_id TEXT PRIMARY KEY," +
            "sender TEXT NOT NULL, roomID TEXT NOT NULL, message TEXT NOT NULL, createdAt DATETIME NOT NULL, isPhoto BOOLEAN NOT NULL);";

    public static final String SQL_DELETE_ENTRIES_CHANNELS =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME;

    public static final String SQL_DELETE_ENTRIES_MESSAGES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME;

    public static synchronized FeedReaderDbHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new FeedReaderDbHelper(context);
        }
        return sInstance;
    }

    private FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CHANNELS);
        db.execSQL(SQL_CREATE_MESSAGES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES_CHANNELS);
        db.execSQL(SQL_DELETE_ENTRIES_MESSAGES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createMessage(Message m, Boolean sendBroadcast) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ID, m.getId());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_SENDER, m.getSender());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ROOMID, m.getRoomID());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_MESSAGE, m.getMessage());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_CREATEDAT, m.getCreatedAt());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ISPHOTO, m.isPhoto());

        db.insertWithOnConflict(FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);


        if(sendBroadcast) {
            Intent i = new Intent("message_inserted");

            i.putExtra("message", m);

            dbContext.sendBroadcast(i);
        }
        db.close();
    }

    public List<Message> getAllMessages(String roomID) {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME +
                " WHERE roomID = '" + roomID + "' ORDER BY " + FeedReaderContract.FeedEntry.KEY_MESSAGE_CREATEDAT + ";";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                boolean isPhoto = c.getInt(5) == 1;
                Message m = new Message(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4), isPhoto);
                messages.add(m);
                c.moveToNext();
            }
        }

        c.close();
        db.close();
        return messages;
    }

    public synchronized void clearUnreadMessages(String id) {
        String query = "UPDATE " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME + " SET " + FeedReaderContract.FeedEntry.KEY_CHANNEL_UNREAD
                + " = 0  WHERE " + FeedReaderContract.FeedEntry.KEY_CHANNEL_ID + "= '" + id + "';";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();

        db.close();
        c.close();

        Intent i = new Intent("messages_read");

        i.putExtra("roomID", id);

        dbContext.sendBroadcast(i);
    }

    public void incrementUnreadMessages(String id, int amount) {
       String query = "UPDATE " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME + " SET " + FeedReaderContract.FeedEntry.KEY_CHANNEL_UNREAD
               + " = " + FeedReaderContract.FeedEntry.KEY_CHANNEL_UNREAD + " + " + amount + " WHERE " + FeedReaderContract.FeedEntry.KEY_CHANNEL_ID + "= '" + id + "';";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        db.close();
        c.close();
    }

    public boolean isChannelSubscribed(String ID) {
        String selectQuery = "SELECT * FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME + " WHERE channel_id = '" +  ID + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        boolean result = c.getCount() == 0;

        db.close();
        c.close();
        return result;
    }

    public void createChannel(Room r) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_ID, r.getRoomId());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_NAME, r.getRoomName());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_ISGEOFENCED, r.isGeoFenced());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_LAT, r.getLat());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_LNG, r.getLng());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_RADIUS, r.getRadius());
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_UNREAD, r.getUnreadNum());

        db.insertWithOnConflict(FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        db.close();
    }

    public Room getRoom(String roomID) {
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME +
                " WHERE " + FeedReaderContract.FeedEntry.KEY_CHANNEL_ID + "='" + roomID + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        Room r = null;

        if (c.moveToFirst()) {
            boolean isGeoFenced = c.getInt(2) == 2;
            r = new Room(c.getString(0), c.getString(1),isGeoFenced,c.getDouble(3),c.getDouble(4),c.getInt(5), c.getInt(6));
            c.moveToNext();
        }

        db.close();
        c.close();
        return r;
    }

    public String getChannelName(String id) {
        String selectQuery = "SELECT * FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME +
                " WHERE " +  FeedReaderContract.FeedEntry.KEY_CHANNEL_ID + " = '" +  id + "';";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        String result = "";

        if (c.moveToFirst()) {
            result = c.getString(1);
        }

        db.close();
        c.close();
        return result;
    }

    public void leaveRoom(String id) {
        String query = "DELETE FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME +
                " WHERE " + FeedReaderContract.FeedEntry.KEY_CHANNEL_ID + " = '" + id + "' ;";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery(query, null);

        c.moveToFirst();
        db.close();
        c.close();
    }

    public ArrayList<Room> getGeoFencedRooms(int i) {
        ArrayList<Room> rooms = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.KEY_CHANNEL_ISGEOFENCED + "=" + i + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                boolean isGeoFenced = c.getInt(2) == 1;
                rooms.add(new Room(c.getString(0), c.getString(1),isGeoFenced,c.getDouble(3),c.getDouble(4),c.getInt(5), c.getInt(6)));
                c.moveToNext();
            }
        }

        db.close();
        c.close();
        return rooms;
    }

}
