package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.vk.sdk.api.model.VKApiFeedItem;
import com.vk.sdk.api.model.VKApiPost;

public class PostActivity extends Activity {
    public static final String EXTRA_POST = PostActivity.class.getCanonicalName() + ".EXTRA_POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_activity);

        Intent intent = getIntent();
        VKApiFeedItem feedItem = intent.getParcelableExtra(EXTRA_POST);
        if (feedItem == null) {
            throw new IllegalArgumentException("Should be called with some specific feed item");
        }
        if (feedItem.post == null) {
            throw new IllegalArgumentException("Should be called with post feed item");
        }
        ((PostView) findViewById(R.id.postView)).setPost(feedItem.post);
    }
}
