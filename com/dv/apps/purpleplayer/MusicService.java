package com.dv.apps.purpleplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.Utils.Library;
import com.dv.apps.purpleplayer.Utils.MediaStyleHelper;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.dv.apps.purpleplayer.Utils.C.MEDIA_SESSION_TAG;

/**
 * Created by Dhaval on 18-09-2017.
 */

public class MusicService extends MediaBrowserServiceCompat implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    MediaPlayer mediaPlayer;
    public ArrayList<Song> songList;
    public ArrayList<Song> globalSongList;
    public int songPosn;
    public int[] lastSongPosn = new int[]{-1, -1, -1};
    static boolean systemStopped = false;
    public static boolean userStopped = false;
    static boolean randomize = false;
    static boolean looping = false;

    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    BroadcastReceiver becomingNoisyReceiver;

    MediaSessionCompat mediaSessionCompat;
    PlaybackStateCompat.Builder playbackStateBuilder;
    MediaMetadataCompat.Builder metadataBuilder;

    private static final int NOTIFY_ID = 1;

    SharedPreferences preferences;
    public static final String MUSICSERVICE_PREFERENCES = "MusicService_Preferences";
    public static final String SONG_POSITION = "Song_Pos";
    public static final String SHUFFLE_STATUS = "Shuffle_Status";
    public static final String REPEAT_STATUS = "Repeat_Status";
    public static boolean PERMISSION_GRANTED = false;

    public static MusicService musicService;

    //Exoplayer Implementation
    Player player;
    private PlayerNotificationManager playerNotificationManager;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private Library mediaLibrary;
    private ConcatenatingMediaSource cms;
    private ArrayList<Song> songList;

    private static boolean IS_STREAMING = false;
    public static String KEY_REPEATMODE = "key_repeatmode";
    public static String KEY_SHUFFLEMODE = "key_shufflemode";
    public static String KEY_GAPLESS = "key_gapless";
    public static boolean AUTO_PLAY_ON_FOCUS_GAIN = false;
    public static int PREV_AUDIOSESSION_ID;
    public static int COLUMNS_COUNT;

    AudioRendererEventListener audioRendererEventListener = new AudioRendererEventListener() {
        @Override
        public void onAudioEnabled(DecoderCounters counters) {}

        @Override
        public void onAudioSessionId(int audioSessionId) {
            //Equalizer Code

            if (PREV_AUDIOSESSION_ID != 0) {
                Intent audioFx = new Intent();
                audioFx.setAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                audioFx.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
                audioFx.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, PREV_AUDIOSESSION_ID);
                sendBroadcast(audioFx);
            }

            Intent audioFx = new Intent();
            audioFx.setAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            audioFx.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getApplicationContext().getPackageName());
            audioFx.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
            sendBroadcast(audioFx);
            PREV_AUDIOSESSION_ID = audioSessionId;
        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {}

        @Override
        public void onAudioInputFormatChanged(Format format) {}

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {}

        @Override
        public void onAudioDisabled(DecoderCounters counters) {}
    };


    public static MusicService getInstance(){
        if (musicService == null){
            musicService = new MusicService();
        }
        return musicService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = this;
        musicService = this;

        //Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Mediasession
        mediaSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG);
        mediaSession.setActive(true);

        //Fetching Songs
        mediaLibrary = new Library();
        songList = mediaLibrary.getSongs(this);

        initMusicPlayer();
        initMediaSession();
        getAudioFocus();

        becomingNoisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //pause audio on ACTION_AUDIO_BECOMING_NOISY
                pausePlayer();
            }
        };

        preferences = getSharedPreferences(MUSICSERVICE_PREFERENCES, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);
        getPreferences();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        }
        return START_NOT_STICKY;
    }


    public int getAudioFocus(){

        //AudioManager Code
        if (onAudioFocusChangeListener == null) {
            onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.setVolume(0.2f, 0.2f);
                            }
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            if (mediaSessionCompat.isActive()) {
                                pausePlayer();
                                systemStopped = true;
                            }
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS):
                            if (mediaSessionCompat.isActive()) {
                                pausePlayer();
                                systemStopped = false;
                            }
                            stopSelf();
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN):
                            mediaPlayer.setVolume(1f, 1f);
                            if (systemStopped && !userStopped) {
                                if (mediaSessionCompat.isActive()) {
                                    startPlayer();
                                    systemStopped = false; //resetting variable to default
                                }
                            }
                            break;

                    }

                }
            };
        }


        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int focusResult = audioManager.requestAudioFocus(onAudioFocusChangeListener , AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return focusResult;

    }

    public void initMusicPlayer(){
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        Intent audioFx = new Intent();
        audioFx.setAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        audioFx.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        audioFx.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, mediaPlayer.getAudioSessionId());
        sendBroadcast(audioFx);

    }

    public void initMediaSession(){
        mediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "TAG");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND |
                PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_STOP |
                PlaybackStateCompat.ACTION_SET_REPEAT_MODE | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED);
        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
        mediaSessionCompat.setMetadata(metadataBuilder.build());
        mediaSessionCompat.setCallback(new MediaSessionCallback());
        setSessionToken(mediaSessionCompat.getSessionToken());

        //Set Extra Parameters
        Bundle bundle = new Bundle();
        bundle.putInt("AudioSessionId", mediaPlayer.getAudioSessionId());
        mediaSessionCompat.setExtras(bundle);

        //MediaButtonReceiver
        Intent mediaButoonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButoonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButoonIntent, 0);
        mediaSessionCompat.setMediaButtonReceiver(pendingIntent);

    }

    public void updatePreferences(SharedPreferences.Editor preference){
        preference.putInt(SONG_POSITION, songPosn);
        preference.putBoolean(SHUFFLE_STATUS, randomize);
        preference.putBoolean(REPEAT_STATUS, looping);
        preference.apply();
    }

    public void setSongList(ArrayList<Song> tempSongList){
        songList = tempSongList;
    }

    public void setGlobalSongList(ArrayList<Song> songs){
        globalSongList = new ArrayList<Song>();
        globalSongList.addAll(songs);
        if (songList == null) {
            songList = new ArrayList<Song>();
            songList.addAll(songs);
        }
    }

    public Song getSong(){
        return songList.get(songPosn);
    }

    public void getPreferences(){
        if (preferences != null){
            randomize = preferences.getBoolean(SHUFFLE_STATUS, false);
            looping = preferences.getBoolean(REPEAT_STATUS, false);
            songPosn = preferences.getInt(SONG_POSITION, 0);
        }
    }

    public void playSong(){
        mediaPlayer.reset();
        Song sampleSong = getSong();
        long currentSong = sampleSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer.getCurrentPosition(), 1.0f).build());
        updateNotification();
        updateWidget();
    }

    public void startPlayer(){
        if (getAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            startService(new Intent(getApplicationContext(), MusicService.class)); //Starting Foreground service
            mediaSessionCompat.setActive(true); //Activating mediasession
            mediaPlayer.start(); //Starting player
            registerBecomingNoisyReceiver(); //Registering receiver

            //Setting playbackState
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer.getCurrentPosition(),1.0f).build());

            //Setting metadata
            mediaSessionCompat.setMetadata(metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSong().getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getSong().getArtist())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, getSong().getImageBitmap())
                    .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, getSong().getImage().toString())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getSong().getImageBitmap())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, getSong().getImage().toString())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getSong().getDuration()).build());

            //Starting Notification
            startForeground(NOTIFY_ID, setupNotification());

            updateWidget();
        }

        //Updating Preferences
        updatePreferences(preferences.edit());
        updateNotification();
    }

    public void playPrev(){
        if (randomize){
            if ((lastSongPosn[0] != -1) && lastSongPosn[0] != songPosn) {
                songPosn = lastSongPosn[0];
                lastSongPosn[0] = lastSongPosn[1];
                lastSongPosn[1] = lastSongPosn[2];
                lastSongPosn[2] = -1;
            }else {
                songPosn--;
            }
        }else {
            songPosn--;
        }
        if (songPosn == -1){
            songPosn = songList.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        if (randomize){
            lastSongPosn[2] = lastSongPosn[1];
            lastSongPosn[1] = lastSongPosn[0];
            lastSongPosn[0] = songPosn;
            songPosn = getRandom();
        }else {
            songPosn++;
        }
        if (songPosn == songList.size()){
            songPosn = 0;
        }
        playSong();
    }

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    public void updateWidget(){
        //Update Widget
        Intent intent = new Intent(this, PurpleWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(getApplication(), PurpleWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mediaSessionCompat.isActive()) {
            if (looping) {
                mediaPlayer.seekTo(0);
                startPlayer();
            } else {
                mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        mediaPlayer.getCurrentPosition(), 1.0f).build());
                playNext();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startPlayer();
    }

    public Notification setupNotification(){
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = MediaStyleHelper.from(getApplicationContext(), mediaSessionCompat);
        builder.setContentIntent(pendingIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(mediaSessionCompat.getController().getMetadata().getDescription().getIconBitmap())
                .setColor(ContextCompat.getColor(this, android.R.color.holo_purple));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous, "Prev", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_rew, "FastReve", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_REWIND)));
        if (mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE){
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)));
        }else {
            builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE)));
        }
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_ff, "FastForw", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_FAST_FORWARD)));
        builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));


        builder.setShowWhen(false);
        builder.setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(2, 4)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSessionCompat.getSessionToken()));
        return builder.build();

    }

    public void updateNotification(){
        if (mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.ACTION_PAUSE) {
            startForeground(NOTIFY_ID, setupNotification());
            stopForeground(false);
        }else if(mediaSessionCompat.getController().getPlaybackState().getState() == PlaybackStateCompat.ACTION_PLAY){
            startForeground(NOTIFY_ID, setupNotification());
        }
    }

    //get random song when randomize/shuffle ON
    public int getRandom(){
        int[] nums = new int[songList.size()];
        for (int i = 0; i < songList.size(); i++){
            nums[i] = i;
        }
        Collections.shuffle(Arrays.asList(nums));
        return new Random().nextInt(nums.length);

    }

    public Song getSongByName (File file){

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getPath());
        String songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (songName == null) {
            File file2 = new File(file.getName());
            String temp = file.getName();
            songName = temp.substring(0, temp.lastIndexOf("."));
        }

        for(Song song : globalSongList) {
            if(song.getTitle().equals(songName)) {
                return song;
            }
        }
        return new Song(this, "Error", 555, 0, "Error", null);
    }

    @Override
    public void onDestroy() {
        if (mediaSessionCompat.isActive()) {
            mediaSessionCompat.setActive(false);
            mediaSessionCompat.release();
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            stopForeground(true);
            preferences.unregisterOnSharedPreferenceChangeListener(this);
            unregisterReceiver(becomingNoisyReceiver);
            updateWidget();
        }

        if (mediaPlayer != null){
            mediaPlayer.pause();
        }

        super.onDestroy();

    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback{
        @Override
        public void onPlay() {
            super.onPlay();
            mediaSessionCompat.setPlaybackState((playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(),
                    1.0f)).build());
            if (mediaSessionCompat.isActive()) {
                if (mediaPlayer.getCurrentPosition() > 0) {
                    startPlayer();
                } else {
                    playSong();
                }
            }else {
                playSong();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
            mediaSessionCompat.setPlaybackState((playbackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,mediaPlayer.getCurrentPosition(),
                    1.0f)).build());
            pausePlayer();
        }

        @Override
        public void onStop() {
            updateWidget();
            stopSelf();

        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            playNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            playPrev();
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            if ((mediaPlayer.getCurrentPosition() + 15000) < mediaPlayer.getDuration()){
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 15000);
            }
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
        }

        @Override
        public void onRewind() {
            super.onRewind();
            if ((mediaPlayer.getCurrentPosition() - 15000) > 0){
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 15000);
            }
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mediaPlayer.seekTo((int) pos);
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
            mediaSessionCompat.setPlaybackState(playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mediaPlayer.getCurrentPosition(), 1.0f).build());
        }

        @Override
        public void onSetShuffleModeEnabled(boolean enabled) {
            super.onSetShuffleModeEnabled(enabled);
            mediaSessionCompat.setShuffleModeEnabled(enabled);
            if (enabled){
                randomize = true;
            }else {
                randomize = false;
            }
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            mediaSessionCompat.setRepeatMode(repeatMode);
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                looping = true;
                mediaPlayer.setLooping(true);

            }else {
                mediaPlayer.setLooping(false);
                looping = false;
            };

        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
            boolean found = false;
            int pos = 0;
            for (int i = 0; i < songList.size(); i++) {
                if (songList.get(i).getTitle().equals(query)) {
                    pos = i;
                    found = true;
                    break;
                }
            }
            mediaPlayer.reset();
            Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songList.get(pos).getId());
            Bundle bundle = new Bundle();
            bundle.putInt("Pos", pos);
            onPlayFromUri(playUri, bundle);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            songPosn = extras.getInt("Pos");
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(getApplicationContext(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
            mediaSessionCompat.setPlaybackState((playbackStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1.0f)).build());

//            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
//            metadataRetriever.setDataSource(getApplicationContext(), uri);
//            String name = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            String album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//            String genre = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

        }
    }

}
