package com.example.ma1le.smartview;

/**
 * Created by ma1le on 2016-12-02.
 */

public class Position {
    public String Name;
    public String Position_Layer;
    public String Position_X;
    public String Position_Y;

    public Position(String Name, String Position_X, String Position_Y, String Position_Layer){
        this.Name = Name;
        this.Position_Layer = Position_Layer;
        this.Position_X = Position_X;
        this.Position_Y = Position_Y;
    }
}
