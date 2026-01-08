package com.dilanhansaja.fixit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.MySharedPreference;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SendRequestActivity extends AppCompatActivity {
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private JsonObject intentJsonObject =  new JsonObject();
    private MyAlert myAlert = MyAlert.getMyAlert();
    private LatLng userLocation;
    private LatLng mechanicLocation;
    private MySharedPreference sharedPreference = MySharedPreference.getMySharedPreference();
    private  Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sendReqScrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String mechanicId = intent.getStringExtra("mechanicId");
        Log.d("FixItLog", "SendRequestActivity: Mechanic id:" + mechanicId);

        selectVehicleType();

        getMechanicData(mechanicId);

        Button continueBtn = findViewById(R.id.sendReqContinueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getRequestDetails();

            }
        });

    }

    MaterialCardView selectedCard = null;

    private void selectVehicleType() {

        MaterialCardView card1 = findViewById(R.id.sendReqVehicleCard1);
        MaterialCardView card2 = findViewById(R.id.sendReqVehicleCard2);
        MaterialCardView card3 = findViewById(R.id.sendReqVehicleCard3);
        MaterialCardView card4 = findViewById(R.id.sendReqVehicleCard4);
        MaterialCardView card5 = findViewById(R.id.sendReqVehicleCard5);
        MaterialCardView card6 = findViewById(R.id.sendReqVehicleCard6);

        ArrayList<MaterialCardView> cardViewList = new ArrayList<>();
        cardViewList.add(card1);
        cardViewList.add(card2);
        cardViewList.add(card3);
        cardViewList.add(card4);
        cardViewList.add(card5);
        cardViewList.add(card6);

        for (MaterialCardView cardView : cardViewList) {
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for (MaterialCardView card : cardViewList) {
                        card.setStrokeWidth(0);
                        card.setCardBackgroundColor(getColor(R.color.light_gray));
                    }
                    cardView.setStrokeWidth(10);
                    cardView.setStrokeColor(getColor(R.color.blue));
                    cardView.setCardBackgroundColor(getColor(R.color.white));

                    selectedCard = cardView;
                }
            });
        }
    }

    private void getMechanicData(String mechanicId) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("mechanic").document(mechanicId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (documentSnapshot.exists()) {

                                loadMechanicDetails(
                                        documentSnapshot.getString("fname") + " " + documentSnapshot.getString("lname"),
                                        String.valueOf(documentSnapshot.getDouble("rate")),
                                        documentSnapshot.getString("account_verification")
                                );

                                mechanicLocation = new LatLng(documentSnapshot.getDouble("lat"), documentSnapshot.getDouble("lng"));

                                getUserCurrentLocation();

                                loadMap(documentSnapshot.getDouble("lat"), documentSnapshot.getDouble("lng"));

                                intentJsonObject.addProperty("mechanic_fname",documentSnapshot.getString("fname"));
                                intentJsonObject.addProperty("mechanic_lname",documentSnapshot.getString("lname"));
                                intentJsonObject.addProperty("mechanic_rate",documentSnapshot.getDouble("rate"));
                                intentJsonObject.addProperty("mechanic_id",documentSnapshot.getId());

                            } else {
                                Log.d("FixItLog", "SendRequestActivity: load mechanic document not exists");
                            }
                        } else {
                            Log.d("FixItLog", "SendRequestActivity: load mechanic task unsuccessful");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "SendRequestActivity: load mechanic failure");
                    }
                });

    }

    private void loadMechanicDetails(String name, String rate, String account_verification) {

        TextView mechanic_name_field = findViewById(R.id.send_req_mechanic_name);
        TextView rate_field = findViewById(R.id.send_req_rate);
        ImageView verifiedIcon = findViewById(R.id.send_req_verifiedBadge);

        String ratePerHour = rate + "/h";

        mechanic_name_field.setText(name);
        rate_field.setText(ratePerHour);

        if (account_verification.equals("verified")) {
            verifiedIcon.setVisibility(View.VISIBLE);
        } else {
            verifiedIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void loadMap(double lat, double lng) {

        SupportMapFragment mapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.sendReqFrameLayout1, mapFragment);
        fragmentTransaction.commit();

        LatLng latLng = new LatLng(lat, lng);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);


                googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(latLng)
                                        .zoom(17)
                                        .build()
                        )
                );

                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("Mechanic")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mechanic))
                );

            }
        });
    }

    private void getUserCurrentLocation() {

        String userId = sharedPreference.get(SendRequestActivity.this, "documentId");
        String type = sharedPreference.get(SendRequestActivity.this, "type");

        if (userId != null && type != null) {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection(type).document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();

                                if (documentSnapshot.exists()) {

                                    userLocation = new LatLng(documentSnapshot.getDouble("lat"), documentSnapshot.getDouble("lng"));
                                    getDistance();
                                    intentJsonObject.addProperty("user_id",documentSnapshot.getId());
                                    intentJsonObject.addProperty("user_lat",documentSnapshot.getDouble("lat"));
                                    intentJsonObject.addProperty("user_lng",documentSnapshot.getDouble("lng"));
                                    intentJsonObject.addProperty("user_name",documentSnapshot.getString("fname")+" "+documentSnapshot.getString("lname"));

                                } else {
                                    Log.d("FixItLog", "SendRequestActivity: user documentSnapshot not exits.");
                                }
                            } else {
                                Log.d("FixItLog", "SendRequestActivity: get user task unsuccessful.");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FixItLog", "SendRequestActivity: get user data failure.");
                        }
                    });

        } else {
            Log.d("FixItLog", "SendRequestActivity: User id or type not found in shared preferences.");
        }

    }

    private void getDistance() {

        Gson gson = new Gson();

        if (mechanicLocation != null && userLocation != null) {

            String origin = String.valueOf(mechanicLocation.latitude) + "," + String.valueOf(mechanicLocation.longitude);
            String destination = String.valueOf(userLocation.latitude) + "," + String.valueOf(userLocation.longitude);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin
                            + "&destination=" + destination
                            + "&key=" + BuildConfig.GOOGLE_MAPS_KEY;

                    OkHttpClient okHttpClient = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    try {

                        Response response = okHttpClient.newCall(request).execute();

                        if (response.isSuccessful()) {

                            String responseText = response.body().string();

                            JsonObject responseObject = gson.fromJson(responseText, JsonObject.class);

                            JsonArray routes = responseObject.get("routes").getAsJsonArray();
                            JsonObject route = routes.get(0).getAsJsonObject();
                            JsonArray legs = route.get("legs").getAsJsonArray();
                            JsonObject leg = legs.get(0).getAsJsonObject();
                            JsonObject distance = leg.get("distance").getAsJsonObject();
                            String distanceText = distance.get("text").getAsString();

                            Log.d("FixItLog", "SendRequestActivity: distance =" + distanceText);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView distance_field = findViewById(R.id.send_req_distance);
                                    distance_field.setText(distanceText);
                                }
                            });

                        }

                    } catch (Exception e) {
                        Log.d("FixItLog", "SendRequestActivity: " + e);
                    }

                }
            }).start();

        } else {
            Log.d("FixItLog", "SendRequestActivity: user location" + userLocation);
            Log.d("FixItLog", "SendRequestActivity: mechanic location" + mechanicLocation);
        }

    }

    private void getRequestDetails() {

        Log.d("FixItLog","SendRequestActivity : getRequestDataCalled");

        String vehicle_type=null;
        EditText noteEditText =findViewById(R.id.send_req_notes);

        if(selectedCard!=null){

            if (selectedCard.getId() == R.id.sendReqVehicleCard1) {
                vehicle_type = "Bike";
            } else if (selectedCard.getId() == R.id.sendReqVehicleCard2) {
                vehicle_type = "Three-Wheeler";
            } else if (selectedCard.getId() == R.id.sendReqVehicleCard3) {
                vehicle_type = "Car";
            } else if (selectedCard.getId() == R.id.sendReqVehicleCard4) {
                vehicle_type = "Van";
            } else if (selectedCard.getId() == R.id.sendReqVehicleCard5) {
                vehicle_type = "Heavy";
            } else if (selectedCard.getId() == R.id.sendReqVehicleCard6) {
                vehicle_type = "Other";
            }

        }else{
            myAlert.showOkAlert(
                    SendRequestActivity.this,
                    "Vehicle Type Required",
                    "Please select a vehicle type before sending a mechanic request.",
                    R.drawable.alert_warning
            );
        }



        if(vehicle_type!=null){

            intentJsonObject.addProperty("vehicle_type",vehicle_type);

            if(!noteEditText.getText().toString().isBlank()){
                intentJsonObject.addProperty("notes",noteEditText.getText().toString());
            }

            Intent intent = new Intent(SendRequestActivity.this,RequestSummaryActivity.class);
            intent.putExtra("details",gson.toJson(intentJsonObject));
            startActivity(intent);

        }

    }

}

