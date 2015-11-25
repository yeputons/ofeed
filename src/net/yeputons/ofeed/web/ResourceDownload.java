package net.yeputons.ofeed.web;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface ResourceDownload {
    public static enum State {
        NOT_STARTED,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED
    }

    abstract public void setDownloadCompleteListener(DownloadCompleteListener listener);

    @NotNull
    abstract public State getState();

    @NotNull
    abstract public File getLocalFile();

    public abstract void start();
}
