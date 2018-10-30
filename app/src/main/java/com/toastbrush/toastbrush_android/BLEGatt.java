package com.toastbrush.toastbrush_android;

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
import android.os.Handler;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.UUID;

import static com.android.volley.VolleyLog.TAG;

public class BLEGatt extends BluetoothGattCallback {

    private Handler mHandler;
    private boolean mScanning;
    private UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID RX_CHAR_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID TX_CHAR_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private String data;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int PERMISSIONS_GRANTED = 10;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int REQUEST_ENABLE_BT = 12;
    private String DEVICE_ADDRESS = "30:AE:A4:BB:F4:2A";
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 3000;

    private boolean writeInProgress;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public BLEGatt(Context context) {
        super();
        mContext = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)mContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mHandler = new Handler();
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }, SCAN_PERIOD);
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        mContext = context;
    }

    public void connectGATT()
    {
        if(mDevice != null && mConnectionState == STATE_DISCONNECTED) {
            mBluetoothGatt = mDevice.connectGatt(mContext, true, mGattCallback);
        }
    }

    public int getmConnectionState()
    {
        return mConnectionState;
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        //broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        try {
                            Thread.sleep(600);
                        }catch(Exception e){}
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        //broadcastUpdate(intentAction);
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
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d(TAG,"Characteristic write successful");
                    }
                    writeInProgress = false;
                }
            };

    public String getData()
    {
        return data;
    }

    public void sendData(String s)
    {
        if(mConnectionState == STATE_CONNECTED) {
            byte[] data = s.getBytes(Charset.forName("UTF-8"));
            rx.setValue(data);
            writeInProgress = true; // Set the write in progress flag
            mBluetoothGatt.writeCharacteristic(rx);
        }
        //while (writeInProgress); // Wait for the flag to clear in onCharacteristicWrite
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    // getActivity().runOnUiThread(new Runnable() {
                    //  @Override
                    //   public void run() {
                    if(device.getAddress().equals(DEVICE_ADDRESS)){
                        mDevice = device;
                        mBluetoothAdapter.stopLeScan(this);
                        //testing.add(device.getAddress());
                        //mTextView.setText("TEST: " + device.getAddress());
                    }
                    //  }
                    //});
                }
            };
}
