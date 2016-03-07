package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vk.sdk.api.model.VKApiCommunity;

@DatabaseTable(tableName = "groups")
public class CachedGroup {
    @DatabaseField(id = true)
    @NonNull
    public int id;

    @DatabaseField
    @Nullable
    public String name;

    @DatabaseField(columnName = "photo_100")
    @Nullable
    public String photo100;

    public CachedGroup() {
    }

    public CachedGroup(VKApiCommunity group) {
        id = group.id;
        name = group.name;
        photo100 = group.photo_100;
    }
}
