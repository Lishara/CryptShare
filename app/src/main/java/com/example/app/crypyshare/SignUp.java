package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * Created by Rangana on 11/29/2017.
 */

public class SignUp extends AppCompatActivity implements View.OnClickListener{

    private TextView passwordWrong;
    private TextView emailWrong;
    private Button backButton;
    private ProgressDialog progressDialog;

    private EditText Vusername;
    private EditText Vemail;
    private EditText Vpassword;
    private EditText Vconfirm;
    private Button next;

    private FirebaseAuth mAuth;
    private InternalDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        next = (Button) findViewById(R.id.next);
        db= new InternalDB(this);

        passwordWrong = (TextView) findViewById(R.id.passwordWrong);
        emailWrong = (TextView) findViewById(R.id.wrongEmail);
        progressDialog = new ProgressDialog(this);


        backButton = (Button) findViewById(R.id.back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignUp.this, MainActivity.class);
                startActivity(i);
            }
        });

        Vemail = (EditText) findViewById(R.id.email);
        Vpassword = (EditText) findViewById(R.id.password);
        Vconfirm = (EditText) findViewById(R.id.passwordConfirm);
        Vusername = (EditText) findViewById(R.id.userName);

        mAuth = FirebaseAuth.getInstance();

        Vemail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!Patterns.EMAIL_ADDRESS.matcher(Vemail.getText().toString()).matches()){
                    emailWrong.setVisibility(View.VISIBLE);
                }else{
                    emailWrong.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Vconfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordWrong.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Vpassword.getText().toString().equals(Vconfirm.getText().toString())) {
                    passwordWrong.setVisibility(View.INVISIBLE);
                }else{
                    passwordWrong.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        next.setOnClickListener(this);


    }
    public void onClick(View view){
        if(view==next){
            if(Vusername.getText().toString().equals("")){
                Toast.makeText(SignUp.this, "Please Enter a User Name!", Toast.LENGTH_SHORT).show();
            }
            else if(Vemail.getText().toString().equals("")){
                Toast.makeText(SignUp.this, "Please Enter an Email!", Toast.LENGTH_SHORT).show();
            }
            else if(Vpassword.getText().toString().equals("")){
                Toast.makeText(SignUp.this, "Please Enter a Password!", Toast.LENGTH_SHORT).show();
            }
            else if (Vconfirm.getText().toString().equals("")){
                Toast.makeText(SignUp.this, "Please Enter Confirmation Password!", Toast.LENGTH_SHORT).show();
            }
            else {
                progressDialog.setMessage("Registering..");
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(Vemail.getText().toString(), Vpassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                databaseWork();
                                startActivity(new Intent(getApplicationContext(), ConfirmationPage.class));
                            } else {
                                Toast.makeText(SignUp.this, "This email is already used!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
            }
        }
    }

    public void databaseWork(){
        db.truncateTemp();
        db.insertTempUserData(Vemail.getText().toString(), Vusername.getText().toString());
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }

}
