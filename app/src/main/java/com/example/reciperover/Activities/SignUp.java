package com.example.reciperover.Activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.reciperover.R.*;

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

import com.example.reciperover.Models.User;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUp extends AppCompatActivity {
    private EditText user_name, user_Email1, user_pass1, cnfrm_pass;
    String name, email, password, confirmPassword;
    private Button sign_up;
    private TextView sign_in_txt;
    public  static final int RC_SIGN_IN = 1;
    FirebaseAuth fAuth;
    CardView google_btn_card;
    ProgressBar signUpProgress;
    FirebaseFirestore fireStore;
    String userID;
    User u;
    GoogleSignInClient mgoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient = GoogleSignIn.getClient(this,gso);


        sign_in_txt.setOnClickListener(v -> {onBackPressed();});
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                User u = userData();
                if (isValid(u)) {
                    registerUser(u);
                }
            }
        });
        google_btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
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
                            startActivity(new Intent(SignUp.this, Home.class));
                            finish();
                        }else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }
    void init() {
        u = new User();
        user_name = findViewById(R.id.user_name);
        user_Email1 = findViewById(R.id.user_Email1);
        user_pass1 = findViewById(R.id.user_pass1);
        cnfrm_pass = findViewById(R.id.cnfrm_pass);
        sign_up = findViewById(R.id.sign_up);
        fAuth = FirebaseAuth.getInstance();
        sign_in_txt = findViewById(R.id.sign_in_txt);
        fireStore = FirebaseFirestore.getInstance();
        signUpProgress = findViewById(R.id.signUpProgress);
        google_btn_card = findViewById(R.id.google_btn_card);
    }

    User userData(){
        name = user_name.getText().toString().trim();
        email = user_Email1.getText().toString().trim();
        password = user_pass1.getText().toString().trim();
        confirmPassword = cnfrm_pass.getText().toString().trim();

        u.setName(name);
        u.setEmail(email);
        u.setBlock(false);

        return u;
    }

    boolean isValid(User u) {

        if (u.getName().length() <= 4 || u.getName() == null) {
            user_name.setError("Enter valid name");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(u.getEmail()).matches() || u.getEmail().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter a Valid Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6 ) {
            Toast.makeText(getApplicationContext(), "Enter a Valid Password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!confirmPassword.matches(password)) {
            Toast.makeText(getApplicationContext(), "Confirm Password Does Not Match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    void registerUser(User u) {
        fAuth.createUserWithEmailAndPassword(u.getEmail(), password)
                .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userID = fAuth.getCurrentUser().getUid();
                            u.setuId(userID);
                            saveUserDetail(u);
                        } else {
                            Toast.makeText(SignUp.this, "Registration Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void saveUserDetail(User u){
        signUpProgress.setVisibility(View.VISIBLE);
        sign_up.setVisibility(View.INVISIBLE);

        fireStore
                .collection("Users")
                .document(u.getuId())
                .set(u)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"onSuccess: user profile is created for "+u.getuId());
                            signUpProgress.setVisibility(View.INVISIBLE);
                            sign_up.setVisibility(View.VISIBLE);
                            goToHome();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        signUpProgress.setVisibility(View.INVISIBLE);
                        sign_up.setVisibility(View.VISIBLE);
                        Log.d(TAG,"onFailure: "+ e.getMessage());
                    }
                });

    }
    private void goToHome(){
        startActivity(new Intent(getApplicationContext(),Home.class));
    }


}