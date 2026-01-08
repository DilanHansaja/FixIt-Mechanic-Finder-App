package com.dilanhansaja.fixit.model;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {

    private static MySharedPreference mySharedPreference;

    private MySharedPreference() {}

    public static MySharedPreference getMySharedPreference() {

        if (mySharedPreference == null) {

            mySharedPreference=new MySharedPreference();
        }

        return mySharedPreference;
    }

    public String get(Context context,String key){

      SharedPreferences sharedPreferences =  context.getSharedPreferences("lk.javainstitute.fixit.data",Context.MODE_PRIVATE);

        return  sharedPreferences.getString(key,null);
    }

    public void clearAll(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences("lk.javainstitute.fixit.data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }


}
