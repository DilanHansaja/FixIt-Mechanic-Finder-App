package com.dilanhansaja.fixit.navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.adapter.RequestHistoryAdapter;
import com.dilanhansaja.fixit.model.MySharedPreference;


public class RequestHistoryFragment extends Fragment {

    private View inflatedView;
    private MySharedPreference sharedPreference=MySharedPreference.getMySharedPreference();
    private ArrayList<JsonObject> requestHistoryArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inflatedView=inflater.inflate(R.layout.fragment_request_history, container, false);

        loadMyRequests();

        return inflatedView;
    }

    private void loadMyRequests(){

        String user_id=sharedPreference.get(inflatedView.getContext(),"documentId");
        requestHistoryArrayList=new ArrayList<JsonObject>();

        if(user_id!=null){

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            firestore.collection("mechanic_request")
                    .where(
                            Filter.and(
                                    Filter.equalTo("user_id",user_id),
                                    Filter.or(
                                            Filter.equalTo("status","Completed"),
                                            Filter.equalTo("status","Paid"),
                                            Filter.equalTo("status","Rated"),
                                            Filter.equalTo("status","Declined"),
                                            Filter.equalTo("status","Cancelled")
                                    )
                            )
                    )
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if(task.isSuccessful()){

                                List<DocumentSnapshot> docList =  task.getResult().getDocuments();

                                if(!docList.isEmpty()){

                                    for (DocumentSnapshot doc: docList) {

                                        String[] dateTime = doc.getString("request_made_on").split(" ");

                                        JsonObject jsonObject= new JsonObject();

                                        jsonObject.addProperty("mechanic_name",doc.getString("mechanic_name"));
                                        jsonObject.addProperty("mechanic_id",doc.getString("mechanic_id"));
                                        jsonObject.addProperty("user_name",doc.getString("user_name"));
                                        jsonObject.addProperty("requested_on",dateTime[0]);
                                        jsonObject.addProperty("rate",doc.getDouble("rate"));
                                        jsonObject.addProperty("status",doc.getString("status"));
                                        jsonObject.addProperty("startAt",doc.getString("start_at"));
                                        jsonObject.addProperty("endAt",doc.getString("end_at"));
                                        jsonObject.addProperty("vehicle_type",doc.getString("vehicle_type"));
                                        jsonObject.addProperty("request_id",doc.getId());

                                        requestHistoryArrayList.add(jsonObject);
                                    }

                                    Log.d("FixItLog","RequestHistory: Added jsonObjects to request arraylist");

                                }else{
                                    Log.d("FixItLog","RequestHistory: doc list is empty");
                                }
                            }else{
                                Log.d("FixItLog","RequestHistory: load request task unsuccessful");
                            }

                            updateRecyclerView();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("FixItLog","RequestHistory: load requests failure");
                        }
                    });

        }else{
            Log.d("FixItLog","RequestHistory: user id not found in shared preferences");
        }

    }

    private void updateRecyclerView(){
        RecyclerView recyclerView = inflatedView.findViewById(R.id.req_history_recyclerView);
        RequestHistoryAdapter requestHistoryAdapter = new RequestHistoryAdapter(requestHistoryArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(requestHistoryAdapter);
    }
}