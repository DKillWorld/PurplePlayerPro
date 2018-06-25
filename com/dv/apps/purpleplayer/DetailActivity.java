package com.dv.apps.purpleplayer;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.PlaybackParams;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dv.apps.purpleplayer.ListAdapters.SongAdapter;
import com.dv.apps.purpleplayer.Models.Song;
import com.dv.apps.purpleplayer.Utils.OnSwipeTouchListener;
import com.dv.apps.purpleplayer.Utils.PurpleColorHelper;
import com.dv.apps.purpleplayer.Utils.PurpleHelper;
import com.github.florent37.viewanimator.ViewAnimator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.shape.CircleShape;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;

import static com.dv.apps.purpleplayer.MusicService.looping;
import static com.dv.apps.purpleplayer.MusicService.randomize;
import static com.dv.apps.purpleplayer.MusicService.userStopped;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    TextView textView1, textView2, timer1, timer2;
    ImageView imageView, rootBackground;
    ImageButton playPause, loop, next, prev, shuffle, showLyrics;
    int currentPrimaryColor;
    SeekBar seekBar;

    Handler seekHandler;

    SharedPreferences preferences;

    ShareActionProvider shareActionProvider;

    private MediaBrowserCompat mediaBrowserCompat;
    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            MediaSessionCompat.Token token = mediaBrowserCompat.getSessionToken();
            MediaControllerCompat mediaControllerCompat = null;
            try {
                mediaControllerCompat = new MediaControllerCompat(DetailActivity.this, token);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            MediaControllerCompat.setMediaController(DetailActivity.this, mediaControllerCompat);
            buildTransportControls();
        }
    };
    private MediaControllerCompat.Callback mediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING){
                playPause.setImageResource(R.mipmap.ic_pause);
            }else if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                    | state.getState() == PlaybackStateCompat.STATE_STOPPED){
                playPause.setImageResource(R.mipmap.ic_play);
            }
            updateSeekbar();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            //Setting up Title
            textView1.setText(metadata.getDescription().getTitle());

            //Setting up Artist
            textView2.setText(metadata.getDescription().getSubtitle());

            //Setting up AlmubArt

            //Old code for ImageLoading
//            imageView.setImageURI(musicService.getSong().getImage());
//            if (imageView.getDrawable() == null) {
//                imageView.setImageResource(R.mipmap.ic_launcher_web);
//            }

//            Glide.with(getApplicationContext())
//                    .load(metadata.getDescription().getIconUri())
//                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
//                    .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(imageView);

            Picasso.with(DetailActivity.this)
                    .load(metadata.getDescription().getIconUri())
                    .transform(new jp.wasabeef.picasso.transformations.RoundedCornersTransformation(20, 0))
                    .placeholder(R.mipmap.ic_launcher_web)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
