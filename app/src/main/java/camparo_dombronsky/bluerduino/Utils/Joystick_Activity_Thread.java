package camparo_dombronsky.bluerduino.Utils;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import camparo_dombronsky.bluerduino.Utils.Listeners.JoystickTaskListener;


public class Joystick_Activity_Thread extends AsyncTask<Void, Void, Void> {

    private String ip;
    private int port;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private JoystickTaskListener listener;
    private Activity joystick_activity;
    private boolean andando;

    public Joystick_Activity_Thread(String addr, int port, JoystickTaskListener list) {
        ip = addr;
        this.port = port;
        listener = list;
        joystick_activity = (Activity) list;

    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            andando = true;
            System.out.println("EMPEZO EL HILO MAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            listener.onControllerConnected();
            while(andando) {
                int size = dataInputStream.readInt();
                final byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                joystick_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (buffer.length > 20) {
                            if (listener != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                listener.onCameraImageIncoming(bitmap);
                            }

                        }
                    }
                });
            }

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

    public void frenar(){
        andando = false;
    }

    public void sendData (String data){
        try {
            dataOutputStream.writeUTF(data);
            //dataOutputStream.writeInt(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
