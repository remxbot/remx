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

package com.remxbot.bot.command;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * Per guild audio manager and dispatcher using lavaplayer
 */
public class GuildAudioDispatcher {
    private Snowflake guild;
    final private AudioPlayer player;
    final private AudioPlayerManager manager;
    final private AudioProviderImpl provider = new AudioProviderImpl();

    public GuildAudioDispatcher(Snowflake guild, AudioPlayerManager manager) {
        this.guild = guild;
        player = manager.createPlayer();
        this.manager = manager;
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
        // TODO implement queue
        player.playTrack(track);
    }

    public AudioProvider getProvider() {
        return provider;
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
}
