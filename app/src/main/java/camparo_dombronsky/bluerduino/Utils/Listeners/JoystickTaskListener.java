package camparo_dombronsky.bluerduino.Utils.Listeners;

import android.graphics.Bitmap;

public interface JoystickTaskListener {
    public void onControllerConnected();
    public void onCameraImageIncoming(Bitmap bitmap);
    //public void onWrongPassword();
    //public void onControllerDisconnected();
    // public void onControllerClosed();
    //   public void onDataIncoming();
}