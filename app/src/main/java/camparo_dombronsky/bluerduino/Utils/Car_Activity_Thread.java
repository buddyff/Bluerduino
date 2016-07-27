package camparo_dombronsky.bluerduino.Utils;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import camparo_dombronsky.bluerduino.Utils.Listeners.CarTaskListener;

public class Car_Activity_Thread extends AsyncTask<Void, Void, Void> {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;
    //CarTaskListener listener;

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream;
    OutputStream out;
    //private OutputStream btOut;

    private boolean isConnected = false;


    public Car_Activity_Thread(BluetoothSocket socket) {
        mmSocket = socket;
        try {
            if (mmSocket.isConnected()) {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();
            }
        } catch (Exception e) { }
    }

    public void setBluetoothSocket(BluetoothSocket soc) {
        mmSocket = soc;
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        }
        catch (IOException e){}
    }



    @Override
    public Void doInBackground(Void... arg0) {
        try {

            serverSocket = new ServerSocket(SocketServerPORT);
            Socket socket = null;
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
                if (messageFromClient.equals("9999")){
                    socket=null;
                }
                else {
                    byte[] msgBuffer = messageFromClient.getBytes();
                    if (mmOutStream != null) {
                        mmOutStream.write(msgBuffer);
                        mmOutStream.flush();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void sendImageData(byte[] data) {
        try {
            if (dataOutputStream != null) {
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected(){
        return isConnected;
    }


    public void setBtOut(OutputStream btOut) {

    }
}




