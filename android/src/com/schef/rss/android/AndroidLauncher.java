package com.schef.rss.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.splunk.mint.Mint;

import java.io.IOException;
import java.util.ArrayList;

public class AndroidLauncher extends AndroidApplication implements TextureView.SurfaceTextureListener{

    MyCustomAdapter dataAdapter = null;

    private TextureView surface;
    private MediaPlayer player;

    private int surfaceWidth;
    private int surfaceHeight;

    boolean clicked = false;
    Ars3d ars3d;

    @Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Mint.startSession(AndroidLauncher.this);

        // Display the fragment as the main content.
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new UserSettingsFragment())
//                .commit();


        setContentView(R.layout.rootlayout);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useAccelerometer = false;
        cfg.useCompass = false;

        cfg.hideStatusBar = false;

        ars3d = new Ars3d(getApplicationContext(), this);
        View vw = initializeForView(ars3d, cfg);
        FrameLayout rl = (FrameLayout)findViewById(R.id.content_frame);
        rl.addView(vw);

        ImageView iv = (ImageView)findViewById(R.id.handImage);
        iv.setOnClickListener(new HandClickListener(ars3d));

//        ImageView upArrow = (ImageView) findViewById(R.id.upTurnImage);
//        upArrow.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    ars3d.startRotateGlobe(1.01f);
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ars3d.stopRotateGlobe();
//                }
//
//                return true;
//            }
//        });

//        ImageView downArrow = (ImageView) findViewById(R.id.downTurnImage);
//        downArrow.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    ars3d.startRotateGlobe(-1.01f);
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ars3d.stopRotateGlobe();
//                }
//
//                return true;
//            }
//        });


        ArrayList<String> menuItems = new ArrayList<String>();
        menuItems.add("One");
        menuItems.add("Two");
        menuItems.add("Three");


        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this, R.layout.listitem, menuItems);
        ListView listView = (ListView) findViewById(R.id.listView);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                String country = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + country,
                        Toast.LENGTH_LONG).show();
            }
        });

        surface = (TextureView) findViewById(R.id.surface);
        surface.setSurfaceTextureListener(this);
	}

    private void startPlaying()
    {
        player = new MediaPlayer();

        try
        {
            //http://r18---sn-25ge7n7e.googlevideo.com/videoplayback?ipbits=0&expire=1417182828&id=o-AK53dmJngSk0IpcT3R2cCz_y5fmn1BYlsY2ac3CYyAf8&fexp=900243%2C905639%2C907259%2C924639%2C927622%2C932404%2C938652%2C938676%2C939975%2C943909%2C947209%2C948124%2C948703%2C952302%2C952605%2C952901%2C953912%2C957103%2C957105%2C957201&ip=46.23.65.202&ms=au&mv=m&initcwndbps=138750&source=youtube&sparams=dur%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Cmm%2Cms%2Cmv%2Csource%2Cupn%2Cexpire&dur=160.194&sver=3&mt=1417161166&itag=22&key=yt5&upn=HAzcTfg_ky4&signature=8103B6AC8284005C901DC69C57CAA3197300B5EA.1710939DC85D17998819E6B94AB545A2350D49DA&mm=31&title=Jurassic+World+-+Official+Trailer+%28HD%29
//            AssetFileDescriptor afd = getAssets().openFd("big_buck_bunny.mp4");
//            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.setDataSource("http://r18---sn-25ge7n7e.googlevideo.com/videoplayback?ipbits=0&expire=1417182828&id=o-AK53dmJngSk0IpcT3R2cCz_y5fmn1BYlsY2ac3CYyAf8&fexp=900243%2C905639%2C907259%2C924639%2C927622%2C932404%2C938652%2C938676%2C939975%2C943909%2C947209%2C948124%2C948703%2C952302%2C952605%2C952901%2C953912%2C957103%2C957105%2C957201&ip=46.23.65.202&ms=au&mv=m&initcwndbps=138750&source=youtube&sparams=dur%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Cmm%2Cms%2Cmv%2Csource%2Cupn%2Cexpire&dur=160.194&sver=3&mt=1417161166&itag=22&key=yt5&upn=HAzcTfg_ky4&signature=8103B6AC8284005C901DC69C57CAA3197300B5EA.1710939DC85D17998819E6B94AB545A2350D49DA&mm=31&title=Jurassic+World+-+Official+Trailer+%28HD%29");
            player.setSurface(new Surface(ars3d.surfaceTexture));
            player.setLooping(true);
            player.prepare();
            player.start();

        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open input video!");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        surfaceWidth = width;
//        surfaceHeight = height;
//        startPlaying();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    public static class HandClickListener implements View.OnClickListener {

         Ars3d ars3d;
         boolean clicked = true;

         public HandClickListener(Ars3d ars3d) {
             this.ars3d = ars3d;
         }

         @Override
         public void onClick(View v) {
             if(clicked) {
                 Drawable drawable = v.getResources().getDrawable(R.drawable.unchecked);
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                     v.setBackground(drawable);
                 }
                 else {
                     v.setBackgroundDrawable(drawable);
                 }
                 ((ImageView)v).setImageDrawable(v.getResources().getDrawable(R.drawable.plane));
                 clicked = false;
                 ars3d.flyMode = false;
                 Toast.makeText(v.getContext(), "Touch Mode Enabled",
                         Toast.LENGTH_SHORT).show();
             } else {
                 Drawable drawable = v.getResources().getDrawable(R.drawable.checked);
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                     v.setBackground(drawable);
                 }
                 else {
                     v.setBackgroundDrawable(drawable);
                 }
                 ((ImageView)v).setImageDrawable(v.getResources().getDrawable(R.drawable.whand));
                 ars3d.flyMode = true;
                 clicked = true;
                 Toast.makeText(v.getContext(), "Flight Mode Enabled",
                         Toast.LENGTH_SHORT).show();
             }
         }
     }

    private class MyCustomAdapter extends ArrayAdapter<String> {

        private ArrayList<String> stringList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<String> countryList) {
            super(context, textViewResourceId, countryList);
            this.stringList = new ArrayList<String>();
            this.stringList.addAll(countryList);
        }

        private class ViewHolder {
            TextView code;
            CheckBox name;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.listitem, null);

                holder = new ViewHolder();
                holder.code = (TextView) convertView.findViewById(R.id.code);
                holder.code.setText(stringList.get(position));
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Mint.closeSession(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Mint.startSession(AndroidLauncher.this);
    }
}
