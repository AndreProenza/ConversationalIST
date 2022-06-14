package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.

    public static FeedReaderDbHelper sInstance;

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "FeedReader.db";

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

    public void createMessage(Message m) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ID, m.getId());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_SENDER, m.getSender());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ROOMID, m.getRoomID());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_MESSAGE, m.getMessage());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_CREATEDAT, m.getCreatedAt());
        values.put(FeedReaderContract.FeedEntry.KEY_MESSAGE_ISPHOTO, m.isPhoto());

        db.insert(FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME, null, values);
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FeedReaderContract.FeedEntry.MESSAGES_TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Message m = new Message(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getInt(5));
                messages.add(m);
            } while (c.moveToNext());
        }

        return messages;
    }

}
