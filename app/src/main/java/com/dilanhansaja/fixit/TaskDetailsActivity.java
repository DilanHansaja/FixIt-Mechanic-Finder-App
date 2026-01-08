package com.dilanhansaja.fixit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dilanhansaja.fixit.model.MyAlert;

public class TaskDetailsActivity extends AppCompatActivity {

    private String request_id;

    private double amount=0;

    private String mechanic_id;

    private String paymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.task_details_scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        request_id = intent.getStringExtra("req_id");
        addSnapshotListener(request_id);

    }

    private void addSnapshotListener(String request_id) {

        if (request_id != null) {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("mechanic_request").document(request_id)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {

                            if (doc != null) {
                                if (doc.exists()) {
                                    loadDetails(doc);
                                    Log.d("FixItLog", "TaskDetailsActivity: document details received");
                                } else {
                                    Log.d("FixItLog", "TaskDetailsActivity: document not exists");
                                }
                            } else {
                                Log.d("FixItLog", "TaskDetailsActivity: document result is null");
                            }
                        }
                    });
        } else {
            Log.d("FixItLog", "TaskDetailsActivity: Status or request id not found : request id=" + request_id);
        }
    }

    private void loadDetails(DocumentSnapshot doc) {

        TextView clientNameView = findViewById(R.id.task_details_clinetNameTextView);
        TextView paymentMethodTextView = findViewById(R.id.task_details_paymentMethodTextView);
        TextView statusTextView = findViewById(R.id.task_details_statusTextView);
        TextView mobileTextView = findViewById(R.id.task_details_mobileTextView);
        Button callBtn = findViewById(R.id.task_details_callBtn);
        ImageView vehicleTypeImageView = findViewById(R.id.task_details_vehicleTypeImageView);
        ImageView locationImageView = findViewById(R.id.task_details_locationImageView);
        EditText notesEditText = findViewById(R.id.task_details_notesEditText);
        EditText codeEditText = findViewById(R.id.task_details_requestCodeEditText);
        TextView dateTextView = findViewById(R.id.task_details_DateTextView);
        TextView startAtTextView = findViewById(R.id.task_details_startAtTextView);
        TextView endAtTextView = findViewById(R.id.task_details_endAtTextView);
        TextView timeSpentTextView = findViewById(R.id.task_details_timeSpent);
        TextView earningsTextView = findViewById(R.id.task_details_earningsTextView);
        Button startBtn = findViewById(R.id.task_details_startBtn);
        Button endBtn = findViewById(R.id.task_details_endBtn);


        String vehicle_type = doc.getString("vehicle_type");

        if (vehicle_type.equals("Bike")) {
            vehicleTypeImageView.setImageResource(R.drawable.motorcycle);
        } else if (vehicle_type.equals("Three-Wheeler")) {
            vehicleTypeImageView.setImageResource(R.drawable.three_wheeler);
        } else if (vehicle_type.equals("Car")) {
            vehicleTypeImageView.setImageResource(R.drawable.car);
        } else if (vehicle_type.equals("Van")) {
            vehicleTypeImageView.setImageResource(R.drawable.van);
        } else if (vehicle_type.equals("Heavy")) {
            vehicleTypeImageView.setImageResource(R.drawable.heavy);
        } else if (vehicle_type.equals("Other")) {
            vehicleTypeImageView.setImageResource(R.drawable.tractor);
        }

        paymentMethod=doc.getString("payment_method");

        clientNameView.setText(doc.getString("user_name"));
        paymentMethodTextView.setText(paymentMethod);
        mobileTextView.setText(doc.getString("user_mobile"));

        mechanic_id=doc.getString("mechanic_id");

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCall(mobileTextView);
            }
        });

        statusTextView.setText(doc.getString("status"));

        locationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String lat = String.valueOf(doc.getDouble("user_lat"));
                String lng = String.valueOf(doc.getDouble("user_lng"));

                String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        if (doc.contains("notes")) {
            notesEditText.setText(doc.getString("notes"));
        }

        String[] requestDateArray = doc.getString("request_made_on").split(" ");

        String startAt = null;
        String endAt = null;

        if (!doc.getString("start_at").isBlank()) {

            startAt = doc.getString("start_at");

            String[] startDateArray = startAt.split(" ");

            if (startDateArray[0].equals(requestDateArray[0])) {
                String s = startDateArray[1] + " " + startDateArray[2];
                startAtTextView.setText(s);
            } else {
                String s = startDateArray[0] + " " + startDateArray[1] + " " + startDateArray[2];
                startAtTextView.setText(s);
            }

        }

        if (!doc.getString("end_at").isBlank()) {

            endAt = doc.getString("end_at");

            String[] endDateArray = endAt.split(" ");

            if (endDateArray[0].equals(requestDateArray[0])) {
                String s = endDateArray[1] + " " + endDateArray[2];
                endAtTextView.setText(s);
            } else {
                String s = endDateArray[0] + " " + endDateArray[1] + " " + endDateArray[2];
                endAtTextView.setText(s);
            }

        }

        if (startAt != null && endAt != null) {

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

            try {
                Date startDate = format.parse(startAt);
                Date endDate = format.parse(endAt);

                long durationMillis = endDate.getTime() - startDate.getTime();

                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;

                String durationText = hours + "h " + minutes + "m";

                timeSpentTextView.setText(durationText);

                // Convert milliseconds to total hours
                double totalHours = (double) durationMillis / (1000 * 60 * 60);

                double totalEarnings;

                if (totalHours <= 1) {
                    totalEarnings = doc.getDouble("rate");
                } else {
                    totalEarnings = totalHours * doc.getDouble("rate");
                }

                DecimalFormat df = new DecimalFormat("0.00");

                amount=Double.parseDouble(df.format(totalEarnings));

                String total = "LKR " + df.format(totalEarnings);
                earningsTextView.setText(total);

            } catch (ParseException e) {
                Log.d("FixItLog", "TaskDetailsActivity: Exception =" + e);
            }
        }

        dateTextView.setText(requestDateArray[0]);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!codeEditText.getText().toString().isBlank()) {

                    String code = codeEditText.getText().toString();


                    if (code.equals(doc.getString("request_code"))) {

                        updateStatus("Ongoing");

                    } else {
                        showAlert(
                                codeEditText,
                                "Invalid Request Code",
                                "The request code you entered is incorrect. Please double-check with the client and try again."
                        );
                    }

                } else {

                    showAlert(
                            codeEditText,
                            "Request Code Required",
                            "You must enter the request code provided by the client before starting the task. Please ask the client for the code and try again."
                    );
                }
            }
        });

        if (doc.getString("status").equals("Ongoing")) {
            endBtn.setEnabled(true);
            startBtn.setEnabled(false);
            codeEditText.setEnabled(false);
            codeEditText.setText(doc.getString("request_code"));

            endBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateStatus("Completed");
                }
            });
        }else if(doc.getString("status").equals("Completed")){

            codeEditText.setEnabled(false);
            codeEditText.setText(doc.getString("request_code"));

            LinearLayout btnLayout = findViewById(R.id.task_details_ButtonLayout);
            LinearLayout cashLayout = findViewById(R.id.task_details_markPaymentRecievedLayout);
            Button cashBtn = findViewById(R.id.task_details_cashBtn);

            btnLayout.setVisibility(View.GONE);

            if(paymentMethod.equals("Cash")){
                cashLayout.setVisibility(View.VISIBLE);

                cashBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {



                        MyAlert myAlert = MyAlert.getMyAlert();
                        View alertView = myAlert.getConfirmationAlert(
                                TaskDetailsActivity.this,
                                "Receive cash payment ?",
                                "Please confirm that you have received the full cash payment from the user before proceeding."
                        );

                        AlertDialog alertDialog = myAlert.getDialog(alertView,TaskDetailsActivity.this);

                        Button confirmBtn = alertView.findViewById(R.id.alert_right_button);
                        Button cancelBtn = alertView.findViewById(R.id.alert_left_button);

                        confirmBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                                updateStatus("Paid");

                            }
                        });

                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.show();

                    }
                });
            }

        }
    }

    private void updateStatus(String status) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        String date = simpleDateFormat.format(new Date());

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);

        if (status.equals("Ongoing")) {
            map.put("start_at", date);
        } else if (status.equals("Completed")) {
            map.put("end_at", date);
        }

        firestore.collection("mechanic_request").document(request_id)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("FixItLog", "TaskDetailsActivity: update status - success");

                        if (status.equals("Completed")) {
                            showCompletedAlert();
                        }
                        if (status.equals("Ongoing")) {
                            EditText codeEditText=findViewById(R.id.task_details_requestCodeEditText);
                            codeEditText.setEnabled(false);
                        }
                        if (status.equals("Paid")) {
                            Log.d("FixItLog", "TaskDetailsActivity: update status - success (Paid cash)");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            updateIncome(sdf.format(new Date()));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "TaskDetailsActivity: update status - failure");
                    }
                });


    }

    private void showAlert(EditText codeEditText, String title, String msg) {
        MyAlert myAlert = MyAlert.getMyAlert();
        View alertView = myAlert.showOkBtnAlert(
                TaskDetailsActivity.this,
                title,
                msg,
                R.drawable.alert_warning
        );

        Button button = alertView.findViewById(R.id.ok_alert_btn);
        AlertDialog alertDialog = myAlert.getDialog(alertView, TaskDetailsActivity.this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                codeEditText.requestFocus();
            }
        });

        alertDialog.show();
    }

    private void showCompletedAlert() {


        MyAlert myAlert = MyAlert.getMyAlert();
        View alertView = myAlert.showOkBtnAlert(
                TaskDetailsActivity.this,
                "Task Completed Successfully",
                "You have successfully completed the task. You can check the task history for more details.",
                R.drawable.alert_success
        );

        Button button = alertView.findViewById(R.id.ok_alert_btn);
        AlertDialog alertDialog = myAlert.getDialog(alertView, TaskDetailsActivity.this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                if(paymentMethod.equals("Card")){
                    Intent intent = new Intent(TaskDetailsActivity.this,HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("TaskHistory","");
                    startActivity(intent);
                }
            }
        });

        alertDialog.show();
    }

    private void startCall(TextView mobileTextView){

        if(checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED){

            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri uri = Uri.parse("tel:"+mobileTextView.getText().toString());
            intent.setData(uri);
            startActivity(intent);

        }else{
            requestPermissions(
                    new String[]{Manifest.permission.CALL_PHONE},
                    200
            );
        }
    }

    private void updateIncome(String date) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("income")
                .where(
                        Filter.and(
                                Filter.equalTo("mechanic_id", mechanic_id),
                                Filter.equalTo("date",date )
                        )
                ).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            Log.d("FixItLog", "TaskDetailsActivity: update income search success");

                            List<DocumentSnapshot> docList = task.getResult().getDocuments();

                            if (!docList.isEmpty()) {

                                Log.d("FixItLog", "TaskDetailsActivity: update income doc is not empty");

                                if (docList.size() == 1) {

                                    Log.d("FixItLog", "TaskDetailsActivity: income already exists");

                                    DocumentSnapshot doc = docList.get(0);

                                    double existingIncome = doc.getDouble("income");
                                    double newIncome = existingIncome + amount;

                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("income", newIncome);

                                    firestore.collection("income").document(doc.getId())
                                            .update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("FixItLog", "TaskDetailsActivity: update income - update success");

                                                    Intent intent = new Intent(TaskDetailsActivity.this,HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.putExtra("TaskHistory","");
                                                    startActivity(intent);

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("FixItLog", "TaskDetailsActivity: update income - update failure");
                                                }
                                            });

                                }

                            } else {

                                Log.d("FixItLog", "TaskDetailsActivity: update income update income doc is empty");

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("income", amount);
                                map.put("mechanic_id", mechanic_id);
                                map.put("date", date);

                                firestore.collection("income")
                                        .add(map)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("FixItLog", "TaskDetailsActivity: update income - insert success");

                                                Intent intent = new Intent(TaskDetailsActivity.this,HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.putExtra("TaskHistory","");
                                                startActivity(intent);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("FixItLog", "TaskDetailsActivity: update income - insert failure");
                                            }
                                        });
                            }
                        }else{
                            Log.d("FixItLog", "TaskDetailsActivity: update income - task is unsuccessful");

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "TaskDetailsActivity: update income - search failure");
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==200){

            if(grantResults[0]==PackageManager.PERMISSION_DENIED){

                MyAlert.getMyAlert().showOkAlert(
                        TaskDetailsActivity.this,
                        "Permission Required for Calls",
                        "Calling permission is necessary to make phone calls from the app. Without it, some features may not work properly. Please enable the permission in settings to continue.",
                        R.drawable.alert_warning
                );

            }

        }
    }
}