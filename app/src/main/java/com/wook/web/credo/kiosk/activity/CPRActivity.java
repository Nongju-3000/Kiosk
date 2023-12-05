package com.wook.web.credo.kiosk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.polidea.rxandroidble2.RxBleDevice;
import com.wook.web.credo.kiosk.BuildConfig;
import com.wook.web.credo.kiosk.R;
import com.wook.web.credo.kiosk.ble.BluetoothLeServiceCPR;
import com.wook.web.credo.kiosk.db.AppDatabase;
import com.wook.web.credo.kiosk.db.Converters;
import com.wook.web.credo.kiosk.db.Report;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.disposables.Disposable;
import soup.neumorphism.NeumorphButton;
import soup.neumorphism.NeumorphImageButton;

@SuppressLint({"LogNotTimber", "SetTextI18n", "SimpleDateFormat"})
public class CPRActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {
    @Override
    public void onNewData(byte[] data) {
        handler.post(() ->{
           receive(data);
        });
    }

    @Override
    public void onRunError(Exception e) {
        handler.post(() -> {
            Log.e(TAG, "onRunError connection lost: " + e.getMessage());
            connect();
        });
    }

    static class ListItem{
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }
    private final static String TAG = CPRActivity.class.getSimpleName();
    private static int depth_true, depth_false, depth_over;
    private static ToBinary ToBinary;
    private SharedPreferences sharedPreferences;
    private final HashMap<String, String> Devices = new HashMap<>();
    private BackPressCloseHandler backPressCloseHandler;
    private boolean mConnected = false;
    private final ArrayList<UserItem> cprItem01 = new ArrayList<>();
    public static ListViewAdapterCPR listViewAdapterCPR;
    public static ListView listviewCPR;
    private USBDeviceListAdapter usbDeviceListAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 20000; //scan time 4000
    private Button depth_btn01, press_ave_btn01, standard_btn01;
    private View view01;
    private ImageView lung01, test_lung01, cpr_arrow01, cpr_arrow01_, cpr_ani01, cpr_ani02, press_position;
    private ClipDrawable lung_clip01;
    private TextView cpr_timer;
    private Button standardCPR_btn01, depth_btn_cpr_up;
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime, interval01, StartTime_L = 0L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int score_01, cycle_01 = 0;
    private int cycle = 30;
    private int Seconds_, Seconds, Minutes, mSeconds_, handOff_01;

    private final ArrayList<Float> breathVal01 = new ArrayList<Float>() {{
        add(200.0f);
    }};
    private final ArrayList<Float> breathTime01 = new ArrayList<Float>() {{
        add(0.0f);
    }};
    private final ArrayList<Float> pressTimeList01 = new ArrayList<Float>();
    private ArrayList<Float> bluetoothTimeList01 = new ArrayList<Float>() {{
        add(0f);
    }};
    private ArrayList<Float> lungTimeList01;
    private boolean isBreath01 = false, device2_connect = false, testMode = false, playCheck = false, isCali01 = false,
            isAdult = true, isReversed = false, isLungDrawing = false, isBreOver01 = false, isBreBelow01 = false, isImageNormal01 = true,
            isReady = false;
    private final ArrayList<Long> peakTimes = new ArrayList<Long>();
    ArrayList<Float> bpm1 = new ArrayList<>(), tmp_bpm1 = new ArrayList<>();
    private float position_bpm = 0f, div_interval;
    private int position_num01, position_correct01, lung_num01, lung_correct01, interval = 100, minDepth = 55, maxDepth = 65,
            frame_width, frame_interval, press_width, max_lung01 = 100, min_lung01 = 64, ventil_volume_01, bre_threshold01 = 69, over_breath01,
            bre_level01, bre_level02, event_time, depth_correct, depth_num, angle01, position01, breath01;
    private TextView remote_depth_text, remote_arrow_down_text, remote_arrow_up_text;
    private ImageButton press_point_btn;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION"};
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private ArrayList<Integer> lung_list01, lung_list_LONG;
    private ImageView anne;
    private View depthCPR_view01;
    private LinearLayout cpr_layout_01;
    private enum UsbPermission { Unknown, Requested, Granted, Denied }
    private static final int WRITE_WAIT_MILLIS = 2000;
    private static final int READ_WAIT_MILLIS = 2000;

