package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import net.yeputons.ofeed.vk.VKApiFeed;
import net.yeputons.ofeed.vk.VKApiFeedPage;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
    private static final String TAG = "OFEED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        VKSdk.login(this, "friends", "wall");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResult(final VKAccessToken res) {
        Toast.makeText(this, "OnResult: " + res.userId, Toast.LENGTH_SHORT).show();
        new VKApiFeed().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiFeedPage page = (VKApiFeedPage) response.parsedModel;
                Log.d(TAG, "next_from=" + page.next_from);
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                Toast.makeText(MainActivity.this, String.format("Progress %d/%d", bytesLoaded, bytesTotal), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "OnError: " + error, Toast.LENGTH_LONG).show();
    }
}
