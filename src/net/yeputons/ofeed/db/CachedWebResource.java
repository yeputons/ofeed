package net.yeputons.ofeed.db;

import android.support.annotation.NonNull;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "web_resources")
public class CachedWebResource {
    public static final String URI_FIELD = "uri";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(canBeNull = false, index = true)
    @NonNull
    public String uri;

    @DatabaseField(canBeNull = false)
    @NonNull
    public String localFile;
}
