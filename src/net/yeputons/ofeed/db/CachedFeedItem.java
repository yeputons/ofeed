package net.yeputons.ofeed.db;

import android.os.Parcel;
import android.support.annotation.NonNull;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vk.sdk.api.model.VKApiFeedItem;

@DatabaseTable(tableName = "feed_items")
public class CachedFeedItem {
    @DatabaseField(id = true, canBeNull = false)
    @NonNull
    public String id;

    @DatabaseField
    public long date;

    @DatabaseField(canBeNull = false)
    @NonNull
    public String nextPageToLoad;

    @DatabaseField(canBeNull = false, persisterClass = VKApiFeedItemPersister.class)
    @NonNull
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
}
