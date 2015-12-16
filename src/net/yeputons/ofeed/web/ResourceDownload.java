package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;

import java.io.File;

public interface ResourceDownload {
    public static enum State {
        NOT_STARTED,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED
    }

    abstract public void addDownloadCompleteListener(DownloadCompleteListener listener);

    @NonNull
    abstract public State getState();

    @NonNull
    abstract public File getLocalFile();

    public abstract void start();
}
