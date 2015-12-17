package net.yeputons.ofeed;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vk.sdk.api.model.VKApiPost;
import net.yeputons.ofeed.db.*;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class FeedListViewAdapter extends BaseAdapter {
    private static final String TAG = FeedListViewAdapter.class.getName();
    private final MainActivity mainActivity;
    private final Dao<CachedFeedItem, String> itemDao = DbHelper.get().getCachedFeedItemDao();

    public FeedListViewAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private QueryBuilder<CachedFeedItem, String> getItemsQuery() {
        return itemDao.queryBuilder().orderBy("date", false).orderBy("id", true);
    }

    @Override
    public int getCount() {
        try {
            long answer = itemDao.countOf();
            if (answer > Integer.MAX_VALUE) {
                Log.e(TAG, "Too many feed items found (integer overflow!)");
                return Integer.MAX_VALUE;
            }
            return (int)answer;
        } catch (SQLException e) {
            Log.e(TAG, "Unable to get count of feed items", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public CachedFeedItem getItem(int i) {
        try {
            return getItemsQuery().offset((long) i).limit(1L).queryForFirst();
        } catch (SQLException e) {
            Log.e(TAG, "Unable to get item by position in feed from database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getItemId(int i) {
        try {
            return getItemsQuery().offset((long)i).limit(1L).selectColumns("id").queryForFirst().id.hashCode();
        } catch (SQLException e) {
            Log.e(TAG, "Unable to get item's id by position in feed from database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View postView = convertView;

        final CachedFeedItem feedItem = getItem(position);
        if (feedItem.isPageEnd()) {
            // Load more
            if (postView == null || postView.findViewById(R.id.buttonLoadMore) == null) {
                postView = View.inflate(mainActivity, R.layout.load_more, null);
            }

            Button buttonLoadMore = (Button) postView.findViewById(R.id.buttonLoadMore);
            buttonLoadMore.setTag(feedItem.nextPageToLoad);
            buttonLoadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getTag() == feedItem.nextPageToLoad) {
                        mainActivity.loadFrom(feedItem.nextPageToLoad);
                    }
                }
            });
            return postView;
        }
        if (feedItem.feedItem == null) {
            Log.e(TAG, "Invalid feed item loaded: neither page end nor standard feed item");
            return postView;
        }

        // Post
        VKApiPost post = feedItem.feedItem.post;

        if (postView == null || postView.findViewById(R.id.postText) == null) {
            postView = View.inflate(mainActivity, R.layout.post, null);
        }

        ((TextView) postView.findViewById(R.id.postText)).setText(post.text);
        ((TextView) postView.findViewById(R.id.postDate)).setText(new Date(post.date * 1000).toString());

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
        final ImageView imageView = (ImageView) postView.findViewById(R.id.postAuthorPhoto);
        imageView.setImageResource(R.drawable.default_avatar);
        if (imageUri != null) {
            final URI imageUriFinal = URI.create(imageUri);
            imageView.setTag(imageUriFinal);
            WebResource cached = WebResourcesCache.getCachedDownloadingWebResource(imageUriFinal);
            boolean found = false;
            if (cached != null) {
                ResourceDownload download = cached.getDownloaded();
                if (download != null) {
                    imageView.setImageURI(Uri.fromFile(download.getLocalFile()));
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
                                if (imageView.getTag() != imageUriFinal) {
                                    return;
                                }
                                final ResourceDownload d = resource.getDownloaded();
                                final Uri resultingUri = d != null ? Uri.fromFile(d.getLocalFile()) : null;
                                imageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (imageView.getTag() != imageUriFinal) {
                                            return;
                                        }
                                        if (d == null) {
                                            Toast.makeText(mainActivity, "Unable to load image", Toast.LENGTH_SHORT).show();
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
