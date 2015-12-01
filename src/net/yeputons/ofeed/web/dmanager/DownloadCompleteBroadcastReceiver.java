package net.yeputons.ofeed.web.dmanager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DownloadCompleteBroadcastReceiver extends BroadcastReceiver {
    private static final ConcurrentMap<Long, DownloadCompleteListener> listeners = new ConcurrentHashMap<>();

    public static void setCompleteListener(long id, @NonNull DownloadCompleteListener l) {
        listeners.put(id, l);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        DownloadCompleteListener l = listeners.remove(id);
        if (l != null) {
            Log.i("TAG", "Download completed: " + id);
            l.onDownloadComplete();
        }
    }
}
