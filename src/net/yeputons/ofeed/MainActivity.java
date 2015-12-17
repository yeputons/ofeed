package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
    private static final String TAG = "ofeed";
    private Menu optionsMenu;

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

    public void logout(MenuItem item) {
        VKSdk.logout();
        updateFeed();
        ((TextView) findViewById(R.id.textCurrentUser)).setText("Not logged in");
        updateMenuStatus();
    }

    private void updateMenuStatus() {
        if (optionsMenu == null) {
            return;
        }
        boolean isLoggedIn = VKAccessToken.currentToken() != null;
        optionsMenu.findItem(R.id.menuItemLogin).setEnabled(!isLoggedIn);
        optionsMenu.findItem(R.id.menuItemLogout).setEnabled(isLoggedIn);
    }

    @Override
    public void onResult(final VKAccessToken res) {
        ((TextView) findViewById(R.id.textCurrentUser)).setText("UserId = " + res.userId);
        updateMenuStatus();
        new VKApiFeed().get(VKParameters.from(VKApiConst.COUNT, 100)).executeWithListener(new VKRequest.VKRequestListener() {
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
                            }
                            return null;
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Unable to update db with received feed items", e);
                }
                updateFeed();
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
        });
    }

    private void updateFeed() {
        Dao<CachedFeedItem, String> itemDao = DbHelper.get().getCachedFeedItemDao();

        CloseableIterator<CachedFeedItem> iterator = null;
        ArrayList<VKApiPost> posts = new ArrayList<>();
        try {
            iterator = itemDao.queryBuilder().orderBy("date", false).iterator();
            while (iterator.hasNext()) {
                CachedFeedItem cached = iterator.next();
                if (cached.feedItem.type.equals(VKApiFeedItem.TYPE_POST)) {
                    posts.add(cached.feedItem.post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (iterator != null) {
                iterator.closeQuietly();
            }
        }

        ListView list = (ListView) findViewById(R.id.listFeed);
        list.setAdapter(new PostViewAdapter(this, posts));
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "Error while logging in: " + error, Toast.LENGTH_LONG).show();
    }
}