    private int baudRate = 28800;
    private int deviceId, portNum;
    private boolean connected = false;
    private SerialInputOutputManager usbIoManager;
    private UsbSerialPort usbSerialPort;
    private UsbPermission usbPermission = UsbPermission.Unknown;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";
    private String today = "";
    private String startCommand = "%";
    private String stopCommand = "&";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                usbPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        ? UsbPermission.Granted : UsbPermission.Denied;
                connect();
            }
        }
    };

    public void onBackPressed() {
        this.backPressCloseHandler.onBackPressed();
    }


    //TODO onCreate
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mode_cpr);
        usbDeviceListAdapter = new USBDeviceListAdapter();
        lung_list_LONG = new ArrayList<>();
        lung_list01 = new ArrayList<>();
        lungTimeList01 = new ArrayList<>();
        bluetoothTimeList01 = new ArrayList<>();

        cpr_layout_01 = findViewById(R.id.cpr_layout_01);
        press_position = findViewById(R.id.press_position);
        press_point_btn = findViewById(R.id.press_point_btn);

        sharedPreferences = getApplication().getSharedPreferences("DeviceCPR", MODE_PRIVATE);

        anne = findViewById(R.id.anne);
        depth_btn_cpr_up = findViewById(R.id.depth_btn_cpr_up);
        standardCPR_btn01 = findViewById(R.id.standardCPR_btn01);

        cpr_timer = findViewById(R.id.cpr_timer);
        cpr_arrow01 = findViewById(R.id.cpr_arrow01);
        cpr_arrow01_ = findViewById(R.id.cpr_arrow01_);
        depthCPR_view01 = findViewById(R.id.depthCPR_view01);
        depth_btn01 = findViewById(R.id.depth_btn_cpr_01);
        press_ave_btn01 = findViewById(R.id.press_ave_btn_cpr_01);
        view01 = findViewById(R.id.depthCPR_view01);

        view01.post(() -> {
            depth_true = view01.getHeight();
            depth_over = view01.getHeight() + 50;
            depth_false = view01.getHeight() / 3;
        });

        LinearLayout layout100 = findViewById(R.id.cpr_layout100);
        LinearLayout layout120 = findViewById(R.id.cpr_layout120);

        FrameLayout positionLayout = findViewById(R.id.position_layout);
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(this, R.drawable.position_press);
        positionLayout.post(() -> {
            int layout2_width = layout120.getWidth();
            frame_width = positionLayout.getWidth();
            ViewGroup.LayoutParams btn_params = (ViewGroup.LayoutParams) press_ave_btn01.getLayoutParams();
            btn_params.width = frame_width / 9;
            press_ave_btn01.setLayoutParams(btn_params);
            press_width = press_ave_btn01.getWidth();

            frame_interval = (frame_width - press_width) / 4;
            int text_interval = frame_interval + layout2_width / 2;

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layout100.getLayoutParams();
            params.setMargins(text_interval, 0, 0, 0);
            layout100.setLayoutParams(params);
            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) layout120.getLayoutParams();
            params2.setMargins(0, 0, text_interval, 0);
            layout120.setLayoutParams(params2);

            div_interval = (float) frame_interval / (float) 10;

            positionLayout.setBackground(layerDrawable);
        });

        depthCPR_view01.post(() -> {
            view01.setTranslationY(depthCPR_view01.getHeight() * -1.0f);
        });

        standard_btn01 = findViewById(R.id.standardCPR_btn01);

        lung01 = findViewById(R.id.lung01);
        test_lung01 = findViewById(R.id.test_lung01);
        lung_clip01 = (ClipDrawable) test_lung01.getDrawable();
        lung_clip01.setLevel(0);

        cpr_ani01 = findViewById(R.id.cpr_ani01);
        cpr_ani02 = findViewById(R.id.cpr_ani02);

        backPressCloseHandler = new BackPressCloseHandler(this);

        remote_arrow_down_text = findViewById(R.id.remote_arrow_down_text);
        remote_arrow_up_text = findViewById(R.id.remote_arrow_up_text);
        remote_depth_text = findViewById(R.id.remote_depth_text);

        //showConnectDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!connected) {
            refresh();
        }
        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private void connect(){
        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == deviceId)
                device = v;
        if(device == null) {
            connectProgressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, "connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            connectProgressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, "connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < portNum) {
            connectProgressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, "connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if(usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(driver.getDevice())) {
            usbPermission = UsbPermission.Requested;
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_MUTABLE);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if(usbConnection == null) {
            connectProgressBar.setVisibility(View.INVISIBLE);
            if (!usbManager.hasPermission(driver.getDevice()))
                Log.e(TAG, "connection failed: permission denied");
            else
                Log.e(TAG, "connection failed: open failed");
            return;
        }

        try {
            usbSerialPort.open(usbConnection);
            try{
                usbSerialPort.setParameters(baudRate, 8, 1, UsbSerialPort.PARITY_NONE);
            }catch (UnsupportedOperationException e){
                Log.e(TAG, "UnsupportedOperationException");
            }
            usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            usbIoManager.start();

            Log.e(TAG, "usbIoManager start");
            connected = true;
          //  connectProgressBar.setVisibility(View.INVISIBLE);
         //   connectDialog.dismiss();
            showStart(this);
        } catch (Exception e) {
        //    connectProgressBar.setVisibility(View.INVISIBLE);
            disconnect();
        }
    }
    private void disconnect(){
        connected = false;
        if(startDialog != null && startDialog.isShowing()){
            startDialog.dismiss();
        }
        if(usbIoManager != null) {
            usbIoManager.setListener(null);
            usbIoManager.stop();
        }
        usbIoManager = null;
        try {
            usbSerialPort.close();
        } catch (IOException ignored) {}
        catch (NullPointerException e){
            Log.e(TAG, "null pointer exception");
        }
        usbSerialPort = null;
    }

    private ArrayList<Integer> packet = new ArrayList<>();

    private void receive(byte[] data){
        for (byte byteChar : data) {
            int result = Byte.toUnsignedInt(byteChar);
            if(result == 3){
                if(packet.size() == 3){
                    displayData(packet.get(1), packet.get(2));
                    packet.clear();
                }else{
                    packet.add(result);
                }
            }else if(packet.size() == 0 || packet.get(0) == 2){
                packet.add(result);
            }
        }
    }

    private void displayData(int position, int depth){
        switch (position){
            case 0:
            case 15:
                final Animation animation00 = new TranslateAnimation(0, 0, 0, 0);
                animation00.setDuration(200);
                animation00.setFillAfter(false);
                press_point_btn.startAnimation(animation00);
                position_num01++;
                position_correct01++;

                cpr_arrow01.setVisibility(View.INVISIBLE);
                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                break;
            case 11:
                final Animation animation0 = new TranslateAnimation(0, -100, 0, 0);
                animation0.setDuration(200);
                animation0.setFillAfter(false);
                press_point_btn.startAnimation(animation0);
                position_num01++;

                cpr_arrow01.setVisibility(View.INVISIBLE);
                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                break;
            case 12:
                final Animation animation2 = new TranslateAnimation(0, 100, 0, 0);
                animation2.setDuration(200);
                animation2.setFillAfter(false);
                press_point_btn.startAnimation(animation2);
                position_num01++;

                cpr_arrow01.setVisibility(View.INVISIBLE);
                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                break;
            case 13:
                final Animation animation3 = new TranslateAnimation(0, 0, 0, -100);
                animation3.setDuration(200);
                animation3.setFillAfter(false);
                press_point_btn.startAnimation(animation3);
                position_num01++;

                cpr_arrow01.setVisibility(View.INVISIBLE);
                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                break;
            case 14:
                final Animation animation4 = new TranslateAnimation(0, 0, 0, 100);
                animation4.setDuration(200);
                animation4.setFillAfter(false);
                press_point_btn.startAnimation(animation4);
                position_num01++;

                cpr_arrow01.setVisibility(View.INVISIBLE);
                remote_arrow_up_text.setVisibility(View.INVISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                break;
            case 17:
                final Animation animation7 = new TranslateAnimation(0, -100, 0, 0);
                animation7.setDuration(200);
                animation7.setFillAfter(false);
                press_point_btn.startAnimation(animation7);
                position_num01++;
                cpr_arrow01.setVisibility(View.VISIBLE);
                remote_arrow_up_text.setVisibility(View.VISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                break;
            case 18:
                final Animation animation8 = new TranslateAnimation(0, 100, 0, 0);
                animation8.setDuration(200);
                animation8.setFillAfter(false);
                press_point_btn.startAnimation(animation8);
                position_num01++;
                cpr_arrow01.setVisibility(View.VISIBLE);
                remote_arrow_up_text.setVisibility(View.VISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                break;
            case 19:
                final Animation animation9 = new TranslateAnimation(0, 0, 0, -100);
                animation9.setDuration(200);
                animation9.setFillAfter(false);
                press_point_btn.startAnimation(animation9);
                position_num01++;
                cpr_arrow01.setVisibility(View.VISIBLE);
                remote_arrow_up_text.setVisibility(View.VISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                break;
            case 20:
                final Animation animation10 = new TranslateAnimation(0, 0, 0, 100);
                animation10.setDuration(200);
                animation10.setFillAfter(false);
                press_point_btn.startAnimation(animation10);
                position_num01++;
                cpr_arrow01.setVisibility(View.VISIBLE);
                remote_arrow_up_text.setVisibility(View.VISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                break;
            case 21:
                final Animation animation11 = new TranslateAnimation(0, 0, 0, 0);
                animation11.setDuration(200);
                animation11.setFillAfter(false);
                press_point_btn.startAnimation(animation11);
                position_num01++;
                position_correct01++;
                cpr_arrow01.setVisibility(View.VISIBLE);
                remote_arrow_up_text.setVisibility(View.VISIBLE);
                depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point_red));
                break;
        }

        if (!isReversed) {
            anne.setVisibility(View.VISIBLE);
            if (isAdult) {
                remote_depth_text.setVisibility(View.VISIBLE);
            }
            cpr_ani01.setVisibility(View.VISIBLE);
            cpr_ani02.setVisibility(View.VISIBLE);
            standardCPR_btn01.setVisibility(View.VISIBLE);
            depth_btn01.setVisibility(View.VISIBLE);
            depth_btn_cpr_up.setVisibility(View.VISIBLE);
            depthCPR_view01.setVisibility(View.VISIBLE);
            lung01.setVisibility(View.INVISIBLE);
            test_lung01.setVisibility(View.INVISIBLE);
            lung_list01.clear();
            lungTimeList01.clear();
        }

        lung01.setVisibility(View.INVISIBLE);
        test_lung01.setVisibility(View.INVISIBLE);
        lung_list01.clear();
        lungTimeList01.clear();

        interval01 = System.currentTimeMillis();

        Animation animation = new TranslateAnimation(0, 0, 0, depth_true);

        new Thread(() -> runOnUiThread(() -> {
            int value;
            if (depth >= 70)
                value = 70;
            else
                value = depth;
            remote_depth_text.setText(String.valueOf(value));
            if ((0 < depth && depth < minDepth) || (maxDepth < depth)) {
                cprItem01.add(new UserItem(Seconds_, depth, 0, angle01, position01));
                view01.setBackgroundColor(Color.parseColor("#FF4D4D"));
            } else if (depth >= minDepth) {
                cprItem01.add(new UserItem(Seconds_, 0, depth, angle01, position01));
                view01.setBackgroundColor(Color.parseColor("#4AFF5E"));
            }

        })).start();

        int Depth_correct_sum01 = 0;
        int Depth_size = 0;

        for (UserItem userItem : cprItem01) {
            if (userItem.getDepth_correct() != 0)
                Depth_correct_sum01 = Depth_correct_sum01 + 1;
            if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0)
                Depth_size = Depth_size + 1;
        }

        while (peakTimes.size() > 2) {
            peakTimes.remove(0);
        }
        long now2 = System.currentTimeMillis();
        now2 = now2 - (now2 % 10);
        peakTimes.add(now2);
        setBpm();
        float presstime = (float) ((now2 - StartTime_L) / 1000.0f);
        pressTimeList01.add(presstime);

        if (cprItem01.size() != 0) {
            score_01 = (int) (((double) Depth_correct_sum01 / (double) Depth_size) * 100);
            cycle_01 = (Depth_size / cycle) + 1;
            depth_correct = Depth_correct_sum01;
            depth_num = Depth_size;
        }

        if (!isReversed) {
            if (depth >= minDepth && depth <= maxDepth)
                animation = new TranslateAnimation(0, 0, 0, depth_true);
            if (depth < minDepth) {
                animation = new TranslateAnimation(0, 0, 0, depth_false);
            }
            if (depth > maxDepth) {
                animation = new TranslateAnimation(0, 0, 0, depth_over);
            }

            animation.setDuration(350);
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (depth > minDepth && depth < maxDepth) {
                        standard_btn01.setBackground(getDrawable(R.drawable.anne_point_green));
                    } else if ((0 < depth && depth <= minDepth) || (maxDepth <= depth)) {
                        standard_btn01.setBackground(getDrawable(R.drawable.anne_point_red));
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final Animation animation1 = new TranslateAnimation(0, 0, 0, 0);
                    animation1.setDuration(200);
                    animation1.setFillAfter(false);

                    view01.setBackgroundColor(Color.parseColor("#777777"));
                    standard_btn01.setBackground(getDrawable(R.drawable.anne_point));
                    depth_btn01.startAnimation(animation1);
                    view01.startAnimation(animation1);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if (depth >= minDepth && depth <= maxDepth) {
                Animation animation1 = new TranslateAnimation(0, cpr_ani01.getWidth() + 100, 0, 0);
                animation1.setDuration(550);
                animation1.setFillAfter(false);
                animation1.setInterpolator(new AccelerateDecelerateInterpolator());

                Animation animation2 = new TranslateAnimation(0, -cpr_ani02.getWidth() - 100, 0, 0);
                animation2.setDuration(550);
                animation2.setFillAfter(false);
                animation2.setInterpolator(new AccelerateDecelerateInterpolator());

                cpr_ani01.startAnimation(animation1);
                cpr_ani02.startAnimation(animation2);
            }

            depth_btn01.startAnimation(animation);
            view01.startAnimation(animation);
        }
    }

    void setBpm() {
        float currentBpm = 0f;
        int peak_size = peakTimes.size();

        if (peak_size > 1) {
            long lastPeakTime = peakTimes.get(0);
            if (System.currentTimeMillis() - lastPeakTime <= 1500) {
                float interval = (float) (peakTimes.get(1) - peakTimes.get(0));
                while (tmp_bpm1.size() > 4) {
                    tmp_bpm1.remove(0);
                }
                tmp_bpm1.add((60_000f / interval));
            } else {
                try {
                    if (peakTimes != null) {
                        peakTimes.clear();
                        tmp_bpm1.clear();
                    }
                } catch (Exception e) {
                }
            }
        }

        if (!tmp_bpm1.isEmpty() && tmp_bpm1.size() > 4) {
            float tmp_bpm = 0;
            for (float bpm : tmp_bpm1)
                tmp_bpm += bpm;
            currentBpm = tmp_bpm / tmp_bpm1.size();
            bpm1.add(currentBpm);
        }

        Animation animation = null;
        if (currentBpm != 0) {
            if (currentBpm > 140) {
                float XDelta = (float) frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 120) {
                float XDelta = frame_interval * 3 + (currentBpm - 120) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 110) {
                float XDelta = frame_interval * 2 + (currentBpm - 110) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else if (currentBpm >= 100) {
                float XDelta = frame_interval + (currentBpm - 100) * div_interval;
                if (XDelta > frame_width - press_width)
                    XDelta = frame_width - press_width;
                animation = new TranslateAnimation(position_bpm, XDelta, 0, 0);
                position_bpm = XDelta;
            } else {
                animation = new TranslateAnimation(position_bpm, currentBpm * div_interval / 10, 0, 0);
                position_bpm = currentBpm * div_interval / 10;
            }
            animation.setDuration(200);
            animation.setFillAfter(true);
            press_ave_btn01.startAnimation(animation);

            if (currentBpm > 120 || currentBpm < 100) {
                press_ave_btn01.setBackgroundResource(R.drawable.position_press_red);
            } else {
                press_ave_btn01.setBackgroundResource(R.drawable.position_press_green);
            }

        } else {
            animation = new TranslateAnimation(position_bpm, 0, 0, 0);
            animation.setDuration(400);
            animation.setFillAfter(true);
            position_bpm = 0;
            press_ave_btn01.startAnimation(animation);
            press_ave_btn01.setBackgroundResource(R.drawable.position_press_red);
        }
    }

    private class USBDeviceListAdapter extends BaseAdapter{
        private ArrayList<ListItem> mLeDevices;
        private LayoutInflater mInflater;

        public USBDeviceListAdapter(){
            super();
            mLeDevices = new ArrayList<>();
            mInflater = CPRActivity.this.getLayoutInflater();
        }

        public ListItem getDevice(int position){
            return mLeDevices.get(position);
        }

        public void addDevice(ListItem device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public void remove(int position) {
            mLeDevices.remove(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override   //리스트뷰의 아이템을 가져옴
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            CPRActivity.ViewHolder viewHolder;

            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_usbdevice, null);
                viewHolder = new CPRActivity.ViewHolder();
                viewHolder.deviceName = view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (CPRActivity.ViewHolder) view.getTag();
            }

            ListItem device = mLeDevices.get(i);
            final String deviceName = device.device.getDeviceName();

            viewHolder.deviceName.setText(deviceName);
            viewHolder.deviceName.setTextColor(Color.parseColor("#ff5b00"));
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
    }

    public class BackPressCloseHandler {
        private final Activity activity;
        private long backKeyPressedTime;
        private Toast toast;

        public BackPressCloseHandler(Activity context) {
            this.backKeyPressedTime = 0;
            this.activity = context;
        }

        public void onBackPressed() {
        }
    }

    private ProgressBar connectProgressBar;
    private AlertDialog connectDialog;
    private void showConnectDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(CPRActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.cpr_connect_layout, null);
        builder.setView(view);

        connectProgressBar = view.findViewById(R.id.connectProgressBar);
        ListView listView = view.findViewById(R.id.List_device);
        connectDialog = builder.create();

        refresh();

        listView.setAdapter(usbDeviceListAdapter);
        listView.setOnItemClickListener((parent, v, position, id) -> {
            ListItem item = usbDeviceListAdapter.getDevice(position);
            deviceId = item.device.getDeviceId();
            portNum = item.port;
            connectProgressBar.setVisibility(View.VISIBLE);
            connect();
        });

        connectDialog.setCancelable(false);
        connectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        connectDialog.show();
    }

    private void refresh(){
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        usbDeviceListAdapter.clear();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                //vendorId = 4292, productId = 60000, prooductname = CP2102 USB to UART Bridge Controller, getManufacturerName = Silicon Labs
               // for(int port = 0; port < driver.getPorts().size(); port++) //vendor = 4294 port = 60000
             //       usbDeviceListAdapter.addDevice(new ListItem(device, port, driver));
                if(device.getVendorId() == 4292 && device.getProductId() == 60000){
                    deviceId = device.getDeviceId();
                    portNum = 0;
                    connect();
                }
            } else {
                usbDeviceListAdapter.addDevice(new ListItem(device, 0, null));
            }
        }

        usbDeviceListAdapter.notifyDataSetChanged();
    }
    private Button startbtn;
    private AlertDialog startDialog;
    private boolean isStartClicked = false;

    private void showStart(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.show_start_ayout, null);
        builder.setView(view);
        startbtn = (NeumorphButton) view.findViewById(R.id.show_start_01);

        startbtn.setOnClickListener(view1 -> {
            if(!isStartClicked) start();
            isStartClicked = true;
        });

        startDialog = builder.create();

        startDialog.setCancelable(false);
        startDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (!CPRActivity.this.isFinishing())
            startDialog.show();
    }

    private void start(){
        new Thread(() -> {
            for (int i = 5; i >= 0; i--) {
                try {
                    startbtn.setText(String.valueOf(i));
                    if (i == 0) {
                        startbtn.setText(getString(R.string.start));
                        Thread.sleep(1000);

                        StartTime = SystemClock.uptimeMillis();
                        StartTime_L = System.currentTimeMillis();
                        interval01 = System.currentTimeMillis();
                        handler.postDelayed(runnable, 0);
                        interval01 = System.currentTimeMillis();

                        breathTime01.clear();
                        breathTime01.add(0.0f);
                        breathVal01.clear();
                        breathVal01.add(200.0f);
                        lung_list_LONG.clear();
                        cprItem01.clear();
                        angle01 = 0;
                        position01 = 0;
                        breath01 = 0;
                        peakTimes.clear();
                        position_bpm = 0f;
                        Seconds_ = 0;
                        score_01 = 0;
                        cycle_01 = 0;
                        depth_num = 0;
                        depth_correct = 0;
                        position_num01 = 0;
                        position_correct01 = 0;
                        lung_num01 = 0;
                        lung_correct01 = 0;
                        startDialog.dismiss();
                        isStartClicked = false;

                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd/HH:mm:ss");
                        today = format.format(date);

                        send(startCommand);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void send(String str){
        try{
            byte[] data = (str).getBytes();
            usbSerialPort.write(data, WRITE_WAIT_MILLIS);
            Log.e(TAG, "write "+str);
        }catch (Exception e){
            onRunError(e);
        }
    }

    float max_secs = 0.0f;

    protected void onDestroy() {
        super.onDestroy();

    }

    //TODO Timer
    private Runnable runnable = new Runnable() {

        public void run() {
            int event_time = 60;
            //Log.e("event_time", String.valueOf(event_time));

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            //Log.e("update_time", String.valueOf(UpdateTime / 1000));

            //Seconds_ = event_time - (int) (UpdateTime / 1000);

            Seconds_ = (int) (UpdateTime / 1000);

            //MilliSeconds = (int) (UpdateTime % 1000);

            mSeconds_ = event_time - (int) (UpdateTime / 1000);

            Minutes = mSeconds_ / 60;

            Seconds = mSeconds_ % 60;

            /*Log.e("Minutes_time", String.valueOf(Minutes));
            Log.e("Seconds_time", String.valueOf(Seconds));*/

            if(Seconds_ == 60){
                reset(2);
            }

            cpr_timer.setText("" + String.format("%02d", Minutes) + " : "
                    + String.format("%02d", Seconds));

            handler.postDelayed(this, 0);

            long now = System.currentTimeMillis() - interval01;
            float secs = now / 1000.0f;
            if (now >= 1500) {
                if (handOff_01 < Seconds_) {
                    if (max_secs < secs)
                        max_secs = secs;
                    handOff_01 = Seconds_;
                }
                if (!isBreath01) {
                    depth_btn_cpr_up.setBackground(getDrawable(R.drawable.anne_point));
                    cpr_arrow01_.setVisibility(View.VISIBLE);
                    remote_arrow_down_text.setVisibility(View.VISIBLE);
                    cpr_arrow01.setVisibility(View.INVISIBLE);
                    remote_arrow_up_text.setVisibility(View.INVISIBLE);
                }
            } else {
                if (max_secs != 0)
                    cprItem01.add(new UserItem(Seconds_, max_secs, 0));

                max_secs = 0.0f;
                handOff_01 = Seconds_;
                cpr_arrow01_.setVisibility(View.INVISIBLE);
                remote_arrow_down_text.setVisibility(View.INVISIBLE);
            }
        }
    };

    private void reset(int set) {
        TimeBuff += MillisecondTime;
        handler.removeCallbacks(runnable);

        lung01.setVisibility(View.INVISIBLE);
        test_lung01.setVisibility(View.INVISIBLE);
        anne.setVisibility(View.VISIBLE);
        remote_depth_text.setVisibility(View.VISIBLE);
        cpr_ani01.setVisibility(View.VISIBLE);
        cpr_ani02.setVisibility(View.VISIBLE);
        standardCPR_btn01.setVisibility(View.VISIBLE);
        depth_btn01.setVisibility(View.VISIBLE);
        depth_btn_cpr_up.setVisibility(View.VISIBLE);
        depthCPR_view01.setVisibility(View.VISIBLE);
        press_position.setVisibility(View.VISIBLE);

        send(stopCommand);

        // runHander(false);

        if (set == 2) {
            if (max_secs != 0)
                cprItem01.add(new UserItem(Seconds_, max_secs, 0));

            try {
                ReportItem reportItem = report_setting(cprItem01, "", pressTimeList01, breathVal01, breathTime01, String.valueOf(ventil_volume_01),
                        String.valueOf(cycle_01), String.valueOf(score_01),
                        String.valueOf(minDepth), String.valueOf(maxDepth),
                        String.valueOf(depth_num), String.valueOf(depth_correct), String.valueOf(position_num01), String.valueOf(position_correct01),
                        String.valueOf(lung_num01), String.valueOf(lung_correct01), bpm1, bluetoothTimeList01);

                Intent intent = new Intent(this, ReportActivity.class);
                Gson gson = new Gson();
                String json = gson.toJson(reportItem);
                intent.putExtra("report", json);
                intent.putExtra("today", today);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MillisecondTime = 0L;
        StartTime = 0L;
        StartTime_L = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        angle01 = 0;
        mSeconds_ = 0;
        max_secs = 0.0f;
        position01 = 0;

        breath01 = 0;

        cpr_timer.setText("00:00");

        cpr_arrow01.setVisibility(View.INVISIBLE);

        cpr_arrow01_.setVisibility(View.INVISIBLE);

        remote_depth_text.setText("0");
        remote_arrow_down_text.setVisibility(View.INVISIBLE);
        remote_arrow_up_text.setVisibility(View.INVISIBLE);

        final Animation animation = new TranslateAnimation(0, 0, 0, 0);
        depth_btn01.startAnimation(animation);
        press_ave_btn01.startAnimation(animation);

        Thread.interrupted();

        breathTime01.clear();
        breathTime01.add(0.0f);
        breathVal01.clear();
        breathVal01.add(200.0f);
        pressTimeList01.clear();
        peakTimes.clear();
        bluetoothTimeList01.clear();
        position_bpm = 0f;
        ventil_volume_01 = 0;

        if (set == 1) {
            Intent intent;
            mConnected = false;
            finish();
            overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        }

        Seconds_ = 0;
        score_01 = 0;
        cycle_01 = 0;

        cprItem01.clear();

        depth_num = 0;
        depth_correct = 0;
        position_num01 = 0;
        position_correct01 = 0;
        lung_num01 = 0;
        lung_correct01 = 0;
    }

    private ReportItem report_setting(ArrayList<UserItem> Useritem, String name, ArrayList<Float> presstimeList,
                                      ArrayList<Float> breathval, ArrayList<Float> breathtime, String ventil_volume,
                                      String cycle, String score,
                                      String min, String max, String depth_num, String depth_correct,
                                      String position_num, String position_correct, String lung_num, String lung_correct, ArrayList<Float> gBpm, ArrayList<Float> bluetoothtime_list) {
        ReportItem reportItem = null;
        ArrayList<Float> arrayList = new ArrayList<Float>();
        ArrayList<Float> stopList = new ArrayList<>();

        int Depth_size = 0;
        int position_six = 0;
        int angleSum = 0;
        int hand_off = 0;
        int position = 0;

        if (!Useritem.isEmpty())
            for (UserItem userItem : Useritem) {
                if (userItem.getAngle() != 0)
                    angleSum = angleSum + userItem.getAngle();

                if (userItem.getPosition() > 16)
                    position_six = position_six + 1;

                if (userItem.getPosition() == 0 || userItem.getPosition() == 15)
                    position = position + 1;

                if (userItem.getDepth_correct() != 0 || userItem.getDepth() != 0) {
                    Depth_size = Depth_size + 1;
                    if (userItem.getDepth_correct() != 0) {
                        if (userItem.getPosition() > 16)
                            arrayList.add((userItem.getDepth_correct() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth_correct());
                    } else {
                        if (userItem.getPosition() > 16)
                            arrayList.add((userItem.getDepth() + 100f));
                        else
                            arrayList.add((float) userItem.getDepth());
                    }
                }

                if (userItem.getHand_off_start() != 0) {
                    arrayList.add((float) 0);
                    stopList.add(userItem.getHand_off_start());
                    hand_off += (int) userItem.getHand_off_start();
                }

                if (userItem.getBreath() != 0) {
                    arrayList.add((float) userItem.getBreath());
                }
            }

        int add_bpm = 0;

        if (gBpm != null && !gBpm.isEmpty()) {
            for (Float item_bpm : gBpm) {
                add_bpm += item_bpm;
            }
        }

        int up_depth = (int) (100 - ((double) position_six / (double) Depth_size) * 100);
        int position_;
        position_ = (int) (((double) position / (double) Depth_size) * 100);

        int bpm = 0;
        if (gBpm.size() > 0) {
            bpm = add_bpm / gBpm.size();
        }
        int angle = (int) (((double) angleSum / (double) Depth_size));
        if (bluetoothtime_list.isEmpty()) {
            bluetoothtime_list.add(0f);
        }

        reportItem = new ReportItem(name
                , String.valueOf(Seconds_)
                , String.valueOf(hand_off)
                , cycle
                , String.valueOf(position_)
                , String.valueOf(up_depth)
                , score
                , String.valueOf(bpm)
                , String.valueOf(angle)
                , arrayList
                , presstimeList
                , breathtime
                , breathval
                , ventil_volume
                , min
                , max
                , depth_num
                , depth_correct
                , position_num
                , position_correct
                , lung_num
                , lung_correct
                , stopList
                , bluetoothtime_list
        );

        return reportItem;
    }

}

