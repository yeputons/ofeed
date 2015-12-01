package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;

public interface ResourceDownloader {
    @NonNull
    public ResourceDownload createDownload(@NonNull WebResource resource);
}
