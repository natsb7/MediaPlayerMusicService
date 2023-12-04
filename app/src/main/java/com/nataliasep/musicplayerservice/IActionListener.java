package com.nataliasep.musicplayerservice;

public interface IActionListener {

    void onSongClicked(int position);
    void onArrayRellena();
    void onSongChanged(int position);
    void onTimeUpdate(int currentPosition, int duration);
}
