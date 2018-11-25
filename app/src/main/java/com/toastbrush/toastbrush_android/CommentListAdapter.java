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
public class CommentListAdapter extends ArrayAdapter<CommentListItem> {

    private Context context;
    private List<CommentListItem> commentList = null;

    public CommentListAdapter(Context context, int textViewResourceId,
                               List<CommentListItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.commentList = objects;
    }

    public int getCount() {
        return this.commentList.size();
    }

    public CommentListItem getItem(int index) {
        return this.commentList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.comment_layout, parent, false);
        }
        // Get item
        CommentListItem commentItem = getItem(position);

        // Get reference to TextView
        TextView usernameView = (TextView) row.findViewById(R.id.commenter);

        // Get reference to TextView
        TextView commentView = (TextView) row.findViewById(R.id.comment);

        // Get reference to TextView
        TextView timestampView = (TextView) row.findViewById(R.id.comment_timestamp);

        usernameView.setText(commentItem.mUsername);
        commentView.setText(commentItem.mComment);
        timestampView.setText((new DateFormat()).format("MM/dd/yyyy", commentItem.mTimestamp).toString());

        return row;
    }
}