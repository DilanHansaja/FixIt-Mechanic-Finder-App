package com.dilanhansaja.fixit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import com.dilanhansaja.fixit.R;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    ArrayList<JsonObject> reviewArrayList;

    public ReviewAdapter(ArrayList<JsonObject> reviewArrayList){
        this.reviewArrayList=reviewArrayList;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder{


        TextView review_item_username_textview;
        RatingBar review_item_rating_bar;
        TextView review_item_review_textview;


        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            this.review_item_username_textview=itemView.findViewById(R.id.review_item_username_textview);
            this.review_item_rating_bar=itemView.findViewById(R.id.review_item_rating_bar);
            this.review_item_review_textview=itemView.findViewById(R.id.review_item_review_textview);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.review_item,parent,false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {

        JsonObject reviewsObject = reviewArrayList.get(position);

        holder.review_item_username_textview.setText(reviewsObject.get("user_name").getAsString());
        holder.review_item_rating_bar.setRating(Float.parseFloat(String.valueOf(reviewsObject.get("rating").getAsDouble())));
        holder.review_item_review_textview.setText(reviewsObject.get("feedback").getAsString());

    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }


}