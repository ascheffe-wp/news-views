package com.schef.rss.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.schef.rss.android.db.ArsEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by scheffela on 11/1/14.
 */
public class ArsWearListener extends WearableListenerService {

    protected static final String TAG = ArsWearListener.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = true;

    private List<Node> curNode;

    public class LocalBinder extends Binder {
        public ArsWearListener getService() {
            // Return this instance of LocalService so clients can call public methods
            return ArsWearListener.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startGoogleApiClient();
    }

    public void startGoogleApiClient() {
        if (mResolvingError) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                            mResolvingError = false;

                            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                        @Override
                                        public void onResult(NodeApi.GetConnectedNodesResult nodeResult) {
                                            if (nodeResult.getStatus().isSuccess()) {
                                                Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                                        + nodeResult.getStatus());
                                                curNode = nodeResult.getNodes();
                                            }
                                        }
                                    });

                            Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<DataItemBuffer>() {
                                        @Override
                                        public void onResult(DataItemBuffer dataItemBuffer) {
                                            for (DataItem di : dataItemBuffer) {
                                                Wearable.DataApi.deleteDataItems(mGoogleApiClient, di.getUri()).setResultCallback(
                                                        new ResultCallback<DataApi.DeleteDataItemsResult>() {
                                                            @Override
                                                            public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                                                                if (deleteDataItemsResult.getStatus().isSuccess()) {
                                                                    Log.e(TAG, "Failed to connect to Google Api Client with status: "
                                                                            + deleteDataItemsResult.getStatus());
                                                                }
                                                            }
                                                        });
                                                ;
                                            }
                                        }
                                    });


                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                            mResolvingError = true;
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                            mResolvingError = true;
                        }
                    })
                    .addApi(Wearable.API)
                    .build();

            mGoogleApiClient.connect();
            Wearable.DataApi.addListener(mGoogleApiClient, this);
            Wearable.MessageApi.addListener(mGoogleApiClient, this);
            Wearable.NodeApi.addListener(mGoogleApiClient, this);
        }
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();

            if(event.getType() == DataEvent.TYPE_CHANGED) {
                if (uri.getPath().startsWith("/imageRequests")) {
                    try {
                        String tmp = new String(event.getDataItem().getData(), "UTF-8");
                        tmp = tmp.substring(tmp.indexOf("[{"));
                        Gson gson = new Gson();
                        ArrayList<ArsEntity> newFromJson = gson.fromJson(tmp , new TypeToken<ArrayList<ArsEntity>>() {}.getType());

                        for(ArsEntity arsEntity : newFromJson) {
                            try {
                                postAsset(arsEntity.getLocalImgPath());
                            } catch (Exception e) {
                                Log.e(TAG,"Problem Posting Asset",e);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

//        if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
//        }

        // Check to see if the message is to start an activity
        if (messageEvent.getPath().equals("/getLayout")) {
//
            try {

                PutDataMapRequest pdr = PutDataMapRequest.create("/sectionLayout");
                PutDataRequest request = pdr.asPutDataRequest();
                Gson gson = new Gson();
                String json = gson.toJson(NewApplication.getInstance().getItemsTreeSet());
                pdr.getDataMap().putString("layout",json);
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

//                for(ArsEntity arsEntity : NewApplication.getInstance().getItemsTreeSet()) {
//                    try {
//                        postAsset(arsEntity.getLocalImgPath());
//                    } catch (Exception e) {
//                        Log.e(TAG,"Problem Posting Asset",e);
//                    }
//                }

            } catch (Exception e) {
            }

        } else if (messageEvent.getPath().equals("/start")) {

        } else if (messageEvent.getPath().equals("/stop")) {

        }


    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        curNode.add(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        curNode.remove(peer);
    }


    private byte[] readBytes(File file) {
        FileInputStream fin = null;
        byte fileContent[] = null;
        try {
            // create FileInputStream object
            fin = new FileInputStream(file);
            fileContent = new byte[(int) file.length()];
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        return fileContent;
    }

    private void postAsset (String url) throws IOException {
//        Bitmap bitmap = BitmapFactory.decodeFile(url);
//        Asset asset = createAssetFromBitmap(bitmap);
//        PutDataRequest request = PutDataRequest.create(url);
//        request.putAsset(url, asset);
//        Wearable.DataApi.putDataItem(mGoogleApiClient, request);

        Bitmap b = BitmapFactory.decodeFile(url);
        PutDataMapRequest putRequest = PutDataMapRequest.create(url);
        DataMap map = putRequest.getDataMap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 80, stream);
        stream.flush();
        byte[] byteArray = stream.toByteArray();
        Asset asset = Asset.createFromBytes(byteArray);
        map.putAsset("profileImage", asset);
        map.putLong("dataSize", byteArray.length);

        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());

    }


    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}
