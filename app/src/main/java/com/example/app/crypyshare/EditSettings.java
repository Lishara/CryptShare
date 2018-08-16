package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Rangana on 12/2/2017.
 */

public class EditSettings extends AppCompatActivity implements View.OnClickListener{

    private Button back;
    private Button selectImage;
    private Button save;
    private static final int SELECT_PICTURE = 1;

    private Uri selectedImageUri;
    private ImageView img;
    private ProgressDialog pd;
    private Bitmap bits;
    private EditText username,newpass,confpass;

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        pd = new ProgressDialog(this);

        user = mAuth.getCurrentUser();

        String newem = user.getEmail().replace(".",",");
        setDetails(newem);

        back = (Button) findViewById(R.id.back);
        save = (Button) findViewById(R.id.save);
        selectImage = (Button) findViewById(R.id.changePic);
        img = (ImageView)findViewById(R.id.profilePictire);
        username = (EditText) findViewById(R.id.editUserName);
        newpass = (EditText) findViewById(R.id.newPassword);
        confpass = (EditText) findViewById(R.id.confirmNewPassword);

        back.setOnClickListener(this);
        selectImage.setOnClickListener(this);
        save.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == back){
            onBackPressed();
        }
        if(view == selectImage){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);
        }
        if(view == save){
            saveData();
        }
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,Settings.class);
        startActivity(i);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                img.setImageURI(selectedImageUri);
                saveImageFirebase(selectedImageUri);
            }
        }
    }

//    public String getPath(Uri uri) {
//        String[] projection = { MediaStore.Images.Media.DATA };
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
//    }

    public void saveImageFirebase(Uri image){
        try {
            Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bYtE);
            bmp.recycle();
            byte[] byteArray = bYtE.toByteArray();
            final String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

            String newem = user.getEmail().replace(".",",");
            DatabaseReference ref = mRef.child("users").child(newem).child("ProfilePic");
            ref.setValue(imageFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
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

        DatabaseReference ref2 = mRef.child("users").child(uid).child("ProfilePic");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String base = dataSnapshot.getValue(String.class);
                if(!base.equals("")){
                    bits = stringToBitMap(base);
                    img.setImageBitmap(bits);
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

    public void saveData(){
        if(newpass.getText().toString().equals("") && confpass.getText().toString().equals("")){
            saveUser();
        }else{
            pd.setMessage("Saving changes..");
            pd.show();
            String newPassword = confpass.getText().toString();
            saveUser();
            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        pd.dismiss();
                        Toast.makeText(EditSettings.this,"Saved Settings!",Toast.LENGTH_SHORT).show();
                    }else{
                        pd.dismiss();
                        Toast.makeText(EditSettings.this,"Failure!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void saveUser(){
        String un = username.getText().toString();
        String newem = user.getEmail().replace(".",",");
        DatabaseReference db = mRef.child("users").child(newem).child("UserName");
        db.setValue(un);
    }


}
