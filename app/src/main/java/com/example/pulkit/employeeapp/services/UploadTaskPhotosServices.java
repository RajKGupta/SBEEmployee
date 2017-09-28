package com.example.pulkit.employeeapp.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.example.pulkit.employeeapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import static com.example.pulkit.employeeapp.EmployeeApp.DBREF;

/**
 * Created by SoumyaAgarwal on 6/28/2017.
 */

public class UploadTaskPhotosServices extends IntentService
{
    public static ArrayList<String> picUriList = new ArrayList<String>();
    public static String taskid;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private static int totalnoofimages;
    private static int s = 0;
    private static int f = 0;

    //private boolean isSuccess;

    public UploadTaskPhotosServices() {
        super("Upload");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        int icon = R.mipmap.ic_upload;
        //isSuccess = false;
        mBuilder = new NotificationCompat.Builder(
                getApplicationContext());
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_upload))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getApplicationContext().getResources().getColor(R.color.white))
                .setContentText("Uploading photos...");
        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            picUriList = intent.getStringArrayListExtra("picUriList");
            taskid = intent.getStringExtra("taskid");
            totalnoofimages = picUriList.size();
            saveImagesToFirebase(picUriList);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void saveImagesToFirebase(ArrayList<String> picUriList)
    {
        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference mediaRef;

        Toast.makeText(getBaseContext(),"Uploading Images in Background", Toast.LENGTH_SHORT).show();
        for (String p: picUriList)
        {


            final long timestamp = System.currentTimeMillis();
            final String fileNameOnFirebase = String.valueOf(timestamp);

            mediaRef = storageRef.getReference().child("TaskImages").child(taskid).child(fileNameOnFirebase);

            Uri l = Uri.fromFile(new File(p));

                mediaRef.putFile(Uri.fromFile(new File(p))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {

                        s++;
                        DatabaseReference ref = DBREF.child("Task").child(taskid).child("DescImages");
                        ref.child(fileNameOnFirebase).setValue(taskSnapshot.getDownloadUrl().toString());

                        if (f+s==totalnoofimages)
                        {
                            updateNotification(s+" Uploaded ,"+f+" Failed ");
                            stopSelf();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        f++;
                        if (f+s==totalnoofimages)
                        {
                            updateNotification(s+" Uploaded ,"+f+" Failed ");
                            stopSelf();
                        }
                    }
                });
        }
    }

    private void updateNotification(String information) {
        notificationManager.cancel(0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext());
        int icon = R.mipmap.ic_launcher;
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getApplicationContext().getResources().getColor(R.color.white))
                .setContentText(information)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(information));

        //Log.d("check","service started"+id);
        synchronized (this) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }
    }
}