package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import com.vk.sdk.api.model.*;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
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
        VKApi.users().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiUser user = (VKApiUser) ((VKList) response.parsedModel).get(0);
                Toast.makeText(MainActivity.this, user.first_name + " " + user.last_name, Toast.LENGTH_SHORT).show();
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
