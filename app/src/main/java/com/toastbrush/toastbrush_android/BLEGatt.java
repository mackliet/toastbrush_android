package com.toastbrush.toastbrush_android;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import static com.android.volley.VolleyLog.TAG;
import static com.toastbrush.ToastbrushApplication.getAppContext;
import static java.lang.Integer.parseInt;

public class BLEGatt extends BluetoothGattCallback {

    private final BluetoothManager mBluetoothManager;
    private UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID RX_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID TX_CHAR_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private String data;
    private int mPacketCounter;
    private boolean mPrinting;
    private boolean mWaitingToStart;
    private Queue<String> mSendQueue;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int REQUEST_ENABLE_BT = 12;
    private String DEVICE_ADDRESS = "30:AE:A4:BB:F4:2A";

    private boolean writeInProgress;

    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;

    @SuppressWarnings("SameParameterValue")
    private UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        return new UUID(MSB | (((long)i) << 32), LSB);
    }

    public BLEGatt() {
        super();
        mBluetoothManager =
                (BluetoothManager) getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(mBluetoothManager).getAdapter();
        mSendQueue = new LinkedBlockingDeque<>();
        mPacketCounter = -1;
        mPrinting = false;
        mWaitingToStart = false;
    }

    public BLEGatt enableBluetooth(Activity context)
    {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mConnectionState = STATE_DISCONNECTED;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(context, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                context.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        return this;
    }

    public void connectGATT()
    {
        if(mConnectionState == STATE_DISCONNECTED && mBluetoothAdapter.isEnabled())
        {
            Set<BluetoothDevice> connectedDevices = mBluetoothAdapter.getBondedDevices();
            mDevice = null;
            for (BluetoothDevice device : connectedDevices) {
                if (device.getAddress().equals(DEVICE_ADDRESS)) {
                    mDevice = device;
                }
            }
        }

        if(mDevice == null)
        {
            Toast.makeText(getAppContext(),"Not paired with toaster. Open bluetooth settings and pair before trying to connect",Toast.LENGTH_SHORT).show();
        }

        if(mDevice != null && mConnectionState == STATE_DISCONNECTED) {
            mConnectionState = STATE_CONNECTING;
            mBluetoothGatt = mDevice.connectGatt(getAppContext(), true, mGattCallback);
        }
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback;

    {
        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                    try {
                        Thread.sleep(600);
                        mConnectionState = STATE_CONNECTED;
                    } catch (Exception e) {
                        Log.i(TAG, "Exception in onConnectionStateChange: " + e.getMessage());
                    }
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.i(TAG, "Service Discovered");
                super.onServicesDiscovered(gatt, status);
                tx = gatt.getService(SERVICE_UUID).getCharacteristic(TX_CHAR_UUID);
                rx = gatt.getService(SERVICE_UUID).getCharacteristic(RX_CHAR_UUID);

                gatt.setCharacteristicNotification(tx, true);

                BluetoothGattDescriptor desc = tx.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                Log.i(TAG, "Getting value: " + desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
                //desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                Log.i(TAG, "Write descriptor: " + gatt.writeDescriptor(desc));
                //gatt.writeDescriptor(desc);

            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.i(TAG, characteristic.getStringValue(0) + "  " + characteristic.toString());
                data = characteristic.getStringValue(0);
                Log.d("TOASTER", "RECEIVED: " + data);
                try {
                    if (data.equals("start")) {
                        mPrinting = true;
                        mWaitingToStart = false;
                        Toast.makeText(getAppContext(), "Started Toasting", Toast.LENGTH_SHORT).show();

                    } else if (data.equals("cancelled")) {
                        mPrinting = false;
                        Toast.makeText(getAppContext(), "Cancelled Toast Job", Toast.LENGTH_SHORT).show();
                    } else if (data.equals("end")) {
                        mPrinting = false;
                        Toast.makeText(getAppContext(), "Finished Toasting", Toast.LENGTH_SHORT).show();
                    } else {
                        int requested = parseInt(data);
                        if (requested > mPacketCounter && !writeInProgress) {
                            sendChunk();
                        }
                    }
                } catch (Exception e) {
                    Log.e("TESTING", "Failed to parse packet");
                }

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Characteristic write successful");
                }
                writeInProgress = false;
            }
        };
    }

    public boolean isConnected()
    {
        boolean ret_val = mDevice != null && mConnectionState == STATE_CONNECTED && mBluetoothAdapter.isEnabled();
        if(!mBluetoothAdapter.isEnabled() || mDevice == null)
        {
            mConnectionState = STATE_DISCONNECTED;
        }
        return  ret_val;
    }
    public boolean readyToSend() { return mSendQueue.isEmpty() && !mWaitingToStart && isConnected() && !mPrinting;}

    public String getData()
    {
        return data;
    }

    public void cancel_print()
    {
        mSendQueue.clear();
        mPacketCounter = -1;
        mWaitingToStart = false;
        send("CANCEL");
    }

    public void sendData(String s)
    {
        ArrayList<String> instructions = new ArrayList<>(Arrays.asList(s.split("\n")));
        String compressed;
        if(instructions.size() > 0)
        {
            mWaitingToStart = true;
        }
        for(String instruction : instructions)
        {
            if(instruction.equals("M30"))
            {
                compressed = "M";
            }
            else
            {
                String[] insts = instruction.split(" ");
                int x = Integer.parseInt(insts[1].substring(1));
                int y = Integer.parseInt(insts[2].substring(1));
                compressed = "0";
                if(insts[0].equals("G01"))
                {
                    compressed = "1";
                    int f = Integer.parseInt(insts[3].substring(1));
                    compressed += "" + (char)x + (char)y + (char)f;

                    Log.e("COMPRESSED\n", instruction + "\n" + "x=" + (int)((char)x) + "y=" + (int)((char)y) + "f=" + (int)((char)f) + "\n" + compressed + "\n" + compressed.length() + "bytes");
                }
                else {
                    compressed += "" + (char) x + (char) y;
                    Log.e("COMPRESSED\n", instruction + "\n" + "x=" + (int)((char)x) + "y=" + (int)((char)y) + "\n" + compressed + "\n" + compressed.length() + "bytes");

                }
            }
            mSendQueue.add(compressed + "\n");
        }
    }

    private void sendChunk()
    {
        Log.d("TESTING", "Sending chunk");
        StringBuilder data_to_send = new StringBuilder();
        while(!mSendQueue.isEmpty())
        {
            String test_string = data_to_send + mSendQueue.peek();
            if(test_string.length() > 580)
            {
                break;
            }
            else
            {
                data_to_send.append(mSendQueue.poll());
            }
        }
        if(data_to_send.toString().equals("")) {
            return;
        }
        send(data_to_send.toString());
        try
        {
            Thread.sleep(4000);
        }
        catch(Exception e)
        {
            Log.e("TESTING", "Exception in sendChunk: " + e.getMessage());
        }
        if(data_to_send.toString().contains("M"))
        {
            mPacketCounter = -1;
        }
        else
        {
            ++mPacketCounter;
        }
    }

    private void send(String s)
    {
        Log.d("TESTING", "Sending:\n" + s);
        if(mConnectionState == STATE_CONNECTED && !writeInProgress && rx != null) {
            byte[] data = s.getBytes(Charset.forName("ISO-8859-1"));
            rx.setValue(data);
            writeInProgress = true; // Set the write in progress flag
            mBluetoothGatt.writeCharacteristic(rx);
        }
    }

    public String getState()
    {
        String state = "Not Connected";
        if(isConnected())
        {
            if(readyToSend())
            {
                state = "Ready to toast";
            }
            else if (mPrinting)
            {
                state = "Toasting";
            }
            else if (!mSendQueue.isEmpty() || mWaitingToStart)
            {
                state = "Sending image";
            }
            else
            {
                state = "Waiting for toaster...";
            }
        }
        return state;
    }
}
