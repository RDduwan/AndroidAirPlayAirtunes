package ss.serven.rduwan.airtunesandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by rduwan on 17/6/29.
 */

public class AirTunesService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AirTunesService","start AirTunes");
        new Thread(AirTunesRunnable.getInstance()).start();
    }
}
