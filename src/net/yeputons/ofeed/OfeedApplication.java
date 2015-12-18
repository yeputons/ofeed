package net.yeputons.ofeed;

import android.app.Application;
import android.support.annotation.NonNull;
import com.vk.sdk.VKSdk;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.web.DeepWebPageSaver;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.dmanager.ThreadPoolResourceDownloader;

public class OfeedApplication extends Application {
    //private static DownloadManagerResourceDownloader downloader;
    private static ThreadPoolResourceDownloader downloader;

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
        DbHelper.initializeHelper(getApplicationContext());
        //downloader = new DownloadManagerResourceDownloader((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
        downloader = new ThreadPoolResourceDownloader();
    }

    @NonNull
    public static ResourceDownloader getDownloader() {
        if (downloader == null) {
            throw new IllegalStateException();
        }
        return downloader;
    }

    @Override
    public void onTerminate() {
        downloader.shutdown();
        downloader = null;
        DbHelper.deinitializeHelper();
        super.onTerminate();
    }
}
