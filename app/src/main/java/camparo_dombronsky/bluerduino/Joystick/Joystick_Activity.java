package camparo_dombronsky.bluerduino.Joystick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Joystick_Activity_Thread;

/**
 * Created by rcamparo on 13/04/2016.
 */
public class Joystick_Activity extends AppCompatActivity {
    private Joystick_Activity_Thread joystick_task;
    private boolean isConnected = false;
    private ImageView cameraImage;
    private Button forward, backward;
    private SeekBar direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        forward = (Button) findViewById(R.id.forward);
        backward = (Button) findViewById(R.id.backward);
        direction = (SeekBar) findViewById(R.id.direction);

        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        Bundle bundle = getIntent().getExtras();

        joystick_task = new Joystick_Activity_Thread((String) bundle.getString("ip"), 7000, this);

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

                    Matrix matrix = new Matrix();
                    float degrees = 90.0f / 255.0f * (progress - 255);
                    System.out.println("voy a rotar " +degrees+" grados");
                    matrix.postRotate(degrees);
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
                    Bitmap b = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
                    Drawable d = new BitmapDrawable(getResources(), b);
                    direction.setThumb(d);
                    System.out.println("Se roto la imagen");
                } else if (progress < 255) {
                    aux = 4255 - progress;
                    joystick_task.sendData(Integer.toString(aux));

                    Matrix matrix = new Matrix();
                    float degrees = 360.0f - (90.0f / 255.0f * (255.0f - progress));
                    System.out.println("voy a rotar " +degrees+" grados");
                    matrix.postRotate(degrees);
                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
                    Bitmap b = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, true);
                    Drawable d = new BitmapDrawable(getResources(), b);
                    direction.setThumb(d);
                } else {
                    joystick_task.sendData("3000");

                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.wheel);
                    Drawable d = new BitmapDrawable(getResources(), icon);
                    direction.setThumb(d);
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                joystick_task.sendData("3000");
                direction.setProgress(255);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        //Para avisar que se desconecto
        joystick_task.sendData("9999");
        System.out.println("Se ejecuta el onStop");
        joystick_task.frenar();
    }

    @Override
    public void onResume() {
        super.onResume();
        joystick_task.execute();
    }


    public void onControllerConnected() {
        isConnected = true;
    }



    public void onCameraImageIncoming(Bitmap bitmap) {
        cameraImage.setImageBitmap(bitmap);
    }


}
