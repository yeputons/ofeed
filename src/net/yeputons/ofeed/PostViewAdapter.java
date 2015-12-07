package net.yeputons.ofeed;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKApiUser;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;
import net.yeputons.ofeed.web.dmanager.DownloadManagerResourceDownloader;

import java.net.URI;
import java.util.Date;

public class PostViewAdapter extends ArrayAdapter<VKApiPost> {
    private final VKApiPost[] values;
    private final DownloadManagerResourceDownloader downloader;

    public PostViewAdapter(Context context, VKApiPost[] values) {
        super(context, R.layout.post, values);
        this.values = values;
        downloader = new DownloadManagerResourceDownloader((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View postView = View.inflate(getContext(), R.layout.post, null);

        VKApiPost post = values[position];

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
            final WebResource resource = new WebResource(URI.create(imageUri));
            final ResourceDownload d = resource.addDownload(downloader);
            d.start();
            d.setDownloadCompleteListener(new DownloadCompleteListener() {
                @Override
                public void onDownloadComplete() {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            ResourceDownload d = resource.getDownloaded();
                            if (d == null) {
                                Toast.makeText(getContext(), "Unable to load image", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            imageView.setImageURI(Uri.fromFile(d.getLocalFile()));
                        }
                    });
                }
            });
        }
        ((TextView) postView.findViewById(R.id.postAuthorName)).setText(name);
        return postView;
    }
}
