package com.dilanhansaja.fixit.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.MySharedPreference;


public class UserProfileFragment extends Fragment {

    private View inflatedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        Button user_profile_updateBtn = inflatedView.findViewById(R.id.user_profile_updateBtn);
        user_profile_updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        loadProfile(inflatedView);

        return inflatedView;
    }

    private String currentFname;
    private String currentLname;
    private double currentRate;

    private String type;

    private void loadProfile(View inflatedView) {

        Gson gson = new Gson();

        TextView user_profile_name = inflatedView.findViewById(R.id.user_profile_name);
        TextView user_profile_email = inflatedView.findViewById(R.id.user_profile_email);
        EditText user_profile_fname = inflatedView.findViewById(R.id.user_profile_fname);
        EditText user_profile_lname = inflatedView.findViewById(R.id.user_profile_lname);
        EditText user_profile_emailEditText = inflatedView.findViewById(R.id.user_profile_emailEditText);
        EditText user_profile_regDate = inflatedView.findViewById(R.id.user_profile_regDate);

        String userJson = MySharedPreference.getMySharedPreference().get(inflatedView.getContext(), "object");
        type = MySharedPreference.getMySharedPreference().get(inflatedView.getContext(), "type");

        if (userJson != null && type != null) {

            JsonObject jsonObject = gson.fromJson(userJson, JsonObject.class);

            currentFname = jsonObject.get("fname").getAsString();
            currentLname = jsonObject.get("lname").getAsString();

            String name = jsonObject.get("fname").getAsString() + " " + jsonObject.get("lname").getAsString();

            user_profile_name.setText(name);
            user_profile_email.setText(jsonObject.get("email").getAsString());
            user_profile_fname.setText(jsonObject.get("fname").getAsString());
            user_profile_lname.setText(jsonObject.get("lname").getAsString());
            user_profile_emailEditText.setText(jsonObject.get("email").getAsString());
            user_profile_regDate.setText(jsonObject.get("registered_date").getAsString());

            if (type.equals("mechanic")) {

                TextInputLayout user_profile_hourly_rate_layout = inflatedView.findViewById(R.id.user_profile_hourly_rate_layout);
                TextView user_profile_rateEditText = inflatedView.findViewById(R.id.user_profile_rateEditText);
                user_profile_hourly_rate_layout.setVisibility(View.VISIBLE);
                user_profile_rateEditText.setText(String.valueOf(jsonObject.get("rate").getAsDouble()));

                currentRate = jsonObject.get("rate").getAsDouble();

            }

        } else {
            Log.d("FixItLog", "UserProfileFragment: userJson or type was null");
        }


    }

    private void updateProfile() {

        String document_id = MySharedPreference.getMySharedPreference().get(inflatedView.getContext(), "documentId");

        if (type != null && document_id != null) {

            EditText user_profile_fname = inflatedView.findViewById(R.id.user_profile_fname);
            EditText user_profile_lname = inflatedView.findViewById(R.id.user_profile_lname);
            EditText user_profile_rateEditText = inflatedView.findViewById(R.id.user_profile_rateEditText);

            HashMap<String, Object> map = new HashMap<>();

            if (String.valueOf(user_profile_fname.getText()).isBlank()) {

                showAlert("Enter First name", "Please enter your first name",R.drawable.alert_warning);

            } else if (String.valueOf(user_profile_lname.getText()).isBlank()) {

                showAlert("Enter Last name", "Please enter your last name",R.drawable.alert_warning);

            } else if (
                    !String.valueOf(user_profile_fname.getText()).equals(currentFname)
                            ||
                            !String.valueOf(user_profile_lname.getText()).equals(currentLname)
            ) {

                //update fname and lname
                map.put("fname", String.valueOf(user_profile_fname.getText()));
                map.put("lname", String.valueOf(user_profile_lname.getText()));

                if (type.equals("mechanic")) {

                    if (String.valueOf(user_profile_rateEditText.getText()).isBlank()) {
                        showAlert("Enter your rate", "Please enter your hourly rate. (Min LKR 250.00)",R.drawable.alert_warning);
                    } else if (Double.parseDouble(String.valueOf(user_profile_rateEditText.getText())) < 250) {
                        showAlert("Less than minimum rate", "Minimum hourly rate is LKR 250.00",R.drawable.alert_warning);
                    } else {
                        //update mechanic rate
                        map.put("rate", Double.parseDouble(String.valueOf(user_profile_rateEditText.getText())));
                    }
                }

                updateDatabase(map,document_id);

            } else {

                if (type.equals("mechanic")) {

                    if (!String.valueOf(user_profile_rateEditText.getText()).equals(String.valueOf(currentRate))) {


                        if (String.valueOf(user_profile_rateEditText.getText()).isBlank()) {
                            showAlert("Enter your rate", "Please enter your hourly rate. (Min LKR 250.00)",R.drawable.alert_warning);
                        } else if (Double.parseDouble(String.valueOf(user_profile_rateEditText.getText())) < 250) {
                            showAlert("Less than minimum rate", "Minimum hourly rate is LKR 250.00",R.drawable.alert_warning);
                        } else {
                            //update mechanic rate
                            map.put("rate", Double.parseDouble(String.valueOf(user_profile_rateEditText.getText())));
                            updateDatabase(map,document_id);
                        }

                    } else {
                        //no changes
                        showAlert("No changes", "Please make changes to update",R.drawable.alert_warning);
                    }

                } else {
                    //no changes detected
                    showAlert("No changes", "Please make changes to update",R.drawable.alert_warning);
                }
            }


        } else {
            Log.d("FixItLog", "UserProfileFragment: Type is or document id is null");
        }
    }

    private void showAlert(String title, String msg,int icon) {

        MyAlert myAlert = MyAlert.getMyAlert();
        myAlert.showOkAlert(inflatedView.getContext(), title, msg, icon);
    }

    private void updateObject(DocumentSnapshot doc){

        Gson gson=new Gson();
        SharedPreferences sharedPreferences = inflatedView.getContext().getSharedPreferences("lk.javainstitute.fixit.data", Context.MODE_PRIVATE);

        String json = gson.toJson(doc.getData());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("object", json);
        editor.apply();

    }

    private void updateDatabase(HashMap<String,Object> map,String document_id){
        //update
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference reference = firestore.collection(type).document(document_id);

        reference.update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void unused) {

                        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        DocumentSnapshot documentSnapshot = task.getResult();

                                        updateObject(documentSnapshot);
                                        loadProfile(inflatedView);
                                        showAlert("Updated Successfully", "Your profile details updated successfully.",R.drawable.alert_success);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("FixItLog", "UserProfileFragment: search failure");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "UserProfileFragment: update failure");
                    }
                });
    }
}