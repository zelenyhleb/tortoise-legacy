package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Random;

import ru.krivocraft.kbmp.constants.Constants;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private TrackList trackList;
    private Context context;
    private final boolean editingAllowed;

    TracksAdapter(TrackList trackList, Context context, boolean editingAllowed) {
        this.trackList = trackList;
        this.context = context;
        this.editingAllowed = editingAllowed;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_list_item, viewGroup, false);
        return new ViewHolder(root, editingAllowed);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.title.setText(trackList.get(i).getTitle());
        viewHolder.artist.setText(trackList.get(i).getArtist());
        viewHolder.track = trackList.get(i);
        viewHolder.trackList = trackList;
        viewHolder.loadArt();
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(trackList.getTracks(), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(trackList.getTracks(), i, i - 1);
            }
        }
        sendUpdate();
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    void shuffle() {
        long seed = System.nanoTime();
        Collections.shuffle(trackList.getTracks(), new Random(seed));
        notifyDataSetChanged();
    }

    private void sendUpdate() {
        context.sendBroadcast(new Intent(Constants.Actions.ACTION_EDIT_TRACK_LIST).putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson()));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView artist;
        ImageView art;
        Track track;
        TrackList trackList;

        ViewHolder(@NonNull View itemView, boolean editingAllowed) {
            super(itemView);
            title = itemView.findViewById(R.id.composition_name_text);
            artist = itemView.findViewById(R.id.composition_author_text);
            art = itemView.findViewById(R.id.item_track_image);
            itemView.setOnClickListener(new OnClickListener());
            if (editingAllowed) {
                itemView.setOnLongClickListener(new OnLongClickListener());
            }
        }

        void loadArt() {
            art.setClipToOutline(true);

            LoadArtTask loadArtTask = new LoadArtTask();
            loadArtTask.setCallback(art -> {
                if (art != null) {
                    this.art.setImageBitmap(art);
                } else {
                    this.art.setImageDrawable(this.art.getContext().getDrawable(R.drawable.ic_track_image_default));
                }
            });
            loadArtTask.execute(track.getPath());
        }

        private class OnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(Constants.Actions.ACTION_PLAY_FROM_LIST);
                serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK, track.toJson());
                serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, trackList.toJson());
                v.getContext().sendBroadcast(serviceIntent);

                Intent interfaceIntent = new Intent(Constants.Actions.ACTION_SHOW_PLAYER);
                v.getContext().sendBroadcast(interfaceIntent);
            }
        }

        private class OnLongClickListener implements View.OnLongClickListener {

            @Override
            public boolean onLongClick(View v) {
                v.getContext().startActivity(new Intent(v.getContext(), MetadataEditorActivity.class).putExtra(Constants.Extras.EXTRA_TRACK, track.toJson()));
                return true;
            }
        }
    }
}
