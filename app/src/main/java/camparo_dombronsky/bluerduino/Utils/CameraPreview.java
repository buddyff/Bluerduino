package camparo_dombronsky.bluerduino.Utils;


import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import camparo_dombronsky.bluerduino.Utils.Listeners.CameraPreviewListener;

/** A basic Camera preview class */
public class CameraPreview implements Camera.PreviewCallback{

    private Camera mCamera;
    private CameraPreviewListener listener;
    int w, h;
    int[] rgbs;
    boolean initialed = false;

    public CameraPreview(CameraPreviewListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        System.out.println("estoy mostrando cosaas");
        if (!initialed) {
            w = mCamera.getParameters().getPreviewSize().width;
            h = mCamera.getParameters().getPreviewSize().height;
            rgbs = new int[w * h];
            initialed = true;
        }

        if (data != null) {
            try {
                decodeYUV420(rgbs, data, w, h);
                listener.onPreviewTaken(Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888));
            } catch (OutOfMemoryError e) {
                listener.onPreviewOutOfMemory(e);
            }
        }
    }

    public void createCameraInstance(SurfaceHolder holder) {
        try {
            mCamera = Camera.open(0);
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decodeYUV420(int[] rgb, byte[] yuv420, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420[uvp++]) - 128;
                    u = (0xff & yuv420[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

}