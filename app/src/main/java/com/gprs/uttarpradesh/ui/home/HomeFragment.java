package com.gprs.uttarpradesh.ui.home;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.gprs.uttarpradesh.APIextract;
import com.gprs.uttarpradesh.AdapterHome;
import com.gprs.uttarpradesh.ApiClient;
import com.gprs.uttarpradesh.IdentityVerification;
import com.gprs.uttarpradesh.Labsfortestingandresults;
import com.gprs.uttarpradesh.Media;
import com.gprs.uttarpradesh.Model.Articles;
import com.gprs.uttarpradesh.Model.Headlines;
import com.gprs.uttarpradesh.R;
import com.gprs.uttarpradesh.UserLocationHelper;
import com.gprs.uttarpradesh.UserSelfAssessHelper;
import com.gprs.uttarpradesh.donate;
import com.gprs.uttarpradesh.pdfViewer;
import com.gprs.uttarpradesh.stepstofollow;
import com.gprs.uttarpradesh.victimalert;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    RelativeLayout todo, helpline;
    TextView confirm, death, mystatus, mystatus1, mystatus2, todotext;
    ImageView todoimage;
    int total = 0, sick = 0;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    RelativeLayout scan, firstresponder;
    TextView scrolltextview;
    View root;
    ConstraintLayout constraintLayout;
    RecyclerView recyclerView;
    Button btnAboutUs;
    Dialog dialog;
    final String API_KEY = "81e919346ed94b8491dc88809d40d4eb";
    AdapterHome adapter;
    List<Articles> articles = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home, container, false);

        constraintLayout = root.findViewById(R.id.layout);

        ImageView implink1, implink2, implink3, implink4, implink5, advice1, advice2, advice3, advice4, advice5, advice6;
        implink1 = root.findViewById(R.id.implink1);
        implink2 = root.findViewById(R.id.implink2);
        implink3 = root.findViewById(R.id.implink3);
        implink4 = root.findViewById(R.id.implink4);
        implink5 = root.findViewById(R.id.implink5);
        advice1 = root.findViewById(R.id.advice1);
        advice2 = root.findViewById(R.id.advice2);
        advice3 = root.findViewById(R.id.advice3);
        advice4 = root.findViewById(R.id.advice4);
        advice5 = root.findViewById(R.id.advice5);
        advice6 = root.findViewById(R.id.advice6);

        new HomeFragment.SetImage(implink1).execute("https://jan-sampark.nic.in/jansampark/images/campaign/2016/01-Jan/Minister/images/mygov-logo.png");
        new HomeFragment.SetImage(implink2).execute("https://pbs.twimg.com/profile_images/876679325285662721/bhGcfaXx.jpg");
        new HomeFragment.SetImage(implink3).execute("https://cdn.telanganatoday.com/wp-content/uploads/2020/05/Centre-marks-six-districts-.jpg");
        new HomeFragment.SetImage(implink4).execute("https://i.pinimg.com/564x/65/30/a5/6530a5e862c58d78b12625850fe1b256.jpg");
        new HomeFragment.SetImage(implink5).execute("https://tukuz.com/wp-content/uploads/2019/12/national-rural-health-mission-logo-vector.png");
        new HomeFragment.SetImage(advice1).execute("https://www.cuchd.in/covid-19/img/icon-1.png");
        new HomeFragment.SetImage(advice2).execute("https://www.cuchd.in/covid-19/img/icon-2.png");
        new HomeFragment.SetImage(advice3).execute("https://www.cuchd.in/covid-19/img/icon-3.png");
        new HomeFragment.SetImage(advice4).execute("https://www.cuchd.in/covid-19/img/icon-4.png");
        new HomeFragment.SetImage(advice5).execute("https://www.cuchd.in/covid-19/img/icon-5.png");
        new HomeFragment.SetImage(advice6).execute("https://www.cuchd.in/covid-19/img/icon-6.png");

        scrolltextview = root.findViewById(R.id.scrollingtextview);
        scrolltextview.setSelected(true);

        scan = root.findViewById(R.id.scan);
        todo = root.findViewById(R.id.todo);
        mystatus = root.findViewById(R.id.mystatus);
        mystatus1 = root.findViewById(R.id.mystatus1);
        helpline = root.findViewById(R.id.helpline);
        mystatus2 = root.findViewById(R.id.mystatus2);
        pref = root.getContext().getSharedPreferences("user", 0); //
        editor = pref.edit();

        todoimage = root.findViewById(R.id.todoimage);
        todotext = root.findViewById(R.id.todotext);

        LinearLayout faq, faq1;
        faq = root.findViewById(R.id.faq);
        faq1 = root.findViewById(R.id.faq1);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://drive.google.com/file/d/1GPoaMCIwbUdd3XDCzHnY_HiP7p2dVJ4x/view?usp=sharing");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        faq1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://drive.google.com/file/d/1A0mY4oMMoSMY5IeuhtKTWVvTkkdOy7lf/view?usp=sharing");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        helpline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), com.gprs.uttarpradesh.helpline.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());

            }
        });
        root.findViewById(R.id.getdirection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), Labsfortestingandresults.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });


        root.findViewById(R.id.action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://drive.google.com/file/d/1cEkR_bTmlz8se71Xtbtk1t4OlhLJYdPA/view?usp=sharing");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        root.findViewById(R.id.publicadvice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), stepstofollow.class);
                startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        root.findViewById(R.id.media).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Media.class);
                startActivity(i, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });


        check();

        if (!androidx.preference.PreferenceManager.getDefaultSharedPreferences(root.getContext()).getBoolean("chatintro", false)) {
            root.findViewById(R.id.rellayout).setVisibility(View.VISIBLE);
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(root.getContext()).edit().putBoolean("chatintro", true).apply();
        }


        root.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                root.findViewById(R.id.rellayout).setVisibility(View.INVISIBLE);
            }
        });


        root.findViewById(R.id.donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(root.getContext(), donate.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });


        recyclerView = root.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.HORIZONTAL, false));

        btnAboutUs = root.findViewById(R.id.aboutUs);
        dialog = new Dialog(root.getContext());


        final String country = getCountry();

        retrieveJson("covid uttar pradesh adityanath india", country, API_KEY);

        TextView seemoreupdates = root.findViewById(R.id.seemoreupdates);
        final NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        final BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
        seemoreupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(bottomNav.getMenu().getItem(1).getItemId());
            }
        });

        confirm = root.findViewById(R.id.confirm);
        death = root.findViewById(R.id.death);

        APIextract apIextract = new APIextract(root.getContext(), confirm, death);


        ImageView i1, i2, i3, i4, i5;
        i1 = root.findViewById(R.id.implink1);
        i2 = root.findViewById(R.id.implink2);
        i3 = root.findViewById(R.id.implink3);
        i4 = root.findViewById(R.id.implink4);
        i5 = root.findViewById(R.id.implink5);
        i1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://www.mohfw.gov.in/");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        i2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://www.nhp.gov.in/disease/communicable-disease/novel-coronavirus-2019-ncov");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });

        i3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://www.mohfw.gov.in/");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        i4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://www.icmr.gov.in/");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });
        i5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), pdfViewer.class);
                intent.putExtra("text", "https://nhm.gov.in/");
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
        });


        todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getBoolean("verify", false))
                    startActivity(new Intent(root.getContext(), com.gprs.uttarpradesh.ToDoHome.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                else {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                            .setDuration(5000)
                            .setAction("Verify", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(getActivity(), IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                }
                            });

                    snackbar.show();
                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pref.getString("status", "").equals("victim"))
                    startActivity(new Intent(root.getContext(), victimalert.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                else {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "You are found victim \nYou can't use this festure!You are found victim \nYou can't use this festure!", Snackbar.LENGTH_LONG);
                    snackbar.show();                }
            }
        });

        firstresponder = root.findViewById(R.id.firstresponder);
        firstresponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pref.getBoolean("verify", false))
                    startActivity(new Intent(root.getContext(), com.gprs.uttarpradesh.firstresponder.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                else {
                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Please verify your Identity before start using services", Snackbar.LENGTH_LONG)
                            .setDuration(5000)
                            .setAction("Verify", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(getActivity(), IdentityVerification.class), ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                }
                            });

                    snackbar.show();
                }
            }
        });

        return root;
    }

    void check() {

        FirebaseDatabase.getInstance().getReference().child("Location").child(pref.getString("user", "")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final UserLocationHelper userLocationHelper1 = dataSnapshot.getValue(UserLocationHelper.class);
                if (userLocationHelper1 != null) {

                    FirebaseDatabase.getInstance().getReference().child("Assess")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if (snapshot.getKey().equals(pref.getString("user", ""))) {
                                            continue;
                                        }
                                        final UserSelfAssessHelper userLocationHelper = snapshot.getValue(UserSelfAssessHelper.class);
                                        float[] results = new float[1];

                                        Location.distanceBetween(userLocationHelper.getLat(), userLocationHelper.getLon(),
                                                userLocationHelper1.getLat(), userLocationHelper.getLon(), results);
                                        if (results[0] < 1000) {
                                            total = total + 1;
                                            if (userLocationHelper.getStatus() == 1) {
                                                sick = sick + 1;
                                            }
                                        }


                                    }
                                    if (sick > 0) {
                                        mystatus.setText(R.string.youhavetobesafe);
                                        mystatus.setTextColor(getResources().getColor(R.color.RED));
                                        mystatus1.setText(getString(R.string.Totalassessed) + total);
                                        mystatus2.setText(getString(R.string.FoundSick) + sick + getString(R.string.Within1KMradius));

                                    } else {
                                        mystatus.setText(R.string.youaresafe);
                                        mystatus.setTextColor(getResources().getColor(R.color.GREEN));
                                        mystatus1.setText(getString(R.string.Totalassessed) + total);
                                        mystatus2.setText(getString(R.string.FoundSick) + sick + getString(R.string.Within1KMradius));
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public void retrieveJson(String query, String country, String apiKey) {
        Call<Headlines> call = ApiClient.getInstance().getApi().getSpecificData(query, apiKey);

        call.enqueue(new Callback<Headlines>() {
            @Override
            public void onResponse(Call<Headlines> call, Response<Headlines> response) {
                if (response.isSuccessful() && response.body().getArticles() != null) {
                    articles.clear();
                    articles = response.body().getArticles();
                    articles = articles.subList(0, 5);
                    adapter = new AdapterHome(root.getContext(), articles);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<Headlines> call, Throwable t) {
                Toast.makeText(root.getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getCountry() {
        Locale locale = Locale.getDefault();
        String country = locale.getCountry();
        return country.toLowerCase();
    }

    class SetImage extends AsyncTask<String, Void, Bitmap> {

        ImageView imageView;

        public SetImage(ImageView bmImage) {
            this.imageView = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);

        }
    }

}
