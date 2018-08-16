package com.example.app.crypyshare;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Rangana on 1/4/2018.
 */

public class Receive extends AppCompatActivity implements OnItemClickListener{

    TypedArray profile_pics;

    ArrayList<String> e = new ArrayList<String>();
    ArrayList f = new ArrayList();
    ArrayList p = new ArrayList();
    ArrayList ke = new ArrayList();
    ArrayList exten = new ArrayList();

    List<RowItem> rowItems;
    ListView mylistview;

    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DownloadManager dm;
    private File dir;
    private ProgressDialog pd;

    String fname;
    String ext;
    String key;
    String path;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reveivefiles);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        File sdcard = Environment.getExternalStorageDirectory();
        dir = new File(sdcard.getAbsolutePath() +"/CryptShare/");
        dir.mkdir();

        check();

        rowItems = new ArrayList<RowItem>();

        profile_pics = getResources().obtainTypedArray(R.array.profile_pics);

//



    }

    public void check(){
        String newem = mAuth.getCurrentUser().getEmail().replace(".",",");


        myRef.child("Sharing").child(newem).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    String ema = postSnapshot.child("UserName").getValue().toString();
                    String fn = (String) postSnapshot.child("Name").getValue();
//                    String fp = (String) postSnapshot.child("Filepath").getValue();
//                    String ex = (String) postSnapshot.child("Extension").getValue();
//                    String k = (String) postSnapshot.child("Key").getValue();

                    f.add(fn);
                    e.add(ema);
//                    p.add(fp);
//                    exten.add(ex);
//                    ke.add(k);
                }
                if (e.size() != 0){
                    for (int i = 0; i < e.size(); i++) {
                        RowItem item = new RowItem(e.get(i).toString(),
                                profile_pics.getResourceId(i, -1), f.get(i).toString());
                        rowItems.add(item);
                    }

                    mylistview = (ListView) findViewById(R.id.list);
                    CustomAdapter adapter = new CustomAdapter(Receive.this, rowItems);
                    mylistview.setAdapter(adapter);

                    mylistview.setOnItemClickListener(Receive.this);
                }
                else{
                    Toast.makeText(Receive.this,"No Files Available!",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        fname = rowItems.get(position).getStatus();
//        Toast.makeText(Receive.this,name,Toast.LENGTH_SHORT).show();
        String newem = mAuth.getCurrentUser().getEmail().replace(".",",");
//        Toast.makeText(Receive.this,newem,Toast.LENGTH_SHORT).show();

        DatabaseReference ref2 = myRef.child("Sharing").child(newem).child(fname).child("Extension");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ext = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref3 = myRef.child("Sharing").child(newem).child(fname).child("Key");
        ref3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                key = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference ref = myRef.child("Sharing").child(newem).child(fname).child("Filepath");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                path = dataSnapshot.getValue(String.class);
//                Toast.makeText(Receive.this,path,Toast.LENGTH_SHORT).show();
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                download(path);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(Receive.this);
                builder.setMessage("Are you sure you want to download this file?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void download(String path){
        pd.setMessage("Downloading..Please wait");
        pd.show();
        dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(path));

        request.setDestinationInExternalPublicDir("/CryptShare/","file.encrypted");
        Long reference = dm.enqueue(request);

    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            pd.dismiss();
            decrypt();
            AlertDialog.Builder builder = new AlertDialog.Builder(Receive.this);
            builder.setMessage("Download Complete! Please check the CryptShare folder..")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/CryptShare/");
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(selectedUri, "resource/folder");

                            if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
                            {
                                startActivity(intent);
                            }
                            else
                            {
                                // if you reach this place, it means there is no any file
                                // explorer app installed on your device
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    };

    public void decrypt(){
        File encryptedFile = new File(dir, "file.encrypted");
        File decryptedFile = new File(dir, fname+"."+ext);
        try {
            fileProcessor(Cipher.DECRYPT_MODE,key,encryptedFile,decryptedFile);
            encryptedFile.delete();
        } catch (Exception ex) {

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

        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Receive.this,Menu.class));
        finish();
    }
}
