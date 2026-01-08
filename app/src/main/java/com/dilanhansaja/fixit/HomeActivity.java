package com.dilanhansaja.fixit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

import com.dilanhansaja.fixit.model.FixItBroadcastReceiver;
import com.dilanhansaja.fixit.model.MyAlert;
import com.dilanhansaja.fixit.model.MyNotification;
import com.dilanhansaja.fixit.model.MySharedPreference;
import com.dilanhansaja.fixit.navigation.HomeFragment;
import com.dilanhansaja.fixit.navigation.RequestHistoryFragment;
import com.dilanhansaja.fixit.navigation.SettingsFragment;
import com.dilanhansaja.fixit.navigation.TaskHistoryFragment;
import com.dilanhansaja.fixit.navigation.TaskRequestFragment;
import com.dilanhansaja.fixit.navigation.UserProfileFragment;
import com.dilanhansaja.fixit.navigation.ViewMyRequestsFragment;

public class HomeActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    MySharedPreference sharedPreference;
    private float MIN_DISTANCE_CHANGE = 50;
    private Location lastSavedLocation = null;
    String type;
    MyAlert myAlert = MyAlert.getMyAlert();
    Gson gson = new Gson();
    private FixItBroadcastReceiver receiver;

    public static boolean isNotiAllowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreference = MySharedPreference.getMySharedPreference();

        registerBroadcastReceiver();

        MyNotification.getMyNotification().createNotificationChannel(HomeActivity.this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);

        NavigationView navigationView = findViewById(R.id.navigationView1);

        Menu menu = navigationView.getMenu();

        type = sharedPreference.get(HomeActivity.this, "type");

        Toolbar toolbar = findViewById(R.id.toolbar1);

        if (type.equals("user")) {
            menu.findItem(R.id.nav_menu_task_requests).setVisible(false);
            menu.findItem(R.id.nav_menu_taskHistory).setVisible(false);
            menu.findItem(R.id.nav_menu_settings).setVisible(false);
            toolbar.setTitle(R.string.home_text1);

        } else if (type.equals("mechanic")) {
            menu.findItem(R.id.nav_menu_requestHistory).setVisible(false);
            menu.findItem(R.id.nav_menu_requests).setVisible(false);
            toolbar.setTitle("Home");
        }

        if(getIntent().hasExtra("RequestHistory")){
            loadFragment(new RequestHistoryFragment());
            toolbar.setTitle("Requests History");

        }else if(getIntent().hasExtra("MyRequests")){
           loadFragment(new ViewMyRequestsFragment());
            toolbar.setTitle("My Requests");

        }else if(getIntent().hasExtra("TaskHistory")){
           loadFragment(new TaskHistoryFragment());
            toolbar.setTitle("Task History");

        }else{

            loadFragment(new HomeFragment());
        }

        loadNavHeader(navigationView);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout1);

        openDrawer(drawerLayout);

        toolbar.setTitleTextAppearance(HomeActivity.this, R.style.textStyle1);
        navigationView.setItemTextAppearance(R.style.textStyle2);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.nav_menu_home) {
                    loadFragment(new HomeFragment());

                    if(type.equals("mechanic")){
                        toolbar.setTitle("Home");
                    }else{
                        toolbar.setTitle(R.string.home_text1);
                    }

                } else if (item.getItemId() == R.id.nav_menu_profile) {
                    loadFragment(new UserProfileFragment());

                } else if (item.getItemId() == R.id.nav_menu_logout) {
                    logout();
                    return false;
                } else if (item.getItemId() == R.id.nav_menu_requests) {
                    loadFragment(new ViewMyRequestsFragment());

                } else if (item.getItemId() == R.id.nav_menu_task_requests) {
                    loadFragment(new TaskRequestFragment());

                } else if (item.getItemId() == R.id.nav_menu_requestHistory) {
                    loadFragment(new RequestHistoryFragment());

                } else if (item.getItemId() == R.id.nav_menu_settings) {
                    loadFragment(new SettingsFragment());

                }else if (item.getItemId() == R.id.nav_menu_taskHistory) {
                    loadFragment(new TaskHistoryFragment());

                }

                if (item.getItemId() != R.id.nav_menu_home) {
                    toolbar.setTitle(item.getTitle());
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isLocationEnabled()) {
            getLocationPermission();
        } else {
            showAlert();
        }
    }

    private void getLocationPermission() {

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ||
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.d("FixItLog", "Already granted");

            if (!isNotiAllowed) {
                isNotiAllowed = checkNotificationPermission();
            }

            getCurrentLocation();

        } else {

            Log.d("FixItLog", "Not granted");

            String permissionArray[] = new String[2];
            permissionArray[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
            permissionArray[1] = Manifest.permission.ACCESS_COARSE_LOCATION;

            requestPermissions(permissionArray, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("FixItLog", "Results array= " + grantResults.length);
        Log.d("FixItLog", "Request code= " + requestCode);


        if (requestCode == 100) {

            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                Log.d("FixItLog", "granted after request");

                if (!isNotiAllowed) {
                    isNotiAllowed = checkNotificationPermission();
                }

                getCurrentLocation();

            } else {
                Log.d("FixItLog", "denied after request");

                View alert = myAlert.showOkBtnAlert(
                        HomeActivity.this,
                        "Permission Denied",
                        "Location permission is required for this app to function properly. Please grant permission to continue.",
                        R.drawable.alert_warning
                );

                AlertDialog alertDialog = myAlert.getDialog(alert, HomeActivity.this);

                Button okBtn = alert.findViewById(R.id.ok_alert_btn);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

                alertDialog.show();

            }
        } else if (requestCode == 200) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                isNotiAllowed = true;

            } else {
                isNotiAllowed = false;
            }

        }
    }

    public void loadFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView1, fragment, null)
                .setReorderingAllowed(true)
                .commit();

    }

    private void loadNavHeader(NavigationView navigationView) {

        String str_object = sharedPreference.get(HomeActivity.this, "object");

        JsonObject jsonObject = gson.fromJson(str_object, JsonObject.class);

        View view = navigationView.getHeaderView(0);

        TextView nameView = view.findViewById(R.id.nav_header_name);
        TextView emailView = view.findViewById(R.id.nav_header_email);

        nameView.setText(jsonObject.get("fname").getAsString() + " " + jsonObject.get("lname").getAsString());
        emailView.setText(jsonObject.get("email").getAsString());

    }

    private void logout() {

        MyAlert myAlert = MyAlert.getMyAlert();

        View alertView = myAlert.getConfirmationAlert(
                HomeActivity.this,
                "Confirm Logout?",
                "Are you sure you want to log out? You will need to log in again to access your account."
        );

        Button confirmBtn = alertView.findViewById(R.id.alert_right_button);
        Button cancelBtn = alertView.findViewById(R.id.alert_left_button);

        AlertDialog alertDialog = myAlert.getDialog(alertView, HomeActivity.this);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                sharedPreference.clearAll(HomeActivity.this);

                Intent intent = new Intent(HomeActivity.this, UserTypeActivity.class);
                startActivity(intent);
                finishAffinity();
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

    private boolean isLocationEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            Log.d("FixItLog", "Device Location is on");
            return true;

        } else {

            Log.d("FixItLog", "Device Location is off");

            return false;
        }

    }

    private void showAlert() {
        View alertView = myAlert.showOkBtnAlert(HomeActivity.this, "Location Unavailable", "Please turn on your device location.", R.drawable.alert_warning);

        AlertDialog alertDialog = myAlert.getDialog(alertView, HomeActivity.this);

        Button okButton = alertView.findViewById(R.id.ok_alert_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.show();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        Log.d("FixItLog", "getCurrent Location");

        if (fusedLocationClient != null) {

            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30 * 60 * 1000).build();

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    if (locationResult != null && !locationResult.getLocations().isEmpty()) {

                        Location latestLocation = locationResult.getLastLocation();

                        double latitude = latestLocation.getLatitude();
                        double longitude = latestLocation.getLongitude();

                        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latitude, longitude));

                        if (lastSavedLocation == null || latestLocation.distanceTo(lastSavedLocation) > MIN_DISTANCE_CHANGE) {

                            updateLocation(type, hash, latitude, longitude);
                            lastSavedLocation = latestLocation;

                        }

                        Log.d("FixItLog", "Location: " + latestLocation.getLatitude() + ", " + latestLocation.getLongitude());

                    } else {

                        Log.d("FixItLog", "Location is null");
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        } else {
            Log.d("FixItLog", "FusedLocationClient null");
        }
    }

    private void updateLocation(String collection, String hash, double lat, double lng) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("geohash", hash);
        map.put("lat", lat);
        map.put("lng", lng);

        String documentId = sharedPreference.get(HomeActivity.this, "documentId");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(collection)
                .document(documentId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("FixItLog", "HomeActivity: update location success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "HomeActivity: update location failure");
                    }
                });
    }

    public void openDrawer(DrawerLayout drawerLayout) {

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            Log.d("FixItLog", "Sensor Found");

            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    float values[] = sensorEvent.values;
                    float x = values[0];

                    if (x < -10) {
                        Log.d("FixItLog", "Sensor value= " + x);
                        drawerLayout.openDrawer(GravityCompat.START);

                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d("FixItLog", "Sensor Not Found");
        }

    }

    private boolean checkNotificationPermission() {

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            Log.d("FixItLog", "Notification permission granted");
            return true;

        } else {
            Log.d("FixItLog", "Requested for notifications");
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    200
            );
        }
        return false;

    }


    private void registerBroadcastReceiver() {

        receiver = new FixItBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.d("FIxItLog", "Unregistered receiver");
    }
}





