package com.example.inthetech.testasynctaskdataview;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by inthetech on 2017-01-16.
 */

public class TestDB extends SQLiteOpenHelper{
    public TestDB(Context context) {super(context, "TestDB", null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table member (_id char(10), air char(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS member");
        onCreate(db);
    }
}