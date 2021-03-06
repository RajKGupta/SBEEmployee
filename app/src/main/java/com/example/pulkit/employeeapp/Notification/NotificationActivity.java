package com.example.pulkit.employeeapp.Notification;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.pulkit.employeeapp.EmployeeApp;
import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.MainViews.TaskHome;
import com.example.pulkit.employeeapp.R;
import com.example.pulkit.employeeapp.adapters.notification_adapter;
import com.example.pulkit.employeeapp.model.Notif;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.pulkit.employeeapp.EmployeeApp.DBREF;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recview;
    notification_adapter adapter;
    List<Notif> list = new ArrayList<>();
    Notif notif = new Notif();
    String Username, senderuid, taskId;
    EmployeeSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification2);

        session = new EmployeeSession(getApplicationContext());
        Username = session.getUsername();

        Intent intent = getIntent();
        if (intent.hasExtra("seen"))
        {

            final String notifId = intent.getStringExtra("id");
            taskId = intent.getStringExtra("taskId");
            senderuid = intent.getStringExtra("senderuid");
            //String content = session.getName() + " has seen the Job" + ;

            final String receiverId = senderuid;//notificationManager.cancel(Integer.parseInt(id));

            DBREF.child("Task").child(taskId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String taskname = dataSnapshot.getValue(String.class);
                    String content = session.getName() + " has seen the Job " + taskname;
                    EmployeeApp.sendNotif(session.getUsername(),receiverId,"seen",content," ");
                    Toast.makeText(NotificationActivity.this,"Informing Coordinator",Toast.LENGTH_LONG).show();
                    NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(Integer.parseInt(notifId));


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            }
        recview = (RecyclerView) findViewById(R.id.notification_list);
        recview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recview.setItemAnimator(new DefaultItemAnimator());

        adapter = new notification_adapter(list, getApplicationContext());
        recview.setAdapter(adapter);

        preparelist();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TaskHome.class);
        startActivity(intent);
        finish();
    }

    private void preparelist() {
        final DatabaseReference db = DBREF.child("Notification").child(Username).getRef();
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    notif = dataSnapshot.getValue(Notif.class);
                    list.add(notif);
                    sortNotification();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void sortNotification() {
        Collections.sort(list, new Comparator<Notif>() {
            @Override
            public int compare(Notif o1, Notif o2) {
                return Long.parseLong(o1.getId()) < Long.parseLong(o2.getId()) ? -1 : 0; // Decreasing Order
            }
        });
    }

}
