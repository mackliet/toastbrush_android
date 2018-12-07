package com.toastbrush.toastbrush_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.toastbrush.ToastbrushApplication;

import static com.toastbrush.ToastbrushApplication.getAppContext;
import static com.toastbrush.ToastbrushApplication.getGoogleAccount;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CreateImageFragment extends Fragment implements View.OnClickListener {
    private Button mClearButton;
    private Button mSaveButton;
    private Button mToastButton;
// ...
    private com.toastbrush.toastbrush_android.DrawingView mDrawingView;

    public CreateImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Instantiate things
        View mThis = inflater.inflate(R.layout.fragment_create_image, container, false);
        mDrawingView = mThis.findViewById(R.id.drawing_view);
        mClearButton = mThis.findViewById(R.id.draw_clear_button);
        mSaveButton = mThis.findViewById(R.id.draw_save_button);
        mToastButton = mThis.findViewById(R.id.draw_send_button);
        mClearButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mToastButton.setOnClickListener(this);
        Bundle bundle = this.getArguments();
        String image_data = bundle == null ? null : bundle.getString("Image_data");
        if(image_data != null)
        {
            mDrawingView.setImage(image_data);
            bundle.remove("Image_data");
        }
        ToastbrushApplication.setupBluetoothServer(getActivity());
        final Handler handler = new Handler();
// Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                String state = ToastbrushApplication.getBluetoothServer().getState();
                Log.d("STATE", state);
                if(state.equals("Toasting") || state.equals("Sending image"))
                {
                    mToastButton.setText("Cancel");
                }
                else
                {
                    mToastButton.setText("Toast");
                }
                handler.postDelayed(this, 500);
            }
        };
        // Run the above code block on the main thread after 1 seconds
        handler.post(runnableCode);
        return mThis;
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.draw_clear_button:
                mDrawingView.clear_canvas();
                break;
            case R.id.draw_save_button:
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this.getContext());
                saveDialog.setTitle("Save Toast Image");
                saveDialog.setMessage("Save image to for future toasting?");
                saveDialog.setPositiveButton("Local Save", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        create_save_dialog(SaveType.LOCAL);
                    }
                });
                if(getGoogleAccount() != null) {
                    saveDialog.setNegativeButton("Database Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            create_save_dialog(SaveType.DATABASE);
                        }
                    });
                }

                saveDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                saveDialog.show();
                break;
            case R.id.draw_send_button:
                ToastbrushApplication.setupBluetoothServer(getActivity()).connectGATT();
                if(!ToastbrushApplication.getBluetoothServer().isConnected())
                {
                    Toast.makeText(getContext(),"Not connected to toaster",Toast.LENGTH_SHORT).show();
                }
                else if(mToastButton.getText().toString().equals("Cancel"))
                {
                    ToastbrushApplication.getBluetoothServer().cancel_print();
                    Toast.makeText(getAppContext(),"Cancelling Toast Print",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String gCode = GCodeBuilder.convertToGcode(mDrawingView.getToastPoints());
                    ToastbrushApplication.getBluetoothServer().sendData(gCode);
                    Toast.makeText(getAppContext(),"Image sent to toaster",Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
        }
    }

    enum SaveType
    {
        LOCAL,
        DATABASE
    }

    private void create_save_dialog(final SaveType save_type)
    {
        String title = "";
        switch(save_type)
        {
            case LOCAL:
                title = "Local Save";
                break;
            case DATABASE:
                title = "Database Save";
                break;
            default:
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle(title);
        builder.setMessage("Input name for toast image");

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View dialoglayout = inflater.inflate(R.layout.save_dialog, null);
        builder.setView(dialoglayout);
        final EditText filenameInput = dialoglayout.findViewById(R.id.save_dialog_filename);
        final EditText descriptionInput = dialoglayout.findViewById(R.id.save_dialog_description);

        // Set up the buttons
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        //Overriding the handler immediately after show is probably a better approach than OnShowListener as described below
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String filename = filenameInput.getText().toString();
                String description = descriptionInput.getText().toString();
                if(filename.equals(""))
                {
                    Toast.makeText(getContext(),"Input an image name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    switch(save_type)
                    {
                        case LOCAL:
                            mDrawingView.save_canvas_local(filename, description);
                            Toast.makeText(getContext(),"Successfully saved file",Toast.LENGTH_SHORT).show();
                            break;
                        case DATABASE:
                            if(getGoogleAccount() == null)
                            {
                                Toast.makeText(getContext(),"Logged out. Can't save to database.",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                mDrawingView.save_canvas_database(filename, getGoogleAccount().getEmail(), description);
                            }
                            break;
                    }
                    dialog.dismiss();
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
    interface OnFragmentInteractionListener {
    }
}
