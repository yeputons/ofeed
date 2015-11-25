package net.yeputons.ofeed.web;

import android.os.Environment;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ResourceToFileDownloader implements ResourceDownloader {
    static private final Pattern extensionPattern = Pattern.compile("(\\.\\p{Alnum}+)$");

    @NotNull
    protected File getFileForUri(@NotNull URI uri) {
        String extension = "";
        Matcher m = extensionPattern.matcher(uri.getPath());
        if (m.matches()) {
            extension = m.group(1);
        }
        return new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/ofeed/",
                UUID.randomUUID().toString() + extension);
    }
}
