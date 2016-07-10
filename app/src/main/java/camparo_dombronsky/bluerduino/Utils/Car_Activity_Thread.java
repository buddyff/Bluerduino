package camparo_dombronsky.bluerduino.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import camparo_dombronsky.bluerduino.Utils.Listeners.CarTaskListener;

public class Car_Activity_Thread extends Thread {

    static final int SocketServerPORT = 7000;
    ServerSocket serverSocket;
    CarTaskListener listener;
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream;
    OutputStream out,btOut;


    public Car_Activity_Thread(OutputStream out, CarTaskListener ctl){
        btOut = out;
        listener = ctl;
    }

    public Car_Activity_Thread(CarTaskListener ctl){
        listener = ctl;
    }


    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(SocketServerPORT);
            Socket socket = null;
            int messageFromClient;

            while (true) {

                //This block will be executed just the first time to establish the connection
                if (socket == null) {
                    socket = serverSocket.accept();
                    if (socket.isConnected()) listener.onControllerConnected();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    out = socket.getOutputStream();
                }

                //If no message sent from client, this code will block the program
                messageFromClient = dataInputStream.readInt();
                //byte[] msgBuffer = messageFromClient.getBytes();
                btOut.write(messageFromClient);

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




