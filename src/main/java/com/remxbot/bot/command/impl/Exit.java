package com.remxbot.bot.command.impl;

import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public class Exit implements Command {
    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.ADMIN;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        return m.getClient().logout();
    }
}
