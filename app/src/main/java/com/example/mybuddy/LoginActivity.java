package com.example.mybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button nextButton;
    EditText phoneCode;     // 1st we declare our components.
    EditText mobileNo;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null) //AutoLogin Code
        {
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        nextButton =findViewById(R.id.next_button);
        phoneCode =findViewById(R.id.phone_code);       // 2nd we intialize them.
        mobileNo =findViewById(R.id.mobile);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isNetworkAvilable(LoginActivity.this)) {     // if internet connection is on.

                    String code = phoneCode.getText().toString();
                    String num = mobileNo.getText().toString();
                    if (code.isEmpty()) {
                        phoneCode.setError("Please Enter Country code");
                        phoneCode.requestFocus();
                        return;
                    }
                    if (code.length() != 3 || code.charAt(0) != '+') {
                        phoneCode.setError("InValid Country code");
                        phoneCode.requestFocus();
                        return;
                    }
                    if (num.isEmpty()) {
                        mobileNo.setError("Please Enter Mobile no.");
                        mobileNo.requestFocus();
                        return;
                    }
                    if (num.length() != 10) {
                        mobileNo.setError("InValid Mobile no.");
                        mobileNo.requestFocus();
                        return;
                    }

                    Intent intent=new Intent(LoginActivity.this,OtpVerificationActivity.class);
                    intent.putExtra("Number",code+num);
                    startActivity(intent);

                }
                else {
                    Toast.makeText(LoginActivity.this, "Make sure your data connection is ON", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public static boolean isNetworkAvilable(Context context)
    {
        if(context==null)
            return false;
        ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager!=null)
        {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            {
                NetworkCapabilities capabilities=connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if(capabilities!=null){
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                        return true;
                    }else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            }

            else {
                try {
                    NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo!=null && activeNetworkInfo.isConnected()){
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status",""+e.getMessage());
                }
            }
        }
        return false;
    }


}
