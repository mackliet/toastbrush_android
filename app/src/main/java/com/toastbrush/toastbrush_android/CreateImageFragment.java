package com.toastbrush.toastbrush_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CreateImageFragment extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    private Button mClearButton;
    private Button mSaveButton;
    private Button mToastButton;
    private BLEGatt mBluetooth;
// ...
    private com.toastbrush.toastbrush_android.DrawingView mDrawingView;
    private boolean LED_ON = true;

    public CreateImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Instantiate things
        View mThis = inflater.inflate(R.layout.fragment_create_image, container, false);
        mBluetooth = new BLEGatt(getContext());
        mDrawingView = (com.toastbrush.toastbrush_android.DrawingView)mThis.findViewById(R.id.drawing_view);
        mClearButton = (Button) mThis.findViewById(R.id.draw_clear_button);
        mSaveButton = (Button) mThis.findViewById(R.id.draw_save_button);
        mToastButton = (Button) mThis.findViewById(R.id.draw_send_button);
        mClearButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mToastButton.setOnClickListener(this);
        return mThis;
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
                saveDialog.setNegativeButton("Database Save", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        create_save_dialog(SaveType.DATABASE);
                    }
                });

                saveDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                saveDialog.show();
                break;
            case R.id.draw_send_button:
                mBluetooth.connectGATT();
                if(LED_ON)
                {
                    mBluetooth.sendData("A");
                }
                else
                {
                   mBluetooth.sendData("B");
                }
                LED_ON = !LED_ON;
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

        // Set up the input textbox
        final EditText input = new EditText(this.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

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
                String filename = input.getText().toString();

                if(filename.equals(""))
                {
                    Toast.makeText(getContext(),"Input an image name",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    switch(save_type)
                    {
                        case LOCAL:
                            mDrawingView.save_canvas_local(filename);
                            break;
                        case DATABASE:
                            mDrawingView.save_canvas_database(filename);
                            break;
                    }
                    dialog.dismiss();
                    Toast.makeText(getContext(),"Successfully saved file",Toast.LENGTH_SHORT).show();
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
