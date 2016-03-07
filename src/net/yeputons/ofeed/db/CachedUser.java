package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.vk.sdk.api.model.VKApiUser;

@DatabaseTable(tableName = "users")
public class CachedUser {
    @DatabaseField(id = true)
    @NonNull
    public int id;

    @DatabaseField
    @Nullable
    public String first_name;

    @DatabaseField
    @Nullable
    public String last_name;

    @DatabaseField
    @Nullable
    public String photo_100;

    public CachedUser() {
    }

    public CachedUser(VKApiUser user) {
        id = user.id;
        first_name = user.first_name;
        last_name = user.last_name;
        photo_100 = user.photo_100;
    }
}
