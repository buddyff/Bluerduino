package camparo_dombronsky.bluerduino.Utils;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.SurfaceHolder;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Car_Activity_Thread extends AsyncTask<Void, Void, Void> implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final int SocketServerPORT = 7000;
    private ServerSocket serverSocket;

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream;
    private OutputStream out;
    private Socket socket;


    private Camera mCamera;
    private int w, h;
    private int[] rgbs;
    private boolean initialed = false;


    private boolean isConnected;

    private static Car_Activity_Thread instance = null;

    public static Car_Activity_Thread getInstance(BluetoothSocket socket) {
        if (instance == null) {
            instance = new Car_Activity_Thread(socket);
        }
        return instance;
    }

    public boolean Instanced() {
        return instance != null;
    }

    protected Car_Activity_Thread(BluetoothSocket socket) {
        mmSocket = socket;
        isConnected = false;
        try {
            if (mmSocket.isConnected()) {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            }
        } catch (Exception e) {
        }
    }

    public void setBluetoothSocket(BluetoothSocket soc) {
        mmSocket = soc;
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
        }
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Void doInBackground(Void... arg0) {
        try {

            serverSocket = new ServerSocket(SocketServerPORT);
            socket = null;
            String messageFromClient;

            while (true) {

                //This block will be executed just the first time to establish the connection
                if (socket == null) {

                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    out = socket.getOutputStream();
                    isConnected = true;
                }


                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();
                System.out.println("Me llego instruccion..");
                if (messageFromClient.equals("9999")) {
                    System.out.println("ME LLEGO LKA DE CE RRAR EL SOCKET");
                    socket = null;
                    isConnected = false;
                } else {
                    byte[] msgBuffer = messageFromClient.getBytes();
                    if (mmOutStream != null) {
                        System.out.println("La mando al Arduino");
                        mmOutStream.write(msgBuffer);
                        mmOutStream.flush();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Termina DoInBackground");
        return null;
    }


    public void sendImageData(byte[] data) {
        try {
            System.out.println("flag 1 sned data");
            if (dataOutputStream != null) {
                System.out.println("flag 2 sned data");
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
                out.flush();
                System.out.println("flag 3 sned data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
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
                System.out.println("Flag 1");

                if (isConnected()) {
                    //Todo : en vez de 50 hay que poner un selector de calidad de imagen como el de ioio
                    Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.JPEG, 50, bos);
                    System.out.println("Flag 2");
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

}




