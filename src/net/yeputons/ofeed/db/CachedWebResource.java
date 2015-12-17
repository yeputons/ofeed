package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "web_resources")
public class CachedWebResource {
    @DatabaseField(canBeNull = false, id = true)
    @NonNull
    public String uri;

    @DatabaseField(canBeNull = false)
    @NonNull
    public String localFile;
}
