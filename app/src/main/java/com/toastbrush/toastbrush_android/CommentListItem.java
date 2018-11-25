package com.toastbrush.toastbrush_android;
public class CommentListItem
{
    public String mUsername;
    public String mComment;
    public long mTimestamp;
    public CommentListItem(String username, String comment, long timestamp)
    {
        mUsername = username;
        mComment = comment;
        mTimestamp = timestamp;
    }
}
