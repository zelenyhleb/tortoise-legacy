package ru.krivocraft.kbmp;

import android.support.v4.app.Fragment;

abstract class AbstractTrackViewFragment extends Fragment implements Track.StateCallback {

    public AbstractTrackViewFragment() {

    }

    abstract void invalidate();

}
