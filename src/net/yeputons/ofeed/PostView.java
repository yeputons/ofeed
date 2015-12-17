package net.yeputons.ofeed;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.vk.sdk.api.model.VKApiPost;
import net.yeputons.ofeed.db.CachedGroup;
import net.yeputons.ofeed.db.CachedUser;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.db.WebResourcesCache;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.sql.SQLException;
import java.util.Date;

public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getName();
    private final TextView postAuthorName;
    private final TextView postText;
    private final TextView postDate;
    private final ImageView postAuthorPhoto;

    public PostView(Context context) {
        super(context);
        inflate(context, R.layout.post, this);
        postAuthorName = (TextView) findViewById(R.id.postAuthorName);
        postText = (TextView) findViewById(R.id.postText);
        postDate = (TextView) findViewById(R.id.postDate);
        postAuthorPhoto = (ImageView) findViewById(R.id.postAuthorPhoto);
    }

    public void setPost(@NonNull VKApiPost post) {
        postText.setText(post.text);
        postDate.setText(new Date(post.date * 1000).toString());

        String imageUri = null, name = "N/A";
        if (post.from_id >= 0) {
            CachedUser user = null;
            try {
                user = DbHelper.get().getCachedUserDao().queryForId(post.from_id);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load user from db", e);
            }
            if (user != null) {
                imageUri = user.photo_100;
                name = user.first_name + " " + user.last_name;
            }
        } else {
            CachedGroup group = null;
            try {
                group = DbHelper.get().getCachedGroupDao().queryForId(-post.from_id);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load group from db", e);
            }
            if (group != null) {
                imageUri = group.photo_100;
                name = group.name;
            }
        }
        postAuthorPhoto.setImageResource(R.drawable.default_avatar);
        if (imageUri != null) {
            final URI imageUriFinal = URI.create(imageUri);
            postAuthorPhoto.setTag(imageUriFinal);
            WebResource cached = WebResourcesCache.getCachedDownloadingWebResource(imageUriFinal);
            boolean found = false;
            if (cached != null) {
                ResourceDownload download = cached.getDownloaded();
                if (download != null) {
                    postAuthorPhoto.setImageURI(Uri.fromFile(download.getLocalFile()));
                    found = true;
                }
            }
            if (!found) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        final WebResource resource = WebResourcesCache.getDownloadingWebResource(imageUriFinal);
                        resource.addOrRunDownloadCompleteListener(new DownloadCompleteListener() {
                            @Override
                            public void onDownloadComplete() {
                                if (postAuthorPhoto.getTag() != imageUriFinal) {
                                    return;
                                }
                                final ResourceDownload d = resource.getDownloaded();
                                final Uri resultingUri = d != null ? Uri.fromFile(d.getLocalFile()) : null;
                                postAuthorPhoto.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (postAuthorPhoto.getTag() != imageUriFinal) {
                                            return;
                                        }
                                        if (d == null) {
                                            Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        postAuthorPhoto.setImageURI(resultingUri);
                                    }
                                });
                            }
                        });
                        return null;
                    }
                }.execute();
            }
        }
        postAuthorName.setText(name);
    }
}
