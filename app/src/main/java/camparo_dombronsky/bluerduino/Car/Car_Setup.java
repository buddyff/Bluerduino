package camparo_dombronsky.bluerduino.Car;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import camparo_dombronsky.bluerduino.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class Car_Setup extends AppCompatActivity {

    // Widgets
    Button start_car;

    // Objects for the Bluetooth Connection
    private  BluetoothAdapter btAdapter;
    private OutputStream outStream;
    private BluetoothDevice btDevice;

    //Thread
    Connect2Arduino  hilo;

    public static String EXTRA_DEVICE_ADDRESS;
    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_setup);

        start_car = (Button) findViewById(R.id.btn_startCar);
        start_car.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Car_Setup.this, Car_Activity.class);
                startActivity(intent);
            }
        });



    }

    @Override
    public void onResume() {
        super.onResume();

        //It is best to check BT status at onResume in case something has changed while app was paused etc
        checkBTState();



        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Look for the Arduino Bluetooth Adapter
        if (pairedDevices.size() > 0) {
            boolean arduino_founded = false;
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(ARDUINO_MAC)) {
                    //connect2arduino(device.getAddress());
                    System.out.println(device.getAddress());
                    hilo = new Connect2Arduino(device);
                    hilo.start();
                    arduino_founded = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("PAUSA");
        //Close BT socket to device
        try {
            if (hilo != null ) {
                if (hilo.getSocket() != null) {
                    System.out.println("CIERRO SOCKET DE PAUSA");
                    hilo.getSocket().close();
                }
            }
        } catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo cerrar el Socket Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("STOP");
        //Close BT socket to device
        try {
            if (hilo != null) {
                if (hilo.getSocket() != null) {
                    System.out.println("CIERRO SOCKET DE STOP");
                    hilo.getSocket().close();
                }
            }
        }catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo cerrar el Socket Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("DESTROY");
        //Close BT socket to device
        try {
            if (hilo != null ) {
                if (hilo.getSocket() != null) {
                    System.out.println("CIERRO SOCKET DE DESTROY");
                    hilo.getSocket().close();
                }
            }
        }
        catch (IOException e2) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo cerrar el Socket Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private void checkBTState() {

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
            }
        }
    }


    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            Toast.makeText(getBaseContext(), "ERROR - Dispositivo no encontrado", Toast.LENGTH_SHORT).show();
            // finish();
        }
    }


    class Connect2Arduino extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public Connect2Arduino(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            this.device = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {

                tmp = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
            } catch (IOException e) { }
            socket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) { }
                return;
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                System.out.println("fin hilo");
                socket.close();
            } catch (IOException e) { }
        }

        public BluetoothSocket getSocket(){
            return socket;
        }
    }
}


