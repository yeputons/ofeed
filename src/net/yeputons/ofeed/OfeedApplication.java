package net.yeputons.ofeed;

import android.app.Application;
import com.vk.sdk.VKSdk;

public class OfeedApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
    }
}
