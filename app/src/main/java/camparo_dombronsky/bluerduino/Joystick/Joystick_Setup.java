package camparo_dombronsky.bluerduino.Joystick;

import camparo_dombronsky.bluerduino.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class Joystick_Setup extends AppCompatActivity {

    TextView ip;
    ImageButton buttonConnect;
    ImageButton btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0,btnD,btnE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_setup);

        ip = (TextView)findViewById(R.id.ip);
        buttonConnect = (ImageButton)findViewById(R.id.connect);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        btn1 = (ImageButton)findViewById(R.id.btn1);
        btn2 = (ImageButton)findViewById(R.id.btn2);
        btn3 = (ImageButton)findViewById(R.id.btn3);
        btn4 = (ImageButton)findViewById(R.id.btn4);
        btn5 = (ImageButton)findViewById(R.id.btn5);
        btn6 = (ImageButton)findViewById(R.id.btn6);
        btn7 = (ImageButton)findViewById(R.id.btn7);
        btn8 = (ImageButton)findViewById(R.id.btn8);
        btn9 = (ImageButton)findViewById(R.id.btn9);
        btn0 = (ImageButton)findViewById(R.id.btn0);
        btnD = (ImageButton)findViewById(R.id.btnD);
        btnE = (ImageButton)findViewById(R.id.btnE);

        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"1");
            }
        });

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"2");
            }
        });

        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"3");
            }
        });

        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"4");
            }
        });

        btn5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"5");
            }
        });

        btn6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"6");
            }
        });

        btn7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"7");
            }
        });

        btn8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"8");
            }
        });

        btn9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"9");
            }
        });

        btn0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText() + "0");
            }
        });



        btnD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+".");
            }
        });

        btnE.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String aux = ip.getText().toString();
                ip.setText(aux.substring(0,ip.length()-1));
            }
        });
    }

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    buttonConnect.setImageResource(R.drawable.on);
                    Intent intent = new Intent(Joystick_Setup.this, Joystick_Activity.class);
                    intent.putExtra("ip",ip.getText().toString());
                    startActivity(intent);
                }};



}
