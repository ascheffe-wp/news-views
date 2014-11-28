package com.schef.rss.android;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by scheffela on 11/28/14.
 */
public class RotateAll implements Runnable {

    public Ars3d ars3d;
    public float degreesToRotate;
    public boolean rotating = true;
    public long cycleTime;
    public Array<ArticleInstance> allPanes;

    public RotateAll(Ars3d ars3d, Array<ArticleInstance> allPanes, float degreesToRotate, long cycleTime) {
        this.ars3d = ars3d;
        this.allPanes = allPanes;
        this.degreesToRotate = degreesToRotate;
        this.cycleTime = cycleTime;
    }

    @Override
    public void run() {
        while (rotating) {
            try {
                Thread.sleep(cycleTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (ArticleInstance ai : allPanes) {
                ai.transform.rotate(0.0f, 600.0f, 0.0f, degreesToRotate);
                ai.center.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
                ai.dimensions.rotate(new Vector3(0.0f, 600.0f, 0.0f), degreesToRotate);
            }
        }
    }
}

