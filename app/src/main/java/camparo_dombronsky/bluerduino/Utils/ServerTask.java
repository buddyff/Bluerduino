package camparo_dombronsky.bluerduino.Utils;

import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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

public class ServerTask extends Thread {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;
    DataInputStream dataInputStream = null;
    Connect2Arduino btConnection;

    public ServerTask(Connect2Arduino bt){
        btConnection = bt;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            Socket socket = null;

            while (true) {
                //This block will be executed just the first time to establish the connection
                if (socket == null)
                    socket = serverSocket.accept();

                dataInputStream = new DataInputStream(socket.getInputStream());

                String messageFromClient = "";

                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();

                btConnection.sendData(messageFromClient);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}




