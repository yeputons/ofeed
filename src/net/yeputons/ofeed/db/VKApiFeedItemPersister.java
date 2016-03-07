package net.yeputons.ofeed.db;

import android.os.Parcel;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.ByteArrayType;
import com.vk.sdk.api.model.VKApiFeedItem;

import java.sql.SQLException;

public class VKApiFeedItemPersister extends ByteArrayType {
    //CHECKSTYLE.OFF: LineLength
    // https://github.com/j256/ormlite-jdbc/blob/master/src/test/java/com/j256/ormlite/examples/datapersister/DateTimePersister.java
    //CHECKSTYLE.ON: LineLength
    private static final VKApiFeedItemPersister SINGLETON = new VKApiFeedItemPersister();

    private VKApiFeedItemPersister() {
        super(SqlType.BYTE_ARRAY, new Class<?>[] {VKApiFeedItem.class});
    }

    public static VKApiFeedItemPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        VKApiFeedItem item = (VKApiFeedItem) javaObject;
        if (item == null) {
            return null;
        } else {
            Parcel p = Parcel.obtain();
            item.writeToParcel(p, 0);
            p.setDataPosition(0);
            Object result = super.javaToSqlArg(fieldType, p.marshall());
            p.recycle();
            return result;
        }
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        byte[] cached = (byte[]) super.sqlArgToJava(fieldType, sqlArg, columnPos);
        Parcel p = Parcel.obtain();
        p.unmarshall(cached, 0, cached.length);
        p.setDataPosition(0);
        VKApiFeedItem result = new VKApiFeedItem(p);
        p.recycle();
        return result;
    }
}
