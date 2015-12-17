package net.yeputons.ofeed;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import net.yeputons.ofeed.db.WebResourcesCache;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

public class PostViewAdapter extends ArrayAdapter<VKApiPost> {
    private final ArrayList<VKApiPost> values;

    public PostViewAdapter(Context context, ArrayList<VKApiPost> values) {
        super(context, R.layout.post, values);
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View postView = convertView;
        if (postView == null || postView.findViewById(R.id.postText) == null) {
            postView = View.inflate(getContext(), R.layout.post, null);
        }

        VKApiPost post = values.get(position);

        ((TextView) postView.findViewById(R.id.postText)).setText(post.text);
        ((TextView) postView.findViewById(R.id.postDate)).setText(new Date(post.date * 1000).toString());

        String imageUri, name;
        if (post.from_id >= 0) {
            VKApiUser user = MainActivity.users.get(post.from_id);
            imageUri = user.photo_100;
            name = user.first_name + " " + user.last_name;
        } else {
            VKApiCommunity group = MainActivity.groups.get(-post.from_id);
            imageUri = group.photo_100;
            name = group.name;
        }
        if (imageUri != null) {
            final ImageView imageView = (ImageView) postView.findViewById(R.id.postAuthorPhoto);
            final URI imageUriFinal = URI.create(imageUri);
            imageView.setTag(imageUriFinal);
            WebResource cached = WebResourcesCache.getCachedDownloadingWebResource(imageUriFinal);
            boolean found = false;
            if (cached != null) {
                ResourceDownload download = cached.getDownloaded();
                if (download != null) {
                    imageView.setImageURI(Uri.fromFile(download.getLocalFile()));
                    found = true;
                } else {
                    imageView.setImageURI(null);
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
                                if (imageView.getTag() != imageUriFinal) {
                                    return;
                                }
                                final ResourceDownload d = resource.getDownloaded();
                                final Uri resultingUri = d != null ? Uri.fromFile(d.getLocalFile()) : null;
                                imageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (d == null) {
                                            Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        imageView.setImageURI(resultingUri);
                                    }
                                });
                            }
                        });
                        return null;
                    }
                }.execute();
            }
        }
        ((TextView) postView.findViewById(R.id.postAuthorName)).setText(name);
        return postView;
    }
}
