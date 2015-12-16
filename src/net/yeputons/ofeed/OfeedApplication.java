package net.yeputons.ofeed;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import com.vk.sdk.VKSdk;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.web.dmanager.DownloadManagerResourceDownloader;

public class OfeedApplication extends Application {
    private static DownloadManagerResourceDownloader downloader;

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
        DbHelper.initializeHelper(getApplicationContext());
        downloader = new DownloadManagerResourceDownloader((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
    }

    @NonNull
    public static DownloadManagerResourceDownloader getDownloader() {
        if (downloader == null) {
            throw new IllegalStateException();
        }
        return downloader;
    }

    @Override
    public void onTerminate() {
        downloader = null;
        DbHelper.deinitializeHelper();
        super.onTerminate();
    }
}
