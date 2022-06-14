package pt.ulisboa.tecnico.cmov.conversational_ist;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;

public class FirebaseMessageReceiver extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO : background save in the localDB -- redirect notification to chat -- implement isphoto with metered data

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        Message m = new Message(remoteMessage.getData().get("id"),
                remoteMessage.getData().get("sender"),
                remoteMessage.getData().get("roomID"),
                message,
                remoteMessage.getData().get("createdAt"),
                0);

        FeedReaderDbHelper db = FeedReaderDbHelper.getInstance(getApplicationContext());
        db.createMessage(m);

        System.out.println(db.getAllMessages());

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message Notification ID: " + remoteMessage.getData().get("id"));
        Log.d(TAG, "Message Notification sender: " + remoteMessage.getData().get("sender"));
        Log.d(TAG, "Message Notification message: " + remoteMessage.getData().get("message"));

        //TODO : chat activity open with DB data
        Intent notificationIntent = new Intent(getApplicationContext(), ChatActivity.class);
        //create pending intent
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyNotification", "MyNotification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"MyNotification")
                .setContentTitle(title)
                .setContentText(message).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(123, notificationBuilder.build());
    }

}

