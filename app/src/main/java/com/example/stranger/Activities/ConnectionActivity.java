package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.stranger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class ConnectionActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;

    String currentUser;

    boolean strangerFound = false;

    ImageView userPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        userPhoto = findViewById(R.id.profileImage);

        currentUser = auth.getUid();

//        String photo = getIntent().getStringExtra("profile");
        Glide.with(getApplicationContext()).load(getIntent().getStringExtra("profile")).into(userPhoto);

        database.getReference().child("videoCalls")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() > 0) {
                            strangerFound = true;
                            // Room Available, Join other's Room
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                               /* It gets the 1st user having 'status = 0', and match our connection with that
                                 stranger by setting our Id/name in the strangers 'Incoming node' and
                                 setting the status to 1. */
                                database.getReference().child("videoCalls").child(childSnap.getKey())
                                        .child("incoming").setValue(currentUser);
                                database.getReference().child("videoCalls").child(childSnap.getKey())
                                        .child("status").setValue(1);

                                String createdBy = childSnap.child("createdBy").getValue(String.class);
                                String Incoming = childSnap.child("incoming").getValue(String.class);
                                boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);

                                Intent intent = new Intent(ConnectionActivity.this, VideoCallActivity.class);
                                intent.putExtra("userId", currentUser);
                                intent.putExtra("createdBy", createdBy);
                                intent.putExtra("incoming", Incoming);
                                intent.putExtra("isAvailable", isAvailable);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            // Room NotAvailable, creating our own Room
                            HashMap<String, Object> Room = new HashMap<>();
                            Room.put("incoming", currentUser);
                            Room.put("createdBy", currentUser);
                            Room.put("isAvailable", true);
                            Room.put("status", 0);

                            database.getReference().child("videoCalls").child(currentUser).setValue(Room)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Now we pass an Intent taking our Rooms data to Call Activity.
                                            database.getReference().child("videoCalls").child(currentUser)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            if (snapshot.child("status").exists()) {
                                                                if (snapshot.child("status").getValue(Integer.class).equals(1)) {
                                                                  /* if (status = 1), It means that the user is find the stranger and then we send
                                                                our details to that stranger to start a video call.
                                                                 * In strangers node, inside Incoming our Id/name is given which shows that these 2 strangers
                                                                  are having a videoCall.*/
                                                                    if (strangerFound) {
                                                                        return;
                                                                    }
                                                                    strangerFound = true;

                                                                    String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                                    String Incoming = snapshot.child("incoming").getValue(String.class);
                                                                    boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);

                                                                    Intent intent = new Intent(ConnectionActivity.this, VideoCallActivity.class);
                                                                    intent.putExtra("userId", currentUser);
                                                                    intent.putExtra("createdBy", createdBy);
                                                                    intent.putExtra("incoming", Incoming);
                                                                    intent.putExtra("isAvailable", isAvailable);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        database.getReference().child("videoCalls").child(currentUser).setValue(null);
        finish();
    }

}