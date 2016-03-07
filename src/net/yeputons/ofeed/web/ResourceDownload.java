package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;

import java.io.File;

public interface ResourceDownload {
    enum State {
        NOT_STARTED,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED
    }

    void addDownloadCompleteListener(DownloadCompleteListener listener);

    @NonNull
    State getState();

    @NonNull
    File getLocalFile();

    void start();
}
