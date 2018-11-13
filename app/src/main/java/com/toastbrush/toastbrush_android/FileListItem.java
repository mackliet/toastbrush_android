package com.toastbrush.toastbrush_android;
import android.graphics.Bitmap;

import org.json.JSONArray;

public class FileListItem
{
    public String mFilename;
    public Bitmap mIcon;
    public JSONArray mPoints;
    public Long mTimestamp;
    public FileListItem(String filename)
    {
        mFilename = filename;
        mIcon = null;
        mPoints = null;
        mTimestamp = null;
    }
}
