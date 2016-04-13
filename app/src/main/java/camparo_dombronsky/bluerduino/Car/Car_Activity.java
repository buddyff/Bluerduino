package camparo_dombronsky.bluerduino.Car;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Listeners.CameraPreviewListener;
import camparo_dombronsky.bluerduino.Utils.Connect2Arduino;
import camparo_dombronsky.bluerduino.Utils.CarTask;
import camparo_dombronsky.bluerduino.Utils.CameraPreview;
import camparo_dombronsky.bluerduino.Utils.Listeners.CarTaskListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

public class Car_Activity extends AppCompatActivity implements SurfaceHolder.Callback,CameraPreviewListener,CarTaskListener {

    private TextView info, infoip, msg;
    private String message = "";
    //private ServerSocket serverSocket;
    private CarTask car_task;
    private Connect2Arduino connect2arduino;
    private BluetoothAdapter btAdapter;
    private SurfaceView frameLayout;
    private Camera mCamera;
    private CameraPreview mPreview;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_activity);

        infoip = (TextView) findViewById(R.id.ip_address);
        infoip.setText(getIpAddress());

        frameLayout= (SurfaceView) findViewById(R.id.camera_preview);

        infoip.setText(getIpAddress());



        // Create our Preview view and set it as the content of our activity.
        frameLayout.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        frameLayout.getHolder().addCallback(this);

        mPreview = new CameraPreview(this);

    }

    @Override
    protected void onResume(){
         super.onResume();
      //  try {
            //It is best to check BT status at onResume in case something has changed while app was paused etc
           // if (checkBTState()) {
            //    connect2arduino = new Connect2Arduino(btAdapter, this);

             //   if (connect2arduino.getSocket() != null) {
               //     connect2arduino.start();
                //    if (connect2arduino.getSocket().isConnected()) {
                  //      Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();
                       // car_task = new CarTask(connect2arduino,this);
                        car_task = new CarTask(this);
                        car_task.start();
                   // }
             //   }
           // }
    //    }
      /*  catch (IOException e){
            Toast.makeText(getBaseContext(), "No se pudo establecer conexión Bluetooth", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*if (CarTask.getSocket() != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/
    }

    //method to check if the device has Bluetooth and if it is on.
    //Prompts the user to turn it on if it is off
    private boolean checkBTState() {
        // Check if the device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no tiene Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                return false;
            }
        }
        return true;
    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPreview.createCameraInstance(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    }


    @Override
    public void onPreviewTaken(Bitmap bitmap) {
        if (isConnected) {
            System.out.println("flag 1");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            System.out.println("flag 2");
            //Todo : en vez de 100 hay que poner un selector de calidad de imagen como el de ioio
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
            System.out.println("flag 3");
            car_task.sendImageData(bos.toByteArray());
            System.out.println("Mande imagenes");
        }
    }

    @Override
    public void onPreviewOutOfMemory(OutOfMemoryError e) {

    }

    @Override
    public void onControllerConnected() {
        isConnected = true;
    }
}