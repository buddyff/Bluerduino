package camparo_dombronsky.bluerduino.Car;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Listeners.CameraPreviewListener;
import camparo_dombronsky.bluerduino.Utils.Car_Activity_Thread;
import camparo_dombronsky.bluerduino.Utils.CameraPreview;
import camparo_dombronsky.bluerduino.Utils.Listeners.CarTaskListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

public class Car_Activity extends AppCompatActivity implements SurfaceHolder.Callback,CameraPreviewListener,CarTaskListener {

    private TextView info, infoip, msg;
    private String message = "";
    //private ServerSocket serverSocket;
    private Car_Activity_Thread car_thread;
    private BluetoothAdapter btAdapter;
    private SurfaceView frameLayout;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isConnected = false;

    //ATRIBUTOS DE CONNECTION2ARDUINO
    private BluetoothSocket btSocket;
    private BluetoothDevice device;
    private OutputStream outStream;
    private Activity callerActivity;

    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_activity);

        infoip = (TextView) findViewById(R.id.ip_address);
        infoip.setText(getIpAddress());

        frameLayout= (SurfaceView) findViewById(R.id.camera_preview);

        infoip.setText(getIpAddress());

        // Create our Preview view and set it as the content of our activity.
        frameLayout.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        frameLayout.getHolder().addCallback(this);

        mPreview = new CameraPreview(this);

        //-------------------------------------------
        //It is best to check BT status at onResume in case something has changed while app was paused etc
        if(checkBTState()) {

            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

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
                try {
                    btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else {
                Toast.makeText(callerActivity, "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume(){
         super.onResume();
        try {
                if (btSocket != null) {
                    btAdapter.cancelDiscovery();
                    btSocket.connect();
                    outStream = btSocket.getOutputStream();
                    Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

                    car_thread = new Car_Activity_Thread(outStream, this);
                    car_thread.start();
                }
        }
        catch (IOException e){
            Toast.makeText(getBaseContext(), "No se pudo establecer conexión Bluetooth", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*if (Car_Activity_Thread.getSocket() != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/
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

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPreview.createCameraInstance(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    }


    @Override
    public void onPreviewTaken(Bitmap bitmap) {
        if (isConnected) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            //Todo : en vez de 50 hay que poner un selector de calidad de imagen como el de ioio
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);

            car_thread.sendImageData(bos.toByteArray());
        }
    }

    @Override
    public void onPreviewOutOfMemory(OutOfMemoryError e) {

    }

    @Override
    public void onControllerConnected() {
        isConnected = true;
    }
}