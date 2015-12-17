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
    private static final int DATABASE_VERSION = 3;
    private static volatile DbHelper dbHelper;

    private volatile Dao<CachedWebResource, String> cachedWebResourcesDao;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, CachedWebResource.class);
            TableUtils.createTable(connectionSource, CachedFeedItem.class);
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
        if (oldVer == 2) {
            try {
                TableUtils.createTable(connectionSource, CachedFeedItem.class);
            } catch (SQLException e) {
                Log.e(TAG, "Error while creating CachedFeedItem table");
                throw new RuntimeException(e);
            }
            oldVer++;
        }
        if (oldVer != newVer) {
            throw new AssertionError("Internal error during db migration");
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
