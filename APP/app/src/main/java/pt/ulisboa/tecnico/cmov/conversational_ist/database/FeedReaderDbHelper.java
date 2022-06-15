package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversational_ist.model.Channel;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.

    public static FeedReaderDbHelper sInstance;

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "FeedReader.db";
    private Context dbContext;

    public static final String SQL_CREATE_CHANNELS = "CREATE TABLE channels ( channel_id TEXT PRIMARY KEY," +
            "channel_name TEXT NOT NULL);";

    public static final String SQL_CREATE_MESSAGES = "CREATE TABLE messages ( message_id TEXT PRIMARY KEY," +
            "sender TEXT NOT NULL, roomID TEXT NOT NULL, message TEXT NOT NULL, createdAt DATETIME NOT NULL, isPhoto BOOLEAN NOT NULL);";

    public static final String SQL_DELETE_ENTRIES_CHANNELS =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME;

    public static final String SQL_DELETE_ENTRIES_MESSAGES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME;

    public static synchronized FeedReaderDbHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new FeedReaderDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public FeedReaderDbHelper(Context context) {
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

        db.insert(FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME, null, values);

        if(sendBroadcast) {
            Intent i = new Intent("message_inserted_" + m.getRoomID());

            i.putExtra("message", (Serializable) m);

            dbContext.sendBroadcast(i);
        }
        db.close();
    }

    public List<Message> getAllMessages(String roomID) {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME + " WHERE roomID = '" + roomID + "';";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Message m = new Message(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getInt(5));
                messages.add(m);
            }
        }

        db.close();
        return messages;
    }

    public void createChannel(String id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_ID, id);
        values.put(FeedReaderContract.FeedEntry.KEY_CHANNEL_NAME, name);

        db.insert(FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME, null, values);

        db.close();
    }

    public List<Channel> getAllChannels() {
        List<Channel> channels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.CHANNELS_TABLE_NAME + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                Channel m = new Channel(c.getString(0),c.getString(1));
                channels.add(m);
            }
        }

        db.close();
        return channels;
    }

}
