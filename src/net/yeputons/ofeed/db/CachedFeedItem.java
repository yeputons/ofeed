package net.yeputons.ofeed.db;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vk.sdk.api.model.VKApiFeedItem;

@DatabaseTable(tableName = "feed_items")
public class CachedFeedItem {
    public static final String NEXT_PAGE_TO_LOAD = "nextPageToLoad";

    // tilde is intended to make IDs of page ends bigger than everything else
    public static final String TYPE_PAGE_END = "~pageEnd";

    @DatabaseField(id = true, canBeNull = false)
    @NonNull
    public String id;

    @DatabaseField
    public long date;

    @DatabaseField(canBeNull = false)
    @NonNull
    public String nextPageToLoad;

    @DatabaseField(canBeNull = true, persisterClass = VKApiFeedItemPersister.class)
    @Nullable
    public VKApiFeedItem feedItem;

    public CachedFeedItem() {
    }

    public CachedFeedItem(VKApiFeedItem item) {
        if (item.type.equals(VKApiFeedItem.TYPE_POST)) {
            id = item.type + "_" + item.post.to_id + "_" + item.post.id;
        } else {
            throw new UnsupportedOperationException();
        }
        date = item.date;
        feedItem = item;
    }

    public CachedFeedItem(VKApiFeedItem item, String nextPageToLoad) {
        id = getPageEndId(nextPageToLoad);
        date = item.date;
        this.nextPageToLoad = nextPageToLoad;
    }

    public static String getPageEndId(String nextPageToLoad) {
        return TYPE_PAGE_END + "_" + nextPageToLoad;
    }

    public boolean isPageEnd() {
        return id.startsWith(TYPE_PAGE_END + "_");
    }
}
