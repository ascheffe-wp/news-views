package com.schef.rss.android;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.schef.rss.android.db.ArsEntity;
import com.splunk.mint.Mint;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by scheffela on 7/26/14.
 */
public class NewApplication extends Application {

    protected static final String TAG = NewApplication.class.getSimpleName();

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final long KEEP_ALIVE_TIME = 4;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;

    private TextToSpeech mTts;
    private android.speech.tts.TextToSpeech.OnInitListener textListener;
    private boolean txtInit = false;

    // A queue of Runnables
    private BlockingQueue<Runnable> mDecodeWorkQueue;

    // A Thread Pool Of Runnables
    private ThreadPoolExecutor threadPoolExecutor;

    private TreeSet<ArsEntity> itemsTreeSet = new TreeSet<ArsEntity>();

    private Ars3d ars3d;

    private ConfigPojo configPojo;

    private static NewApplication instance;

    public static NewApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;

        ConfigPojo tmp = getConfigFromFile(getApplicationContext());
        if(tmp != null) {
            Log.i(TAG,"Got config from file");
            configPojo = tmp;
        } else {
            Log.i(TAG,"Using default Config");
            configPojo = new ConfigPojo();
        }

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Mint.initAndStartSession(this, "45b86b4a");
        Mint.setUserIdentifier(android_id);


        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

        // Creates a thread pool manager
        threadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mDecodeWorkQueue);

        Log.e("ServiceConnected","Starting Service");
        Intent intent = new Intent(this, ArsDataFetcherService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        textListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                txtInit = true;
            }
        };
        mTts = new TextToSpeech(getApplicationContext(), textListener);

    }

    public ArsDataFetcherService mService;
    public boolean mBound;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e("ServiceConnected","Called Connect");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ArsDataFetcherService.LocalBinder binder = (ArsDataFetcherService.LocalBinder) service;
            mService = binder.getService();

            mService.registerHandler(newHandler);
            mService.setItemsTreeSetRef(itemsTreeSet);
            Log.e("ServiceConnected","Called Runnable");
            mBound = true;
            performParse(false);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void performParse (final boolean clean) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                Log.e("Handler","Called Parse");
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                int netType = getNetworkClass(getApplicationContext());

                if (mService != null && (wifi != null && wifi.isConnectedOrConnecting() || (netType > 0 && netType != -1))) {
                    mService.parse(clean);
                } else {

                }
            }
        }).start();
    }

    public static int NET2G = 1;
    public static int NET3G = 2;
    public static int NET4G = 3;

    public static int getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NET2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NET3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return NET4G;
            default:
                return -1;
        }
    }

    private final Handler newHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("Handler", "got called");
            String action = msg.getData().getString("action");
            if(action != null && !action.isEmpty()) {
                if(action.equalsIgnoreCase("update")) {
                    if(ars3d != null) {
                        ars3d.setImageUpdates(true);
                    }
                }
            }
        }
    };

    public ConfigPojo getConfigFromFile (Context context) {
        ConfigPojo configPojo = null;
        try {
            Gson gson = new Gson();
            FileInputStream fis = context.openFileInput("config.json");
            String serial = IOUtils.toString(fis);
            configPojo = gson.fromJson(serial, ConfigPojo.class);
            IOUtils.closeQuietly(fis);
        } catch (Exception e) {
            Log.e("ConfigLoader", "problem loading config", e);
        }
        return configPojo;
    }


    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public TreeSet<ArsEntity> getItemsTreeSet() {
        return itemsTreeSet;
    }

    public void setItemsTreeSet(TreeSet<ArsEntity> itemsTreeSet) {
        this.itemsTreeSet = itemsTreeSet;
    }

    public Ars3d getArs3d() {
        return ars3d;
    }

    public void setArs3d(Ars3d ars3d) {
        this.ars3d = ars3d;
    }

    public ConfigPojo getConfigPojo() {
        return configPojo;
    }

    public void setConfigPojo(ConfigPojo configPojo) {
        this.configPojo = configPojo;
    }

    public TextToSpeech.OnInitListener getTextListener() {
        return textListener;
    }

    public void setTextListener(TextToSpeech.OnInitListener textListener) {
        this.textListener = textListener;
    }

    public TextToSpeech getmTts() {
        return mTts;
    }

    public void setmTts(TextToSpeech mTts) {
        this.mTts = mTts;
    }


}
