package com.toastbrush.toastbrush_android;
import android.graphics.Bitmap;

import org.json.JSONArray;

import java.util.ArrayList;

public class FileListItem
{
    public String mFilename;
    public String mDescription;
    public Bitmap mIcon;
    public JSONArray mPoints;
    public ArrayList<CommentListItem> mComments;
    public CommentListAdapter mCommentListAdapter;
    public Long mTimestamp;
    public String mDatabaseId;
    public String mOwner;
    public long mScore;
    public ToastbrushWebAPI.VoteValue mVote;
    public FileListItem(String filename)
    {
        mFilename = filename;
        mDescription = "";
        mIcon = null;
        mPoints = null;
        mTimestamp = null;
        mDatabaseId = null;
        mComments = null;
        mComments = null;
        mCommentListAdapter = null;
        mOwner = null;
        mScore = 0;
        mVote = ToastbrushWebAPI.VoteValue.NO_VOTE;
    }
}
