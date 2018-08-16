package com.example.app.crypyshare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Rangana on 11/30/2017.
 */

public class Compose extends AppCompatActivity{

    private Button Vcontacts,selectFile,sendFile;
    private RelativeLayout host;
    private ProgressDialog pd;
    private Uri file,downlink;
    private String ext;
    private EditText filename,targetemails;
    private TextView filepath;
    private String p;

    private StorageReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference Ref;

    private File dir;
    private File encryptedFile;

    private static final int SELECT_FILE = 1;
    private String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendfiles1);

        String characters =  "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int len = characters.length();
       // key = "";

        for (int i = 0; i < 16; i++){
            int n = (int)(Math.random() * 100)%len;
            key+= characters.charAt(n);
        }

        Vcontacts = (Button) findViewById(R.id.contacts);
        selectFile = (Button) findViewById(R.id.selectFile);
        sendFile = (Button) findViewById(R.id.sendFile);
        host = (RelativeLayout) findViewById(R.id.host1);
        filepath = (TextView) findViewById(R.id.filePath);
        filename = (EditText) findViewById(R.id.fileName);
        targetemails = (EditText) findViewById(R.id.targetEmails);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        Ref = database.getReference();

        pd = new ProgressDialog(this);

        user = mAuth.getCurrentUser();
        mRef = FirebaseStorage.getInstance().getReference();

        File sdcard = Environment.getExternalStorageDirectory();
        dir = new File(sdcard.getAbsolutePath() +"/CryptShare/");
        dir.mkdir();

        Vcontacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Compose.this, Contacts.class);
                startActivity(i);
            }
        });

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
            }
        });

        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file != null){
                    encrypt();
                }else{
                    Toast.makeText(Compose.this,"No file Selected",Toast.LENGTH_SHORT).show();
                }
            }
        });

        host.setOnTouchListener(new OnSwipeTouchListener(Compose.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {

            }
            public void onSwipeLeft() {
                Intent i = new Intent(Compose.this, Contacts.class);
                startActivity(i);
            }
            public void onSwipeBottom() {

            }

        });


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                file = data.getData();
                String arr[] = getContentResolver().getType(file).split("/");
                ext = arr[1];

                File sdcard = Environment.getExternalStorageDirectory();

                String arr1[] = file.getPath().split(":");
                p = sdcard.getAbsolutePath()+"/"+arr1[1];
                filepath.setText(p);
            }
        }
    }


    public void saveFile(Uri files){
        if (files != null) {
            pd.setMessage("Uploading..");
            pd.show();
            StorageReference storageRef = mRef.child(System.currentTimeMillis()+".encrypted");
            storageRef.putFile(files).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downlink = taskSnapshot.getDownloadUrl();
                    pd.dismiss();
                    saveFirebase();
                    encryptedFile.delete();
                    Toast.makeText(Compose.this, "Successful", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(Compose.this, "Failure", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    pd.setMessage("Uploading " + (int) progress + "%");
                }
            });
        }else{
            Toast.makeText(Compose.this, "Please select a file to Upload!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){
        Intent i = new Intent(this,Menu.class);
        startActivity(i);
        finish();
    }

    public void encrypt(){
        File inputFile = new File(p);
        encryptedFile = new File(dir, "file.encrypted");
        try {
            fileProcessor(Cipher.ENCRYPT_MODE, key, inputFile, encryptedFile);
//            Toast.makeText(Compose.this, "Encrypted", Toast.LENGTH_SHORT).show();
            saveFile(Uri.fromFile(encryptedFile));


        } catch (Exception ex) {
//            Toast.makeText(Compose.this, "Failure to Encrypt", Toast.LENGTH_SHORT).show();
        }
    }

    public void fileProcessor(int cipherMode, String key, File inputFile, File outputFile) {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
//            Toast.makeText(Compose.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void saveFirebase(){
        String name = filename.getText().toString().trim();
        String x = targetemails.getText().toString()+",";
        String arr[] = x.split(",");

        for(int i=0; i<arr.length; i++){
            String id = arr[i].replace(".",",");
            DatabaseReference c1 = Ref.child("Sharing").child(id).child(name);
            c1.child("Name").setValue(name);
            c1.child("Filepath").setValue(downlink.toString());
            c1.child("Key").setValue(key);
            c1.child("UserName").setValue(Menu.uname);
            c1.child("Extension").setValue(ext);
        }
    }

}
