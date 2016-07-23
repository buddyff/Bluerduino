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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Car_Activity extends AppCompatActivity implements SurfaceHolder.Callback{

    private TextView info, infoip, msg;
    private String message = "";

    //private ServerSocket serverSocket;
    private Car_Activity_Thread car_thread;
    private BluetoothAdapter btAdapter;
    private SurfaceView frameLayout;
    //private CameraPreview mPreview;
    private boolean isConnected = false;

    //ATRIBUTOS DE CONNECTION2ARDUINO
    private BluetoothSocket btSocket;
    private BluetoothDevice device;
    //private OutputStream outStream;
    boolean isTurnedOn = true;
    ImageButton prendeApaga;

    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_activity);

        infoip = (TextView) findViewById(R.id.ip_address);
        infoip.setText(getIpAddress());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        frameLayout = (SurfaceView) findViewById(R.id.camera_preview);


        // Create our Preview view and set it as the content of our activity.
        frameLayout.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        frameLayout.getHolder().addCallback(this);

        prendeApaga = (ImageButton) findViewById(R.id.btn_prende_apaga);
        prendeApaga.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isTurnedOn){
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isTurnedOn = false;
                    //prendeApaga.setBackground();
                    Toast.makeText(getBaseContext(), "La pantalla se apagara pronto", Toast.LENGTH_SHORT).show();
                }
                else{
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isTurnedOn = true;
                    Toast.makeText(getBaseContext(), "La pantalla permanecera encendida", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //mPreview = new CameraPreview(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            //It is best to check BT status at onResume in case something has changed while app was paused etc
            if (checkBTState()) {
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
                        //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device
                        btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
                        System.out.println("Creo el socket bluetooth");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                else {
                    Toast.makeText(getBaseContext(), "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
                }

                //------------------------------------------------------------------------------
            }
            System.out.println("Socket conectado : "+ btSocket.toString());
            if (btSocket != null && !btSocket.isConnected()) {
                try {
                    System.out.println("Voy a conectar el bluetooth");
                    btAdapter.cancelDiscovery();
                    btSocket.connect();
                    System.out.println("Conecto el bluetooth");
                    //outStream = btSocket.getOutputStream();
                    Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

                    if (car_thread == null) {
                        car_thread = new Car_Activity_Thread(btSocket);
                        car_thread.execute();
                    } else
                        car_thread.setBluetoothSocket(btSocket);
                }
                catch (IOException e){e.printStackTrace();}
            }

            if(car_thread == null) {
                System.out.println("Creo el thread con socket null "+ btSocket);
                car_thread = new Car_Activity_Thread(btSocket);
                car_thread.execute();
            }

        } catch (Exception e) {
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
        car_thread.createCameraInstance(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    }

    private void prendeApagaListener(){
        if(isTurnedOn){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            isTurnedOn = false;
        }
        else{
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isTurnedOn = true;
        }
    }

}