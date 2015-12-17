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
import java.util.HashSet;
import java.util.Set;

public class FeedListViewAdapter extends BaseAdapter {
    private static final String TAG = FeedListViewAdapter.class.getName();
    private final MainActivity mainActivity;
    private final Dao<CachedFeedItem, String> itemDao = DbHelper.get().getCachedFeedItemDao();
    private Set<String> pageLoadsInProgress = new HashSet<>();

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
            return getItemsQuery().offset((long) i).limit(1L).selectColumns("id").queryForFirst().id.hashCode();
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
            if (postView == null || !(postView instanceof LoadMoreView)) {
                postView = new LoadMoreView(mainActivity, pageLoadsInProgress);
            }

            LoadMoreView loadMoreView = (LoadMoreView) postView;
            loadMoreView.setNextPageToLoad(feedItem.nextPageToLoad);
            loadMoreView.updateButtonState();
            return postView;
        }
        if (feedItem.feedItem == null) {
            Log.e(TAG, "Invalid feed item loaded: neither page end nor standard feed item");
            return postView;
        }

        // Post
        VKApiPost post = feedItem.feedItem.post;

        if (postView == null || !(postView instanceof PostView)) {
            postView = new PostView(mainActivity);
        }

        ((PostView) postView).setPost(post);
        return postView;
    }

    public void completePageLoad(String startFrom) {
        pageLoadsInProgress.remove(startFrom);
    }
}
