package com.dilanhansaja.fixit.navigation;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.adapter.MechanicAdapter;
import com.dilanhansaja.fixit.model.Mechanic;
import com.dilanhansaja.fixit.model.MySharedPreference;

public class HomeFragment extends Fragment {

    private MySharedPreference sharedPreference;

    private ArrayList<Mechanic> mechanicArrayList = new ArrayList<>();

    public static List<Date> getLast7Days() {
        List<Date> last7Days = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, -7);

        for (int i = 0; i < 7; i++) {
            last7Days.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1); // Move to the next day
        }

        return last7Days;
    }

    private static ArrayList<String> datesUpToToday() {

        ArrayList<String> dateList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Set calendar to the first day of the current month
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        for (int i = 1; i <= currentDay; i++) {
            dateList.add(dateFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateList;

    }

    private View homeView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        homeView = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPreference = MySharedPreference.getMySharedPreference();

        LinearLayout userLayout = homeView.findViewById(R.id.home_fragment_userLayout);
        LinearLayout mechanicLayout = homeView.findViewById(R.id.home_fragment_mechanicLayout);

        if (sharedPreference.get(homeView.getContext(), "type").equals("user")) {

            mechanicLayout.setVisibility(View.GONE);
            userLayout.setVisibility(View.VISIBLE);
            getUsersLatestLocation(homeView);

        } else {
            userLayout.setVisibility(View.GONE);
            mechanicLayout.setVisibility(View.VISIBLE);
            getData(sharedPreference.get(homeView.getContext(), "documentId"));
        }

        return homeView;
    }

    private void getUsersLatestLocation(View view) {

        if (sharedPreference.get(getContext(), "type") != null) {

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String documentId = sharedPreference.get(getContext(), "documentId");
            String type = sharedPreference.get(getContext(), "type");

            firestore.collection(type).document(documentId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {

                            if (doc != null) {
                                if (doc.exists()) {

                                    LatLng userLocation = new LatLng(doc.getDouble("lat"), doc.getDouble("lng"));
                                    Log.d("FixItLog", "HomeFragment: User location received");

                                    loadMechanics(view, userLocation);

                                } else {
                                    Log.d("FixItLog", "HomeFragment: User document not exists");
                                }
                            } else {
                                Log.d("FixItLog", "HomeFragment: User document null");
                            }

                        }
                    });

        } else {
            Log.d("FixItLog", "HomeFragment: User type not found");
        }
    }

    private void loadMechanics(View view, LatLng location) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Find cities within 50km of London
        final GeoLocation center = new GeoLocation(location.latitude, location.longitude);
        final double radiusInM = 50 * 1000;

        // Each item in 'bounds' represents a startAt/endAt pair. We have to issue
        // a separate query for each pair. There can be up to 9 pairs of bounds
        // depending on overlap, but in most cases there are 4.

        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = firestore.collection("mechanic")
                    .whereGreaterThanOrEqualTo("rate", 250)
                    .orderBy("geohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                    .limit(20);

            tasks.add(q.get());
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();

                            for (DocumentSnapshot doc : snap.getDocuments()) {

                                double lat = doc.getDouble("lat");
                                double lng = doc.getDouble("lng");

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(lat, lng);
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    matchingDocs.add(doc);
                                }
                            }
                        }

                        // matchingDocs contains the results

                        mechanicArrayList.clear();

                        for (DocumentSnapshot doc : matchingDocs) {

                            mechanicArrayList.add(new Mechanic(
                                            doc.getString("fname"),
                                            doc.getString("lname"),
                                            doc.getString("registered_date"),
                                            doc.getDouble("rate"),
                                            doc.getId(),
                                            doc.getString("account_verification")
                                    )
                            );
                        }

                        RecyclerView recyclerView = view.findViewById(R.id.home_fragment_recyclerView);
                        MechanicAdapter mechanicAdapter = new MechanicAdapter(mechanicArrayList);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(mechanicAdapter);

                        Log.d("FixItLog", "HomeFragment: load mechanics task success");
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "HomeFragment: load mechanics task failure");
                    }
                });
    }


    private void getData(String mechanic_id) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("income")
                .whereEqualTo("mechanic_id", mechanic_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            List<DocumentSnapshot> list = task.getResult().getDocuments();
                            processData(list);

                        } else {
                            Log.d("FixItLog", "Home Fragment: get data task unsuccessful");
                        }

                    }
                });
    }

    private void processData(List<DocumentSnapshot> list) {

        HashMap<String, Double> incomeMap = new HashMap<>();

        for (DocumentSnapshot doc : list) {

            incomeMap.put(doc.getString("date"), doc.getDouble("income"));

        }

        ArrayList<Entry> valuesList = new ArrayList<>();
        List<Date> last7Days = getLast7Days();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String today = format.format(new Date());
        TextView home_fragment_todayEarning = homeView.findViewById(R.id.home_fragment_todayEarning);
        TextView home_fragment_monthEarning = homeView.findViewById(R.id.home_fragment_monthEarning);


        double monthlyEarning = 0;

        for (String date : datesUpToToday()) {

            if (incomeMap.containsKey(date)) {
                monthlyEarning += Double.valueOf(incomeMap.get(date));
            }
        }

        DecimalFormat df = new DecimalFormat("0.00");
        String strMonthlyEarning = df.format(monthlyEarning);

        home_fragment_monthEarning.setText(strMonthlyEarning);

        if (incomeMap.containsKey(today)) {
            home_fragment_todayEarning.setText(String.valueOf(incomeMap.get(today)));
        } else {
            home_fragment_todayEarning.setText("0.00");
        }

        for (Date date : last7Days) {

            String stringDate = format.format(date);

            Log.d("FixItLog", stringDate);

            float x = Float.parseFloat(stringDate.split("-")[2]);
            Log.d("FixItLog", "x = " + x);

            if (incomeMap.containsKey(stringDate)) {

                float floatIncome = Float.parseFloat(String.valueOf(incomeMap.get(stringDate)));
                Log.d("FixItLog", "map y= " + floatIncome);

                valuesList.add(new Entry(x, floatIncome));

            } else {
                valuesList.add(new Entry(x, 0f));
                Log.d("FixItLog", "y= " + 0);
            }
        }

        loadLineChart1(valuesList);

    }

    private void loadLineChart1(ArrayList<Entry> valuesList) {

        LineChart lineChart = homeView.findViewById(R.id.home_fragment_lineChart1);

        LineDataSet dataSet = new LineDataSet(valuesList, "");
        dataSet.setCircleHoleRadius(3f);
        dataSet.setColor(ContextCompat.getColor(homeView.getContext(), R.color.blue));
        dataSet.setCircleColor(ContextCompat.getColor(homeView.getContext(), R.color.blue));
        dataSet.setCircleRadius(6f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);

        // Dotted Line
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setLineWidth(2.5f);

        // Gradient Fill
        dataSet.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) { // API 18+ supports gradient
            Drawable gradientDrawable = ContextCompat.getDrawable(homeView.getContext(), R.drawable.chart_gradient);
            dataSet.setFillDrawable(gradientDrawable);
        } else {
            dataSet.setFillColor(ContextCompat.getColor(homeView.getContext(), R.color.mint_green));
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setForm(Legend.LegendForm.NONE);
        lineChart.animateY(1200, Easing.EaseInCubic);

        // X-Axis settings
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        // Y-Axis settings
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(false);

        lineChart.invalidate(); // Refresh Chart

    }


}

