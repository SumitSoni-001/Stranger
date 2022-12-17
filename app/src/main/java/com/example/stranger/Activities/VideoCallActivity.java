package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stranger.InterfaceJava;
import com.example.stranger.Models.Users;
import com.example.stranger.R;
import com.example.stranger.databinding.ActivityVideoCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class VideoCallActivity extends AppCompatActivity {

    ActivityVideoCallBinding binding;

    //    FirebaseAuth auth;
    FirebaseDatabase database;

    String uniqueId;

    String UserId, createdBy, Incoming;
    String FriendId;

    boolean isAudio = true;
    boolean isVideo = true;
    boolean isPeerConnected = false;
    boolean cameraSwitch = false;

    boolean exitCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        UserId = getIntent().getStringExtra("userId");
        createdBy = getIntent().getStringExtra("createdBy");
        Incoming = getIntent().getStringExtra("incoming");

        FriendId = Incoming;    // Because, Incoming node consists the strangers id.

        setUpWebView();

        binding.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;     // Audio Toggled
                callJavaScriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");

                if (isAudio) {
                    binding.audio.setImageResource(R.drawable.mic_on);
                } else {
                    binding.audio.setImageResource(R.drawable.mic_off);
                }
            }
        });

        binding.video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;     // Video Toggled
                callJavaScriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");

                if (isVideo) {
                    binding.video.setImageResource(R.drawable.video_on);
                } else {
                    binding.video.setImageResource(R.drawable.video_off);
                }
            }
        });

        binding.callEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.callEnd.setImageResource(R.drawable.call_end);
//                Toast.makeText(getApplicationContext(), "Call Ended", Toast.LENGTH_SHORT).show();

                // The below 2 lines will stop audio and video after call is ended.
                callJavaScriptFunction("javascript:toggleAudio(\"" + "false" + "\")");
                callJavaScriptFunction("javascript:toggleVideo(\"" + "false" + "\")");

                finish();
            }
        });

/*
        binding.switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraSwitch = !cameraSwitch;
                callJavaScriptFunction("javascript:toggleCamera(\"" + cameraSwitch + "\")");
            }
        });
*/

    }

    private void setUpWebView() {   // Initialising WebView
        /* If we just want to render HTML, we can use "webViewClient"
         * But if we want to use various JavaScript features, then we use "webChromeClient". */
        binding.callWebView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // It request audio and video permissions.
                request.grant(request.getResources());
            }
        });

        binding.callWebView.getSettings().setJavaScriptEnabled(true);
        binding.callWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.callWebView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.callWebView.loadUrl(filePath);

        binding.callWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                /* Notify the host application that a page has finished loading.
                    This method is called only for main frame(Here, 'CallActivity'). */
                super.onPageFinished(view, url);
                initializePeer();
            }
        });

    }

    /* A 'peer' is a node, that provides the same functionality as another.
     * For Ex:- 2 PC's in a network are peers. But a PC and a server are not peers because both perform diff. operations.
     * So, In 'PeerToPeer' connection, we connect 2 or more users via a unique Id where they share their data. */
    private void initializePeer() {
        uniqueId = getUniqueId();
        // Initialising peer/connection with unique Id.
        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        // Now we set the value of "Connection Id" node in firebase. Note:- Only the host can set ConnId.
        /* If we are the host(i.e we created the Room) then we can set the ConnId and if we are joining other's room, then we don't need to update or set ConnId.
        and in this case, we will read the connection id from friends Node.*/
        if (createdBy.equalsIgnoreCase(UserId)) {

            if (exitCall) {  /* If call is ended then do not initialise connection again.(i.e If one user ended the call then the
                 call is automatically ended from other side). */
                return;
            }

            database.getReference().child("videoCalls").child(UserId).child("Connection Id").setValue(uniqueId);
            database.getReference().child("videoCalls").child(UserId).child("isAvailable").setValue(true);

            binding.loadingVideo.setVisibility(View.GONE);
            binding.VideoControls.setVisibility(View.VISIBLE);

            // Setting Values in Profile Layout
            database.getReference().child("Users").child(FriendId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users user = snapshot.getValue(Users.class);

                            binding.StrangerName.setText(Objects.requireNonNull(user).getName());
                            Glide.with(getApplicationContext()).load(user.getProfile()).placeholder(R.drawable.user_placeholder).into(binding.StrangerImage);
                            binding.StrangerCity.setText(user.getCity());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(VideoCallActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {    // If we are not the Host, then we join the meeting after 2 seconds(CUSTOM) to prevent connection error.

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    FriendId = createdBy;   // Because if we are not the host, then the Incoming node will contain our Id. That's why friendId will be the host Id.

                    // Setting Values in Profile Layout
                    database.getReference().child("Users").child(FriendId)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users userModel = snapshot.getValue(Users.class);

                                    binding.StrangerName.setText(userModel.getName());

                                    Glide.with(getApplicationContext()).load(userModel.getProfile()).placeholder(R.drawable.user_placeholder).into(binding.StrangerImage);
                                    binding.StrangerCity.setText(userModel.getCity());

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(VideoCallActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    database.getReference().child("videoCalls").child(FriendId).child("Connection Id")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                    Toast.makeText(VideoCallActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }, 3000);
        }

    }

    private String getUniqueId() {
        return UUID.randomUUID().toString();    // UUID is a class that creates uniqueId's.
    }

    private void callJavaScriptFunction(String function) {
        /* This method is used to use/toggle javaScript features (like, audio, video; etc) in the webView. */
        binding.callWebView.post(new Runnable() {
            @Override
            public void run() {
                binding.callWebView.evaluateJavascript(function, null);
            }
        });
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "you are not connected,\nPlease check your Internet Connection", Toast.LENGTH_SHORT).show();
            finish();
            return;     // Because we can only use 'break' statement inside loop or switch only.
        }

        listenConnId();
    }

    private void listenConnId() {
        /* In this,  If the Unique Connection ID exist then video call is started among those 2 strangers
         * else video call does not occur (Because the views are Invisible by default) */
        database.getReference().child("videoCalls").child(FriendId).child("Connection Id")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null) {
                            // If ConnId doesn't exist, then exit.
                            return;
                        }

                        binding.loadingVideo.setVisibility(View.GONE);
                        binding.VideoControls.setVisibility(View.VISIBLE);
                        String ConnId = snapshot.getValue(String.class);
                        callJavaScriptFunction("javascript:startCall(\"" + ConnId + "\")");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        exitCall = true;
        database.getReference().child("videoCalls").child(createdBy).setValue(null);
        finish();

    }
}