//                            if (preferences.getBoolean("auto_color", false)) {
//                                getAutoColor();
//                            }
                            getAutoBackground();
                        }

                        @Override
                        public void onError() {
                            getAutoBackground();
                        }
                    });

            if (preferences.getBoolean("Animate_Albumart", true)) {
                ViewAnimator
                        .animate(imageView)
                        .flipVertical()
                        .decelerate()
                        .duration(1000)
                        .start();
            }

        }
    };

    public void getAutoColor(){
        if (imageView.getDrawable() != null){
            Palette palette = PurpleColorHelper.generatePalette(((BitmapDrawable) imageView.getDrawable()).getBitmap());

            int color = PurpleColorHelper.getColor(palette, MainActivity.PRIMARY_COLOR_DEFAULT);

//            int vibrant = palette.getVibrantSwatch().getRgb();
//            int muted = palette.getMutedSwatch().getRgb();
//            int darkVibrant = palette.getDarkVibrantSwatch().getRgb();
//            int darkMuted = palette.getDarkMutedSwatch().getRgb();
//            int lightVibrant = palette.getLightVibrantSwatch().getRgb();
//            int lightMuted = palette.getLightMutedSwatch().getRgb();

            Aesthetic.get()
                    .colorPrimary(color)
                    .colorStatusBarAuto()
                    .colorNavigationBarAuto()
                    .apply();

            currentPrimaryColor = color;
        }
    }

    public void getAutoBackground(){
        if (preferences.getBoolean("Use_Root_Background", false)) {
//                Glide.with(getApplicationContext())
//                        .load(metadata.getDescription().getIconUri())
//                        .apply(new RequestOptions().placeholder(rootBackground.getDrawable()).error(R.mipmap.background_list))
//                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(rootBackground);

            Picasso.with(getApplicationContext())
                    .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                    .fit()
                    .error(new ColorDrawable(currentPrimaryColor))
                    .placeholder(rootBackground.getDrawable())
                    .transform(new BlurTransformation(getApplicationContext(), 20))
                    .into(rootBackground);
        }else{
//                Glide.with(getApplicationContext())
//                        .load(R.mipmap.background_list)
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(rootBackground);
//                if  (Build.VERSION.SDK_INT >= 21 && (rootBackground.getDrawable() != null))  {
//                    rootBackground.setColorFilter(Aesthetic.get().colorPrimary().blockingFirst(), PorterDuff.Mode.OVERLAY);
//                }

            Picasso.with(getApplicationContext())
                    .load(R.mipmap.background_list)
                    .fit()
                    .transform(new ColorFilterTransformation(ColorUtils.setAlphaComponent(currentPrimaryColor, 100)))
                    .into(rootBackground);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aesthetic.attach(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nowPlaying);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentPrimaryColor = Aesthetic.get().colorPrimary().blockingFirst();

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        mediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), connectionCallback, null);

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
//        getWindow().getDecorView().setBackgroundResource(R.mipmap.background_list);
//        getWindow().getDecorView().getBackground().setColorFilter(new ColorDrawable(preferences.getInt("primary_color", PRIMARY_COLOR_DEFAULT)).getColor(), PorterDuff.Mode.ADD);

    }

    public void buildTransportControls(){
        final MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(DetailActivity.this);
        MediaMetadataCompat mediaMetadataCompat = mediaControllerCompat.getMetadata();
        PlaybackStateCompat playbackStateCompat = mediaControllerCompat.getPlaybackState();
        mediaControllerCompat.registerCallback(mediaControllerCallback);

        //Title and Artise Textview, Album Art ImageView
        textView1 = (TextView) findViewById(R.id.titleDetail);
        textView1.setText(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getTitle());

        textView2 = (TextView) findViewById(R.id.artistDetail);
        textView2.setText(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getSubtitle());

        imageView = (ImageView) findViewById(R.id.albumArt);

        imageView.setOnTouchListener(new OnSwipeTouchListener(DetailActivity.this) {
            public void onSwipeRight() {
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    if (preferences.getBoolean("Inverse_Swipe", false)) {
                        MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().skipToPrevious();
                    }else {
                        MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().skipToNext();
                    }
                }
            }
            public void onSwipeLeft() {
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    if (preferences.getBoolean("Inverse_Swipe", false)) {
                        MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().skipToNext();
                    }else {
                        MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().skipToPrevious();
                    }
                }
            }

            public void onSwipeBottom() {
                if (MusicService.getInstance().mediaSessionCompat.isActive()) {
                    MaterialDialog dialog = new MaterialDialog.Builder(DetailActivity.this)
                            .customView(R.layout.now_playing, false)
                            .cancelable(true)
                            .show();
                    ListView nowPlayingList = dialog.getCustomView().findViewById(R.id.now_playing_list);
                    GridView nowPlayingGrid = dialog.getCustomView().findViewById(R.id.now_playing_grid);
                    final SongAdapter songAdapter = new SongAdapter(DetailActivity.this, MusicService.getInstance().songList);
                    if (preferences.getBoolean("show_track_as", true)){
                        nowPlayingGrid.setVisibility(View.VISIBLE);
                        nowPlayingList.setVisibility(View.GONE);
                        nowPlayingGrid.setAdapter(songAdapter);
                        nowPlayingGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Song tempSong = songAdapter.getItem(position);
                                Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, tempSong.getId());
                                Bundle bundle = new Bundle();
                                bundle.putInt("Pos", MusicService.getInstance().songList.indexOf(tempSong));
                                MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls()
                                        .playFromUri(playUri, bundle);
                            }
                        });
                    }else {
                        nowPlayingList.setVisibility(View.VISIBLE);
                        nowPlayingGrid.setVisibility(View.GONE);
                        nowPlayingList.setAdapter(songAdapter);
                        nowPlayingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Song tempSong = songAdapter.getItem(position);
                                Uri playUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, tempSong.getId());
                                Bundle bundle = new Bundle();
                                bundle.putInt("Pos", MusicService.getInstance().songList.indexOf(tempSong));
                                MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls()
                                        .playFromUri(playUri, bundle);
                            }
                        });
                    }

