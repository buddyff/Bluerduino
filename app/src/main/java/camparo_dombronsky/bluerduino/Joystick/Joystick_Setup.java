package camparo_dombronsky.bluerduino.Joystick;

import camparo_dombronsky.bluerduino.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Joystick_Setup extends AppCompatActivity {

    EditText ip;
    Button buttonConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_setup);

        ip = (EditText)findViewById(R.id.ip);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
    }

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(Joystick_Setup.this, Joystick_Activity.class);
                    intent.putExtra("ip",ip.getText().toString());
                    startActivity(intent);
                }};


}
