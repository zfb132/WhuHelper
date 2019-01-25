package tools.sms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zfb15 on 2017/8/7.
 */

public class HeadlessSmsSendService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //TODO 待定
        return null;
    }
}
