package com.toastbrush.toastbrush_android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
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
public class CommentListAdapter extends ArrayAdapter<CommentListItem> {

    private List<CommentListItem> commentList;

    CommentListAdapter(Context context, int textViewResourceId,
                       List<CommentListItem> objects) {
        super(context, textViewResourceId, objects);
        this.commentList = objects;
    }

    public int getCount() {
        return this.commentList.size();
    }

    public CommentListItem getItem(int index) {
        return this.commentList.get(index);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            // ROW INFLATION
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = Objects.requireNonNull(inflater).inflate(R.layout.comment_layout, parent, false);
        }
        // Get item
        CommentListItem commentItem = getItem(position);

        // Get reference to TextView
        TextView usernameView = row.findViewById(R.id.commenter);

        // Get reference to TextView
        TextView commentView = row.findViewById(R.id.comment);

        // Get reference to TextView
        TextView timestampView = row.findViewById(R.id.comment_timestamp);

        if (commentItem != null) {
            usernameView.setText(commentItem.mUsername);

            commentView.setText(commentItem.mComment);
            timestampView.setText(DateFormat.format("MM/dd/yyyy", commentItem.mTimestamp).toString());
        }

        return row;
    }
}