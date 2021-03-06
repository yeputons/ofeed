package net.yeputons.ofeed;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import com.vk.sdk.VKSdk;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.dmanager.DownloadManagerResourceDownloader;
import net.yeputons.ofeed.web.dmanager.ThreadPoolResourceDownloader;

public class OfeedApplication extends Application {
    private static ResourceDownloader downloader;

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
        DbHelper.initializeHelper(getApplicationContext());
        createThreadPoolResourceDownloader();
    }

    // Use either of the following two methods to specify application-wide downloader

    private void createDownloadManagerResourceDownloader() {
        downloader = new DownloadManagerResourceDownloader(
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
    }

    private void createThreadPoolResourceDownloader() {
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
