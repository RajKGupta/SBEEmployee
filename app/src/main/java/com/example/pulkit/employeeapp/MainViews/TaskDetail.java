package com.example.pulkit.employeeapp.MainViews;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pulkit.employeeapp.adapters.ViewImageAdapter;
import com.example.pulkit.employeeapp.helper.CompressMe;

import com.example.pulkit.employeeapp.helper.DividerItemDecoration;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pulkit.employeeapp.EmployeeLogin.EmployeeSession;
import com.example.pulkit.employeeapp.R;
import com.example.pulkit.employeeapp.adapters.bigimage_adapter;
import com.example.pulkit.employeeapp.adapters.measurement_adapter;
import com.example.pulkit.employeeapp.adapters.taskdetailDescImageAdapter;
import com.example.pulkit.employeeapp.chat.ChatActivity;
import com.example.pulkit.employeeapp.helper.MarshmallowPermissions;
import com.example.pulkit.employeeapp.helper.TouchImageView;
import com.example.pulkit.employeeapp.listener.ClickListener;
import com.example.pulkit.employeeapp.listener.RecyclerTouchListener;
import com.example.pulkit.employeeapp.measurement.MeasureList;
import com.example.pulkit.employeeapp.model.CompletedBy;
import com.example.pulkit.employeeapp.model.CompletedJob;
import com.example.pulkit.employeeapp.model.CustomerAccount;
import com.example.pulkit.employeeapp.model.Quotation;
import com.example.pulkit.employeeapp.model.Task;
import com.example.pulkit.employeeapp.model.measurement;
import com.example.pulkit.employeeapp.services.DownloadFileService;
import com.example.pulkit.employeeapp.services.UploadQuotationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static com.example.pulkit.employeeapp.EmployeeApp.AppName;
import static com.example.pulkit.employeeapp.EmployeeApp.DBREF;
import static com.example.pulkit.employeeapp.EmployeeApp.sendNotif;
import static com.example.pulkit.employeeapp.EmployeeApp.sendNotifToAllCoordinators;
import static com.example.pulkit.employeeapp.EmployeeApp.simpleDateFormat;

public class TaskDetail extends AppCompatActivity implements taskdetailDescImageAdapter.ImageAdapterListener, bigimage_adapter.bigimage_adapterListener, measurement_adapter.measurement_adapterListener {

    private static final int REQUEST_CODE = 101;
    DatabaseReference dbRef, dbTask, dbCompleted, dbAssigned, dbMeasurement, dbDescImages;
    ImageButton download;
    public static String task_id, customerId, taskName;
    public String emp_id, desig;
    private Task task;
    String item;
    public static String customer_id;
    private ArrayList<String> docPaths = new ArrayList<>(), photoPaths = new ArrayList<>();
    private String customername, mykey;
    EditText startDate, endDate, quantity, description, coordinators_message;
    RecyclerView rec_measurement, rec_DescImages;
    Button forward;
    CompressMe compressMe;
    ArrayList<measurement> measurementList = new ArrayList<>();
    measurement_adapter adapter_measurement;
    TextView appByCustomer, uploadStatus;
    DatabaseReference dbQuotation;
    ProgressDialog progressDialog;
    LinearLayout ll;
    TextView text, measure_and_hideme;
    Button measure;
    ScrollView scroll;
    taskdetailDescImageAdapter adapter_taskimages;
    bigimage_adapter adapter;
    ViewImageAdapter madapter;
    private AlertDialog viewSelectedImages, confirmation, viewSelectedImages1, viewSelectedImages_measure;
    ArrayList<String> DescImages = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    EmployeeSession session;
    String dbTablekey, id;
    String num;
    private MarshmallowPermissions marshmallowPermissions;
    private static final int PICK_FILE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        dbRef = DBREF;

        session = new EmployeeSession(getApplicationContext());

        marshmallowPermissions = new MarshmallowPermissions(this);
        progressDialog = new ProgressDialog(this);
        download = (ImageButton) findViewById(R.id.download);
        if (!session.getDesig().toLowerCase().equals("accounts")) {
            download.setVisibility(View.GONE);
        }
        uploadStatus = (TextView) findViewById(R.id.uploadStatus);
        appByCustomer = (TextView) findViewById(R.id.appByCustomer);

