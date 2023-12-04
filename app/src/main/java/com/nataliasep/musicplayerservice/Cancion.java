package com.nataliasep.musicplayerservice;
import android.net.Uri;

public class Cancion {
    private Uri uri;
    private String titulo;
    private String artista;
    private String album;
    private String anyo;
    private String duracion;
    private int caratula;

    public Cancion(Uri uri, String titulo, String artista, String anyo, String album, String duracion, int caratula) {
        this.titulo = titulo;
        this.uri = uri;
        this.artista = artista;
        this.anyo = anyo;
        this.album = album;
        this.duracion = duracion;
        this.caratula = caratula;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTituloCompleto(){
        return titulo + " - " + artista + " - " + album;
    }

    public Uri getUri(){
        return uri;
    }
    public String getArtista() {
        return artista;
    }

    public String getAlbum() {
        return album;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getAnyo() {
        return anyo;
    }
    public int getCaratula() {
        return caratula;
    }
}

