package com.main.anecnotes.utils;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.main.anecnotes.R;
import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<NoteImage> images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(NoteImage image);
        void onImageLongClick(NoteImage image);
    }

    public ImageAdapter(Context context, List<NoteImage> images, OnImageClickListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        NoteImage noteImage = images.get(position);

        // Check if imageUri is not null before loading
        if (noteImage.getImageUri() != null && !noteImage.getImageUri().isEmpty()) {
            try {
                Uri imageUri = Uri.parse(noteImage.getImageUri());
                Glide.with(context)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .into(holder.imageView);
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to error image
                holder.imageView.setImageResource(R.drawable.ic_broken_image);
            }
        } else {
            // Show placeholder if URI is null or empty
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Set caption
        if (noteImage.getCaption() != null && !noteImage.getCaption().isEmpty()) {
            holder.captionText.setText(noteImage.getCaption());
            holder.captionText.setVisibility(View.VISIBLE);
        } else {
            holder.captionText.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && noteImage.getImageUri() != null && !noteImage.getImageUri().isEmpty()) {
                listener.onImageClick(noteImage);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onImageLongClick(noteImage);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return images != null ? images.size() : 0;
    }

    public void updateImages(List<NoteImage> newImages) {
        this.images = newImages;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView captionText;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.note_image_view);
            captionText = itemView.findViewById(R.id.image_caption_text);
        }
    }
}