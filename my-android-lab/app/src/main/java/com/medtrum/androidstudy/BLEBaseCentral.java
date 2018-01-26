package com.medtrum.androidstudy;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public abstract class BLEBaseCentral {
    public static final String LOCAL_BROADCAST_SCAN_NEW_DEVICE = "com.medtrum.healthcareforandroid.ble.LOCAL_BROADCAST.SCAN_NEW_DEVICE";
    public static final String LOCAL_BROADCAST_DEVICE_STATE_CHANGE = "com.medtrum.healthcareforandroid.ble.LOCAL_BROADCAST.DEVICE_STATE_CHANGE";

    protected static Handler sHandler;
    protected static Handler pHandler;

    private boolean mIsWritingValue = false;
    private byte[] mWillWritingValue;
    private boolean mIsReadingValue = false;
    private byte[] mHaveReadingValue;
    private int mReadingValueLength = 0;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Object mScanCallback;
    private OnDeviceScanListener mOnDeviceScanListener;
    private OnCharacteristicValueReceiveListener mOnCharacteristicValueReceiveListener;
    private OnOperationPrepareListener mOnOperationPrepareListener;
    private OnStateChangeListener mOnStateChangeListener;
    private OnReadRssiListener mOnReadRssiListener;
    private static final String TAG = BLEBaseCentral.class.getSimpleName();
    private State mState = State.Idle;
    private static final String MEDTRUM_DEVICE_NAME = "MT";
    private static final int MEDTRUM_COMPANY_IDENTIFIER = 0x4781;
    public static final int MEDTRUM_DEVICE_TYPE_TY012 = 0xA5;
    public static final int MEDTRUM_DEVICE_TYPE_TY015 = 0xA7;
    public static final int MEDTRUM_DEVICE_TYPE_JN012 = 0x33;
    public static final int MEDTRUM_DEVICE_TYPE_JN013 = 0x13;
    public static final int MEDTRUM_DEVICE_TYPE_FM011 = 0x11;
    private static final int MAX_CHARACTERISTIC_VALUE_LENGTH = 20;

    protected static final String MEDTRUM_BASE_UUID = "669A%04x-0008-968F-E311-6050405558B3";
    private static final int MEDTRUM_UUID_CHARACTERISTIC_CTRLPT = 0x9101;
    protected static final int MEDTRUM_UUID_CHARACTERISTIC_IPS_RTN = 0x9120;
    protected static final int MEDTRUM_UUID_CHARACTERISTIC_CGM_DATA = 0x9140;
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    protected static final int DEVICE_AUTHORIZE_REQUEST_COMMAND_CODE = 0x05;
    protected static final int DEVICE_AUTHORIZE_REQUEST_RESPONSE_CODE = 0x95;

    protected int mScanMode = 0;
    private int mDeviceId;
    private int mDeviceType;
    protected int mDeviceVersion;
    private int mRSSI;
    protected BluetoothDevice mBluetoothDevice;
    protected UUID mServiceUuid;
    protected static final UUID sCharacteristicCtrlPointUuid = UUID.fromString(String.format(MEDTRUM_BASE_UUID, MEDTRUM_UUID_CHARACTERISTIC_CTRLPT));
    protected Set<Integer> mDeviceCategory;
    private List<Integer> mDeviceIdArray = new ArrayList<>();
    public enum State {
        Idle,
        Scanning,
        //        Disconnected,
        Connecting,
        Connected,
        Disconnecting,
        Closed,
        Lost,
    }

    public interface OnDeviceScanListener {
        void onDeviceScan(int deviceId, int deviceType, float version, byte[] payload);
    }

    public interface OnCharacteristicValueReceiveListener {
        void onNotificationReceive(byte[] data);

        void onIndicationReceive(byte[] data);
    }

    public interface OnOperationPrepareListener {
        void OnOperationPrepare();
    }

    public interface OnStateChangeListener {
        void OnStateChange(State newState);
    }

    public interface OnReadRssiListener {
        void OnReadRssi(int status);
    }

    /**
     * Utils
     */
    public static int bytesToInt(byte[] value) {
        switch (value.length) {
            case 1:
                // Convert a signed byte to an unsigned int.
                return value[0] & 0xFF;

            case 2:
                // Convert signed bytes to a 16-bit unsigned int.
                return (value[0] & 0xFF) + ((value[1] & 0xFF) << 8);

            case 3:
                return (value[0] & 0xFF) + ((value[1] & 0xFF) << 8)
                        + ((value[2] & 0xFF) << 16);

            case 4:
                // Convert signed bytes to a 32-bit unsigned int.
                return (value[0] & 0xFF) + ((value[1] & 0xFF) << 8)
                        + ((value[2] & 0xFF) << 16) + ((value[3] & 0xFF) << 24);

            default:
                return 0;
        }
    }

    public State getState() {
        return mState;
    }

    private static byte[] addLengthToPrefix(byte[] data) {
        int dataLength = data.length;
        byte[] returnData = new byte[dataLength + 2];
        returnData[0] = (byte) (dataLength % 256);
        returnData[1] = (byte) (dataLength / 256);
        System.arraycopy(data, 0, returnData, 2, data.length);
        return returnData;
    }

    private void writeCharacteristicValue(UUID characteristicUuid, byte[] value) {
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(mServiceUuid);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
            if (characteristic != null) {
                if (value.length > 0) {
//                    Log.d(TAG, "writeCharacteristicValue value = " + Arrays.toString(value));
                    characteristic.setValue(value);
                    mBluetoothGatt.writeCharacteristic(characteristic);
                }
            }
        }
    }

    public void writeCommand(byte[] bytes) {
        if (bytes.length == 0) {
            Log.w(TAG, "writeCommand commandData is empty");
            return;
        }

        if (!mIsWritingValue) {
            bytes = addLengthToPrefix(bytes);
            Log.d(TAG, "writeCommand commandData = " + Arrays.toString(bytes));
        }

        byte[] value;
        if (bytes.length > MAX_CHARACTERISTIC_VALUE_LENGTH) {
            value = MyScanRecord.extractBytes(bytes, 0, MAX_CHARACTERISTIC_VALUE_LENGTH);
            mIsWritingValue = true;
            mWillWritingValue = MyScanRecord.extractBytes(bytes, MAX_CHARACTERISTIC_VALUE_LENGTH, bytes.length - MAX_CHARACTERISTIC_VALUE_LENGTH);
        } else {
            value = bytes;
            mIsWritingValue = false;
            mWillWritingValue = null;
        }
        writeCharacteristicValue(sCharacteristicCtrlPointUuid, value);
    }

    public static byte[] bytesCombine(byte[] bytes1, byte[] bytes2) {
        byte[] returnBytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, returnBytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, returnBytes, bytes1.length, bytes2.length);
        return returnBytes;
    }

    private void parseCharacteristicValue(byte[] data) {
        if (data != null) {
            if (!mIsReadingValue) {
                byte[] dataLengthBytes = new byte[2];
                int offset = 0;
                dataLengthBytes = MyScanRecord.extractBytes(data, offset, dataLengthBytes.length);
                offset += dataLengthBytes.length;

                int dataLength = bytesToInt(dataLengthBytes);
                if (dataLength > (MAX_CHARACTERISTIC_VALUE_LENGTH - offset)) {
                    mIsReadingValue = true;
                    mReadingValueLength = dataLength;
                    mHaveReadingValue = MyScanRecord.extractBytes(data, offset, MAX_CHARACTERISTIC_VALUE_LENGTH - offset);
                } else {
                    if (mOnCharacteristicValueReceiveListener != null) {
                        byte[] resultData = MyScanRecord.extractBytes(data, offset, dataLength);
                        mOnCharacteristicValueReceiveListener.onIndicationReceive(resultData);
                    }
                }
            } else {
                mHaveReadingValue = bytesCombine(mHaveReadingValue, MyScanRecord.extractBytes(data, 0, data.length));
                if (mHaveReadingValue.length >= mReadingValueLength) {
                    mIsReadingValue = false;
                    mReadingValueLength = 0;
                    if (mOnCharacteristicValueReceiveListener != null) {
                        mOnCharacteristicValueReceiveListener.onIndicationReceive(mHaveReadingValue);
                    }
                } else {
                    Log.e(TAG, "parseCharacteristicValue: 接收数据长度不匹配！！！");
                }
            }
        }
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            mBluetoothGatt = gatt;
            Log.i(TAG, "onConnectionStateChange. status = " + status + " newState = " + newState);
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                if (getState().equals(State.Connected) || getState().equals(State.Connecting)) {
                    setState(State.Idle);

                    /*
                    if ((retryConnectTimes >= 0 || mIsSendingCalibrateCommand || mIsNeedWriteCommand)) {
                        if (MyApplication.getAppType() == 0) {
                            if (!MyApplication.isOpenUpload()) {
                                Log.i(TAG, "onConnectionStateChange: 断开重连");
                                close();
                                connectDevice(gatt.getDevice());
                                retryConnectTimes--;
                            }
                        } else {
                            close();
                            connectDevice(gatt.getDevice());
                            retryConnectTimes--;
                        }
                    } else {
                        close();
                        justDisableConnect();
//                        retryConnectTimes=3;
                    } */

                } else {
                    setState(State.Idle);
                }

            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");


                //sHandler.removeCallbacks(sReconnectRunnable);
                if (status == 133) {
                    /*
                    if ((retryConnectTimes >= 0 || mIsSendingCalibrateCommand || mIsNeedWriteCommand) && !PDMInstance.getInstance().isUploadData()) {
                        Log.i(TAG, "onConnectionStateChange: 连接中断开重连！");
                        refreshDeviceCache();
                        close();
                        connectDevice(gatt.getDevice());
                        retryConnectTimes--;
                    } else {
                        justDisableConnect();
//                        retryConnectTimes=3;
                    } */
                } else {
                    setState(State.Connected);
//                    gatt.readRemoteRssi();
//                    Log.i(TAG, "BondState = " + gatt.getDevice().getBondState());
                    Log.i(TAG, "Attempting to start service discovery: " +
                            gatt.discoverServices());
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered. status = " + status);
//            UUID serviceUuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
                BluetoothGattService service = gatt.getService(mServiceUuid);
                if (service != null) {
                    List characteristics = service.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) characteristics.get(j);
                        Log.d(TAG, "characteristic = " + characteristic.getUuid().toString());
//                    Log.i(TAG, String.format("characteristic.properties = %X", characteristic.getProperties()));
                        if ((characteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0) {
                            gatt.setCharacteristicNotification(characteristic, true);

                            // writeDescriptor 不能连续执行, 至少间隔100ms
                            // 0.1.5: 如果间隔时间超过250ms SAMSUNG 2s内连接不上会被动断开
                            // 0.1.8: TY012/015 在 1.32 后校验超时时间改为 4s
                            sHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                            mBluetoothGatt.writeDescriptor(descriptor);
                                        } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                            mBluetoothGatt.writeDescriptor(descriptor);
                                        }
                                    } catch (Exception e) {
                                        //justDisableConnect();
                                        e.printStackTrace();
                                    }
                                }
                            }, 700 * j);
                        }
                    }
                }
            } else {
                //justDisableConnect();
                Log.w(TAG, "onServicesDiscovered. status = " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead. status = " + status);
            } else {
                Log.w(TAG, "onCharacteristicRead. status = " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            mBluetoothGatt = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicWrite. status = " + status + ", "+ mIsWritingValue);
                if (mIsWritingValue) {
                    writeCommand(mWillWritingValue);
                }
            } else {
                Log.w(TAG, "onCharacteristicWrite. status = " + status);
                //justDisableConnect();
                sHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startScan(0);
                    }
                }, 6000);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            mBluetoothGatt = gatt;
            if (characteristic.getUuid().equals(sCharacteristicCtrlPointUuid)) {
                Log.d(TAG, "onCharacteristicChanged.\ncharacteristic.uuid = MEDTRUM_UUID_CHARACTERISTIC_CTRLPT\ncharacteristic.value = " + Arrays.toString(characteristic.getValue()));
                parseCharacteristicValue(characteristic.getValue());
            } else if (characteristic.getUuid().equals(UUID.fromString(String.format(MEDTRUM_BASE_UUID, MEDTRUM_UUID_CHARACTERISTIC_IPS_RTN)))) {
//                Log.d(TAG, "onCharacteristicChanged.\n characteristic.uuid = MEDTRUM_UUID_CHARACTERISTIC_IPS_RTN\n characteristic.value = " + Arrays.toString(characteristic.getValue()));
                if (mOnCharacteristicValueReceiveListener != null) {
                    mOnCharacteristicValueReceiveListener.onNotificationReceive(characteristic.getValue());
                }
            } else if (characteristic.getUuid().equals(UUID.fromString(String.format(MEDTRUM_BASE_UUID, MEDTRUM_UUID_CHARACTERISTIC_CGM_DATA)))) {
//                Log.d(TAG, "onCharacteristicChanged.\n characteristic.uuid = MEDTRUM_UUID_CHARACTERISTIC_CGM_DATA\n characteristic.value = " + Arrays.toString(characteristic.getValue()));
                if (mOnCharacteristicValueReceiveListener != null) {
                    mOnCharacteristicValueReceiveListener.onNotificationReceive(characteristic.getValue());
                }
            } else {
                Log.w(TAG, "onCharacteristicChanged. Unknown characteristic.\ncharacteristic.uuid = " + characteristic.getUuid().toString() + "\ncharacteristic.value = " + Arrays.toString(characteristic.getValue()));
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            mBluetoothGatt = gatt;
//            Log.d(TAG, "onDescriptorRead. status = " + status + " descriptor = " + Arrays.toString(descriptor.getValue()));
            if (bytesToInt(descriptor.getValue()) > 0) {
                BluetoothGattService service = gatt.getService(mServiceUuid);
                if (service != null) {
                    boolean isNotificationEnabled = true;
                    List characteristics = service.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) characteristics.get(j);
                        BluetoothGattDescriptor aDescriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                        if (aDescriptor.getValue() != null && bytesToInt(aDescriptor.getValue()) > 0) {
                        } else {
                            isNotificationEnabled = false;
                        }
                    }

                    if (isNotificationEnabled) {
                        Log.d(TAG, "All notifications are enabled.");
                        if (mOnOperationPrepareListener != null) {
                            mOnOperationPrepareListener.OnOperationPrepare();
                        }
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            mBluetoothGatt = gatt;
            if (BluetoothGatt.GATT_SUCCESS == status) {
//                Log.d(TAG, "onDescriptorWrite. status = " + status + " descriptor = " + Arrays.toString(descriptor.getValue()));
                mBluetoothGatt.readDescriptor(descriptor);
            } else {
//                Log.w(TAG, "onDescriptorWrite. status = " + status + " descriptor = " + Arrays.toString(descriptor.getValue()));
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            mBluetoothGatt = gatt;
            Log.i(TAG, "onReliableWriteCompleted. status = " + status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            mBluetoothGatt = gatt;
            if (BluetoothGatt.GATT_SUCCESS == status) {
                Log.i(TAG, "onReadRemoteRssi. status = " + status + " rssi = " + rssi);
                mRSSI = rssi;
            } else {
                Log.w(TAG, "onReadRemoteRssi. status = " + status + " rssi = " + rssi);
            }
            if (mOnReadRssiListener != null) {
                mOnReadRssiListener.OnReadRssi(status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            mBluetoothGatt = gatt;
            Log.i(TAG, "onMtuChanged. status = " + status + " mtu = " + mtu);
        }
    };

    private void connectDevice(final BluetoothDevice device) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "connectDevice: BluetoothAdapter not initialized.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "connectDevice: BluetoothAdapter is disabled");
            return;
        }
        if (device == null) {
            Log.w(TAG, "connectDevice: Unspecified device.");
            return;
        }

//         防止连接出现133错误, 不能发现Services
        if (mBluetoothGatt != null) {
            Log.i(TAG, "connectDevice: closeGatt");
            close();
        }

        if (mBluetoothGatt != null && mBluetoothGatt.getDevice() != null && mBluetoothGatt.getDevice().equals(device)) {
            Log.d(TAG, "connectDevice: Trying to use an existing mBluetoothGatt for connection.");
            mBluetoothGatt.connect();
            setState(State.Connecting);
        } else {
            Log.d(TAG, "connectDevice: Trying to create a new connection.");
            // directly connect to the remote device (false)
            sHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mBluetoothGatt = device.connectGatt(MyApplication.getInstance(), false, mGattCallback, BluetoothDevice.TRANSPORT_AUTO);
                    } else {
                        mBluetoothGatt = device.connectGatt(MyApplication.getInstance(), false, mGattCallback);
                    }
                }
            });
            setState(State.Connecting);
        }
    }

    public void tryConnectDevice() {
        if (mState == State.Closed) {
            Log.w(TAG, "tryConnectDevice when state = " + mState.toString());
            return;
        }
        if (mState == State.Connecting || mState == State.Connected) {
            Log.d(TAG, "tryConnectDevice: has already " + mState.toString());
            return;
        }
        if (mState == State.Scanning) {
            stopScan();
        }
        connectDevice(mBluetoothDevice);
    }

    private int parseManufacturerSpecificData(byte[] data, int rssi, BluetoothDevice bluetoothDevice) {
        if (data != null && data.length >= 6) {
            byte[] deviceIdBytes = new byte[4];
            int deviceType;
            int version;
            int offset = 0;
            deviceIdBytes = MyScanRecord.extractBytes(data, offset, deviceIdBytes.length);
            offset += deviceIdBytes.length;
            deviceType = data[offset] & 0xFF;
            offset += 1;
            version = data[offset] & 0xFF;
            offset += 1;

            int deviceId = bytesToInt(deviceIdBytes);
            Log.v(TAG, String.format("deviceId = %d, version = %f, deviceType = %X", deviceId, version / 100.0, deviceType));
            Log.v(TAG, String.format("we need find deviceId = %d", mDeviceId));

            if (mDeviceCategory.contains(deviceType)) {
                if (!mDeviceIdArray.contains(deviceId)) {
                    mDeviceIdArray.add(deviceId);

                    Intent intent = new Intent(LOCAL_BROADCAST_SCAN_NEW_DEVICE);
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getInstance());
                    localBroadcastManager.sendBroadcast(intent);
                }

                //找到对应设备
                if (deviceId != 0 && deviceId == mDeviceId) {
                    stopScan();
                    mDeviceType = deviceType;
                    mDeviceVersion = version;
                    mBluetoothDevice = bluetoothDevice;
                    mRSSI = rssi;
                    byte[] payload = MyScanRecord.extractBytes(data, offset, data.length - offset);
                    if (mOnDeviceScanListener != null) {
                        mOnDeviceScanListener.onDeviceScan(deviceId, deviceType, (float) (version / 100.0), payload);
                    }
                    //retryConnectTimes = 3;
                }
                return deviceId;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void setOnDeviceScanListener(OnDeviceScanListener onDeviceScanListener) {
        mOnDeviceScanListener = onDeviceScanListener;
    }

    public void setOnCharacteristicValueReceiveListener(OnCharacteristicValueReceiveListener onCharacteristicValueReceiveListener) {
        mOnCharacteristicValueReceiveListener = onCharacteristicValueReceiveListener;
    }

    public void setOnOperationPrepareListener(OnOperationPrepareListener onOperationPrepareListener) {
        mOnOperationPrepareListener = onOperationPrepareListener;
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    public void setOnReadRssiListener(OnReadRssiListener onReadRssiListener) {
        mOnReadRssiListener = onReadRssiListener;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }

    public BLEBaseCentral() {
        sHandler = new Handler(MyApplication.getInstance().getMainLooper());
        pHandler = new Handler(MyApplication.getInstance().getMainLooper());

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!MyApplication.getInstance().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) MyApplication.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return;
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return;
        }
        if (mBluetoothGatt != null) {
            close();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device.getName() != null && device.getName().equals(MEDTRUM_DEVICE_NAME)) {
                        Log.v(TAG, "--==Find BluetoothDevice==--");
                        if (device.getUuids() != null && device.getUuids().length > 0) {
                            Log.d(TAG, "BluetoothDevice.uuid = " + Arrays.toString(device.getUuids()));
                        }
                        Log.v(TAG, "rssi = " + rssi);
//                        Log.i(TAG, "scanRecord.length = " + scanRecord.length);
//                        Log.i(TAG, "scanRecord = " + Arrays.toString(scanRecord));

                        MyScanRecord data = MyScanRecord.parseFromBytes(scanRecord);
//                        Log.i(TAG, "data = " + data.toString());
                        byte[] manufacturerSpecificData = data.getManufacturerSpecificData(MEDTRUM_COMPANY_IDENTIFIER);
                        Log.v(TAG, "manufacturerSpecificData = " + Arrays.toString(manufacturerSpecificData));
                        //parseManufacturerSpecificData(manufacturerSpecificData, rssi, device);
                    }
                }
            };
        } else {
            mScanCallback = new ScanCallback() {
                @Override
                @TargetApi(21)
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if (result.getDevice().getName() != null && result.getDevice().getName().equals(MEDTRUM_DEVICE_NAME)) {
                        Log.v(TAG, "ScanResult: callbackType = " + callbackType);
                        Log.v(TAG, "--==Find BluetoothDevice==--");
                        Log.v(TAG, "rssi = " + result.getRssi());
                        if (result.getScanRecord() != null) {
                            byte[] manufacturerSpecificData = result.getScanRecord().getManufacturerSpecificData(MEDTRUM_COMPANY_IDENTIFIER);
                            Log.v(TAG, "manufacturerSpecificData = " + Arrays.toString(manufacturerSpecificData));
                            parseManufacturerSpecificData(manufacturerSpecificData, result.getRssi(), result.getDevice());
                        }
                    }
                }

                @Override
                @TargetApi(21)
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                    Log.d(TAG, "BatchScanResults: results = " + results.toString());
                    for (ScanResult result : results) {
                        ScanRecord scanRecord = result.getScanRecord();
                        if (scanRecord != null && scanRecord.getDeviceName() != null && scanRecord.getDeviceName().equals(MEDTRUM_DEVICE_NAME)) {
                            byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(MEDTRUM_COMPANY_IDENTIFIER);
                            Log.v(TAG, "manufacturerSpecificData = " + Arrays.toString(manufacturerSpecificData));
//                            parseManufacturerSpecificData(manufacturerSpecificData, results.get(i).getRssi());
                        }
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
//                    disableDevice();
                    /*如果出现2错误，那么就重启蓝牙*/
//                    if (errorCode==2){
//                        mBluetoothAdapter.disable();
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                while (true) {
//                                    try {
//                                        Thread.sleep(500);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
//                                        mBluetoothAdapter.enable();
//                                        break;
//                                    }
//                                }
//                            }
//                        }).start();
//                    }

//                    if (errorCode!=1) {
//                        disableDevice();
//                        sHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                // Checks if Bluetooth is supported on the device.
//                                if (mBluetoothAdapter == null) {
//                                    Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
//                                    return;
//                                }
//                                if (mBluetoothGatt != null) {
//                                    close();
//                                }
//                                startScan(0);
//                            }
//                        }, 3000);
//                    }
                    Log.w(TAG, "ScanFailed: errorCode = " + errorCode);
                }
            };
        }

    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.d(TAG, "BluetoothGatt close");
        //setState(State.Closed);
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void startScan(long scanPeriod) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "startScan: BluetoothAdapter not initialized.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "startScan: BluetoothAdapter is disabled");
            return;
        }

        if (mState.equals(State.Connecting) || mState.equals(State.Connected)) {
            Log.w(TAG, "Start scan when state = " + mState.toString());
            return;
        }

        if (mState.equals(State.Scanning)) {
            Log.d(TAG, "startScan: has already scanning");
//            stopScan();
//            sHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startScan(0);
//                }
//            },10* MTDateUtils.SECOND_IN_MILLIS);
            return;
        }

        //mDeviceIdArray.clear();
        setState(State.Scanning);
        //Log.i(TAG, "startScan: 开始扫描---->SN:" + getDeviceId());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "startScan");
            mBluetoothAdapter.startLeScan((BluetoothAdapter.LeScanCallback) mScanCallback);
        } else {
            boolean isBackground = MyApplication.getInstance().isBackground();
            Log.i(TAG, "startScan on " + (isBackground ? "background" : "foreground"));
            final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//            bluetoothLeScanner.startScan((ScanCallback) mScanCallback);

            if (bluetoothLeScanner == null) {
                Log.w(TAG, "startScan: BluetoothLeScanner is not exist.");
                return;
            }

            final List<ScanFilter> scanFilters = new ArrayList<>();
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setDeviceName(MEDTRUM_DEVICE_NAME)
                    .build();
            scanFilters.add(scanFilter);
            final ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(mScanMode)
//                    .setReportDelay(1000 * 20)
                    .build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.i(TAG, "ss0");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothLeScanner.startScan(scanFilters, scanSettings, (ScanCallback) mScanCallback);
                    }
                }).start();
            } else {
                bluetoothLeScanner.startScan(scanFilters, scanSettings, (ScanCallback) mScanCallback);
            }

        }

        //if (scanPeriod != 0) {
        //    sHandler.postDelayed(sStopScanRunnable, scanPeriod);
        //}
    }

    public void stopScan() {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "stopScan: BluetoothAdapter not initialized.");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "stopScan: BluetoothAdapter is disabled");
            return;
        }
        //当处于校准状态时，必须停止扫描
        //if (!mState.equals(State.Scanning) && !mState.equals(State.Idle) && !MyApplication.isCalibrating()) {
        //    Log.w(TAG, "Stop scan when state = " + mState.toString());
        //    return;
        //}
        Log.i(TAG, "stopScan");
        setState(State.Idle);
        //sHandler.removeCallbacks(sStopScanRunnable);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) mScanCallback);
        } else {
            BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner == null) {
                Log.w(TAG, "stopScan: BluetoothLeScanner is not exist.");
                return;
            }
            bluetoothLeScanner.stopScan((ScanCallback) mScanCallback);
        }
    }

    public void setState(State state) {
        if (mState.equals(state)) {
            return;
        }
        mState = state;

        //if (mOnStateChangeListener != null) {
        //    mOnStateChangeListener.OnStateChange(state);
        //}

        //Intent intent = new Intent(LOCAL_BROADCAST_DEVICE_STATE_CHANGE);
        //LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getInstance());
        //localBroadcastManager.sendBroadcast(intent);
    }
}