package com.example.mybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity {

    Button submitButton;
    EditText inputOtp;                          // 1st we declare important components.
    TextView resend,wrong;

    ProgressDialog dialog;

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    FirebaseAuth mAuth;

    String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        dialog=new ProgressDialog(OtpVerificationActivity.this);

         submitButton=findViewById(R.id.submit);
         inputOtp=findViewById(R.id.otp);
         resend=findViewById(R.id.resend);                        //2nd we intiallize them.
        wrong=findViewById(R.id.wrong_no);


        mAuth=FirebaseAuth.getInstance();


        Intent intent=getIntent();
        number=intent.getStringExtra("Number");

        wrong.setText("wrong ? "+number);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LoginActivity.isNetworkAvilable(OtpVerificationActivity.this)){
                    String code=inputOtp.getText().toString();
                    if(code.isEmpty()){
                        inputOtp.setError("Feild is Empty");
                        inputOtp.requestFocus();
                        return;
                    }

                    dialog.setTitle("Phone Verification");
                    dialog.setMessage("Please Wait While Verification");
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    verifyVerificationCode(code);
                }
                else{
                    Toast.makeText(OtpVerificationActivity.this, "NO Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LoginActivity.isNetworkAvilable(OtpVerificationActivity.this)){
                    resend.setClickable(false);
                    dialog.show();
                    sendVerificationCode(number);
                }
                else{
                    Toast.makeText(OtpVerificationActivity.this, "NO Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });


        wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(OtpVerificationActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });


        dialog.setTitle("Phone Verification");
        dialog.setMessage("Please Wait While Verification");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        sendVerificationCode(number);                 // 3rd we send(get) OTP
    }

    private void sendVerificationCode(String number) {                  // 3rd so this will run.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                callbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {    // 5th we recieve the OTP
            String code=phoneAuthCredential.getSmsCode();                                //5th Now it is set automatically Or type it .
            if(code!=null)
            {
                inputOtp.setText(code);
                dialog.setTitle("Phone Verification");
                dialog.setMessage("Please Wait While we are Verifying");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                verifyVerificationCode(code);                                 // 6th Now it is send for verification.
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {            // 8th
            dialog.dismiss();
            resend.setClickable(true);
            Toast.makeText(OtpVerificationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);                                                   //4th this will run.
            dialog.dismiss();
            Toast.makeText(OtpVerificationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
            resend.setClickable(false);
            mVerificationId=verificationId;
            mResendToken=forceResendingToken;
            new CountDownTimer(60000,1000){

                @Override
                public void onTick(long l) {
                    resend.setText("resend in "+(l/1000)+"s");
                }

                @Override
                public void onFinish() {
                    resend.setText("resend");
                    resend.setClickable(true);
                }
            }.start();
        }
    };

    private void verifyVerificationCode(String code) {                    // 6th  this is done.
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId ,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {                     // 7th this is  done.
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialog.dismiss();
                if(task.isSuccessful()){

                    SharedPreferences.Editor edit=getSharedPreferences("Phone",MODE_PRIVATE).edit();     // means shared storage
                    edit.putString("number",number);                                                         // here we creating a file named "Phone" in which we store "number" in key-value format.
                    edit.apply();

                    Intent intent=new Intent(OtpVerificationActivity.this,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    String message="Something went Wrong.....";
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        message="Please enter the correct OTP";
                    }
                    Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
