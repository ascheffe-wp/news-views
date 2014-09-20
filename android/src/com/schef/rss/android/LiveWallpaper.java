package com.schef.rss.android;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidLiveWallpaperService;

/**
 * Created by scheffela on 9/13/14.
 */
public class LiveWallpaper extends AndroidLiveWallpaperService {

    @Override
    public void onCreateApplication () {
        super.onCreateApplication();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        config.getTouchEventsForLiveWallpaper = false;
        Ars3d ars3d = new Ars3d(getApplicationContext(), -7f, 45f);
        ars3d.startAllGlobeRotation();
//        ApplicationListener listener = new MyLiveWallpaperListener();
        initialize(ars3d, config);
        this.linkedEngine.blockAllTouch = true;
    }



    // implement AndroidWallpaperListener additionally to ApplicationListener
//    // if you want to receive callbacks specific to live wallpapers
//    public static class MyLiveWallpaperListener extends MeshShaderTest implements AndroidWallpaperListener {
//
//        @Override
//        public void offsetChange (float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
//                                  int yPixelOffset) {
//            Log.i("LiveWallpaper test", "offsetChange(xOffset:" + xOffset + " yOffset:" + yOffset + " xOffsetSteep:" + xOffsetStep + " yOffsetStep:" + yOffsetStep + " xPixelOffset:" + xPixelOffset + " yPixelOffset:" + yPixelOffset + ")");
//        }
//
//        @Override
//        public void previewStateChange (boolean isPreview) {
//            Log.i("LiveWallpaper test", "previewStateChange(isPreview:"+isPreview+")");
//        }
//    }


//    @Override
//    public ApplicationListener createListener () {
//        return new Ars3d(getApplicationContext(), null);
//    }



//    @Override
//    public AndroidApplicationConfiguration createConfig () {
//        return new AndroidApplicationConfiguration();
//    }
//
//    @Override
//    public void offsetChange (ApplicationListener listener, float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
//                              int xPixelOffset, int yPixelOffset) {
//        Gdx.app.log("LiveWallpaper", "offset changed: " + xOffset + ", " + yOffset);
//    }
}