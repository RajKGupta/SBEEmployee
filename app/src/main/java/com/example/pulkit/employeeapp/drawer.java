package com.example.pulkit.employeeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.MyProfile.CompletedJobs;
import com.example.pulkit.employeeapp.MyProfile.ContactCoordinator;
import com.example.pulkit.employeeapp.MyProfile.MyProfile;
import com.example.pulkit.employeeapp.MyProfile.phonebook;
import com.example.pulkit.employeeapp.Notification.NotificationActivity;

public class drawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    EmployeeSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        session = new EmployeeSession(getApplicationContext());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        TextView nav_name = (TextView) header.findViewById(R.id.nav_name);
        nav_name.setText(session.getName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawers();
            } else {
                super.onBackPressed();
            }
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.first:
                Intent intent2 = new Intent(getApplicationContext(), MyProfile.class);
                startActivity(intent2);
                break;
            case R.id.second:
                Intent intent = new Intent(getApplicationContext(), ContactCoordinator.class);
                startActivity(intent);
                break;
            case R.id.fifth:
                //TODO completed jobs
                Intent intent3 = new Intent(getApplicationContext(), CompletedJobs.class);
                startActivity(intent3);
                break;
            case R.id.third:
                Intent intent1 = new Intent(getApplicationContext(), phonebook.class);
                startActivity(intent1);
                break;
            case R.id.fourth:
                //TODO About the firm
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notif:
                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

}
