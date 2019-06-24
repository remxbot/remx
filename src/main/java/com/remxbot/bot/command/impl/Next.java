package com.remxbot.bot.command.impl;

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.music.GuildAudioDispatcher;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public class Next implements Command {
    private RemxBot bot;

    public Next(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "next";
    }

    @Override
    public String getDescription() {
        return "Switches to the next song, if there is no such track does nothing.";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getGuild()
                .flatMap(x -> Mono.just(bot.getGuildAudioDispatcher(x.getId())))
                .flatMap(GuildAudioDispatcher::next);
    }
}
