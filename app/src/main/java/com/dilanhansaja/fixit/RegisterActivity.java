package com.dilanhansaja.fixit;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.dilanhansaja.fixit.model.Mechanic;
import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.User;
import com.dilanhansaja.fixit.model.Validation;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    Validation validation = Validation.getValidation();
    MyAlert myAlert = MyAlert.getMyAlert();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button registerButton = findViewById(R.id.reg_RegisterBtn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        TextView textView = findViewById(R.id.reg_bottom_text);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, UserTypeActivity.class);
                startActivity(intent);
            }
        });
    }

    String type;

    private void register() {

        type = "user";

        EditText fnameEditText = findViewById(R.id.reg_fnameEdittext);
        EditText lnameEditText = findViewById(R.id.reg_lnameEdittext);
        EditText emailEditText = findViewById(R.id.reg_emailEdittext);
        EditText passwordEditText = findViewById(R.id.reg_passwordEdittext);
        EditText conPasswordEditText = findViewById(R.id.reg_conPasswordEdittext);
        RadioButton mechanicRadioButton = findViewById(R.id.reg_mechanicRadioBtn);

        if (mechanicRadioButton.isChecked()) {
            this.type = "mechanic";
        }

        if (validation.isEditTextEmpty(fnameEditText, RegisterActivity.this, "Please enter first name.")) {

        } else if (validation.isEditTextEmpty(lnameEditText, RegisterActivity.this, "Please enter last name.")) {

        } else if (validation.isEditTextEmpty(emailEditText, RegisterActivity.this, "Please enter email.")) {

        } else if (!validation.isEmailValid(emailEditText.getText().toString(), RegisterActivity.this, "invalid email")) {

        } else if (validation.isEditTextEmpty(passwordEditText, RegisterActivity.this, "Please enter password")) {

        } else if (!validation.isPasswordValid(passwordEditText.getText().toString(), RegisterActivity.this, "Password must contain at least one lowercase letter, one uppercase letter, one number, one special character, and be at least 8 characters long.")) {

        } else if (validation.isEditTextEmpty(conPasswordEditText, RegisterActivity.this, "Please confirm your password")) {

        } else if (!passwordEditText.getText().toString().equals(conPasswordEditText.getText().toString())) {
            Toast.makeText(RegisterActivity.this, "Passwords not matched.", Toast.LENGTH_LONG).show();

        } else {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            Gson gson = new Gson();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

            View confirmationView = myAlert.getConfirmationAlert(
                    RegisterActivity.this,
                    "Are you sure, type is " + type + "?",
                    "You selected " + type + " as your registration type. Please confirm if this is correct");

            Button confirmBtn = confirmationView.findViewById(R.id.alert_right_button);
            Button cancelBtn = confirmationView.findViewById(R.id.alert_left_button);

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(RegisterActivity.this).setView(confirmationView);
            AlertDialog confirmDialog = alertBuilder.create();

            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    confirmDialog.dismiss();

                    //selected type is mechanic
                    if (type.equals("mechanic")) {

                        //check if mechanic email is already registered or not

                        firestore.collection("mechanic")
                                .whereEqualTo("email", emailEditText.getText().toString())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        if (task.isSuccessful()) {

                                            List<DocumentSnapshot> docList = task.getResult().getDocuments();

                                            if (docList.isEmpty()) {
                                                //Email is available to register

                                                Mechanic mechanic = new Mechanic(
                                                        fnameEditText.getText().toString(),
                                                        lnameEditText.getText().toString(),
                                                        emailEditText.getText().toString(),
                                                        passwordEditText.getText().toString(),
                                                        Double.parseDouble("250"),
                                                        simpleDateFormat.format(new Date()),
                                                        "unverified",
                                                        "unverified",
                                                        "0",
                                                        0,
                                                        0
                                                );

                                                JsonObject jsonObject = new JsonObject();
                                                jsonObject.addProperty("email", emailEditText.getText().toString());
                                                jsonObject.addProperty("fname", fnameEditText.getText().toString());
                                                jsonObject.addProperty("type", type);
                                                jsonObject.add("mechanic", gson.toJsonTree(mechanic));

                                                sendVerificationCode(jsonObject, gson, firestore);

                                            } else {
                                                //email already exists

                                                myAlert.showOkAlert(
                                                        RegisterActivity.this,
                                                        "Warning", "This email is already in use. Try another one or log in to your account.",
                                                        R.drawable.alert_warning);
                                            }

                                        } else {
                                            Log.e("FixItLog", "Check mechanic email before register: Failed.");
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                                    }
                                });

                        //selected type is user
                    } else {

                        //check if user email is already registered or not

                        firestore.collection("user")
                                .whereEqualTo("email", emailEditText.getText().toString())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                        if (task.isSuccessful()) {

                                            List<DocumentSnapshot> docList = task.getResult().getDocuments();

                                            if (docList.isEmpty()) {
                                                //Email is available to register

                                                User user = new User(
                                                        fnameEditText.getText().toString(),
                                                        lnameEditText.getText().toString(),
                                                        emailEditText.getText().toString(),
                                                        passwordEditText.getText().toString(),
                                                        simpleDateFormat.format(new Date()),
                                                        "unverified",
                                                        "0",
                                                        0,
                                                        0
                                                );

                                                JsonObject jsonObject = new JsonObject();
                                                jsonObject.addProperty("email", emailEditText.getText().toString());
                                                jsonObject.addProperty("fname", fnameEditText.getText().toString());
                                                jsonObject.addProperty("type", type);
                                                jsonObject.add("user", gson.toJsonTree(user));

                                                sendVerificationCode(jsonObject, gson, firestore);

                                            } else {

                                                //email already exists

                                                myAlert.showOkAlert(
                                                        RegisterActivity.this,
                                                        "Warning", "This email is already in use. Try another one or log in to your account.",
                                                        R.drawable.alert_warning);

                                            }

                                        } else {
                                            Log.e("FixItLog", "Check user email before register: Failed.");
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDialog.dismiss();
                }
            });

            confirmDialog.show();

        }

    }

    private void sendVerificationCode(JsonObject jsonObject, Gson gson, FirebaseFirestore firestore) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                JsonObject requestObject = new JsonObject();
                requestObject.addProperty("email", jsonObject.get("email").getAsString());
                requestObject.addProperty("type", jsonObject.get("type").getAsString());
                requestObject.addProperty("name", jsonObject.get("fname").getAsString());

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(gson.toJson(requestObject), MediaType.get("text/plain"));

                Request request = new Request.Builder()
                        .url("http://192.168.8.105:8080/FixItWeb/SendEmail")
                        .post(requestBody)
                        .build();

                try {

                    Response response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {

                        String responseText = response.body().string();
                        JsonObject responseObject = gson.fromJson(responseText, JsonObject.class);

                        if (!responseObject.get("code").getAsString().isEmpty()) {

                            if (jsonObject.get("type").getAsString().equals("user")) {

                                JsonObject userObject = gson.fromJson(jsonObject.get("user"), JsonObject.class);

                                HashMap<String,Object> userMap = new HashMap();

                                userMap.put("fname",userObject.get("fname").getAsString());
                                userMap.put("lname",userObject.get("lname").getAsString());
                                userMap.put("email",userObject.get("email").getAsString());
                                userMap.put("email_verification",responseObject.get("code").getAsString());
                                userMap.put("geohash","0");
                                userMap.put("lat",0);
                                userMap.put("lng",0);
                                userMap.put("password",userObject.get("password").getAsString());
                                userMap.put("registered_date",userObject.get("registered_date").getAsString());

                                insertUserToFirestore(firestore, userMap, gson);

                            } else if (jsonObject.get("type").getAsString().equals("mechanic")) {

                                JsonObject mechanicObject = gson.fromJson(jsonObject.get("mechanic"), JsonObject.class);

                                HashMap<String,Object> mechanicMap = new HashMap();

                                mechanicMap.put("fname",mechanicObject.get("fname").getAsString());
                                mechanicMap.put("lname",mechanicObject.get("lname").getAsString());
                                mechanicMap.put("email",mechanicObject.get("email").getAsString());
                                mechanicMap.put("email_verification",responseObject.get("code").getAsString());
                                mechanicMap.put("account_verification",mechanicObject.get("account_verification").getAsString());
                                mechanicMap.put("rate",250);
                                mechanicMap.put("geohash","0");
                                mechanicMap.put("lat",0);
                                mechanicMap.put("lng",0);
                                mechanicMap.put("password",mechanicObject.get("password").getAsString());
                                mechanicMap.put("registered_date",mechanicObject.get("registered_date").getAsString());

                                insertMechanicToFirestore(firestore, mechanicMap, gson);

                            }

                        } else {
                            Log.d("FixItLog", "Verification code not found in response");
                        }

                    } else {
                        Log.d("FixItLog", "Send email response failed.");
                    }

                } catch (Exception e) {
                    Log.i("FixItLog", e.toString());
                }

            }
        }).start();
    }

    private void insertUserToFirestore(FirebaseFirestore firestore, HashMap<String,Object> userMap, Gson gson) {

        firestore.collection(type).add(userMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("type", "user");
                        intent.putExtra("email", String.valueOf(userMap.get("email")));
                        intent.putExtra("verification_code", String.valueOf(userMap.get("email_verification")));
                        intent.putExtra("id", documentReference.getId());
                        startActivity(intent);

                        Log.d("FixItLog", "User Registered and moved to verify email.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void insertMechanicToFirestore(FirebaseFirestore firestore, HashMap<String,Object> mechanicMap, Gson gson) {

        firestore.collection("mechanic").add(mechanicMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("type", "mechanic");
                        intent.putExtra("email", String.valueOf(mechanicMap.get("email")));
                        intent.putExtra("verification_code", String.valueOf(mechanicMap.get("email_verification")));
                        intent.putExtra("id", documentReference.getId());
                        startActivity(intent);

                        Log.d("FixItLog", "Mechanic Registered and  moved to verify email.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_LONG).show();
                    }
                });
    }


}