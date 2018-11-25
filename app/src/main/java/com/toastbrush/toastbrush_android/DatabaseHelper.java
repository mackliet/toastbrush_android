package com.toastbrush.toastbrush_android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.Log;

import com.toastbrush.ToastbrushApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DatabaseHelper
{
    private static String DATABASE_FILE_PATH;
    public static void saveToastImage(String filename, String description, Bitmap bmp, JSONArray tstPnts)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("DELETE FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        stmt.execute();
        stmt = db.compileStatement("INSERT INTO Toast_images (image_name, data, timestamp, icon, description) VALUES (?, ?, ?, ?, ?)");
        stmt.bindString(1, filename);
        stmt.bindString(2, packageImageInfo(bmp, tstPnts));
        stmt.bindLong(3, System.currentTimeMillis());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        stmt.bindBlob(4, bytes.toByteArray());
        stmt.bindString(5, description);
        stmt.execute();
    }

    public static void deleteToastImage(String filename)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("DELETE FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        stmt.execute();
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
        create_image_table_if_not_exists(db);
        Cursor cursor = db.rawQuery("SELECT image_name FROM Toast_images", new String[]{});
        while(cursor.moveToNext())
        {
            int index = cursor.getColumnIndex("image_name");
            filenames.add(cursor.getString(index));
        }
        return filenames;
    }

    public static ArrayList<FileListItem> getFileListItems()
    {
        ArrayList<FileListItem> filenames = new ArrayList<>();
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_image_table_if_not_exists(db);
        ;
        Cursor cursor = db.rawQuery("SELECT image_name, icon, timestamp, description FROM Toast_images", new String[]{});
        try {
            while (cursor.moveToNext()) {
                FileListItem listItem = new FileListItem(cursor.getString(cursor.getColumnIndex("image_name")));
                byte[] byteBitmap = cursor.getBlob(cursor.getColumnIndex("icon"));
                listItem.mIcon = BitmapFactory.decodeByteArray(byteBitmap, 0, byteBitmap.length);
                listItem.mTimestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
                listItem.mDescription = cursor.getString(cursor.getColumnIndex("description"));
                filenames.add(listItem);
            }
        }
        catch(Exception e)
        {

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
        create_image_table_if_not_exists(db);
        Cursor cursor = db.rawQuery("SELECT icon FROM Toast_images WHERE image_name=?", new String[]{filename});
        byte[] byteBitmap = cursor.getBlob(cursor.getColumnIndex("icon"));
        return BitmapFactory.decodeByteArray(byteBitmap, 0, byteBitmap.length);
    }

    public static String getImageData(String filename)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("SELECT data FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        return stmt.simpleQueryForString();
    }

    public static long getTimestamp(String filename)
    {
        if(DATABASE_FILE_PATH == null)
    {
        DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
    }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("SELECT timestamp FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        return stmt.simpleQueryForLong();
    }

    private static void create_image_table_if_not_exists(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, description TEXT, data TEXT, timestamp INTEGER, icon BLOB)");
    }

    private static void create_settings_table_if_not_exists(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS Settings (setting_name TEXT PRIMARY KEY, value TEXT)");
    }

    public static void setSetting(String name, String value)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_settings_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("DELETE FROM Settings WHERE setting_name=?");
        stmt.bindString(1, name);
        stmt.execute();
        stmt = db.compileStatement("INSERT INTO Settings (setting_name, value) VALUES (?, ?)");
        stmt.bindString(1, name);
        stmt.bindString(2, value);
        stmt.execute();
    }

    public static String getSetting(String name)
    {
        if(DATABASE_FILE_PATH == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
        }
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        create_settings_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("SELECT value FROM Settings WHERE setting_name=?");
        stmt.bindString(1, name);
        String ret_val = null;
        try
        {
            ret_val = stmt.simpleQueryForString();
        }
        catch (Exception e)
        {

        }
        return ret_val;
    }

    public static String packageImageInfo(Bitmap bmp, JSONArray tstPnts)
    {
        String ret_val = null;

        try
        {
            JSONObject json = new JSONObject();
            json.put("Image", base64EncodeBitmap(bmp));
            json.put("Points", tstPnts);
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
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    private DatabaseHelper(){}
}
