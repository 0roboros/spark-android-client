package com.sparklounge.client.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sparklounge.client.R;
import com.sparklounge.client.SparkApplication;
import com.sparklounge.client.Utils;
import com.sparklounge.client.interfaces.OnItemClickListener;
import com.sparklounge.client.models.Image;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Chuang on 8/24/2015.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private Context mContext;   // may be used to adjust view
    private List<Image> mImages;
    private OnItemClickListener onItemClickListener;
    private RippleForegroundListener rippleForegroundListener = new RippleForegroundListener(R.id.card_view);
    private int mTextColor;
    private int mBackgroundColor;
    private final LruCache<String, Bitmap> mCache;

    public ImageAdapter() {
        mCache = SparkApplication.getCache();
        mImages = new ArrayList<>();
    }

    public ImageAdapter(List<Image> images) {
        mCache = SparkApplication.getCache();
        this.mImages = images;
    }

    public void updateData(List<Image> images) {
        int oldSize = mImages.size();
        this.mImages = images;
        notifyItemRangeInserted(oldSize, mImages.size());
    }

    private void addImages(List<Image> images) {
        int oldSize = mImages.size();
        this.mImages = images;
        notifyItemRangeInserted(oldSize, mImages.size());
    }

    private void addImage(Image image) {
        if (mCache.get(image.getUri()) != null) {
            this.mImages.add(image);
            notifyItemInserted(mImages.size());
        } else {
            Log.d("", "Tried to load image that is not cached");
        }
    }

    public Image removeImage(int pos) {
        Image imageRemoved = this.mImages.get(pos);
        this.mImages.remove(pos);
        notifyItemRemoved(pos);
        return imageRemoved;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.image_card, viewGroup, false);

        this.mContext = viewGroup.getContext();
        mTextColor = mContext.getResources().getColor(R.color.text_without_palette);
        mBackgroundColor = mContext.getResources().getColor(R.color.text_without_palette);
        return new ImagesViewHolder(itemView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, final int position) {

        // get the image from position
        final Image currentImage = mImages.get(position);
        final String imageLink = currentImage.getUri();

        Bitmap image = mCache.get(imageLink);

        if (image == null) {
            // Image is not cached, need to retrieve
            // TODO: retrieve from local file system
        } else {
            imagesViewHolder.imageView.setImageBitmap(image);
            imagesViewHolder.caption.setText(currentImage.getCaption());
            imagesViewHolder.imageTextContainer.setBackgroundColor(mBackgroundColor);
            if (!image.isRecycled()){
                Palette palette = Palette.generate(image);
                if (palette != null) {
                    Palette.Swatch s = palette.getVibrantSwatch();

                    if (s == null) {
                        s = palette.getLightVibrantSwatch();
                    }
                    if (s == null) {
                        s = palette.getMutedSwatch();
                    }

                    if (s != null && position >= 0 && position < mImages.size()) {
                        Utils.animateViewColor(imagesViewHolder.imageTextContainer, mBackgroundColor, s.getRgb());
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 21) {
                imagesViewHolder.imageView.setTransitionName("cover" + position);
            }
        }
        //imagesViewHolder.itemView.setOnTouchListener(rippleForegroundListener);
    }

    @Override
    public int getItemCount() {
        if (mImages == null) return 0;
        return mImages.size();
    }

}


class ImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final FrameLayout imageTextContainer;
    protected final ImageView imageView;
    protected final TextView caption;
    private final OnItemClickListener onItemClickListener;

    public ImagesViewHolder(View itemView, OnItemClickListener onItemClickListener) {

        super(itemView);
        this.onItemClickListener = onItemClickListener;

        imageTextContainer = (FrameLayout) itemView.findViewById(R.id.caption_container);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        caption = (TextView) itemView.findViewById(R.id.txtCaption);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }

}

class PaletteTransformation implements Transformation {
    private static final PaletteTransformation INSTANCE = new PaletteTransformation();
    private static final Map<Bitmap, Palette> CACHE = new WeakHashMap<>();

    public static PaletteTransformation instance() {
        return INSTANCE;
    }

    public static Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    private PaletteTransformation() {
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Palette palette = Palette.generate(source);
        CACHE.put(source, palette);
        return source;
    }

    @Override
    public String key() {
        return "";
    }
}

