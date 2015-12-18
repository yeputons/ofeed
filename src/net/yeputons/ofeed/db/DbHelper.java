package net.yeputons.ofeed.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DbHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = WebResourcesCache.class.getName();
    private static final String DATABASE_NAME = "ofeed.db";
    private static final int DATABASE_VERSION = 9;
    private static volatile DbHelper dbHelper;

    private volatile Dao<CachedWebResource, String> cachedWebResourcesDao;
    private volatile Dao<CachedFeedItem, String> cachedFeedItemDao;
    private volatile Dao<CachedUser, Integer> cachedUserDao;
    private volatile Dao<CachedGroup, Integer> cachedGroupDao;
    private volatile Dao<CachedWebPage, String> cachedWebPageDao;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, CachedWebResource.class);
            TableUtils.createTable(connectionSource, CachedFeedItem.class);
            TableUtils.createTable(connectionSource, CachedUser.class);
            TableUtils.createTable(connectionSource, CachedGroup.class);
            TableUtils.createTable(connectionSource, CachedWebPage.class);
        } catch (SQLException e) {
            Log.e(TAG, "Error while creating DB", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        if (oldVer == newVer) {
            return;
        }
        if (newVer != DATABASE_VERSION) {
            throw new UnsupportedOperationException();
        }
        if (oldVer < 2) {
            throw new UnsupportedOperationException();
        }
        if (oldVer >= 2 && oldVer <= 7) {
            try {
                if (oldVer > 2) {
                    TableUtils.dropTable(connectionSource, CachedFeedItem.class, true);
                }
                TableUtils.createTable(connectionSource, CachedFeedItem.class);
            } catch (SQLException e) {
                Log.e(TAG, "Error while creating CachedFeedItem table");
                throw new RuntimeException(e);
            }
        }
        if (oldVer <= 6) {
            try {
                TableUtils.createTable(connectionSource, CachedGroup.class);
                TableUtils.createTable(connectionSource, CachedUser.class);
            } catch (SQLException e) {
                Log.e(TAG, "Error while creating CachedGroup and CachedUser tables");
                throw new RuntimeException(e);
            }
        }
        if (oldVer <= 8) {
            try {
                TableUtils.createTable(connectionSource, CachedWebPage.class);
            } catch (SQLException e) {
                Log.e(TAG, "Error while creating CachedWebPage tables");
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    public Dao<CachedWebResource, String> getCachedWebResourcesDao() {
        if (cachedWebResourcesDao == null) {
            synchronized (this) {
                if (cachedWebResourcesDao == null) {
                    try {
                        cachedWebResourcesDao = DaoManager.createDao(getConnectionSource(), CachedWebResource.class);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot create DAO", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedWebResourcesDao;
    }

    @NonNull
    public Dao<CachedFeedItem, String> getCachedFeedItemDao() {
        if (cachedFeedItemDao == null) {
            synchronized (this) {
                if (cachedFeedItemDao == null) {
                    try {
                        cachedFeedItemDao = DaoManager.createDao(getConnectionSource(), CachedFeedItem.class);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot create DAO", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedFeedItemDao;
    }

    @NonNull
    public Dao<CachedUser, Integer> getCachedUserDao() {
        if (cachedUserDao == null) {
            synchronized (this) {
                if (cachedUserDao == null) {
                    try {
                        cachedUserDao = DaoManager.createDao(getConnectionSource(), CachedUser.class);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot create DAO", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedUserDao;
    }

    @NonNull
    public Dao<CachedGroup, Integer> getCachedGroupDao() {
        if (cachedGroupDao == null) {
            synchronized (this) {
                if (cachedGroupDao == null) {
                    try {
                        cachedGroupDao = DaoManager.createDao(getConnectionSource(), CachedGroup.class);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot create DAO", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedGroupDao;
    }

    @NonNull
    public Dao<CachedWebPage, String> getCachedWebPageDao() {
        if (cachedWebPageDao == null) {
            synchronized (this) {
                if (cachedWebPageDao == null) {
                    try {
                        cachedWebPageDao = DaoManager.createDao(getConnectionSource(), CachedWebPage.class);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot create DAO", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return cachedWebPageDao;
    }

    @NonNull
    public synchronized static DbHelper get() {
        if (dbHelper == null) {
            throw new IllegalStateException("DbHelper is not initialized yet");
        }
        return dbHelper;
    }

    public synchronized static void initializeHelper(Context context) {
        if (dbHelper != null) {
            throw new IllegalStateException("DbHelper is already initialized");
        }
        dbHelper = OpenHelperManager.getHelper(context, DbHelper.class);
    }

    public synchronized static void deinitializeHelper() {
        if (dbHelper == null) {
            throw new IllegalStateException("DbHelper is not initialized");
        }
        OpenHelperManager.releaseHelper();
        dbHelper = null;
    }
}
