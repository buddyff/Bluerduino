package camparo_dombronsky.bluerduino.Joystick;

import camparo_dombronsky.bluerduino.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class Joystick_Setup extends AppCompatActivity {

    private TextView ip;
    private ImageButton buttonConnect;
    private ImageButton btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0,btnD,btnE;

    private Vibrator vibrator;

    OnClickListener buttonConnectOnClickListener =
            new OnClickListener(){

                @Override
                public void onClick(View arg0) {
                    buttonConnect.setImageResource(R.drawable.on);
                    Intent intent = new Intent(Joystick_Setup.this, Joystick.class);
                    intent.putExtra("ip",ip.getText().toString());
                    startActivity(intent);
                }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.joystick_setup);

        vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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
                vibrator.vibrate(50);
            }
        });

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"2");
                vibrator.vibrate(50);
            }
        });

        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"3");
                vibrator.vibrate(50);
            }
        });

        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"4");
                vibrator.vibrate(50);
            }
        });

        btn5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"5");
                vibrator.vibrate(50);
            }
        });

        btn6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"6");
                vibrator.vibrate(50);
            }
        });

        btn7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"7");
                vibrator.vibrate(50);
            }
        });

        btn8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"8");
                vibrator.vibrate(50);
            }
        });

        btn9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+"9");
                vibrator.vibrate(50);
            }
        });

        btn0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText() + "0");
                vibrator.vibrate(50);
            }
        });



        btnD.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ip.setText(ip.getText()+".");
                vibrator.vibrate(50);
            }
        });

        btnE.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String aux = ip.getText().toString();
                if(ip.length() > 0)
                    ip.setText(aux.substring(0,ip.length()-1));
                vibrator.vibrate(50);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        buttonConnect.setImageResource(R.drawable.off);
    }

}
