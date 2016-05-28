package camparo_dombronsky.bluerduino.Utils;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

import camparo_dombronsky.bluerduino.Car.Car_Setup;
import camparo_dombronsky.bluerduino.R;

public class Connection2Arduino extends Thread {
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BluetoothAdapter adapter;
    private OutputStream outStream;
    private Activity callerActivity;

    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public Connection2Arduino(BluetoothAdapter adap, Activity act) throws IOException {
        adapter = adap;
        callerActivity = act;

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

        // Look for the Arduino Bluetooth Device
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice paired_device : pairedDevices) {
                if (paired_device.getAddress().equals(ARDUINO_MAC)) {
                   device = paired_device;
                    break;
                }
            }
        }
        if (device != null)
            socket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
        else{
            callerActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(callerActivity, "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        adapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
            outStream = socket.getOutputStream();
            callerActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(callerActivity, "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();
                }
                });

        }
        catch (IOException connectException) {
            // Unable to connect; close the socket,toast a message and get out
            try {
                socket.close();
                callerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(callerActivity, "No se pudo establecer conexion con el Arduino", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch (IOException closeException) { }
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

    // Method to send data
    public void sendData(String message) throws IOException {
        byte[] msgBuffer = message.getBytes();
        //attempt to place data on the outstream to the BT device
        outStream.write(msgBuffer);
    }

    public BluetoothSocket getSocket(){return socket;}

    public OutputStream getOutStream(){return outStream;}
}