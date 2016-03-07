package net.yeputons.ofeed;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Set;

public class LoadMoreView extends LinearLayout implements View.OnClickListener {
    private static final String TAG = LoadMoreView.class.getName();
    private final Button buttonLoadMore;
    private final Set<String> pageLoadsInProgress;
    private final MainActivity mainActivity;
    @Nullable private String nextPageToLoad;

    public LoadMoreView(MainActivity mainActivity, @NonNull Set<String> pageLoadsInProgress) {
        super(mainActivity);
        inflate(mainActivity, R.layout.load_more, this);
        buttonLoadMore = (Button) findViewById(R.id.buttonLoadMore);
        this.pageLoadsInProgress = pageLoadsInProgress;
        this.mainActivity = mainActivity;

        buttonLoadMore.setOnClickListener(this);
    }

    public void setNextPageToLoad(@NonNull String nextPageToLoad) {
        this.nextPageToLoad = nextPageToLoad;
        updateButtonState();
    }

    @Override
    public void onClick(View view) {
        if (nextPageToLoad == null) {
            Log.e(TAG, "nextPageToLoad is null in LoadMoreView.onClick");
            return;
        }
        pageLoadsInProgress.add(nextPageToLoad);
        mainActivity.loadFrom(nextPageToLoad);
        updateButtonState();
    }

    public void updateButtonState() {
        if (nextPageToLoad == null) {
            Log.e(TAG, "nextPageToLoad is null in LoadMoreView.updateButtonState");
            return;
        }
        if (pageLoadsInProgress.contains(nextPageToLoad)) {
            buttonLoadMore.setText("Loading...");
            buttonLoadMore.setEnabled(false);
        } else {
            buttonLoadMore.setText("Load feed here...");
            buttonLoadMore.setEnabled(true);
        }
    }
}
