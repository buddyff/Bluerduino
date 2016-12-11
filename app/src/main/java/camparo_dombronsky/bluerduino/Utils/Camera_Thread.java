package camparo_dombronsky.bluerduino.Utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import camparo_dombronsky.bluerduino.Camera.Camera;

public class Camera_Thread extends Thread implements SurfaceHolder.Callback, android.hardware.Camera.PreviewCallback {

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

    private android.hardware.Camera mCamera;
    private int w, h;
    private int[] rgbs;
    private boolean initialed = false;
    private ByteArrayOutputStream bos;
    private YuvImage yuv;

    private Camera carActivity;

    private boolean isConnected,flashing;

   // private static Camera_Thread instance = null;

    /*public static Camera_Thread getInstance(Camera _carActivity) {
        if (instance == null) {
            instance = new Camera_Thread(_carActivity);
        }
        return instance;
    }

    public boolean Instanced() {
        return instance != null;
    }*/

    public Camera_Thread(Camera _carActivity) {
        carActivity = _carActivity;
        isConnected = false;
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
    public void run() {
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

        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!isInterrupted()) {
            try {
                //This block will be executed just the first time to establish the connection
                if (socket == null) {
                    System.out.println("Espero por una conexion");
                    socket = serverSocket.accept();
                    socket.setTcpNoDelay(true);
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    out = socket.getOutputStream();
                    isConnected = true;
                }

                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();
                System.out.println("Me llego instruccion..");
                if (messageFromClient.equals("9999")) {
                    System.out.println("ME LLEGO LA DE CERRAR EL SOCKET");
                    socket = null;
                    isConnected = false;
                }
                else if(messageFromClient.equals("8888")){      //FLASH!
                    if(flashing)
                        mCamera.getParameters().setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
                    else
                        mCamera.getParameters().setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                }
                else {
                    byte[] msgBuffer = messageFromClient.getBytes();
                    if (btOutStream != null) {
                        System.out.println("La mando al Arduino " + messageFromClient);
                        btOutStream.write(msgBuffer);
                        btOutStream.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //System.out.println("Termina DoInBackground");
        //return null;
    }


    public void sendImageData(byte[] data) {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
                out.flush();
                System.out.println("Mande la imagen");
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.interrupt();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            this.interrupt();
        }
    }

    public void killJoystick() {
        try {
            dataOutputStream.writeInt(-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                mCamera = android.hardware.Camera.open(0);
                mCamera.setPreviewCallback(this);
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();

                w = mCamera.getParameters().getPreviewSize().width;
                h = mCamera.getParameters().getPreviewSize().height;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        if (data != null) {
            try {
                bos = new ByteArrayOutputStream();
                if (isConnected) {
                    yuv = new YuvImage(data, camera.getParameters().getPreviewFormat(), w, h, null);
                    yuv.compressToJpeg(new Rect(0, 0, w, h), 40, bos);
                    sendImageData(bos.toByteArray());
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
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




