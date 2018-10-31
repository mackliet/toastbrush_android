package com.toastbrush.toastbrush_android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.Pair;
import android.util.Base64;

import com.toastbrush.ToastbrushApplication;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DatabaseHelper
{
    private static String DATABASE_FILE_PATH;
    public static void saveToastImage(String filename, String pkgedTstImg)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, data TEXT, timestamp INTEGER)");;
        SQLiteStatement stmt = db.compileStatement("DELETE FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        stmt.execute();
        stmt = db.compileStatement("INSERT INTO Toast_images (image_name, data, timestamp) VALUES (?, ?, ?)");
        stmt.bindString(1, filename);
        stmt.bindString(2, pkgedTstImg);
        stmt.bindLong(3, System.currentTimeMillis());
        stmt.execute();
    }

    public static ArrayList<String> getFilenames()
    {
        ArrayList<String> filenames = new ArrayList<>();
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, data TEXT, timestamp INTEGER)");;
        Cursor cursor = db.rawQuery("SELECT image_name FROM Toast_images", new String[]{});
        while(cursor.moveToNext())
        {
            int index = cursor.getColumnIndex("image_name");
            filenames.add(cursor.getString(index));
        }
        return filenames;
    }

    public static Bitmap getIcon(String filename)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, data TEXT, timestamp INTEGER)");;
        SQLiteStatement stmt = db.compileStatement("SELECT data FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        String encoded_bitmap = null;
        try{
            JSONObject json = new JSONObject(stmt.simpleQueryForString());
            encoded_bitmap = json.getString("Image");
        }
        catch(Exception e)
        {

        }

        return base64DecodeBitmap(encoded_bitmap);
    }

    public static long getTimestamp(String filename)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, data TEXT, timestamp INTEGER)");;
        SQLiteStatement stmt = db.compileStatement("SELECT timestamp FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        return stmt.simpleQueryForLong();
    }

    public static String packageImageInfo(Bitmap bmp, ArrayList<ArrayList<Pair<Float, Float>>> tstPnts)
    {
        String ret_val = null;
        try
        {
            JSONObject json = new JSONObject();
            json.put("Image", base64EncodeBitmap(bmp));
            json.put("Points", tstPnts.toString());
            ret_val = json.toString();
        }
        catch(Exception e)
        {

        }
        return ret_val;
    }

    public static String base64EncodeBitmap(Bitmap bmp)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap base64DecodeBitmap(String base64str)
    {
        byte[] decodedString = Base64.decode(base64str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private DatabaseHelper(){}
}
