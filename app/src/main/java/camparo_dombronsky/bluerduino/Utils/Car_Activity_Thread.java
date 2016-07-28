package camparo_dombronsky.bluerduino.Utils;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Car_Activity_Thread extends AsyncTask<Void, Void, Void> {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream;
    OutputStream out;
    Socket socket;


    private boolean isConnected;


    public Car_Activity_Thread(BluetoothSocket socket) {
        mmSocket = socket;
        isConnected = false;
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
                if (messageFromClient.equals("9999")){
                    System.out.println("ME LLEGO LKA DE CE RRAR EL SOCKET");
                    socket=null;
                    isConnected = false;
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

    public boolean isConnected(){
        return isConnected;
    }


    public void setBtOut(OutputStream btOut) {

    }
}




