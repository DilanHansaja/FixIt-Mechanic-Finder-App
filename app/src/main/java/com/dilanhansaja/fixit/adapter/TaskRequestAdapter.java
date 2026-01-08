package com.dilanhansaja.fixit.adapter;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.TaskDetailsActivity;
import com.dilanhansaja.fixit.model.MyAlert;

public class TaskRequestAdapter extends RecyclerView.Adapter<TaskRequestAdapter.TaskRequestViewHolder> {

    ArrayList<JsonObject> taskRequestArrayList;

    private MyAlert myAlert = MyAlert.getMyAlert();

    public TaskRequestAdapter(ArrayList<JsonObject> taskRequestArrayList) {
        this.taskRequestArrayList = taskRequestArrayList;
    }

    static class TaskRequestViewHolder extends RecyclerView.ViewHolder {

        ImageView task_req_vehicle_type;
        TextView task_req_name;
        TextView task_req_time;
        TextView task_req_date;
        Button task_req_decline_btn;
        Button task_req_approve_btn;
        Button task_req_locate_btn;
        CardView task_req_Item_cardView;

        public TaskRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            this.task_req_approve_btn = itemView.findViewById(R.id.task_req_approve_btn);
            this.task_req_vehicle_type = itemView.findViewById(R.id.task_req_vehicle_type);
            this.task_req_name = itemView.findViewById(R.id.task_req_name);
            this.task_req_time = itemView.findViewById(R.id.task_req_time);
            this.task_req_date = itemView.findViewById(R.id.task_req_date);
            this.task_req_decline_btn = itemView.findViewById(R.id.task_req_decline_btn);
            this.task_req_locate_btn = itemView.findViewById(R.id.task_req_locate_btn);
            this.task_req_Item_cardView = itemView.findViewById(R.id.task_req_Item_cardView);

        }
    }

    @NonNull
    @Override
    public TaskRequestAdapter.TaskRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.task_request_item, parent, false);

        return new TaskRequestAdapter.TaskRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskRequestAdapter.TaskRequestViewHolder holder, int position) {

        int listItem = position;

        JsonObject taskRequestObject = taskRequestArrayList.get(listItem);

        String vehicle_type = taskRequestObject.get("vehicle_type").getAsString();

        if (vehicle_type.equals("Bike")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.motorcycle);
        } else if (vehicle_type.equals("Three-Wheeler")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.three_wheeler);
        } else if (vehicle_type.equals("Car")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.car);
        } else if (vehicle_type.equals("Van")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.van);
        } else if (vehicle_type.equals("Heavy")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.heavy);
        } else if (vehicle_type.equals("Other")) {
            holder.task_req_vehicle_type.setImageResource(R.drawable.tractor);
        }

        holder.task_req_name.setText(taskRequestObject.get("user_name").getAsString());
        holder.task_req_date.setText(taskRequestObject.get("date").getAsString());
        holder.task_req_time.setText(taskRequestObject.get("time").getAsString());

        if(taskRequestObject.get("status").getAsString().equals("Pending")){

            holder.task_req_approve_btn.setVisibility(View.VISIBLE);
            holder.task_req_decline_btn.setText("Decline");
            holder.task_req_decline_btn.setVisibility(View.VISIBLE);

            holder.task_req_approve_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    showConfirmationAlert(
                            view,
                            taskRequestObject.get("mechanic_request_id").getAsString(),
                            "Approved",
                            listItem,
                            "Approve Request?",
                            "Are you sure you want to approve this request?"
                    );

                }
            });

            holder.task_req_decline_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfirmationAlert(
                            view,
                            taskRequestObject.get("mechanic_request_id").getAsString(),
                            "Declined",
                            listItem,
                            "Decline Request?",
                            "Are you sure you want to decline this request?"
                    );
                }
            });

        }else if (taskRequestObject.get("status").getAsString().equals("Ongoing")) {

            holder.task_req_approve_btn.setVisibility(View.INVISIBLE);
            holder.task_req_decline_btn.setVisibility(View.INVISIBLE);

            holder.task_req_Item_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), TaskDetailsActivity.class);
                    intent.putExtra("req_id", taskRequestObject.get("mechanic_request_id").getAsString());
                    view.getContext().startActivity(intent);
                }
            });

        } else if (taskRequestObject.get("status").getAsString().equals("Approved")) {

            holder.task_req_approve_btn.setVisibility(View.INVISIBLE);
            holder.task_req_decline_btn.setText("Cancel task");
            holder.task_req_decline_btn.setVisibility(View.VISIBLE);

            holder.task_req_decline_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfirmationAlert(
                            view,
                            taskRequestObject.get("mechanic_request_id").getAsString(),
                            "Cancelled",
                            listItem,
                            "Cancel Request?",
                            "Are you sure you want to cancel this request?"
                    );
                }
            });

            holder.task_req_Item_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), TaskDetailsActivity.class);
                    intent.putExtra("req_id", taskRequestObject.get("mechanic_request_id").getAsString());
                    view.getContext().startActivity(intent);
                }
            });
        }

        String lat = taskRequestObject.get("lat").getAsString();
        String lng = taskRequestObject.get("lng").getAsString();

        holder.task_req_locate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uri = "geo:" + lat + "," + lng + "?q=" + lat + "," + lng;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                view.getContext().startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return taskRequestArrayList.size();
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

        if(status.equals("Approved")){
            int intCode = (int) (100000 + Math.random() * 900000);
            map.put("request_code",String.valueOf(intCode));
        }

        map.put("status", status);
        firestore.collection("mechanic_request").document(requestId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        taskRequestArrayList.remove(listItem);
                        notifyItemRemoved(listItem);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("FixItLog", "TaskRequest adapter : mechanic request update failed");
                    }
                });
    }

}
