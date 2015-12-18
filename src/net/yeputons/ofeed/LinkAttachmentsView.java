package net.yeputons.ofeed;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.vk.sdk.api.model.VKApiLink;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import net.yeputons.ofeed.web.ResourceOpener;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.util.ArrayList;

public class LinkAttachmentsView extends LinearLayout {
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

            Spannable text = new SpannableString(l.title != null && !l.title.isEmpty() ? l.title : l.url);
            final WebResource resource = new WebResource(URI.create(l.url));
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
