package net.yeputons.ofeed.web.dmanager;

import android.app.DownloadManager;
import android.util.Log;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.ResourceToFileDownloader;
import net.yeputons.ofeed.web.WebResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;

public class DownloadManagerResourceDownloader extends ResourceToFileDownloader {
    static private final String TAG = DownloadManagerResourceDownloader.class.getName();

    private final DownloadManager downloadManager;

    public DownloadManagerResourceDownloader(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @NotNull
    @Override
    public ResourceDownload createDownload(@NotNull WebResource resource) {
        URI uri = resource.uri;
        File destination = getFileForUri(uri);
        Log.d(TAG, String.format("Download '%s' to '%s'", uri, destination));
        return new ResourceDownloadToFile(uri, destination, downloadManager);
    }
}
