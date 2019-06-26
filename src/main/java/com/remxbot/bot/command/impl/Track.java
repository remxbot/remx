package com.remxbot.bot.command.impl;

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.util.StringUtil;
import discord4j.core.object.entity.GuildMessageChannel;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.List;

public class Track implements Command {
    final private RemxBot bot;

    public Track(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String getDescription() {
        return "Prints information about the currently playing track";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getChannel()
                .ofType(GuildMessageChannel.class)
                .zipWhen(x -> Mono.justOrEmpty(x.getGuildId()))
                .flatMap(x -> x.getT1().createEmbed(embed -> {
                    var track = bot.getGuildAudioDispatcher(x.getT2()).getPlayer().getPlayingTrack();
                    if (track == null) {
                        embed.setDescription("No track playing.");
                        return;
                    }
                    var info = track.getInfo();
                    var description = new StringBuilder();
                    embed.setTitle(info.title);
                    // noinspection SwitchStatementWithTooFewBranches there will be more
                    switch (track.getSourceManager().getSourceName()) {
                        case "youtube":
                            var thumb = StringUtil.youtubeThumb(info.identifier);
                            embed.setThumbnail(thumb);
                            embed.setImage(thumb);
                            embed.setColor(Color.RED);
                            break;
                    }
                    embed.setAuthor(info.author, info.uri, null);
                    description.append(StringUtil.generateProgress(track.getPosition() * 1f / track.getDuration(),
                                       info.uri))
                               .append(" `[")
                               .append(StringUtil.formatLength(track.getPosition())).append('/')
                               .append(StringUtil.formatLength(track.getDuration())).append("]`");
                    embed.setDescription(description.toString());
                })).then();
    }
}
