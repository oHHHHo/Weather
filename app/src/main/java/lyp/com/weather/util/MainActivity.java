package lyp.com.weather.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private static final int TAG = 1;

    private CircleDial mCircleDial;
    private EditText inputMin,inputMax,inputCurrent;
    private Button btnSure;

    private int min,max,center;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TAG:
                    mCircleDial.setAngle(center);
                    mCircleDial.setMinMaxTem(min,max);
                    mCircleDial.setCenterTemper(center);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCircleDial =  findViewById(R.id.temp_line_dial);
        inputMin =  findViewById(R.id.min_temp);
        inputMax = findViewById(R.id.max_temp);
        inputCurrent = findViewById(R.id.current_temp);
        btnSure = findViewById(R.id.sure_temp);

        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        min = Integer.parseInt(inputMin.getText().toString());
                        max = Integer.parseInt(inputMax.getText().toString());
                        center = Integer.parseInt(inputCurrent.getText().toString());

                        Message msg = new Message();
                        msg.what = TAG;
                        mHandler.sendMessage(msg);
                    }
                }).start();

            }
        });
    }


        <lyp.com.miui_tq.Widget.CircleDial
    android:id="@+id/temp_line_dial"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
}
