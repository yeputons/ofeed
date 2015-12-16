package net.yeputons.ofeed.db;

import android.app.DownloadManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.j256.ormlite.dao.Dao;
import net.yeputons.ofeed.OfeedApplication;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.WebResource;
import net.yeputons.ofeed.web.dmanager.DownloadManagerResourceDownloader;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.WeakHashMap;

public class WebResourcesCache {
    private static final String TAG = WebResourcesCache.class.getName();
    private static final WeakHashMap<URI, WebResource> inMemoryResources = new WeakHashMap<>();

    @NonNull
    private static synchronized WebResource getWebResource(@NonNull URI uri) {
        WebResource result = inMemoryResources.get(uri);
        if (result != null) {
            return result;
        }
        result = new WebResource(uri);
        File foundFile = null;
        try {
            Dao<CachedWebResource, Integer> dao = DbHelper.get().getCachedWebResourcesDao();
            List<CachedWebResource> foundList = dao.queryForEq(CachedWebResource.URI_FIELD, uri.toString());
            if (foundList.size() > 1) {
                Log.e(TAG, "Found more than one WebResource for an URI: " + uri);
            }
            for (CachedWebResource attempt : foundList) {
                foundFile = new File(attempt.localFile);
                if (foundFile.canRead()) {
                    break;
                } else {
                    Log.w(TAG, "File disappeared from cache: " + foundFile.toString() + " for " + uri.toString());
                    foundFile = null;
                    dao.delete(attempt);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error while retrieving WebResource from DB cache", e);
        }
        if (foundFile != null) {
            final CachedResourceDownload download = new CachedResourceDownload(foundFile);
            result.addDownload(new ResourceDownloader() {
                @NonNull
                @Override
                public ResourceDownload createDownload(@NonNull WebResource resource) {
                    return download;
                }
            });
        }
        inMemoryResources.put(uri, result);
        return result;
    }

    @Nullable
    public static WebResource getCachedDownloadingWebResource(@NonNull URI uri) {
        return inMemoryResources.get(uri);
    }

    @NonNull
    public static WebResource getDownloadingWebResource(@NonNull URI uri) {
        final WebResource resource  = getWebResource(uri);
        ResourceDownload download = resource.getActualDownload();
        if (download != null) {
            if (download.getState() == ResourceDownload.State.NOT_STARTED) {
                download.start();
            } else if (download .getState() == ResourceDownload.State.FAILED) {
                download = null;
            }
        }
        if (download == null) {
            final ResourceDownload d = resource.addDownload(OfeedApplication.getDownloader());
            d.start();
            d.addDownloadCompleteListener(new DownloadCompleteListener() {
                @Override
                public void onDownloadComplete() {
                    Dao<CachedWebResource, Integer> dao = DbHelper.get().getCachedWebResourcesDao();
                    CachedWebResource r = new CachedWebResource();
                    r.uri = resource.uri.toString();
                    r.localFile = d.getLocalFile().getAbsolutePath();
                    try {
                        dao.create(r);
                    } catch (SQLException e) {
                        Log.e(TAG, "Unable to save downloaded resource into db", e);
                    }
                }
            });
        }
        return resource;
    }
}
