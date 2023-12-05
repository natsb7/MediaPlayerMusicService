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

public class MyService extends Service {
    private ArrayList<Cancion> listaCanciones;
    private MediaMetadataRetriever retriever;
    private int playingSong;
    private final MyBinder myBinder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private IActionListener listener;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;
    private boolean isLooping = false;
    private float currentSpeed = 1.0f;


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
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    int currentPositionSecs = mediaPlayer.getCurrentPosition()/1000; //Da en segundos la posición en la canción
                    Log.d("msg position", currentPositionSecs+"");
                    int duration = mediaPlayer.getDuration(); // Da la duración total de la canción reproducciendose
                    Log.d("msg duration", duration+"");
                    listener.onTimeUpdate(currentPositionSecs, duration);
                }
                handler.postDelayed(this, 1000); // Actualiza cada segundo
            }
        };
        return START_NOT_STICKY;
    }

    /**
     * Comprueba si el mediaplayer tiene alguna canción reproduciendose, si no es así, le indica al método playSong que prepare la canción
     * que esté seleccionada en la array (si no se ha seleccionado nada, se reproduce la primera de la lista)
     */
    public void play() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }else{
            playSong(playingSong);
        }
    }

    /**
     * pausa el reproductor cuando se pulsa el botón de pausa
     */
    public void pause() {
        mediaPlayer.pause();
    }

    /**
     * Método que se activa cuando el botón de "siguiente ha sido pulsado, hace que se reproduzca la siguiente canción a la que se está
     * reproduciendo en este momento. Comprueba la canción actual y le suma una posición dentro de la array, si está en la última posición
     * pasará a la posición 0 de la array. Cuando acaba de hacer estos cambios, se llama al método playSong para que prepare la canción
     */
    public void next(){
        if(playingSong == listaCanciones.size()-1){
            playingSong = 0;
        }else{
            playingSong ++;
        }
        playSong(playingSong);
    }

    /**
     * Método que se activa cuando el botón de "anterior" ha sido pulsado, hace que se reproduzca la anterior canción a la que se está
     * reproduciendo en este momento. Comprueba la canción actual y le resta una posición en la array, si está en la primera posición
     * pasará a la posicion size()-1 de la array. Cuando acaba de hacer estos cambios, se llama al método playSong para que repare la canción a reproducir
     */
    public void previous(){
        if (playingSong == 0) {
            playingSong = listaCanciones.size()-1;
        }else{
            playingSong --;
        }
        playSong(playingSong);
    }

    /**
     * Este método nos permite elegir que la canción se reproduzca en bucle
     * Comprueba si el boolean isLooping está en false, si es así y el botón se ha pulsado, se pondrá a true para que se reproduzca en bucle
     */
    public void loop(){
        if(!isLooping){
            isLooping = true;
        }else{
            isLooping = false;
        }
    }

    /**
     * Este método nos ayuda a aumentar la velocidad de reproducción, el usuario puede seleccionarlo desde la interfaz
     */
    public void velocity(){
        currentSpeed += 0.5f; // Incrementa la velocidad en 0.5
        if (currentSpeed > 2.0f) {
            currentSpeed = 0.5f; // Si supera 2.0, restablece a 0.5
        }
        PlaybackParams params = new PlaybackParams(); //Clase que nos ayuda a aumentar la velocidad de la reproducción de la canción actual
        params.setSpeed(currentSpeed);
        mediaPlayer.setPlaybackParams(params);
    }

    /**
     * Este método nos prepara la canción que se va a reproducir pasandole la posición de la array de la canción seleccionada
     * sacamos la uri de la canción seleccionada para poder pasarselo al setDataSource
     * El setOnPreparedListener nos avisa si la canción ya está preparada, hasta que esto no ocurra, no empezará el reproductor
     *
     * @param playingSong
     */
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
                listener.onSongChanged(playingSong); // Listener que se activa cada vez que cambie la canción
                // sea por el motivo que sea, para que se cambién los datos referentes a la canción en la interfaz

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
                // Nos avisa si la canción se acabado, para saber si el usuario ha pulsado el botón del bucle se comprueba la variable isLooping
            }
        });
        //se prepara el mediaPlayer de manera asíncrona, esperando a que la canción esté preparada
        mediaPlayer.prepareAsync();
    }

    /**
     * Este método nos ayuda a que la reproducción de las canciones sea continúa dentro de la array
     * Cuando acaba una canción pasa automáticamente a la siguiente aunque no le demos la orden desde la interfaz
     * Si la canción está en la última posición (size()-1), vuelve a la inicial, que es 0
     */
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

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setPlayingSong(int position){
        // Método que nos ayuda a reproducir la canción que se ha elegido desde el recyclerView, nos pasa la posición clickada
        this.playingSong = position;
        playSong(playingSong);
    }
    public String getCurrentSpeed(){
        // Nos devuelve la velocidad actual a la que se reproduce la canción, para poder rellenar el textView
        String speed = String.valueOf(currentSpeed);
        return speed;
    }
    public boolean isLooping(){
        return mediaPlayer.isLooping();
    }
}