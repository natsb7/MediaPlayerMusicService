package com.nataliasep.musicplayerservice;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MyService extends Service {

    private ArrayList<Cancion> listaCanciones;
    private MediaMetadataRetriever retriever;
    public static final String ACTION_PLAY_PAUSE = "PLAY_PAUSE";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";
    public static final String ACTION_LOOP = "LOOP";
    public static final String ACTION_VELOCITY = "VELOCITY";
    private int playingSong;
    private final MyBinder myBinder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private IActionListener listener;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private boolean isLooping = false;


    public void setListener(IActionListener listener) {
        this.listener = listener;
        listaCanciones = getListaCaciones();
        if(listaCanciones != null){
            listener.onArrayRellena();
        }
        Log.d("Mensaje del Service ", "array rellena");
        Log.d("posiciones array", listaCanciones.size()+"");

    }
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        listaCanciones = new ArrayList<>();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Mensaje", "comienza el servicio");
        manejarIntent(intent);
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    int currentPositionSecs = mediaPlayer.getCurrentPosition()/1000;
                    Log.d("msg position", currentPositionSecs+"");
                    int duration = mediaPlayer.getDuration();
                    Log.d("msg duration", duration+"");
                    listener.onTimeUpdate(currentPositionSecs, duration);
                }
                handler.postDelayed(this, 1000); // Actualiza cada segundo
            }
        };
        return START_NOT_STICKY;
    }

    private void manejarIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        switch (action) {
            case ACTION_PLAY_PAUSE:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }else{
                    playSong(playingSong);
                }
                Log.d("Cancion numero", playingSong+ "");
                break;
            case ACTION_NEXT:
                Log.d("Mensaje", "next pressed");
                Log.d("Cancion numero", playingSong+ "");
                if(playingSong == listaCanciones.size()-1){
                    playingSong = 0;
                }else{
                    playingSong ++;
                }
                Log.d("Cancion numero", playingSong+ "");
                playSong(playingSong);
                break;
            case ACTION_PREVIOUS:
                Log.d("Mensaje", "prev pressed ");
                Log.d("Cancion numero", playingSong+ "");
                if (playingSong == 0) {
                    playingSong = listaCanciones.size()-1;
                }else{
                    playingSong --;
                }
                Log.d("Cancion numero", playingSong+ "");
                playSong(playingSong);
                break;
            case ACTION_LOOP:
                if(!isLooping){
                    isLooping = true;
                }else{
                    isLooping = false;
                }
                break;
            case ACTION_VELOCITY:
                /*if (mediaPlayer != null) {
                    PlaybackParams params = mediaPlayer.getPlaybackParams();
                    params.setSpeed(velocidad);
                    mediaPlayer.setPlaybackParams(params);
                }*/
                break;
        }
    }

    public void playSong(int playingSong) {
        Uri uri = listaCanciones.get(playingSong).getUri();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                handler.post(updateSeekBarRunnable); // Inicia la actualización de la SeekBar
                listener.onSongChanged(playingSong);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                handler.removeCallbacks(updateSeekBarRunnable);
                if(!isLooping){
                    nextSong();
                }else{
                    playSong(playingSong);
                }
            }
        });

        mediaPlayer.prepareAsync();
    }

    public void nextSong(){
        if(playingSong == listaCanciones.size()-1){
            playingSong = 0;
        }else{
            playingSong ++;
        }
        playSong(playingSong);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public MyService getMyService(){
            return MyService.this;
        }
    }

    public ArrayList<Cancion> getListaCaciones(){
        listaCanciones = new ArrayList<>();
        String[] nombresCanciones = {"cancion1", "cancion2", "cancion3", "cancion4"};
        for (String nombreCancion : nombresCanciones) {
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + nombreCancion);
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);
            String artista = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String titulo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String anyo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            String duracion = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int caratula = 0;
            if (nombreCancion.equals("cancion1")){
                caratula = R.drawable.mikrokosmos;
            } else if (nombreCancion.equals("cancion2")) {
                caratula = R.drawable.mitski;
            }else if(nombreCancion.equals("cancion3")){
                caratula = R.drawable.bloopin;
            }else{
                caratula = R.drawable.atarashiigakko;
            }
            Cancion cancion = new Cancion(uri, titulo, artista, anyo, album, duracion, caratula);
            listaCanciones.add(cancion);
        }
        return listaCanciones;
    }
    public void setPlayingSong(int position){
        this.playingSong = position;
        playSong(playingSong);
    }
}
