package com.dv.apps.purpleplayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class PurpleWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.purple_widget);

        PendingIntent openActivity = PendingIntent
                .getActivity(context, 0, new Intent(context.getApplicationContext(), MainActivity.class), 0);

        PendingIntent prev = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        PendingIntent fastRewind = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_REWIND);
        PendingIntent playPause = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE);
        PendingIntent fastForward = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_FAST_FORWARD);
        PendingIntent next = MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);

        views.setOnClickPendingIntent(R.id.widgetImageView, openActivity);

        if ((MusicService.getInstance().mediaSessionCompat != null) && (MusicService.getInstance().mediaSessionCompat.isActive())) {
            views.setImageViewBitmap(R.id.widgetImageView, MusicService.getInstance().getSong().getImageBitmap());

            if (MusicService.getInstance().mediaPlayer.isPlaying()) {
                views.setImageViewResource(R.id.widgetPlayPause, R.drawable.ic_pause_white_24dp);
                views.setTextViewText(R.id.widgetText, MusicService.getInstance().getSong().getTitle());

                views.setOnClickPendingIntent(R.id.widgetPrev, prev);
                views.setOnClickPendingIntent(R.id.widgetFastRewind, fastRewind);
                views.setOnClickPendingIntent(R.id.widgetPlayPause, playPause);
                views.setOnClickPendingIntent(R.id.widgetFastForward, fastForward);
                views.setOnClickPendingIntent(R.id.widgetNext, next);
            } else {
                views.setImageViewResource(R.id.widgetPlayPause, R.drawable.ic_play_arrow_white_24dp);
            }
        }else {
            views.setOnClickPendingIntent(R.id.widgetPrev, openActivity);
            views.setOnClickPendingIntent(R.id.widgetFastRewind, openActivity);
            views.setOnClickPendingIntent(R.id.widgetPlayPause, openActivity);
            views.setOnClickPendingIntent(R.id.widgetFastForward, openActivity);
            views.setOnClickPendingIntent(R.id.widgetNext, openActivity);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