//                    FloatingActionButton floatingActionButton = dialog.getCustomView().findViewById(R.id.playFab);
//                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Collections.shuffle(MusicService.getInstance().songList);
//                            songAdapter.notifyDataSetChanged();
//                        }
//                    });
                }
            }

            public void onSwipeTop() {
                if (BuildConfig.APPLICATION_ID.equals("com.dv.apps.purpleplayerpro")) {
                    if (MusicService.getInstance().mediaSessionCompat.isActive()) {
                        MaterialDialog dialog = new MaterialDialog.Builder(DetailActivity.this)
                                .customView(R.layout.tag_editor_activity, true)
                                .cancelable(true)
                                .positiveText(R.string.ok)
                                .title(R.string.simple_tag_editor)
                                .show();
                        final EditText eT1 = dialog.getCustomView().findViewById(R.id.editText);
                        final EditText eT2 = dialog.getCustomView().findViewById(R.id.editText2);

                        eT1.setText(MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getDescription().getTitle());
                        eT2.setText(MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getDescription().getSubtitle());

                        final String textToSearch = eT1.getText().toString();

                        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.Audio.Media.TITLE, eT1.getText().toString());
                                contentValues.put(MediaStore.Audio.Media.ARTIST, eT2.getText().toString());

                                    boolean success = getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues,
                                            MediaStore.Audio.Media.TITLE + "= \"" + textToSearch + "\"", null) == 1;
                                    if (success) {
                                        MusicService.getInstance().getSong().setTile(eT1.getText().toString());
                                        MusicService.getInstance().getSong().setArtist(eT2.getText().toString());
                                    }
                                    MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().pause();
                                    MediaControllerCompat.getMediaController(DetailActivity.this).getTransportControls().play();
                            }
                        });
                    }
                }else {
//                    Toast.makeText(DetailActivity.this, "Tag editor is pro feature", Toast.LENGTH_SHORT).show();
                    MaterialDialog dialog = new MaterialDialog.Builder(DetailActivity.this)
                            .content("• \"Simple Tag Editor\" is pro feature. \n• You can edit track \"Title\" and \"Artist\" with it." +
                                    "\n• Get \"Simple Tag Editor\" + all transition effects + remove all ads by upgrading to pro.")
                            .cancelable(true)
                            .positiveText(R.string.upgradeToPurplePlayerPro)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=com.dv.apps.purpleplayerpro"));
                                    startActivity(intent);
                                }
                            })
                            .title(R.string.info)
                            .show();
                }
            }
        });
