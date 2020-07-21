package com.example.mybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;  // 3rd for Auto Login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);   // 1st we do this.

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();     // 3rd for Auto Login

        new CountDownTimer(3000,1000)    // 2nd timer to hold Screen.
        {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                Intent intent;

                if (firebaseUser!=null)       // 3rd  for Auto Login
                    intent=new Intent(SplashActivity.this,MainActivity.class);
                else
                    intent=new Intent(SplashActivity.this,LoginActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }.start();

    }
}
