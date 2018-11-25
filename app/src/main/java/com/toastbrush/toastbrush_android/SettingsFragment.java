package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.toastbrush.ToastbrushApplication;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements AdapterView.OnItemClickListener {
    private OnFragmentInteractionListener mListener;
    private ArrayList<SettingsListItem> mSettingsList;
    private ListView mListView;
    private SettingsListAdapter mListAdapter;
    private Handler mHandler;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View this_view = inflater.inflate(R.layout.fragment_settings, container, false);
        mSettingsList = new ArrayList<>();
        addSettings(mSettingsList);
        mListAdapter = new SettingsListAdapter(getContext(), R.layout.settings_list_item, mSettingsList);
        mListView = this_view.findViewById(R.id.settings_list);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mListAdapter);
        mHandler = new Handler();
// Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                String lastState = mSettingsList.get(1).mDescription;
                String newState = ToastbrushApplication.getBluetoothServer().getState();
                if(!lastState.equals(newState))
                {
                    mSettingsList.get(1).mDescription = newState;
                    mListAdapter.notifyDataSetChanged();
                }
                mHandler.postDelayed(this, 1000);
            }
        };
    // Run the above code block on the main thread after 1 seconds
        mHandler.post(runnableCode);
        return this_view;
    }

    private void addSettings(ArrayList<SettingsListItem> mSettingsList)
    {
        String darkness = DatabaseHelper.getSetting("Toast Darkness");
        mSettingsList.add(new SettingsListItem("Toast Darkness", darkness == null ? "1" : darkness));
        mSettingsList.add(new SettingsListItem("Toaster Status", "Not Connected"));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if(mSettingsList.get(position).mName.equals("Toast Darkness"))
        {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Set Toast Darkness");
            final String[] types = {"1", "2", "3", "4", "5"};
            b.setItems(types, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    dialog.dismiss();
                    mSettingsList.get(0).mDescription = types[which];
                    DatabaseHelper.setSetting("Toast Darkness", types[which]);
                    mListAdapter.notifyDataSetChanged();
                }
            });
            b.show();
        }
        else if(mSettingsList.get(position).mName.equals("Toaster Status"))
        {
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Try Reconnecting to toaster?");
            b.setNegativeButton("Reconnect", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    ToastbrushApplication.setupBluetoothServer(getActivity()).connectGATT();
                }
            });

            b.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });

            b.show();
        }
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
