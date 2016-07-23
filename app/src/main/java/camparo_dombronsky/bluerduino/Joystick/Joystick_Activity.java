package camparo_dombronsky.bluerduino.Joystick;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
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
    private Button forward, backward;
    private SeekBar direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        direction = (SeekBar) findViewById(R.id.direction);

        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        Bundle bundle = getIntent().getExtras();

        joystick_task = new Joystick_Activity_Thread((String)bundle.getString("ip"),7000,this);
        joystick_task.execute();


        //Forward Listener
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        joystick_task.sendData("1000");
                        break;
                    case MotionEvent.ACTION_UP:
                        joystick_task.sendData("5000");
                        break;
                }
                return true;
            }
        });

        //Backward Listener
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        joystick_task.sendData("2000");
                        break;
                    case MotionEvent.ACTION_UP:
                        joystick_task.sendData("5000");
                        break;
                }
                return true;
            }
        });

        //Direction Listener
        direction.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int aux;
                if (progress > 255) {
                    aux = 3000 + progress - 255;
                    joystick_task.sendData(Integer.toString(aux));
                } else if (progress < 255) {
                    aux = 4255 - progress;
                    joystick_task.sendData(Integer.toString(aux));
                } else
                    joystick_task.sendData("3000");
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
