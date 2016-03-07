package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;

public interface ResourceDownloader {
    @NonNull
    ResourceDownload createDownload(@NonNull WebResource resource);

    void shutdown();
}
