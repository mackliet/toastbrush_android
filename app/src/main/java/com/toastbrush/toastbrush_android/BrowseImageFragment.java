package com.toastbrush.toastbrush_android;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BrowseImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BrowseImageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private ViewPager mViewPager;
    private BrowsePagerAdapter mPagerAdapter;

    public BrowseImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_view = inflater.inflate(R.layout.fragment_browse_image, container, false);
        ArrayList<FileListItem> toastImageList = new ArrayList<>();
        File directory = new File(getContext().getFilesDir(), "Toast_images");
        directory.mkdirs();
        File[] files = directory.listFiles();
        for(File file : files)
        {
            String filename = file.getName();
            if(filename.endsWith(".tst"))
            {
                Log.d("TESTING", "FOUND FILE:" + file.getAbsolutePath());
                String file_no_extension = filename.substring(0, filename.lastIndexOf('.'));
                FileListItem item = new FileListItem(file_no_extension);
                item.mTimestamp = file.lastModified();
                toastImageList.add(item);
            }
        }
        FileListAdapter listAdapter = new FileListAdapter(getContext(), R.layout.file_list_item, toastImageList);
        ListView list_view = (ListView) this_view.findViewById(R.id.file_list);
        list_view.setAdapter(listAdapter);
        return this_view;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
