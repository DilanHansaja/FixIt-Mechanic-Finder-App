package com.dilanhansaja.fixit.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.dilanhansaja.fixit.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;


import com.dilanhansaja.fixit.RequestDetailsActivity;
import com.dilanhansaja.fixit.model.MyAlert;

public class MyRequestAdapter extends RecyclerView.Adapter<MyRequestAdapter.MyRequestViewHolder>{

    ArrayList<JsonObject> requestArrayList;

    public MyRequestAdapter(ArrayList<JsonObject> requestArrayList){
        this.requestArrayList=requestArrayList;
    }

    static class MyRequestViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView mechanicNameTextView;
        TextView rateTextView;
        TextView statusTextView;
        TextView timeTextView;
        TextView dateTextView;
        MaterialCardView cardView;
        Button view_my_req_cancelBtn;

        public MyRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageView= itemView.findViewById(R.id.view_my_req_image);
            this.mechanicNameTextView=itemView.findViewById(R.id.view_my_req_mechanicName);
            this.statusTextView=itemView.findViewById(R.id.view_my_req_status);
            this.rateTextView = itemView.findViewById(R.id.view_my_req_rate);
            this.timeTextView=itemView.findViewById(R.id.view_my_req_time);
            this.dateTextView=itemView.findViewById(R.id.view_my_req_date);
            this.cardView=itemView.findViewById(R.id.my_req_card);
            this.view_my_req_cancelBtn=itemView.findViewById(R.id.view_my_req_cancelBtn);
        }
    }

    @NonNull
    @Override
    public MyRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.view_my_req_item,parent,false);

        return new MyRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRequestViewHolder holder, int position) {

        int listItem =position;

        JsonObject requestObject = requestArrayList.get(position);

        String vehicle_type = requestObject.get("vehicle_type").getAsString();

        if(vehicle_type.equals("Bike")){
            holder.imageView.setImageResource(R.drawable.motorcycle);
        }else if(vehicle_type.equals("Three-Wheeler")){
            holder.imageView.setImageResource(R.drawable.three_wheeler);
        }else if(vehicle_type.equals("Car")){
            holder.imageView.setImageResource(R.drawable.car);
        }else if(vehicle_type.equals("Van")){
            holder.imageView.setImageResource(R.drawable.van);
        }else if(vehicle_type.equals("Heavy")){
            holder.imageView.setImageResource(R.drawable.heavy);
        }else if(vehicle_type.equals("Other")){
            holder.imageView.setImageResource(R.drawable.tractor);
        }

        holder.mechanicNameTextView.setText(requestObject.get("mechanic_name").getAsString());
        holder.timeTextView.setText(requestObject.get("time").getAsString());
        holder.dateTextView.setText(requestObject.get("date").getAsString());
        holder.rateTextView.setText(requestObject.get("rate").getAsString());
        holder.statusTextView.setText(requestObject.get("status").getAsString());

        if(requestObject.get("status").getAsString().equals("Ongoing")||requestObject.get("status").getAsString().equals("Approved")){

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), RequestDetailsActivity.class);
                    intent.putExtra("mechanic_request_id",requestObject.get("mechanic_request_id").getAsString());
                    view.getContext().startActivity(intent);
                }
            });

        }

        if(requestObject.get("status").getAsString().equals("Pending")||requestObject.get("status").getAsString().equals("Approved")){

            holder.view_my_req_cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfirmationAlert(
                            view,
                            requestObject.get("mechanic_request_id").getAsString(),
                            "Cancelled",
                            listItem,
                            "Cancel Request ?",
                            "Are you sure you want to cancel this request ?"
                    );
                }
            });
            holder.view_my_req_cancelBtn.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public int getItemCount() {
        return requestArrayList.size();
    }

    private void showConfirmationAlert(View view,String request_id,String status, int listItem,String title, String msg) {

        MyAlert myAlert = MyAlert.getMyAlert();
        View alertVIew = myAlert.getConfirmationAlert(view.getContext(), title, msg);

        AlertDialog alertDialog = myAlert.getDialog(alertVIew, view.getContext());

        Button confirmBtn = alertVIew.findViewById(R.id.alert_right_button);
        Button cancelBtn = alertVIew.findViewById(R.id.alert_left_button);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                updateRequest(request_id, status, listItem);
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

    private void updateRequest(String requestId, String status, int listItem) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        firestore.collection("mechanic_request").document(requestId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        requestArrayList.remove(listItem);
                        notifyItemRemoved(listItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "MyRequest adapter : mechanic request update failed");
                    }
                });
    }

}
