package com.remxbot.bot.music.playlists;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator to go over a Playlist
 */
public class PlaylistIterator implements Iterator<AudioTrack> {
    Entry current;
    private Playlist playlist;

    PlaylistIterator(Playlist playlist) {
        this.playlist = playlist;
    }

    @Override
    public boolean hasNext() {
        return current == null || current.next != null;
    }

    @Override
    public AudioTrack next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (current == null) {
            return (current = playlist.tail).getTrack();
        }
        current = current.next;
        return current.getTrack();
    }

    @Override
    public void remove() {
        current.remove();
    }

    /**
     * @return true if and only if the currently selected track is the same as the current object pointed to by the
     *         iterator
     */
    public boolean isActuallyCurrent() {
        return current != null && current.equals(playlist.getCurrent());
    }
}
