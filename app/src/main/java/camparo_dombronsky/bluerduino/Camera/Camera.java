package camparo_dombronsky.bluerduino.Camera;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Camera_Thread;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Camera extends AppCompatActivity {

    private TextView infoip;
    private Camera_Thread car_thread;
    private SurfaceView frameLayout;

    private boolean isTurnedOn = true;
    private ImageButton prendeApaga;

    private ImageButton statusBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_activity);

        infoip = (TextView) findViewById(R.id.ip_address);
        infoip.setText(getIpAddress());


        statusBtn = (ImageButton) findViewById(R.id.status_btn);
        statusBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                retryConnection();
                return true;
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        prendeApaga = (ImageButton) findViewById(R.id.btn_prende_apaga);
        prendeApaga.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isTurnedOn) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isTurnedOn = false;
                    prendeApaga.setBackground(getResources().getDrawable(R.drawable.light_off));
                    Toast.makeText(getBaseContext(), "La pantalla se apagara pronto", Toast.LENGTH_SHORT).show();
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    isTurnedOn = true;
                    prendeApaga.setBackground(getResources().getDrawable(R.drawable.light_on));
                    Toast.makeText(getBaseContext(), "La pantalla permanecera encendida", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            car_thread = new Camera_Thread(this);

            frameLayout = (SurfaceView) findViewById(R.id.camera_preview);
            // Create our Preview view and set it as the content of our activity.
            frameLayout.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            frameLayout.getHolder().addCallback(car_thread);

            car_thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        car_thread.killJoystick();
        car_thread.closeSockets();
        super.onDestroy();
        System.out.println("Destruyo el thread");

        //car_thread.cancel(true);
        car_thread.interrupt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    car_thread.checkBTState();
                } else {
                    // Acciones adicionales a realizar si el usuario no activa el Bluetooth
                }
                break;
            }
            default:
                break;
        }
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


    private void prendeApagaListener() {
        if (isTurnedOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            isTurnedOn = false;
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            isTurnedOn = true;
        }
    }


    private void retryConnection() {
        try {
            car_thread.checkBTState();
        } catch (Exception e) {

        }
    }

    public void statusButtonStyle(boolean connected){
        if(connected){
            Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

            statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.check));
            statusBtn.setOnTouchListener(null);
        }
        else{
            Toast.makeText(getBaseContext(), "No se pudo establecer conexión BT", Toast.LENGTH_SHORT).show();
            statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.retry));
        }
    }

}