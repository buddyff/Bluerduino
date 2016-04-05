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

public class SocketServerThread extends Thread {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;
    DataInputStream dataInputStream = null;


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            Socket socket = null;

            while (true) {
                //Just for create the socket
                if (socket == null)
                    socket = serverSocket.accept();

                dataInputStream = new DataInputStream(socket.getInputStream());

                String messageFromClient = "";

                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readUTF();

                System.out.println(messageFromClient);

              /*  int bytesRead;
                byte[] buffer = new byte[1024];
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

                /*SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                        socket, count);
                socketServerReplyThread.run();*/

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
/*
class SocketServerReplyThread extends Thread {

    private Socket hostThreadSocket;
    int cnt;

    SocketServerReplyThread(Socket socket, int c) {
        hostThreadSocket = socket;
        cnt = c;
    }

    @Override
    public void run() {
        OutputStream outputStream;
        String msgReply = "Hello from Android, you are #" + cnt;

        try {
            outputStream = hostThreadSocket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(msgReply);
            printStream.close();

            message += "replayed: " + msgReply + "\n";

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                }
            });

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            message += "Something wrong! " + e.toString() + "\n";
        }

        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                msg.setText(message);
            }
        });
    }
}*/



