package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import com.vk.sdk.api.methods.VKApiFeed;
import com.vk.sdk.api.model.*;
import net.yeputons.ofeed.db.CachedFeedItem;
import net.yeputons.ofeed.db.CachedGroup;
import net.yeputons.ofeed.db.CachedUser;
import net.yeputons.ofeed.db.DbHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
    private static final String TAG = "ofeed";
    private Menu optionsMenu;
    private FeedListViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (VKAccessToken.currentToken() == null) {
            logout(null);
            login(null);
        } else {
            onResult(VKAccessToken.currentToken());
        }
        adapter = new FeedListViewAdapter(this);
        ((ListView) findViewById(R.id.listFeed)).setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        optionsMenu = null;
        adapter = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        optionsMenu = menu;
        updateMenuStatus();
        return true;
    }

    public void login(MenuItem item) {
        VKSdk.login(this, "friends", "wall");
    }

    public void loadBeginning(MenuItem item) {
        new VKApiFeed().get(VKParameters.from(VKApiConst.COUNT, 2)).executeWithListener(feedGetListener);
    }

    public void loadFrom(final String startFrom) {
        new VKApiFeed().get(VKParameters.from(VKApiConst.COUNT, 2, VKApiFeed.START_FROM, startFrom)).executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                feedGetListener.onComplete(response);
                UpdateBuilder<CachedFeedItem, String> update = DbHelper.get().getCachedFeedItemDao().updateBuilder();
                try {
                    update.where().eq(CachedFeedItem.NEXT_PAGE_TO_LOAD, startFrom);
                    update.updateColumnValue(CachedFeedItem.NEXT_PAGE_TO_LOAD, "");
                    update.update();

                    DbHelper.get().getCachedFeedItemDao().deleteById(CachedFeedItem.getPageEndId(startFrom));
                } catch (SQLException e) {
                    Log.e(TAG, "Unable to remove 'next page' marker from some feed items", e);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                feedGetListener.attemptFailed(request, attemptNumber, totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                feedGetListener.onError(error);
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                feedGetListener.onProgress(progressType, bytesLoaded, bytesTotal);
            }
        });
    }

    public void clearCache(MenuItem item) {
        DbHelper h = DbHelper.get();
        try {
            h.getCachedFeedItemDao().deleteBuilder().delete();
            h.getCachedGroupDao().deleteBuilder().delete();
            h.getCachedUserDao().deleteBuilder().delete();
            h.getCachedWebResourcesDao().deleteBuilder().delete();
        } catch (SQLException e) {
            Log.e(TAG, "Unable to clear cache", e);
            Toast.makeText(this, "Error while clearing cache, it may become inconsistent", Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
    }

    public void logout(MenuItem item) {
        VKSdk.logout();
        clearCache(item);
        updateMenuStatus();
    }

    private void updateMenuStatus() {
        if (optionsMenu == null) {
            return;
        }
        boolean isLoggedIn = VKAccessToken.currentToken() != null;
        optionsMenu.findItem(R.id.menuItemLogin).setEnabled(!isLoggedIn);
        optionsMenu.findItem(R.id.menuItemLoadBeginning).setEnabled(isLoggedIn);
        optionsMenu.findItem(R.id.menuItemLogout).setEnabled(isLoggedIn);
    }

    private final VKRequest.VKRequestListener feedGetListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            final VKApiFeedPage page = (VKApiFeedPage) response.parsedModel;
            final Dao<CachedUser, Integer> userDao = DbHelper.get().getCachedUserDao();
            final Dao<CachedGroup, Integer> groupDao = DbHelper.get().getCachedGroupDao();
            final ArrayList<VKApiFeedItem> feed = new ArrayList<VKApiFeedItem>();
            for (VKApiFeedItem item : page.items) {
                if (item.type.equals(VKApiFeedItem.TYPE_POST)) {
                    feed.add(item);
                }
            }
            final Dao<CachedFeedItem, String> itemDao = DbHelper.get().getCachedFeedItemDao();
            try {
                userDao.callBatchTasks(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (VKApiUser u : page.profiles) {
                            userDao.createOrUpdate(new CachedUser(u));
                        }
                        return null;
                    }
                });
                groupDao.callBatchTasks(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (VKApiCommunity g : page.groups) {
                            groupDao.createOrUpdate(new CachedGroup(g));
                        }
                        return null;
                    }
                });
                itemDao.callBatchTasks(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        for (int i = 0; i < feed.size(); i++) {
                            CachedFeedItem item = new CachedFeedItem(feed.get(i));
                            if (i + 1 == feed.size()) {
                                item.nextPageToLoad = page.next_from;
                            } else {
                                item.nextPageToLoad = "";
                            }
                            List<CachedFeedItem> old =
                                    itemDao.query(
                                            itemDao.queryBuilder()
                                                    .selectColumns("nextPageToLoad")
                                                    .where().eq("id", item.id).prepare());
                            if (!old.isEmpty() && old.get(0).nextPageToLoad.isEmpty()) {
                                item.nextPageToLoad = "";
                            }
                            itemDao.createOrUpdate(item);
                            if (!item.nextPageToLoad.isEmpty()) {
                                itemDao.createOrUpdate(new CachedFeedItem(feed.get(i), item.nextPageToLoad));
                            }
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Unable to update db with received feed items", e);
            }
            adapter.notifyDataSetChanged();
            Log.d(TAG, "next_from=" + page.next_from);
        }

        @Override
        public void onError(VKError error) {
            Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
            Toast.makeText(MainActivity.this, String.format("Progress %d/%d", bytesLoaded, bytesTotal), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onResult(final VKAccessToken res) {
        updateMenuStatus();
        loadBeginning(null);
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "Error while logging in: " + error, Toast.LENGTH_LONG).show();
    }
}
