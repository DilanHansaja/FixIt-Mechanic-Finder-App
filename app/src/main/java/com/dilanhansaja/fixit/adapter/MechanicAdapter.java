package com.dilanhansaja.fixit.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.dilanhansaja.fixit.R;
import com.dilanhansaja.fixit.SendRequestActivity;
import com.dilanhansaja.fixit.ViewMechanicProfileActivity;
import com.dilanhansaja.fixit.model.Mechanic;

public class MechanicAdapter extends RecyclerView.Adapter<MechanicAdapter.MechanicViewHolder> {

    ArrayList<Mechanic> mechanicArrayList;

    public MechanicAdapter(ArrayList<Mechanic> mechanicArrayList) {
        this.mechanicArrayList = mechanicArrayList;
    }

    static class MechanicViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTextView;
        TextView yearTextView;
        TextView rateTextView;
        Button button;
        ImageView home_item_verifyIcon;

        public MechanicViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageView = itemView.findViewById(R.id.home_item_image);
            this.nameTextView = itemView.findViewById(R.id.home_item_name);
            this.yearTextView = itemView.findViewById(R.id.home_item_year);
            this.rateTextView = itemView.findViewById(R.id.home_item_rate);
            this.button = itemView.findViewById(R.id.home_item_btn);
            this.home_item_verifyIcon = itemView.findViewById(R.id.home_item_verifyIcon);

        }
    }

    @NonNull
    @Override
    public MechanicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.home_item, parent, false);

        return new MechanicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MechanicViewHolder holder, int position) {

        Mechanic mechanic = mechanicArrayList.get(position);

        String name = mechanic.getFname() + " " + mechanic.getLname();
        String registered_date = mechanic.getRegistered_date();
        String rate = "LKR " + mechanic.getRate() + "/h";

        String[] date = registered_date.split("-");

        holder.nameTextView.setText(name);
        holder.yearTextView.setText(date[2]);
        holder.rateTextView.setText(rate);
        holder.imageView.setImageResource(R.drawable.headshot_sample);

        if (mechanic.getAccount_verification().equals("verified")) {
            holder.home_item_verifyIcon.setVisibility(View.VISIBLE);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SendRequestActivity.class);
                intent.putExtra("mechanicId", mechanic.getMechanicId());
                view.getContext().startActivity(intent);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ViewMechanicProfileActivity.class);
                intent.putExtra("mechanic_id", mechanic.getMechanicId());
                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mechanicArrayList.size();
    }
}
