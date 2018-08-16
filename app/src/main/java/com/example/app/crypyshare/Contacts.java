package com.example.app.crypyshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by Rangana on 11/30/2017.
 */

public class Contacts extends AppCompatActivity implements View.OnClickListener{

    private Button Vcompose;
    private RelativeLayout host;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendfiles2);

        Vcompose = (Button) findViewById(R.id.compose);
        host = (RelativeLayout) findViewById(R.id.host2);

        Vcompose.setOnClickListener(this);

        host.setOnTouchListener(new OnSwipeTouchListener(Contacts.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
                Intent i = new Intent(Contacts.this, Compose.class);
                startActivity(i);
            }
            public void onSwipeLeft() {

            }
            public void onSwipeBottom() {

            }

        });

    }

    public void onClick(View view) {
        if (view == Vcompose) {
            Intent i = new Intent(this, Compose.class);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,Compose.class);
        startActivity(i);
        finish();
    }
}
