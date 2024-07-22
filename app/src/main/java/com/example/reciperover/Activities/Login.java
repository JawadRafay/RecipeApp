package com.example.reciperover.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reciperover.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private EditText user_Email, user_pass;
    CardView google_btn_card;
    private Button sign_in;
    String email, password;
    FirebaseAuth fAuth;
    TextView sign_up_txt,forgot_pass;
    ProgressBar loginProgress;
    public  static final int RC_SIGN_IN = 1;
    public  static final String TAG = "GoogleAuth";
    GoogleSignInClient mgoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        //Configure Google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(this,gso);


        //Getting google button click
        google_btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        forgot_pass.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ForgotPassword.class)));
        sign_up_txt.setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    signInUser(email,password);
                }
            }
        });

    }
    void init() {
        sign_up_txt = findViewById(R.id.sign_up_txt);
        fAuth = FirebaseAuth.getInstance();
        user_Email = findViewById(R.id.user_Email);
        user_pass = findViewById(R.id.user_pass);
        sign_in = findViewById(R.id.sign_in);
        forgot_pass = findViewById(R.id.forgot_pass);
        loginProgress = findViewById(R.id.loginProgress);
        google_btn_card = findViewById(R.id.google_btn_card);
    }
    private void signIn()
    {
        Intent signInIntent = mgoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e){

            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fAuth.getCurrentUser();
                            startActivity(new Intent(Login.this, Home.class));
                            finish();
                        }else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
    boolean isValid() {
        email = user_Email.getText().toString().trim();
        password = user_pass.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter a Valid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6 || password == null) {
            Toast.makeText(getApplicationContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    void signInUser(String email,String password){
        loginProgress.setVisibility(View.VISIBLE);
        sign_in.setVisibility(View.INVISIBLE);
        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            loginProgress.setVisibility(View.INVISIBLE);
                            sign_in.setVisibility(View.VISIBLE);
                            goToHomeScreen();
                        }else {
                            Toast.makeText(getApplicationContext(), "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loginProgress.setVisibility(View.INVISIBLE);
                        sign_in.setVisibility(View.VISIBLE);
                        Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    void goToHomeScreen(){
        Toast.makeText(getApplicationContext(), "LoggedIn Successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), Home.class));
        finish();
    }
}