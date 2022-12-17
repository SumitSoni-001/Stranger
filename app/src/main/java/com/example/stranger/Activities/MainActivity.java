package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stranger.Models.Users;
import com.example.stranger.R;
import com.example.stranger.databinding.ActivityMainBinding;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    Users userModel;

    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    long coins = 0;
    long TotalUsers = 0;

    KProgressHUD progress;
    AlertDialog ConnDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();
        navigationColor();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.setLabel("Please Wait");
        progress.show();

        if (!isConnected(this)) {
            showNetworkAlertDialog();
        }

        // If a node(videoCall) is available with current userId, then delete it.
        database.getReference().child("videoCalls").child(Objects.requireNonNull(auth.getUid())).setValue(null);

        // Getting User details
        database.getReference().child("Users").child(Objects.requireNonNull(user).getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.dismiss();
                        userModel = snapshot.getValue(Users.class);

                        coins = Objects.requireNonNull(userModel).getCoins();

                        binding.tvCoin.setText("You have: " + coins + " ");
                        Glide.with(getApplicationContext()).load(userModel.getProfile()).placeholder(R.drawable.demo_user).into(binding.userProfile);
                        Glide.with(getApplicationContext()).load(userModel.getProfile()).placeholder(R.drawable.user_placeholder).into(binding.profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Getting Total Users
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TotalUsers = snapshot.getChildrenCount();
                binding.TotalOnlineUsers.setText(String.valueOf(TotalUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DetailsActivity.class));
            }
        });

        binding.findPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected(MainActivity.this)) {
                    showNetworkAlertDialog();
                } else {
                    if (isPermissionsEnabled()) {
                        if (coins >= 10) {

                            coins = coins - 10;
                            database.getReference().child("Users").child(auth.getUid()).child("coins").setValue(coins);

                            Intent intent = new Intent(MainActivity.this, ConnectionActivity.class);
                            intent.putExtra("profile", userModel.getProfile());
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Insufficient Coins\nWatch videos to earn coins", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        AskForPermissions();
                    }
                }
            }
        });

        binding.treasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Treasure.class));
            }
        });

    }

    private void showNetworkAlertDialog() {
        ConnDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.connection_error)
                .setTitle("No Internet !")
                .setMessage("Please check your internet connectivity and try again")
                .setCancelable(false)
                .setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));   // Wifi
//                        startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS));   // Internet
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConnDialog.dismiss();
                    }
                })
                .show();
    }

    private boolean isConnected(MainActivity context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi.isAvailable() && wifi.isConnectedOrConnecting()) || (mobile.isAvailable() && mobile.isConnectedOrConnecting())) {
            return true;
        } else {
            return false;
        }
    }

    private void AskForPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, permissions, 11);
    }

    private boolean isPermissionsEnabled() {
        for (String Permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void navigationColor() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }

}