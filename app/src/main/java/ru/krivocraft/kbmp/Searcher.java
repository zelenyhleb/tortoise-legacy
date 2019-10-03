package ru.krivocraft.kbmp;

import android.content.Context;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import ru.krivocraft.kbmp.api.TracksStorageManager;

public class Searcher {

    private TracksStorageManager tracksStorageManager;

    public Searcher(Context context) {
        this.tracksStorageManager = new TracksStorageManager(context);
    }

    public List<TrackReference> search(CharSequence string, List<TrackReference> input) {
        List<TrackReference> trackList = new ArrayList<>();
        List<Track> searched = tracksStorageManager.getTracks(input);
        for (Track track : searched) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();

            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.add(input.get(searched.indexOf(track)));
            }
        }
        return trackList;
    }

    public List<Track> searchInTracks(CharSequence string, List<Track> input) {
        List<Track> trackList = new ArrayList<>();
        for (Track track : input) {

            String formattedName = track.getTitle().toLowerCase();
            String formattedArtist = track.getArtist().toLowerCase();
            String formattedSearchStr = string.toString().toLowerCase();
            if (formattedName.contains(formattedSearchStr) || formattedArtist.contains(formattedSearchStr)) {
                trackList.add(input.get(input.indexOf(track)));
            }
        }
        return trackList;
    }
}
