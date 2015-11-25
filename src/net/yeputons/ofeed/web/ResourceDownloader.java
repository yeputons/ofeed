package net.yeputons.ofeed.web;

import org.jetbrains.annotations.NotNull;

public interface ResourceDownloader {
    @NotNull
    public ResourceDownload createDownload(@NotNull WebResource resource);
}
