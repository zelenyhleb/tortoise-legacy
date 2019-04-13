package ru.krivocraft.kbmp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class TrackListPage extends AbstractTrackViewFragment {

    private TrackList trackList;
    private AdapterView.OnItemClickListener listener;
    private TrackList.TracksAdapter adapter;
    private ListView listView;
    private boolean showControls;

    public TrackListPage() {
        super();
    }

    @Override
    void invalidate() {
        if (listView != null) {
            listView.invalidateViews();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    void init(TrackList trackList, AdapterView.OnItemClickListener listener, boolean showControls) {
        this.listener = listener;
        this.showControls = showControls;
        this.updateData(trackList);
    }

    void updateData(TrackList trackList) {
        this.trackList = trackList;
        this.adapter = trackList.getTracksAdapter();
        if (listView != null) {
            this.listView.setAdapter(adapter);
        }
        invalidate();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        listView = rootView.findViewById(R.id.fragment_track_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);

        if (showControls){
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    TrackList trackListSearched = Utils.search(s, trackList);
                    listView.setAdapter(trackListSearched.getTracksAdapter());
                    if (s.length() == 0) {
                        listView.setAdapter(adapter);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            buttonShuffle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trackList.shuffle();
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}