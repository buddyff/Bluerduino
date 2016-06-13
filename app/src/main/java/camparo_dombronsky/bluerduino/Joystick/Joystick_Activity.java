package camparo_dombronsky.bluerduino.Joystick;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Joystick_Activity_Thread;
import camparo_dombronsky.bluerduino.Utils.Listeners.JoystickTaskListener;

/**
 * Created by rcamparo on 13/04/2016.
 */
public class Joystick_Activity extends AppCompatActivity implements JoystickTaskListener, JoyStickManager.JoyStickEventListener {
    private Joystick_Activity_Thread joystick_task;
    private boolean isConnected = false;
    private ImageView cameraImage;
    private RelativeLayout joystick;
    private JoyStickManager joystickManager;
    private Button left,right,forward,backward;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        /*left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);*/

      /*  joystick = (RelativeLayout) findViewById(R.id.joystick);
        joystickManager = new JoyStickManager(this, joystick, getWindowManager().getDefaultDisplay().getHeight());
        joystickManager.setJoyStickEventListener(this);*/
        /*  0 : DETENERSE
            1 : ADELANTE
            2 : ATRAS
            3 : DERECHA
            4 : IZQUIERDA */
        /*forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                if (action == MotionEvent.ACTION_DOWN)
                    joystick_task.sendData("A");
                else
                if (action == MotionEvent.ACTION_UP)
                    joystick_task.sendData("G");

                return true;
            }
        });
        */

        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        Bundle bundle = getIntent().getExtras();

       // joystick_task = new Joystick_Activity_Thread((String)bundle.getString("ip"),7000,this);
       // joystick_task.execute();
    }



    @Override
    public void onControllerConnected() {
        isConnected = true;
    }

    @Override
    public void onCameraImageIncoming(Bitmap bitmap) {
        cameraImage.setImageBitmap(bitmap);
    }

    @Override
    public void onJoyStickUp(int speed) {

    }

    @Override
    public void onJoyStickUpRight(int speed) {

    }

    @Override
    public void onJoyStickUpLeft(int speed) {

    }

    @Override
    public void onJoyStickDown(int speed) {

    }

    @Override
    public void onJoyStickDownRight(int speed) {

    }

    @Override
    public void onJoyStickDownLeft(int speed) {

    }

    @Override
    public void onJoyStickRight(int speed) {

    }

    @Override
    public void onJoyStickLeft(int speed) {

    }

    @Override
    public void onJoyStickNone() {

    }

}
