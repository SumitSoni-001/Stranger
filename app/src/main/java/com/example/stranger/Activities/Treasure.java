package com.example.stranger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.stranger.R;
import com.example.stranger.databinding.ActivityTreasureBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Treasure extends AppCompatActivity {

    ActivityTreasureBinding binding;
    AdRequest adRequest;
    RewardedAd mRewardedAd;

    FirebaseAuth auth;
    FirebaseDatabase database;

    long coins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTreasureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        adRequest = new AdRequest.Builder().build();

        LoadAd();

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("coins")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        coins = snapshot.getValue(Integer.class);
                        binding.yourCoins.setText(String.valueOf(coins));   // String.valueOf :- Returns the string representation of the long argument.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.watchAdsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRewardedAd != null) {
                    Activity activityContext = Treasure.this;
                    mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Log.d("RewardEarned", "The user earned the reward.");
                            LoadAd();

                            coins = coins + 10;
                            database.getReference().child("Users").child(auth.getUid()).child("coins").setValue(coins);
                            binding.yourCoins.setText(String.valueOf(coins));
                        }
                    });
                } else {
                    Toast.makeText(Treasure.this, "Ad not available", Toast.LENGTH_SHORT).show();
                    Log.d("NoReward", "The rewarded ad wasn't ready yet.");
                }

            }
        });

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void LoadAd() {
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        LoadAd();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d("AdLoaded", "Ad was loaded.");
                    }
                });
    }

}