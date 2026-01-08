package com.dilanhansaja.fixit.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.dilanhansaja.fixit.adapter.TaskRequestAdapter;
import com.dilanhansaja.fixit.model.MySharedPreference;

public class TaskRequestFragment extends Fragment {

    private Chip pending;
    private Chip approved;
    private Chip ongoing;
    private Chip selectedChip;
    private ArrayList<Chip> chips;
    private View inflatedView;
    private MySharedPreference sharedPreference;
    private ArrayList<JsonObject> taskRequestArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        chips = new ArrayList<>();
        sharedPreference = MySharedPreference.getMySharedPreference();

        inflatedView = inflater.inflate(R.layout.fragment_task_request, container, false);
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pending = view.findViewById(R.id.task_req_chip1);
        approved = view.findViewById(R.id.task_req_chip2);
        ongoing = view.findViewById(R.id.task_req_chip3);

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
                    loadTaskRequests();

                }
            });
        }

        loadTaskRequests();

    }

    private void deSelectChips() {

        for (Chip chip : chips) {
            if (chip != selectedChip) {
                chip.setChecked(false);
                chip.setChipStrokeWidth(3);
            }
        }

    }

    private void loadTaskRequests() {

        if (sharedPreference.get(inflatedView.getContext(), "type").equals("mechanic")) {

            if (sharedPreference.get(inflatedView.getContext(), "documentId") != null) {

                String mechanicId = sharedPreference.get(inflatedView.getContext(), "documentId");

                if (selectedChip != null) {

                    String status = "Pending";

                    if (selectedChip.getId() == R.id.task_req_chip1) {
                        status = "Pending";
                    } else if (selectedChip.getId() == R.id.task_req_chip2) {
                        status = "Approved";
                    } else if (selectedChip.getId() == R.id.task_req_chip3) {
                        status = "Ongoing";
                    }

                    taskRequestArrayList = new ArrayList<JsonObject>();

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("mechanic_request")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("status", status),
                                            Filter.equalTo("mechanic_id", mechanicId)
                                    )
                            )
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    if (task.isSuccessful()) {

                                        List<DocumentSnapshot> docList = task.getResult().getDocuments();

                                        if (!docList.isEmpty()) {

                                            for (DocumentSnapshot doc: docList) {

                                                String[] array = doc.getString("request_made_on").split(" ");

                                                JsonObject jsonObject = new JsonObject();
                                                jsonObject.addProperty("user_name", doc.getString("user_name"));
                                                jsonObject.addProperty("status", doc.getString("status"));
                                                jsonObject.addProperty("date", array[0]);
                                                jsonObject.addProperty("time", array[1] + " " + array[2]);
                                                jsonObject.addProperty("lat", String.valueOf(doc.getDouble("user_lat")));
                                                jsonObject.addProperty("lng", String.valueOf(doc.getDouble("user_lng")));
                                                jsonObject.addProperty("vehicle_type", doc.getString("vehicle_type"));
                                                jsonObject.addProperty("mechanic_request_id", doc.getId());

                                                taskRequestArrayList.add(jsonObject);
                                            }

                                        } else {
                                            Log.d("FixItLog", "TaskRequestFragment: doc list is empty.");
                                        }

                                        updateRecyclerView();

                                    } else {
                                        Log.d("FixItLog", "TaskRequestFragment: task unsuccessful.");
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("FixItLog", "TaskRequestFragment: task load failure");
                                }
                            });

                } else {
                    Log.d("FixItLog", "TaskRequestFragment: chip is null.");
                }

            } else {
                Log.d("FixItLog", "TaskRequestFragment: mechanicId not found in shared preferences.");
            }

        } else {
            Log.d("FixItLog", "TaskRequestFragment: mechanic not found in shared preferences.");
        }

    }

    private void updateRecyclerView() {

        RecyclerView recyclerView = inflatedView.findViewById(R.id.task_req_recyclerView);
        TaskRequestAdapter taskRequestAdapter = new TaskRequestAdapter(taskRequestArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskRequestAdapter);
    }

}