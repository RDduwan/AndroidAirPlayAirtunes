package ss.serven.rduwan.airtunesandroid;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by rduwan on 17/6/29.
 */
public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        startAirTuneService();
    }

    private void startAirTuneService() {
        Intent intent = new Intent(mContext, AirTunesService.class);
        startService(intent);
    }
}
