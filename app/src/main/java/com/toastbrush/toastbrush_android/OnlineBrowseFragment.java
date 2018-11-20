package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.toastbrush.ToastbrushApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
    private JSONArray mQueriedImages;
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
                        item.mDatabaseId = imageInfo.getString("Image");
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
                ToastbrushWebAPI.getImagesByUser(query, orderValue, 1000, 0, callback);
                break;
            case "Keyword":
                ToastbrushWebAPI.getImagesByKeyword(query, orderValue, 1000, 0, callback);
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
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(this.getContext());
        saveDialog.setTitle("Image Options");
        saveDialog.setMessage("What would you like to do with the image?");
        saveDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which)
            {
                deleteImage(position);
            }
        });
        saveDialog.setNegativeButton("Open", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                FileListItem listItem = mToastImageList.get(position);
                String pkged_info = DatabaseHelper.packageImageInfo(listItem.mIcon, listItem.mPoints);
                ((MainActivity)getActivity()).openImageInCreateImageFragment(pkged_info);
            }
        });

        saveDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        saveDialog.show();
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
