package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;

import java.io.File;

public class CachedResourceDownload implements ResourceDownload {
    @NonNull private final File localFile;

    public CachedResourceDownload(@NonNull File localFile) {
        this.localFile = localFile;
    }

    @Override
    public void setDownloadCompleteListener(DownloadCompleteListener listener) {
    }

    @NonNull
    @Override
    public State getState() {
        return State.COMPLETED;
    }

    @NonNull
    @Override
    public File getLocalFile() {
        return localFile;
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException();
    }
}
