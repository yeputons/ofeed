package net.yeputons.ofeed;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.vk.sdk.api.model.VKApiPost;
import net.yeputons.ofeed.db.CachedGroup;
import net.yeputons.ofeed.db.CachedUser;
import net.yeputons.ofeed.db.DbHelper;
import net.yeputons.ofeed.db.WebResourcesCache;
import net.yeputons.ofeed.web.DownloadCompleteListener;
import net.yeputons.ofeed.web.ResourceDownload;
import net.yeputons.ofeed.web.WebResource;

import java.net.URI;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PostView extends LinearLayout {
    private static final String TAG = PostView.class.getName();
    private final TextView postAuthorName;
    private final TextView postText;
    private final TextView postDate;
    private final DownloadableImageView postAuthorPhoto;

    private final View postCopy;
    private final TextView postCopyAuthorName;
    private final TextView postCopyText;
    private final TextView postCopyDate;
    private final DownloadableImageView postCopyAuthorPhoto;

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

        postCopy = findViewById(R.id.postCopy);
        postCopyAuthorName = (TextView) findViewById(R.id.postCopyAuthorName);
        postCopyText = (TextView) findViewById(R.id.postCopyText);
        postCopyDate = (TextView) findViewById(R.id.postCopyDate);
        postCopyAuthorPhoto = (DownloadableImageView) findViewById(R.id.postCopyAuthorPhoto);

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
        }
    }

    private static PostAuthorInformation getAuthorInformation(int from_id) {
        if (from_id >= 0) {
            CachedUser user = null;
            try {
                user = DbHelper.get().getCachedUserDao().queryForId(from_id);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load user from db", e);
            }
            if (user != null) {
                return new PostAuthorInformation(user.first_name + " " + user.last_name, user.photo_100);
            }
        } else {
            CachedGroup group = null;
            try {
                group = DbHelper.get().getCachedGroupDao().queryForId(-from_id);
            } catch (SQLException e) {
                Log.e(TAG, "Unable to load group from db", e);
            }
            if (group != null) {
                return new PostAuthorInformation(group.name == null ? "N/A" : group.name, group.photo_100);
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
