package net.yeputons.ofeed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import net.yeputons.ofeed.db.WebResourcesCache;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;

public class DownloadableImageView extends ImageView {
    public DownloadableImageView(Context context) {
        super(context);
    }

    public DownloadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImageURI(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImageResource(int resId) {
        throw new UnsupportedOperationException();
    }

    @Nullable private URI imageUri;

    public void setDownloadableImageUri(@Nullable String imageUriStr) {
        super.setImageURI(null);
        if (imageUriStr == null) {
            return;
        }

        this.imageUri = URI.create(imageUriStr);
        final URI downloadingImageUri = this.imageUri;
        WebResource cached = WebResourcesCache.getCachedDownloadingWebResource(downloadingImageUri);
        if (cached != null) {
            ResourceDownload download = cached.getDownloaded();
            if (download != null) {
                super.setImageURI(Uri.fromFile(download.getLocalFile()));
                return;
            }
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                final WebResource resource = WebResourcesCache.getDownloadingWebResource(downloadingImageUri);
                resource.addOrRunDownloadCompleteListener(new DownloadCompleteListener() {
                    @Override
                    public void onDownloadComplete() {
                        imageDownloadCompleted(resource);
                    }
                });
                return null;
            }
        }.execute();
    }

    private void imageDownloadCompleted(final WebResource resource) {
        if (imageUri == null || !imageUri.equals(resource.uri)) {
            return;
        }
        final ResourceDownload d = resource.getDownloaded();
        final Uri resultingUri = d != null ? Uri.fromFile(d.getLocalFile()) : null;
        post(new Runnable() {
            @Override
            public void run() {
                if (!imageUri.equals(resource.uri)) {
                    return;
                }
                if (d == null) {
                    //Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
                } else {
                    DownloadableImageView.super.setImageURI(resultingUri);
                }
            }
        });
    }
}
