package com.schef.rss.android;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class MyActivity extends Activity implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = MyActivity.class.getSimpleName();

    /** Request code for launching the Intent to resolve Google Play services errors. */
    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private TextView mTextView;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private List<Node> curNode = new ArrayList<Node>();

    ViewPager mPager;
    FragmentPagerAdapter mAdapter;

    private List<ArsEntity> fromJson = new ArrayList<ArsEntity>();

    SampleGridPagerAdapter sampleGridPagerAdapter;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my);
//        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
//        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
//            @Override
//            public void onLayoutInflated(WatchViewStub stub) {
//                mTextView = (TextView) stub.findViewById(R.id.text);
//            }
//        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_main);
        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);
                return insets;
            }
        });

        sampleGridPagerAdapter = new SampleGridPagerAdapter(this, getFragmentManager(), fromJson, mGoogleApiClient , curNode);
        pager.setAdapter(sampleGridPagerAdapter);

        context = getApplicationContext();

        pager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, int i2, float v, float v2, int i3, int i4) {

            }

            @Override
            public void onPageSelected(int i, int i2) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                    if (mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                        Wearable.MessageApi.sendMessage(
                                mGoogleApiClient, curNode.get(0).getId(), "/stop", "2".getBytes()).setResultCallback(
                                new ResultCallback<MessageApi.SendMessageResult>() {
                                    @Override
                                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                        if (sendMessageResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                                    + sendMessageResult.getStatus());
                                        }
                                    }
                                }
                        );
                    }
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        mResolvingError = false;

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodeResult) {
                        if (nodeResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                    + nodeResult.getStatus());
                            curNode.clear();
                            curNode.addAll(nodeResult.getNodes());
                            if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                                Wearable.MessageApi.sendMessage(
                                        mGoogleApiClient, curNode.get(0).getId(), "/getLayout", "2".getBytes()).setResultCallback(
                                        new ResultCallback<MessageApi.SendMessageResult>() {
                                            @Override
                                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                                if (sendMessageResult.getStatus().isSuccess()) {
                                                    Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                                            + sendMessageResult.getStatus());
                                                }
                                            }
                                        }
                                );
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
            Wearable.DataApi.addListener(mGoogleApiClient, this);
            Wearable.MessageApi.addListener(mGoogleApiClient, this);
            Wearable.NodeApi.addListener(mGoogleApiClient, this);
        } else {

        }
    }

    @Override
    protected void onStop() {
        if (!mResolvingError) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            mResolvingError = false;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();

            if(event.getType() == DataEvent.TYPE_CHANGED) {
                if (uri.getPath().startsWith("/sectionLayout")) {
                    // Set the data of the message to be the bytes of the Uri.
                    try {
                        String tmp = new String(event.getDataItem().getData(), "UTF-8");
                        tmp = tmp.substring(tmp.indexOf("[{"));
                        Gson gson = new Gson();
                        TreeSet<ArsEntity> newFromJson =
                                gson.fromJson(tmp , new TypeToken<TreeSet<ArsEntity>>() {}.getType());

                        fromJson.clear();
                        fromJson.addAll(newFromJson);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sampleGridPagerAdapter.notifyDataSetChanged();
                            }
                        });

                        DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                        downloadFilesTask.execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (uri.getPath().startsWith("/data/data")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                    Bitmap bmp = loadBitmapFromAsset(profileAsset);
                    FileOutputStream out = null;
                    try {
                        String newPath = uri.getPath().replaceAll(".*/app_localfiles","");
                        File tmp = new File(context.getFilesDir(),newPath);
                        tmp.createNewFile();
                        out = new FileOutputStream(tmp);
                        bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, uri).setResultCallback(
                            new ResultCallback<DataApi.DeleteDataItemsResult>() {
                                @Override
                                public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                                    if (deleteDataItemsResult.getStatus().isSuccess()) {
                                        Log.e(TAG, "Deleted Uri " + uri + " with status: "
                                                + deleteDataItemsResult.getStatus());
                                    }
                                }
                            });
                }
            }
        }

    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
//        ConnectionResult result =
//                mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
//        if (!result.isSuccess()) {
//            return null;
//        }
        // convert asset into a file descriptor and block until it's ready
//        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                mGoogleApiClient, asset).await().getInputStream();

        PendingResult<DataApi.GetFdForAssetResult> pr =  Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset);
        if(pr != null) {
            DataApi.GetFdForAssetResult getFdForAssetResult = pr.await();
            if(getFdForAssetResult != null) {
                InputStream assetInputStream = getFdForAssetResult.getInputStream();
                if (assetInputStream == null) {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }
                // decode the stream into a bitmap
                return BitmapFactory.decodeStream(assetInputStream);
            }
        }
        return null;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/startedPlayback")) {
//            Fragment cur = getFragmentManager().findFragmentById(R.id.watch_frag_id);
//            cur.getView().findViewById(R.id.progress_id).setVisibility(View.GONE);
//            sampleGridPagerAdapter.
//            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_id);
        }

//
    }

    @Override
    public void onPeerConnected(Node node) {
        curNode.add(node);

        if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, curNode.get(0).getId(), "/getLayout", "2".getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                        + sendMessageResult.getStatus());
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onPeerDisconnected(Node node) {
        curNode.remove(node);
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {
//        private Context context;

        @Override
        protected Void doInBackground(Void... params) {
//            String [] exts = {"jpg"};
//            Map<String,String> fileMap = new HashMap<String,String>();
            Map<String,File> localfileMap = new HashMap<String,File>();
            Collection<File> files =  FileUtils.listFiles(context.getFilesDir(), null, false);
            for(File file : files) {
                localfileMap.put(file.getName(),file);
            }
//            for(ArsEntity arsEntity : fromJson) {
//                fileMap.put(arsEntity.getLocalImgPath(),arsEntity.getLocalImgPath());
//            }
            List<ArsEntity> neededImages = new ArrayList<ArsEntity>();
            for(ArsEntity arsEntity : fromJson) {
                if(localfileMap.containsKey(arsEntity.getLocalImgPath().replaceAll(".*/",""))) {
                    localfileMap.remove(arsEntity.getLocalImgPath().replaceAll(".*/",""));
                } else {
                    neededImages.add(arsEntity);
                }
            }

            for(File file : localfileMap.values()) {
                file.delete();
            }

            PutDataMapRequest pdr = PutDataMapRequest.create("/imageRequests");
            PutDataRequest request = pdr.asPutDataRequest();
            Gson gson = new Gson();
            String json = gson.toJson(neededImages);
            pdr.getDataMap().putString("request",json);
            request = pdr.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(
                    new ResultCallback<DataApi.DataItemResult> () {
                        @Override
                        public void onResult(DataApi.DataItemResult result) {
                            if(result.getStatus() != null) {

                            }
                        }
                    }
            );


            return null;
        }
    }
}
