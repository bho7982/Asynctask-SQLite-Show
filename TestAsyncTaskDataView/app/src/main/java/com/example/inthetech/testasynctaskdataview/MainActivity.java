package com.example.inthetech.testasynctaskdataview;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button ShowDataButton;
    Button SaveDataButton;
    Button ClearDataButton;
    EditText SaveDataEditText;
    TextView ShowDataTextView;
    TextView ShowSyncDataViewTextView;
    SQLiteDatabase sql;
    TestDB testDB;
    private AsyncTask<Void, Void, Void> mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testDB = new TestDB(this);

        SaveDataEditText = (EditText)findViewById(R.id.editText);
        ShowDataTextView = (TextView)findViewById(R.id.textView);
        ShowSyncDataViewTextView = (TextView)findViewById(R.id.textView3);

        SaveDataButton = (Button)findViewById(R.id.Button2);
        SaveDataButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {

                sql = testDB.getWritableDatabase();
                testDB.onUpgrade(sql, 1, 2);
                sql.execSQL("INSERT INTO member VALUES(null,'"
                        + SaveDataEditText.getText().toString() + "');"
                );
                sql.close();
            }
        });

        ShowDataButton = (Button) findViewById(R.id.button);
        ShowDataButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                sql = testDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);

                String TestShowString = "Data" + "\r\n";

                while (cursor.moveToNext())
                {
                    TestShowString += cursor.getString(1) + "\r\n";
                }
                ShowDataTextView.setText(TestShowString);

            }
        });

        ClearDataButton = (Button)findViewById(R.id.button3);
        ClearDataButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                sql = testDB.getWritableDatabase();
                testDB.onUpgrade(sql, 1, 2);
                sql.close();
            }
        });

        ShowDataTask();
    }

    public void ShowDataTask()
    {
        mTask = new AsyncTask<Void, Void, Void>() {
            String Test = "Test" + "\r\n";

            @Override
            protected Void doInBackground(Void... voids) {

                while (true)
                {
                    try
                    {
                        sql = testDB.getReadableDatabase();
                        Cursor cursor;
                        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);



                        while (cursor.moveToNext())
                        {
                            Test = cursor.getString(1);
                        }
                        cursor.close();
                        sql.close();

                        publishProgress();
                        Thread.sleep(1000);
                    }catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Void... progress)
            {
                ShowSyncDataViewTextView.setText(Test);
            }
        };
        mTask.execute();
    }
}
