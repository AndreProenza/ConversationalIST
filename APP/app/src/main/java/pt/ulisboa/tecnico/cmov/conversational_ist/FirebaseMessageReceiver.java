package pt.ulisboa.tecnico.cmov.conversational_ist;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import pt.ulisboa.tecnico.cmov.conversational_ist.database.FeedReaderDbHelper;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.Message;
import pt.ulisboa.tecnico.cmov.conversational_ist.database.NotifyActive;
import pt.ulisboa.tecnico.cmov.conversational_ist.model.Room;
import pt.ulisboa.tecnico.cmov.conversational_ist.view.activities.RoomActivity;

public class FirebaseMessageReceiver extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String roomID = remoteMessage.getData().get("roomID");
        String date = remoteMessage.getData().get("createdAt");
        boolean isPhoto = Boolean.parseBoolean(remoteMessage.getData().get("isPhoto"));
        boolean isGeoFenced = Boolean.parseBoolean(remoteMessage.getData().get("isGeoFenced"));
        int notificationID = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("notificationID")));

        Message m = new Message(remoteMessage.getData().get("id"),
                remoteMessage.getData().get("sender"),
                roomID,
                message,
                date,
                isPhoto);

        FeedReaderDbHelper db = FeedReaderDbHelper.getInstance(getApplicationContext());
        db.createMessage(m,true);

        if (!NotifyActive.getInstance().getActive().equals(roomID)) {
            if (isGeoFenced) {
                sendNotificationWithinRoomLocation(title, message, roomID,notificationID);
            } else {
                sendNotification(title, message, roomID ,notificationID);
            }
            FeedReaderDbHelper.getInstance(getApplicationContext()).incrementUnreadMessages(roomID,1);
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void sendNotification(String title, String message, String roomID, int notificationID) {
        Intent notificationIntent = new Intent(getApplicationContext(), RoomActivity.class);

        notificationIntent.putExtra("roomId", roomID);

        PendingIntent contentIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        else
            contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyNotification", "MyNotification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"MyNotification")
                .setContentTitle(title)
                .setContentText(message).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @SuppressLint("MissingPermission")
    private void sendNotificationWithinRoomLocation(String title, String message, String roomID, int notificationID) {
        FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(this);

        Room r = FeedReaderDbHelper.sInstance.getRoom(roomID);

        locationProvider.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                float[] result = new float[1];
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),r.getLat(),r.getLng(),result);
                System.out.println("Location distance : " + result[0]/1000);
                if((result[0]/1000) < r.getRadius()){
                    sendNotification(title, message, roomID, notificationID);
                }
            }
        });
    }

}

