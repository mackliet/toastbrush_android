package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.toastbrush.ToastbrushApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.toastbrush.ToastbrushApplication.getAppContext;
import static com.toastbrush.ToastbrushApplication.getGoogleAccount;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnlineBrowseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class OnlineBrowseFragment extends Fragment implements SearchView.OnQueryTextListener,  AdapterView.OnItemClickListener{
    private OnFragmentInteractionListener mListener;

    private SearchView mSearchView;
    private Spinner mSearchTypeSpinner;
    private Spinner mOrderSpinner;
    private ListView mListView;
    private ArrayList<FileListItem> mToastImageList;
    private FileListAdapter mListAdapter;
    private Drawable mBackground;
    private final Lock mMutex = new ReentrantLock();

    public OnlineBrowseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View this_view = inflater.inflate(R.layout.fragment_browse_online, container, false);
        mToastImageList = new ArrayList<>();
        mListAdapter = new FileListAdapter(getContext(), R.layout.file_list_item, mToastImageList);
        mListView = this_view.findViewById(R.id.online_file_list);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mListAdapter);
        setupSpinners(this_view);
        mSearchView = (SearchView)this_view.findViewById(R.id.onlineSearchView);
        mSearchView.setOnQueryTextListener(this);
        return this_view;
    }

    private void setupSpinners(View this_view)
    {
        mSearchTypeSpinner = (Spinner)this_view.findViewById(R.id.searchTypeSpinner);
        mOrderSpinner = (Spinner)this_view.findViewById(R.id.orderSpinner);
        ArrayAdapter<CharSequence> searchTypeAdapter = ArrayAdapter.createFromResource(this_view.getContext(), R.array.search_type_options, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(this_view.getContext(), R.array.sort_order_options, android.R.layout.simple_spinner_item);
        searchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSearchTypeSpinner.setAdapter(searchTypeAdapter);
        mOrderSpinner.setAdapter(orderAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        String query = mSearchView.getQuery().toString();
        String type = mSearchTypeSpinner.getSelectedItem().toString();
        String order = mOrderSpinner.getSelectedItem().toString();
        ToastbrushWebAPI.OrderValue orderValue = ToastbrushWebAPI.OrderValue.NEWEST;
        Toast.makeText(getContext(), "Searching " + query + "...", Toast.LENGTH_SHORT).show();
        switch(order)
        {
            case "Newest":
                orderValue = ToastbrushWebAPI.OrderValue.NEWEST;
                break;
            case "Oldest":
                orderValue = ToastbrushWebAPI.OrderValue.OLDEST;
                break;
            case "High Score":
                orderValue = ToastbrushWebAPI.OrderValue.HIGH_SCORE;
                break;
            case "Low Score":
                orderValue = ToastbrushWebAPI.OrderValue.LOW_SCORE;
                break;
            default:
                break;
        }
        final String currentUser = getGoogleAccount() == null ? null : getGoogleAccount().getEmail();
        final Response.Listener<String> callback = new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                try
                {
                    JSONObject json = new JSONObject(response);
                    JSONArray list = json.getJSONArray("list");
                    mMutex.lock();
                    mToastImageList.clear();
                    Toast.makeText(getContext(), "Found " + list.length() + " results", Toast.LENGTH_SHORT).show();
                    for(int i = 0; i < list.length(); i++)
                    {
                        JSONObject imageInfo = list.getJSONObject(i);
                        FileListItem item = new FileListItem(imageInfo.getString("Name"));
                        item.mTimestamp = imageInfo.getLong("date");
                        item.mDescription = imageInfo.getString("Description");
                        item.mScore = imageInfo.getLong("Score");
                        item.mOwner = imageInfo.getString("User");
                        item.mDatabaseId = imageInfo.getString("Image");
                        if(currentUser != null)
                        {
                            int score = imageInfo.getInt("UserScore");
                            if(score == ToastbrushWebAPI.VoteValue.UP_VOTE.asInt())
                            {
                                item.mVote = ToastbrushWebAPI.VoteValue.UP_VOTE;
                            }
                            else if(score == ToastbrushWebAPI.VoteValue.DOWN_VOTE.asInt())
                            {
                                item.mVote = ToastbrushWebAPI.VoteValue.DOWN_VOTE;
                            }
                            else
                            {
                               item.mVote = ToastbrushWebAPI.VoteValue.NO_VOTE;
                            }
                        }
                        mToastImageList.add(item);
                    }
                    mListAdapter.notifyDataSetChanged();
                    mMutex.unlock();
                    for(final FileListItem item : mToastImageList)
                    {
                        ToastbrushWebAPI.getImage(item.mDatabaseId, new Response.Listener<String>()
                        {

                            @Override
                            public void onResponse(String response)
                            {
                                mMutex.lock();
                                try
                                {
                                    JSONObject json = new JSONObject(response);
                                    JSONObject packagedInfo = new JSONObject(json.getString("Encoded"));
                                    Bitmap icon = DatabaseHelper.base64DecodeBitmap(packagedInfo.getString("Image"));
                                    JSONArray points = packagedInfo.getJSONArray("Points");
                                    for(FileListItem listItem : mToastImageList)
                                    {
                                        if(listItem.mDatabaseId.equals(item.mDatabaseId))
                                        {
                                            listItem.mIcon = icon;
                                            listItem.mPoints = points;
                                            break;
                                        }
                                    }
                                    mListAdapter.notifyDataSetChanged();
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(ToastbrushApplication.getAppContext(), "Exception parsing server response", Toast.LENGTH_SHORT).show();
                                }
                                mMutex.unlock();
                            }
                        });

                        ToastbrushWebAPI.getComments(item.mDatabaseId, ToastbrushWebAPI.OrderValue.OLDEST, 1000, 0, new Response.Listener<String>()
                        {

                            @Override
                            public void onResponse(String commentResponse)
                            {
                                mMutex.lock();
                                try
                                {
                                    JSONObject json = new JSONObject(commentResponse);
                                    JSONArray commentListJson = json.getJSONArray("list");
                                    ArrayList<CommentListItem> commentList = new ArrayList<>();
                                    for(int i = 0; i < commentListJson.length(); i++)
                                    {
                                        try
                                        {
                                            JSONObject commentJson = commentListJson.getJSONObject(i);
                                            String owner = commentJson.getString("Owner");
                                            String fileId = commentJson.getString("File");
                                            String comment = commentJson.getString("Comment");
                                            long timestamp = commentJson.getLong("date");
                                            if(fileId.equals(item.mDatabaseId))
                                            {
                                                commentList.add(new CommentListItem(owner, comment, timestamp));
                                            }
                                        }
                                        catch(Exception e)
                                        {
                                            Log.d("TESTING", "Error getting comment");
                                        }
                                    }
                                    item.mComments = commentList;
                                }
                                catch(Exception e)
                                {
                                    Toast.makeText(ToastbrushApplication.getAppContext(), "Exception parsing server response", Toast.LENGTH_LONG).show();
                                }
                                mMutex.unlock();
                            }
                        });
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(ToastbrushApplication.getAppContext(), "Exception parsing server response", Toast.LENGTH_SHORT).show();
                }

            }
        };

        switch(type)
        {
            case "User":
                ToastbrushWebAPI.getImagesByUser(query, orderValue, 1000,0, currentUser, callback);
                break;
            case "Keyword":
                ToastbrushWebAPI.getImagesByKeyword(query, orderValue, 1000,  0, currentUser, callback);
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        image_option_menu(position);
    }

    private void image_option_menu(final int position)
    {
        try {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialoglayout = inflater.inflate(R.layout.online_image_dialog_view, null);
            final Button commentButton = dialoglayout.findViewById(R.id.comment_button);
            final EditText commentText = dialoglayout.findViewById(R.id.comment_text);
            final TextView title = dialoglayout.findViewById(R.id.image_dialog_title);
            final ImageView dialogIcon = dialoglayout.findViewById(R.id.image_dialog_icon);
            title.setText(mToastImageList.get(position).mFilename + " by " + mToastImageList.get(position).mOwner);
            Bitmap icon = mToastImageList.get(position).mIcon;
            icon = icon == null ? DrawingView.getBlankImage() : icon;
            dialogIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 200, 200, false));
            commentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getGoogleAccount() == null) {
                        Toast.makeText(ToastbrushApplication.getAppContext(), "Must be signed in to comment", Toast.LENGTH_SHORT).show();
                    } else {
                        final CommentListItem comment = new CommentListItem(getGoogleAccount().getEmail(), commentText.getText().toString(), System.currentTimeMillis());
                        ToastbrushWebAPI.addComment(mToastImageList.get(position).mDatabaseId, comment.mUsername, comment.mComment, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    if (json.getBoolean("Success")) {
                                        commentText.setText("");
                                        mToastImageList.get(position).mComments.add(comment);
                                        mToastImageList.get(position).mCommentListAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(ToastbrushApplication.getAppContext(), "Failed to add comment", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(ToastbrushApplication.getAppContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                                    Log.e("TESTING", e.toString());
                                    Log.e("TESTING", response);
                                }
                            }
                        });
                    }
                }
            });
            final TextView statsView = dialoglayout.findViewById(R.id.image_stats);
            statsView.setText("Score: " + mToastImageList.get(position).mScore);
            final Button upvoteButton = dialoglayout.findViewById(R.id.upvote_button);
            final Button downvoteButton = dialoglayout.findViewById(R.id.downvote_button);
            mBackground = upvoteButton.getBackground();
            upvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mToastImageList.get(position).mVote != ToastbrushWebAPI.VoteValue.UP_VOTE) {
                        upvote(position, upvoteButton, downvoteButton, statsView);
                    } else {
                        neutral_vote(position, upvoteButton, downvoteButton, statsView);
                    }
                }
            });

            downvoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mToastImageList.get(position).mVote != ToastbrushWebAPI.VoteValue.DOWN_VOTE) {
                        downvote(position, downvoteButton, upvoteButton, statsView);
                    } else {
                        neutral_vote(position, upvoteButton, downvoteButton, statsView);
                    }
                }
            });
            dialogBuilder.setView(dialoglayout);
            setupComments(dialoglayout, mToastImageList.get(position));
            if (getGoogleAccount() != null && (getGoogleAccount().getEmail().equals(mToastImageList.get(position).mOwner) || getGoogleAccount().getEmail().equals("m.mackliet@gmail.com"))) {
                dialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImage(position);
                    }
                });
            }
            dialogBuilder.setNegativeButton("Open", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    FileListItem listItem = mToastImageList.get(position);
                    String pkged_info = DatabaseHelper.packageImageInfo(listItem.mIcon, listItem.mPoints);
                    ((MainActivity) getActivity()).openImageInCreateImageFragment(pkged_info);
                }
            });

            dialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            if(getGoogleAccount() != null) {
                switch (mToastImageList.get(position).mVote) {
                    case UP_VOTE:
                        upvote(position, upvoteButton, downvoteButton, statsView);
                        break;
                    case DOWN_VOTE:
                        downvote(position, downvoteButton, upvoteButton, statsView);
                        break;
                    default:
                        neutral_vote(position, upvoteButton, downvoteButton, statsView);
                        break;
                }
            }
            dialogBuilder.show();
        }
        catch (Exception e)
        {
            Toast.makeText(ToastbrushApplication.getAppContext(), "Try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void neutral_vote(final int position, final Button upvoteButton, final Button downvoteButton, final TextView statsView)
    {
        if(getGoogleAccount() == null)
        {
            Toast.makeText(ToastbrushApplication.getAppContext(),"Must be signed in to vote", Toast.LENGTH_SHORT).show();
        }
        else {
            ToastbrushWebAPI.voteImage(mToastImageList.get(position).mDatabaseId, getGoogleAccount().getEmail(), ToastbrushWebAPI.VoteValue.NO_VOTE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("Success")) {
                            downvoteButton.setBackground(mBackground);
                            upvoteButton.setBackground(mBackground);
                            mToastImageList.get(position).mVote = ToastbrushWebAPI.VoteValue.NO_VOTE;
                            ToastbrushWebAPI.getImageInfo(mToastImageList.get(position).mDatabaseId, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        long score = json.getLong("Score");
                                        statsView.setText("Score: " + score);
                                        mToastImageList.get(position).mScore = score;

                                    } catch (Exception e) {

                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        Toast.makeText(ToastbrushApplication.getAppContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                        Log.e("TESTING", response);
                        Log.e("TESTING", e.toString());
                    }
                }
            });
        }
    }

    private void downvote(final int position, final Button downvoteButton, final Button upvoteButton, final TextView statsView) {
        if(getGoogleAccount() == null)
        {
            Toast.makeText(ToastbrushApplication.getAppContext(),"Must be signed in to vote", Toast.LENGTH_SHORT).show();
        }
        else {
            ToastbrushWebAPI.voteImage(mToastImageList.get(position).mDatabaseId, getGoogleAccount().getEmail(), ToastbrushWebAPI.VoteValue.DOWN_VOTE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("Success")) {
                            downvoteButton.setBackgroundColor(Color.RED);
                            upvoteButton.setBackground(mBackground);
                            mToastImageList.get(position).mVote = ToastbrushWebAPI.VoteValue.DOWN_VOTE;
                            ToastbrushWebAPI.getImageInfo(mToastImageList.get(position).mDatabaseId, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        long score = json.getLong("Score");
                                        statsView.setText("Score: " + score);
                                        mToastImageList.get(position).mScore = score;
                                    } catch (Exception e) {

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(ToastbrushApplication.getAppContext(), "Failed to downvote", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(ToastbrushApplication.getAppContext(), "Error parsing server response", Toast.LENGTH_SHORT).show();
                        Log.e("TESTING", response);
                        Log.e("TESTING", e.toString());
                    }
                }
            });
        }
    }

    private void upvote(final int position, final Button upvoteButton, final Button downvoteButton, final TextView statsView) {
        if(getGoogleAccount() == null)
        {
            Toast.makeText(ToastbrushApplication.getAppContext(),"Must be signed in to vote", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ToastbrushWebAPI.voteImage(mToastImageList.get(position).mDatabaseId, getGoogleAccount().getEmail(), ToastbrushWebAPI.VoteValue.UP_VOTE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try
                    {
                        JSONObject json = new JSONObject(response);
                        if(json.getBoolean("Success"))
                        {
                            upvoteButton.setBackgroundColor(Color.GREEN);
                            downvoteButton.setBackground(mBackground);
                            mToastImageList.get(position).mVote = ToastbrushWebAPI.VoteValue.UP_VOTE;
                            ToastbrushWebAPI.getImageInfo(mToastImageList.get(position).mDatabaseId, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try
                                    {
                                        JSONObject json = new JSONObject(response);
                                        long score = json.getLong("Score");
                                        statsView.setText("Score: " + score);
                                        mToastImageList.get(position).mScore = score;
                                    }
                                    catch (Exception e)
                                    {

                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(ToastbrushApplication.getAppContext(),"Failed to upvote", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(ToastbrushApplication.getAppContext(),"Error parsing server response", Toast.LENGTH_SHORT).show();
                        Log.e("TESTING", response);
                        Log.e("TESTING", e.toString());
                    }
                }
            });
        }
    }

    private void setupComments(View dialogLayout, FileListItem listItem)
    {
        CommentListAdapter commentListAdapter = new CommentListAdapter(getContext(), R.layout.comment_layout, listItem.mComments);
        listItem.mCommentListAdapter = commentListAdapter;
        ListView commentListView = dialogLayout.findViewById(R.id.comment_list);
        commentListView.setAdapter(commentListAdapter);
    }

    public void deleteImage(final int position)
    {
        final FileListItem image = mToastImageList.get(position);
        ToastbrushWebAPI.deleteImage(image.mDatabaseId, new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject json = new JSONObject(response);
                    if(json.getBoolean("success"))
                    {
                        mToastImageList.remove(position);
                        mListAdapter.notifyDataSetChanged();
                        Toast.makeText(ToastbrushApplication.getAppContext(),"Successfully deleted " + image.mFilename, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ToastbrushApplication.getAppContext(),"Failed to delete " + image.mFilename, Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(ToastbrushApplication.getAppContext(),"Error parsing server response", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
