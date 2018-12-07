package com.toastbrush.toastbrush_android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class GCodeBuilder
{
    private GCodeBuilder(){}

    public static String convertToGcode(JSONArray tstPnts)
    {
        StringBuilder ret_val = new StringBuilder(rapidMove(0,0));
        try {
            for (int i = 0; i < tstPnts.length(); i++) {
                JSONArray line = tstPnts.getJSONArray(i);
                for (int j = 0; j < line.length(); j++)
                {
                    JSONObject point = line.getJSONObject(j);
                    int x = convertX(point.getDouble("x"));
                    int y = convertY(point.getDouble("y"));
                    if(j == 0)
                    {
                        ret_val.append(rapidMove(x, y));
                    }
                    else
                    {
                        String setting = DatabaseHelper.getSetting("Toast Darkness");
                        setting = setting == null ? "1" : setting;
                        int speed = (Integer.parseInt(setting) - 6) * -1; // Convert darkness to speed
                        ret_val.append(toastMove(x,y, speed));
                    }
                }
            }
            ret_val.append(rapidMove(0,0));
        }
        catch(Exception e)
        {

        }
        ret_val.append(endProgram());
        Log.d("TESTING", "GCODE\n" + ret_val.toString());
        return ret_val.toString();
    }

    public static String endProgram()
    {
        return "M30\n";
    }

    private static int convertY(double y)
    {
        int ret_y = (int)y;
        ret_y = ret_y > 255 ? 255 : ret_y;
        ret_y = ret_y < 0 ? 0 : ret_y;
        ret_y = -ret_y + 255;
        return ret_y;
    }

    private static int convertX(double x)
    {
        int ret_x = (int)x;
        ret_x = ret_x > 255 ? 255 : ret_x;
        ret_x = ret_x < 0 ? 0 : ret_x;
        return ret_x;
    }

    public static String rapidMove(int x, int y)
    {
        return "G00 X" + x + " Y" + y + "\n";
    }

    public static String toastMove(int x, int y, int speed)
    {
        return "G01 X" + x + " Y" + y + " F" + speed +"\n";
    }
}
