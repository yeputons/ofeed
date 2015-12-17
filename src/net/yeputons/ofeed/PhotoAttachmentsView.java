package net.yeputons.ofeed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;

import java.util.ArrayList;

public class PhotoAttachmentsView extends TableLayout {
    private final TableRow row;

    public PhotoAttachmentsView(Context context) {
        this(context, null);
    }

    public PhotoAttachmentsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.photo_attachments_view, this);
        row = (TableRow) findViewById(R.id.photoAttachmentsRow);
    }

    public void setAttachments(VKAttachments attachments) {
        setStretchAllColumns(false);
        row.removeAllViews();
        setVisibility(GONE);
        if (attachments == null) {
            return;
        }

        ArrayList<VKApiPhoto> photos = new ArrayList<>();
        for (VKAttachments.VKApiAttachment a : attachments) {
            if (a instanceof VKApiPhoto) {
                photos.add((VKApiPhoto) a);
                if (photos.size() >= 2) {
                    break;
                }
            }
        }

        if (photos.isEmpty()) {
            return;
        }

        setVisibility(VISIBLE);
        for (VKApiPhoto p : photos) {
            DownloadableImageView imageView = new DownloadableImageView(getContext());
            imageView.setDownloadableImageUri(photos.size() == 1 && p.photo_1280 != null && !p.photo_1280.isEmpty() ? p.photo_1280 : p.photo_604);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            params.leftMargin = 4;
            params.rightMargin = 4;
            imageView.setLayoutParams(params);
            row.addView(imageView);
        }
        if (!photos.isEmpty()) {
            setStretchAllColumns(true);
        }
    }
}
