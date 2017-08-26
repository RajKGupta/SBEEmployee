package com.example.pulkit.employeeapp;

import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.model.Notif;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class EmployeeApp extends android.support.multidex.MultiDexApplication {
    private static EmployeeApp mInstance;
    public static DatabaseReference DBREF;
    private EmployeeSession session;
    public static SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
    public static String AppName = "demo";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        Fresco.initialize(getApplicationContext());

        DBREF = FirebaseDatabase.getInstance().getReference().child(AppName).getRef();
        session = new EmployeeSession(this);
        String userkey = session.getUsername();
        setOnlineStatus(userkey);
        Fresco.initialize(getApplicationContext());

    }

    public static synchronized EmployeeApp getInstance() {
        return mInstance;
    }

    public static void setOnlineStatus(String userkey) {
        if (!userkey.equals("")) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myConnectionsRef = DBREF.child("Users").child("Usersessions").child(userkey).child("online").getRef();

// stores the timestamp of my last disconnect (the last time I was seen online)
//            final DatabaseReference lastOnlineRef = database.getReference().child("Users").child("Usersessions").child(userkey).child("lastseen").getRef();

            final DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        myConnectionsRef.setValue(Boolean.TRUE);
                        myConnectionsRef.onDisconnect().setValue(Boolean.FALSE);

                        // when I disconnect, update the last time I was seen online
//                        lastOnlineRef.onDisconnect().setValue(Calendar.getInstance().getTime()+"");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("Listener was cancelled at .info/connected");
                }
            });
        }

    }

    public static void sendNotif(final String senderId, final String receiverId, final String type, final String content, final String taskId) {
        long idLong = Calendar.getInstance().getTimeInMillis();
        idLong = 9999999999999L - idLong;
        final String id = String.valueOf(idLong);
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        DBREF.child("Fcmtokens").child(receiverId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String receiverFCMToken = dataSnapshot.getValue(String.class);
                if (receiverFCMToken != null && !receiverFCMToken.equals("")) {
                    Notif newNotif = new Notif(id, timestamp, type, senderId, receiverId, receiverFCMToken, content, taskId);
                    DBREF.child("Notification").child(receiverId).child(id).setValue(newNotif);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public static void sendNotifToAllCoordinators(final String senderId, final String type, final String content, final String taskId) {
        long idLong = Calendar.getInstance().getTimeInMillis();
        idLong = 9999999999999L - idLong;
        final String id = String.valueOf(idLong);
        final String timestamp = formatter.format(Calendar.getInstance().getTime());
        final DatabaseReference dbCoordinator = DBREF.child("Coordinator").getRef();
        dbCoordinator.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    final String receiverId = ds.getKey();
                    DBREF.child("Fcmtokens").child(receiverId).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String receiverFCMToken = dataSnapshot.getValue(String.class);
                            if (receiverFCMToken != null&&!receiverFCMToken.equals("") ) {
                                Notif newNotif = new Notif(id, timestamp, type, senderId, receiverId, receiverFCMToken, content, taskId);
                                DBREF.child("Notification").child(receiverId).child(id).setValue(newNotif);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}