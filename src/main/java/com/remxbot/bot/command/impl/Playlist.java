package com.remxbot.bot.command.impl;

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.music.playlists.PlaylistIterator;
import com.remxbot.bot.util.ResourceLock;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.Message;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

public class Playlist implements Command {
    private RemxBot bot;

    public Playlist(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public String getDescription() {
        return "Shows up to 65 songs around the currently selected one.";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    private String formatTrack(AudioTrack track) {
        return String.format("  %s - %s\n", track.getInfo().author, track.getInfo().title);
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getGuild()
                .flatMap(x -> Mono.just(bot.getGuildAudioDispatcher(x.getId()).getPlaylist()))
                .zipWith(m.getChannel())
                .flatMap(x -> {
                    var playlist = x.getT1();
                    var channel = x.getT2();
                    var desc = new StringBuilder();
                    desc.append(String.format("There are currently **%d** songs in the queue.```diff\n",
                                              playlist.count()));
                    var queue = new CircularFifoQueue<String>(32);
                    // TODO align all the dashes
                    try (var underscore = new ResourceLock(playlist.getLock())) {
                        var iter = playlist.playlistIterator();
                        AudioTrack next = null;
                        while (iter.hasNext()) {
                            next = iter.next();
                            if (iter.isActuallyCurrent()) {
                                break;
                            }
                            queue.add(formatTrack(next));
                        }
                        if (next == null) {
                            return channel.createMessage("No songs in playlist.");
                        }
                        for (String title : queue) {
                            desc.append(title);
                        }
                        if (iter.isActuallyCurrent()) {
                            desc.append('+').append(formatTrack(next).substring(1));
                        }
                        for (int i = 0; i < (64 - queue.size()) && iter.hasNext(); i++) {
                            desc.append(formatTrack(iter.next()));
                        }
                        return channel.createMessage(desc.append("```").toString());
                    }
                }).then();
    }
}
