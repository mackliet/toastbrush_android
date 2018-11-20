package com.toastbrush.toastbrush_android;

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
                        //TODO set speed value to something meaningful
                        ret_val.append(toastMove(x,y,1));
                    }
                }
            }
            ret_val.append(rapidMove(0,0));
        }
        catch(Exception e)
        {

        }
        ret_val.append(endProgram());
        return ret_val.toString();
    }

    public static String endProgram()
    {
        return "M30\n";
    }

    private static int convertY(double y)
    {
        // TODO
        y = y / 3.5;
        return(int)y;
    }

    private static int convertX(double x)
    {
        // TODO
        x = x / 3.5;
        return (int)x;
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
