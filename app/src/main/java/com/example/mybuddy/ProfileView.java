package com.example.mybuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileView extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView profile_image;
    ImageButton edit_profile_picture;
    ProgressBar progressBar;
    TextView textViewName,textViewStatus,textViewPhone;
    ImageView edit_name,edit_status;
    
    FirebaseUser fuser;
    DatabaseReference userRef,refrence1;
    ValueEventListener listener;
    
    StorageReference UserProfileImagesRef;
    
    ProgressDialog loadingBar;
    StorageTask uploadTask;

    String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        profile_image = findViewById(R.id.profile_image);
        edit_profile_picture = findViewById(R.id.set_profile_button);
        progressBar = findViewById(R.id.progressbar);
        textViewName = findViewById(R.id.display_name);
        textViewStatus = findViewById(R.id.display_status);
        textViewPhone = findViewById(R.id.display_phone);
        edit_name = findViewById(R.id.edit_name_button);
        edit_status = findViewById(R.id.edit_status_button);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(ProfileView.this);

        user_id = getIntent().getStringExtra("userId");

        if (user_id.equals(fuser.getUid())) {
            edit_profile_picture.setVisibility(View.VISIBLE);
            edit_name.setVisibility(View.VISIBLE);
            edit_status.setVisibility(View.VISIBLE);
        }

        LoadFromDatabase();

        edit_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCropActivity();
            }
        });

        edit_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog=new AlertDialog.Builder(ProfileView.this).create();
                LayoutInflater inflater=getLayoutInflater();
                View dialogView=inflater.inflate(R.layout.dialog_for_change_info,null);
                final EditText editTextInputstatus=dialogView.findViewById(R.id.input_name);
                Button submit_btn=dialogView.findViewById(R.id.submit_btn);
                final ProgressBar progressBar=dialogView.findViewById(R.id.progressbar);
                editTextInputstatus.setText(textViewStatus.getText().toString());

                submit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String status=editTextInputstatus.getText().toString();
                        if(status.isEmpty()){
                            editTextInputstatus.setError("Field is Empty");
                            editTextInputstatus.requestFocus();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);

                        userRef.child(user_id).child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                if(task.isSuccessful()){
                                    dialog.dismiss();
                                    Toast.makeText(ProfileView.this, "Status Changed", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(ProfileView.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    Log.i("statusChanged",task.getException().getMessage());
                                }
                            }
                        });
                    }
                });
                dialog.setView(dialogView);
                dialog.show();
            }
        });

        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog=new AlertDialog.Builder(ProfileView.this).create();
                LayoutInflater inflater=getLayoutInflater();
                View dialogView=inflater.inflate(R.layout.dialog_for_change_info,null);
                final EditText editTextInputName=dialogView.findViewById(R.id.input_name);
                Button submit_btn=dialogView.findViewById(R.id.submit_btn);
                final ProgressBar progressBar=dialogView.findViewById(R.id.progressbar);
                editTextInputName.setText(textViewName.getText().toString());

                submit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name=editTextInputName.getText().toString();
                        if(name.isEmpty()){
                            editTextInputName.setError("Field is Empty");
                            editTextInputName.requestFocus();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);

                        userRef.child(user_id).child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                if(task.isSuccessful()){
                                    dialog.dismiss();
                                    Toast.makeText(ProfileView.this, "Name Changed", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(ProfileView.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    Log.i("nameChanged",task.getException().getMessage());
                                }
                            }
                        });
                    }
                });
                dialog.setView(dialogView);
                dialog.show();
            }
        });

    }

    private void startCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(ProfileView.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

                final StorageReference filePath=UserProfileImagesRef.child(fuser.getUid()+".jpg");
                uploadTask=filePath.putBytes(fileInBytes);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                            userRef.child(fuser.getUid()).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadingBar.dismiss();
                                    if(task.isSuccessful()){
                                        Toast.makeText(ProfileView.this, "Image Saved", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Log.i("Saving Image",task.getException().toString());
                                    }
                                }
                            });
                        }
                        else{
                            Log.i("refrenceChild",task.getException().toString());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Log.i("uploadingImage",e.getMessage()+"");
                    }
                });
            }
        }

    }

    private void LoadFromDatabase() {

        listener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("image").exists()){
                    progressBar.setVisibility(View.VISIBLE);
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(profile_image, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Log.i("Profile Picasso",e.getMessage() + "");
                        }
                    });
                }
                textViewName.setText(dataSnapshot.child("name").getValue().toString());
                textViewStatus.setText(dataSnapshot.child("status").getValue().toString());
                textViewPhone.setText(dataSnapshot.child("phone").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        userRef.child(user_id).addValueEventListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userRef.child(user_id).addValueEventListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRef.child(user_id).removeEventListener(listener);
    }
}