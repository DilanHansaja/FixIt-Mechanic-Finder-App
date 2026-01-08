package com.dilanhansaja.fixit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.dilanhansaja.fixit.adapter.ReviewAdapter;

public class ViewMechanicProfileActivity extends AppCompatActivity {

    ArrayList<JsonObject> reviewsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_mechanic_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.uta_constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String id = intent.getStringExtra("mechanic_id");

        loadMechanicData(id);
        loadReviews(id);

        Button button = findViewById(R.id.vmp_sendReqBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ViewMechanicProfileActivity.this,SendRequestActivity.class);
                i.putExtra("mechanicId",id);
                startActivity(i);
            }
        });

    }

    private void loadMechanicData(String id) {

        TextView view_mechanic_profile_name = findViewById(R.id.view_mechanic_profile_name);
        TextView view_mechanic_profile_rate = findViewById(R.id.view_mechanic_profile_rate);
        TextView view_mechanic_profile_year = findViewById(R.id.view_mechanic_profile_year);
        ImageView view_mechanic_profile_verified = findViewById(R.id.view_mechanic_profile_verified);
        TextView view_mechanic_profile_verification=findViewById(R.id.view_mechanic_profile_verification);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("mechanic").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot doc = task.getResult();

                            String name = doc.getString("fname") + " " + doc.getString("lname");
                            view_mechanic_profile_name.setText(name);

                            view_mechanic_profile_rate.setText("LKR " + doc.getDouble("rate"));

                            String[] date = doc.getString("registered_date").split("-");

                            view_mechanic_profile_year.setText(date[2]);

                            if (doc.getString("account_verification").equals("verified")) {
                                view_mechanic_profile_verified.setVisibility(View.VISIBLE);
                                view_mechanic_profile_verification.setText("Verified");
                                view_mechanic_profile_verification.setTextColor(getColor(R.color.blue));

                            }else{
                                view_mechanic_profile_verification.setText("Unverified");
                            }

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FioxItLog","ViewMechanicProfileActivity: mechanic load failure");
                    }
                });

    }

    private void loadReviews(String id){

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("reviews")
                .whereEqualTo("mechanic_id",id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        reviewsArrayList=new ArrayList<>();

                        if(task.isSuccessful()){
                                  List<DocumentSnapshot> list =  task.getResult().getDocuments();

                                  if(!list.isEmpty()){

                                      TextView totalReviewTextView= findViewById(R.id.vmp_totalReviews);
                                      String totalReview = list.size()+" Total";
                                      totalReviewTextView.setText(totalReview);

                                      double count =0;

                                      for (DocumentSnapshot doc: list) {

                                          JsonObject jsonObject = new JsonObject();
                                          jsonObject.addProperty("user_name",doc.getString("user_name"));
                                          jsonObject.addProperty("rating",doc.getDouble("rating"));
                                          jsonObject.addProperty("feedback",doc.getString("feedback"));
                                          count+=doc.getDouble("rating");

                                          reviewsArrayList.add(jsonObject);

                                      }

                                      DecimalFormat df = new DecimalFormat("0.0");

                                      String finalRating = df.format(count/list.size());
                                      TextView vmp_finalReview = findViewById(R.id.vmp_finalReview);
                                      vmp_finalReview.setText(finalRating);

                                  }else{
                                      Log.d("FixItLog","ViewMechanicProfileActivity: reviews list is empty");
                                  }

                                  updateRecyclerView();
                        }else{
                            Log.d("FixItLog","ViewMechanicProfileActivity: task unsuccessful");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog","ViewMechanicProfileActivity: Load Reviews Failure");
                    }
                });

    }

    private void updateRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.vmp_reviews_recyclerView);
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviewsArrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(ViewMechanicProfileActivity.this));
        recyclerView.setAdapter(reviewAdapter);
    }
}