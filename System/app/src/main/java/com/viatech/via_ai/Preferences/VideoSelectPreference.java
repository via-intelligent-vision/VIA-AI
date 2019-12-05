/* /////////////////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//                                 MIT License
//                            Copyright (c) 2019 VIA, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//
// ////////////////////////////////////////////////////////////////////////////////////////////// */

package com.viatech.via_ai.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Looper;
import android.preference.Preference;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viatech.via_ai.R;

import java.io.File;

import static com.viatech.via_ai.Preferences.Utils.postFileDialog;

public class VideoSelectPreference extends Preference {
    private String mVideoPathValue = "";
    private Context mContext;
    private View mView = null;
    private Boolean b_ShowPreview = null;
    private ImageView mUI_PreviewImage;
    private Integer mBkgColor = null;
    private String mFileExtension =".mp4";
    private boolean b_FileSelect = true;
    private int mWidth = 1280;
    private int mHeight = 1280;

    public VideoSelectPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public VideoSelectPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public VideoSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VideoSelectPreference(Context context) {
        super(context);
        init(context);
    }

    private Size getSize() {
        return new Size(mWidth, mHeight);
    }

    private void init(Context context) {
        mContext = context;

        final Preference thiz = this;
        this.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                postFileDialog(mContext, thiz, mFileExtension, b_FileSelect);
                return false;
            }
        });
    }

    public void setPreviewResource(final Drawable drawable) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            if(mUI_PreviewImage != null) {
                mUI_PreviewImage.setImageDrawable(drawable);
            }
        }
        else {
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPreviewResource(drawable);
                }
            });
        }
    }
    public void setPreviewBitmap(final Bitmap bitmap) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            if(mUI_PreviewImage != null) {
                mUI_PreviewImage.setImageBitmap(bitmap);
            }
        }
        else {
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPreviewBitmap(bitmap);
                }
            });
        }
    }

    public String getVideoPathValue() {
        return mVideoPathValue;
    }

    public void setVideoPath(String path) {
        boolean isValid = false;
        boolean isFileExist = false;
        mVideoPathValue = path;

        do {
            if(path == null || path == "") break;

            isFileExist = isFileExist(path);
            if(isFileExist) {
                try {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(path);
                    mWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    mHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    this.setSummary("    Video Path : " + path + " < " + mWidth + " x " + mHeight + " >");
                    persistString((String) path);
                    if (thumb != null) {
                        setPreviewBitmap(thumb);
                    } else {
                        setPreviewResource(mContext.getResources().getDrawable(R.drawable.icon_unknown_video_fmt));
                    }
                    isValid = true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while(false);

        if(!isValid) {
            if(!isFileExist) {
                this.setSummary("    File not exist or permission denied. Click to select a new \".mp4 \" file...");
            }
            else {
                this.setSummary("    Fail to parse bitmap in this file. Click to select a new \".mp4 \" file...");
            }
            persistString("");
            if(isEnabled()) {
                this.setBackgroundColor(Color.rgb(170, 30, 30));
            }
            else {
                this.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        else {
            this.setBackgroundColor(Color.TRANSPARENT);
        }

        if(mView != null) {
            super.onBindView(mView);    // use base class's oBindView to avoid recursive.
        }
    }

    public void setPreviewVisibility(final boolean show) {
        b_ShowPreview = show;
        if(mUI_PreviewImage != null) {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                if (b_ShowPreview) {
                    mUI_PreviewImage.setVisibility(View.VISIBLE);
                } else {
                    mUI_PreviewImage.setVisibility(View.GONE);
                }
            }
            else {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPreviewVisibility(show);
                    }
                });
            }
        }
    }

    public void setBackgroundColor(@ColorInt int color) {
        if(mView != null) {
            mView.setBackgroundColor(color);
        }
        mBkgColor = color;
    }

    public void setDialog(boolean isFileSelector, String fileExtension) {
        b_FileSelect = b_FileSelect;
        mFileExtension = fileExtension;
    }

    @Override
    protected View onCreateView( ViewGroup parent ) {
        super.onCreateView(parent);
        final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(R.layout.layout_preference_video_selector, parent, false);
        mView = layout;
        mUI_PreviewImage = mView.findViewById(R.id.video_preview_image);

        if(mBkgColor != null) {
            mView.setBackgroundColor(mBkgColor);
        }

        if(mUI_PreviewImage != null) {
            if(b_ShowPreview != null)
                setPreviewVisibility(b_ShowPreview);
            else {
                setPreviewVisibility(isEnabled());
            }
        }
        setVideoPath(this.getPersistedString(""));

        return layout;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onBindView(View view) {
        setVideoPath(mVideoPathValue);
        super.onBindView(view);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            // Restore existing state
            mVideoPathValue = this.getPersistedString(mVideoPathValue);
        } else {
            // Set default state from the XML attribute
            persistString((String) defaultValue);
            if(defaultValue != null) mVideoPathValue = (String)defaultValue;
        }
        setVideoPath(mVideoPathValue);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setPreviewVisibility(isEnabled());
    }

    private boolean isFileExist(String path)  {
        boolean ret = false;

        if(path.length() > 0) {
            File file = new File(path);
            if (file.exists() && file.canWrite() && file.canRead()) ret = true;
        }
        return ret;
    }
}
