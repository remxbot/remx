package com.remxbot.bot.music.playlists;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Objects;

/**
 * Single entry in the double linked playlist
 */
public class Entry {
    private AudioTrack track;
    Entry next;
    Entry prev;
    private boolean gotten = false;
    private Playlist playlist;

    Entry(Playlist playlist, Entry next, Entry prev, AudioTrack track) {
        this.playlist = playlist;
        if (track == null || playlist == null) {
            throw new IllegalArgumentException("track and playlist must be non-null");
        }
        this.next = next;
        this.prev = prev;
        if (prev != null) {
            prev.next = this;
        }
        if (next != null) {
            next.prev = this;
        }
        this.track = track;
    }

    /**
     * @return the track this entry refers to, or a clone of it if it has already been seen once
     */
    public AudioTrack getTrack() {
        return track.makeClone();
    }

    /**
     * Unlinks current entry from the playlist
     * @throws IllegalStateException if this entry is not in any list
     */
    public void remove() throws IllegalStateException {
        if (playlist == null) {
            throw new IllegalStateException("Already removed");
        }
        playlist.size--;
        if (next == null && prev == null) {
            playlist.head = null;
            playlist.tail = null;
            playlist.current = null;
        }
        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        // double removing could lead to issues with size counting
        this.playlist = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return gotten == entry.gotten &&
                track.equals(entry.track) &&
                Objects.equals(next, entry.next) &&
                Objects.equals(prev, entry.prev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(track, next, prev, gotten);
    }
}

