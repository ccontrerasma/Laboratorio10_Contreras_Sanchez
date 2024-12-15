package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class AudioPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "audio_player_channel";
    private boolean isPaused = false;
    private String currentFile = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AudioPlayerService", "Servicio creado");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            switch (action) {
                case "start":
                    startAudio(); // Llamar directamente al método
                    break;
                case "pause":
                    pauseAudio();
                    break;
                case "resume":
                    resumeAudio();
                    break;
                case "stop":
                    stopAudio();
                    break;
            }
        }

        startForeground(NOTIFICATION_ID, createNotification("Preparando audio", R.drawable.ic_play, "Pausar", "pause"));
        return START_STICKY;
    }


    // Método para iniciar la reproducción desde assets
    private void startAudio() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            // Cargar el archivo desde res/raw/audio_file.mp3
            mediaPlayer = MediaPlayer.create(this, R.raw.audio_file);

            if (mediaPlayer == null) {
                Log.e("AudioPlayerService", "Error al cargar el archivo de audio.");
                return;
            }

            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                updateNotification("Reproduciendo audio", R.drawable.ic_pause, "Pausar", "pause");
                Log.d("AudioPlayerService", "Audio iniciado");
            });

            mediaPlayer.setOnCompletionListener(mp -> stopAudio());

            // Si el archivo ya está listo, no es necesario prepareAsync.
            Log.d("AudioPlayerService", "Archivo cargado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AudioPlayerService", "Error al iniciar el audio: " + e.getMessage());
        }
    }


    // Método para pausar la reproducción
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            updateNotification("Audio en pausa", R.drawable.ic_play, "Reanudar", "resume");
            Log.d("AudioPlayerService", "Audio pausado");
        }
    }

    // Método para reanudar la reproducción
    private void resumeAudio() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
            updateNotification("Reproduciendo audio", R.drawable.ic_pause, "Pausar", "pause");
            Log.d("AudioPlayerService", "Audio reanudado");
        }
    }

    // Método para detener la reproducción
    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
            currentFile = null;
            stopForeground(true);
            stopSelf();
            Log.d("AudioPlayerService", "Audio detenido y servicio finalizado");
        }
    }

    // Método para crear una notificación
    private Notification createNotification(String contentText, int iconResId, String actionText, String action) {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Audio Player",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Control de audio")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.audio)
                .addAction(iconResId, actionText, getActionPendingIntent(action))
                .addAction(R.drawable.ic_stop, "Detener", getActionPendingIntent("stop"))
                .build();
    }

    // Método para actualizar la notificación
    private void updateNotification(String contentText, int iconResId, String actionText, String action) {
        Notification notification = createNotification(contentText, iconResId, actionText, action);
        startForeground(NOTIFICATION_ID, notification);
    }

    // Creación de un PendingIntent para las acciones de la notificación
    private PendingIntent getActionPendingIntent(String action) {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

