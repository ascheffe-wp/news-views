package com.schef.rss.android;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;

/**
 * Created by scheffela on 11/27/14.
 */
public class VideoTextureData implements TextureData {
    @Override
    public TextureDataType getType() {
        return TextureDataType.Custom;
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @Override
    public void prepare() {

    }

    @Override
    public Pixmap consumePixmap() {
        return null;
    }

    @Override
    public boolean disposePixmap() {
        return false;
    }

    @Override
    public void consumeCustomData(int target) {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public Pixmap.Format getFormat() {
        return null;
    }

    @Override
    public boolean useMipMaps() {
        return false;
    }

    @Override
    public boolean isManaged() {
        return false;
    }
}
