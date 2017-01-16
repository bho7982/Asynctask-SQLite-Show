package com.example.ma1le.smartview;

/**
 * Created by ma1le on 2016-12-03.
 */

public class NowLocation {
    String now_x;
    String now_y;

    public NowLocation(String now_x, String now_y){
        this.now_x = now_x;
        this.now_y = now_y;
    }

    public Double get_X(){
        return Double.parseDouble(now_x);
    }

    public Double get_Y(){
        return Double.parseDouble(now_y);
    }
}