        scroll = (ScrollView) findViewById(R.id.scroll);
        measure = (Button) findViewById(R.id.measure);
        forward = (Button) findViewById(R.id.forward);
        coordinators_message = (EditText) findViewById(R.id.coordinators_message);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        quantity = (EditText) findViewById(R.id.quantity);
        description = (EditText) findViewById(R.id.description);
        rec_measurement = (RecyclerView) findViewById(R.id.rec_measurement);
        rec_DescImages = (RecyclerView) findViewById(R.id.rec_DescImages);
        measure_and_hideme = (TextView) findViewById(R.id.measure_and_hideme);
        text = (TextView) findViewById(R.id.textView6);
        ll = (LinearLayout) findViewById(R.id.quotation_container);

        mykey = session.getUsername();
        Intent intent = getIntent();
        task_id = intent.getStringExtra("task_id");

        // to get customer id
        DBREF.child("Task").child(task_id).child("customerId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                id = dataSnapshot.getValue(String.class);
                customer_id = id;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        emp_id = TaskHome.emp_id;
        desig = TaskHome.desig;

        dbTask = dbRef.child("Task").child(task_id);
        dbQuotation = dbTask.child("Quotation").getRef();
        dbCompleted = dbTask.child("CompletedBy").getRef();
        dbAssigned = dbTask.child("AssignedTo").getRef();
        dbMeasurement = dbTask.child("Measurement").getRef();
        dbDescImages = dbTask.child("DescImages").getRef();

        rec_measurement.setLayoutManager(new LinearLayoutManager(this));
        rec_measurement.setItemAnimator(new DefaultItemAnimator());
        rec_measurement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter_measurement = new measurement_adapter(measurementList, this, this);
        rec_measurement.setAdapter(adapter_measurement);

        rec_DescImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rec_DescImages.setItemAnimator(new DefaultItemAnimator());
        rec_DescImages.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        adapter_taskimages = new taskdetailDescImageAdapter(DescImages, getApplicationContext(), this);
        rec_DescImages.setAdapter(adapter_taskimages);


       /* if(desig.toLowerCase().equals("field executive"))
            measure.setVisibility(View.GONE);
*/
        dbDescImages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    rec_DescImages.setVisibility(View.VISIBLE);
                    String item = dataSnapshot.getValue(String.class);
                    DescImages.add(item);
                    adapter_taskimages.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String item = dataSnapshot.getKey();
                DescImages.remove(item);
                adapter_taskimages.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TaskDetail.this, MeasureList.class));
                finish();
            }
        });

        dbTask.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                task = dataSnapshot.getValue(Task.class);
                setValue(task);
                dbMeasurement.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (task.getMeasurementApproved() != null) {
                                if (task.getMeasurementApproved() == Boolean.TRUE) {
                                    measure_and_hideme.setText("Approved By Me: Yes");
                                } else {
                                    measure_and_hideme.setText("Approved By Me: No");
                                }
                            }

                        } else {
                            measure_and_hideme.setText("No measurement taken for this job");

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                getSupportActionBar().setTitle(task.getName());
                taskName = task.getName();
                customerId = task.getCustomerId();
                DatabaseReference dbCustomerName = DBREF.child("Customer").child(customerId).getRef();
                dbCustomerName.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        customername = dataSnapshot.child("name").getValue(String.class);
                        getSupportActionBar().setSubtitle(customername);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmation = new AlertDialog.Builder(TaskDetail.this)
                        .setView(R.layout.confirmation_layout).create();
                confirmation.show();

                final EditText employeeNote = (EditText) confirmation.findViewById(R.id.employeeNote);
                Button okcompleted = (Button) confirmation.findViewById(R.id.okcompleted);
                Button okcanceled = (Button) confirmation.findViewById(R.id.okcanceled);

                okcanceled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmation.dismiss();
                    }
                });

                okcompleted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String employeesnote = employeeNote.getText().toString().trim();
                        Calendar c = Calendar.getInstance();
                        final String curdate = simpleDateFormat.format(c.getTime());

                        final DatabaseReference db, databaseReference;

                        DBREF.child("Employee").child(emp_id).child("CompletedTask").child(task_id).setValue("complete");
                        db = DBREF.child("Employee").child(emp_id).child("AssignedTask").child(task_id);
                        db.removeValue();

                        databaseReference = DBREF.child("Task").child(task_id).child("AssignedTo").child(emp_id);

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    CompletedBy completedBy = dataSnapshot.getValue(CompletedBy.class);
                                    CompletedJob completedJob = new CompletedJob();
                                    completedJob.setEmpId(completedBy.getEmpId());
                                    completedJob.setAssignedByName(completedBy.getAssignedByName());
                                    completedJob.setAssignedByUsername(completedBy.getAssignedByUsername());
                                    completedJob.setCoordinatorNote(completedBy.getNote());
                                    completedJob.setDateassigned(completedBy.getDateassigned());
                                    completedJob.setDatecompleted(curdate);
                                    completedJob.setEmpployeeNote(employeesnote);
                                    completedJob.setEmpName(session.getName());
                                    completedJob.setEmpDesignation(session.getDesig());

                                    databaseReference.removeValue();
                                    DBREF.child("Task").child(task_id).child("CompletedBy").child(emp_id).setValue(completedJob);

                                    String contentforme = "You completed " + task.getName();
                                    sendNotif(mykey, mykey, "completedJob", contentforme, task_id);
                                    String contentforother = "Employee " + session.getName() + " completed " + task.getName();
                                    sendNotif(mykey, completedJob.getAssignedByUsername(), "completedJob", contentforother, task_id);
                                    Toast.makeText(TaskDetail.this, contentforme, Toast.LENGTH_SHORT).show();
                                    Intent intent1 = new Intent(TaskDetail.this, TaskHome.class);
                                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent1);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        confirmation.dismiss();
                    }
                });
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!marshmallowPermissions.checkPermissionForCamera()) {
                    marshmallowPermissions.requestPermissionForExternalStorage();
                    if (!marshmallowPermissions.checkPermissionForExternalStorage())
                        showToast("Cannot Download because external storage permission not granted");
                    else
                        launchLibrary();
                } else {

                    launchLibrary();
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    uploadFile(photoPaths.get(0), "photo");
                }
                break;

            case FilePickerConst.REQUEST_CODE_DOC:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    for (String result : docPaths) {
                        uploadFile(result, "doc");
                    }
                }
                break;

        }

    }


    private void uploadFile(String filePath, String type) {

        if (filePath != null && !filePath.equals("")) {
            ArrayList<String> taskid_list = new ArrayList<>();

            taskid_list.add(task.getTaskId());

            String temp = Uri.fromFile(new File(filePath)).toString();

            Intent serviceIntent = new Intent(this, UploadQuotationService.class);
            serviceIntent.putExtra("TaskIdList", taskid_list);
            serviceIntent.putExtra("customerId", customerId);
            serviceIntent.putExtra("selectedFileUri", temp);
            serviceIntent.putExtra("customerName", customername);

            this.startService(serviceIntent);
        } else {
            Toast.makeText(this, "Cannot upload file to server", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taskdetail_menu, menu);
        final MenuItem item = menu.findItem(R.id.item3);
        String desig = session.getDesig();
        if (session.getDesig().equals("Accounts")) {
            item.setVisible(true);
        } else
            item.setVisible(false);
        return true;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                DBREF.child("Customer").child(id).child("phone_num").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        num = dataSnapshot.getValue(String.class);
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + num));
                        startActivity(callIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;
            case R.id.item2:
                checkChatref(mykey, id);
                break;

            case R.id.manage_account:
                AlertDialog customerAccountDialog = new AlertDialog.Builder(this)
                        .setView(R.layout.account_info_layout)
                        .create();
                customerAccountDialog.show();
                final Button edit, submit;
                final EditText total, advance, balance;
                final LinearLayout balanceLayout;
                total = (EditText) customerAccountDialog.findViewById(R.id.total);
                advance = (EditText) customerAccountDialog.findViewById(R.id.advance);
                balance = (EditText) customerAccountDialog.findViewById(R.id.balance);
                edit = (Button) customerAccountDialog.findViewById(R.id.edit);
                submit = (Button) customerAccountDialog.findViewById(R.id.submit);
                balanceLayout = (LinearLayout) customerAccountDialog.findViewById(R.id.balanceLayout);
                final DatabaseReference dbaccountinfo = DBREF.child("Customer").child(customerId).child("Account").getRef();
                ValueEventListener dbaccountlistener = dbaccountinfo.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            CustomerAccount customerAccount = dataSnapshot.getValue(CustomerAccount.class);
                            total.setText(customerAccount.getTotal() + "");
                            advance.setText(customerAccount.getAdvance() + "");
                            balance.setText((customerAccount.getTotal() - customerAccount.getAdvance()) + "");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        total.setEnabled(true);
                        advance.setEnabled(true);
                        balanceLayout.setVisibility(View.GONE);
                        submit.setVisibility(View.VISIBLE);
                        edit.setVisibility(View.GONE);
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CustomerAccount customerAccount = new CustomerAccount();
                        String totalString =total.getText().toString().trim();
                        String advanceTotal = advance.getText().toString().trim();
                        if(totalString!=null&&advanceTotal!=null&&!totalString.equals("")&&!advanceTotal.equals("")) {
                            Integer total_amount = Integer.parseInt(totalString);
                            customerAccount.setTotal(total_amount);
                            Integer advance_amount = Integer.parseInt(advanceTotal);
                            customerAccount.setAdvance(advance_amount);
                            if(total_amount<advance_amount){
                                Toast.makeText(getApplicationContext(),"Invalid amount entered",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                dbaccountinfo.setValue(customerAccount);
                                total.setEnabled(false);
                                advance.setEnabled(false);
                                balanceLayout.setVisibility(View.VISIBLE);
                                submit.setVisibility(View.GONE);
                                edit.setVisibility(View.VISIBLE);
                                sendNotif(emp_id, emp_id, "accountReset", "You modified the account details of " + customername, customer_id);
                                sendNotif(emp_id, customerId, "accountReset", "Your advance deposited is Rs." + advance_amount + " and balance left is Rs." + (total_amount - advance_amount), customerId);
                                sendNotifToAllCoordinators(emp_id, "accountReset",session.getName()+" modified account details of "+ customername + ". Advance deposited is Rs." + advance_amount + " and balance left is Rs." + (total_amount - advance_amount), customerId);
                            }}
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Invalid amount entered",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            case R.id.item3:
                if (desig.equals("Accounts")) {
                    LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
                    View mView = layoutInflaterAndroid.inflate(R.layout.options_foruploadquotation, null);
                    AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
                    alertDialogBuilderUserInput.setView(mView);

                    LinearLayout uploadPhoto = (LinearLayout) mView.findViewById(R.id.uploadPhoto);
                    LinearLayout uploadDoc = (LinearLayout) mView.findViewById(R.id.uploadDoc);


                    alertDialogBuilderUserInput.setCancelable(true);
                    final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                    alertDialogAndroid.show();
                    uploadPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FilePickerBuilder.getInstance().setMaxCount(1)
                                    .setActivityTheme(R.style.AppTheme)
                                    .pickPhoto(TaskDetail.this);
                            alertDialogAndroid.dismiss();
                        }
                    });
                    uploadDoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FilePickerBuilder.getInstance().setMaxCount(1)
                                    .setActivityTheme(R.style.AppTheme)
                                    .pickFile(TaskDetail.this);
                            alertDialogAndroid.dismiss();
                        }
                    });
                    break;
                }}
                return true;
        }




    private void checkChatref(final String mykey, final String otheruserkey) {
        DatabaseReference dbChat = DBREF.child("Chats").child(mykey + otheruserkey).getRef();
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("query1" + mykey + otheruserkey);
                System.out.println("datasnap 1" + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    System.out.println("datasnap exists1" + dataSnapshot.toString());
                    dbTablekey = mykey + otheruserkey;
                    goToChatActivity();

                } else {
                    checkChatref2(mykey, otheruserkey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkChatref2(final String mykey, final String otheruserkey) {
        final DatabaseReference dbChat = DBREF.child("Chats").child(otheruserkey + mykey).getRef();
        dbTablekey = otheruserkey + mykey;
        dbChat.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println("query1" + otheruserkey + mykey);
                    goToChatActivity();
                } else {
                    DBREF.child("Users").child("Userchats").child(mykey).child(otheruserkey).setValue(dbTablekey);
                    DBREF.child("Users").child("Userchats").child(otheruserkey).child(mykey).setValue(dbTablekey);
                    goToChatActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void goToChatActivity() {
        Intent in = new Intent(this, ChatActivity.class);
        in.putExtra("dbTableKey", dbTablekey);
        in.putExtra("otheruserkey", id);
        startActivity(in);
    }

    private void launchLibrary() {
        final String[] url = new String[1];
        dbQuotation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Quotation quotation = dataSnapshot.getValue(Quotation.class);
                    url[0] = quotation.getUrl();
                    Intent serviceIntent = new Intent(getApplicationContext(), DownloadFileService.class);
                    serviceIntent.putExtra("TaskId", task_id);
                    serviceIntent.putExtra("url", url[0]);
                    startService(serviceIntent);
                } else {
                    Toast.makeText(TaskDetail.this, "No Quotation Uploaded Yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(TaskDetail.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(TaskDetail.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TaskDetail.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        } else {
            launchLibrary();
        }

    }

    private void prepareListData() {
        dbMeasurement.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    measurement item = dataSnapshot.getValue(measurement.class);
                    measurementList.add(item);
                    adapter_measurement.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                measurement item = dataSnapshot.getValue(measurement.class);
                measurementList.remove(item);
                adapter_measurement.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void setValue(Task task) {
        quantity.setText(task.getQty());
        description.setText(task.getDesc());

        dbAssigned.child(session.getUsername()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    CompletedBy completedBy = dataSnapshot.getValue(CompletedBy.class);
                    if (completedBy.getNote()!=null)
                        coordinators_message.setText(completedBy.getNote());
                    if(completedBy.getDatecompleted()!=null)
                        endDate.setText(completedBy.getDatecompleted());
                    startDate.setText(completedBy.getDateassigned());



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dbQuotation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    appByCustomer.setVisibility(View.VISIBLE);
                    Quotation quotation = dataSnapshot.getValue(Quotation.class);
                    appByCustomer.setText(" " + quotation.getApprovedByCust());
                    uploadStatus.setText(" Yes");
                } else {
                    appByCustomer.setVisibility(View.GONE);
                    uploadStatus.setText(" No");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchLibrary();
                } else {
                    checkPermission();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        measurementList.clear();
        prepareListData();
    }

    @Override
    public void onImageClicked(int position) {
        viewSelectedImages = new AlertDialog.Builder(TaskDetail.this)
                .setView(R.layout.view_image_on_click).create();
        viewSelectedImages.show();

        RecyclerView bigimage = (RecyclerView) viewSelectedImages.findViewById(R.id.bigimage);

        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        bigimage.setLayoutManager(linearLayoutManager);
        bigimage.setItemAnimator(new DefaultItemAnimator());
        bigimage.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.HORIZONTAL));

        adapter = new bigimage_adapter(DescImages, this, this);
        bigimage.setAdapter(adapter);

        bigimage.scrollToPosition(position);
    }

    @Override
    public void ondownloadButtonClicked(final int position, final bigimage_adapter.MyViewHolder holder) {
        if (!marshmallowPermissions.checkPermissionForExternalStorage()) {
            marshmallowPermissions.requestPermissionForExternalStorage();
        } else {
            holder.progressBar = new ProgressBar(TaskDetail.this);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.download_taskdetail_image.setVisibility(View.GONE);
            String url = DescImages.get(position);

            final StorageReference str = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            final String[] ext = new String[1];
            str.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    ext[0] = storageMetadata.getContentType();
                    int p = ext[0].lastIndexOf("/");
                    String l = "." + ext[0].substring(p + 1);


                    File rootPath = new File(Environment.getExternalStorageDirectory(), AppName + "/TaskDetailImages");

                    if (!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    String uriSting = System.currentTimeMillis() + l;

                    final File localFile = new File(rootPath, uriSting);

                    str.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                            holder.download_taskdetail_image.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.GONE);
                            Toast.makeText(TaskDetail.this, "Image " + position + 1 + " Downloaded", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("firebase ", ";local tem file not created  created " + exception.toString());
                            holder.download_taskdetail_image.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.GONE);
                            Toast.makeText(TaskDetail.this, "Failed to download image " + position + 1, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }
    }

    @Override
    public void onImageClicked(int position, measurement_adapter.MyViewHolder holder) {
        viewSelectedImages_measure = new AlertDialog.Builder(TaskDetail.this)
                .setView(R.layout.viewmeasureimage).create();
        viewSelectedImages_measure.show();

        measurement m = measurementList.get(position);
        String uri = m.getFleximage();

        TouchImageView viewchatimage = (TouchImageView) viewSelectedImages_measure.findViewById(R.id.chatimage);
        ImageButton backbutton = (ImageButton) viewSelectedImages_measure.findViewById(R.id.back);

        Glide.with(getApplicationContext())
                .load(Uri.parse(uri))
                .placeholder(R.color.black)
                .crossFade()
                .centerCrop()
                .into(viewchatimage);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewSelectedImages_measure.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}