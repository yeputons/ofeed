package net.yeputons.ofeed;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vk.sdk.api.model.VKApiPost;
import net.yeputons.ofeed.db.CachedGroup;
import net.yeputons.ofeed.db.CachedUser;
import net.yeputons.ofeed.db.DbHelper;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;

public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getName();
    private final TextView postAuthorName;
    private final TextView postText;
    private final TextView postDate;
    private final DownloadableImageView postAuthorPhoto;
    private final PhotoAttachmentsView postPhotoAttachments;
    private final LinkAttachmentsView postLinkAttachments;

    private final View postCopy;
    private final TextView postCopyAuthorName;
    private final TextView postCopyText;
    private final TextView postCopyDate;
    private final DownloadableImageView postCopyAuthorPhoto;
    private final PhotoAttachmentsView postCopyPhotoAttachments;
    private final LinkAttachmentsView postCopyLinkAttachments;

    public PostView(Context context) {
        this(context, null, 0);
    }

    public PostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.post, this);
        postAuthorName = (TextView) findViewById(R.id.postAuthorName);
        postText = (TextView) findViewById(R.id.postText);
        postDate = (TextView) findViewById(R.id.postDate);
        postAuthorPhoto = (DownloadableImageView) findViewById(R.id.postAuthorPhoto);
        postPhotoAttachments = (PhotoAttachmentsView) findViewById(R.id.postPhotoAttachments);
        postLinkAttachments = (LinkAttachmentsView) findViewById(R.id.postLinkAttachments);

        postCopy = findViewById(R.id.postCopy);
        postCopyAuthorName = (TextView) findViewById(R.id.postCopyAuthorName);
        postCopyText = (TextView) findViewById(R.id.postCopyText);
        postCopyDate = (TextView) findViewById(R.id.postCopyDate);
        postCopyAuthorPhoto = (DownloadableImageView) findViewById(R.id.postCopyAuthorPhoto);
        postCopyPhotoAttachments = (PhotoAttachmentsView) findViewById(R.id.postCopyPhotoAttachments);
        postCopyLinkAttachments = (LinkAttachmentsView) findViewById(R.id.postCopyLinkAttachments);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PostView, 0, 0);
        try {
            if (!a.getBoolean(R.styleable.PostView_compact, true)) {
                postText.setMaxLines(Integer.MAX_VALUE);
                postText.setEllipsize(null);
                postCopyText.setMaxLines(Integer.MAX_VALUE);
                postCopyText.setEllipsize(null);
            }
        } finally {
            a.recycle();
        }
    }

    private static class PostAuthorInformation {
        @NonNull public final String name;
        @Nullable public final String imageUriStr;

        private PostAuthorInformation(@NonNull String name, @Nullable String imageUriStr) {
            this.name = name;
            this.imageUriStr = imageUriStr;
        }
    }

    public void setPost(@NonNull VKApiPost post) {
        postText.setText(post.text);
        postDate.setText(formatPostDate(post.date));

        PostAuthorInformation author = getAuthorInformation(post.from_id);
        postAuthorPhoto.setDownloadableImageUri(author.imageUriStr);
        postAuthorName.setText(author.name);
        postPhotoAttachments.setAttachments(post.attachments);
        postLinkAttachments.setAttachments(post.attachments);

        if (post.copy_history == null || post.copy_history.size() == 0) {
            postCopy.setVisibility(GONE);
        } else {
            postCopy.setVisibility(VISIBLE);

            VKApiPost copyPost = post.copy_history.get(0);
            postCopyText.setText(copyPost.text);
            postCopyDate.setText(formatPostDate(copyPost.date));
            PostAuthorInformation copyAuthor = getAuthorInformation(copyPost.from_id);
            postCopyAuthorPhoto.setDownloadableImageUri(copyAuthor.imageUriStr);
            postCopyAuthorName.setText(copyAuthor.name);
            postCopyPhotoAttachments.setAttachments(copyPost.attachments);
            postCopyLinkAttachments.setAttachments(copyPost.attachments);
        }
    }

    private static PostAuthorInformation getAuthorInformation(int fromId) {
        if (fromId >= 0) {
            CachedUser user = null;
            try {
                user = DbHelper.get().getCachedUserDao().queryForId(fromId);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load user from db", e);
            }
            if (user != null) {
                return new PostAuthorInformation(user.firstName + " " + user.lastName, user.photo100);
            }
        } else {
            CachedGroup group = null;
            try {
                group = DbHelper.get().getCachedGroupDao().queryForId(-fromId);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load group from db", e);
            }
            if (group != null) {
                return new PostAuthorInformation(group.name == null ? "N/A" : group.name, group.photo100);
            }
        }
        return new PostAuthorInformation("N/A", null);
    }

    private static String formatPostDate(long dateMillis) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateMillis * 1000);

        Calendar curDay = Calendar.getInstance();
        curDay.set(Calendar.HOUR, 0);
        curDay.set(Calendar.MINUTE, 0);
        curDay.set(Calendar.SECOND, 0);

        DateFormat dateFormat = date.after(curDay) ? DateFormat.getTimeInstance() : DateFormat.getDateTimeInstance();
        dateFormat.setCalendar(date);
        return dateFormat.format(date.getTime());
    }
}
