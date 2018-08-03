package lyp.com.weather;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by liyp on 18-8-3.
 */

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView backBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);

        backBtn = findViewById(R.id.title_selectCity_back);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.title_selectCity_back:
                finish();
                break;
            default:
                break;
        }
    }
}
