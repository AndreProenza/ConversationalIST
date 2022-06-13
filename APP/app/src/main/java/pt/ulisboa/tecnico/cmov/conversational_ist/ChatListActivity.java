package pt.ulisboa.tecnico.cmov.conversational_ist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversational_ist.database.CustomAdapter;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderContract;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;

public class ChatListActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView = findViewById(R.id.rview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //TODO Background thread
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this.getBaseContext());

        insert_items_test();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
/*
// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE
        };

// Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "My Title" };

// How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<Long> itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);
        }
        cursor.close();
*/

        Cursor cursor = db.rawQuery("select * from " + FeedReaderContract.FeedEntry.TABLE_NAME,null);

        String[] s = new String[10];
        int i = 0;

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(1);

                s[i] = name;
                cursor.moveToNext();
                i++;
            }
        }

        RecyclerView.Adapter adapterList = new CustomAdapter(s);

        recyclerView.setAdapter(adapterList);
    }



    private void insert_items_test() {
        // Gets the data repository in write mode
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this.getBaseContext());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys

        ContentValues values = new ContentValues();
        int id = (int)(Math.random() * 10000000);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, id);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, "Canal de teste: " + id);

// Insert the new row, returning the primary key value of the new row
        db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        db.close();
    }
}