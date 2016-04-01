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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class Car_Setup extends AppCompatActivity {

    // Widgets
    TextView textConnectionStatus;
    Button start_car;

    // Objects for the Bluetooth Connection
    private  BluetoothAdapter btAdapter;
    private OutputStream outStream;
    private BluetoothDevice btDevice;

    //Thread
    ConnectThread  hilo;

    public static String EXTRA_DEVICE_ADDRESS;
    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_setup);

        textConnectionStatus = (TextView) findViewById(R.id.connecting);
        textConnectionStatus.setTextSize(40);

        start_car = (Button) findViewById(R.id.start_car);



    }

    @Override
    public void onResume() {
        super.onResume();

        //It is best to check BT status at onResume in case something has changed while app was paused etc
        checkBTState();

        textConnectionStatus.setText(" "); //makes the textview blank

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Look for the Arduino Bluetooth Adapter
        if (pairedDevices.size() > 0) {
            boolean arduino_founded = false;
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(ARDUINO_MAC)) {
                    //connect2arduino(device.getAddress());
                    System.out.println(device.getAddress());
                    hilo = new ConnectThread(device);
                    hilo.start();
                    arduino_founded = true;
                    break;
                }
            }
            if (!arduino_founded)
                textConnectionStatus.setText("No se encuentra el Arduino");
        } else {
            textConnectionStatus.setText("No hay nada emparejado guacho");
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

    private void connect2arduino(String mac) {

        // Set up a pointer to the Arduino Bluetooth device using its mac address.
        //BluetoothDevice device = btAdapter.getRemoteDevice(mac);



        //Attempt to create a bluetooth socket for comms
      /*  try {
            btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
        } catch (IOException e1) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo crear el Socket Bluetooth", Toast.LENGTH_SHORT).show();
        }

        // Establish the connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();        //If IO exception occurs attempt to close socket
            } catch (IOException e2) {
                Toast.makeText(getBaseContext(), "ERROR - No se pudo cerrar el Socket Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }

        // Create a data stream so we can send data to the device
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "ERROR - No se pudo crear el OutStream", Toast.LENGTH_SHORT).show();
        }
        //When activity is resumed, attempt to send a piece of junk data ('TEST') so that it will fail if not connected
        // On this way the app doesn't have to wait for a user to press button to recognise connection failure
        sendData("TEST");*/
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


    class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
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


