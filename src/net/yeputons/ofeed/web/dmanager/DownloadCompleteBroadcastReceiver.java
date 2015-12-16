package net.yeputons.ofeed.web.dmanager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadCompleteBroadcastReceiver extends BroadcastReceiver {
    private static final Map<Long, List<DownloadCompleteListener>> listeners = new HashMap<>();

    public static void addCompleteListener(long id, @NonNull DownloadCompleteListener l) {
        synchronized (listeners) {
            List<DownloadCompleteListener> ls = listeners.get(id);
            if (ls == null) {
                ls = new ArrayList<DownloadCompleteListener>();
                ls.add(l);
                listeners.put(id, ls);
            } else {
                ls.add(l);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        synchronized (listeners) {
            List<DownloadCompleteListener> ls = listeners.remove(id);
            if (ls != null) {
                Log.i("TAG", "Download completed: " + id);
                for (DownloadCompleteListener l : ls) {
                    l.onDownloadComplete();
                }
            }
        }
    }
}
