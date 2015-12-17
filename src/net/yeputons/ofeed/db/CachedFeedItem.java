package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vk.sdk.api.model.VKApiFeedItem;

@DatabaseTable(tableName = "feed_items")
public class CachedFeedItem {
    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField
    public long date;

    @DatabaseField
    public String nextPageToLoad;

    @DatabaseField(canBeNull = false)
    @NonNull
    public String serializedFeedItem;
}
