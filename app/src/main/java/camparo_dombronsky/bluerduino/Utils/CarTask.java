package camparo_dombronsky.bluerduino.Utils;

import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Listeners.CarTaskListener;

public class CarTask extends Thread {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;
    CarTaskListener listener;
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream;
    OutputStream out;

    Connect2Arduino btConnection;

    public CarTask(Connect2Arduino bt,CarTaskListener ctl){
        btConnection = bt;
        listener = ctl;
    }

    public CarTask(CarTaskListener ctl){
        listener = ctl;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            Socket socket = null;
            String messageFromClient = "";
            while (true) {
                //This block will be executed just the first time to establish the connection
                if (socket == null) {
                    socket = serverSocket.accept();
                    if (socket.isConnected()) listener.onControllerConnected();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    out = socket.getOutputStream();
                }
                //System.out.println("itero");
                //

                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();

                //btConnection.sendData(messageFromClient);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendImageData(byte[] data) {
        try {
            dataOutputStream.writeInt(data.length);
            dataOutputStream.write(data);
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}




