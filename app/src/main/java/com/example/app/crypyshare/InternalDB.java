package com.example.app.crypyshare;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Rangana on 11/30/2017.
 */

public class InternalDB extends SQLiteOpenHelper {

    public static final String database_name="cryptshare.db";
    public static final String table_name="friends";
    public static final String table_name2="tempuserdetails";

    public static final String col1="email";
    public static final String col2="uname";




    public InternalDB(Context context) {
        super(context, database_name, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+table_name+" (email text primary key, uname text)");
        db.execSQL("CREATE TABLE "+table_name2+" (email text, uname text)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if exists"+table_name);
        onCreate(db);
    }

    public void insertUserData(String email,String uname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col1,email);
        contentValues.put(col2,uname);
        db.insert(table_name,null ,contentValues);
    }

    public void insertTempUserData(String email,String uname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(col1,email);
        contentValues.put(col2,uname);

        db.insert(table_name2,null ,contentValues);
    }

    public void truncateTemp(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_name2,null,null);
    }

    public Cursor getTempData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+table_name2,null);
        return res;
    }
}
