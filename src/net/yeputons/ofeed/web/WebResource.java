package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Deque;

public class WebResource {
    @NonNull public final URI uri;
    protected final Deque<ResourceDownload> downloads = new ArrayDeque<>();

    public WebResource(@NonNull URI uri) {
        this.uri = uri;
    }

    @NonNull
    public ResourceDownload addDownload(@NonNull ResourceDownloader d) {
        ResourceDownload result = d.createDownload(this);
        downloads.add(result);
        return result;
    }

    @Nullable
    public ResourceDownload getDownloaded() {
        for (ResourceDownload d : downloads) {
            if (d.getState() == ResourceDownload.State.COMPLETED) {
                return d;
            }
        }
        return null;
    }

    @Nullable
    public ResourceDownload getLastDownload() {
        return downloads.peekLast();
    }

    @Nullable
    public ResourceDownload getActualDownload() {
        ResourceDownload result = getDownloaded();
        if (result != null) {
            return result;
        }
        return getLastDownload();
    }

    @NonNull
    public URI getActualUri() {
        ResourceDownload d = getDownloaded();
        if (d != null) {
            URI result = d.getLocalFile().toURI();
            try {
                return new URI(
                        result.getScheme(),
                        "",
                        result.getPath(),
                        result.getQuery(),
                        result.getFragment()
                );
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return uri;
            }
        } else {
            return uri;
        }
    }

    public void addOrRunDownloadCompleteListener(DownloadCompleteListener downloadCompleteListener) {
        ResourceDownload download = getActualDownload();
        if (download == null) {
            throw new IllegalStateException("No actual download");
        }
        if (download.getState() == ResourceDownload.State.COMPLETED) {
            downloadCompleteListener.onDownloadComplete();
        } else {
            download.addDownloadCompleteListener(downloadCompleteListener);
        }
    }
}
