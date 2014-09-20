package com.schef.rss.android;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.schef.rss.android.db.ArsEntity;

import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by scheffela on 7/26/14.
 */
public class NewApplication extends Application {

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final long KEEP_ALIVE_TIME = 4;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;

    // A queue of Runnables
    private BlockingQueue<Runnable> mDecodeWorkQueue;

    // A Thread Pool Of Runnables
    private ThreadPoolExecutor threadPoolExecutor;

    private TreeSet<ArsEntity> itemsTreeSet = new TreeSet<ArsEntity>();

    private Ars3d ars3d;

    private static NewApplication instance;

    public static NewApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        BugSenseHandler.initAndStartSession(this, "45b86b4a");

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
            new Thread( new Runnable() {
                @Override
                public void run() {
                    Log.e("Handler","Called Parse");
                    mService.parse();
                }
            }).start();

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
}
