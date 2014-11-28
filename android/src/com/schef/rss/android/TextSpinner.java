package com.schef.rss.android;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;

/**
 * Created by scheffela on 11/28/14.
 */
public class TextSpinner implements Runnable {

    public ArticleInstance ai;
    public static float[] all2 = new float[10000];
    public static float[] all2Back = new float[10000];
    public long sleepTime;
    public long initialPause;
    public boolean running = true;
    public boolean modelSpin = false;

    public TextSpinner(ArticleInstance ai, long sleepTime, long initialPause) {
        this.ai = ai;
        this.sleepTime = sleepTime;
        this.initialPause = initialPause;
    }

    public void stopSpinner() {
        running = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(initialPause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (running) {

            if(modelSpin) {
                if (ai.textModel != null) {
                    if (Ars3d.incenter) {
                        ai.textModel.transform.rotate(0.0f, 600.0f, 0.0f, 0.5f);
                    } else {
                        ai.textModel.transform.rotate(0.0f, 600.0f, 0.0f, 0.5f);
                    }
                }
            } else {
                MeshPart meshPart = ai.nodes.get(0).parts.get(1).meshPart;
                Mesh mesh = meshPart.mesh;
                MeshPart meshPartBack = ai.nodes.get(0).parts.get(2).meshPart;
                Mesh meshBack = meshPartBack.mesh;

                int cap2 = mesh.getVerticesBuffer().capacity();
                mesh.getVerticesBuffer().limit(cap2);
                meshBack.getVerticesBuffer().limit(cap2);

                synchronized (all2) {
                    mesh.getVertices(0, cap2, all2);
                    meshBack.getVertices(0, cap2, all2Back);

                    if (Ars3d.incenter) {
                        for (int i = 6; i < cap2; i += 8) {
                            float tmp = all2[i] - .005f;
                            if (tmp < 0.0) {
                                tmp = tmp + 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2[i] = tmp;

                            float tmp2 = all2Back[i] - .005f;
                            if (tmp2 < 0.0) {
                                tmp2 = tmp2 + 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2Back[i] = tmp2;
                        }
                    } else {
                        for (int i = 6; i < cap2; i += 8) {
                            float tmp = all2[i] + .005f;
                            if (tmp > 1.0) {
                                tmp = tmp - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2[i] = tmp;

                            float tmp2 = all2Back[i] + .005f;
                            if (tmp2 > 1.0) {
                                tmp2 = tmp2 - 1.0f; //tmp = 0.0f;//tmp + 1.0f;
                            }
                            all2Back[i] = tmp2;
                        }
                    }
                    mesh.setVertices(all2, 0, cap2);
                    meshBack.setVertices(all2Back, 0, cap2);
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}