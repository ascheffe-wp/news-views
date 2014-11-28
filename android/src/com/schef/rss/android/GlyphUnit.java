package com.schef.rss.android;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

import java.util.ArrayDeque;

/**
 * Created by scheffela on 11/28/14.
 */
public class GlyphUnit {
    public ArrayDeque<BitmapFont.Glyph> glyphs = new ArrayDeque<BitmapFont.Glyph>();
    public int totalWidth = 0;

}
