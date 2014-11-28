package com.schef.rss.android;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.schef.rss.android.db.ArsEntity;

/**
 * Created by scheffela on 11/28/14.
 */
public class ArticleInstance extends ModelInstance {
    public Vector3 center = new Vector3();
    public Vector3 dimensions = new Vector3();
    public float radius;
    public float row;
    public float col;

    public BoundingBox bounds = new BoundingBox();

    public ArsEntity arsEntity;
    public boolean update;

    public TextSpinner textSpinner;
    public Thread textSpinnerThread;
    public long textSleepTime;
    public long initialPause;

    public boolean flipped = false;
    public boolean video = false;

    public ModelInstance textModel;
    public boolean textureHeadline = false;

    public ArticleInstance(Model model, Vector3 center, Vector3 dimensions, float radius, BoundingBox bounds,
                           float row, float col, long textSleepTime, long initialPause) {
        super(model);
        this.center = center;
        this.dimensions = dimensions;
        this.radius = radius;
        this.bounds = bounds;
        this.row = row;
        this.col = col;
        this.textSleepTime = textSleepTime;
        this.initialPause = initialPause;
    }

    public ArticleInstance(Model model) {
        super(model);
        calculateBoundingBox(bounds);
        center.set(bounds.getCenter());
        dimensions.set(bounds.getDimensions());
        radius = dimensions.len() / 2f;
    }

    public ArticleInstance(Model model, float f1, float f2, float f3) {
        super(model, f1, f2, f3);
        calculateBoundingBox(bounds);
        center.set(bounds.getCenter());
        dimensions.set(bounds.getDimensions());
        radius = dimensions.len() / 2f;
    }

    public void startSpinner() {
        if (textSpinnerThread == null) {
            textSpinner = new TextSpinner(this, textSleepTime, initialPause);
            textSpinnerThread = new Thread(textSpinner);
            textSpinnerThread.start();
        }

    }

    public void stopSpinner() {
        if (textSpinner != null) {
            textSpinner.stopSpinner();
            textSpinnerThread = null;
            textSpinner = null;
        }
    }
}

