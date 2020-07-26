package com.gprs.uttarpradesh;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Login extends AppCompatActivity {
    Animation top, bottom;

    TextView register, toptext, forgot, textView7;
    ImageView imageView5;
    TextInputLayout inputEmail, inputPassword;
    Button login;
    private ProgressDialog progressDialog;
    ImageView language;

    private SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {
        super.onStart();


        pref = getApplicationContext().getSharedPreferences("user", 0); // 0 - for private mode

        if (!pref.getString("user", "").equals("")) {
            startActivity(new Intent(Login.this, bio.class));
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        register = findViewById(R.id.gotoRegister);
        toptext = findViewById(R.id.topText);
        imageView5 = findViewById(R.id.imageView5);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        login = findViewById(R.id.btnLogin);
        forgot = findViewById(R.id.forgotPassword);
        textView7 = findViewById(R.id.textView7);
        language = findViewById(R.id.language);
        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setTitle("Logging In");
        progressDialog.setMessage("connecting...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        top = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);

        toptext.setAnimation(top);
        imageView5.setAnimation(top);
        register.setAnimation(bottom);
        forgot.setAnimation(bottom);
        textView7.setAnimation(bottom);
        login.setAnimation(bottom);
        inputEmail.setAnimation(top);
        inputPassword.setAnimation(top);


        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Bottomsheetlanguagefragment().show(getSupportFragmentManager(), "Dialog");
            }
        });


        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Forgotpassword.class));
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate(inputEmail, inputPassword)) {
                    progressDialog.show();


                    checkUser(inputEmail.getEditText().getText().toString(), inputPassword.getEditText().getText().toString());


                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                Pair[] pairs = new Pair[6];
                pairs[0] = new Pair<View, String>(toptext, "text1");
                pairs[1] = new Pair<View, String>(imageView5, "image");
                pairs[2] = new Pair<View, String>(inputEmail, "phone");
                pairs[3] = new Pair<View, String>(inputPassword, "password");
                pairs[4] = new Pair<View, String>(login, "go");
                pairs[5] = new Pair<View, String>(register, "switch");

                //wrap the call in API level 21 or higher
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
                    startActivity(intent, options.toBundle());
                }
                finish();
            }
        });

    }

    private boolean validate(TextInputLayout mobile, TextInputLayout password) {
        if (mobile.getEditText().getText().toString().isEmpty()) {
            mobile.setError("\nMobile cannot be empty\n");
            return false;
        } else {
            mobile.setError("");
            mobile.setErrorEnabled(false);
        }
        if (password.getEditText().getText().toString().isEmpty()) {
            password.setError("\nPassword cannot be empty\n");
            return false;
        } else {
            password.setError("");
            password.setErrorEnabled(false);
        }
        return true;
    }

    void checkUser(final String phone1, final String pass) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference("Users").child(phone1);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserRegistrationHelper helper = dataSnapshot.getValue(UserRegistrationHelper.class);
                if (helper != null) {
                    try {
                        if (helper.getPass().equals(encrypt(pass))) {
                            SharedPreferences pref;
                            SharedPreferences.Editor editor;

                            pref = getApplicationContext().getSharedPreferences("user", 0); // 0 - for private mode
                            editor = pref.edit();

                            editor.putString("user", phone1);
                            editor.apply();

                            startActivity(new Intent(Login.this, Home.class), ActivityOptions.makeSceneTransitionAnimation(Login.this).toBundle());
                            finish();
                        } else {
                            progressDialog.hide();
                            inputPassword.setError("Invalid Password");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    progressDialog.hide();
                    inputEmail.setError("Mobile number doesn't exists");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.hide();
                Toast.makeText(Login.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public void onBackPressed() {
        Exit1();
    }

    public void Exit1() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";

    public static String encrypt(String value) throws Exception {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;

    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        return key;
    }

}
