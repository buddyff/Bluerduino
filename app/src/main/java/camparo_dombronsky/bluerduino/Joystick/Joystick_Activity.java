package camparo_dombronsky.bluerduino.Joystick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Joystick_Thread;
import camparo_dombronsky.bluerduino.Utils.Listeners.JoystickTaskListener;

/**
 * Created by rcamparo on 13/04/2016.
 */
public class Joystick_Activity extends AppCompatActivity implements JoystickTaskListener {

    Joystick_Thread joystick_task;
    boolean isConnected = false;
    ImageView cameraImage;

    Button left,right,forward,backward;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);

        forward.setOnTouchListener(new View.OnTouchListener() {
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


        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        Bundle bundle = getIntent().getExtras();

        joystick_task = new Joystick_Thread((String)bundle.getString("ip"),7000,this);
        joystick_task.execute();
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
