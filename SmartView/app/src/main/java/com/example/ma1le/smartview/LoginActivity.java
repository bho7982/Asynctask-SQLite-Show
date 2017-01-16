package com.example.ma1le.smartview;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {


    Button button;
    EditText etId;
    EditText etPw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        button = (Button)findViewById(R.id.btnLogin);
        etId = (EditText)findViewById(R.id.etId);
        etPw = (EditText)findViewById(R.id.etPw);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://119.202.196.158/login.php";
                url += "?id=";
                url += etId.getText();
                url += "&pw=";
                url += etPw.getText();

                Log.d("연결", "여기1");

                try {

                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = conManager.getActiveNetworkInfo();
                    Log.d("연결", "여기2");
                    if (netInfo != null && netInfo.isConnected()) {
                        Log.d("연결", "여기3");
                        new DownloadJson().execute(url);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "네트워크에 연결되지 않았습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                    finish();

                }
            }
        });


    }


    private class DownloadJson extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... arg0) {
            try {
                Log.d("연결", "여기4");
                return (String) getData((String) arg0[0]);
            } catch (Exception e) {
                return "Json download failed";

            }
        }

        protected void onPostExecute(final String result) {
            int index;
            LoginInfo.LoginName = "";
            LoginInfo.LoginAge = "";
            LoginInfo.LoginId = "";
            if (result.equals("Json download failed")) {
                Toast.makeText(getApplicationContext(), "서버와 연결할 수 없습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                finish();
                return;
            }
            try {
                JSONObject jObject = new JSONObject(result);
                JSONArray Jarray = jObject.getJSONArray("result");
                for (index = 0; index < Jarray.length(); index++) {
                    JSONObject buffer = Jarray.getJSONObject(index); // JSONObject 추출
                    LoginInfo.LoginId = buffer.getString("ID");
                    LoginInfo.LoginName = buffer.getString("Name");
                    LoginInfo.LoginAge = buffer.getString("Age");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "서버에서 알 수 없는 응답을 하였습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                finish();
            }

            if(LoginInfo.LoginName.equals("")){
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(new Intent(getApplicationContext(), com.example.ma1le.smartview.View.class));
            finish();

        }

        private String getData(String strUrl) {
            StringBuilder sb = new StringBuilder();
            Log.d("연결", "여기5");
            try {
                BufferedInputStream bis = null;
                URL url = new URL(strUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                int responseCode;

                con.setConnectTimeout(3000);
                con.setReadTimeout(3000);
                Log.d("연결", "여기6");
                responseCode = con.getResponseCode();
                Log.d("연결", "여기7 " + responseCode);
                if (responseCode == 200) {
                    Log.d("연결", "여기8");
                    bis = new BufferedInputStream(con.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(bis, "UTF-8"));
                    String line = null;

                    while ((line = reader.readLine()) != null)
                        sb.append(line);

                    bis.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return sb.toString();
        }
    }
}
