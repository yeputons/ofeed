package net.yeputons.ofeed.web;

import android.support.annotation.NonNull;

import java.net.URI;

public interface WebPageSaver {
    @NonNull
    WebResource savePage(@NonNull URI uri);
}
