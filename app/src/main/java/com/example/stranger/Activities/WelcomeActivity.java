package com.example.stranger.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.example.stranger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        navigationColor();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            goToNextActivity();
        }

        findViewById(R.id.getStartedBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextActivity();
            }
        });

    }

    private void goToNextActivity() {
        startActivity(new Intent(WelcomeActivity.this , LoginActivity.class));
        finish();
    }

    private void navigationColor() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.welcomeNav));
//            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }

}