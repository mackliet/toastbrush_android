package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BrowseImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BrowseImageFragment extends Fragment implements AdapterView.OnItemClickListener {
    private OnFragmentInteractionListener mListener;
    private ListView mListView;
    private ArrayList<FileListItem> mToastImageList;
    private FileListAdapter mListAdapter;

    public BrowseImageFragment() {
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
        View this_view = inflater.inflate(R.layout.fragment_browse_image, container, false);
        mToastImageList = DatabaseHelper.getFileListItems();
        mListAdapter = new FileListAdapter(getContext(), R.layout.file_list_item, mToastImageList);
        mListView = this_view.findViewById(R.id.file_list);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mListAdapter);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
    {
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
                String image_name = mToastImageList.get(position).mFilename;
                String pkged_info = DatabaseHelper.getImageData(image_name);
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

    public void deleteImage(int position)
    {
        FileListItem image = mToastImageList.get(position);
        mToastImageList.remove(position);
        mListAdapter.notifyDataSetChanged();
        DatabaseHelper.deleteToastImage(image.mFilename);
        Toast.makeText(getContext(),"Deleted " + image.mFilename,Toast.LENGTH_SHORT).show();
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
