package com.medtrum.androidstudy;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PDMInstance extends BLEBaseCentral implements
        BLEBaseCentral.OnDeviceScanListener,
        BLEBaseCentral.OnCharacteristicValueReceiveListener,
        BLEBaseCentral.OnOperationPrepareListener,
        BLEBaseCentral.OnStateChangeListener{

    private static final String TAG = "PDMInstance";

    private static final int MEDTRUM_UUID_SERVICE_DTS = 0x9004; // Data Transmit Service - 手持机数据上传和配置下载
    private static final int DTS_OPERA_LOAD_PATCH_SESSION = 0x01;
    private static final int DTS_OPERA_LOAD_PATCH_BOLUS = 0x02;
    private static final int DTS_OPERA_LOAD_PATCH_BASAL = 0x03;
    private static final int DTS_OPERA_LOAD_PATCH_ALARM = 0x04;
    private static final int DTS_OPERA_LOAD_CGM_SESSION = 0x05;
    private static final int DTS_OPERA_LOAD_CGM_SESSION_DATA = 0x06;
    private static final int DTS_OPERA_LOAD_CGM_SESSION_CALIB = 0x07;
    private static final int DTS_OPERA_LOAD_CGM_SESSION_ALARM = 0x08;
    private static final int DTS_OPERA_LOAD_EVENT = 0x09;
    private static final int DTS_OPERA_LOAD_USER_SETTING = 0x0a;
    private static final int DTS_OPERA_LOAD_PDM_STATUS = 0x0b;    //读取PDM状态数据
    private static final int DTS_OPERA_NOTIFY_DISCONNECT = 0x21;    //通知断开

    private static final int MESSAGE_UPLOAD_CANCEL = 0;
    private static final int PDM_DATA_TYPE_ALARM = 10;

//    private PeerState mPeerState;
    private int mSessionCount;
    private boolean mNeedReadPumpData;
    private boolean mNeedReadPumpBasalData;
    private boolean mNeedReadPumpBolusData;
    private boolean mNeedReadPumpAlarmData;
    private boolean mNeedReadSensorData;
    private boolean mNeedReadSensorGlucoseData;
    private boolean mNeedReadSensorCalibrationData;
    private boolean mNeedReadSensorAlarmData;
    private boolean mNeedReadEventData;
    private boolean mNeedReadUserSettingData;
    private boolean mNeedReadPDMStatusData;
    private boolean mNeedDisconnectFromReadStatusData;
    private List<int[]> mPatchIdArray;
    private int mStartRecord;
    private byte[] mPumpBasalData;
    private byte[] mPumpBolusData;
    private byte[] mPumpAlarmData;
    private byte[] mSensorGlucoseData;
    private byte[] mSensorCalibrationData;
    private byte[] mSensorAlarmData;
    private byte[] mEventData;

    private static class InstanceHolder {
        private static final PDMInstance sInstance = new PDMInstance();

    }

    @Override
    public void onDeviceScan(int deviceId, int deviceType, float version, byte[] payload) {
        Log.i(TAG, String.format("DeviceScan: deviceId = %d, deviceType = %X, version = %.2f", deviceId, deviceType, version));
        tryConnectDevice();

        //pHandler.removeCallbacks(receiveNextDataRunnable);
        //2分钟未接收到数据，那么就重新获取数据
        //pHandler.postDelayed(receiveNextDataRunnable, MTDateUtils.SECOND_IN_MILLIS * (60 * 2 - 5));
        //打开扫描PDM10S之后就关闭扫描
        //if (isFirstConnected && !MyApplication.isOpenUpload()) {
//            Log.i(TAG, "onDeviceScan: PDM连接将在30s后断开");
//            pHandler.postDelayed(timeLimitDisableRunnable, 11 * MTDateUtils.SECOND_IN_MILLIS);
        //} else if (!MyApplication.isOpenUpload()) {
        //    Log.i(TAG, "onDeviceScan: PDM连接将在11s后断开");
        //    pHandler.postDelayed(timeLimitDisableRunnable, 11 * MTDateUtils.SECOND_IN_MILLIS);
        //}
    }

    @Override
    public void onNotificationReceive(byte[] data) {
        Log.d(TAG, "NotificationReceive: data = " + Arrays.toString(data));
    }

    @Override
    public void onIndicationReceive(byte[] data) {
        Log.d(TAG, "IndicationReceive: data = " + Arrays.toString(data));
    }

    @Override
    public void OnOperationPrepare() {
        Log.d(TAG, "OnOperationPrepare: data = ");
        //setPeerState(PeerState.Connected);
        //if (!MyApplication.isOpenUpload()) {
            pHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PDMInstance.getInstance().readPDMStatusData();
                }
            }, 200);
        //}

    }


    @Override
    public void OnStateChange(State newState) {
        Log.d(TAG, "OnStateChange: data = "+newState);
    }

    public void readPDMStatusData() {
        mNeedReadPDMStatusData = true;
        writeCommand();

    }

    public void readDate(Context context, int sessions, long duration) {
        if (context == null) {
            return;
        }

//        if (!URLUtils.isNetworkAvailable(context)) {
//            Log.w(TAG, "Read PDM data without network");
//            Toast.makeText(context, R.string.network_alert_title_disabled, Toast.LENGTH_LONG).show();
//            return;
//        }

        Log.i(TAG, "Start to read PDM data: sessions = " + sessions);

//        setPeerState(PeerState.Transfer);
        mSessionCount = sessions;

        mNeedReadPumpData = true;
        mNeedReadPumpBasalData = false;
        mNeedReadPumpBolusData = false;
        mNeedReadPumpAlarmData = false;
        mNeedReadSensorData = true;
        mNeedReadSensorGlucoseData = false;
        mNeedReadSensorCalibrationData = false;
        mNeedReadSensorAlarmData = false;
        mNeedReadEventData = true;
        mNeedReadUserSettingData = true;
        mNeedReadPDMStatusData = false;
//        mNeedDisconnectFromReadStatusData = false;
        mPatchIdArray = null;
        mStartRecord = 0;
        mPumpBolusData = new byte[0];
        mPumpBasalData = new byte[0];
        mPumpAlarmData = new byte[0];
        mSensorGlucoseData = new byte[0];
        mSensorCalibrationData = new byte[0];
        mSensorAlarmData = new byte[0];
        mEventData = new byte[0];

//        isUploadData = true;
//        setPdmData2WebUploading(true);
//
//
//        uploadPdmDataJson = new JSONObject();
//        pumpJson = new JSONObject();
//        sensorJson = new JSONObject();

//        this.duration = duration;

        writeCommand();

//        if (SpUtils.getString(MyApplication.getInstance(), SpUtils.COOKIE, null) != null) {
//            writeCommand();
//        } else {
//            if (MyApplication.isAlertWindowEnabled(MyApplication.getInstance())) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(MyApplication.getInstance());
//                builder.setTitle(MyApplication.getInstance().getString(R.string.settings_PDM_alert_title_cancel));
//                builder.setMessage("No Login");
//                builder.setPositiveButton(R.string.button_OK, null);
//                AlertDialog alertDialog = builder.create();
//                alertDialog.setCanceledOnTouchOutside(false);
//                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                alertDialog.show();
//            }
//        }
    }

    private void writeCommand() {
        byte[] commandData = new byte[]{};
        if (mNeedReadPDMStatusData) {    //读取数据
            byte commandCode = DTS_OPERA_LOAD_PDM_STATUS;
            commandData = new byte[]{commandCode};
            Log.i(TAG, "writeCommand: Read PDM Status Data = " + Arrays.toString(commandData));
            mNeedReadPDMStatusData = false;

        } else if (mNeedReadPumpData) {        //泵进程信息
            byte commandCode = DTS_OPERA_LOAD_PATCH_SESSION;
            int count = mSessionCount;
            if (count == 0) {
                count = 1024;
            }
            commandData = new byte[]{commandCode};
            commandData = bytesCombine(commandData, intToBytes(count, 2));
            Log.i(TAG, "writeCommand: Load PumpPatch commandData = " + Arrays.toString(commandData));
            mNeedReadPumpData = false;
        } else if (mNeedReadPumpBolusData || mNeedReadPumpBasalData || mNeedReadPumpAlarmData) {
            if (mPatchIdArray != null && mPatchIdArray.size() > 0) {
                byte commandCode = 0;
                if (mNeedReadPumpBolusData) {
                    commandCode = DTS_OPERA_LOAD_PATCH_BOLUS;
                } else if (mNeedReadPumpBasalData) {
                    commandCode = DTS_OPERA_LOAD_PATCH_BASAL;
                } else if (mNeedReadPumpAlarmData) {
                    commandCode = DTS_OPERA_LOAD_PATCH_ALARM;
                }
                int deviceId = mPatchIdArray.get(0)[0];
                int patchId = mPatchIdArray.get(0)[1];
                int startRecord = mStartRecord;
                int count = 1024;
                commandData = new byte[]{commandCode};
                commandData = bytesCombine(commandData, intToBytes(deviceId, 4));
                commandData = bytesCombine(commandData, intToBytes(patchId, 2));
                commandData = bytesCombine(commandData, intToBytes(startRecord, commandCode == DTS_OPERA_LOAD_PATCH_ALARM ? 4 : 2));
                commandData = bytesCombine(commandData, intToBytes(count, 2));
                Log.i(TAG, String.format("writeCommand: Load PumpData commandCode = 0x%x commandData = %s", commandCode, Arrays.toString(commandData)));
            } else {
                Log.e(TAG, "writeCommand: Load PumpData error: sessionIds = " + mPatchIdArray);
                mNeedReadPumpBolusData = false;
                mNeedReadPumpBasalData = false;
                mNeedReadPumpAlarmData = false;

                /*
                * 如果没有Pump数据那么就直接发送命令读取CGM数据
                * */
                byte commandCode = DTS_OPERA_LOAD_CGM_SESSION;
                int count = mSessionCount;
                if (count == 0) {
                    count = 1024;
                }
                commandData = new byte[]{commandCode};
                commandData = bytesCombine(commandData, intToBytes(count, 2));
                Log.i(TAG, "writeCommand: Load SensorSession commandData = " + Arrays.toString(commandData));
                mNeedReadSensorData = false;
            }
        } else if (mNeedReadSensorData) {
            byte commandCode = DTS_OPERA_LOAD_CGM_SESSION;
            int count = mSessionCount;
            if (count == 0) {
                count = 1024;
            }
            commandData = new byte[]{commandCode};
            commandData = bytesCombine(commandData, intToBytes(count, 2));
            Log.i(TAG, "writeCommand: Load SensorSession commandData = " + Arrays.toString(commandData));
            mNeedReadSensorData = false;
        } else if (mNeedReadSensorGlucoseData || mNeedReadSensorCalibrationData || mNeedReadSensorAlarmData) {
            if (mPatchIdArray != null && mPatchIdArray.size() > 0) {
                byte commandCode = 0;
                if (mNeedReadSensorGlucoseData) {
                    commandCode = DTS_OPERA_LOAD_CGM_SESSION_DATA;
                } else if (mNeedReadSensorCalibrationData) {
                    commandCode = DTS_OPERA_LOAD_CGM_SESSION_CALIB;
                } else if (mNeedReadSensorAlarmData) {
                    commandCode = DTS_OPERA_LOAD_CGM_SESSION_ALARM;
                }
                int deviceId = mPatchIdArray.get(0)[0];
                int patchId = mPatchIdArray.get(0)[1];
                int startRecord = mStartRecord;
                int count = 1024;
                commandData = new byte[]{commandCode};
                commandData = bytesCombine(commandData, intToBytes(deviceId, 4));
                commandData = bytesCombine(commandData, intToBytes(patchId, 2));
                commandData = bytesCombine(commandData, intToBytes(startRecord, 2));
                commandData = bytesCombine(commandData, intToBytes(count, 2));
                Log.i(TAG, String.format("writeCommand: Load SensorData commandCode = 0x%x commandData = %s", commandCode, Arrays.toString(commandData)));
            } else {
                Log.e(TAG, "writeCommand: Load SensorData error: sessionIds = " + mPatchIdArray);
                mNeedReadSensorGlucoseData = false;
                mNeedReadSensorCalibrationData = false;
                mNeedReadSensorAlarmData = false;

                /*
                * 如果没有血糖数据，那么就直接读取事件
                * */
                byte commandCode = DTS_OPERA_LOAD_EVENT;
                int startRecord = mStartRecord;
                int count = 1024;
                commandData = new byte[]{commandCode};
                commandData = bytesCombine(commandData, intToBytes(startRecord, 4));
                commandData = bytesCombine(commandData, intToBytes(count, 2));
                Log.i(TAG, "writeCommand: Load Event commandData = " + Arrays.toString(commandData));
            }
        } else if (mNeedReadEventData) {
            byte commandCode = DTS_OPERA_LOAD_EVENT;
            int startRecord = mStartRecord;
            int count = 1024;
            commandData = new byte[]{commandCode};
            commandData = bytesCombine(commandData, intToBytes(startRecord, 4));
            commandData = bytesCombine(commandData, intToBytes(count, 2));
            Log.i(TAG, "writeCommand: Load Event commandData = " + Arrays.toString(commandData));
        } else if (mNeedReadUserSettingData) {
            byte commandCode = DTS_OPERA_LOAD_USER_SETTING;
            commandData = new byte[]{commandCode};
            Log.i(TAG, "writeCommand: Load UserSettings commandData = " + Arrays.toString(commandData));
            mNeedReadUserSettingData = false;

        } else if (mNeedDisconnectFromReadStatusData) {
            byte commandCode = DTS_OPERA_NOTIFY_DISCONNECT;
            commandData = new byte[]{commandCode};
            Log.i(TAG, "writeCommand: Disconnect form PDM status Data read = " + Arrays.toString(commandData));
            mNeedDisconnectFromReadStatusData = false;
        }

        if (commandData.length > 0) {
            writeCommand(commandData);
        } else {
//            if (getPeerState().equals(PeerState.Transfer)) {
//                setPeerState(PeerState.Connected);
//            }
        }
    }

    public static byte[] intToBytes(int value, int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        for (int i = 0; i < bytesLength; i++) {
            bytes[i] = (byte) ((value >> 8 * i) & 0xFF);
        }
        return bytes;
    }

    public PDMInstance() {
        super();
        //this.init();

        mServiceUuid = UUID.fromString(String.format(MEDTRUM_BASE_UUID, MEDTRUM_UUID_SERVICE_DTS));

        Set<Integer> deviceCategory = new HashSet<>();
        deviceCategory.add(MEDTRUM_DEVICE_TYPE_FM011);
        mDeviceCategory = deviceCategory;
        setDeviceId(101000437);
        setOnDeviceScanListener(this);
        setOnCharacteristicValueReceiveListener(this);
        setOnOperationPrepareListener(this);
        setOnStateChangeListener(this);


        Log.i(TAG, "sss");
    }

    public static PDMInstance getInstance() {
        return InstanceHolder.sInstance;
    }

}