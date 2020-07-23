package com.example.mybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mybuddy.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private View chatView;
    private RecyclerView myChatList;

    private FloatingActionButton fab_add_buddy;

    private FirebaseAuth mAuth;
    private DatabaseReference buddyRef,userRef,chatListRef;

    private String currentUserID,currentUserName,currentUserPhone;

    private EditText editTextSearch;
    TextView textViewNothing;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        chatView = inflater.inflate(R.layout.activity_chats_fragment, container, false);

        setHasOptionsMenu(true);

        myChatList = chatView.findViewById(R.id.chat_list);

        mAuth = FirebaseAuth.getInstance();
        buddyRef = FirebaseDatabase.getInstance().getReference().child("Buddies");
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatListRef= FirebaseDatabase.getInstance().getReference().child("ChatList");
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myChatList.setLayoutManager(linearLayoutManager);

        fab_add_buddy = chatView.findViewById(R.id.fab_add_buddy);
        editTextSearch = chatView.findViewById(R.id.search_user);
        textViewNothing = chatView.findViewById(R.id.txt_nothing);

        textViewNothing.setVisibility(View.VISIBLE);

        searchChats("");

        return chatView;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchChats(String s) {

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(chatListRef.child(currentUserID).orderByChild("Time"),User.class).build();

        FirebaseRecyclerAdapter<User, BuddiesFragment.ViewHolder> adapter=new FirebaseRecyclerAdapter<User, BuddiesFragment.ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final BuddiesFragment.ViewHolder holder, int postion, @NonNull User user) {

                textViewNothing.setVisibility(View.GONE);

                final String usersIDs=getRef(postion).getKey();


                userRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            textViewNothing.setVisibility(View.GONE);

                            String retName=dataSnapshot.child("name").getValue().toString();
                            String retStatus=dataSnapshot.child("status").getValue().toString();


                            holder.userName.setText(retName);

                            if (dataSnapshot.hasChild("image")){
                                String retImage=dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage).placeholder(R.drawable.ic_account_circle).into(holder.profileImage);
                            }

                            holder.userStatus.setText(retStatus);
                            holder.img_on.setVisibility(View.GONE);
                            holder.img_off.setVisibility(View.GONE);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent=new Intent(getContext(),MessageActivity.class);
                                    intent.putExtra("id",usersIDs);
                                    startActivity(intent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public BuddiesFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.buddies_display_layout,parent,false);
                return new BuddiesFragment.ViewHolder(view);
            }
        };

        myChatList.setAdapter(adapter);
        adapter.startListening();

    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus, textViewLastDate, textViewCountUnseenMsg;
        CircleImageView profileImage, img_on, img_off;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.username);

            userStatus = itemView.findViewById(R.id.userstatusOrlastmessage);
            profileImage = itemView.findViewById(R.id.profile_image);
            textViewLastDate = itemView.findViewById(R.id.last_date);
            textViewCountUnseenMsg = itemView.findViewById(R.id.count_unseen_message);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);


        }

    }

}