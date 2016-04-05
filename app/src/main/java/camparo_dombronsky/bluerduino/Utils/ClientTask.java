package camparo_dombronsky.bluerduino.Utils;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;


public class ClientTask extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    Socket socket;
    DataOutputStream dataOutputStream;

    public ClientTask(String addr, int port) {
        dstAddress = addr;
        dstPort = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            socket = new Socket(dstAddress, dstPort);
            System.out.println("Se conecto");
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            dataOutputStream.writeUTF("EEEESA GUACHO");





        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("UnknownHostException");
            //response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("IOException");
            //response = "IOException: " + e.toString();
        } /*finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }*/
        return null;
    }

   /* @Override
    protected void onPostExecute(Void result) {
        System.out.println(response);
        super.onPostExecute(result);
    }*/

    public void sendData (String data){
        try {
            dataOutputStream.writeUTF("ARSENAL F.C");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        String data;

        SocketServerReplyThread(Socket socket, String data) {
            hostThreadSocket = socket;
            this.data = data;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            String msgReply = "Hello from Android : " + data;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();



            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
