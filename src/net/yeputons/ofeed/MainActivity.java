package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import com.vk.sdk.api.methods.VKApiFeed;
import com.vk.sdk.api.model.*;
import net.yeputons.ofeed.db.*;
import net.yeputons.ofeed.web.DeepWebPageSaver;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
    private static final String TAG = MainActivity.class.getName();
    private Menu optionsMenu;
    private FeedListViewAdapter adapter;
    private int loadStep = 50;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        adapter = new FeedListViewAdapter(this);

        ListView listFeed = (ListView) findViewById(R.id.listFeed);
        listFeed.setAdapter(adapter);
        listFeed.setEmptyView(findViewById(R.id.textEmptyFeed));
        listFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                VKApiFeedItem feedItem = adapter.getItem(position).feedItem;
                if (feedItem != null) {
                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    intent.putExtra(PostActivity.EXTRA_POST, feedItem);
                    startActivity(intent);
                }
            }
        });

        if (VKAccessToken.currentToken() == null) {
            logout(null);
            login(null);
        } else {
            onResult(VKAccessToken.currentToken());
        }
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
        new VKApiFeed().get(VKParameters.from(
                VKApiConst.COUNT, loadStep,
                VKApiFeed.FILTERS, VKApiFeed.FILTERS_POST
        )).executeWithListener(feedGetListener);
    }

    public void loadFrom(final String startFrom) {
        new VKApiFeed().get(VKParameters.from(
                VKApiConst.COUNT, loadStep,
                VKApiFeed.FILTERS, VKApiFeed.FILTERS_POST,
                VKApiFeed.START_FROM, startFrom
        )).executeWithListener(new VKRequest.VKRequestListener() {
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
                adapter.completePageLoad(startFrom);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                feedGetListener.attemptFailed(request, attemptNumber, totalAttempts);
                adapter.completePageLoad(startFrom);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(VKError error) {
                feedGetListener.onError(error);
                adapter.completePageLoad(startFrom);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                feedGetListener.onProgress(progressType, bytesLoaded, bytesTotal);
                adapter.completePageLoad(startFrom);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void loadStep2(MenuItem item) {
        loadStep = 2;
    }

    public void loadStep50(MenuItem item) {
        loadStep = 50;
    }

    public void clearCache(MenuItem item) {
        DbHelper h = DbHelper.get();
        try {
            h.getCachedFeedItemDao().deleteBuilder().delete();
            h.getCachedGroupDao().deleteBuilder().delete();
            h.getCachedUserDao().deleteBuilder().delete();
            h.getCachedWebResourcesDao().deleteBuilder().delete();
            h.getCachedWebPageDao().deleteBuilder().delete();
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
            final ArrayList<CachedFeedItem> feed = new ArrayList<CachedFeedItem>();
            if (page.items.length == 0) {
                return;
            }

            Set<String> occuredItems = new HashSet<>();
            for (VKApiFeedItem item : page.items) {
                CachedFeedItem item2 = new CachedFeedItem(item);
                if (occuredItems.contains(item2.id)) {
                    Log.w(TAG, "Received duplicated item in response from VK: " + item2.id);
                    continue;
                }
                occuredItems.add(item2.id);
                feed.add(item2);
                if (item.post != null && item.post.attachments != null) {
                    for (VKAttachments.VKApiAttachment a : item.post.attachments) {
                        if (a instanceof VKApiLink) {
                            VKApiLink l = (VKApiLink) a;
                            final URI uri = URI.create(l.url);
                            if (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                                DeepWebPageSaver saver = new DeepWebPageSaver(OfeedApplication.getDownloader());
                                final WebResource resource = saver.savePage(uri);
                                saver.setDownloadCompleteListener(new DownloadCompleteListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        CachedWebPage page = new CachedWebPage();
                                        page.uri = uri.toString();
                                        ResourceDownload d = resource.getDownloaded();
                                        if (d == null) {
                                            Log.e(TAG, "Oops, unable to deep save web page");
                                            return;
                                        }
                                        page.localFile = d.getLocalFile().getAbsolutePath();
                                        try {
                                            DbHelper.get().getCachedWebPageDao().create(page);
                                            adapter.notifyDataSetChanged();
                                        } catch (SQLException e) {
                                            Log.e(TAG, "Cannot save deep downloaded page info to db", e);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
            final CachedFeedItem pageEndPlaceholder =
                    new CachedFeedItem(page.items[page.items.length - 1], page.next_from);
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
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        for (VKApiUser u : page.profiles) {
                            WebResourcesCache.getDownloadingWebResource(URI.create(u.photo_100));
                        }
                        for (VKApiCommunity g : page.groups) {
                            WebResourcesCache.getDownloadingWebResource(URI.create(g.photo_100));
                        }
                        return null;
                    }
                }.execute();
                itemDao.callBatchTasks(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        boolean pageDoesNotContinue = true;
                        for (int i = 0; i < feed.size(); i++) {
                            CachedFeedItem item = feed.get(i);
                            CachedFeedItem old =
                                    itemDao.queryForFirst(
                                            itemDao.queryBuilder()
                                                    .selectColumns("nextPageToLoad")
                                                    .where().eq("id", item.id).prepare());
                            if (i + 1 == feed.size()) {
                                if (old != null && old.nextPageToLoad.isEmpty()) {
                                    item.nextPageToLoad = "";
                                    pageDoesNotContinue = false;
                                } else {
                                    item.nextPageToLoad = page.next_from;
                                }
                            } else {
                                if (old != null && !old.nextPageToLoad.isEmpty()) {
                                    itemDao.deleteById(CachedFeedItem.getPageEndId(old.nextPageToLoad));
                                }
                                item.nextPageToLoad = "";
                            }
                            itemDao.createOrUpdate(item);
                        }
                        if (pageDoesNotContinue) {
                            itemDao.createOrUpdate(pageEndPlaceholder);
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
            Toast.makeText(
                    MainActivity.this,
                    String.format("Progress %d/%d", bytesLoaded, bytesTotal),
                    Toast.LENGTH_SHORT
            ).show();
        }
    };

    @Override
    public void onResult(final VKAccessToken res) {
        updateMenuStatus();
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "Error while logging in: " + error, Toast.LENGTH_LONG).show();
    }
}
