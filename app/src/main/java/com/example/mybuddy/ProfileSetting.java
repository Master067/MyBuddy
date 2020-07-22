package com.example.mybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetting extends AppCompatActivity {

    CircleImageView profileImage;
    ImageButton setProfileImage;
    EditText username,userStatus;
    Button saveButton;

    String current_user_ID;
    FirebaseAuth auth;
    DatabaseReference rootRef;
    ProgressBar imageProgressBar;                                      //1st we declare
    Toolbar toolbar;

    StorageReference userProfileImagesRef;
    ProgressDialog loadingBar;
    StorageTask uploadTask;

    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        profileImage=findViewById(R.id.profile_image);
        setProfileImage=findViewById(R.id.set_profil_button);
        username=findViewById(R.id.input_name);                                 // 2nd we intiallize.
        userStatus=findViewById(R.id.input_status);
        saveButton=findViewById(R.id.save_button);
        imageProgressBar=findViewById(R.id.image_progress_bar);
        toolbar=findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Setting");                   // setting a toolbaar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        auth=FirebaseAuth.getInstance();
        current_user_ID=auth.getCurrentUser().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();

        userProfileImagesRef= FirebaseStorage.getInstance().getReference().child("profile images");
        loadingBar=new ProgressDialog(ProfileSetting.this);

        loadProfileImage();           //3rd if image is there then it is loaded.


        setProfileImage.setOnClickListener(new View.OnClickListener() {    // 4th otherwise we start this.
            @Override
            public void onClick(View view) {
                startCropActivity();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSetting();
            }
        });      //5th
    }

    private void saveSetting() {                     //   5th
        String name=username.getText().toString();
        String status=userStatus.getText().toString();
        if(name.isEmpty()){
            username.setError("Field is Empty");
            username.requestFocus();
            return;
        }
        if(status.isEmpty()){
            userStatus.setError("Field is Empty");
            userStatus.requestFocus();
            return;
        }

        loadingBar.setTitle("Profile Setting");
        loadingBar.setMessage("Please wait while we are updating your profile...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        SharedPreferences prefs= getSharedPreferences("Phone",MODE_PRIVATE);
        String phoneNumber =prefs.getString("number","");

        HashMap<String,Object> profileMap=new HashMap<>();              // adding this hashMap in below of "Users"  (i.e. child of rootRef).
        profileMap.put("uid",current_user_ID);
        profileMap.put("name",name);
        profileMap.put("status",status);
        profileMap.put("phone",phoneNumber);

        rootRef.child("Users").child(current_user_ID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                    Toast.makeText(ProfileSetting.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProfileSetting.this,MainActivity.class));
                    finish();
                }
                else{
                    String message=task.getException().toString();
                    Log.i("updateSettings",message);
                }
            }
        });
    }

    private void loadProfileImage() {

        listener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("image").exists())
                {
                    imageProgressBar.setVisibility(View.VISIBLE);
                    String profileImageUrl=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(profileImageUrl).into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            imageProgressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileSetting.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        rootRef.child("Users").child(current_user_ID).addValueEventListener(listener);
    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(ProfileSetting.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);                                  //6th (at last) this will run.
                                                                                                //inorder to save image URL in database.
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resultUri=result.getUri();

                Bitmap bmp=null;
                try {
                    bmp= MediaStore.Images.Media.getBitmap(getContentResolver(),resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG,25,baos);
                byte[] fileInBytes=baos.toByteArray();

                loadingBar.setTitle("Setting Your Profile");
                loadingBar.setMessage("Please wait as your profile is Updating....");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final StorageReference filePath=userProfileImagesRef.child(current_user_ID+".jpg");
                uploadTask=filePath.putBytes(fileInBytes);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        loadingBar.dismiss();
                        if(task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            String downloadUrl=downloadUri.toString();
                            loadingBar.show();
                            rootRef.child("Users").child(current_user_ID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingBar.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(ProfileSetting.this, "Image Saved", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Log.i("Saving Image",task.getException().toString());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }

    }
}
