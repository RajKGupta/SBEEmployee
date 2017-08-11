package com.example.pulkit.employeeapp.MyProfile;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.MainViews.TaskHome;
import com.example.pulkit.employeeapp.R;
import com.google.firebase.database.DatabaseReference;

import org.apache.commons.lang3.text.WordUtils;

import static com.example.pulkit.employeeapp.EmployeeApp.DBREF;

public class MyProfile extends AppCompatActivity {
    EditText name, num, add, username;
    EmployeeSession session;
    AlertDialog customerEditDetails;
    String Name, Num, Add, temp_name,temp_add,temp_num;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        name = (EditText)findViewById(R.id.name);
        num = (EditText)findViewById(R.id.num);
        add = (EditText)findViewById(R.id.add);
        username = (EditText)findViewById(R.id.username);

        session = new EmployeeSession(getApplicationContext());
        db = DBREF.child("Employee").child(session.getUsername());

        Name = session.getName();
        Num = session.getContact();
        Add = session.getAddress();

        name.setText(Name);
        num.setText(Num);
        add.setText(Add);
        username.setText(session.getUsername());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TaskHome.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                final EditText name_new,num_new,add_new;
                Button sub;
                customerEditDetails = new AlertDialog.Builder(this)
                        .setView(R.layout.edit_myself)
                        .create();
                customerEditDetails.show();

                name_new = (EditText) customerEditDetails.findViewById(R.id.name);
                num_new = (EditText) customerEditDetails.findViewById(R.id.num);
                add_new = (EditText) customerEditDetails.findViewById(R.id.add);
                sub = (Button) customerEditDetails.findViewById(R.id.submit);

                name_new.setText(Name);
                num_new.setText(Num);
                add_new.setText(Add);

                sub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        temp_add = add_new.getText().toString();
                        temp_name = name_new.getText().toString();
                        temp_num = num_new.getText().toString();

                        temp_name = WordUtils.capitalizeFully(temp_name);
                        temp_add= WordUtils.capitalizeFully(temp_add);

                        if(TextUtils.isEmpty(temp_add) || TextUtils.isEmpty(temp_name) || TextUtils.isEmpty(temp_num))
                            Toast.makeText(MyProfile.this,"Enter details...",Toast.LENGTH_SHORT).show();

                        else
                        {
                            db.child("name").setValue(temp_name);
                            db.child("address").setValue(temp_add);
                            db.child("phone_num").setValue(temp_num);
                            DBREF.child("Users").child("Usersessions").child(session.getUsername()).child("num").setValue(temp_num);

                            session.edit_oldusersession(temp_name,temp_num,temp_add);
                            customerEditDetails.dismiss();

                            name.setText(temp_name);
                            num.setText(temp_num);
                            add.setText(temp_add);
                        }
                    }
                });

                break;
        }
        return true;
    }


}