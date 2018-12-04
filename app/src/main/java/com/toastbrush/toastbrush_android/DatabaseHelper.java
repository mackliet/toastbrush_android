package com.toastbrush.toastbrush_android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.toastbrush.ToastbrushApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DatabaseHelper
{
    private static String DATABASE_FILE_PATH;
    private static SQLiteDatabase mDB;
    public static void saveToastImage(String filename, String description, Bitmap bmp, JSONArray tstPnts)
    {
        SQLiteDatabase db = getDB();
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("INSERT OR IGNORE INTO Toast_images (image_name, data, timestamp, icon, description) VALUES (?, ?, ?, ?, ?)");
        stmt.bindString(1, filename);
        stmt.bindString(2, packageImageInfo(bmp, tstPnts));
        stmt.bindLong(3, System.currentTimeMillis());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        stmt.bindBlob(4, bytes.toByteArray());
        stmt.bindString(5, description);
        stmt.execute();
        stmt.close();
        stmt = db.compileStatement("UPDATE Toast_images SET data=?, timestamp=?, icon=?, description=? WHERE image_name=?");
        stmt.bindString(1, packageImageInfo(bmp, tstPnts));
        stmt.bindLong(2, System.currentTimeMillis());
        stmt.bindBlob(3, bytes.toByteArray());
        stmt.bindString(4, description);
        stmt.bindString(5, filename);
        stmt.execute();
        stmt.close();
    }

    static void deleteToastImage(String filename)
    {
        SQLiteDatabase db = getDB();
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("DELETE FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        stmt.execute();
        stmt.close();
    }

    static ArrayList<FileListItem> getFileListItems()
    {
        ArrayList<FileListItem> filenames = new ArrayList<>();
        SQLiteDatabase db = getDB();
        create_image_table_if_not_exists(db);
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
            Log.e("TESTING", "Exception in getFileListItems: " + e.getMessage());
        }
        cursor.close();
        return filenames;
    }

    static String getImageData(String filename)
    {
        SQLiteDatabase db = getDB();
        create_image_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("SELECT data FROM Toast_images WHERE image_name=?");
        stmt.bindString(1, filename);
        String ret_val = stmt.simpleQueryForString();
        stmt.close();
        return ret_val;
    }

    private static void create_image_table_if_not_exists(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS Toast_images (image_name TEXT PRIMARY KEY, description TEXT, data TEXT, timestamp INTEGER, icon BLOB)");
    }

    private static void create_settings_table_if_not_exists(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS Settings (setting_name TEXT PRIMARY KEY, value TEXT)");
    }

    private static void create_cache_table(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS Restore_cache (value_name TEXT PRIMARY KEY, value BLOB)");
    }

    private static SQLiteDatabase getDB()
    {
        if(mDB == null)
        {
            DATABASE_FILE_PATH = ToastbrushApplication.getAppContext().getFilesDir() + "database.db";
            mDB = SQLiteDatabase.openOrCreateDatabase(DATABASE_FILE_PATH, null);
        }
        return mDB;
    }

    public static byte[] getCacheValue(String name)
    {
        SQLiteDatabase db = getDB();
        create_cache_table(db);
        Cursor cursor = db.rawQuery("SELECT value FROM Restore_cache WHERE value_name=?", new String[]{name});
        cursor.moveToFirst();
        byte[] ret_val = cursor.getBlob(cursor.getColumnIndex("value"));
        cursor.close();
        db.execSQL("VACUUM");
        return ret_val;
    }

    public static void setCacheValue(String name, byte[] value)
    {
        SQLiteDatabase db = getDB();
        create_cache_table(db);
        SQLiteStatement stmt  = db.compileStatement("INSERT OR IGNORE INTO Restore_cache (value_name, value) VALUES (?, ?)");
        stmt.bindString(1, name);
        stmt.bindBlob(2, value);
        stmt.execute();
        stmt.close();
        stmt = db.compileStatement("UPDATE Restore_cache SET value=? WHERE value_name=?");
        stmt.bindString(2, name);
        stmt.bindBlob(1, value);
        stmt.execute();
        stmt.close();
    }

    public static void setSetting(String name, String value)
    {
        SQLiteDatabase db = getDB();
        create_settings_table_if_not_exists(db);
        SQLiteStatement stmt = db.compileStatement("INSERT OR IGNORE INTO Settings (setting_name, value) VALUES (?, ?)");
        stmt.bindString(1, name);
        stmt.bindString(2, value);
        stmt.execute();
        stmt.close();
        stmt = db.compileStatement("UPDATE Settings SET value=? WHERE setting_name=?");
        stmt.bindString(2, name);
        stmt.bindString(1, value);
        stmt.execute();
        stmt.close();
    }

    public static String getSetting(String name)
    {
        SQLiteDatabase db = getDB();
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
            Log.e("TESTING", "Exception in getSetting: " + e.getMessage());
        }
        stmt.close();
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
            Log.e("TESTING", "Exception in packageImageInfo: " + e.getMessage());
        }
        return ret_val;
    }

    @SuppressWarnings("WeakerAccess")
    public static String base64EncodeBitmap(Bitmap bmp)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap.createScaledBitmap(bmp, 500, 500, false).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static void vacuum()
    {
        SQLiteDatabase db = getDB();
        db.execSQL("VACUUM");
    }

    public static Bitmap base64DecodeBitmap(String base64str)
    {
        byte[] decodedString = Base64.decode(base64str, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public static byte[] readInFile(String path)
    {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void saveToFile(String path, byte[] fileBytes)
    {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DatabaseHelper(){}
}
