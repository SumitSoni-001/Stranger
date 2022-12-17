package com.example.stranger.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stranger.Models.Users;
import com.example.stranger.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import javax.security.auth.login.LoginException;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> startActivityForResult;
    FirebaseAuth auth;
    FirebaseDatabase database;
    private long coins = 200;
    private String city = "";
    private String profile;
    private String name;

    ProgressDialog progress;
    AlertDialog ConnDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SystemNavColor();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() != null) {
            goToNextActivity();
        }

/*
        database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("coins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Users user = new Users();
                    coins = user.getCoins();
                    Toast.makeText(LoginActivity.this, coins+"", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
*/

        // "gso" gets emails from our device and if no Email exist, then it also provides a option for "useAnotherAccount".
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isConnected(LoginActivity.this)) {
                    showNetworkAlertDialog();
                } else {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult.launch(signInIntent);
                }
            }
        });

        startActivityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());

                    } catch (ApiException e) {
                        Log.w("TAG", "Google sign in failed", e);
                        Toast.makeText(getApplicationContext(), "Google Sign In failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void showNetworkAlertDialog() {
        ConnDialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.connection_error)
                .setTitle("No Internet !")
                .setMessage("Please check your internet connectivity and try again")
                .setCancelable(false)
                .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConnDialog.dismiss();
                    }
                })
                .show();
    }

    private boolean isConnected(LoginActivity login) {
        ConnectivityManager connectivityManager = (ConnectivityManager) login.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifi.isAvailable() && wifi.isConnectedOrConnecting()) || (mobile.isAvailable() && mobile.isConnectedOrConnecting())) {
            return true;
        } else {
            return false;
        }
    }

    private void goToNextActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finishAffinity();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            showProgressDialog();
                            FirebaseUser user = auth.getCurrentUser();

                            // If user already has account,  so its old details were taken else a new account is created.
                            database.getReference().child("Users").child(Objects.requireNonNull(user).getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChildren()) {
                                        Users userData = snapshot.getValue(Users.class);
                                        coins = Objects.requireNonNull(userData).getCoins();
                                        city = userData.getCity();

                                        if (userData.getName().isEmpty()) { // Gmail name will be taken
                                            name = Objects.requireNonNull(user.getDisplayName());
                                        } else {    // Old name is taken
                                            name = userData.getName();
                                        }

                                        if (userData.getProfile().isEmpty()) {
                                            profile = Objects.requireNonNull(user.getPhotoUrl()).toString();
                                        } else {
                                            profile = userData.getProfile();
                                        }

                                        Users UserModel = new Users(user.getUid(), name, profile, city, coins);
                                        database.getReference().child("Users").child(user.getUid()).setValue(UserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progress.dismiss();
                                                    goToNextActivity();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Users UserModel = new Users(user.getUid(), user.getDisplayName(), Objects.requireNonNull(user.getPhotoUrl()).toString(), city, coins);
                                        database.getReference().child("Users").child(user.getUid()).setValue(UserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progress.dismiss();
                                                    goToNextActivity();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                           /* Users UserModel = new Users(user.getUid(), user.getDisplayName(), profile, city, coins);
                            database.getReference().child("Users").child(user.getUid()).setValue(UserModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        goToNextActivity();
                                    } else {
                                        Toast.makeText(getApplicationContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });*/

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("CredentialFailure", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void SystemNavColor() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.welcomeNav));
//            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }
    }

    private void showProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle("Logging in...");
        progress.setMessage("Please wait while we are fetching your details.");
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();
    }

}