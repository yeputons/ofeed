package com.vk.sdk.api.methods;

import android.support.annotation.Nullable;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKParser;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiFeedPage;
import org.json.JSONException;
import org.json.JSONObject;

public class VKApiFeed extends VKApiBase {
    public static final String START_FROM = "start_from";
    public static final String FILTERS = "filters";

    public static final String FILTERS_POST = "post";

    public VKRequest get() {
        return get(null);
    }

    public VKRequest get(@Nullable VKParameters params) {
        return prepareRequest("get", params, new VKParser() {
            @Override
            public Object createModel(JSONObject object) {
                try {
                    return new VKApiFeedPage().parse(object);
                } catch (JSONException e) {
                    return null;
                }
            }
        });
    }

    @Override
    protected String getMethodsGroup() {
        return "newsfeed";
    }
}
