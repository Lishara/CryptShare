package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Rangana on 12/2/2017.
 */

public class Settings extends AppCompatActivity implements View.OnClickListener{

    private Button edit;
    private Button back;
    private TextView email,username;

    private ProgressDialog pd;
    private Bitmap bits;
    private ImageView image;

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        pd = new ProgressDialog(this);

        user = mAuth.getCurrentUser();

        edit = (Button) findViewById(R.id.editDetails);
        back = (Button) findViewById(R.id.exitProfile);
        image = (ImageView) findViewById(R.id.displayProfilePic);
        email = (TextView) findViewById(R.id.displayEmail);
        username = (TextView) findViewById(R.id.displayUName);

        String newem = user.getEmail().replace(".",",");
        setDetails(newem);

        back.setOnClickListener(this);
        edit.setOnClickListener(this);

    }

    public void onClick(View view){
        if(view == back){
            onBackPressed();
        }
        if(view == edit){
            finish();
            startActivity(new Intent(this, EditSettings.class));
        }
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,Menu.class);
        startActivity(i);
        finish();
    }

    public void setDetails(String uid){
        pd.setMessage("Loading profile, Please wait...");
        pd.show();

        DatabaseReference ref1 = mRef.child("users").child(uid).child("UserName");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uname = dataSnapshot.getValue(String.class);
                username.setText(uname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        email.setText(user.getEmail());

        DatabaseReference ref3 = mRef.child("users").child(uid).child("ProfilePic");
        ref3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String base = dataSnapshot.getValue(String.class);
                if(!base.equals("")){
                    bits = stringToBitMap(base);
                    image.setImageBitmap(bits);
                    pd.dismiss();
                }else{
                    pd.dismiss();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
