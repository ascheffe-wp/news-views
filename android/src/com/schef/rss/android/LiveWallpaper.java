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
        initialize(ars3d, config);
        this.linkedEngine.blockAllTouch = true;
    }

}