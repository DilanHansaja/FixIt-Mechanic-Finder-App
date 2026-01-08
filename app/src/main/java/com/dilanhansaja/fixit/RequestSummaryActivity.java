package com.dilanhansaja.fixit;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.SQLiteHelper;
import com.dilanhansaja.fixit.model.Validation;

public class RequestSummaryActivity extends AppCompatActivity {

    private JsonObject details;
    private MyAlert myAlert = MyAlert.getMyAlert();
    private boolean isMobileSaved;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_summary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reqSumScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String str_details = intent.getStringExtra("details");
        details = gson.fromJson(str_details, JsonObject.class);

        loadRequestSummary();

        Button button = findViewById(R.id.req_sum_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
        searchMobile();

    }

    private void loadRequestSummary() {

        TextView mechanic_name_textView = findViewById(R.id.req_sum_mechanic_name);
        TextView rate_textView = findViewById(R.id.req_sum_rate);
        TextView vehicle_type_textView = findViewById(R.id.req_sum_vehicle_type);

        String fullName = details.get("mechanic_fname").getAsString() + " " + details.get("mechanic_lname").getAsString();

        mechanic_name_textView.setText(fullName);
        rate_textView.setText(details.get("mechanic_rate").getAsString());
        vehicle_type_textView.setText(details.get("vehicle_type").getAsString());


        loadMap(details.get("user_lat").getAsString(), details.get("user_lng").getAsString());

    }

    private void loadMap(String lat, String lng) {

        SupportMapFragment mapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.reqSummaryMapFrameLayout, mapFragment);
        fragmentTransaction.commit();

        if (lat != null && lng != null) {

            LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {

                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    googleMap.getUiSettings().setCompassEnabled(true);

                    googleMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder()
                                            .target(latLng)
                                            .zoom(18)
                                            .build()
                            )
                    );

                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(latLng)
                                    .title("Your location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))

                    );
                }
            });

        } else {
            Log.d("FixItLog", "User location data not received");
        }


    }

    private String payment_method = "Card";

    private void sendRequest() {

        RadioButton cashBtn = findViewById(R.id.req_sum_cash_radioButton);

        if (cashBtn.isChecked()) {
            payment_method = "Cash";
        }

        EditText mobileEditText = findViewById(R.id.req_sum_mobile);

        if (Validation.getValidation().isEditTextEmpty(mobileEditText, RequestSummaryActivity.this, "Please enter your mobile")) {

        } else if (!Validation.getValidation().isMobileNumberValid(mobileEditText.getText().toString())) {
            Toast.makeText(RequestSummaryActivity.this, "Invalid mobile number", Toast.LENGTH_SHORT).show();

        } else {

            View alertView = myAlert.getConfirmationAlert(
                    RequestSummaryActivity.this,
                    "Confirm Mechanic Request",
                    "Are you sure you want to send this request to the mechanic?"
            );

            AlertDialog alertDialog = myAlert.getDialog(alertView, RequestSummaryActivity.this);

            Button confirmBtn = alertView.findViewById(R.id.alert_right_button);
            Button cancelBtn = alertView.findViewById(R.id.alert_left_button);

            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                    String datetime = simpleDateFormat.format(new Date());

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("mechanic_id", details.get("mechanic_id").getAsString());
                    hashMap.put("mechanic_name", details.get("mechanic_fname").getAsString() + " " + details.get("mechanic_lname").getAsString());
                    hashMap.put("user_name", details.get("user_name").getAsString());
                    hashMap.put("user_id", details.get("user_id").getAsString());
                    hashMap.put("vehicle_type", details.get("vehicle_type").getAsString());
                    hashMap.put("rate", details.get("mechanic_rate").getAsDouble());
                    hashMap.put("start_at", "");
                    hashMap.put("end_at", "");
                    hashMap.put("status", "Pending");
                    hashMap.put("request_code", "");
                    hashMap.put("user_mobile", mobileEditText.getText().toString());
                    hashMap.put("user_lat", details.get("user_lat").getAsDouble());
                    hashMap.put("user_lng", details.get("user_lng").getAsDouble());
                    hashMap.put("payment_method", payment_method);
                    hashMap.put("request_made_on", datetime);


                    if (details.has("notes")) {
                        hashMap.put("notes", details.get("notes").getAsString());
                    }

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection("mechanic_request").add(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    View alertView = myAlert.showOkBtnAlert(
                                            RequestSummaryActivity.this,
                                            "Request Sent Successfully",
                                            "Your mechanic request has been sent successfully. You will be notified once the mechanic responds",
                                            R.drawable.alert_success
                                    );

                                    AlertDialog alertDialog = myAlert.getDialog(alertView, RequestSummaryActivity.this);

                                    Button okBtn = alertView.findViewById(R.id.ok_alert_btn);

                                    okBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            alertDialog.dismiss();

                                            if (!isMobileSaved) {
                                                saveMobile(mobileEditText.getText().toString());
                                            }else{
                                                moveToBack();
                                            }
                                        }
                                    });
                                    alertDialog.show();


                                    Log.d("FixItLog", "RequestSummaryActivity: send mechanic request success");

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    myAlert.showOkAlert(
                                            RequestSummaryActivity.this,
                                            "Request Failed",
                                            "Failed to send the mechanic request. Please try again later.",
                                            R.drawable.alert_incorrect
                                    );

                                    Log.d("FixItLog", "RequestSummaryActivity: send mechanic request failure");
                                }
                            });

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
    }

    private void saveMobile(String number) {

        MyAlert myAlert = MyAlert.getMyAlert();

        View alertView = myAlert.getConfirmationAlert(
                RequestSummaryActivity.this,
                "Save Your Number?",
                "Would you like to save your phone number for next time?"
        );

        AlertDialog alertDialog = myAlert.getDialog(alertView, RequestSummaryActivity.this);

        Button confirmBtn = alertView.findViewById(R.id.alert_right_button);
        Button cancelBtn = alertView.findViewById(R.id.alert_left_button);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                SQLiteHelper sqLiteHelper = new SQLiteHelper(
                        RequestSummaryActivity.this,
                        "fixIt.db",
                        null,
                        1
                );

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("number", number);

                        long id = sqLiteDatabase.insert("mobile", null, contentValues);
                        Log.d("FixItLog", "Sqlite inserted id= " + id);

                    }
                }).start();

                moveToBack();


            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                moveToBack();
            }
        });
        alertDialog.show();

    }

    private void searchMobile() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteHelper sqLiteHelper = new SQLiteHelper(
                        RequestSummaryActivity.this,
                        "fixIt.db",
                        null,
                        1
                );

                SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
                String projection[] = new String[]{"number"};

                Cursor cursor = sqLiteDatabase.query(
                        "mobile",
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                String mobile = null;

                if (cursor.moveToNext()) {
                    mobile = cursor.getString(0);
                }

                if (mobile != null) {
                    String finalMobile = mobile;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isMobileSaved = true;
                            EditText mobileEditText = findViewById(R.id.req_sum_mobile);
                            mobileEditText.setText(finalMobile);
                        }
                    });
                }
            }
        }).start();


    }

    private void moveToBack(){

        Intent intent = new Intent(RequestSummaryActivity.this,HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("MyRequests","");
        startActivity(intent);

    }
}