package net.yeputons.ofeed;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import net.yeputons.ofeed.db.CachedFeedItem;
import net.yeputons.ofeed.db.DbHelper;

import java.sql.SQLException;
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
        final CachedFeedItem feedItem = getItem(position);
        if (feedItem.isPageEnd()) {
            // Load more
            if (convertView == null || !(convertView instanceof LoadMoreView)) {
                convertView = new LoadMoreView(mainActivity, pageLoadsInProgress);
            }

            LoadMoreView loadMoreView = (LoadMoreView) convertView;
            loadMoreView.setNextPageToLoad(feedItem.nextPageToLoad);
            loadMoreView.updateButtonState();
            return convertView;
        }
        if (feedItem.feedItem == null) {
            Log.e(TAG, "Invalid feed item loaded: neither page end nor standard feed item");
            return convertView;
        }

        if (convertView == null || !(convertView instanceof PostView)) {
            convertView = new PostView(mainActivity);
        }

        ((PostView) convertView).setPost(feedItem.feedItem.post);
        return convertView;
    }

    public void completePageLoad(String startFrom) {
        pageLoadsInProgress.remove(startFrom);
    }
}
