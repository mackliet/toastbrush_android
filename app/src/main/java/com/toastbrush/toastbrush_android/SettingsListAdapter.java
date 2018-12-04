package com.toastbrush.toastbrush_android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

/**
 *
 * Refactored from code found at
 * https://androidcookbook.com/Recipe.seam;jsessionid=84D3762FD44502EEE9A5DCB786480362?recipeId=830&recipeFrom=ViewTOC
 *
 */
public class SettingsListAdapter extends ArrayAdapter<SettingsListItem> {

    private List<SettingsListItem> settingsList;

    SettingsListAdapter(Context context, int textViewResourceId,
                        List<SettingsListItem> objects) {
        super(context, textViewResourceId, objects);
        this.settingsList = objects;
    }

    public int getCount() {
        return this.settingsList.size();
    }

    public SettingsListItem getItem(int index) {
        return this.settingsList.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.settings_list_item, parent, false);
        }
        // Get item
        SettingsListItem settingsItem = getItem(position);

        // Get reference to TextView
        TextView settingName = row.findViewById(R.id.setting_name);

        // Get reference to TextView
        TextView settingDescription = row.findViewById(R.id.setting_description);

        settingName.setText(Objects.requireNonNull(settingsItem).mName);
        settingDescription.setText(settingsItem.mDescription);

        return row;
    }
}