package net.yeputons.ofeed.web.dmanager;

import android.support.annotation.NonNull;
import android.util.Log;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.ResourceToFileDownloader;
import net.yeputons.ofeed.web.WebResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolResourceDownloader extends ResourceToFileDownloader {
    private static final String TAG = ThreadPoolResourceDownloader.class.getName();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @NonNull
    @Override
    public ResourceDownload createDownload(@NonNull WebResource resource) {
        URI uri = resource.uri;
        File destination = getFileForUri(uri);
        return new AtResourceDownload(uri, destination);
    }

    public void shutdown() {
        executor.shutdown();
    }

    private class AtResourceDownload implements ResourceDownload, Runnable {
        private final URI uri;
        private final File destination;

        private State state;
        private final List<DownloadCompleteListener> listeners = new ArrayList<>();
        private boolean taskStarted = false;

        @Override
        public void run() {
            HttpURLConnection urlConnection;
            try {
                Log.d(TAG, String.format("Download '%s' to '%s'", uri, destination));
                setState(State.IN_PROGRESS);
                urlConnection = (HttpURLConnection) uri.toURL().openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode / 100 != 2) {
                    Log.w(TAG, "Strange response code during download: " + responseCode + " " + urlConnection.getResponseMessage());
                }

                InputStream input = urlConnection.getInputStream();
                destination.getParentFile().mkdirs();
                FileOutputStream output = new FileOutputStream(destination);

                byte[] buffer = new byte[4 * 1024];
                int bufferLength = 0;
                while ((bufferLength = input.read(buffer)) > 0) {
                    output.write(buffer, 0, bufferLength);
                }
                output.close();

                Log.d(TAG, "Download completed");
                setState(State.COMPLETED);
                input.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                Log.w(TAG, "Download failed", e);
                setState(State.FAILED);
            }
        }

        private AtResourceDownload(URI uri, File destination) {
            this.uri = uri;
            this.destination = destination;
        }

        @Override
        public synchronized void addDownloadCompleteListener(DownloadCompleteListener listener) {
            listeners.add(listener);
        }

        @NonNull
        @Override
        public synchronized State getState() {
            return state;
        }

        @NonNull
        @Override
        public File getLocalFile() {
            return destination;
        }

        private synchronized void setState(State state) {
            this.state = state;
            if (this.state == State.COMPLETED || this.state == State.FAILED) {
                for (DownloadCompleteListener l : listeners) {
                    l.onDownloadComplete();
                }
                listeners.clear();
            }
        }

        @Override
        public synchronized void start() {
            if (taskStarted) {
                throw new IllegalStateException("Download was already started");
            }
            taskStarted = true;
            executor.submit(this);
        }
    }
}
