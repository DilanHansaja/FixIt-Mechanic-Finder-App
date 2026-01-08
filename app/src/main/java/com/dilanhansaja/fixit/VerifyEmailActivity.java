package com.dilanhansaja.fixit;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import com.dilanhansaja.fixit.model.MyAlert;

public class VerifyEmailActivity extends AppCompatActivity {

    MyAlert myAlert = MyAlert.getMyAlert();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_email);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sendReqFrame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();

        EditText codeField = findViewById(R.id.verify_email_codeField);
        Button button = findViewById(R.id.verify_email_btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                separateUsers(intent, codeField.getText().toString());

            }
        });

    }

    private void separateUsers(Intent intent, String code) {

        if (intent.getStringExtra("type").equals("user")) {

            if (intent.getStringExtra("verification_code").equals(code)) {

                verifyEmail("user", intent.getStringExtra("id"), intent.getStringExtra("email"));

            } else {

                myAlert.showOkAlert(
                        VerifyEmailActivity.this,
                        "Incorrect Verification Code",
                        "The code you entered is incorrect. Please check your email and try again.",
                        R.drawable.alert_incorrect);

            }

        } else if (intent.getStringExtra("type").equals("mechanic")) {


            if (intent.getStringExtra("verification_code").equals(code)) {

                verifyEmail("mechanic", intent.getStringExtra("id"), intent.getStringExtra("email"));

            } else {

                myAlert.showOkAlert(
                        VerifyEmailActivity.this,
                        "Incorrect Verification Code",
                        "The code you entered is incorrect. Please check your email and try again.",
                        R.drawable.alert_incorrect);
            }
        }
    }

    private void verifyEmail(String type, String id, String email) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email_verification", "verified");

        firestore.collection(type).document(id).update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        View alertView = myAlert.showOkBtnAlert(
                                VerifyEmailActivity.this,
                                "Verification Successful",
                                "Your email has been successfully verified. You can now login to your account",
                                R.drawable.alert_success);

                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VerifyEmailActivity.this).setView(alertView);
                        AlertDialog alertDialog = alertBuilder.create();

                        Button okBtn = alertView.findViewById(R.id.ok_alert_btn);

                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                                intent.putExtra("type", type);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finishAffinity();
                            }
                        });

                        alertDialog.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "Something went wrong: VerifyEmailActivity");
                    }
                });

    }

}