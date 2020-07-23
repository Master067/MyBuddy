package com.example.mybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mybuddy.Adapter.MessageAdapter;
import com.example.mybuddy.model.Chat;
import com.example.mybuddy.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private static final int REQUEST_IT_MULTIPLE_PERMISSIONS=1;
    RecyclerView recyclerView;
    CircleImageView profile_image;
    TextView username,status;

    ImageButton btn_send;
    EditText text_send;
    ProgressBar progressBar;

    MessageAdapter messageAdapter;

    RelativeLayout userSpace;

    ProgressDialog loadingBar;

    Intent intent;

    String currentUserID,currentUserName,userNameForSearch,userid;

    private long mSigningSequence=1;
    private static String callType="";

    FirebaseUser fuser;
    DatabaseReference reference,userRef,chatRef,chatListRef;
    ValueEventListener listener1,listener2,listener3,listener4;

    List<Chat> mChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView=findViewById(R.id.recyler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);
        userSpace=findViewById(R.id.user_space);
        status=findViewById(R.id.status);
        btn_send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);
        progressBar=findViewById(R.id.progressbar);

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRef= FirebaseDatabase.getInstance().getReference().child("Chats");
        chatListRef= FirebaseDatabase.getInstance().getReference().child("ChatList");
        currentUserID=fuser.getUid();

        intent=getIntent();

        userid=intent.getStringExtra("id");

        userRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserName=snapshot.child("name").getValue().toString();
            }
                                                                                                        // these are the two types of value event listener
            @Override                                                                                     //which we are going to use .
            public void onCancelled(@NonNull DatabaseError error) {                                     //  for accesing the values from firebase.

            }
        });

        listener2=new ValueEventListener() {                                                         //Both ways can be used.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    chatListRef.child(userid).child(fuser.getUid()).child("messageSeen").setValue("Seen");
                    //chatListRef.child(userid).child(fuser.getUid()).child("unseenMsgCount").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        chatListRef.child(userid).child(fuser.getUid()).addValueEventListener(listener2);


        loadingBar=new ProgressDialog(MessageActivity.this);

        readUserInfo();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MessageActivity.this,ProfileView.class);
                intent.putExtra("userId",userid);
                startActivity(intent);
            }
        });

        userSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MessageActivity.this,ProfileView.class);
                intent.putExtra("userId",userid);
                startActivity(intent);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (LoginActivity.isNetworkAvilable(MessageActivity.this)) {
                    String msg = text_send.getText().toString();
                    if (!equals("") && msg.trim().length() > 0) {       // we trim message to remove wide Spaces.
                        sendMessage(currentUserID, userid, msg);
                    } else
                        Toast.makeText(MessageActivity.this, "You can't Send empty Message", Toast.LENGTH_SHORT).show();
                    text_send.setText("");
                }
                else {
                    Toast.makeText(MessageActivity.this, "Make sure your data connection is ON", Toast.LENGTH_SHORT).show();
                }
            }
        });

        readMessage(currentUserID,userid);

    }
    private void sendMessage(final String sender,final String receiver,String message){

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateTime=sdf.format(new Date());
        final long timeInMillis=ConvertDateTimeIntoMillis(currentDateTime);

        final HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("date",timeInMillis+"");

        chatListRef.child(sender).child(receiver).child("messageSeen").setValue("Sending...");
        chatRef.child(sender).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    chatRef.child(receiver).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                chatListRef.child(sender).child(receiver).child("messageSeen").setValue("delivered");
                                chatListRef.child(sender).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (!snapshot.hasChild(receiver)) {

                                            chatListRef.child(sender).child(receiver).child("NameForSearch").setValue(userNameForSearch);
                                            chatListRef.child(receiver).child(sender).child("NameForSearch").setValue(currentUserName);
                                        }
                                        chatListRef.child(sender).child(receiver).child("Time").setValue(timeInMillis);
                                        chatListRef.child(receiver).child(sender).child("Time").setValue(timeInMillis);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });

                }

            }
        });

    }

    private void readMessage(final String myid, final String userid){
        mChat=new ArrayList<>();
        listener3=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat=snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }
                }
                messageAdapter=new MessageAdapter(MessageActivity.this,mChat);
                recyclerView.setAdapter(messageAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference=chatRef.child(myid);
        reference.addValueEventListener(listener3);
    }


    private long ConvertDateTimeIntoMillis(String currentDateTime) {



        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date=null;
        try {
            date=sdf.parse(currentDateTime);
        }
        catch (ParseException e){
            e.printStackTrace();
        }
        return date.getTime();
    }

    void readUserInfo(){
        listener4=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user=dataSnapshot.getValue(User.class);
                userNameForSearch=user.getName();
                username.setText(userNameForSearch);

                if(user.getImage()!=null){
                    Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_account_circle).into(profile_image);
                }

               /* if(user.getCurrentStatus().equals("online")){
                    status.setText("Online");
                }
                else if(user.getCurrentStatus().equals("typing...")){
                    status.setText("typing...");
                }
                else{
                    String dateWithTime=user.getLastSeenDate();
                    String day=dateWithTime.substring(0,2);
                    String month=dateWithTime.substring(6,10);
                    String
                    String hour=dateWithTime.substring(11,13);
                    String min=dateWithTime.substring(14,16);
                    String  sec=dateWithTime.substring(17,19);

                    SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.getDefault());
                    String currentDateTime=sdf.format(new Date());

                    if(year.equals(currentDateTime.substring())   )

                }*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userRef.child(userid).addValueEventListener(listener4);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatListRef.child(userid).child(fuser.getUid()).removeEventListener(listener2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatListRef.child(userid).child(fuser.getUid()).addValueEventListener(listener2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        chatListRef.child(userid).child(fuser.getUid()).removeEventListener(listener2);
    }
}















