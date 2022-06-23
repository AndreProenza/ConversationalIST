package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import android.provider.BaseColumns;

public final class FeedReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}


    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String CHANNELS_TABLE_NAME = "channels";
        public static final String KEY_CHANNEL_ID = "channel_id";
        public static final String KEY_CHANNEL_NAME = "channel_name";
        public static final String KEY_CHANNEL_ISGEOFENCED = "channel_isGeoFenced";
        public static final String KEY_CHANNEL_LAT = "channel_lat";
        public static final String KEY_CHANNEL_LNG = "channel_lng";
        public static final String KEY_CHANNEL_RADIUS = "channel_radius";
        public static final String KEY_CHANNEL_UNREAD = "channel_unread";

        public static final String MESSAGES_TABLE_NAME = "messages";
        public static final String KEY_MESSAGE_ID = "message_id";
        public static final String KEY_MESSAGE_SENDER = "sender";
        public static final String KEY_MESSAGE_ROOMID = "roomID";
        public static final String KEY_MESSAGE_MESSAGE = "message";
        public static final String KEY_MESSAGE_CREATEDAT = "createdAt";
        public static final String KEY_MESSAGE_ISPHOTO = "isPhoto";

    }
}
