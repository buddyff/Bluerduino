package camparo_dombronsky.bluerduino.Joystick;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.ClientTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Joystick_Setup extends AppCompatActivity {

    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear, buttonSend;
    ClientTask myClientTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_setup);

        editTextAddress = (EditText)findViewById(R.id.address);
        editTextPort = (EditText)findViewById(R.id.port);
        buttonConnect = (Button)findViewById(R.id.connect);
        buttonClear = (Button)findViewById(R.id.clear);
        buttonSend = (Button)findViewById(R.id.send);

        buttonSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                myClientTask.sendData("Comando guachoooo");
            }
        });

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
    }

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    myClientTask = new ClientTask(
                            "192.168.10.29",
                            7000);
                    myClientTask.execute();
                }};


}
