package camparo_dombronsky.bluerduino.Car;

import camparo_dombronsky.bluerduino.R;
import camparo_dombronsky.bluerduino.Utils.Car_Activity_Thread;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

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

public class Car_Activity extends AppCompatActivity {

    private TextView infoip;


    private Car_Activity_Thread car_thread;
    private BluetoothAdapter btAdapter;
    private SurfaceView frameLayout;
    private ImageButton statusBtn;

    //ATRIBUTOS DE CONNECTION2ARDUINO
    private BluetoothSocket btSocket;
    private BluetoothDevice device;
    private boolean isTurnedOn = true;
    private ImageButton prendeApaga;


    private static final String ARDUINO_MAC = "98:D3:35:00:98:52";
    private static final UUID ARDUINO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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
        System.out.println("ON CREATEEEEEEEEEEEEEEEEEEEEEEEE");


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
            checkBTState();
            System.out.println("Creo el thread GUACHOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
            car_thread = Car_Activity_Thread.getInstance(btSocket);

            frameLayout = (SurfaceView) findViewById(R.id.camera_preview);
            // Create our Preview view and set it as the content of our activity.
            frameLayout.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            frameLayout.getHolder().addCallback(car_thread);

            car_thread.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("PAUSEEEEEEE");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("STOPEOOOOO");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("Destruyo el thread");
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        car_thread.cancel(true);
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
            }
            else conectarBluetooth();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    conectarBluetooth();

                } else {
                    // Acciones adicionales a realizar si el usuario no activa el Bluetooth
                }
                break;
            }
            default:
                break;
        }

    }

    private void conectarBluetooth() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        // Look for the Arduino Bluetooth Device
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice paired_device : pairedDevices) {
                if (paired_device.getAddress().equals(ARDUINO_MAC)) {
                    device = paired_device;
                    break;
                }
            }
        }

        if (device != null) {
            //Create an RFCOMM BluetoothSocket ready to start a secure outgoing connection to this remote device
            try {
                btSocket = device.createRfcommSocketToServiceRecord(ARDUINO_UUID);
            } catch (Exception e) {
            }
            System.out.println("Creo el socket bluetooth");
        } else {
            Toast.makeText(getBaseContext(), "El dispositivo Bluetooth del Arduino no se encuentra emparejado", Toast.LENGTH_SHORT).show();
        }

        //System.out.println("Socket conectado : " + btSocket.toString());
        if (btSocket != null && !btSocket.isConnected()) {
            try {
                System.out.println("Voy a conectar el bluetooth");
                btAdapter.cancelDiscovery();
                btSocket.connect();
                System.out.println("Conecto el bluetooth");
                //outStream = btSocket.getOutputStream();
                Toast.makeText(getBaseContext(), "Conexion con Arduino establecida correctamente", Toast.LENGTH_SHORT).show();

                statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.check));
                statusBtn.setOnTouchListener(null);

                if (car_thread == null)
                    car_thread = Car_Activity_Thread.getInstance(btSocket);
                else car_thread.setBluetoothSocket(btSocket);

            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "No se pudo establecer conexión BT", Toast.LENGTH_SHORT).show();
                statusBtn.setBackground(ContextCompat.getDrawable(getBaseContext(), R.drawable.retry));
                e.printStackTrace();
            }
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
            checkBTState();
        } catch (Exception e) {

        }
    }

}