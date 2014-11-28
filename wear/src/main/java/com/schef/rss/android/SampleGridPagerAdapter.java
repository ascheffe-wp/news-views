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
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;

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
    private List<Node> curNode;

    public SampleGridPagerAdapter(Context ctx, FragmentManager fm, List<ArsEntity> fromJson,
                                  GoogleApiClient mGoogleApiClient, List<Node> curNode) {
        super(fm);
        this.mContext = ctx;
        this.fromJson = fromJson;
        this.mGoogleApiClient = mGoogleApiClient;
        this.curNode = curNode;
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
//            {
//                    new Page(R.string.welcome_title, R.string.welcome_text, R.drawable.bugdroid,
//                            Gravity.CENTER_VERTICAL),
//            },
//            {
//                    new Page(R.string.about_title, R.string.about_text, false),
//            },
//            {
//                    new Page(R.string.cards_title, R.string.cards_text, true, 2),
//                    new Page(R.string.expansion_title, R.string.expansion_text, true, 10),
//            },
//            {
//                    new Page(R.string.backgrounds_title, R.string.backgrounds_text, true, 2),
//                    new Page(R.string.columns_title, R.string.columns_text, true, 2)
//            },
//            {
//                    new Page(R.string.dismiss_title, R.string.dismiss_text, R.drawable.bugdroid,
//                            Gravity.CENTER_VERTICAL),
//            },

    };

    @Override
    public Fragment getFragment(int row, int col) {
//        Page page = PAGES[row][col];
//        String title = page.titleRes != 0 ? mContext.getString(page.titleRes) : null;
//        String text = page.textRes != 0 ? mContext.getString(page.textRes) : null;
//        CardFragment fragment;
//        if(fromJson.isEmpty()) {
//            fragment = CardFragment.create("test", "test", R.drawable.card_background);
//        } else {
//
//            fragment = CardFragment.create(arsEntity.getTitle(), arsEntity.getTitle(), R.drawable.card_background);
//        }
//        // Advanced settings
//        fragment.setCardGravity(Gravity.BOTTOM);
//        fragment.setExpansionEnabled(true);
//        fragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
//        fragment.setExpansionFactor(1.0f);
//
//        return fragment;

        if(!fromJson.isEmpty()) {
            ArsEntity arsEntity = fromJson.get(col);
            NotificationPresetFragment fragment = NotificationPresetFragment.newInstance(
                    mGoogleApiClient, curNode, arsEntity);
            return fragment;
        } else {
            BlankFragment bf = new BlankFragment();
            return bf;
//            CardFragment cardFragment = CardFragment.create("test", "test", R.drawable.card_background);
//            return cardFragment;
        }

    }

//    @Override
//    public ImageReference getBackground(int row, int column) {
//
//        if(!fromJson.isEmpty()) {
//            ArsEntity arsEntity = fromJson.get(column);
//            File file = new File("/profileImage" + arsEntity.getLocalImgPath());
//            if(file.exists()) {
//                Bitmap bitmap = BitmapFactory.decodeFile("/profileImage" + arsEntity.getLocalImgPath());
//                return ImageReference.forBitmap(bitmap);
//            } else {
//                return ImageReference.forDrawable(R.drawable.card_background);
//            }
//        } else {
//            return ImageReference.forDrawable(R.drawable.card_background);
//        }
//    }



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
        private List<Node> curNode;
//        private Typeface typeface;

        private ImageView stopImageView;
        private ImageView startImageView;
        private ImageView cardBackImageView;
        private ProgressBar progressBar;

        ArsEntity arsEntity;


        public NotificationPresetFragment(GoogleApiClient mGoogleApiClient, List<Node> curNode, ArsEntity arsEntity) {
            super();
            this.mGoogleApiClient = mGoogleApiClient;
            this.curNode = curNode;
            this.arsEntity = arsEntity;
        }

        public static NotificationPresetFragment newInstance(GoogleApiClient mGoogleApiClient, List<Node> curNode, ArsEntity arsEntity) {
            Bundle args = new Bundle();
            NotificationPresetFragment newFragment = new NotificationPresetFragment(mGoogleApiClient, curNode, arsEntity);
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
            String newPath = arsEntity.getLocalImgPath().replaceAll(".*/app_localfiles","");
            File file = new File(view.getContext().getFilesDir(), newPath);
            if(file.exists()) {

//                R.layout.fragment_action

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//                BitmapDrawable ob = new BitmapDrawable(bitmap);
//                view.setBackground(ob);
                cardBackImageView.setImageBitmap(bitmap);
            }

//            view.setOnClickListener(this);
        }


        @Override  // OnClickListener
        public void onClick(View view) {
            int presetIndex = getArguments().getInt(KEY_PRESET_INDEX);
//            ((MainActivity) getActivity()).updateNotification(presetIndex);

            if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                if(playing) {
                    stopImageView.setVisibility(View.INVISIBLE);
                    startImageView.setVisibility(View.VISIBLE);
                    playing = false;
                    if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                        if(curNode != null && !curNode.isEmpty() && arsEntity != null && arsEntity.getLink() != null && !arsEntity.getLink().isEmpty()) {
                            progressBar.setVisibility(View.GONE);
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
                } else {
                    stopImageView.setVisibility(View.VISIBLE);
                    startImageView.setVisibility(View.INVISIBLE);
                    playing = true;
                    if(mGoogleApiClient != null && curNode != null && !curNode.isEmpty()) {
                        if(curNode != null && !curNode.isEmpty() && arsEntity != null && arsEntity.getLink() != null && !arsEntity.getLink().isEmpty()) {
                            progressBar.setVisibility(View.VISIBLE);
                            ProgressTask pt = new ProgressTask(progressBar);
                            pt.execute();
                            Wearable.MessageApi.sendMessage(
                                    mGoogleApiClient, curNode.get(0).getId(), "/start", arsEntity.getLink().getBytes()).setResultCallback(
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


