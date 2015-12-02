package net.yeputons.ofeed.vk;

import com.vk.sdk.api.VKParser;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.methods.VKApiBase;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import org.json.JSONException;
import org.json.JSONObject;

public class VKApiFeed extends VKApiBase {
    public VKRequest get() {
        return prepareRequest("get", null, new VKParser() {
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
