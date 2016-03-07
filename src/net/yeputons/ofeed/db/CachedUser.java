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

    @DatabaseField(columnName = "first_name")
    @Nullable
    public String firstName;

    @DatabaseField(columnName = "last_name")
    @Nullable
    public String lastName;

    @DatabaseField(columnName = "photo_100")
    @Nullable
    public String photo100;

    public CachedUser() {
    }

    public CachedUser(VKApiUser user) {
        id = user.id;
        firstName = user.first_name;
        lastName = user.last_name;
        photo100 = user.photo_100;
    }
}
