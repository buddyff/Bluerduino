package camparo_dombronsky.bluerduino.Utils;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;


public class ClientTask extends AsyncTask<Void, Void, Void> {

    private String ip;
    private int port;
    private Socket socket;
    private DataOutputStream dataOutputStream;

    public ClientTask(String addr, int port) {
        ip = addr;
        this.port = port;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

   /* @Override
    protected void onPostExecute(Void result) {
        System.out.println(response);
        super.onPostExecute(result);
    }*/

    public void sendData (String data){
        try {
            dataOutputStream.writeUTF(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
