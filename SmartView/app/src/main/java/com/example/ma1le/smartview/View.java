package com.example.ma1le.smartview;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class View extends AppCompatActivity implements BeaconConsumer, SurfaceHolder.Callback, SensorEventListener {


    SearchLocation location;

    EditText findEditText;

    TextView textView;

    TextView txtView33;

    public ArrayList<String> mUserNameArrayList = new ArrayList<String>();
    public android.app.AlertDialog mUserListDialog;
    private Menu mMenu;
    phpDown task;

    boolean findmode = false;
    Camera camera; // camera class variable
    SurfaceView camView; // drawing camera preview using this variable
    SurfaceHolder surfaceHolder; // variable to hold surface for surfaceView which means display
    boolean camCondition = false;  // conditional variable for camera preview checking and set to false
    Button cap;    // image capturing button
    //    static TextView textView2;
    private BeaconManager beaconManager;
    // 감지된 비콘들을 임시로 담을 리스트
    private List<Beacon> beaconList = new ArrayList<>();
    //    static TextView textView;
    Button find;

    private SensorManager sm;
    private Sensor s;
    private ImageView imageview01;

    double center_X = 0;
    double center_Y = 0;
    double touch_X = 0;
    double touch_Y = 0;
    double radian;
    double degree;
    double error = 95;
    //실제 방위와 좌표기준상 방위의 오차, 실측해보고 좌표에 맞춰 0으로 맞춰지도록 계산할 것


    double rangeAera = 0.1;
    //목적지 근처의 범위율, 이 범위 안에 들어갔을때 목적지 도착.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        task = new phpDown();
        task.execute("http://119.202.196.158/findfriend.php");

        findEditText = (EditText) findViewById(R.id.editText2);
        find = (Button) findViewById(R.id.button2);
        textView = (TextView) findViewById(R.id.textView);

        Toast.makeText(getApplicationContext(), LoginInfo.LoginName + " : 로그인 성공", Toast.LENGTH_SHORT).show();

        FirebaseInstanceIDService ID = new FirebaseInstanceIDService();
        String tokenId = ID.tokenReturn();
        Toast.makeText(getApplicationContext(), tokenId, Toast.LENGTH_SHORT).show();

        try {
            String url = "http://119.202.196.158/register.php";
            url += "?Id=" + LoginInfo.LoginId;
            url += "&Token=" + tokenId;
            ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conManager.getActiveNetworkInfo();
            Log.d("연결", "여기2");
            if (netInfo != null && netInfo.isConnected()) {
                Log.d("연결", "여기3");
                new orderSend().execute(url);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch(Exception ex){
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), "네트워크에 연결되지 않았습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
            finish();

        }


        // 실제로 비콘을 탐지하기 위한 비콘매니저 객체를 초기화
        beaconManager = BeaconManager.getInstanceForApplication(this);
//        textView = (TextView)findViewById(R.id.textView1);
//        textView2 = (TextView)findViewById(R.id.textView2);

        // 여기가 중요한데, 기기에 따라서 setBeaconLayout 안의 내용을 바꿔줘야 하는듯 싶다.
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        // 비콘 탐지를 시작한다. 실제로는 서비스를 시작하는것.
        beaconManager.bind(this);


        getWindow().setFormat(PixelFormat.UNKNOWN);
        // refering the id of surfaceView
        camView = (SurfaceView) findViewById(R.id.camerapreview);
        // getting access to the surface of surfaceView and return it to surfaceHolder
        surfaceHolder = camView.getHolder();
        // adding call back to this context means MainActivity
        surfaceHolder.addCallback(this);
        // to set surface type
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);


        Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera c) {

                FileOutputStream outStream = null;
                try {

                    // Directory and name of the photo. We put system time
                    // as a postfix, so all photos will have a unique file name.
                    outStream = new FileOutputStream("/sdcard/AndroidCodec_" +
                            System.currentTimeMillis() + ".jpg");
                    outStream.write(data);
                    outStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

            }
        };

        handler.sendEmptyMessage(0);

        find.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String url = "http://119.202.196.158/findlocation.php";
                url += "?find=";
                url += findEditText.getText().toString();
                Log.d("연결", "여기1");

                try {
                    ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = conManager.getActiveNetworkInfo();
                    Log.d("연결", "여기2");
                    if (netInfo != null && netInfo.isConnected()) {
                        Log.d("연결", "여기3");
                        new View.DownloadJson().execute(url);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "네트워크에 연결되지 않았습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                    finish();

                }
            }
        });


        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        s = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION); // 방향센서
        imageview01 = (ImageView) findViewById(R.id.imageView01);
        imageview01.setVisibility(android.view.View.INVISIBLE);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ShowFriendsList_Menu) {
            initUserListDialog();
            mUserListDialog.show();
            return true;
        } else {
            return true;
        }
    }


    public void initUserListDialog() {

        String Finduser = " ";

        String url = "http://119.202.196.158/findfriend.php";
        url += "?find=";
        url += Finduser;




        final String[] items = new String[location.searchLocationList.size()];
        int i = 0;
        for(Position position : location.searchLocationList){
            items[i++] = position.Name;
        }


        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Select User");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] str = new String(items[which]).split(",");

                    for(Position position : location.searchLocationList){
                        if(position.Name ==  items[which]){
                            touch_X = Double.parseDouble(position.Position_X);
                            touch_Y = Double.parseDouble(position.Position_Y);
                            findmode = true;
                            break;
                        }
                    }

                    Toast.makeText(getApplicationContext(), "방향을 안내합니다 >>>  " + touch_X + ":" + touch_Y, Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mUserListDialog = builder.create();
        mUserListDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onResume() { // 화면에 보이기 직전에 센서자원 획득
        super.onResume();
        // 센서의 값이 변경되었을 때 콜백 받기위한 리스너를 등록한다
        sm.registerListener(this,        // 콜백 받을 리스너
                s,            // 콜백 원하는 센서
                SensorManager.SENSOR_DELAY_UI); // 지연시간
    }

    public void findClick(View view) {


    }


    @Override
    protected void onPause() { // 화면을 빠져나가면 즉시 센서자원 반납해야함!!
        super.onPause();
        sm.unregisterListener(this); // 반납할 센서
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        double dx = center_X - touch_X;
        double dy = center_Y - touch_Y;
        radian = Math.atan2(dx, dy);
        degree = Math.toDegrees(radian);

        // center x,y가 내 지점, touch x,y 목표지점


        // 목적지 근처라면 여기에 들어옴.
        if (findmode && touch_X - rangeAera < center_X && center_X < touch_X + rangeAera && touch_Y - rangeAera < center_Y && center_Y < touch_Y + rangeAera) {
            Toast.makeText(getApplicationContext(), "목적지 근처입니다.", Toast.LENGTH_SHORT).show();
            findmode = false;
            imageview01.setVisibility(android.view.View.INVISIBLE);
        }
        // 센서값이 변경되었을 때 호출되는 콜백 메서드
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION && findmode) {
            // 방향센서값이 변경된거라면

            double newDegree;


            if(degree <= 180 && degree >= 0){

            }else if(degree < 0){
                degree += 360;
            }

            newDegree = degree + 90;


            int A = (int) (event.values[0] + newDegree + error);

            if(A >= 360){
                A -= 360;
            }else if(A < 0){
                A += 360;
            }


            //textView.append("\n\n 계산각도 : " + newDegree + "\n 보정각도 : " + A);

            if ((int) event.values[1] <= 0 && (int) event.values[1] > -90) {
                imageview01.setVisibility(android.view.View.VISIBLE);

                if (265 > A && A >= 100) {
                    imageview01.setImageResource(R.drawable.down);
                } else if (285 > A && A >= 265) {
                    imageview01.setImageResource((R.drawable.right00));
                } else if (295 > A && A >= 285) {
                    imageview01.setImageResource(R.drawable.right01);
                } else if (305 > A && A >= 295) {
                    imageview01.setImageResource(R.drawable.right02);
                } else if (315 > A && A >= 305) {
                    imageview01.setImageResource(R.drawable.right03);
                } else if (325 > A && A >= 315) {
                    imageview01.setImageResource(R.drawable.right04);
                } else if (335 > A && A >= 325) {
                    imageview01.setImageResource(R.drawable.right06);
                } else if (345 > A && A >= 335) {
                    imageview01.setImageResource(R.drawable.right07);
                } else if (355 > A && A >= 345) {
                    imageview01.setImageResource(R.drawable.right08);
                } else if (15 > A && A >= 5) {
                    imageview01.setImageResource(R.drawable.left07);
                } else if (25 > A && A >= 15) {
                    imageview01.setImageResource(R.drawable.left06);
                } else if (35 > A && A >= 25) {
                    imageview01.setImageResource(R.drawable.left05);
                } else if (45 > A && A >= 35) {
                    imageview01.setImageResource(R.drawable.left04);
                } else if (55 > A && A >= 45) {
                    imageview01.setImageResource(R.drawable.left03);
                } else if (65 > A && A >= 55) {
                    imageview01.setImageResource(R.drawable.left02);
                } else if (75 > A && A >= 65) {
                    imageview01.setImageResource(R.drawable.left01);
                } else if (95 > A && A >= 75) {
                    imageview01.setImageResource(R.drawable.left00);
                } else {
                    imageview01.setImageResource(R.drawable.up);
                }

            } else {
                imageview01.setVisibility(android.view.View.INVISIBLE);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서의 정확도가 변경되었을 때 호출되는 콜백 메서드
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));

        } catch (RemoteException e) {
        }
    }

    // 버튼이 클릭되면 textView 에 비콘들의 정보를 뿌린다.


    public void OnButtonClicked(android.view.View view) {
        // 아래에 있는 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
        // 1초마다 불러온다.
        handler.sendEmptyMessage(0);


    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            textView.setText("");
//            비콘리스트 출력할걸 초기화

            // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.

            LocationCalculation LC = new LocationCalculation();
            textView.setText(LC.Calculation(beaconList, BeaconInfo.DBbeaconList));
            NowLocation nowLocation = LC.CalculationLocation();
            center_X = nowLocation.get_X();
            center_Y = nowLocation.get_Y();

            try {
                String url = "http://119.202.196.158/userlocation.php";
                url += "?Id=" + LoginInfo.LoginId;
                url += "&Lx=" + String.valueOf(center_X);
                url += "&Ly=" + String.valueOf(center_Y);
                ConnectivityManager conManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = conManager.getActiveNetworkInfo();
                Log.d("연결", "여기2");
                if (netInfo != null && netInfo.isConnected()) {
                    Log.d("연결", "여기3");
                    new orderSend().execute(url);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "네트워크 연결 실패", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } catch(Exception ex){
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), "네트워크에 연결되지 않았습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                finish();

            }


            // 비콘 계산한거 출력함

            // 자기 자신을 1초마다 호출
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();   // opening camera
        camera.setDisplayOrientation(90);   // setting camera preview orientation
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camCondition) {
            camera.stopPreview(); // stop preview using stopPreview() method
            camCondition = false; // setting camera condition to false means stop
        }
        // condition to check whether your device have camera or not
        if (camera != null) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                //parameters.setColorEffect(Camera.Parameters.EFFECT_SEPIA); //applying effect on camera
                camera.setParameters(parameters); // setting camera parameters
                camera.setPreviewDisplay(surfaceHolder); // setting preview of camera
                camera.startPreview();  // starting camera preview

                camCondition = true; // setting camera to true which means having camera
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();  // stopping camera preview
        camera.release();
        camera = null;
        camCondition = false;
    }

    //검색을 위한 부분

    private class DownloadJson extends AsyncTask<String, String, String> {
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
            if (result.equals("Json downloddad failed")) {
                Toast.makeText(getApplicationContext(), "서버와 연결할 수 없습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                finish();
                return;
            }
            try {
                final SearchLocation searchLocation = new SearchLocation();
                JSONObject jObject = new JSONObject(result);
                JSONArray Jarray = jObject.getJSONArray("result");
                for (index = 0; index < Jarray.length(); index++) {
                    JSONObject buffer = Jarray.getJSONObject(index); // JSONObject 추출
                    Position position = new Position(buffer.getString("NAME"), buffer.getString("P_X"), buffer.getString("P_Y"), buffer.getString("P_LAYER"));
                    searchLocation.searchLocationList.add(position);
                }

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(View.this);

                alertBuilder.setTitle("선택하세요");
                // List Adapter 생성
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_item);

                for (Position buffer : searchLocation.searchLocationList) {
                    adapter.add(buffer.Name);
                }

                // 버튼 생성
                alertBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                // Adapter 셋팅
                alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String strName = adapter.getItem(id);

                        Position buffer = new Position("없음", "0", "0", "0");
                        for (Position temp : searchLocation.searchLocationList) {
                            if (temp.Name.equals(strName)) {
                                buffer = temp;
                                break;
                            }
                        }

                        touch_X = Double.parseDouble(buffer.Position_X);
                        touch_Y = Double.parseDouble(buffer.Position_Y);
                        findmode = true;
                        Toast.makeText(getApplicationContext(), "방향을 안내합니다 >>>  " + buffer.Position_X + ":" + buffer.Position_Y, Toast.LENGTH_SHORT).show();

                    }
                });
                alertBuilder.show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "서버에서 알 수 없는 응답을 하였습니다.\n애플리케이션을 종료합니다.", Toast.LENGTH_SHORT);
                finish();
            }
        }

        public void dialog() {


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


    private class orderSend extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... arg0) {
            try {
                Log.d("연결", "여기4");
                return (String)getData((String) arg0[0]);
            } catch (Exception e){
                return "Json download failed";

            }
        }

        protected void onPostExecute(final String result) {

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

    private class phpDown extends AsyncTask<String, Integer, String> {


        @Override

        protected String doInBackground(String... urls) {

            StringBuilder jsonHtml = new StringBuilder();

            try {

                // 연결 url 설정

                URL url = new URL(urls[0]);

                // 커넥션 객체 생성

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // 연결되었으면.

                if (conn != null) {

                    conn.setConnectTimeout(10000);

                    conn.setUseCaches(false);

                    // 연결되었음 코드가 리턴되면.

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                        for (; ; ) {

                            // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.

                            String line = br.readLine();

                            if (line == null) break;

                            // 저장된 텍스트 라인을 jsonHtml에 붙여넣음

                            jsonHtml.append(line + "\n");

                        }

                        br.close();

                    }

                    conn.disconnect();

                }

            } catch (Exception ex) {

                ex.printStackTrace();

            }

            return jsonHtml.toString();


        }


        protected void onPostExecute(String str) {

            String UserID = "Hello";
            String Token = "There";


            location = new SearchLocation();
            try{
                JSONObject root = new JSONObject(str);
                JSONArray ja = root.getJSONArray("result");
                for(int i=0; i<ja.length(); i++){
                    JSONObject jo = ja.getJSONObject(i);
                    Position buffer = new Position(jo.getString("User_ID"), jo.getString("User_X"), jo.getString("User_Y"), jo.getString("Token"));

                    location.searchLocationList.add(buffer);


                }

            }catch(JSONException e) {
                e.printStackTrace();
            }
        }
    }


}