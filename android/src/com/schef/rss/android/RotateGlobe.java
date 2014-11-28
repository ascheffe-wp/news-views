package com.schef.rss.android;

import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;

/**
 * Created by scheffela on 11/28/14.
 */
public class RotateGlobe implements Runnable {

    private CameraInputController cameraInputController;
    private float degrees;
    public boolean running = true;

    public RotateGlobe(CameraInputController cameraInputController, float degrees) {
        this.cameraInputController = cameraInputController;
        this.degrees = degrees;
    }

    @Override
    public void run() {
        if (cameraInputController != null && cameraInputController.camera != null && cameraInputController.camera.up != null) {
            while (running) {
                cameraInputController.camera.up.rotate(degrees, 0f, 0f, 1.01f);
                cameraInputController.camera.update();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

