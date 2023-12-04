package com.nataliasep.musicplayerservice;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CancionAdapter extends RecyclerView.Adapter<CancionAdapter.CancionViewHolder> {
    private ArrayList<Cancion> canciones;
    private IActionListener listener;

    public CancionAdapter(ArrayList<Cancion> canciones, IActionListener listener) {
        this.canciones = canciones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CancionAdapter.CancionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new CancionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CancionAdapter.CancionViewHolder holder, int position) {
        Cancion cancion = canciones.get(position);
        holder.bindCancion(cancion);

    }
    @Override
    public int getItemCount() {
        if(canciones != null)
          return canciones.size();
        return 0;
    }

    public class CancionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvTitulo;
        public CancionViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvTitulo = itemView.findViewById(R.id.tvSongTitle);
            itemView.setOnClickListener(this);
        }
        public void bindCancion(Cancion cancion){
            tvTitulo.setText(cancion.getTituloCompleto());
        }

        @Override
        public void onClick(View v) {
            if(listener != null){
                Log.d("Mensaje adapter", "item clickado");
                listener.onSongClicked(getLayoutPosition());
                Log.d("Mensaje adapter", getLayoutPosition()+"");
            }
        }
    }

}
