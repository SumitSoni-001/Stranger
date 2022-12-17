package com.example.stranger.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.stranger.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {

    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Objects.requireNonNull(getSupportActionBar()).hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        animationView = findViewById(R.id.splashAnime);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animationView.playAnimation();
                startActivity(new Intent(SplashScreen.this, WelcomeActivity.class));
            }
        }, 3500);

    }
}