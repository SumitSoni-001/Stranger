package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stranger.Models.Users;
import com.example.stranger.R;
import com.example.stranger.databinding.ActivityDetailsBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DetailsActivity extends AppCompatActivity implements LocationListener {

    ActivityDetailsBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser user;

    String currentUser;

    KProgressHUD progress, locationProgress;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.setLabel("Loading Profile");
        progress.show();

        locationProgress = KProgressHUD.create(DetailsActivity.this);
        locationProgress.setDimAmount(0.5f);
        locationProgress.setLabel("Please Wait..");

        currentUser = auth.getUid();
        user = auth.getCurrentUser();

        binding.currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                enableGPS();
            }
        });

        // Getting Data from firebase
        database.getReference().child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    progress.dismiss();

                    Users model = snapshot.getValue(Users.class);

                    binding.etName.setText(model.getName());
                    Glide.with(getApplicationContext()).load(model.getProfile()).placeholder(R.drawable.user_placeholder).into(binding.profile);
                    binding.Location.setText(model.getCity());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.addProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(DetailsActivity.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
//                        .saveDir(File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!, "ImagePicker"))
                        .start(11);
            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newName = binding.etName.getText().toString();
                String newLocation = binding.Location.getText().toString();

                if (binding.etName.getText().toString().isEmpty()) {
                    binding.etName.setError("Enter your Name");
                    return;
                } else {
                    if (binding.Location.getText().toString().isEmpty()) {
                        binding.Location.setError("Enter Locality or get current location");
                        return;
                    } else {
                        database.getReference().child("Users").child(currentUser).child("name").setValue(newName);
                        database.getReference().child("Users").child(currentUser).child("city").setValue(newLocation);
                        Toast.makeText(DetailsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = new AlertDialog.Builder(DetailsActivity.this)
                        .setIcon(R.drawable.logout)
                        .setTitle("Logout")
                        .setMessage("Are you sure? You want to logout.")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                auth.signOut();
                                startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            if (data.getData() != null) {
                Uri img = data.getData();

                Users userModel = new Users();
                userModel.setProfile(img.toString());

                Glide.with(getApplicationContext()).load(img.toString()).placeholder(R.drawable.user_placeholder).into(binding.profile);

                database.getReference().child("Users").child(currentUser).child("profile").setValue(img.toString());

            }
        }

    }

    private void goToNextActivity() {
        startActivity(new Intent(DetailsActivity.this, MainActivity.class));
        finish();
    }

    private void grantPermissions() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED)
        ) {

            ActivityCompat.requestPermissions(
                    this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    private void enableGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("networkState", "gpsEnabled = $gpsEnabled");
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("networkState", "gpsEnabled = $networkEnabled");
        }

        if (!gpsEnabled && !networkEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable GPS Service")
                    .setMessage("We need your GPS location to show Near Places around you.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            getLocation();
        }

    }

    private void getLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    500,        // minimum time interval between location updates in milliseconds
                    5f,     // minimum distance between location updates in meters
                    this
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            locationProgress.show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            binding.Location.setText(address.get(0).getLocality() + " , " + address.get(0).getAdminArea());
            locationProgress.dismiss();
//            binding.tvSubLocality.text = address[0].subLocality   // Colony or Landmark
//            binding.tvSubAdmin.text = address[0].subAdminArea     // District

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}