package ru.krivocraft.kbmp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerService extends Service implements Track.OnTrackStateChangedListener {

    private MediaPlayer player;
    private Binder mBinder = new LocalBinder();

    private static boolean running = false;

    private Playlist currentPlaylist;
    private int trackIndex = 0;

    private List<Track.OnTrackStateChangedListener> listeners = new ArrayList<>();

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case Constants.HEADSET_STATE_PLUG_IN:
                            if (getCurrentTrack() != null) {
                                start();
                            }
                            break;
                        case Constants.HEADSET_STATE_PLUG_OUT:
                            stop();
                            break;
                    }

                }
            }
        }
    };

    private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case Constants.ACTION_PLAY:
                    start();
                    break;
                case Constants.ACTION_PAUSE:
                    stop();
                    break;
                case Constants.ACTION_NEXT:
                    nextComposition();
                    break;
                case Constants.ACTION_PREVIOUS:
                    previousComposition();
                    break;
                case Constants.ACTION_CLOSE:
                    dismissNotification();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onTrackStateChanged(Track.TrackState state) {
        updateNotification();
    }

    class LocalBinder extends Binder {
        PlayerService getServerInstance() {
            return PlayerService.this;
        }

    }

    static boolean isRunning() {
        return running;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setCurrentPlaylist(new Playlist(this, "default"));
        addListener(this);

        IntentFilter controlFilter = new IntentFilter();
        controlFilter.addAction(Constants.ACTION_PLAY);
        controlFilter.addAction(Constants.ACTION_PAUSE);
        controlFilter.addAction(Constants.ACTION_NEXT);
        controlFilter.addAction(Constants.ACTION_PREVIOUS);
        controlFilter.addAction(Constants.ACTION_CLOSE);
        registerReceiver(controlReceiver, controlFilter);

        IntentFilter musicFilter = new IntentFilter();
        musicFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, musicFilter);

        running = true;
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        sendBroadcast(new Intent().setAction(Constants.ACTION_PREVIOUS));
        unregisterReceiver(controlReceiver);
        unregisterReceiver(headsetReceiver);
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    public void start() {
        start(getCurrentTrackProgress());
    }

    public void start(final int progress) {

        if (player != null) {
            stop();
        }

        player = new MediaPlayer();
        try {
            player.setDataSource(getCurrentTrack().getPath());
            getCurrentTrack().setSelected(true);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    player.seekTo(progress);
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            nextComposition();
                        }
                    });
                    player.start();
                    setPlaying(true);
                    for (Track.OnTrackStateChangedListener listener : listeners) {
                        listener.onTrackStateChanged(Track.TrackState.PLAY_PAUSE_TRACK);
                    }
                }
            });
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, TortoiseActivity.class).setAction(Constants.ACTION_SHOW_PLAYER), PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent playIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PLAY), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PAUSE), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent nextCompositionIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_NEXT), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent previousCompositionIntent = PendingIntent.getBroadcast(this, 0, new Intent().setAction(Constants.ACTION_PREVIOUS), PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews notificationLayout = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);

        if (isPlaying()) {
            notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_pause);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, pauseIntent);
        } else {
            notificationLayout.setImageViewResource(R.id.notification_play_pause, R.drawable.ic_play);
            notificationLayout.setOnClickPendingIntent(R.id.notification_play_pause, playIntent);
        }

        notificationLayout.setImageViewResource(R.id.notification_next, R.drawable.ic_next);
        notificationLayout.setOnClickPendingIntent(R.id.notification_next, nextCompositionIntent);

        notificationLayout.setImageViewResource(R.id.notification_previous, R.drawable.ic_previous);
        notificationLayout.setOnClickPendingIntent(R.id.notification_previous, previousCompositionIntent);

        notificationLayout.setTextViewText(R.id.notification_composition_author, getCurrentTrack().getArtist());
        notificationLayout.setTextViewText(R.id.notification_composition_name, getCurrentTrack().getName());

        NotificationManager service = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String CHANNEL_ID = "channel_01";

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContent(notificationLayout)
//                .setStyle(new android.support.v4.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
//                .setCustomContentView(notificationLayout)
                .setSound(null)
                .setContentIntent(contentIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tortoise", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setImportance(NotificationManager.IMPORTANCE_LOW);
            notification.setChannelId(CHANNEL_ID);
            if (service != null) {
                service.createNotificationChannel(channel);
            }
        }


        startForeground(Constants.NOTIFY_ID, notification.build());
    }

    private void dismissNotification() {
        stopForeground(true);
    }

    void nextComposition() {
        newComposition(trackIndex + 1);
    }

    void previousComposition() {
        newComposition(trackIndex - 1);
    }

    Track getCurrentTrack() {
        return getCurrentPlaylist().getTrack(trackIndex);
    }

    void newComposition(int compositionIndex) {
        if (compositionIndex >= 0 && compositionIndex < getCurrentPlaylist().getSize()) {
            stop();

            getCurrentTrack().setSelected(false);
            setTrackIndex(compositionIndex);

            getCurrentTrack().setProgress(0);

            for (Track.OnTrackStateChangedListener listener : listeners) {
                listener.onTrackStateChanged(Track.TrackState.NEW_TRACK);
            }

            start();
        }
    }

    void stop() {
        if (player != null) {
            getCurrentTrack().setProgress(player.getCurrentPosition());
            player.stop();

            release();

            setPlaying(false);

            for (Track.OnTrackStateChangedListener listener : listeners) {
                listener.onTrackStateChanged(Track.TrackState.PLAY_PAUSE_TRACK);
            }
        }
    }

    private void release() {
        player.reset();
        player.release();
        player = null;
    }

    boolean isPlaying() {
        return getCurrentTrack().isPlaying();
    }

    private void setPlaying(boolean playing) {
        getCurrentTrack().setPlaying(playing);
    }

    int getPlayerProgress() {
        if (player != null) {
            return player.getCurrentPosition();
        } else {
            return getCurrentTrackProgress();
        }
    }

    private int getCurrentTrackProgress() {
        if (getCurrentTrack() != null) {
            return getCurrentTrack().getProgress();
        } else {
            return 0;
        }
    }

    private void setTrackIndex(int index) {
        this.trackIndex = index;
    }


    Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
        if (currentPlaylist.getSize() > 0) {
            trackIndex = 0;
            setTrackIndex(trackIndex);
        }
    }

    void addListener(Track.OnTrackStateChangedListener listener) {
        listeners.add(listener);
    }

    void removeListener(Track.OnTrackStateChangedListener listener) {
        listeners.remove(listener);
    }
}
