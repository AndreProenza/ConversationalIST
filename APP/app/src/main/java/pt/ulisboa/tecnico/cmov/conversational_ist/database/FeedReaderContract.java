package pt.ulisboa.tecnico.cmov.conversational_ist.database;

import android.provider.BaseColumns;

public final class FeedReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}


    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "channels";
        public static final String COLUMN_NAME_TITLE = "channel_id";
        public static final String COLUMN_NAME_SUBTITLE = "channel_name";
    }
}
