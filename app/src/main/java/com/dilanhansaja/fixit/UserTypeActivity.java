package com.dilanhansaja.fixit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dilanhansaja.fixit.model.MySharedPreference;

public class UserTypeActivity extends AppCompatActivity {

    MySharedPreference sharedPreference = MySharedPreference.getMySharedPreference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_type);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.uta_constraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String user_type  = sharedPreference.get(UserTypeActivity.this,"type");

        if(user_type!=null){

            Intent intent = new Intent(UserTypeActivity.this, HomeActivity.class);
            startActivity(intent);
            finishAffinity();

        }else{

            TextView textView = findViewById(R.id.uta_createAccountText);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UserTypeActivity.this,RegisterActivity.class);
                    startActivity(intent);
                }
            });

            Button userBtn = findViewById(R.id.uta_userBtn);
            userBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(UserTypeActivity.this,LoginActivity.class);
                    intent.putExtra("type","user");
                    startActivity(intent);


                }
            });

            Button mechanicBtn = findViewById(R.id.uta_mechanicBtn);
            mechanicBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(UserTypeActivity.this,LoginActivity.class);
                    intent.putExtra("type","mechanic");
                    startActivity(intent);
                }
            });

        }

    }
}