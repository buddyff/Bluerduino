package camparo_dombronsky.bluerduino.Utils;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import camparo_dombronsky.bluerduino.Joystick.Joystick_Activity;
import camparo_dombronsky.bluerduino.Joystick.Joystick_Setup;


public class Joystick_Activity_Thread extends Thread {

    private String ip;
    private int port;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    //private JoystickTaskListener listener;
    private Joystick_Activity joystick_activity;
    private boolean andando;

    public Joystick_Activity_Thread(String addr, int port, Joystick_Activity activity) {
        ip = addr;
        this.port = port;
       //listener = list;
        joystick_activity = activity;

    }

    @Override
    public void run(){
        try {
            System.out.println("EMPEZO EL HILO MAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAN");
            socket = new Socket(ip, port);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
            //listener.onControllerConnected();
            joystick_activity.onControllerConnected();
            while(!isInterrupted()) {
                int size = dataInputStream.readInt();
                if(size < 0){
                    socket.close();
                    joystick_activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(joystick_activity, Joystick_Setup.class);
                            joystick_activity.startActivity(intent);
                            Toast.makeText(joystick_activity.getApplicationContext(), "Se perdio la conexion con Camera", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                }
                final byte[] buffer = new byte[size];
                dataInputStream.readFully(buffer);
                joystick_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (buffer.length > 20) {
                            if (joystick_activity != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                joystick_activity.onCameraImageIncoming(bitmap);
                            }
                        }
                    }
                });
            }
            System.out.println("Muere Joystick Activity Thread");
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

   /* @Override
    protected void onPostExecute(Void result) {
        System.out.println(response);
        super.onPostExecute(result);
    }*/

    public void sendData (String data){
        try {
            dataOutputStream.writeUTF(data);
            //dataOutputStream.writeInt(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
