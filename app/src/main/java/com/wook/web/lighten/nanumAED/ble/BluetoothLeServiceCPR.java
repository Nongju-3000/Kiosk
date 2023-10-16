package com.wook.web.lighten.nanumAED.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.exceptions.BleGattException;
import com.wook.web.lighten.nanumAED.data.GattAttributes;
import com.wook.web.lighten.nanumAED.utils.HexString;
import com.wook.web.lighten.nanumAED.utils.Print;
import com.wook.web.lighten.nanumAED.utils.SoundPoolHandler;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class BluetoothLeServiceCPR extends Service {

    private final static String TAG = BluetoothLeServiceCPR.class.getSimpleName();

    /**
     * GATT Status constants
     */

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_SOUND = "ACTION_SOUND";

    private static final UUID CHAR_POSITION_UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    private static final UUID CHAR_BREATH_UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB");
    private static final UUID CHAR_DEPTH_UUID = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_WRITE = UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_READY = "ACTION_READY";
    public final static String ACTION_DEPTH_CHANGE = "ACTION_DEPTH_CHANGE";
    public final static String ACTION_CALL = "ACTION_CALL";
    public final static String ACTION_COMPANY = "ACTION_COMPANY";
    public final static String ACTION_MAGNET = "ACTION_MAGNET";
    public final static String ACTION_CALIBRATION = "ACTION_CALIBRATION";
    public final static String ACTION_CALIBRATION_MAGNET = "ACTION_CALIBRATION_MAGNET";
    public final static String ACTION_NEW_STATE = "ACTION_NEW_STATE";
    public final static String ACTION_BLE_CONNECTED = "ACTION_BLE_CONNECTED";

    public static final String CMD_RANGE_INCOMMING_FLAG = "f5";
    public static final String CMD_READY = "f1";
    public static final String CMD_CALL = "f2";
    public static final String CMD_MAGNET = "c0";
    public static final String CMD_CALIBRATION = "b0";
    public static final String CMD_CALIBRATION_MAGNET = "ba";
    public static final String CMD_STX = "02";
    public static final String CMD_ETX = "03";
    public static final String CMD_PLAY = "b1";
    public static final String CMD_PAUSE = "b2";
    public static final String CMD_NEXT = "b4";
    public static final String CMD_PREV = "b3";
    /**
     * BluetoothAdapter for handling connections
     */

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;


    public static final String EXTRA_BLE_DEVICE_ADDRESS = "BLE DEVICE ADDRESS";
    public static final String EXTRA_NEW_STATE = "BLE NEW STATE";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);
    public static final String DATA1_NOT_KEY = "datanotkey1";
    public static final String DATA2_NOT_KEY = "datanotkey2";

    public static ArrayList<Boolean> isCharDRegistereds = new ArrayList<>();
    public static ArrayList<Boolean> isCharARegistereds = new ArrayList<>();
    private static RxBleClient rxBleClient;

    private Handler mHander = new Handler(Looper.getMainLooper());
    private boolean isStart = false;
    private String mode = "";

    //TODO ble data service setting

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        parsingActions(intent);
        return START_NOT_STICKY;
    }

    int bpm;

    private void parsingActions(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            Print.e(TAG, "The Service is started");
        } else if (action.equals(ACTION_SOUND)) {
            bpm = intent.getIntExtra(DATA1_NOT_KEY, 0);
            beepSchedule((bpm != 0));
        } else if (action.equals(ACTION_READY)) {
            setReady();
        } else if (action.equals(ACTION_DEPTH_CHANGE)) {
            int min = intent.getIntExtra(DATA1_NOT_KEY, 0);
            int max = intent.getIntExtra(DATA2_NOT_KEY, 0);
            setDepth(min, max);
        } else if (action.equals(ACTION_CALL)) {
            int index = intent.getIntExtra(DATA1_NOT_KEY, 0);
            call(index);
        } else if (action.equals(ACTION_COMPANY)) {
            int index = intent.getIntExtra(DATA1_NOT_KEY, 1);
            int value = intent.getIntExtra(DATA2_NOT_KEY, 0);
            sendCompany(index, value);
        } else if (action.equals(ACTION_MAGNET)) {
            int index = intent.getIntExtra(DATA1_NOT_KEY, 1);
            sendMagnet(index);
        } else if (action.equals(ACTION_CALIBRATION)) {
            int index = intent.getIntExtra(DATA1_NOT_KEY, 1);
            sendCalibration(index);
        } else if (action.equals(ACTION_CALIBRATION_MAGNET)) {
            int index = intent.getIntExtra(DATA1_NOT_KEY, 1);
            sendCalibrationMagnet(index);
        }
    }

    private Thread sendReferThread = null;
    private boolean isHandlerFree = true;

    private ArrayList<Integer> min_list = new ArrayList<>();
    private ArrayList<Integer> max_list = new ArrayList<>();

    private void sendCalibration(int index) {
        Print.e(TAG, "sendCalibration Call!");
        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                while (!isHandlerFree) ;
                Print.e(TAG, "CALIBRATION sending");
                writeCharacteristic(index, CMD_CALIBRATION);
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void sendCalibrationMagnet(int index) {
        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                while (!isHandlerFree) ;
                Print.e(TAG, "magnet calibration sending");
                writeCharacteristic(index, CMD_CALIBRATION_MAGNET);
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void sendMagnet(int index) {
        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                while (!isHandlerFree) ;
                Print.e(TAG, "magnet sending");
                writeCharacteristic(index, CMD_MAGNET);
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void call(int index) {
        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                while (!isHandlerFree) ;
                Print.e(TAG, "call sending");
                writeCharacteristic(index, CMD_CALL);
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void sendCompany(int index, int value) { // 161 ~ 175
        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                while (!isHandlerFree) ;
                Print.e(TAG, "company sending");
                writeCharacteristic(index, Integer.toHexString(value));
                try {
                    Thread.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void setDepth(int min, int max) {
        min_list.clear();
        min_list.clear();
        for (int i = 0; i < 6; i++) {
            min_list.add(0);
            max_list.add(0);
        }

        if (sendReferThread == null) {
            sendReferThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    for (RxBleConnection connection : mRxBleConnections) {
                        while (!isHandlerFree) ;
                        Print.e(TAG, "inner sending loop");
                        if (connection != null) {
                            min_list.set(count, min);
                            max_list.set(count, max);
                        }
                        writeCharacteristic(connection, CMD_RANGE_INCOMMING_FLAG, count);
                        count++;
                        try {
                            Thread.sleep(150);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    sendReferThread.interrupt();
                    sendReferThread = null;
                }
            });
            sendReferThread.start();
        }
    }

    private void setReady() {
        if (sendReferThread != null)
            Print.e("Test", "sendReferThread is not null");

        isStart = false;

        if (sendReferThread == null) {
            sendReferThread = new Thread(() -> {
                int index = 0;
                for (RxBleConnection rxBleConnection : mRxBleConnections) {
                    while (!isHandlerFree) ;
                    Print.e(TAG, "inner sending loop");
                    writeCharacteristic(rxBleConnection, CMD_READY, index);
                    try {
                        index++;
                        Thread.sleep(150);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                sendReferThread.interrupt();
                sendReferThread = null;
            });
            sendReferThread.start();
        }
    }

    private void writeCharacteristic(RxBleConnection rxBleConnection, String data, int index) {
        try {
            Log.e(TAG, "writeCharacteristic: " + data);
            byte[] sender = HexString.hexToBytes(data);
            if (connectionChecking.get(index) == RxBleConnection.RxBleConnectionState.CONNECTED) {
                rxBleConnection.writeCharacteristic(UUID_WRITE, sender).subscribe();
            }
            if (data.equals(CMD_RANGE_INCOMMING_FLAG)) {
                isHandlerFree = false;
                mHander.postDelayed(() -> {
                    int min = min_list.get(index);
                    if (min != 0) {
                        min_list.set(index, 0);
                        writeCharacteristic(rxBleConnection, Integer.toHexString(min), index);
                        Print.e(TAG, "min sent..");
                    }
                    isHandlerFree = true;
                }, 200);
            }
            int value = Integer.parseInt(data, 16);
            if (value >= 30 && value <= 70) { // new Range min or max sent
                if (min_list.get(index) == 0 && max_list.get(index) != 0) {
                    Print.e(TAG, "sent min data is end. -> time to send max flag");
                    isHandlerFree = false;
                    mHander.postDelayed(() -> {
                        int max = max_list.get(0);
                        if (max != 0) {
                            max_list.set(0, 0);
                            writeCharacteristic(rxBleConnection, Integer.toHexString(max), index);
                            Print.e(TAG, "max sent..");
                        }
                        isHandlerFree = true;
                    }, 200);
                } else if (min_list.get(0) == 0 && max_list.get(0) == 0) {
                    Print.e(TAG, "sent max data end!");
                }
            }
        } catch (NumberFormatException | NullPointerException ignored) {
        }
    }

    private Handler beepHandler;

    private void beepSchedule(boolean run) {
        if (beepHandler != null) {
            beepHandler.removeCallbacks(beepRun);
            beepHandler = null;
        }
        if (run) {
            beepHandler = new Handler(Looper.getMainLooper());
            long interval = 60000 / bpm;
            beepHandler.postDelayed(beepRun, interval);
        }
    }

    private Runnable beepRun = () -> {
        playBeepSound();
        beepSchedule(true);
    };
    private SoundPoolHandler soundPoolHandler;

    private void playBeepSound() {
        if (soundPoolHandler == null) soundPoolHandler = new SoundPoolHandler(this);
        soundPoolHandler.playSound();
    }

    /**
     * Local binder class
     */

    public class LocalBinder extends Binder {
        public BluetoothLeServiceCPR getService() {
            return BluetoothLeServiceCPR.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        disconnect();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        if (rxBleClient == null)
            rxBleClient = RxBleClient.create(this);
        if (mRxBleConnections.size() != 2) {
            for (int i = 0; i < 2; i++) {
                mRxBleConnections.add(null);
                isCharDRegistereds.add(false);
                isCharARegistereds.add(false);
                connectionChecking.add(RxBleConnection.RxBleConnectionState.DISCONNECTED);
                //connectionDisposables.add(null);
            }
        }
        return true;
    }

    public boolean isConnected(String macAddress) {
        if (macAddress.equals("-"))
            return false;
        else
            return rxBleClient.getBleDevice(macAddress).getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private String preStatus = "";

    private void sendNewState(final RxBleDevice device, RxBleConnection.RxBleConnectionState newState, int index) {
        connectionChecking.set(index, newState);
        String connectedStatus = "";
        if (RxBleConnection.RxBleConnectionState.DISCONNECTED == newState) {
            isCharDRegistereds.set(index, false);
            isCharARegistereds.set(index, false);
            connect(device.getMacAddress(), index);
            connectedStatus = "disconnected";
        } else if (RxBleConnection.RxBleConnectionState.CONNECTED == newState) {
            connectedStatus = "connected";
        }
        if (!connectedStatus.equals(preStatus) && (connectedStatus.equals("disconnected") || connectedStatus.equals("connected"))) {
            final Intent intent = new Intent(ACTION_NEW_STATE);
            intent.putExtra(EXTRA_BLE_DEVICE_ADDRESS, device.getMacAddress());
            intent.putExtra(EXTRA_NEW_STATE, connectedStatus);
            sendBroadcast(intent);
            preStatus = connectedStatus;
        }
    }

    public void broadCastRxConnectionUpdate(final RxBleDevice device, int index) {
        RxBleConnection rxBleConnection = mRxBleConnections.get(index);
        Print.e(TAG, "broadCastRxConnectionUpdate");

        if (isConnected(device.getMacAddress())) {
            //connectCompositeDisposable.add(connectionDisposables.get(index));
            Disposable disposable = rxBleConnection.setupNotification(CHAR_POSITION_UUID)
                    .doOnSubscribe(notificationObservable -> {
                        rxEnableNotification(device, rxBleConnection, index);
                        Print.e(TAG, "Position Notification Setup");
                    })
                    .flatMap(notificationObservable -> notificationObservable)
                    .subscribe(
                            bytes -> onPositionReceived(device, bytes),
                            this::onNotificationSetupFailure
                    );
            compositeDisposable.add(disposable);
        }
    }

    private void rxEnableNotification(RxBleDevice device, RxBleConnection rxBleConnection, int index) {
        if (!isCharDRegistereds.get(index)) {
            isCharDRegistereds.set(index, true);

            /*if(Objects.requireNonNull(device.getName()).contains("AIO")) {
                if (isConnected(device.getMacAddress())) {
                    Disposable disposable = rxBleConnection.setupNotification(CHAR_BREATH_UUID)
                            .doOnSubscribe(notificationObservable -> {
                                rxEnableNotification(device, rxBleConnection, index);
                                Print.e(TAG, "Breath Notification Setup");
                            })
                            .flatMap(notificationObservable -> notificationObservable)
                            .subscribe(
                                    bytes -> onBreathReceived(device, bytes),
                                    this::onNotificationSetupFailure
                            );
                    compositeDisposable.add(disposable);
                }
            }else{
                if(isConnected(device.getMacAddress())){
                    sendConnection(device, index);
                    angleHandler.postDelayed(getAngleRunnable(device, rxBleConnection), 0);
                }
            }*/
            if (isConnected(device.getMacAddress())) {
                Disposable disposable = rxBleConnection.setupNotification(CHAR_BREATH_UUID)
                        .doOnSubscribe(notificationObservable -> {
                            rxEnableNotification(device, rxBleConnection, index);
                            Print.e(TAG, "Breath Notification Setup");
                        })
                        .flatMap(notificationObservable -> notificationObservable)
                        .subscribe(
                                bytes -> onBreathReceived(device, bytes),
                                this::onNotificationSetupFailure
                        );
                compositeDisposable.add(disposable);
            }
        } else {
            if (!isCharARegistereds.get(index)) {
                isCharARegistereds.set(index, true);

                if (isConnected(device.getMacAddress())) {
                    Disposable disposable = rxBleConnection.setupNotification(CHAR_DEPTH_UUID)
                            .doOnNext(notificationObservable -> sendConnection(device, index))
                            .flatMap(notificationObservable -> notificationObservable)
                            .subscribe(
                                    bytes -> onDepthReceived(device, bytes),
                                    this::onNotificationSetupFailure
                            );
                    compositeDisposable.add(disposable);
                }
            }
        }
    }

    private Runnable getAngleRunnable(RxBleDevice device, RxBleConnection rxBleConnection) {
        return () -> {
            if (isConnected(device.getMacAddress())) {
                Disposable disposable = rxBleConnection.setupNotification(CHAR_BREATH_UUID)
                        .flatMap(notificationObservable -> notificationObservable)
                        .subscribe(
                                bytes -> onBreathReceived(device, bytes),
                                this::onNotificationSetupFailure
                        );
                compositeDisposable.add(disposable);
            }
        };
    }

    private void sendConnection(RxBleDevice device, int index) {
        if (isStart) {
            writeCharacteristic(index, mode);
        }
        if (Objects.requireNonNull(device.getName()).contains("AIO")) {
            writeCharacteristic(index, "e0");
        }
        final Intent intent = new Intent(ACTION_BLE_CONNECTED);
        intent.putExtra(EXTRA_BLE_DEVICE_ADDRESS, device.getMacAddress());
        sendBroadcast(intent);
    }

    private void onPositionReceived(RxBleDevice bleDevice, byte[] bytes) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        if (bytes != null && bytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
            for (byte byteChar : bytes)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, stringBuilder + "," + CHAR_POSITION_UUID + "," + bleDevice.getMacAddress());
        }
        sendBroadcast(intent);
    }

    private void onBreathReceived(RxBleDevice bleDevice, byte[] bytes) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        if (bytes != null && bytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
            for (byte byteChar : bytes)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, stringBuilder.toString() + "," + CHAR_BREATH_UUID + "," + bleDevice.getMacAddress());
        }
        sendBroadcast(intent);
    }

    private void onDepthReceived(RxBleDevice bleDevice, byte[] bytes) {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        if (bytes != null && bytes.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(bytes.length);
            for (byte byteChar : bytes)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, stringBuilder + "," + CHAR_DEPTH_UUID + "," + bleDevice.getMacAddress());
        }
        sendBroadcast(intent);
    }

    private void onNotificationSetupFailure(Throwable throwable) {
        //noinspection ConstantConditions
        Print.e(TAG, "Notification setup Failure : " + throwable.getMessage());
        setCompositeDisposable();
    }

    private ArrayList<RxBleConnection> mRxBleConnections = new ArrayList<>();
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final CompositeDisposable statecompositeDisposable = new CompositeDisposable();
    private ArrayList<Boolean> isregisterstateDisposable = new ArrayList<Boolean>() {{
        add(false);
        add(false);
    }};
    public CompositeDisposable notiDisposable = new CompositeDisposable();
    private ArrayList<RxBleConnection.RxBleConnectionState> connectionChecking = new ArrayList<>();
    private CompositeDisposable angleDisposables = new CompositeDisposable();
    private Handler angleHandler = new Handler(Looper.getMainLooper());
    private ArrayList<Disposable> connectionDisposables = new ArrayList<>();
    private CompositeDisposable connectCompositeDisposable = new CompositeDisposable();

    public boolean connect(final String macAddress, int index) {
        RxBleDevice bleDevice = rxBleClient.getBleDevice(macAddress);

        Observable<RxBleConnection> connectionObservable = bleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());

        final Disposable connectionDisposable = connectionObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(
                        connection -> {
                            Log.e("connect", macAddress);
                            mRxBleConnections.set(index, connection);
                            broadCastRxConnectionUpdate(bleDevice, index);
                        },
                        this::onConnectionFailure,
                        this::onConnectionFinished
                );
        compositeDisposable.add(connectionDisposable);

        if (!isregisterstateDisposable.get(index)) {
            Log.e("connect", "stateDisposable");
            Disposable stateDisposable = bleDevice.observeConnectionStateChanges()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(newState -> sendNewState(bleDevice, newState, index));
            statecompositeDisposable.add(stateDisposable);
            isregisterstateDisposable.set(index, true);
        }

        return true;
    }

    private void dispose() {
    }

    private void onConnectionFinished() {
    }

    private void onConnectionFailure(Throwable throwable) {
        //connect(mac, index);
        Print.e(TAG, "Connection Failure : " + throwable.getMessage());
    }

    public void writeCharacteristic(int index, String data) {
        if (!isStart) {
            if (data.equals("f3")) {
                mode = data;
                isStart = true;
            }
        }
        byte[] sender = HexString.hexToBytes(data);
        if (connectionChecking.get(index) == RxBleConnection.RxBleConnectionState.CONNECTED) {
            try {
                mRxBleConnections.get(index).writeCharacteristic(UUID_WRITE, sender).subscribe();
                Log.e(TAG, "write characteristic, data = " + data + ", index = " + index);
            } catch (BleGattException e) {
                int status = e.getStatus();
                Log.e(TAG, "write characteristic, data = " + data + ", index = " + index + ", status = " + status);
                if (status == 133) {
                    setCompositeDisposable();
                }
            }
        }
    }


    public void disconnect() {
        compositeDisposable.clear();
        statecompositeDisposable.clear();
        isregisterstateDisposable.set(0, false);
        isregisterstateDisposable.set(1, false);
        for (int i = 0; i < isCharARegistereds.size(); i++) {
            isCharDRegistereds.set(i, false);
            isCharARegistereds.set(i, false);
        }
    }

    public void setCompositeDisposable(){
        compositeDisposable.clear();
    }

} // end