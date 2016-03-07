package net.yeputons.ofeed;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vk.sdk.api.model.VKApiLink;
import com.vk.sdk.api.model.VKAttachments;
import net.yeputons.ofeed.db.CachedResourceDownload;
import net.yeputons.ofeed.db.CachedWebPage;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.ResourceDownloader;
import net.yeputons.ofeed.web.ResourceOpener;
import net.yeputons.ofeed.web.WebResource;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;

public class LinkAttachmentsView extends LinearLayout {
    private static final String TAG = LinkAttachmentsView.class.getName();
    private final ResourceOpener opener;

    public LinkAttachmentsView(Context context) {
        this(context, null, 0);
    }

    public LinkAttachmentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkAttachmentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        opener = new ResourceOpener(context);
    }

    public void setAttachments(VKAttachments attachments) {
        removeAllViews();
        setVisibility(GONE);
        if (attachments == null) {
            return;
        }

        ArrayList<VKApiLink> links = new ArrayList<>();
        for (VKAttachments.VKApiAttachment a : attachments) {
            if (a instanceof VKApiLink) {
                VKApiLink l = (VKApiLink) a;
                URI uri = URI.create(l.url);
                if (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                    links.add(l);
                }
            }
        }

        if (links.isEmpty()) {
            return;
        }

        setVisibility(VISIBLE);
        for (VKApiLink l : links) {
            TextView view = new TextView(getContext());
            view.setMovementMethod(LinkMovementMethod.getInstance());
            view.setLinkTextColor(Color.BLUE);

            CachedWebPage cachedWebPage = null;
            try {
                cachedWebPage = DbHelper.get().getCachedWebPageDao().queryForId(l.url);
            } catch (SQLException e) {
                Log.e(TAG, "Cannot query cached web page from db", e);
            }
            final WebResource resource = new WebResource(URI.create(l.url));
            if (cachedWebPage != null) {
                final File localFile = new File(cachedWebPage.localFile);
                if (!localFile.canRead()) {
                    Log.w(TAG, "Cached web page disappeared");
                    try {
                        DbHelper.get().getCachedWebPageDao().delete(cachedWebPage);
                    } catch (SQLException e) {
                        Log.e(TAG, "Cannot purge cached web page from db", e);
                    }
                } else {
                    resource.addDownload(new ResourceDownloader() {
                        @NonNull
                        @Override
                        public ResourceDownload createDownload(@NonNull WebResource resource) {
                            return new CachedResourceDownload(localFile);
                        }
                    });
                };
            }
            String title = l.title != null && !l.title.isEmpty() ? l.title : l.url;
            if (resource.getDownloaded() != null) {
                title = "(cached) " + title;
            }
            Spannable text = new SpannableString(title);
            text.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    opener.openResource(resource);
                }
            }, 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            view.setText(text);
            addView(view);
        }
    }
}
