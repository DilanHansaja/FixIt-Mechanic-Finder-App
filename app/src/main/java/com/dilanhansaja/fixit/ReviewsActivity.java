package com.dilanhansaja.fixit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.dilanhansaja.fixit.model.MyAlert;

public class ReviewsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String mechanic_id = intent.getStringExtra("mechanic_id");
        String user_name = intent.getStringExtra("user_name");
        String request_id = intent.getStringExtra("request_id");

        Button button = findViewById(R.id.reviews_rateNow_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReview(mechanic_id,user_name,request_id);
            }
        });

    }

    private void addReview(String mechanic_id, String user_name,String request_id) {

        EditText feedback = findViewById(R.id.reviews_feedback_edittext);
        RatingBar ratingBar = findViewById(R.id.reviews_rating_bar);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        if (ratingBar.getRating() < 1) {
            showOkAlert("Rating required", "Please rate at least 1 star.");
        } else if (String.valueOf(feedback.getText()).isBlank()) {
            showOkAlert("Feedback required", "Feedback can't be empty.");

        } else {

            HashMap<String, Object> map = new HashMap<>();
            map.put("mechanic_id", mechanic_id);
            map.put("user_name", user_name);
            map.put("rating", ratingBar.getRating());
            map.put("feedback", String.valueOf(feedback.getText()));
            map.put("date", format.format(new Date()));

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("reviews")
                    .add(map)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("FixItLog", "ReviewsActivity: Review insert success");

                            MyAlert myAlert = MyAlert.getMyAlert();
                            View alertView = myAlert.showOkBtnAlert(
                                    ReviewsActivity.this,
                                    "Review Submitted",
                                    "Thank you for your feedback! Your review has been submitted successfully and will help others choose reliable mechanics.",
                                    R.drawable.alert_success
                            );

                            AlertDialog alertDialog = myAlert.getDialog(alertView,ReviewsActivity.this);

                            Button okBtn= alertView.findViewById(R.id.ok_alert_btn);
                            okBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                    updateRequestStatus(request_id);
                                    finish();
                                }
                            });

                            alertDialog.show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FixItLog", "ReviewsActivity: Review insert failure");
                        }
                    });

        }
    }

    private void updateRequestStatus(String request_id){

        HashMap<String,Object> map =new HashMap<>();
        map.put("status","Rated");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("mechanic_request")
                .document(request_id)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("FixItLog", "ReviewsActivity: Update status success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "ReviewsActivity: Update status failure");
                    }
                });
    }

    private void showOkAlert(String title, String msg) {

        MyAlert myAlert = MyAlert.getMyAlert();
        myAlert.showOkAlert(ReviewsActivity.this, title, msg, R.drawable.alert_warning);

    }
}