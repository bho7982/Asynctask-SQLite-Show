package com.example.ma1le.smartview;

import android.app.SearchableInfo;
import android.util.Log;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by User219 on 2016-11-14.
 */


public class LocationCalculation {
    double X;
    double Y;

    public String Calculation(Collection<Beacon> beaconList, ArrayList<DBBeacon> DBbeaconList) {
        int index = 4;
        int x[] = new int[5];
        int y[] = new int[5];
        double D[] = new double[5];
        double resultX[] = new double[5];
        double resultY[] = new double[5];

        ArrayList<SearchBeacon> searchBeaconList = new ArrayList<>();
        ArrayList<SearchBeacon> searchBeaconList1 = new ArrayList<>();
        searchBeaconList.clear();
        searchBeaconList1.clear();
        SearchBeacon SB;

        for (Beacon beacon : beaconList) {
            if (beacon.getId2().toString().equals("12345")) {
                SB = new SearchBeacon();
                SB.ID = beacon.getId3().toString();
                SB.distance = Double.parseDouble(String.format("%.3f", beacon.getDistance()));
                searchBeaconList.add(SB);
            }
        }

        //거리순(오름차순)으로 sort
        Collections.sort(searchBeaconList, new NoAscCompare());

        int i = 0;
        for (SearchBeacon searchBeacon : searchBeaconList) {
            if (i == 4) break;
            else {
                SB = new SearchBeacon();
                SB.ID = searchBeacon.ID;
                SB.distance = searchBeacon.distance;
                searchBeaconList1.add(SB);
                i++;
            }
        }

        //ID순(오름차순)으로 sort
        Collections.sort(searchBeaconList1, new NameAscCompare());

        int j = 1;
        for (SearchBeacon searchBeacon : searchBeaconList1) {
            for (DBBeacon dbBeacon : DBbeaconList) {
                if (searchBeacon.ID.equals(dbBeacon.B_ID)) {
                    x[j] = Integer.parseInt(dbBeacon.B_X);
                    y[j] = Integer.parseInt(dbBeacon.B_Y);
                    D[j] = searchBeacon.distance;
                }
            }
            j++;
        }

        String buffer = "";
        int k = 1;
        for (SearchBeacon searchBeacon : searchBeaconList1) {
            buffer += k + ".  " + searchBeacon.ID + " : " + x[k] + "." + y[k] + " / " + D[k] + "\n";
            k++;
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        buffer += "\n";


        resultX[1] = (Math.pow(D[1], 2) - Math.pow(D[2], 2) + Math.pow(x[2], 2) - Math.pow(x[1], 2)) / (2 * (x[2] - x[1]));     //1과 2
        resultY[1] = Math.sqrt(Math.abs(Math.pow(D[1], 2) - Math.pow(resultX[2] - x[1], 2))) + y[1];
        if(Double.isNaN(resultX[1]) || Double.isNaN(resultY[1]) || Double.isInfinite(resultX[1]) || Double.isInfinite(resultY[1])) {
            resultX[1] = 0;
            resultY[1] = 0;
            index--;
            buffer += "제외1\n";
        }else{
            buffer += String.format("%.3f", resultX[1]) + "/" + String.format("%.3f", resultY[1]) + "\n";
        }


        resultX[2] = (Math.pow(D[3], 2) - Math.pow(D[4], 2) + Math.pow(x[4], 2) - Math.pow(x[3], 2)) / (2 * (x[4] - x[3]));     //3와 4
        resultY[2] = -Math.sqrt(Math.abs(Math.pow(D[3], 2) - Math.pow(resultX[2] - x[3], 2))) + y[3];
        if(Double.isNaN(resultX[2]) || Double.isNaN(resultY[2]) || Double.isInfinite(resultX[2]) || Double.isInfinite(resultY[2])) {
            resultX[2] = 0;
            resultY[2] = 0;
            index--;
            buffer += "제외2\n";
        }else{
            buffer += String.format("%.3f", resultX[2]) + "/" + String.format("%.3f", resultY[2]) + "\n";
        }


        resultY[3] = (Math.pow(D[1], 2) - Math.pow(D[3], 2) + Math.pow(y[3], 2) - Math.pow(y[1], 2)) / (2 * (y[3] - y[1]));     //1과 3
        resultX[3] = Math.sqrt(Math.abs(Math.pow(D[1], 2) - Math.pow(resultY[3] - y[1], 2))) + x[1];
        if(Double.isNaN(resultX[3]) || Double.isNaN(resultY[3]) || Double.isInfinite(resultX[3]) || Double.isInfinite(resultY[3])) {
            resultX[3] = 0;
            resultY[3] = 0;
            index--;
            buffer += "제외3\n";
        }else{
            buffer += String.format("%.3f", resultX[3]) + "/" + String.format("%.3f", resultY[3]) + "\n";
        }


        resultY[4] = (Math.pow(D[2], 2) - Math.pow(D[4], 2) + Math.pow(y[4], 2) - Math.pow(y[2], 2)) / (2 * (y[4] - y[2]));     //2와 4
        resultX[4] = -Math.sqrt(Math.abs(Math.pow(D[2], 2) - Math.pow(resultY[4] - y[2], 2))) + x[2];
        if(Double.isNaN(resultX[4]) || Double.isNaN(resultY[4]) || Double.isInfinite(resultX[4]) || Double.isInfinite(resultY[4])) {
            resultX[4] = 0;
            resultY[4] = 0;
            index--;
            buffer += "제외4\n";
        }else{
            buffer += String.format("%.3f", resultX[4]) + "/" + String.format("%.3f", resultY[4]) + "\n";
        }

        X = (resultX[1] + resultX[2] + resultX[3] + resultX[4]) / index;
        Y = (resultY[1] + resultY[2] + resultY[3] + resultY[4]) / index;

        buffer += "\n" + String.format("%.3f", X) + "." + String.format("%.3f", Y);

        return buffer;
        //return new NowLocation(String.format("%.3f", X), String.format("%.3f", Y));
    }

    public NowLocation CalculationLocation() {
        return new NowLocation(String.format("%.3f", X), String.format("%.3f", Y));
    }
}

class SearchBeacon {
    String ID;
    double distance;
}

class NoAscCompare implements Comparator<SearchBeacon> {
    /**
     * 오름차순(ASC)
     */
    @Override
    public int compare(SearchBeacon arg0, SearchBeacon arg1) {
        // TODO Auto-generated method stub
        return arg0.distance < arg1.distance ? -1 : arg0.distance > arg1.distance ? 1 : 0;
    }
}


class NameAscCompare implements Comparator<SearchBeacon> {
    /**
     * 오름차순(ASC)
     */
    @Override
    public int compare(SearchBeacon arg0, SearchBeacon arg1) {
        // TODO Auto-generated method stub
        return arg0.ID.compareTo(arg1.ID);
    }
}

