package net.yeputons.ofeed.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

public class ResourceOpener {
    private final Context context;

    public ResourceOpener(@NonNull Context context) {
        this.context = context;
    }

    public void openResource(@NonNull WebResource r) {
        Uri uri = Uri.parse(r.getActualUri().toString());
        Intent intentChrome = new Intent(Intent.ACTION_VIEW);
        if (uri.getScheme().equalsIgnoreCase("file")) {
            intentChrome.setDataAndType(uri, "multipart/related");
        } else {
            intentChrome.setData(uri);
        }
        context.startActivity(intentChrome);

//        Intent intentDefault = new Intent(Intent.ACTION_VIEW);
//        intentDefault.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
//        intentDefault.setDataAndType(uri, "html/text");
//
//        Intent intentOther = new Intent(Intent.ACTION_VIEW);
//        intentDefault.addCategory(Intent.CATEGORY_BROWSABLE);
//        intentDefault.setDataAndType(uri, "html/text");
//
//        Intent intentChooser = Intent.createChooser(intentChrome, "Open in...");
//        intentChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intentDefault, intentOther });
//        context.startActivity(intentChooser);
    }
}
