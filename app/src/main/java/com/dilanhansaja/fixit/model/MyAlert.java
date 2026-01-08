package com.dilanhansaja.fixit.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.dilanhansaja.fixit.R;

public class MyAlert {

    private static MyAlert myAlert;

    private MyAlert() {

    }
    public static MyAlert getMyAlert() {

        if(myAlert==null){
            myAlert=new MyAlert();
        }
        return myAlert;
    }

    public void showOkAlert(Context context,String title, String msg,int resource){

        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.ok_alert, null, false);

        TextView alertTitle = alertView.findViewById(R.id.ok_alert_title);
        TextView alertMsg = alertView.findViewById(R.id.ok_alert_msg);
        ImageView alertIcon = alertView.findViewById(R.id.ok_alert_icon);

        alertTitle.setText(title);
        alertMsg.setText(msg);
        alertIcon.setImageResource(resource);

        MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(context).setView(alertView);
        AlertDialog alertDialog = alertBuilder.create();

        Button alert_ok_btn = alertView.findViewById(R.id.ok_alert_btn);
        alert_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public View getConfirmationAlert(Context context,String title, String msg){

        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.confirmation_alert, null, false);

        TextView alertTitle = alertView.findViewById(R.id.alert_title);
        TextView alertMsg = alertView.findViewById(R.id.alert_msg);

        alertTitle.setText(title);
        alertMsg.setText(msg);

        return alertView;
    }

    public View showOkBtnAlert(Context context,String title, String msg,int resource){

        LayoutInflater inflater = LayoutInflater.from(context);
        View alertView = inflater.inflate(R.layout.ok_alert, null, false);

        TextView alertTitle = alertView.findViewById(R.id.ok_alert_title);
        TextView alertMsg = alertView.findViewById(R.id.ok_alert_msg);
        ImageView alertIcon = alertView.findViewById(R.id.ok_alert_icon);

        alertTitle.setText(title);
        alertMsg.setText(msg);
        alertIcon.setImageResource(resource);

        return alertView;
    }

    public AlertDialog getDialog (View view, Context context){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context).setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        return alertDialog ;
    }
}
