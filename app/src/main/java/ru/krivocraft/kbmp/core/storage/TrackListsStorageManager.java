package ru.krivocraft.kbmp.core.storage;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import ru.krivocraft.kbmp.core.track.TrackList;
import ru.krivocraft.kbmp.core.track.TrackReference;
import ru.krivocraft.kbmp.sqlite.DBConnection;

public class TrackListsStorageManager {

    private final DBConnection database;

    public TrackListsStorageManager(@NonNull Context context) {
        this.database = new DBConnection(context);
    }

    public void updateTrackListData(TrackList trackList) {
        database.updateTrackListData(trackList);
    }

    public void updateTrackListContent(TrackList trackList) {
        database.updateTrackListContent(trackList);
    }

    public void clearTrackList(TrackList trackList) {
        database.clearTrackList(trackList);
    }

    public void updateRootTrackList(TrackList trackList) {
        database.updateRootTrackList(trackList);
    }

    public void removeTracks(TrackList trackList, List<TrackReference> references) {
        database.removeTracks(trackList, references);
    }

    public void writeTrackList(TrackList trackList) {
        database.writeTrackList(trackList);
    }

    public void removeTrackList(TrackList trackList) {
        database.removeTrackList(trackList);
    }

    public List<TrackList> readAllTrackLists() {
        return database.getTrackLists();
    }

    public List<TrackList> readTrackLists(boolean sortByTag, boolean sortByAuthor) {
        return database.getTrackLists(sortByTag, sortByAuthor);
    }

    public List<String> getExistingTrackListNames() {
        return database.getTrackListNames();
    }

}