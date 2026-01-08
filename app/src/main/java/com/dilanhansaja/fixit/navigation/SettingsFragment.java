package com.dilanhansaja.fixit.navigation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.MySharedPreference;

public class SettingsFragment extends Fragment {

    View inflatedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inflatedView = inflater.inflate(R.layout.fragment_settings, container, false);

        checkMechanicAccountStatus();

        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button uploadBtn = getView().findViewById(R.id.settings_kyc_uploadBtn);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFiles();
            }
        });

        Button selfieBtn = getView().findViewById(R.id.settings_selfieBtn);
        selfieBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, 100);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

            }
        });

        Button nicFrontBtn = getView().findViewById(R.id.settings_nicFrontBtn);
        nicFrontBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, 200);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

            }
        });

        Button nicBackBtn = getView().findViewById(R.id.settings_nicBackBtn);
        nicBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    startActivityForResult(takePictureIntent, 300);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }

            }
        });
    }

    File selfie;
    File nicFront;
    File nicBack;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            if (data != null && data.getExtras() != null) {

                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                if (imageBitmap != null) {

                    Log.d("FixItLog", "Selfie captured successfully");

                    selfie = saveBitmapToFile(imageBitmap, "selfie.png");

                    TextView textView = getView().findViewById(R.id.settings_selfieTextView);
                    textView.setText("selfie.png");

                    ImageView selfie = getView().findViewById(R.id.selfieImageView);
                    selfie.setImageBitmap(imageBitmap);

                } else {
                    Log.e("FixItLog", "Captured image is null");
                }
            } else {
                Log.e("FixItLog", "Intent data is null");
            }
        }

        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {

            if (data != null && data.getExtras() != null) {

                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                if (imageBitmap != null) {

                    Log.d("FixItLog", "Nic Front captured successfully");

                    nicFront = saveBitmapToFile(imageBitmap, "nicFront.png");

                    TextView textView = getView().findViewById(R.id.settings_nicFrontTextView);
                    textView.setText("nic-front.png");
                    ImageView nicFront = getView().findViewById(R.id.nicFrontImageView);
                    nicFront.setImageBitmap(imageBitmap);

                } else {
                    Log.e("FixItLog", "Captured image is null");
                }
            } else {
                Log.e("FixItLog", "Intent data is null");
            }
        }

        if (requestCode == 300 && resultCode == Activity.RESULT_OK) {

            if (data != null && data.getExtras() != null) {

                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                if (imageBitmap != null) {

                    Log.d("FixItLog", "Nic Back captured successfully");

                    nicBack = saveBitmapToFile(imageBitmap, "nicBack.png");

                    TextView textView = getView().findViewById(R.id.settings_nicBackTextView);
                    textView.setText("nic-back.png");

                    ImageView nicBack = getView().findViewById(R.id.nicBackImageView);
                    nicBack.setImageBitmap(imageBitmap);

                } else {
                    Log.e("FixItLog", "Captured image is null");
                }
            } else {
                Log.e("FixItLog", "Intent data is null");
            }
        }

    }

    private File saveBitmapToFile(Bitmap bitmap, String name) {
        File imageFile = null;
        try {
            // Create a temporary file in the cache directory
            String fileName = name;
            imageFile = new File(requireContext().getCacheDir(), fileName);

            // Open file output stream
            FileOutputStream fos = new FileOutputStream(imageFile);

            // Compress the bitmap and write to the file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Log.d("FixItLog", "Image saved to: " + imageFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("FixItLog", "Error saving image", e);
        }

        return imageFile;
    }

    private void uploadFiles() {

        MySharedPreference sharedPreference = MySharedPreference.getMySharedPreference();
        String documentId = sharedPreference.get(getView().getContext(), "documentId");

        if (selfie != null && nicFront != null && nicBack != null && documentId != null) {

            MyAlert myAlert = MyAlert.getMyAlert();
            View alertView = myAlert.showOkBtnAlert(inflatedView.getContext(),"Uploading","Your documents are uploading.Please wait....",R.drawable.help_icon);

            alertView.findViewById(R.id.ok_alert_btn).setVisibility(View.GONE);

            AlertDialog alertDialog = myAlert.getDialog(alertView,inflatedView.getContext());
            alertDialog.show();

            Button uploadBtn = getView().findViewById(R.id.settings_kyc_uploadBtn);
            uploadBtn.setEnabled(false);

            ArrayList<File> files = new ArrayList<>();
            files.add(selfie);
            files.add(nicFront);
            files.add(nicBack);

            FirebaseStorage storage = FirebaseStorage.getInstance();

            for (File file : files) {

                if (file.exists()) {

                    Uri fileUri = Uri.fromFile(file);

                    StorageReference storageRef = storage.getReference().child("account_verification/" + documentId + "/" + file.getName());

                    storageRef.putFile(fileUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    if (file.getName().equals("nicBack.png")) {
                                        alertDialog.dismiss();
                                        updateMechanic(documentId);
                                    }

                                    Log.d("FixItLog", "File upload success: " + file.getName());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    alertDialog.dismiss();
                                    Log.d("FixItLog", "File upload failure");
                                    Toast.makeText(getContext(), "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    alertDialog.dismiss();
                    Log.d("FixItLog", "File not found: " + file.getAbsolutePath());
                    Toast.makeText(getContext(), "Something went wrong. Please try again", Toast.LENGTH_LONG).show();
                }
            }

        } else {
            Toast.makeText(getContext(), "One or More Files missing. Please upload all files to continue", Toast.LENGTH_LONG).show();
        }

    }

    private void updateMechanic(String mechanic_id) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("account_verification", "waiting");

        firestore.collection("mechanic")
                .document(mechanic_id)
                .update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d("FixItLog", "Document uploaded for Admin Review");
                        checkMechanicAccountStatus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "SettingsFragment: mechanic update failure");
                    }
                });
    }

    private void checkMechanicAccountStatus() {

        MySharedPreference sharedPreference = MySharedPreference.getMySharedPreference();

        String mechanic_id = sharedPreference.get(inflatedView.getContext(), "documentId");

        if (mechanic_id != null) {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("mechanic").document(mechanic_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {

                                DocumentSnapshot doc = task.getResult();

                                loadLayout(doc);

                            } else {
                                Log.d("FixItLog", "SettingsFragment: Search mechanic task unsuccessful");
                            }
                        }
                    });

        } else {
            Toast.makeText(inflatedView.getContext(), "Something went wrong. Please try again later", Toast.LENGTH_LONG).show();
            Log.d("FixItLog", "SettingsFragment: Mechanic id not found in shared preferences");
        }

    }

    private void loadLayout(DocumentSnapshot doc) {

        ConstraintLayout kyc_upload_layout = inflatedView.findViewById(R.id.kyc_upload_layout);
        LinearLayout kyc_message_layout = inflatedView.findViewById(R.id.kyc_message_layout);

        TextView kyc_message_title = inflatedView.findViewById(R.id.kyc_message_title);
        TextView kyc_message_description = inflatedView.findViewById(R.id.kyc_message_description);
        ImageView kyc_message_icon = inflatedView.findViewById(R.id.kyc_message_icon);
        Button kyc_message_button = inflatedView.findViewById(R.id.kyc_message_button);

        kyc_message_button.setVisibility(View.GONE);

        if (doc.getString("account_verification").equals("verified")) {

            kyc_upload_layout.setVisibility(View.GONE);
            kyc_message_layout.setVisibility(View.VISIBLE);

        }else if (doc.getString("account_verification").equals("unverified")) {

            kyc_upload_layout.setVisibility(View.VISIBLE);
            kyc_message_layout.setVisibility(View.GONE);

        } else if (doc.getString("account_verification").equals("waiting")) {

            kyc_message_title.setText("KYC Verification in Progress");
            kyc_message_description.setText("Your documents are under review. This process may take some time. Thank you for your patience!");
            kyc_message_icon.setImageResource(R.drawable.waiting);

            kyc_upload_layout.setVisibility(View.GONE);
            kyc_message_layout.setVisibility(View.VISIBLE);

        }else if (doc.getString("account_verification").equals("rejected")) {

            kyc_message_title.setText("KYC Verification Rejected");
            kyc_message_description.setText("We couldn’t verify your documents. Please check the documents again and try re-uploading them.");
            kyc_message_icon.setImageResource(R.drawable.alert_incorrect);
            kyc_message_button.setVisibility(View.VISIBLE);

            kyc_upload_layout.setVisibility(View.GONE);
            kyc_message_layout.setVisibility(View.VISIBLE);

            kyc_message_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    kyc_upload_layout.setVisibility(View.VISIBLE);
                    kyc_message_layout.setVisibility(View.GONE);

                }
            });
        }

    }
}