//        Glide.with(getApplicationContext())
//                .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
//                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(30, 0)))
//                .apply(new RequestOptions().placeholder(imageView.getDrawable()).error(R.mipmap.ic_launcher_web))
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(imageView);

        Picasso.with(getApplicationContext())
                .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                .transform(new jp.wasabeef.picasso.transformations.RoundedCornersTransformation(20, 0))
                .placeholder(R.mipmap.ic_launcher_web)
                .into(imageView);

        rootBackground = findViewById(R.id.root_background);
        if (preferences.getBoolean("Use_Root_Background", false)) {
//            Glide.with(getApplicationContext())
//                    .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
//                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(rootBackground);

            Picasso.with(getApplicationContext())
                    .load(MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getIconUri())
                    .error(new ColorDrawable(currentPrimaryColor))
                    .placeholder(rootBackground.getDrawable())
                    .fit()
                    .transform(new BlurTransformation(getApplicationContext(), 20))
                    .into(rootBackground);
        }else{
//            Glide.with(getApplicationContext())
//                    .load(R.mipmap.background_list)
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(rootBackground);
//            if  (Build.VERSION.SDK_INT >= 21 && (rootBackground.getDrawable() != null))  {
//                rootBackground.setColorFilter(Aesthetic.get().colorPrimary().blockingFirst(), PorterDuff.Mode.OVERLAY);
//            }

            Picasso.with(getApplicationContext())
                    .load(R.mipmap.background_list)
                    .fit()
                    .transform(new ColorFilterTransformation(ColorUtils.setAlphaComponent(currentPrimaryColor, 100)))
                    .into(rootBackground);
        }

        //Play Pause Button
        playPause = (ImageButton) findViewById(R.id.playPause);
        if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE ||
                MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED){
            playPause.setImageResource(R.mipmap.ic_play);
        }else {
            playPause.setImageResource(R.mipmap.ic_pause);
        }
        playPause.setOnClickListener(this);
        playPause.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    MaterialDialog dialog = new MaterialDialog.Builder(DetailActivity.this)
                            .customView(R.layout.speed_pitch_control, true)
                            .positiveText(R.string.ok)
                            .negativeText(R.string.reset)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    playPause.performClick();
                                    MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setSpeed(1f));
                                    MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setPitch(1f));
                                    playPause.performClick();

                                }
                            })
                            .show();
                    SeekBar speedBar, pitchBar;
                    speedBar = dialog.getCustomView().findViewById(R.id.seekBar_speed);
                    speedBar.setMax(150);
                    speedBar.setProgress(16);
                    speedBar.incrementProgressBy(25);
                    speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            float f = (progress + 50) / 100f;
                            if (fromUser) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (MusicService.getInstance().mediaPlayer.isPlaying()) {
                                        MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setSpeed((float) (progress + 50) / 100));
                                        MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            int tempMax = (int) MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                            DetailActivity.this.seekBar.setMax((int) (tempMax / MusicService.getInstance().mediaPlayer.getPlaybackParams().getSpeed()));
