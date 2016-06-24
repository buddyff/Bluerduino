package camparo_dombronsky.bluerduino.Joystick;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Joystick_Activity_Thread;
import camparo_dombronsky.bluerduino.Utils.Listeners.JoystickTaskListener;

/**
 * Created by rcamparo on 13/04/2016.
 */
public class Joystick_Activity extends AppCompatActivity implements JoystickTaskListener {
    private Joystick_Activity_Thread joystick_task;
    private boolean isConnected = false;
    private ImageView cameraImage;
    private Button forward,backward;
    private SeekBar direction;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        direction = (SeekBar) findViewById(R.id.direction);

        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

//        direction.

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



}
