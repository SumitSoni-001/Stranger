package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Toast;

import com.example.stranger.R;
import com.example.stranger.databinding.ActivityCurrentLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class CurrentLocation extends AppCompatActivity {

    ActivityCurrentLocationBinding binding;

    private GoogleMap Map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient client;

    private int mapType;

    String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCurrentLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.inflateMenu(R.menu.google_map_menu);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(this);

        mapType = GoogleMap.MAP_TYPE_HYBRID;

        if (checkPermission()) {
            getMyLocation();
        } else {
            askPermission();
        }

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getMyLocation() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {

                if (location != null) {
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            Map = googleMap;

                            Map.setMinZoomPreference(6.0f);
                            Map.setMaxZoomPreference(17.0f);
                            Map.setBuildingsEnabled(true);
                            Map.setMapType(mapType);

//                            DrawCircle(Map);

                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("You are here")
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location))
                                    .draggable(true)).showInfoWindow();

//                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        }
                    });
                } else {
                    Toast.makeText(CurrentLocation.this, "Yahi problem thi", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void DrawCircle(GoogleMap gMap) {
        Circle circle;

        CircleOptions CircleMaker = new CircleOptions();

        LatLng latlang = new LatLng(13.0291, 80.2083); //Location

        CircleMaker.center(latlang);
        CircleMaker.radius(200);
        CircleMaker.strokeWidth(4);
        CircleMaker.strokeColor(R.color.logout_tint);
        CircleMaker.fillColor(R.color.coin_spend_bg);

        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(latlang, 17); //Map Zoom Level
        circle = gMap.addCircle(CircleMaker);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                askPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.google_map_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.normal:
                Toast.makeText(getApplicationContext(), "Normal", Toast.LENGTH_LONG).show();
//                Map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                return true;

            case R.id.satellite:
                Toast.makeText(getApplicationContext(), "Satellite", Toast.LENGTH_LONG).show();
//                Map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                getMyLocation();
                return true;

            case R.id.terrain:
                Toast.makeText(getApplicationContext(), "Terrain", Toast.LENGTH_LONG).show();
//                Map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                getMyLocation();
                return true;

            case R.id.hybrid:
                Toast.makeText(getApplicationContext(), "Hybrid", Toast.LENGTH_LONG).show();
//                Map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                getMyLocation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}