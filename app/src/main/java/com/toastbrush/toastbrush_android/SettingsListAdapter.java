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
public class SettingsListAdapter extends ArrayAdapter<SettingsListItem> {

    private Context context;
    private List<SettingsListItem> settingsList = null;

    public SettingsListAdapter(Context context, int textViewResourceId,
                           List<SettingsListItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.settingsList = objects;
    }

    public int getCount() {
        return this.settingsList.size();
    }

    public SettingsListItem getItem(int index) {
        return this.settingsList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.settings_list_item, parent, false);
        }
        // Get item
        SettingsListItem settingsItem = getItem(position);

        // Get reference to TextView
        TextView settingName = (TextView) row.findViewById(R.id.setting_name);

        // Get reference to TextView
        TextView settingDescription = (TextView) row.findViewById(R.id.setting_description);

        settingName.setText(settingsItem.mName);
        settingDescription.setText(settingsItem.mDescription);

        return row;
    }
}