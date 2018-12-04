package com.toastbrush.toastbrush_android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import static android.graphics.Bitmap.createScaledBitmap;

/**
 *
 * Refactored from code found at
 * https://androidcookbook.com/Recipe.seam;jsessionid=84D3762FD44502EEE9A5DCB786480362?recipeId=830&recipeFrom=ViewTOC
 *
 */
public class FileListAdapter extends ArrayAdapter<FileListItem> {

    private ImageView toastImageIcon;
    private TextView toastImageName;
    private TextView toastImageDescription;
    private TextView toastImageTimestamp;
    private List<FileListItem> toastImageList;

    FileListAdapter(Context context, int textViewResourceId,
                    List<FileListItem> objects) {
        super(context, textViewResourceId, objects);
        this.toastImageList = objects;
    }

    public int getCount() {
        return this.toastImageList.size();
    }

    public FileListItem getItem(int index) {
        return this.toastImageList.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.file_list_item, parent, false);
        }
            // Get item
            FileListItem toastImage = getItem(position);

            // Get reference to ImageView
            toastImageIcon = row.findViewById(R.id.toast_image_icon);

            // Get reference to TextView for name
            toastImageName = row.findViewById(R.id.toast_image_name);

            // Get reference to TextView for name
            toastImageDescription = row.findViewById(R.id.toast_image_description);

            // Get reference to TextView for Timestamp
            toastImageTimestamp = row.findViewById(R.id.toast_image_timestamp);

            //Set toastImage name
            toastImageName.setText(Objects.requireNonNull(toastImage).mFilename);

            //Set toastImage name
            toastImageDescription.setText(toastImage.mDescription);

            //Set timestamp
            toastImageTimestamp.setText(DateFormat.format("MM/dd/yyyy", toastImage.mTimestamp).toString());
        if(toastImage.mIcon != null)
        {
            try
            {
                toastImageIcon.setImageBitmap(createScaledBitmap(toastImage.mIcon, 200, 200, false));
            }
            catch(Exception e)
            {
                toastImageIcon.setImageBitmap(createScaledBitmap(DrawingView.getBlankImage(), 200, 200, false));
            }
        }
        else
        {
            toastImageIcon.setImageBitmap(createScaledBitmap(DrawingView.getBlankImage(), 200, 200, false));
        }
        return row;
    }
}