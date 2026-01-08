package com.dilanhansaja.fixit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

import com.dilanhansaja.fixit.model.Validation;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String type;
    Validation validation = Validation.getValidation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.uta_constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button1 = findViewById(R.id.login_btn1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        TextView loginBottomText = findViewById(R.id.login_bottomText);
        loginBottomText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        type = intent.getStringExtra("type");

        TextView title = findViewById(R.id.la_titleText);

        if (type.equals("user")) {
            title.setText(R.string.app_text1);

        } else {
            title.setText(R.string.app_text2);
        }

        if (intent.hasExtra("email")) {
            EditText editTextEmail = findViewById(R.id.login_email);
            editTextEmail.setText(intent.getStringExtra("email"));

            EditText editTextPassword = findViewById(R.id.login_password);
            editTextPassword.requestFocus();
        }

    }

    private void login() {

        Gson gson = new Gson();

        EditText editTextEmail = findViewById(R.id.login_email);
        EditText editTextPassword = findViewById(R.id.login_password);

        if (validation.isEditTextEmpty(editTextEmail, LoginActivity.this, "Please Enter Email")) {

        } else if (validation.isEditTextEmpty(editTextPassword, LoginActivity.this, "Please Enter Password")) {

        } else {

            if (type != null) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());

                firestore.collection(type).where(
                                Filter.and(
                                        Filter.equalTo("email", email),
                                        Filter.equalTo("password", password)
                                )
                        )
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                if (task.isSuccessful()) {

                                    QuerySnapshot querySnapshot = task.getResult();

                                    if (querySnapshot.size() == 1) {

                                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                                        Log.d("FixItLog", doc.getData().toString());

                                        if(doc.getString("email_verification").equals("verified")){

                                            SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.fixit.data", Context.MODE_PRIVATE);

                                            String json = gson.toJson(doc.getData());

                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("type", type);
                                            editor.putString("object", json);
                                            editor.putString("documentId",doc.getId());
                                            editor.apply();

                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finishAffinity();

                                        }else{

                                            JsonObject jsonObject = new JsonObject();
                                            jsonObject.addProperty("email", doc.getString("email"));
                                            jsonObject.addProperty("fname", doc.getString("fname"));
                                            jsonObject.addProperty("type", type);
                                            jsonObject.addProperty("id",doc.getId());

                                            sendVerificationCode(jsonObject,new Gson(),firestore);

                                        }


                                    } else {
                                        Toast.makeText(LoginActivity.this, "Invalid Credentials!", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    Toast.makeText(LoginActivity.this, "Please try again later!", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                            }
                        });
            }

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

                                updateCode(
                                        firestore,
                                        jsonObject.get("type").getAsString(),
                                        jsonObject.get("email").getAsString(),
                                        jsonObject.get("id").getAsString(),
                                        responseObject.get("code").getAsString()
                                );

                        } else {
                            Log.d("FixItLog", "Verification code not found in response : LoginActivity");
                        }

                    } else {
                        Log.d("FixItLog", "Send email response failed : LoginActivity");
                    }

                } catch (Exception e) {
                    Log.i("FixItLog", e.toString());
                }

            }
        }).start();
    }
    private void updateCode(FirebaseFirestore firestore,String type,String email, String id,String verification_code){

        HashMap<String, Object> map = new HashMap<>();
        map.put("email_verification",verification_code);

        firestore.collection(type)
                .document(id)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("email", email);
                        intent.putExtra("verification_code", verification_code);
                        intent.putExtra("id", id);
                        startActivity(intent);

                        Log.d("FixItLog", "Code updated and moved to verify email.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog",e.toString());
                        Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                });
    }
}