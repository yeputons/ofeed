package net.yeputons.ofeed.vk;

import android.os.Parcel;
import com.vk.sdk.api.model.*;

public class VKApiFeedPage extends VKApiModel {
    public VKApiFeedItem[] items;
    public VKList<VKApiUser> profiles;
    public VKList<VKApiCommunity> groups;
    public String next_from;

    public VKApiFeedPage() {
    }

    public VKApiFeedPage(Parcel source) {
        items = new VKApiFeedItem[source.readInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = source.readParcelable(getClass().getClassLoader());
        }
        profiles = source.readParcelable(getClass().getClassLoader());
        groups = source.readParcelable(getClass().getClassLoader());
        next_from = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(items.length);
        for (VKApiFeedItem item : items) {
            dest.writeParcelable(item, flags);
        }
        dest.writeParcelable(profiles, flags);
        dest.writeParcelable(groups, flags);
        dest.writeString(next_from);
    }

    public static Creator<VKApiFeedPage> CREATOR = new Creator<VKApiFeedPage>() {
        public VKApiFeedPage createFromParcel(Parcel source) {
            return new VKApiFeedPage(source);
        }

        public VKApiFeedPage[] newArray(int size) {
            return new VKApiFeedPage[size];
        }
    };
}
