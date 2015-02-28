/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.schef.rss.android;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.ImageReference;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Constructs fragments as requested by the GridViewPager. For each row a
 * different background is provided.
 */
public class SampleGridPagerAdapter extends FragmentGridPagerAdapter {

    private static final String TAG = "SampleGridPagerAdapter";

    private final Context mContext;

    private List<ArsEntity> fromJson;

    private GoogleApiClient mGoogleApiClient;
//    private List<Node> curNode;

    public SampleGridPagerAdapter(Context ctx, FragmentManager fm, List<ArsEntity> fromJson,
                                  GoogleApiClient mGoogleApiClient) {
        super(fm);
        this.mContext = ctx;
        this.fromJson = fromJson;
        this.mGoogleApiClient = mGoogleApiClient;
    }

//    static final int[] BG_IMAGES = new int[] {
//            R.drawable.debug_background_1,
//            R.drawable.debug_background_2,
//            R.drawable.debug_background_3,
//            R.drawable.debug_background_4,
//            R.drawable.debug_background_5
//    };

    /** A simple container for static data in each page */
    private static class Page {
        int titleRes;
        int textRes;
        int iconRes;
        int cardGravity = Gravity.BOTTOM;
        boolean expansionEnabled = true;
        float expansionFactor = 1.0f;
        int expansionDirection = CardFragment.EXPAND_DOWN;

        public Page(int titleRes, int textRes, boolean expansion) {
            this(titleRes, textRes, 0);
            this.expansionEnabled = expansion;
        }

        public Page(int titleRes, int textRes, boolean expansion, float expansionFactor) {
            this(titleRes, textRes, 0);
            this.expansionEnabled = expansion;
            this.expansionFactor = expansionFactor;
        }

        public Page(int titleRes, int textRes, int iconRes) {
            this.titleRes = titleRes;
            this.textRes = textRes;
            this.iconRes = iconRes;
        }

        public Page(int titleRes, int textRes, int iconRes, int gravity) {
            this.titleRes = titleRes;
            this.textRes = textRes;
            this.iconRes = iconRes;
            this.cardGravity = gravity;
        }
    }

    private final Page[][] PAGES = {
    };

    @Override
    public Fragment getFragment(int row, int col) {

        if(!fromJson.isEmpty()) {
            ArsEntity arsEntity = fromJson.get(col);
            NotificationPresetFragment fragment = NotificationPresetFragment.newInstance(
                    mGoogleApiClient, arsEntity);
            return fragment;
        } else {
            BlankFragment bf = new BlankFragment();
            return bf;
        }

    }



    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int rowNum) {
        if(fromJson.size() == 0) {
            return 1;
        }
        return fromJson.size();
    }

    public static boolean playing;

    @SuppressLint("ValidFragment")
    public static class NotificationPresetFragment extends Fragment
            implements View.OnClickListener {

        private static final String KEY_TITLE = "title";
        private static final String KEY_PRESET_INDEX = "preset_index";

        private GoogleApiClient mGoogleApiClient;
        private boolean mResolvingError = false;
//        private List<Node> curNode;
//        private Typeface typeface;

        private ImageView stopImageView;
        private ImageView startImageView;
        private ImageView cardBackImageView;
        private ProgressBar progressBar;

        ArsEntity arsEntity;


        public NotificationPresetFragment(GoogleApiClient mGoogleApiClient, ArsEntity arsEntity) {
            super();
            this.mGoogleApiClient = mGoogleApiClient;
//            this.curNode = curNode;
            this.arsEntity = arsEntity;
        }

        public static NotificationPresetFragment newInstance(GoogleApiClient mGoogleApiClient, ArsEntity arsEntity) {
            Bundle args = new Bundle();
            NotificationPresetFragment newFragment = new NotificationPresetFragment(mGoogleApiClient, arsEntity);
            newFragment.setArguments(args);
            return newFragment;
        }

        @Override  // Fragment
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_action, container, false);
        }

        @Override  // Fragment
        public void onViewCreated(View view, Bundle savedInstanceState) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            titleView.setText(arsEntity.getTitle());

