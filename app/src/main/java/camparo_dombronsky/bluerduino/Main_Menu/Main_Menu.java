package camparo_dombronsky.bluerduino.Main_Menu;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import camparo_dombronsky.bluerduino.Car.Car_Setup;
import camparo_dombronsky.bluerduino.R;

public class Main_Menu extends AppCompatActivity {
    private Button btn_car;
    private Button btn_joystick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        btn_car = (Button) findViewById(R.id.btn_car);
        btn_car.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Main_Menu.this, Car_Setup.class);
                startActivity(intent);
            }
        });


    }
}
