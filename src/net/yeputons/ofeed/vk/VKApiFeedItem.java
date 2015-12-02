package net.yeputons.ofeed.vk;

import android.os.Parcel;
import com.vk.sdk.api.model.VKApiModel;

public class VKApiFeedItem extends VKApiModel {
    public static final String TYPE_POST = "post";
    public static final String TYPE_PHOTO = "photo";
    public static final String TYPE_PHOTO_TAG = "photo_tag";
    public static final String TYPE_WALL_PHOTO = "wall_photo";
    public static final String TYPE_FRIEND = "friend";
    public static final String TYPE_NOTE = "note";

    public String type;
    public int source_id;
    public long date;

    public VKApiFeedItem() {
    }

    public VKApiFeedItem(Parcel source) {
        type = source.readString();
        source_id = source.readInt();
        date = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(source_id);
        dest.writeLong(date);
    }

    public static Creator<VKApiFeedItem> CREATOR = new Creator<VKApiFeedItem>() {
        public VKApiFeedItem createFromParcel(Parcel source) {
            return new VKApiFeedItem(source);
        }

        public VKApiFeedItem[] newArray(int size) {
            return new VKApiFeedItem[size];
        }
    };
}
