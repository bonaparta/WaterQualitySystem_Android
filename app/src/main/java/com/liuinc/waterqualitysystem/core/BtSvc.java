package com.liuinc.waterqualitysystem.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

public class BtSvc implements IBtSearchPairing {
    private String TAG = this.getClass().getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBlueToothDevice;
    private BluetoothSocket mBlueToothSocket;
    private OutputStream mBlueToothOutputStream;
    private InputStream mBlueToothInputStream;
    private Thread mWorkerThread;

    // Data
    private boolean mStopWorker;
    private String mDeviceId = "";
    private String mDeviceName = "";
    private String mThinkSpeakId = "";
    public int mErrno = 0;

    public static final int ERR_BT_DEVICE_FOUND = 1;
    public static final int ERR_BT_NOT_SUPPORT = -1;
    public static final int ERR_BT_TURN_OFF = -2;
    public static final int ERR_BT_NOT_CONNECTED = -3;

    public static final int DEVICE_CALIBRATION_PH7 = 0;
    public static final int DEVICE_CALIBRATION_PH4 = 1;
    public static final int DEVICE_CALIBRATION_DO = 2;
    public static final int DEVICE_SET_WIFI = 3;
    public static final int DEVICE_GET_ID = 4;

    public static final String[] WATER_QUALITY_DEVICE = new String[] { "pH7\n", "pH4\n", "DO_0\n", "wifi_setting\n", "Serial\n" };

    public BtSvc() {
    }

    @Override
    public int onDeviceCallback(int returnCode) {
        return 0;
    }

    public int resetDevice() {
        mStopWorker = true;
        if(mWorkerThread != null) {
            try {
                mWorkerThread.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(mBlueToothSocket != null) {
            try {
                mBlueToothSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public int getDevice(String device, IBtSearchPairing callback) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            return ERR_BT_NOT_SUPPORT;
        if (!mBluetoothAdapter.isEnabled())
            return ERR_BT_TURN_OFF;

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice it : devices) {
                mBlueToothDevice = it;
                if (mBlueToothDevice.getAddress().equals(device)) {
                    openBT();
                    mBluetoothAdapter.cancelDiscovery();
                    return ERR_BT_DEVICE_FOUND;
                }
            }
        }
        return ERR_BT_NOT_CONNECTED;
    }

    public String[] getDeviceIds() {
        if (mThinkSpeakId.length() > 0)
            return new String[] {mDeviceId, mDeviceName, mThinkSpeakId};
        return new String[0];
    }

    public int sendPh7Command() {
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH7]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH7]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH7]);
        return 0;
    }

    public int sendPh4Command() {
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH4]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH4]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_PH4]);
        return 0;
    }

    public int sendDOCommand() {
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_DO]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_DO]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_CALIBRATION_DO]);
        return 0;
    }

    public int sendWifiSettingCommand() {
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_SET_WIFI]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_SET_WIFI]);
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_SET_WIFI]);
        return 0;
    }

    public int sendThinkSpeakIdGetCommand() {
        sendCommand(WATER_QUALITY_DEVICE[DEVICE_GET_ID]);
        return 0;
    }

    public int getUnknownDevice(String[] deviceIds, IBtSearchPairing callback) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            return ERR_BT_NOT_SUPPORT;
        if (!mBluetoothAdapter.isEnabled())
            return ERR_BT_TURN_OFF;

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (BluetoothDevice it : devices) {
                mBlueToothDevice = it;
                boolean hit = false;
                for(String device : deviceIds) {
                    if (mBlueToothDevice.getAddress().equals(device)) {
                        hit = true;
                        break;
                    }
                }
                if (!hit) {
                    mDeviceId = mBlueToothDevice.getAddress();
                    mDeviceName = mBlueToothDevice.getName();
                    int retCode = openBT();
                    if (retCode != ERR_BT_DEVICE_FOUND) {
                        return ERR_BT_NOT_CONNECTED;
                    }
                    mBluetoothAdapter.cancelDiscovery();
                    int error = callback.onDeviceCallback(ERR_BT_DEVICE_FOUND);
                    if (error != 0) {
                        mErrno = ERR_BT_DEVICE_FOUND;
                    }
                    return ERR_BT_DEVICE_FOUND;
                }
            }
        }
        mBluetoothAdapter.cancelDiscovery();
        return ERR_BT_NOT_CONNECTED;
    }

    private int openBT()
    {
        ParcelUuid[] uuids = mBlueToothDevice.getUuids();
        try {
            mBlueToothSocket = mBlueToothDevice.createRfcommSocketToServiceRecord(uuids[0].getUuid());
            if(mBlueToothSocket == null) {
                return ERR_BT_NOT_CONNECTED;
            }
            mBlueToothSocket.connect();
            mBlueToothOutputStream = mBlueToothSocket.getOutputStream();
            mBlueToothInputStream = mBlueToothSocket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return ERR_BT_NOT_CONNECTED;
        }

        beginListenForData();
        return ERR_BT_DEVICE_FOUND;
    }

    private int readBufferPosition;
    private byte[] readBuffer;
    private void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        mStopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        mWorkerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !mStopWorker)
                {
                    try
                    {
                        int bytesAvailable = mBlueToothInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mBlueToothInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    mThinkSpeakId = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        mStopWorker = true;
                    }
                }
            }
        });

        mWorkerThread.start();
    }

    private int sendCommand(String message) {
        try {
            mBlueToothOutputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
        return 0;
    }
}
