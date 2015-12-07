package net.yeputons.ofeed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.*;
import com.vk.sdk.api.methods.VKApiFeed;
import com.vk.sdk.api.model.VKApiFeedPage;

public class MainActivity extends Activity implements VKCallback<VKAccessToken> {
    private static final String TAG = "ofeed";
    private Menu optionsMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (VKAccessToken.currentToken() == null) {
            logout(null);
            login(null);
        } else {
            onResult(VKAccessToken.currentToken());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, this)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        optionsMenu = menu;
        updateMenuStatus();
        return true;
    }

    public void login(MenuItem item) {
        VKSdk.login(this, "friends", "wall");
    }

    public void logout(MenuItem item) {
        VKSdk.logout();
        ((TextView) findViewById(R.id.textCurrentUser)).setText("Not logged in");
        updateMenuStatus();
    }

    private void updateMenuStatus() {
        if (optionsMenu == null) {
            return;
        }
        boolean isLoggedIn = VKAccessToken.currentToken() != null;
        optionsMenu.findItem(R.id.menuItemLogin).setEnabled(!isLoggedIn);
        optionsMenu.findItem(R.id.menuItemLogout).setEnabled(isLoggedIn);
    }

    @Override
    public void onResult(final VKAccessToken res) {
        ((TextView) findViewById(R.id.textCurrentUser)).setText("UserId = " + res.userId);
        updateMenuStatus();
        new VKApiFeed().get().executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiFeedPage page = (VKApiFeedPage) response.parsedModel;
                Log.d(TAG, "next_from=" + page.next_from);
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                Toast.makeText(MainActivity.this, String.format("Progress %d/%d", bytesLoaded, bytesTotal), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(VKError error) {
        Toast.makeText(this, "Error while logging in: " + error, Toast.LENGTH_LONG).show();
    }
}
