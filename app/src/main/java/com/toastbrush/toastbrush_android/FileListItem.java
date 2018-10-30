package com.toastbrush.toastbrush_android;
import android.graphics.Bitmap;

public class FileListItem
{
    public String mFilename;
    public Bitmap mIcon;
    public Long mTimestamp;
    public FileListItem(String filename)
    {
        mFilename = filename;
        mIcon = null;
        mTimestamp = null;
    }
}
