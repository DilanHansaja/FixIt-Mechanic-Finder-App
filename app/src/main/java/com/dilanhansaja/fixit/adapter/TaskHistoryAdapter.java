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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.TaskDetailsActivity;
import com.dilanhansaja.fixit.model.MyAlert;

public class TaskHistoryAdapter extends RecyclerView.Adapter<TaskHistoryAdapter.TaskHistoryViewHolder> {

    ArrayList<JsonObject> taskHistoryArrayList;

    public TaskHistoryAdapter(ArrayList<JsonObject> taskHistoryArrayList) {
        this.taskHistoryArrayList = taskHistoryArrayList;
    }

    static class TaskHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView mechanicNameView;
        ImageView vehicle_type_imageView;
        TextView req_history_rate;
        TextView req_history_status;
        TextView req_history_cost;
        TextView req_history_DateTextView;
        TextView req_history_timeSpentTextView;
        TextView req_history_cost_type;
        TextView req_history_userType;
        Button req_history_payNowBtn;
        Button req_history_reviewBtn;
        MaterialCardView req_history_item_cardView;

        public TaskHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            this.mechanicNameView = itemView.findViewById(R.id.req_history_mechanicName);
            this.vehicle_type_imageView = itemView.findViewById(R.id.req_history_image);
            this.req_history_rate = itemView.findViewById(R.id.req_history_rate);
            this.req_history_status = itemView.findViewById(R.id.req_history_status);
            this.req_history_cost = itemView.findViewById(R.id.req_history_cost);
            this.req_history_DateTextView = itemView.findViewById(R.id.req_history_DateTextView);
            this.req_history_timeSpentTextView = itemView.findViewById(R.id.req_history_timeSpentTextView);
            this.req_history_payNowBtn = itemView.findViewById(R.id.req_history_payNowBtn);
            this.req_history_reviewBtn = itemView.findViewById(R.id.req_history_reviewBtn);
            this.req_history_item_cardView = itemView.findViewById(R.id.req_history_item_cardView);
            this.req_history_userType = itemView.findViewById(R.id.req_history_userType);
            this.req_history_cost_type = itemView.findViewById(R.id.req_history_cost_type);

        }
    }

    @NonNull
    @Override
    public TaskHistoryAdapter.TaskHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.req_history_item, parent, false);

        return new TaskHistoryAdapter.TaskHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHistoryViewHolder holder, int position) {

        int listItem = position;


        JsonObject taskHistoryObject = taskHistoryArrayList.get(listItem);
        String status = taskHistoryObject.get("status").getAsString();

        String vehicle_type = taskHistoryObject.get("vehicle_type").getAsString();

        if (vehicle_type.equals("Bike")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.motorcycle);
        } else if (vehicle_type.equals("Three-Wheeler")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.three_wheeler);
        } else if (vehicle_type.equals("Car")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.car);
        } else if (vehicle_type.equals("Van")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.van);
        } else if (vehicle_type.equals("Heavy")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.heavy);
        } else if (vehicle_type.equals("Other")) {
            holder.vehicle_type_imageView.setImageResource(R.drawable.tractor);
        }

        holder.req_history_userType.setText("Customer");
        holder.req_history_cost_type.setText("Earnings");
        holder.req_history_payNowBtn.setVisibility(View.GONE);
        holder.req_history_reviewBtn.setVisibility(View.GONE);
        holder.req_history_rate.setText("LKR " + taskHistoryObject.get("rate").getAsString() + "/h");
        holder.mechanicNameView.setText(taskHistoryObject.get("user_name").getAsString());
        holder.req_history_DateTextView.setText(taskHistoryObject.get("requested_on").getAsString());

        if (status.equals("Completed")) {

            if (taskHistoryObject.get("payment_method").getAsString().equals("Cash")) {

                holder.req_history_status.setText("Cash Not Received");
                holder.req_history_status.setTextColor(holder.itemView.getResources().getColor(R.color.red));

                holder.req_history_payNowBtn.setVisibility(View.VISIBLE);
                holder.req_history_payNowBtn.setText("Confirm Cash Payment");
                holder.req_history_payNowBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), TaskDetailsActivity.class);
                        intent.putExtra("req_id", taskHistoryObject.get("request_id").getAsString());
                        view.getContext().startActivity(intent);
                    }
                });

            } else {
                holder.req_history_status.setText("Not Paid");
                holder.req_history_status.setTextColor(holder.itemView.getResources().getColor(R.color.red));
            }

        } else if (status.equals("Paid")) {

            holder.req_history_status.setText(status);

        } else if (status.equals("Rated")) {

            holder.req_history_status.setText("Paid and Rated");
            holder.req_history_status.setTextColor(holder.itemView.getResources().getColor(R.color.mint_green));

        } else if (status.equals("Declined")) {

            holder.req_history_status.setText("Declined");
            holder.req_history_status.setTextColor(holder.itemView.getResources().getColor(R.color.red));

        }else if (status.equals("Cancelled")) {

            holder.req_history_status.setText("Cancelled");
            holder.req_history_status.setTextColor(holder.itemView.getResources().getColor(R.color.red));

        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

        String startAt = taskHistoryObject.get("startAt").getAsString();
        String endAt = taskHistoryObject.get("endAt").getAsString();

        if (
                status.equals("Completed") ||
                status.equals("Rated") ||
                status.equals("Paid")
        ) {
            try {
                Date startDate = format.parse(startAt);
                Date endDate = format.parse(endAt);

                long durationMillis = endDate.getTime() - startDate.getTime();

                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;

                String durationText = hours + "h " + minutes + "m";

                holder.req_history_timeSpentTextView.setText(durationText);

                // Convert milliseconds to total hours
                double totalHours = (double) durationMillis / (1000 * 60 * 60);

                DecimalFormat df = new DecimalFormat("0.00");

                double totalCost;

                if (totalHours <= 1) {
                    totalCost = taskHistoryObject.get("rate").getAsDouble();
                } else {
                    totalCost = totalHours * taskHistoryObject.get("rate").getAsDouble();
                }

                String total = "LKR " + String.valueOf(df.format(totalCost));
                holder.req_history_cost.setText(total);

            } catch (ParseException e) {
                Log.d("FixItLog", "RequestHistoryActivity: Exception =" + e);
            }
        }

        processDelete(status, holder, taskHistoryObject.get("request_id").getAsString(), listItem);

    }

    @Override
    public int getItemCount() {
        return taskHistoryArrayList.size();
    }

    ArrayList<String> idList = new ArrayList<>();
    ArrayList<String> listItemList = new ArrayList<>();

    private void processDelete(String status, TaskHistoryViewHolder holder, String request_id, int listItem) {

        if (status.equals("Paid")
                ||
                status.equals("Rated")
                ||
                status.equals("Declined")) {

            holder.req_history_item_cardView.setClickable(true);
            holder.req_history_item_cardView.setCheckable(true);

            holder.req_history_item_cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    holder.req_history_item_cardView.setChecked(true);

                    Log.d("FixItLog", "Id= " + request_id);
                    Log.d("FixItLog", "List Item= " + listItem);

                    if (!idList.contains(request_id)) {
                        idList.add(request_id);
                    }
                    if (!listItemList.contains(String.valueOf(listItem))) {
                        listItemList.add(String.valueOf(listItem));
                    }
                    updateDeleteBtn(view);

                    return true;
                }
            });

            holder.req_history_item_cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.req_history_item_cardView.setChecked(false);
                    idList.remove(request_id);
                    listItemList.remove(String.valueOf(listItem));
                    updateDeleteBtn(view);
                }
            });

        }
    }

    private void updateDeleteBtn(View view) {

        Button btn = view.getRootView().findViewById(R.id.task_history_fragment_deleteBtn);

        if (!idList.isEmpty() && !listItemList.isEmpty()) {

            btn.setVisibility(View.VISIBLE);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    MyAlert myAlert = MyAlert.getMyAlert();

                    View confirmAlert = myAlert.getConfirmationAlert(
                            view.getContext(),
                            "Delete ?",
                            "Are you sure to Delete selected items?"
                    );

                    AlertDialog alertDialog = myAlert.getDialog(confirmAlert, view.getContext());
                    Button confirmButton = confirmAlert.findViewById(R.id.alert_right_button);
                    Button cencelButton = confirmAlert.findViewById(R.id.alert_left_button);

                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            deleteFromDatabase(view, btn);
                        }
                    });

                    cencelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();

                }
            });

        } else {
            btn.setVisibility(View.GONE);
        }

    }

    private void deleteFromDatabase(View view, Button btn) {

        btn.setVisibility(View.GONE);
        Log.d("FixItLog", "Id list Size= " + idList.size());
        Log.d("FixItLog", "Index list Size= " + listItemList.size());

        if (idList.size() <= 10) {

            for (String id : idList) {

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("mechanic_request")
                        .document(id)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("FixItLog", "Delete Success : " + id);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("FixItLog", "Delete failure : " + id);
                            }
                        });
            }

            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            for (String item : listItemList) {
                indexesToRemove.add(Integer.parseInt(item));
            }
            indexesToRemove.sort(Collections.reverseOrder());

            for (int index : indexesToRemove) {

                if (index < taskHistoryArrayList.size()) {
                    taskHistoryArrayList.remove(index);
                    notifyItemRemoved(index);
                } else {
                    Log.e("FixItLog", "Skipping invalid index: " + index);
                }
            }

            idList.clear();
            listItemList.clear();
            indexesToRemove.clear();

            MyAlert myAlert = MyAlert.getMyAlert();
            myAlert.showOkAlert(
                    view.getContext(),
                    "Deleted Successfully",
                    "Selected items were deleted successfully.",
                    R.drawable.alert_success
            );

        } else {
            MyAlert myAlert = MyAlert.getMyAlert();
            myAlert.showOkAlert(
                    view.getContext(),
                    "Limit Exceed",
                    "Please select up to Max 10 items.",
                    R.drawable.alert_warning
            );

        }
    }
}
