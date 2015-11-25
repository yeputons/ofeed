package net.yeputons.ofeed.web;

import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DeepWebPageSaver implements WebPageSaver {
    @NotNull private final Map<URI, WebResource> resources = new HashMap<>();
    @NotNull private final ResourceDownloader downloader;

    private static final String TAG = DeepWebPageSaver.class.getName();

    public DeepWebPageSaver(@NotNull ResourceDownloader downloader) {
        this.downloader = downloader;
    }

    @NotNull private final AtomicInteger downloadsRemaining = new AtomicInteger();

    @Nullable private DownloadCompleteListener downloadCompleteListener = null;

    public void setDownloadCompleteListener(@Nullable DownloadCompleteListener listener) {
        downloadCompleteListener = listener;
    }

    private void fireDownloadCompleteListener() {
        if (downloadCompleteListener != null) {
            downloadCompleteListener.onDownloadComplete();
        }
    }

    @NotNull
    @Override
    public WebResource savePage(@NotNull final URI uri) {
        WebResource result = resources.get(uri);
        if (result != null) {
            return result;
        }
        result = new WebResource(uri);
        final WebResource mainPage = result;
        ResourceDownload download = result.addDownload(downloader);
        download.start();
        downloadsRemaining.incrementAndGet();
        download.setDownloadCompleteListener(new DownloadCompleteListener() {
            @Override
            public void onDownloadComplete() {
                downloadPageResources(mainPage);
            }
        });
        resources.put(uri, result);
        return result;
    }

    private final DownloadCompleteListener generalDownloadCompleteListener = new DownloadCompleteListener() {
        @Override
        public void onDownloadComplete() {
            if (downloadsRemaining.decrementAndGet() == 0) {
                fireDownloadCompleteListener();
            }
        }
    };

    @NotNull
    private ResourceDownload downloadExtraResource(@NotNull URI uri) {
        WebResource resource = new WebResource(uri);
        ResourceDownload d = resource.addDownload(downloader);
        d.start();
        downloadsRemaining.incrementAndGet();
        d.setDownloadCompleteListener(generalDownloadCompleteListener);
        return d;
    }

    @NotNull
    private String saveExtraResource(@NotNull String uri) {
        if (uri.equals("")) {
            return "";
        }
        ResourceDownload resource;
        try {
            resource = downloadExtraResource(new URI(uri));
        } catch (URISyntaxException e) {
            return "";
        }
        URI result = resource.getLocalFile().toURI();
        String[] tmp = result.getPath().split("/");
        return tmp[tmp.length - 1];
    }

    private void downloadPageResources(@NotNull WebResource mainPage) {
        ResourceDownload d = mainPage.getDownloaded();
        try {
            if (d == null) {
                Log.e(TAG, "Page resource downloading is requested before page download is completed. Probably, page download failed");
                return;
            }
            Log.d(TAG, "Loading downloaded web page...");
            File file = d.getLocalFile();
            Document doc;
            doc = Jsoup.parse(file, "utf-8"); // IOException
            doc.setBaseUri(mainPage.uri.toString());
            Log.d(TAG, "Updating elements...");
            for (Element el : doc.getElementsByTag("link")) {
                if (el.attr("rel").toLowerCase().equals("stylesheet")) {
                    el.attr("href", saveExtraResource(el.attr("abs:href")));
                }
            }
            for (Element el : doc.getElementsByTag("img")) {
                el.attr("src", saveExtraResource(el.attr("abs:src")));
            }
            FileWriter fw = null;
            Log.d(TAG, "Saving resulting web page...");
            try {
                fw = new FileWriter(file);
                fw.write(doc.outerHtml());
            } finally {
                if (fw != null) {
                    fw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Completed processing web page");
            generalDownloadCompleteListener.onDownloadComplete();
        }
    }
}
