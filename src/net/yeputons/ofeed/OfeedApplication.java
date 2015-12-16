package net.yeputons.ofeed;

import android.app.Application;
import com.vk.sdk.VKSdk;
import net.yeputons.ofeed.db.DbHelper;

public class OfeedApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
        DbHelper.initializeHelper(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        DbHelper.deinitializeHelper();
        super.onTerminate();
    }
}
