package camparo_dombronsky.bluerduino.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import camparo_dombronsky.bluerduino.Car.Car_Activity;
import camparo_dombronsky.bluerduino.Joystick.Joystick_Setup;
import camparo_dombronsky.bluerduino.R;

public class Car_Activity_Thread extends AsyncTask<Void, Void, Void> implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final int SocketServerPORT = 7000;
    private ServerSocket serverSocket;

    private BluetoothSocket btSocket;
    //private InputStream btInStream;
    private OutputStream btOutStream;

    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream;
    private OutputStream out;
    private Socket socket;

    //ATRIBUTOS DE CONNECTION2ARDUINO
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;


    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private Camera mCamera;
    private int w, h;
    private int[] rgbs;
    private boolean initialed = false;

    private Car_Activity carActivity;

    private boolean isConnected;

   // private static Car_Activity_Thread instance = null;

    /*public static Car_Activity_Thread getInstance(Car_Activity _carActivity) {
        if (instance == null) {
            instance = new Car_Activity_Thread(_carActivity);
        }
        return instance;
    }

    public boolean Instanced() {
        return instance != null;
    }*/

    public Car_Activity_Thread(Car_Activity _carActivity) {
        carActivity = _carActivity;
    }

    public void closeSockets() {
        try {
            if (btSocket != null) btSocket.close();
            if (btOutStream != null) btOutStream.close();
            if (serverSocket != null) serverSocket.close();
            if (socket != null) socket.close();
            dataInputStream = null;
            dataOutputStream = null;
            // btInStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void doInBackground(Void... arg0) {
        String messageFromClient;

        //Try to establish connection via BT
        new Thread() {
            @Override
            public void run() {
                checkBTState();
            }
        }.start();


        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            socket = null;

            //This block will be executed just the first time to establish the connection
            if (socket == null) {
                socket = serverSocket.accept();
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                out = socket.getOutputStream();
                isConnected = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!isCancelled()) {
            try {
                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();
                System.out.println("Me llego instruccion..");
                if (messageFromClient.equals("9999")) {
                    System.out.println("ME LLEGO LKA DE CE RRAR EL SOCKET");
                    socket = null;
                    isConnected = false;
                } else {
                    byte[] msgBuffer = messageFromClient.getBytes();
                    if (btOutStream != null) {
                        System.out.println("La mando al Arduino");
                        btOutStream.write(msgBuffer);
                        btOutStream.flush();
                    }
                }
            } catch (Exception e) {
            }
        }

        System.out.println("Termina DoInBackground");
        return null;
    }


    public void sendImageData(byte[] data) {
        try {
            System.out.println("flag n1 sned data");
            if (dataOutputStream != null) {
                System.out.println("flag n2 sned data");
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
                out.flush();
                System.out.println("flag n3 sned data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void killJoystick() {
        try {
            dataOutputStream.writeInt(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
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
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

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
                System.out.println("Flag n1");

                if (isConnected()) {
                    //Todo : en vez de 50 hay que poner un selector de calidad de imagen como el de ioio
                    Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 50, bos);
                    System.out.println("Flag n2");
                    sendImageData(bos.toByteArray());
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

    public void connectBluetooth(BluetoothDevice device) {

        //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device
        try {
            btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Creo el socket bluetooth");


        //System.out.println("Socket conectado : " + btSocket.toString());
        if (btSocket != null && !btSocket.isConnected()) {
            try {
                System.out.println("Voy a conectar el bluetooth");
                btAdapter.cancelDiscovery();
                btSocket.connect();
                //btInStream = btSocket.getInputStream();
                btOutStream = btSocket.getOutputStream();
                System.out.println("Conecto el bluetooth");
                //outStream = btSocket.getOutputStream();

                carActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        carActivity.statusButtonStyle(true);
                    }
                });

            } catch (IOException e) {
                carActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        carActivity.statusButtonStyle(false);
                    }
                });
                e.printStackTrace();
            }
        }
    }

    //method to check if the device has Bluetooth and if it is on.
    //Also checks that the device is paired
    //Prompts the user to turn it on if it is off
    //Waits for the result of the prompt to establish bt connection
    public boolean checkBTState() {
        // Check if the device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(carActivity.getBaseContext(), "El dispositivo no tiene Bluetooth", Toast.LENGTH_SHORT).show();
            carActivity.finish();
        } else {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                carActivity.startActivityForResult(enableBtIntent, 1);
            } else{
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
                    connectBluetooth(device);
                else
                    carActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(carActivity.getBaseContext(), "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
                        }
                    });

            }

        }
        return true;
    }
}