//            titleView.setTypeface(typeface);
            startImageView = (ImageView) view.findViewById(R.id.button_image);
            startImageView.setOnClickListener(this);
            stopImageView = (ImageView) view.findViewById(R.id.button_image_stop);
            stopImageView.setOnClickListener(this);

            progressBar = (ProgressBar) view.findViewById(R.id.progress_id);


            cardBackImageView = (ImageView) view.findViewById(R.id.cardBackImage);
            if(arsEntity.getLocalImgPath() != null) {
                String newPath = arsEntity.getLocalImgPath().replaceAll(".*/app_localfiles", "");
                File file = new File(view.getContext().getFilesDir(), newPath);
                if (file.exists()) {
                    ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(), cardBackImageView);
                }
            }

//            view.setOnClickListener(this);
        }

        private List<String> getNodes() {
            List<String> results = new ArrayList<String>();
            if(mGoogleApiClient != null) {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                if(nodes != null && nodes.getNodes() != null) {
                    for (Node node : nodes.getNodes()) {
                        results.add(node.getId());
                    }
                }
            }
            return results;
        }


        @Override  // OnClickListener
        public void onClick(View view) {
            int presetIndex = getArguments().getInt(KEY_PRESET_INDEX);
//            ((MainActivity) getActivity()).updateNotification(presetIndex);


            if(mGoogleApiClient != null) {
                if(playing) {
                    stopImageView.setVisibility(View.INVISIBLE);
                    startImageView.setVisibility(View.VISIBLE);
                    playing = false;
                    if(mGoogleApiClient != null && arsEntity != null && arsEntity.getLink() != null && !arsEntity.getLink().isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        StopPlaybackTask spt = new StopPlaybackTask();
                        spt.execute();
//                            Wearable.MessageApi.sendMessage(
//                                    mGoogleApiClient, ids.get(0), "/stop", "2".getBytes()).setResultCallback(
//                                    new ResultCallback<MessageApi.SendMessageResult>() {
//                                        @Override
//                                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
//                                            if (sendMessageResult.getStatus().isSuccess()) {
//                                                Log.e(TAG, "Failed to connect to Google Api Client with status: "
//                                                        + sendMessageResult.getStatus());
//                                            }
//                                        }
//                                    }
//                            );

                    }
                } else {
                    stopImageView.setVisibility(View.VISIBLE);
                    startImageView.setVisibility(View.INVISIBLE);
                    playing = true;
                    if(mGoogleApiClient != null && arsEntity != null && arsEntity.getLink() != null && !arsEntity.getLink().isEmpty()) {
                        progressBar.setVisibility(View.VISIBLE);
                        ProgressTask pt = new ProgressTask(progressBar);
                        pt.execute();
                        StartPlaybackTask spt = new StartPlaybackTask();
                        spt.execute();
//                            Wearable.MessageApi.sendMessage(
//                                    mGoogleApiClient, ids.get(0), "/start", arsEntity.getLink().getBytes()).setResultCallback(
//                                    new ResultCallback<MessageApi.SendMessageResult>() {
//                                        @Override
//                                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
//                                            if (sendMessageResult.getStatus().isSuccess()) {
//                                                Log.e(TAG, "Failed to connect to Google Api Client with status: "
//                                                        + sendMessageResult.getStatus());
//                                            }
//                                        }
//                                    }
//                            );

                    }
                }

            }
        }

        private class StopPlaybackTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                List<String> ids = getNodes();
                if(mGoogleApiClient != null && ids != null && !ids.isEmpty()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, ids.get(0), "/stop", "2".getBytes()).setResultCallback(
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
                return null;
            }
        }

        private class StartPlaybackTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                List<String> ids = getNodes();
                if(mGoogleApiClient != null && ids != null && !ids.isEmpty()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, ids.get(0), "/start", arsEntity.getLink().getBytes()).setResultCallback(
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
                return null;
            }
        }

        private class ProgressTask extends AsyncTask<Void, Void, Void> {
//        private Context context;

            ProgressBar progressBar;

            public ProgressTask (ProgressBar progressBar) {
                this.progressBar = progressBar;
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(progressBar != null) {
                    try {
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

}


