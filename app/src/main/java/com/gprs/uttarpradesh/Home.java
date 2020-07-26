package com.gprs.uttarpradesh;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.BuildConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Home extends AppCompatActivity {

    private static final int LAUNCH_SECOND_ACTIVITY = 2;
    private AppBarConfiguration mAppBarConfiguration;
    BottomNavigationView bottomNav;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DrawerLayout drawer;
    BroadcastReceiver br;
    TextView headename, headerrole;
    ImageView headerimage;
    TextView notificationnumber;
    NavController navController;

    DrawerLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        pref = getApplicationContext().getSharedPreferences("user", 0); // 0 - for private mode
        editor = pref.edit();

        FirebaseDatabase.getInstance().getReference().child("Assess").child(pref.getString("user", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue()!=null) {
                    UserSelfAssessHelper u=dataSnapshot.getValue(UserSelfAssessHelper.class);
                    Integer status = u.getStatus();
                    if (status != null && status == 1) {
                        editor.putString("status", "victim");
                        editor.apply();
                        if (!isMyServiceRunning(VictimAlertForegroundNotification.class)) {
                            startService(VictimAlertForegroundNotification.class);
                        }
                    } else {
                        editor.putString("status", "normal");
                        editor.commit();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(pref.getString("user", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserRegistrationHelper userRegistrationHelper = dataSnapshot.getValue(UserRegistrationHelper.class);
                if (userRegistrationHelper != null) {
                    editor.putBoolean("verify", userRegistrationHelper.getVerify());
                    editor.putString("role", userRegistrationHelper.getRole());
                    editor.commit();
                } else {
                    editor.putString("role", "not defined");
                    editor.commit();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = getSharedPreferences("user", 0); //
        editor = pref.edit();
        br = new InternetBroadcastReciever();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(br, filter);


        constraintLayout = findViewById(R.id.drawer_layout);

        if (pref.getString("user", "").equals("")) {
            startActivity(new Intent(Home.this, logouthome.class), ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            finish();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormat.format(new Date()); // Find todays date


        if (!PreferenceManager.getDefaultSharedPreferences(this).getString("today1", "").equals(currentDateTime)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("today1", currentDateTime).apply();
            FirebaseDatabase.getInstance().getReference().child("IdentityVerification").child(pref.getString("user", "")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        Snackbar snackbar = Snackbar
                                .make(constraintLayout, "Please verify your Identity before start providing services", Snackbar.LENGTH_LONG)
                                .setDuration(5000)
                                .setAction("Verify", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                    }
                                });

                        snackbar.show();
                    } else {
                        IdentityVerificationHelper i = dataSnapshot.getValue(IdentityVerificationHelper.class);
                        if (i.getStatus().equals("rejected")) {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start providing services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }



        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startAlarm(false);
            startAlarm(true);
        }

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);


        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_share, R.id.nav_notification, R.id.nav_profile, R.id.nav_covidcases, R.id.nav_updates, R.id.nav_settings, R.id.nav_share)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemIconTintList(null);
        NavigationUI.setupWithNavController(bottomNav, navController);

        View hView = navigationView.getHeaderView(0);
        headename = hView.findViewById(R.id.navheadername);
        headerrole = hView.findViewById(R.id.navheaderrole);
        headerimage = hView.findViewById(R.id.navheaderimage);

        new notificationHelper(this).createOngoingNotification("COVID19RELIEF", "Stay Safe from COVID-19");

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        navController.navigate(menuItem.getItemId());
                        break;
                    case R.id.nav_updates:
                        navController.navigate(menuItem.getItemId());
                        break;

                    case R.id.nav_covidcases:
                        navController.navigate(menuItem.getItemId());
                        break;

                    case R.id.nav_notification:
                        navController.navigate(menuItem.getItemId());
                        break;

                    case R.id.nav_profile:
                        navController.navigate(menuItem.getItemId());
                        break;
                    case R.id.nav_settings:
                        navController.navigate(menuItem.getItemId());
                        break;

                    case R.id.nav_qr:
                        startActivity(new Intent(Home.this, QRcode.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.donate:
                        startActivity(new Intent(Home.this, donate.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.firstresponder:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, firstresponder.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;

                    case R.id.scan:
                        if (!pref.getString("status", "").equals("victim"))
                            startActivity(new Intent(Home.this, victimalert.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "You are found victim \nYou can't use this festure!You are found victim \nYou can't use this festure!", Snackbar.LENGTH_LONG);
                            snackbar.show();                        }
                        startActivity(new Intent(Home.this, victimalert.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.mapping:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, MapsActivity.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;

                    case R.id.selfassess:
                        startActivity(new Intent(Home.this, SelfAssessment.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.alarm:
                        startActivity(new Intent(Home.this, Alarm.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.assignwork:
                        startActivity(new Intent(Home.this, WorkAssignHome.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.hosadmission:
                        new Bottomsheetadmissionfragment().show(getSupportFragmentManager(), "Dialog");
                        break;

                    case R.id.msme:
                        startActivity(new Intent(Home.this, MSME.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.workers:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, unorganizedsectors.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.mshop:
                        Intent intent = new Intent(Home.this, Medicalshops.class);
                        intent.putExtra("text", "Pharmacies");
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.hos:
                        intent = new Intent(Home.this, Medicalshops.class);
                        intent.putExtra("text", "Hospitals");
                        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.publiccare:
                        startActivity(new Intent(Home.this, publichealthcare.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.onlinecourse:
                        startActivity(new Intent(Home.this, course.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.labs:
                        startActivity(new Intent(Home.this, Labsfortestingandresults.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.epass:
                        try {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("http://164.100.68.164/upepass2/"));
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(Home.this, "You don't have browser installed", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case R.id.quora:
                        startActivity(new Intent(Home.this, Quora.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;

                    case R.id.helpline:
                        startActivity(new Intent(Home.this, helpline.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;


                    case R.id.nav_share:
                        final String appPackageName = BuildConfig.APPLICATION_ID;
                        final String appName = getString(R.string.app_name);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        String shareBodyText = "https://play.google.com/store/apps/details?id=" +
                                appPackageName;
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
                        startActivity(Intent.createChooser(shareIntent, getString(R.string
                                .share_with)));
                        navController.navigate(menuItem.getItemId());
                        break;
                    case R.id.chatbot:
                        startActivity(new Intent(Home.this, Chatbot.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.todo:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, ToDoHome.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.docounselling:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, DoCounselling.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.takecounselling:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, Counselling.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.material:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, MaterialCollection.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.isolation:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, Isolation.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.orphan:
                        if (pref.getBoolean("verify", false))
                            startActivity(new Intent(Home.this, OrphanAndVulnerable.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("Verify", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                                        }
                                    });

                            snackbar.show();
                        }
                        break;
                    case R.id.doverification:
                        if (pref.getString("role", "").equals(getString(R.string.monitors)))
                            startActivity(new Intent(Home.this, VerifyIdentitymonitor.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(constraintLayout, "Only Monitors can use this feature", Snackbar.LENGTH_LONG);

                            snackbar.show();
                        }
                        break;
                    case R.id.verification:
                        startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                        break;
                    case R.id.whatsapp:
                        try {
                            whatsapp(Home.this, "919013151515");
                        } catch (IllegalStateException e) {
                            Toast.makeText(Home.this, "You have no whatsapp", Toast.LENGTH_LONG).show();
                        }
                        break;

                }

                drawer.closeDrawer(GravityCompat.START);
                if (menuItem.getTitle().equals("Notification"))
                    notificationnumber.setVisibility(View.INVISIBLE);
                headename.setText(String.valueOf(menuItem.getItemId()));
                return false;
            }
        });
        drawer.setBackgroundColor(Color.WHITE);

        setheader();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        currentDateTime = dateFormat.format(new Date()); // Find todays date


        FirebaseDatabase.getInstance().getReference().child("Location").child(pref.getString("user", "")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    UserLocationHelper u = dataSnapshot.getValue(UserLocationHelper.class);

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(Home.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(u.getLat(), u.getLon(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();

                        editor.putString("city", city);
                        editor.putString("state", state);
                        editor.apply();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!isMyServiceRunning(AlarmForegroundNotification.class)) {
            startService(AlarmForegroundNotification.class);
        }


        sendBroadcast(new Intent(this, Restarter.class).setAction("Help"));

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                navController.navigate(menuItem.getItemId());
                if (menuItem.getTitle().equals("Notification"))
                    notificationnumber.setVisibility(View.INVISIBLE);
                if (menuItem.getTitle().equals("Donate")) {
                    startActivity(new Intent(Home.this, donate.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
                }
                return true;
            }
        });
        notificationnumber = findViewById(R.id.notificationnumber);


        FirebaseDatabase.getInstance().getReference().child("Notification").child(pref.getString("user", "")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Long count1 = dataSnapshot.getChildrenCount();
                    if (count1 >= 1) {
                        notificationnumber.setVisibility(View.VISIBLE);
                        notificationnumber.setText(String.valueOf(count1));

                    } else
                        notificationnumber.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setheader() {

        pref = getSharedPreferences("user", 0); // 0 - for private mode

        String useremail = pref.getString("user", "");
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        if (mStorageRef.child(useremail) != null) {
            StorageReference sr = mStorageRef.child("proImg").child(useremail + ".jpg");
            sr.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    headerimage.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(useremail);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    UserRegistrationHelper helper = dataSnapshot.getValue(UserRegistrationHelper.class);
                    headename.setText(helper.getFname());
                    headerrole.setText(helper.getRole());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!bottomNav.getMenu().getItem(0).isChecked()) {
            navController.navigate(bottomNav.getMenu().getItem(0).getItemId());
        } else {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }


            this.doubleBackToExitPressedOnce = true;
            Snackbar snackbar = Snackbar
                    .make(constraintLayout, "Please click BACK again to exit", Snackbar.LENGTH_LONG);
            snackbar.show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.identity) {
            startActivity(new Intent(Home.this, IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
        }
        if (item.getItemId() == android.R.id.home) { // use android.R.id
            drawer.openDrawer(GravityCompat.START);
        }
        if (id == R.id.logoutmenu) {
            startAlarm(false);

            new Bottomsheetlogoutfragment().show(getSupportFragmentManager(), "Dialog");

        }

        if (id == R.id.chatbot) {
            startActivityForResult(new Intent(Home.this, Chatbot.class), LAUNCH_SECOND_ACTIVITY, ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());

        }
        if (id == R.id.nav_qr) {
            startActivity(new Intent(Home.this, QRcode.class), ActivityOptions.makeSceneTransitionAnimation(Home.this).toBundle());
        }
        if (id == R.id.translate) {
            new Bottomsheetlanguagefragment().show(getSupportFragmentManager(), "Dialog");
        }

        if (id == R.id.nav_settings) {
            navController.navigate(R.id.nav_settings);
        }
        if (id == R.id.share) {
            navController.navigate(R.id.nav_share);
            final String appPackageName = BuildConfig.APPLICATION_ID;
            final String appName = getString(R.string.app_name);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareBodyText = "https://play.google.com/store/apps/details?id=" +
                    appPackageName;
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(shareIntent, getString(R.string
                    .share_with)));
        }
        return true;
    }


    public void startService(Class<?> serviceClass) {
        Intent serviceIntent = new Intent(this, serviceClass);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopService(Class<?> serviceClass) {
        Intent serviceIntent = new Intent(this, serviceClass);
        stopService(serviceIntent);

    }

    private void setAppLocale(String localeCode) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(localeCode.toLowerCase()));
        } else {
            config.locale = new Locale(localeCode.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(br);
        } catch (Exception e) {

        }
    }

    private void chatbotintro() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        final AlertDialog alert = builder.create();


        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_chatbotintro, null, true);


        Button button = view.findViewById(R.id.btn_get_started);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.hide();

            }
        });

        alert.setView(view);
        alert.show();

    }

    void startAlarm(boolean set) {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent myIntent;


        // SET TIME HERE
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = null;

        // SET TIME HERE
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        myIntent = new Intent(Home.this, YourLocationBroadcastReciever.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);


        if (set) {

            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            1 * 1000, pendingIntent);

        } else if (manager != null) {
            manager.cancel(pendingIntent);
        }

        myIntent = new Intent(Home.this, MyNotificationBroadcastReceiver.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);


        if (set) {

            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            1 * 1000, pendingIntent1);

        } else {
            if (manager != null) {
                manager.cancel((pendingIntent1));
            }
        }


        myIntent = new Intent(Home.this, HelpneededBroadcastReceiver.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);


        if (set) {

            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            1 * 1000, pendingIntent2);

        } else {
            if (manager != null) {
                manager.cancel((pendingIntent2));
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                switch (result) {
                    case "casesreport":
                        navController.navigate(bottomNav.getMenu().getItem(2).getItemId());
                        break;
                    case "updates":
                        navController.navigate(bottomNav.getMenu().getItem(1).getItemId());
                        break;
                    case "home":
                        navController.navigate(bottomNav.getMenu().getItem(0).getItemId());
                        break;
                    case "notification":
                        navController.navigate(bottomNav.getMenu().getItem(3).getItemId());
                        break;
                    case "profile":
                        navController.navigate(bottomNav.getMenu().getItem(4).getItemId());
                        break;


                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public static void whatsapp(Activity activity, String phone) {
        String formattedNumber = (phone);
        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Press 1 for latest updates");
            sendIntent.putExtra("jid", formattedNumber + "@s.whatsapp.net");
            sendIntent.setPackage("com.whatsapp");
            activity.startActivity(sendIntent);
        } catch (Exception e) {
            Toast.makeText(activity, "You don't have Whatsapp" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void callhelpline(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + "1075"));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}

