package com.dilanhansaja.fixit.navigation;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.adapter.MyRequestAdapter;
import com.dilanhansaja.fixit.model.MySharedPreference;

public class ViewMyRequestsFragment extends Fragment {

    private Chip pending;
    private Chip approved;
    private Chip ongoing;
    private MySharedPreference sharedPreference;
    private Chip selectedChip;
    private ArrayList<Chip> chips;
    private ArrayList<JsonObject> requestArrayList;
    private MyRequestAdapter myRequestAdapter;
    private View inflatedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_view_my_requests, container, false);

        chips = new ArrayList<>();
        sharedPreference = MySharedPreference.getMySharedPreference();

        return inflatedView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pending = view.findViewById(R.id.view_my_req_chip1);
        approved = view.findViewById(R.id.view_my_req_chip2);
        ongoing = view.findViewById(R.id.view_my_req_chip3);

        pending.setChecked(true);
        pending.setChipStrokeWidth(0);
        selectedChip = pending;

        chips.add(pending);
        chips.add(approved);
        chips.add(ongoing);

        for (Chip chip : chips) {
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    chip.setChecked(true);
                    chip.setChipStrokeWidth(0);
                    selectedChip = chip;
                    deSelectChips();

                    loadMyRequests();

                }
            });
        }

        loadMyRequests();

        SwipeRefreshLayout refreshLayout = inflatedView.findViewById(R.id.view_my_req_swipeRefreshLayout);
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.blue));

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("FixItLog", "Refresh working");
                        loadMyRequests();
                        myRequestAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });
    }

    private void deSelectChips() {

        for (Chip chip : chips) {
            if (chip != selectedChip) {
                chip.setChecked(false);
                chip.setChipStrokeWidth(3);
            }
        }

    }

    private void loadMyRequests() {

        String user_id = sharedPreference.get(inflatedView.getContext(), "documentId");
        requestArrayList = new ArrayList<JsonObject>();

        String status = "Pending";

        if (user_id != null && selectedChip != null) {

            if (selectedChip.getId() == R.id.view_my_req_chip1) {
                status = "Pending";
            } else if (selectedChip.getId() == R.id.view_my_req_chip2) {
                status = "Approved";
            } else if (selectedChip.getId() == R.id.view_my_req_chip3) {
                status = "Ongoing";
            }

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("mechanic_request")
                    .where(
                            Filter.and(
                                    Filter.equalTo("user_id", user_id),
                                    Filter.equalTo("status", status)
                            )
                    )
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {

                                List<DocumentSnapshot> docList = task.getResult().getDocuments();

                                if (!docList.isEmpty()) {

                                    for (DocumentSnapshot doc : docList) {

                                        String[] dateTime = doc.getString("request_made_on").split(" ");

                                        JsonObject jsonObject = new JsonObject();

                                        jsonObject.addProperty("mechanic_name", doc.getString("mechanic_name"));
                                        jsonObject.addProperty("time", dateTime[1] + " " + dateTime[2]);
                                        jsonObject.addProperty("date", dateTime[0]);
                                        jsonObject.addProperty("rate", "LKR " + String.valueOf(doc.getDouble("rate")) + "/h");
                                        jsonObject.addProperty("status", doc.getString("status"));
                                        jsonObject.addProperty("vehicle_type", doc.getString("vehicle_type"));
                                        jsonObject.addProperty("mechanic_request_id", doc.getId());

                                        requestArrayList.add(jsonObject);
                                    }

                                    Log.d("FixItLog", "ViewMyRequests: Added jsonObjects to request arraylist");

                                } else {
                                    Log.d("FixItLog", "ViewMyRequests: doc list is empty");
                                }
                            } else {
                                Log.d("FixItLog", "ViewMyRequests: load request task unsuccessful");
                            }

                            updateRecyclerView();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FixItLog", "ViewMyRequests: load requests failure");
                        }
                    });

        } else {
            Log.d("FixItLog", "ViewMyRequests: user id not found in shared preferences");
        }

    }

    private void updateRecyclerView() {
        RecyclerView recyclerView = inflatedView.findViewById(R.id.view_my_req_recyclerView);
        myRequestAdapter = new MyRequestAdapter(requestArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myRequestAdapter);
    }

}