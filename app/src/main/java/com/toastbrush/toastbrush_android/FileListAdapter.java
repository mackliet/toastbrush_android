package com.toastbrush.toastbrush_android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 * Refactored from code found at
 * https://androidcookbook.com/Recipe.seam;jsessionid=84D3762FD44502EEE9A5DCB786480362?recipeId=830&recipeFrom=ViewTOC
 *
 */
public class FileListAdapter extends ArrayAdapter<FileListItem> {

    private Context context;
    private ImageView toastImageIcon;
    private TextView toastImageName;
    private TextView toastImageTimestamp;
    private List<FileListItem> toastImageList = null;

    public FileListAdapter(Context context, int textViewResourceId,
                               List<FileListItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.toastImageList = objects;
    }

    public int getCount() {
        return this.toastImageList.size();
    }

    public FileListItem getItem(int index) {
        return this.toastImageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.file_list_item, parent, false);
        }
            // Get item
            FileListItem toastImage = getItem(position);

            // Get reference to ImageView
            toastImageIcon = (ImageView) row.findViewById(R.id.toast_image_icon);

            // Get reference to TextView for name
            toastImageName = (TextView) row.findViewById(R.id.toast_image_name);

            // Get reference to TextView for Timestamp
            toastImageTimestamp = (TextView) row.findViewById(R.id.toast_image_timestamp);

            //Set toastImage name
            toastImageName.setText(toastImage.mFilename);

            //Set timestamp
            toastImageTimestamp.setText((new DateFormat()).format("MM/dd/yyyy", toastImage.mTimestamp).toString());
        if(toastImage.mIcon != null)
        {
            try
            {
                toastImageIcon.setImageBitmap(Bitmap.createScaledBitmap(toastImage.mIcon, 200, 200, false));
            }
            catch(Exception e)
            {
                toastImageIcon.setImageBitmap(Bitmap.createScaledBitmap(DrawingView.getBlankImage(), 200, 200, false));
            }
        }
        else
        {
            toastImageIcon.setImageBitmap(Bitmap.createScaledBitmap(DrawingView.getBlankImage(), 200, 200, false));
        }
        return row;
    }
}