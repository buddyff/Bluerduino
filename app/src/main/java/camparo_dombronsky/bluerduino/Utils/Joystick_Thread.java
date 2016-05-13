package camparo_dombronsky.bluerduino.Utils;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import camparo_dombronsky.bluerduino.Utils.Listeners.JoystickTaskListener;


public class Joystick_Thread extends AsyncTask<Void, Void, Void> {

    private String ip;
    private int port;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private JoystickTaskListener listener;
    private Activity joystick_activity;

    public Joystick_Thread(String addr, int port, JoystickTaskListener list) {
        ip = addr;
        this.port = port;
        listener = list;
        joystick_activity = (Activity) list;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            listener.onControllerConnected();
            while(true) {
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

    public void sendData (String data){
        try {
            dataOutputStream.writeUTF(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
