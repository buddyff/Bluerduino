package camparo_dombronsky.bluerduino.Utils.Listeners;

import android.graphics.Bitmap;

public interface CameraPreviewListener {
    public void onPreviewTaken(Bitmap bitmap);
    public void onPreviewOutOfMemory(OutOfMemoryError e);
}
