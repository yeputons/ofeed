package net.yeputons.ofeed.web;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

public interface WebPageSaver {
    @NotNull
    public WebResource savePage(@NotNull URI uri);
}
