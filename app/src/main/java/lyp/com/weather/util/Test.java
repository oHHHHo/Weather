package lyp.com.weather.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import lyp.com.weather.R;

public class Test extends AppCompatActivity {

    private static final int TAG = 1;

    private CircleDial mCircleDial;

    private int min,max,center;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TAG:
                    mCircleDial.setAngle(15);
                    mCircleDial.setMinMaxTem(10,20);
                    mCircleDial.setCenterTemper(15);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.miui);

//        mCircleDial =  findViewById(R.id.temp_line_dial);
/*        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = TAG;
                        mHandler.sendMessage(msg);
                    }
                }).start();

            }
        });*/
    }

}
