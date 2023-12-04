package com.nataliasep.musicplayerservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity implements IActionListener{
    private MyService myService;
    private RecyclerView recyclerView;
    private CancionAdapter cancionAdapter;
    private ImageView ivFoto;
    private ImageButton bPlayPause, bAnterior, bSiguiente, bBucle, bVelocidad;
    private TextView tvTitulo, tvInicio, tvFinal;
    private SeekBar seekBar;
    private Intent intent;
    boolean isPlaying = false;
    private ArrayList<Cancion> canciones;
    private int cancionSeleccionada;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(MainActivity.class.getSimpleName(), "Estoy conectandome");
            myService = ((MyService.MyBinder)service).getMyService();
            Log.d(MainActivity.class.getSimpleName(), "Conectado al servicio");
            myService.setListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };



    /*private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancionAdapter.setCanciones(canciones);


            }
        });
    }*/

    public void play() {
        intent = new Intent(MainActivity.this, MyService.class);
        if(!isPlaying){
            isPlaying = true;
            bPlayPause.setImageResource(R.drawable.pausa);
        }else{
            isPlaying = false;
            bPlayPause.setImageResource(R.drawable.play);
        }
        intent.setAction(MyService.ACTION_PLAY_PAUSE);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.d("Mensaje", "mandamos el intent play");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(MainActivity.this, MyService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        Log.d("Mensaje", "mandamos el intent");


        ivFoto = findViewById(R.id.ivImagen);
        bPlayPause = findViewById(R.id.ibPlayPause);
        bAnterior = findViewById(R.id.ibAnterior);
        bSiguiente = findViewById(R.id.ibSiguiente);
        bBucle = findViewById(R.id.ibLoop);
        bVelocidad = findViewById(R.id.ibVelocidad);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvInicio = findViewById(R.id.tvInicio);
        tvFinal = findViewById(R.id.tvFinal);
        seekBar = findViewById(R.id.seekBar);

        bPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();

            }
        });
        bAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MyService.class);
                intent.setAction(MyService.ACTION_NEXT);
                startService(intent);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                isPlaying = true;
                bPlayPause.setImageResource(R.drawable.pausa);
                Log.d("Mensaje", "mandamos el intent next");
            }
        });
        bSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MyService.class);
                intent.setAction(MyService.ACTION_PREVIOUS);
                startService(intent);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                isPlaying = true;
                bPlayPause.setImageResource(R.drawable.pausa);
                Log.d("Mensaje", "mandamos el intent prev");
            }
        });

        bBucle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MyService.class);
                intent.setAction(MyService.ACTION_LOOP);
                startService(intent);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);

            }
        });
        bVelocidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, MyService.class);
                intent.setAction(MyService.ACTION_VELOCITY);
                startService(intent);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);

            }
        });
    }


    @Override
    public void onSongClicked(int position) {
        Log.d("cancion click", position+"");
        myService.setPlayingSong(position);
    }


    @Override
    public void onArrayRellena() {
        canciones = myService.getListaCaciones();
        Log.d("Mensaje", "array rellena en el MAIN");
        Log.d("Array de canciones", canciones.get(0).getTituloCompleto());
        recyclerView = findViewById(R.id.rvCanciones);
        cancionAdapter = new CancionAdapter(canciones, this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(cancionAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }

    @Override
    public void onSongChanged(int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("posicion de la cancion", position+"");
                ivFoto.setImageResource(myService.getListaCaciones().get(position).getCaratula());
                tvTitulo.setText(myService.getListaCaciones().get(position).getTituloCompleto());
                tvTitulo.setSelected(true);
            }
        });

    }

    @Override
    public void onTimeUpdate(int currentPosition, int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Actualiza la SeekBar
                seekBar.setMax(duration/1000);
                seekBar.setProgress(currentPosition);

                // Actualiza el TextView con el tiempo transcurrido
                String timeElapsed = formatTime(currentPosition*1000);
                tvInicio.setText(timeElapsed);

                // Actualiza el TextView con el tiempo restante
                int remainingTime = duration - currentPosition*1000;
                String timeRemaining = formatTime(remainingTime);
                tvFinal.setText(timeRemaining);
            }
        });
    }

    private String formatTime(int timeInMillis) {
        int seconds = timeInMillis / 1000;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

}