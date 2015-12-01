package net.yeputons.ofeed.web.dmanager;

import android.app.DownloadManager;
import android.util.Log;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.ResourceToFileDownloader;
import net.yeputons.ofeed.web.WebResource;
import android.support.annotation.NonNull;

import java.io.File;
import java.net.URI;

public class DownloadManagerResourceDownloader extends ResourceToFileDownloader {
    static private final String TAG = DownloadManagerResourceDownloader.class.getName();

    private final DownloadManager downloadManager;

    public DownloadManagerResourceDownloader(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @NonNull
    @Override
    public ResourceDownload createDownload(@NonNull WebResource resource) {
        URI uri = resource.uri;
        File destination = getFileForUri(uri);
        Log.d(TAG, String.format("Download '%s' to '%s'", uri, destination));
        return new ResourceDownloadToFile(uri, destination, downloadManager);
    }
}
