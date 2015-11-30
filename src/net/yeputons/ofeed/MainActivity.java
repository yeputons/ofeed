package net.yeputons.ofeed;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import net.yeputons.ofeed.web.*;
import net.yeputons.ofeed.web.dmanager.DownloadManagerResourceDownloader;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity implements DownloadCompleteListener {
    private WebResource r;
    private final ResourceOpener opener = new ResourceOpener(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void downloadPage(View view) {
        DeepWebPageSaver s = new DeepWebPageSaver(new DownloadManagerResourceDownloader(
                (android.app.DownloadManager) getSystemService(DOWNLOAD_SERVICE)
        ));
        try {
            r = s.savePage(new URI("https://nplus1.ru/news/2015/10/19/drone-control"));
            //r = s.savePage(new URI("https://meteor.com"));
            s.setDownloadCompleteListener(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void openPage(View view) {
        opener.openResource(r);
    }

    @Override
    public void onDownloadComplete() {
        if (r.getDownloaded() != null) {
            findViewById(R.id.openPage).setEnabled(true);
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_LONG).show();
        }
    }
}
