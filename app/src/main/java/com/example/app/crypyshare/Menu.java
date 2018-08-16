package com.example.app.crypyshare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
 * Created by Rangana on 11/29/2017.
 */

public class Menu extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;


    private TextView Vhimsg;
    private Button VsendOption, VreceiveOption;
    public static String uname;
    private ProgressDialog pd;
    private Bitmap bits;
    private ImageView image;

    private FirebaseDatabase database;
    private DatabaseReference mRef;

    private FloatingActionButton fab_main,fab_one,fab_two,fab_three;
    private Animation FabOpen,FabClose,FabRClockwise,FabRAntiClockwise;
    private Boolean isOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        pd = new ProgressDialog(this);

        FirebaseUser user = mAuth.getCurrentUser();

        VsendOption = (Button) findViewById(R.id.sendFiles);
        VreceiveOption = (Button) findViewById(R.id.receivedFiles);
        Vhimsg = (TextView) findViewById(R.id.hiMessage);
        image = (ImageView) findViewById(R.id.imageView);

        fab_main = (FloatingActionButton) findViewById(R.id.fab_main);
        fab_one = (FloatingActionButton) findViewById(R.id.fab_settings);
        fab_two = (FloatingActionButton) findViewById(R.id.fab_logout);
        fab_three = (FloatingActionButton) findViewById(R.id.fab_delete);

        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        FabRAntiClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        String newem = user.getEmail().replace(".",",");
        setDetails(newem);

        VsendOption.setOnClickListener(this);
        VreceiveOption.setOnClickListener(this);
        fab_main.setOnClickListener(this);
        fab_one.setOnClickListener(this);
        fab_two.setOnClickListener(this);
        fab_three.setOnClickListener(this);

    }


    public void onClick(View view) {
        if (view == fab_two) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            mAuth.signOut();
                            finish();
                            startActivity(new Intent(Menu.this, MainActivity.class));
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Menu.this);
            builder.setMessage("Are you sure you want to logout?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
        if (view == VsendOption){
            Intent i = new Intent(this, Compose.class);
            finish();
            startActivity(i);
        }
        if(view == fab_one){
            Intent i = new Intent(this, Settings.class);
            finish();
            startActivity(i);
        }
        if (view == VreceiveOption){
            Intent i = new Intent(this, Receive.class);
            finish();
            startActivity(i);
        }
        if(view == fab_main){
            if(isOpen){
                fab_one.startAnimation(FabClose);
                fab_two.startAnimation(FabClose);
                fab_three.startAnimation(FabClose);
                fab_main.startAnimation(FabRAntiClockwise);
                fab_one.setClickable(false);
                fab_two.setClickable(false);
                fab_three.setClickable(false);
                isOpen = false;
            }else{
                fab_one.startAnimation(FabOpen);
                fab_two.startAnimation(FabOpen);
                fab_three.startAnimation(FabOpen);
                fab_main.startAnimation(FabRClockwise);
                fab_one.setClickable(true);
                fab_two.setClickable(true);
                fab_three.setClickable(true);
                isOpen = true;
            }
        }
        if(view == fab_three){

        }
    }

    public void setDetails(String uid){
        pd.setMessage("Loading profile, Please wait...");
        pd.show();

        DatabaseReference ref1 = mRef.child("users").child(uid).child("UserName");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uname = dataSnapshot.getValue(String.class);
                Vhimsg.setText("Hi.. "+uname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref2 = mRef.child("users").child(uid).child("ProfilePic");
        ref2.addValueEventListener(new ValueEventListener() {
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