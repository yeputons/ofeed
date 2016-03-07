package net.yeputons.ofeed.web;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ResourceToFileDownloader implements ResourceDownloader {
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("(\\.\\p{Alnum}+)$");

    @NonNull
    protected File getFileForUri(@NonNull URI uri) {
        String extension = "";
        Matcher m = EXTENSION_PATTERN.matcher(uri.getPath());
        if (m.find()) {
            extension = m.group(1);
        }
        return new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/ofeed/",
                UUID.randomUUID().toString() + extension);
    }
}
