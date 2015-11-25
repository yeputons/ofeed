package net.yeputons.ofeed.web.dmanager;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;

public class ResourceDownloadToFile implements ResourceDownload {
    @NotNull  final private URI remoteUri;
    @NotNull  final private File localFile;
    @NotNull  final private DownloadManager downloadManager;
    @Nullable private Long downloadManagerRelatedId;

    private static final String TAG = ResourceDownloadToFile.class.getName();

    public ResourceDownloadToFile(@NotNull URI remoteUri, @NotNull File localFile, @NotNull DownloadManager downloadManager) {
        this.remoteUri = remoteUri;
        this.localFile = localFile;
        this.downloadManager = downloadManager;
    }

    @Override
    public void start() {
        DownloadManager.Request request;
        request = new DownloadManager.Request(Uri.parse(remoteUri.toString()));

        File parentDir = getLocalFile().getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            Log.e(TAG, "Unable to mkdirs for file");
        }
        if (getLocalFile().exists() && !getLocalFile().delete()) {
            Log.e(TAG, "Unable to delete downloaded file");
        }
        request.setDestinationUri(Uri.fromFile(getLocalFile()));
        request.setTitle("O'Feed download");
        request.setDescription(remoteUri.toString());
        //request.setVisibleInDownloadsUi(false);
        downloadManagerRelatedId = downloadManager.enqueue(request);
    }

    @NotNull
    @Override
    public File getLocalFile() {
        return localFile;
    }

    @Override
    public void setDownloadCompleteListener(DownloadCompleteListener listener) {
        if (downloadManagerRelatedId == null) {
            throw new IllegalStateException("Download is not started");
        }
        DownloadCompleteBroadcastReceiver.setCompleteListener(downloadManagerRelatedId, listener);
    }

    @NotNull
    @Override
    public State getState() {
        if (downloadManagerRelatedId == null) {
            return State.NOT_STARTED;
        }
        Cursor c;
        c = downloadManager.query(new DownloadManager.Query().setFilterById(downloadManagerRelatedId));
        try {
            if (!c.moveToFirst()) {
                return State.FAILED;
            }
            if (c.getCount() > 1) {
                Log.w(TAG, "More than one row in DownloadManager's answer");
            }
            int result = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (result) {
                case DownloadManager.STATUS_PENDING:
                    return State.NOT_STARTED;
                case DownloadManager.STATUS_RUNNING:
                    return State.IN_PROGRESS;
                case DownloadManager.STATUS_PAUSED:
                    return State.PAUSED;
                case DownloadManager.STATUS_SUCCESSFUL:
                    return State.COMPLETED;
                case DownloadManager.STATUS_FAILED:
                    return State.FAILED;
            }
            throw new AssertionError("Invalid download status returned by DownloadManager");
        } finally {
            c.close();
        }
    }
}
