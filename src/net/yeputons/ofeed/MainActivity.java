package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

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
    public void onResult(VKAccessToken res) {
        Toast.makeText(this, "OnResult: " + res.userId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "OnError: " + error, Toast.LENGTH_LONG).show();
    }
}
