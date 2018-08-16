package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Rangana on 12/1/2017.
 */

public class ConfirmationPage extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private Button resendCode;
    private Button BtnSignUp;
    private Button back;

    private String un;
    private String em;
    private TextView viewname;
    private TextView viewemail;

    private ProgressDialog progressDialog;
    private InternalDB db;
    private Cursor res;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmation_screen);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        db= new InternalDB(this);

        BtnSignUp = (Button) findViewById(R.id.signUp);
        resendCode = (Button) findViewById(R.id.resend);
        back = (Button) findViewById(R.id.back);
        viewname = (TextView) findViewById(R.id.unameEntered);
        viewemail = (TextView) findViewById(R.id.emailEntered);

        res = db.getTempData();
        while (res.moveToNext()){
            em=res.getString(0);
            un=res.getString(1);
        }
        viewname.setText(un);
        viewemail.setText(em);
        saveToFirebase();
        mAuth.getCurrentUser().sendEmailVerification();

        resendCode.setOnClickListener(this);
        back.setOnClickListener(this);
        BtnSignUp.setOnClickListener(this);
    }

    public void onClick(View view){
        if(view == resendCode){
            progressDialog.setMessage("Sending Code..");
            progressDialog.show();
            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(ConfirmationPage.this, "Code sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ConfirmationPage.this, "Unexpected Error!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(view == back){
            mAuth.getCurrentUser().delete();
            finish();
            startActivity(new Intent(ConfirmationPage.this, SignUp.class));
        }
        if (view == BtnSignUp){
            mAuth.signOut();
            finish();
            startActivity(new Intent(ConfirmationPage.this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }

    public void saveToFirebase(){
        String newem = mAuth.getCurrentUser().getEmail().replace(".",",");

        DatabaseReference ref = myRef.child("users");
        DatabaseReference refParent = ref.child(newem);
        DatabaseReference child1 = refParent.child("UserName");
        child1.setValue(un);
        DatabaseReference child2 = refParent.child("ProfilePic");
        child2.setValue("");
    }
}
