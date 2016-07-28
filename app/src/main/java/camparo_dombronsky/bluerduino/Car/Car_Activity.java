package camparo_dombronsky.bluerduino.Car;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Car_Activity_Thread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;



import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Car_Activity extends AppCompatActivity implements SurfaceHolder.Callback,Camera.PreviewCallback{

    private TextView  infoip;


    private Car_Activity_Thread car_thread;
    private BluetoothAdapter btAdapter;
    private SurfaceView frameLayout;
    private ImageButton statusBtn;

    //ATRIBUTOS DE CONNECTION2ARDUINO
    private BluetoothSocket btSocket;
    private BluetoothDevice device;
    private boolean isTurnedOn = true;
    private ImageButton prendeApaga;

    private Camera mCamera;
    int w, h;
    int[] rgbs;
    boolean initialed = false;

    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_activity);

        infoip = (TextView) findViewById(R.id.ip_address);
        infoip.setText(getIpAddress());


        statusBtn = (ImageButton) findViewById(R.id.status_btn);
        statusBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                retryConnection();
                return true;
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    System.out.println("ON CREATEEEEEEEEEEEEEEEEEEEEEEEE");
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
                if (device != null){
                        //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device
                        btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
                        System.out.println("Creo el socket bluetooth");
                }

                else {
                    Toast.makeText(getBaseContext(), "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
                }

                System.out.println("Socket conectado : " + btSocket.toString());
                if (btSocket != null && !btSocket.isConnected()) {
                    try {
                        System.out.println("Voy a conectar el bluetooth");
                        btAdapter.cancelDiscovery();
                        btSocket.connect();
                        System.out.println("Conecto el bluetooth");
                        //outStream = btSocket.getOutputStream();
                        Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

                        statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.check));
                        statusBtn.setOnTouchListener(null);

                        if (car_thread == null) {
                            car_thread = new Car_Activity_Thread(btSocket);
                            car_thread.execute();
                        } else
                            car_thread.setBluetoothSocket(btSocket);
                    }
                    catch (IOException e){
                        Toast.makeText(getBaseContext(), "No se pudo establecer conexión BT", Toast.LENGTH_SHORT).show();
                        statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.retry));
                        e.printStackTrace();
                    }
                }
            }

           /* if(car_thread == null) {
                System.out.println("Creo el thread con socket null GUACHOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
                car_thread = new Car_Activity_Thread(btSocket);
                car_thread.execute();
            }
           /* else
                car_thread.setBluetoothSocket(btSocket);*/

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Exception :(", Toast.LENGTH_SHORT).show();
            statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.retry));
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("PAUSEEEEEEE");

    }

    @Override
    protected void onStop(){
        super.onStop();
        System.out.println("STOPEOOOOO");

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
        createCameraInstance(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Release the Camera resource
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
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

    private void createCameraInstance(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                mCamera = Camera.open(0);
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        System.out.println("estoy mostrando cosaas");
        if (!initialed) {
            w = mCamera.getParameters().getPreviewSize().width;
            h = mCamera.getParameters().getPreviewSize().height;
            rgbs = new int[w * h];
            initialed = true;
        }

        if (data != null) {
            try {
                decodeYUV420(rgbs, data, w, h);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                System.out.println("Flag 1");

                if (car_thread !=  null && car_thread.isConnected()) {
                    //Todo : en vez de 50 hay que poner un selector de calidad de imagen como el de ioio
                    Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 50, bos);
                    System.out.println("Flag 2");
                    car_thread.sendImageData(bos.toByteArray());
                }
                //listener.onPreviewTaken(Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888));
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    private void decodeYUV420(int[] rgb, byte[] yuv420, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420[uvp++]) - 128;
                    u = (0xff & yuv420[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    private void retryConnection(){
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
                if (device != null) {
                    //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device
                    btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
                    System.out.println("Creo el socket bluetooth");
                } else {
                    Toast.makeText(getBaseContext(), "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
                }

                System.out.println("Socket conectado : " + btSocket.toString());
                if (btSocket != null && !btSocket.isConnected()) {
                  try {
                    System.out.println("Voy a conectar el bluetooth");
                    btAdapter.cancelDiscovery();
                    btSocket.connect();
                    System.out.println("Conecto el bluetooth");
                    //outStream = btSocket.getOutputStream();
                    Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

                    statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.check));
                    statusBtn.setOnTouchListener(null);
                  }
                  catch (IOException e){
                    Toast.makeText(getBaseContext(), "No se pudo establecer conexión Bluetooth", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                  }
                }

                if (car_thread == null) {
                    car_thread = new Car_Activity_Thread(btSocket);
                    car_thread.execute();
                } /*else
                    car_thread.setBluetoothSocket(btSocket);*/
            }

        }
        catch(IOException e){
            Toast.makeText(getBaseContext(), "No se pudo establecer conexión BT", Toast.LENGTH_SHORT).show();
            statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.retry));
            e.printStackTrace();
        }
    }

}