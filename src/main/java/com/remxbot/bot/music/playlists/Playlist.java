package com.remxbot.bot.music.playlists;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A playlist is a doubly linked list
 * <p>Thread-safety: nonexistent. To help remedy this issue a lock is provided through {@link #getLock()}</p>
 */
public class Playlist implements Iterable<AudioTrack> {
    Entry head = null;
    // used later for iteration
    Entry tail = null ;
    Entry current = null;
    long size = 0;
    final private Lock lock = new ReentrantLock();

    /**
     * @return true if the playlist is empty
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Adds new track to the top of the playlist
     * @param track the track to add
     * @return whether the current track changed
     */
    public boolean enqueue(AudioTrack track) {
        size++;
        head = new Entry(this, null, head, track);
        if (tail == null) {
            tail = head;
        }
        if (current == null) {
            current = head;
            return true;
        }
        return false;
    }

    /**
     * Switches to the next entry in the playlist
     * @return the new track, or null if the end of the list was reached
     */
    public Entry next() {
        if (current != null) {
            current = current.next;
        }
        return current;
    }

    /**
     * Switches to the previous entry in the playlist
     * @return the new track, or null if the end of the list was reached
     */
    public Entry prev() {
        if (current != null) {
            current = current.prev;
        } else {
            current = head;
        }
        return current;
    }

    /**
     * @return the current track
     */
    public Entry getCurrent() {
        return current;
    }

    @Override
    public Iterator<AudioTrack> iterator() {
        return playlistIterator();
    }

    public PlaylistIterator playlistIterator() {
        return new PlaylistIterator(this);
    }

    /**
     * @return internally tracked size of the playlist
     */
    public long count() {
        return size;
    }

    /**
     * @return the lock associated with this playlist, it is guaranteed not to change.
     */
    public Lock getLock() {
        return lock;
    }
}