//                        int tempProgressPercent = DetailActivity.this.seekBar.getProgress()/DetailActivity.this.seekBar.getMax();
//                        DetailActivity.this.seekBar.setProgress((int) (DetailActivity.this.seekBar.getMax() * tempProgressPercent));

                        }
                    });

                    pitchBar = dialog.getCustomView().findViewById(R.id.seekBar_pitch);
                    pitchBar.setMax(150);
                    pitchBar.setProgress(16);
                    pitchBar.incrementProgressBy(25);
                    pitchBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            float f = (progress + 50) / 100f;
                            if (fromUser) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setPitch((float) (progress + 50) / 100));
                                    MusicService.getInstance().mediaPlayer.setPlaybackParams(MusicService.getInstance().mediaPlayer.getPlaybackParams().setAudioFallbackMode(PlaybackParams.AUDIO_FALLBACK_MODE_DEFAULT));

                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                }else {
                    Toast.makeText(DetailActivity.this, R.string.requires_version6, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        //Loop Button
        loop = (ImageButton) findViewById(R.id.loop);
        if (looping){
            loop.setBackgroundResource(R.drawable.background_button_selected);
        }
        loop.setOnClickListener(this);

        //shuffle Button
        shuffle = (ImageButton) findViewById(R.id.shuffle);
        if (randomize){
            shuffle.setBackgroundResource(R.drawable.background_button_selected);
        }
        shuffle.setOnClickListener(this);

        showLyrics = (ImageButton) findViewById(R.id.showLyrics);
        showLyrics.setOnClickListener(this);


        //Timer Views
        timer1 = (TextView) findViewById(R.id.timer1);
        timer2 = (TextView) findViewById(R.id.timer2);
        setShowTimer();

        //Seekbar
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        updateSeekbar();

        //Next Song Button
        next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(this);

        //Prev Song Button
        prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(this);

        //Setup Tutorial
        setupTutorial();

    }

    //Seekbar Mechanism
    public void updateSeekbar() {
        seekBar.setProgress((int) MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPosition());
        seekBar.setMax((int) MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        final boolean showTimer = preferences.getBoolean("Show_Timer", false);
        final long hours = TimeUnit.MILLISECONDS.toHours(seekBar.getMax());
        if (seekHandler == null){
            seekHandler = new Handler();
        }
        seekHandler.removeMessages(0);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaBrowserCompat.isConnected()){
                    if (MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
                            MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_FAST_FORWARDING) {
                        int current = (int) MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPosition();
                        long timeDelta = SystemClock.elapsedRealtime() - MediaControllerCompat.getMediaController(DetailActivity.this)
                                .getPlaybackState().getLastPositionUpdateTime();
                        current += timeDelta * MediaControllerCompat.getMediaController(DetailActivity.this).getPlaybackState().getPlaybackSpeed();
                        if (current > MediaControllerCompat.getMediaController(DetailActivity.this).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION)) {

                        }
                        seekBar.setProgress(current);
                        seekHandler.postDelayed(this, 1000);

                        if (showTimer) {
                            if (hours == 0) {
                                timer1.setText(String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(current) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(current)), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(current) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current))));
                                timer2.setText(String.format("%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(seekBar.getMax()) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(seekBar.getMax())), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(seekBar.getMax()) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekBar.getMax()))));
                            }else {
                                timer1.setText(String.format("%02d:%02d:%02d",
                                        TimeUnit.MILLISECONDS.toHours(current),
                                        TimeUnit.MILLISECONDS.toMinutes(current) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(current)), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(current) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current))));
                                timer2.setText(String.format("%02d:%02d:%02d",
                                        TimeUnit.MILLISECONDS.toHours(seekBar.getMax()),
                                        TimeUnit.MILLISECONDS.toMinutes(seekBar.getMax()) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(seekBar.getMax())), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(seekBar.getMax()) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekBar.getMax()))));
                            }
                        }
                    }
                }
            }
        });
    }

    public void setupTutorial(){
        ShowcaseConfig configCircle = new ShowcaseConfig();
        ShowcaseConfig configRect = new ShowcaseConfig();

        configCircle.setMaskColor(Color.WHITE);
        configCircle.setShape(new CircleShape(10));
        configCircle.setShapePadding(10);
        configCircle.setDelay(500); // half second between each showcase view

        configRect.setMaskColor(Color.WHITE);
        configRect.setShape(new RectangleShape(0,0));
        configRect.setShapePadding(10);
        configRect.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "ShowIdDetail1");

        sequence.setConfig(configRect);

        sequence.addSequenceItem(findViewById(R.id.linearLayout),
                "Dock","This is your main dock for controls.", "NEXT")
                .setConfig(configCircle);

        sequence.addSequenceItem(findViewById(R.id.playPause),
                "Speed/Pitch Setting","Hold this button to open panel of speed/pitch control. (Requires Android >= 6.0)", "NEXT");

        sequence.addSequenceItem(findViewById(R.id.showLyrics),
                "Lyrics","Get lyrics for currently playing song.", "NEXT");

        sequence.addSequenceItem(findViewById(R.id.equilizerDetail),
                "Equalizer","Optimize audio output according to your mood.", "NEXT");

        sequence.addSequenceItem(findViewById(R.id.settingsDetail),
                "Settings","Customize everything else from here.", "NEXT");

        sequence.addSequenceItem(new View(this),
                "The Big Button","Albumart is gesture controlled big button. \n\n" +
                        "Swipe up : Tag editor \n" +
                        "Swipe down : Now playing tracks list \n" +
                        "Swipe left/right : Change to next/previous tracks \n\n" +
                        "Explore and have fun !!", "GOT IT");


        sequence.start();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.showLyrics:
                //boolean to check if use QuickLyric OR https://api.lyrics.ovh/v1/
                boolean useQuickLyric = preferences.getBoolean(getString(R.string.key_use_quicklyric), true);

                String ArtName = (String) MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getSubtitle();
                String SongName = (String) MediaControllerCompat.getMediaController(this).getMetadata().getDescription().getTitle();
                if ((ArtName == null) || (SongName == null)) {
                    Toast.makeText(this, R.string.nothingIsPlaying, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (useQuickLyric) {
                    boolean qLInstalled = isQLInstalled(getApplicationContext());
                    if (qLInstalled) {
                        startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                .putExtra("TAGS", new String[]{ArtName, SongName}));
                    } else {
                        installQL();
                    }
                } else {
                    String artistAndSongName[] = {ArtName, SongName};
                    FetchLyricsTask fetchLyricsTask = new FetchLyricsTask();
                    fetchLyricsTask.execute(artistAndSongName);
                }

                break;

            case R.id.playPause:
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    if (MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                            MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                            MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                        MediaControllerCompat.getMediaController(this).getTransportControls().play();
                        if (preferences.getBoolean("Animate_Playpause", true)) {
//                            ViewAnimator
//                                    .animate(playPause)
//                                    .rollOut()
//                                    .duration(300)
//                                    .accelerate()
//                                    .thenAnimate(playPause)
//                                    .rollIn()
//                                    .duration(200)
//                                    .decelerate()
//                                    .start();
                            ViewAnimator
                                    .animate(playPause)
                                    .rotation(360)
                                    .decelerate()
                                    .duration(300)
                                    .start();
                        }
                        userStopped = false;
                    } else {
                        MediaControllerCompat.getMediaController(this).getTransportControls().pause();
                        if (preferences.getBoolean("Animate_Playpause", true)) {
//                            ViewAnimator
//                                    .animate(playPause)
//                                    .rollOut()
//                                    .duration(300)
//                                    .accelerate()
//                                    .thenAnimate(playPause)
//                                    .rollIn()
//                                    .duration(200)
//                                    .decelerate()
//                                    .start();
                            ViewAnimator
                                    .animate(playPause)
                                    .rotation(0)
                                    .decelerate()
                                    .duration(300)
                                    .start();
                        }
                        userStopped = true;
                    }
                }else {
                    Toast.makeText(this, R.string.emptyPlaylist, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.loop:
                if (!looping){
                    loop.setBackgroundResource(R.drawable.background_button_selected);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                    Toast.makeText(getApplicationContext(), R.string.repeatOn, Toast.LENGTH_SHORT).show();
                }else{
                    loop.setBackgroundResource(R.drawable.background_buttons);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                    Toast.makeText(getApplicationContext(), R.string.repeatOff, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.shuffle:
                if (!randomize){
                    shuffle.setBackgroundResource(R.drawable.background_button_selected);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setShuffleModeEnabled(true);
                    Toast.makeText(getApplicationContext(), R.string.shuffleOn, Toast.LENGTH_SHORT).show();
                }else{
                    shuffle.setBackgroundResource(R.drawable.background_buttons);
                    MediaControllerCompat.getMediaController(this).getTransportControls().setShuffleModeEnabled(false);
                    Toast.makeText(getApplicationContext(), R.string.shuffleOff, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.next:
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    MediaControllerCompat.getMediaController(this).getTransportControls().skipToNext();
                }else {
                    Toast.makeText(this, R.string.emptyPlaylist, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.prev:
                if ((MusicService.getInstance().songList != null) && (MusicService.getInstance().songList.size() != 0)) {
                    MediaControllerCompat.getMediaController(this).getTransportControls().skipToPrevious();
                }else {
                    Toast.makeText(this, R.string.emptyPlaylist, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private static boolean isQLInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.geecko.QuickLyric", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    private void installQL(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.geecko.QuickLyric"));
        startActivity(intent);
    }

    public void setShowTimer(){
        if (timer2 != null && timer1 != null) {
            if (!preferences.getBoolean("Show_Timer", false)) {
                timer1.setVisibility(View.GONE);
                timer2.setVisibility(View.GONE);
            } else {
                timer1.setVisibility(View.VISIBLE);
                timer2.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser){
            if (mediaBrowserCompat.isConnected()){
                MediaControllerCompat.getMediaController(this).getTransportControls().seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);

//        MenuItem item = menu.findItem(R.id.share_action_provider);
//        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        if (shareActionProvider != null){
//            shareActionProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("audio/*").putExtra(Intent.EXTRA_STREAM, "Test"));
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.settingsDetail:
                Intent sIntent = new Intent(this, SettingsActivity.class);
                startActivity(sIntent);
                break;
            case R.id.equilizerDetail:
                Intent bIntent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                bIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                bIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                        MediaControllerCompat.getMediaController(this).getExtras().getInt("AudioSessionId"));
                if (bIntent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(bIntent, 100);
                }else {
                    Toast.makeText(this, R.string.noEqualierFound, Toast.LENGTH_SHORT).show();
                }
                break;
            case android.R.id.home:
                this.finish();
                break;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserCompat.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowserCompat.disconnect();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    @Override
    protected void onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Aesthetic.pause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Aesthetic.resume(this);
        currentPrimaryColor = Aesthetic.get().colorPrimary().blockingFirst();
        setShowTimer();
        super.onResume();

    }

    /**
     * Created by Dhaval on 31-10-2017.
     * To get lyrics by lyrics.ovh
     */

    private class FetchLyricsTask extends AsyncTask<String, Integer, String> {

        HttpURLConnection urlConnection;
        MaterialDialog progressDialog, lyricsDialog;
        String artName, songName;
        int requestCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new MaterialDialog.Builder(DetailActivity.this)
                    .title(R.string.checkingForLyrics)
                    .progress(true, 0)
                    .content(R.string.pleaseWait)
                    .cancelable(true)
                    .canceledOnTouchOutside(true)
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(true);
                        }
                    })
                    .show();
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            artName = args[0]; //Used to fire up Quicklyric
            songName = args[1]; //Used to fire up QuickLyric

            Uri lyricsBaseUri = Uri.parse(PurpleHelper.getInstance().lyricsServer);
            Uri lyricsUri = lyricsBaseUri.buildUpon().appendPath(args[0]).appendPath(args[1]).build();

            try {
                URL url = new URL(lyricsUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setConnectTimeout(2000);
//                urlConnection.setReadTimeout(2000);
//                requestCode = urlConnection.getResponseCode();
//                if (requestCode == 200) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
//                }else {
//                    result = null;
//                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (result == null || result.length() == 0){
                lyricsDialog = new MaterialDialog.Builder(DetailActivity.this)
                        .title(R.string.oops)
                        .content(R.string.noLyricsFoundTryQuickLyric)
                        .negativeText(R.string.okay)
                        .positiveText("QuickLyric")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                boolean qLInstalled = isQLInstalled(getApplicationContext());
                                if (qLInstalled) {
                                    startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                            .putExtra("TAGS", new String[]{artName, songName}));
                                } else {
                                    installQL();
                                }
                            }
                        })
                        .show();
            }else {

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.has("lyrics")) {
                        String s = jsonObject.getString("lyrics");
                        lyricsDialog = new MaterialDialog.Builder(DetailActivity.this)
                                .title(R.string.lyrics)
                                .content(s)
                                .contentGravity(GravityEnum.CENTER)
                                .neutralText("QuickLyric")
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                        boolean qLInstalled = isQLInstalled(getApplicationContext());
                                        if (qLInstalled) {
                                            startActivity(new Intent("com.geecko.QuickLyric.getLyrics")
                                                    .putExtra("TAGS", new String[]{artName, songName}));
                                        } else {
                                            installQL();
                                        }
                                    }
                                })
                                .positiveText(R.string.great)
                                .show();
                    } else {
                        Toast.makeText(DetailActivity.this, R.string.noLyricsFoundTryQuickLyric, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
