package camparo_dombronsky.bluerduino.Car;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Connect2Arduino;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class Car_Setup extends AppCompatActivity {

    // Widgets
    Button start_car;

    // Objects for the Bluetooth Connection
    private  BluetoothAdapter btAdapter;

    //Thread
    Connect2Arduino connect2arduino;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_setup);

        start_car = (Button) findViewById(R.id.btn_startCar);
        start_car.setEnabled(false);
        start_car.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Car_Setup.this, Car_Activity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
            //It is best to check BT status at onResume in case something has changed while app was paused etc
            if (checkBTState()) {
                connect2arduino = new Connect2Arduino(btAdapter, this);

                if (connect2arduino.getSocket() != null) {
                    connect2arduino.start();
                    if (connect2arduino.getSocket().isConnected())
                        start_car.setEnabled(true);
                }
            }
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(), "No se pudo establecer conexión Bluetooth", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Close BT socket to device
        try {
            if (connect2arduino != null ) {
                if (connect2arduino.getSocket() != null) {
                    connect2arduino.getSocket().close();
                }
            }
        }
        catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo cerrar el Socket Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private boolean checkBTState() {
        // Check if the device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no tiene Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return false;
            }
        }
        return true;
    }



}


