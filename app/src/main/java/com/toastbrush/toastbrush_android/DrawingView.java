package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.support.v4.util.Pair;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DrawingView extends View {
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = Color.parseColor("#f5ca62");
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private ArrayList<ArrayList<Pair<Float, Float>>> mDrawingPoints;

    private int mWidth;
    private int mHeight;

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        mDrawingPoints = new ArrayList<>();
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        Pair<Float, Float> touch_point = new Pair<>(touchX, touchY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                mDrawingPoints.add(new ArrayList<Pair<Float, Float>>());
                mDrawingPoints.get(mDrawingPoints.size() - 1).add(touch_point);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                mDrawingPoints.get(mDrawingPoints.size() - 1).add(touch_point);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                mDrawingPoints.get(mDrawingPoints.size() - 1).add(touch_point);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void clear_canvas()
    {
        //numberOfPoints = 0;
        mDrawingPoints = new ArrayList<>();
        canvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        invalidate();
    }

    public void save_canvas_local(String filename)
    {
        File bitmap_file = new File(getContext().getFilesDir(), "Toast_images/" + filename +".png");
        File points_file = new File(getContext().getFilesDir(), "Toast_images/" + filename +".tstpnts");
        try {
            FileOutputStream bitmap_stream = new FileOutputStream(bitmap_file);
            FileOutputStream points_stream = new FileOutputStream(points_file);

            //Write points and png to files
            points_stream.write(mDrawingPoints.toString().getBytes("UTF-8"));
            canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmap_stream);

            // Zip points and png into a file
            String[] files_to_zip = {bitmap_file.getAbsolutePath(), points_file.getAbsolutePath()};
            String zip_file_name = (new File(getContext().getFilesDir(), "Toast_images/" + filename + ".tst")).getAbsolutePath();
            zip(files_to_zip, zip_file_name);

            bitmap_stream.close();
            points_stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save_canvas_database(String filename)
    {

    }

    /*
    Got this from https://stacktips.com/tutorials/android/how-to-programmatically-zip-and-unzip-file-in-android
     */
    public void zip(String[] _files, String zipFileName) {
        final int BUFFER = 10000;
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

