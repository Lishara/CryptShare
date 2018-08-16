package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private Button signin;
    private EditText Vemail;
    private EditText Vpass;
    private TextView signup;
    private TextView Vforgotp;
    private boolean exit=false;
    private InternalDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = new InternalDB(this);


        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            if(user.isEmailVerified()) {
                finish();
                startActivity(new Intent(this, Menu.class));
            }
        }

        progressDialog = new ProgressDialog(this);
        Vemail = (EditText) findViewById(R.id.userName);
        Vpass = (EditText) findViewById(R.id.password);
        signin = (Button) findViewById(R.id.signIn);
        signup = (TextView) findViewById(R.id.signUp);
        Vforgotp = (TextView) findViewById(R.id.forgotPassword);

        signin.setOnClickListener(this);
        Vforgotp.setOnClickListener(this);
        signup.setOnClickListener(this);
    }

    public void onClick(View view) {
        if(view == Vforgotp){
            if(Vemail.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "Please provide your email", Toast.LENGTH_SHORT).show();
            }
            else{
                progressDialog.setMessage("Sending password reset details to your email..");
                progressDialog.show();

                mAuth.sendPasswordResetEmail(Vemail.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, "Successfully sent reset code..please check mail", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "Failure sending", Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }
                        });
            }
        }

        if (view == signin) {
            if(Vemail.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "Please provide your email", Toast.LENGTH_SHORT).show();
            }
            else if(Vpass.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "Please provide the password", Toast.LENGTH_SHORT).show();
            }
            else {
                progressDialog.setMessage("Logging in, Please wait...");
                progressDialog.show();
                mAuth.signInWithEmailAndPassword(Vemail.getText().toString(), Vpass.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if(mAuth.getCurrentUser().isEmailVerified()) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(MainActivity.this, "Your email or password is incorrect!! ", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Intent i = new Intent(MainActivity.this, Menu.class);
                                        finish();
                                        startActivity(i);
                                    }
                                }else{
                                    Toast.makeText(MainActivity.this, "Your email s not verified!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        if (view == signup) {
            Intent i = new Intent(MainActivity.this, SignUp.class);
            finish();
            startActivity(i);
        }

    }

    @Override
    public void onBackPressed(){
        if(exit){
            finish();
        } else {
            Toast.makeText(this, "Press Back again to exit CryptShare", Toast.LENGTH_SHORT).show();
            exit=true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit=false;
                }
            },3*1000);
        }
    }

}
