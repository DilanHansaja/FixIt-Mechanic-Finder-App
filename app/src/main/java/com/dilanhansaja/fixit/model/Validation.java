package com.dilanhansaja.fixit.model;

import android.content.Context;
import android.widget.EditText;

import com.dilanhansaja.fixit.R;

public class Validation {

    private static Validation validation;

    private Validation() {
    }

    public static Validation getValidation() {

        if (validation == null) {
            validation = new Validation();
        }
        return validation;
    }

    public boolean isEditTextEmpty(EditText editText, Context context,String msg) {

        if (editText.getText().toString().isEmpty()) {

            showAlert(context,"Details required",msg);
            return true;
        }
        return false;
    }

    public boolean isEmailValid(String email,Context context,String msg) {

        if(email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")){

            return true;
        }

        showAlert(context,"Invalid Email",msg);
        return false;

    }

    public boolean isPasswordValid(String password,Context context,String msg) {
        if (password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$")) {
            return true;
        }
        showAlert(context,"Invalid Password",msg);
        return false;
    }

    public boolean isMobileNumberValid(String mobile) {

        return mobile.matches("^07[01245678]{1}[0-9]{7}$");

    }

    private void showAlert(Context context,String title, String msg){

        MyAlert myAlert =MyAlert.getMyAlert();
        myAlert.showOkAlert(context,title,msg,R.drawable.alert_warning);

    }

}
