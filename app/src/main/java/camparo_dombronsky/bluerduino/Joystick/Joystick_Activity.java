package camparo_dombronsky.bluerduino.Joystick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    Button A,G;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_activity);

        A = (Button) findViewById(R.id.mandarA);
        //G = (Button) findViewById(R.id.mandarG);

        A.setOnClickListener(mandarAListener);
      //  G.setOnClickListener(mandarGListener);

        cameraImage = (ImageView) findViewById(R.id.iv_camera_image);

        Bundle bundle = getIntent().getExtras();

        joystick_task = new Joystick_Thread((String)bundle.getString("ip"),7000,this);
        joystick_task.execute();
    }

        View.OnClickListener mandarAListener =
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    joystick_task.sendData("A");

            }};

    View.OnClickListener mandarGListener=
            new View.OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    joystick_task.sendData("G");

                }};


    @Override
    public void onControllerConnected() {
        isConnected = true;
    }

    @Override
    public void onCameraImageIncoming(Bitmap bitmap) {
        cameraImage.setImageBitmap(bitmap);
    }
}
