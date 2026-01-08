package com.dilanhansaja.fixit;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.MyNotification;
import com.dilanhansaja.fixit.model.MySharedPreference;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;


public class RequestDetailsActivity extends AppCompatActivity {
    private String request_id;

    private int PAYHERE_REQUEST = 11001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.req_details_scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        request_id = intent.getStringExtra("mechanic_request_id");

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
                                    Log.d("FixItLog", "RequestDetailsActivity: document details received");
                                } else {
                                    Log.d("FixItLog", "RequestDetailsActivity: document not exists");
                                }
                            } else {
                                Log.d("FixItLog", "RequestDetailsActivity: document result is null");
                            }
                        }
                    });
        } else {
            Log.d("FixItLog", "RequestDetailsActivity: Status or request id not found : request id=" + request_id);
        }
    }

    private double payableAmount = 0;
    private String mechanic_id;

    private void loadDetails(DocumentSnapshot doc) {

        TextView mechanicNameView = findViewById(R.id.req_details_mechanic_name_textView);
        TextView rateTextView = findViewById(R.id.req_details_rate);
        TextView statusTextView = findViewById(R.id.req_details_status_textView);
        ImageView vehicleTypeImageView = findViewById(R.id.req_details_vehicleTypeImageView);
        ImageView locationImageView = findViewById(R.id.req_details_locationImageView);
        EditText notesEditText = findViewById(R.id.req_details_notesEditText);
        EditText codeEditText = findViewById(R.id.req_details_requestCodeEditText);
        TextView dateTextView = findViewById(R.id.req_details_DateTextView);
        TextView startAtTextView = findViewById(R.id.req_details_startAtTextView);
        TextView endAtTextView = findViewById(R.id.req_details_endAtTextView);
        TextView timeSpentTextView = findViewById(R.id.req_details_timeSpent);
        TextView totalTextView = findViewById(R.id.req_details_totalTextView);

        String vehicle_type = doc.getString("vehicle_type");
        mechanic_id = doc.getString("mechanic_id");

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

        mechanicNameView.setText(doc.getString("mechanic_name"));
        String rate = String.valueOf(doc.getDouble("rate")) + "/h";
        rateTextView.setText(rate);
        statusTextView.setText(doc.getString("status"));

        locationImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String lat = String.valueOf(doc.getDouble("user_lat"));
                String lng = String.valueOf(doc.getDouble("user_lng"));

                String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                view.getContext().startActivity(intent);
            }
        });

        if (doc.contains("notes")) {
            notesEditText.setText(doc.getString("notes"));
        }

        codeEditText.setText(doc.getString("request_code"));

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

                DecimalFormat df = new DecimalFormat("0.00");

                double totalCost;

                if (totalHours <= 1) {
                    totalCost = doc.getDouble("rate");
                } else {
                    totalCost = totalHours * doc.getDouble("rate");
                }

                payableAmount = Double.parseDouble(df.format(totalCost));

                String total = "LKR " + String.valueOf(df.format(totalCost));
                totalTextView.setText(total);

            } catch (ParseException e) {
                Log.d("FixItLog", "RequestDetailsActivity: Exception =" + e);
            }
        }

        dateTextView.setText(requestDateArray[0]);

        checkRequestStatus(doc);

    }

    private void checkRequestStatus(DocumentSnapshot doc) {

        Button payNowBtn = findViewById(R.id.req_details_paynowBtn);

        if (doc.getString("status").equals("Completed")) {

            if (HomeActivity.isNotiAllowed) {
                MyNotification.getMyNotification().sendNotification(
                        RequestDetailsActivity.this,
                        "Task Completed – Payment Required",
                        "The mechanic has completed the service.",
                        R.drawable.notification
                );
            }

            if (doc.getString("payment_method").equals("Card")) {

                payNowBtn.setText("Pay Now");
                payNowBtn.setVisibility(View.VISIBLE);

                payNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (payableAmount > 0) {
                            makePayment();
                        }

                    }
                });
            }else{
                payNowBtn.setText("Please HandOver Cash to Mechanic");
                payNowBtn.setVisibility(View.VISIBLE);

            }

        }else if (doc.getString("status").equals("Paid")) {

            payNowBtn.setVisibility(View.GONE);

        }else if (doc.getString("status").equals("Approved")) {

            payNowBtn.setVisibility(View.GONE);

        }
    }

    private void makePayment() {

        Gson gson = new Gson();
        MySharedPreference sharedPreference = MySharedPreference.getMySharedPreference();
        String userJson = sharedPreference.get(RequestDetailsActivity.this, "object");

        if (userJson != null) {

            JsonObject userObject = gson.fromJson(userJson, JsonObject.class);

            InitRequest req = new InitRequest();
            req.setMerchantId(BuildConfig.PAYHERE_ID);       // Merchant ID
            req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
            req.setAmount(payableAmount);       // Final Amount to be charged
            req.setOrderId(request_id);        // Unique Reference ID
            req.setItemsDescription("Door bell wireless");  // Item description title
            req.setCustom1("This is the custom message 1");
            req.setCustom2("This is the custom message 2");
            req.getCustomer().setFirstName(userObject.get("fname").getAsString());
            req.getCustomer().setLastName(userObject.get("lname").getAsString());
            req.getCustomer().setEmail(userObject.get("email").getAsString());
            req.getCustomer().setPhone("+94771234567");
            req.getCustomer().getAddress().setAddress("none");
            req.getCustomer().getAddress().setCity("none");
            req.getCustomer().getAddress().setCountry("Sri Lanka");

            //Optional Params
            req.setNotifyUrl("");           // Notifiy Url
            req.getCustomer().getDeliveryAddress().setAddress("none");
            req.getCustomer().getDeliveryAddress().setCity("none");
            req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
            req.getItems().add(new Item(null, "none", 1, payableAmount));

            Intent intent = new Intent(this, PHMainActivity.class);
            intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
            startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"

        } else {
            Log.d("FixItLog", "RequestDetailsActivity: User not found in shared preferences");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;

                if (response != null) {
                    if (response.isSuccess()) {

                        Log.d("FIxItLog", "RequestDetailsActivity: " + response.getData().toString());

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String date = format.format(new Date());

                        updateTask(date);

                    } else {
                        Log.d("FIxItLog", "RequestDetailsActivity: " + response.toString());
                    }
                } else {
                    msg = "Result: no response";
                    Log.d("FixItLog", msg);
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    Log.d("FixItLog", "RequestDetailsActivity: " + response.toString());
                } else {
                    Log.d("FixItLog", "RequestDetailsActivity: User canceled the request");
                }
            }
        }
    }

    private void updateTask(String date) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", "Paid");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("mechanic_request").document(request_id)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        updateIncome(date);

                        View alertView = MyAlert.getMyAlert().showOkBtnAlert(
                                RequestDetailsActivity.this,
                                "Payment Successful",
                                "Your payment has been processed successfully. Thank you for your payment!",
                                R.drawable.alert_success
                        );
                        AlertDialog alertDialog = MyAlert.getMyAlert().getDialog(alertView, RequestDetailsActivity.this);

                        Button okBtn = alertView.findViewById(R.id.ok_alert_btn);
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog.dismiss();
                                Intent intent = new Intent(RequestDetailsActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("RequestHistory","");
                                startActivity(intent);
                            }
                        });

                        alertDialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "RequestDetailsActivity: update failure");
                    }
                });

    }

    private void updateIncome(String date) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("income")
                .where(
                        Filter.and(
                                Filter.equalTo("mechanic_id", mechanic_id),
                                Filter.equalTo("date", date)
                        )
                ).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            List<DocumentSnapshot> docList = task.getResult().getDocuments();

                            if (!docList.isEmpty()) {

                                if (docList.size() == 1) {

                                    DocumentSnapshot doc = docList.get(0);

                                    double existingIncome = doc.getDouble("income");
                                    double newIncome = existingIncome + payableAmount;

                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("income", newIncome);

                                    firestore.collection("income").document(doc.getId())
                                            .update(map)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("FixItLog", "RequestDetailsActivity: update income - update success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("FixItLog", "RequestDetailsActivity: update income - update failure");
                                                }
                                            });

                                }
                            } else {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("income", payableAmount);
                                map.put("mechanic_id", mechanic_id);
                                map.put("date", date);

                                firestore.collection("income")
                                        .add(map)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("FixItLog", "RequestDetailsActivity: update income - insert success");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("FixItLog", "RequestDetailsActivity: update income - insert failure");
                                            }
                                        });
                            }
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "RequestDetailsActivity: update income - search failure");
                    }
                });

    }
}