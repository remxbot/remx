/*
 * This file is part of remx.
 *
 * remx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * remx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with remx.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.remxbot.bot.music;

import com.remxbot.bot.music.filter.FilterChainManager;
import com.remxbot.bot.music.playlists.Playlist;
import com.remxbot.bot.util.ResourceLock;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Per guild audio manager and dispatcher using lavaplayer
 */
public class GuildAudioDispatcher extends AudioEventAdapter {
    private Snowflake guild;
    final private AudioPlayer player;
    final private AudioPlayerManager manager;
    final private AudioProviderImpl provider = new AudioProviderImpl();
    final private Playlist playlist = new Playlist();
    final private FilterChainManager filterManager = new FilterChainManager(this);

    public GuildAudioDispatcher(Snowflake guild, AudioPlayerManager manager) {
        this.guild = guild;
        player = manager.createPlayer();
        this.manager = manager;
        player.addListener(this);
    }

    public Flux<AudioTrack> findSongs(String url) {
        return Flux.create(sink -> manager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                sink.next(track);
                sink.complete();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // people most often want the first result
                // Potential-TODO: Add a selection menu
                if (playlist.isSearchResult()) {
                    if (!playlist.getTracks().isEmpty()) {
                        sink.next(playlist.getTracks().get(0));
                    }
                } else {
                    playlist.getTracks().forEach(sink::next);
                }
                sink.complete();
            }

            @Override
            public void noMatches() {
                sink.complete();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                sink.error(exception);
            }
        }));
    }

    public Snowflake getGuild() {
        return guild;
    }

    public void enqueue(AudioTrack track) {
        try (var lg = new ResourceLock(playlist.getLock())) {
            if (playlist.enqueue(track)) {
                player.playTrack(track);
            }
        }
    }

    public AudioProvider getProvider() {
        return provider;
    }

    public Mono<Void> previous() {
        return Mono.fromRunnable(() -> {
            try (var lg = new ResourceLock(playlist.getLock())) {
                var t = playlist.prev();
                if (t != null) {
                    player.playTrack(t.getTrack());
                }
            }
        });
    }

    public Mono<Void> next() {
        return Mono.fromRunnable(() -> {
            try (var lg = new ResourceLock(playlist.getLock())) {
                var t = playlist.next();
                if (t != null) {
                    player.playTrack(t.getTrack());
                }
            }
        });
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public Mono<Void> play() {
        return Mono.fromRunnable(() -> player.setPaused(false));
    }

    public Mono<Void> pause() {
        return Mono.fromRunnable(() -> player.setPaused(true));
    }

    public Mono<Void> clear() {
        return Mono.fromRunnable(() -> {
            try (var underscore = new ResourceLock(playlist.getLock())) {
                player.stopTrack();
                playlist.clear();
            }
        });
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    /**
     * @return The filter chain manager associated with this dispatcher
     */
    public FilterChainManager getFilterManager() {
        return filterManager;
    }

    private class AudioProviderImpl extends AudioProvider {
        private final MutableAudioFrame frame = new MutableAudioFrame();

        public AudioProviderImpl() {
            super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
            this.frame.setBuffer(getBuffer());
        }

        @Override
        public boolean provide() {
            if (player.provide(frame)) {
                getBuffer().flip();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            // TODO implement loop & single
            try (var lg = new ResourceLock(playlist.getLock())) {
                var next = playlist.next();
                if (next != null) {
                    player.playTrack(next.getTrack());
                }
            }
        }
    }
}
