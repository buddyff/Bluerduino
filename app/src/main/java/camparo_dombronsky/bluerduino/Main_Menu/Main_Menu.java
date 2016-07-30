package camparo_dombronsky.bluerduino.Main_Menu;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import camparo_dombronsky.bluerduino.Car.Car_Activity;
import camparo_dombronsky.bluerduino.Joystick.Joystick_Setup;
import camparo_dombronsky.bluerduino.R;

public class Main_Menu extends Activity {
    private ImageButton btn_car;
    private ImageButton btn_joystick;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        title = (TextView) findViewById(R.id.menu_title);
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/smart watch.ttf");
        title.setTypeface(type);

        btn_car = (ImageButton) findViewById(R.id.btn_car);
        btn_car.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                } else {
                    Intent intent = new Intent(Main_Menu.this, Car_Activity.class);
                    startActivity(intent);
                }
            }
        });

        btn_joystick = (ImageButton) findViewById(R.id.btn_joystick);
        btn_joystick.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Main_Menu.this, Joystick_Setup.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {

                    Intent intent = new Intent(Main_Menu.this, Car_Activity.class);
                    startActivity(intent);
                } else {
                    // Acciones adicionales a realizar si el usuario no activa el Bluetooth
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter.getDefaultAdapter().disable();
    }
}
