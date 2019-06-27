package com.remxbot.bot.command.impl;

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.util.ResourceLock;
import com.remxbot.bot.util.StringUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.object.entity.Message;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import reactor.core.publisher.Mono;

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
        return "Shows up to 33 songs around the currently selected one.";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    private String formatTrack(VisualPlaylistEntry track, int len) {
        return String.format("%c %s - %s\n", track.isActive ? '+' : ' ',
                StringUtil.pad(len, track.author), track.title);
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getGuild()
                .flatMap(x -> Mono.just(bot.getGuildAudioDispatcher(x.getId()).getPlaylist()))
                .filter(x -> !x.isEmpty())
                .zipWith(m.getChannel())
                .flatMap(x -> {
                    var playlist = x.getT1();
                    var channel = x.getT2();
                    var desc = new StringBuilder();
                    desc.append(String.format("There are currently **%d** songs in the queue.```diff\n",
                                              playlist.count()));
                    CircularFifoQueue<VisualPlaylistEntry> queue = new CircularFifoQueue<>(33);
                    try (var underscore = new ResourceLock(playlist.getLock())) {
                        var iter = playlist.playlistIterator();
                        boolean sw = false;
                        var len = 0;
                        var i = 0;
                        while (iter.hasNext() && (!queue.isAtFullCapacity() || i < 16)) {
                            var next = iter.next();
                            queue.add(new VisualPlaylistEntry(next, iter.isActuallyCurrent()));
                            len = Math.max(len, next.getInfo().author.length());
                            if (sw) {
                                i++;
                            }
                            if (iter.isActuallyCurrent()) {
                                sw = true;
                            }
                        }
                        for (VisualPlaylistEntry e : queue) {
                            var str = formatTrack(e, len);
                            if (desc.length() + str.length() < 1997) {
                                desc.append(str);
                            } else break;
                        }
                        return channel.createMessage(desc.append("```").toString());
                    }
                })
                .switchIfEmpty(m.getChannel().flatMap(c -> c.createMessage("Playlist empty")))
                .then();
    }

    private class VisualPlaylistEntry {
        public String author;
        public String title;
        public boolean isActive;
        public VisualPlaylistEntry(AudioTrack track, boolean active) {
            this.isActive = active;
            this.title = track.getInfo().title;
            this.author = track.getInfo().author;
        }
    }
}
