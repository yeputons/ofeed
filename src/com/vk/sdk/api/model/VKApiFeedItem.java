package com.vk.sdk.api.model;

import android.os.Parcel;
import org.json.JSONException;
import org.json.JSONObject;

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
    public boolean can_edit;
    public boolean can_delete;
    public VKApiPost post;

    public VKApiFeedItem() {
    }

    public VKApiFeedItem(Parcel source) {
        type = source.readString();
        source_id = source.readInt();
        date = source.readLong();
        can_edit = source.readByte() != 0;
        can_delete = source.readByte() != 0;
        post = source.readParcelable(getClass().getClassLoader());
    }

    public VKApiFeedItem(JSONObject from) throws JSONException {
        parse(from);
    }

    public VKApiFeedItem parse(JSONObject source) throws JSONException {
        type = source.optString("type");
        source_id = source.optInt("source_id");
        date = source.optLong("date");
        can_edit = ParseUtils.parseBoolean(source, "can_edit");
        can_delete = ParseUtils.parseBoolean(source, "can_delete");
        post = null;

        if (type.equals(TYPE_POST)) {
            source.put("id", source.opt("post_id"));
            source.put("to_id", source_id);
            source.put("from_id", source_id);
            source.put("reply_owner_id", source.opt("copy_owner_id"));
            source.put("reply_post_id", source.opt("copy_post_id"));
            // TODO: handle final_post
            // TODO: handle copy_post_date
            post = new VKApiPost(source);
        }
        // TODO: handle remaining feed item types
        return this;
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
        dest.writeByte(can_edit ? (byte)1 : (byte)0);
        dest.writeByte(can_delete ? (byte)1 : (byte)0);
        dest.writeParcelable(post, flags);
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
