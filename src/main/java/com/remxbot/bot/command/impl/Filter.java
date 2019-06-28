package com.remxbot.bot.command.impl;

import com.remxbot.bot.RemxBot;
import com.remxbot.bot.command.Command;
import com.remxbot.bot.command.CommandCategory;
import com.remxbot.bot.rest.FilterConfigurationInterface;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import org.apache.commons.lang3.ArrayUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Filter implements Command {
    private RemxBot bot;

    public Filter(RemxBot bot) {
        this.bot = bot;
    }

    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public String getDescription() {
        return "Manages filters in the current guild";
    }

    @Override
    public void formLongDescription(EmbedCreateSpec embed) {
        embed.setDescription("Provides an interface for the configuration of filters used in remx.");
        embed.addField("- list", "Lists all filters in the filter chain", false);
        embed.addField("- add", "Syntax: `r:filter add <type>`\n" +
                "Adds filter of type <type> to the end of the filter chain", false);
        embed.addField("Available filter types:",
                FilterConfigurationInterface.FACTORY_FACTORIES.keySet().stream()
                        .collect(Collectors.joining("`, `", "`", " `")), false);
        embed.addField("- remove", "Syntax: `r:filter remove <uuid>`\n" +
                "Removes filter associated with <uuid> from the filter chain", false);
        embed.addField("- get", "Syntax: `r:filter get <uuid>`\n" +
                "Gets all attributes of filter <uuid> from the filter chain," +
                "for example each attribute may be an equalizer band", false);
        embed.addField("- set", "Syntax: `r:filter set <uuid> <attrid/ALL> <value...>`\n" +
                "Adjusts a filters attributes. <uuid> is the same as for get.\n" +
                "<attrid> would represent the number of the attribute that you want to alter (from 0 onwards)\n" +
                "If <attrid> is `ALL` it'll set all the provided arguments to the respective attributes\n" +
                "<value...> can be one or more values. if there's a specific <attrid> only the first is used, " +
                "the rest are used only if `ALL` is selected.\n" +
                "For example, `r:filter set <uuid> all 0 0 0 0 0 0 0 0 0 0 0 0 0 0` would reset an equalizer " +
                "associated with <uuid>", false);
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.MUSIC;
    }

    @Override
    public Mono<Void> process(Message m, List<String> args) {
        var badSyntax = m.getChannel().flatMap(x -> x.createMessage("Invalid syntax. See `r:help filter`")).then();
        if (args.size() < 2) {
            return badSyntax;
        }
        var disp = m.getGuild().flatMap(x -> Mono.just(bot.getGuildAudioDispatcher(x.getId()).getFilterManager()));
        switch (args.get(1)) {
            case "list":
                return disp.flatMapMany(x -> Flux.fromIterable(x.getFilterMap().entrySet()))
                        .flatMap(x -> Mono.just(String.format("UUID: %s Type: %s", x.getKey().toString(),
                                x.getValue().getClass().getSimpleName())))
                        .defaultIfEmpty("Current audio path clean. No filters are involved.")
                        .collect(Collectors.joining("\n", "```\n", "```"))
                        .zipWith(m.getChannel())
                        .flatMap(x -> x.getT2().createMessage(x.getT1()))
                        .then();
            case "add":
                if (args.size() != 3) {
                    return Flux.fromIterable(FilterConfigurationInterface.FACTORY_FACTORIES.keySet())
                            .flatMap(x -> Mono.just(String.format("`%s`", x)))
                            .collect(Collectors.joining(", "))
                            .zipWith(m.getChannel())
                            .flatMap(x -> x.getT2().createMessage("Available filters:\n" + x.getT1()))
                            .then();
                }
                var fac = FilterConfigurationInterface.FACTORY_FACTORIES.get(args.get(2));
                if (fac != null) {
                    return m.getChannel()
                            .zipWith(disp)
                            .doOnNext(x -> x.getT2().updateFilters())
                            .flatMap(x -> x.getT1().createMessage("Filter ID: " + x.getT2().appendFilter(fac.get())))
                            .then();
                } else {
                    return m.getChannel().flatMap(x -> x.createMessage("Invalid filter")).then();
                }
            case "remove": {
                if (args.size() != 3) {
                    return badSyntax;
                }
                var uuid = UUID.fromString(args.get(2));
                return disp.zipWhen(x -> Mono.just(x.removeFilter(uuid)))
                        .doOnNext(x -> x.getT1().updateFilters())
                        .zipWith(m.getChannel())
                        .flatMap(x -> x.getT2().createMessage(x.getT1().getT2() ? "Filter removed" : "No such filter"))
                        .then();
                // jesus needs to help this poor piece of code
            }
            case "get": {
                if (args.size() != 3) {
                    return badSyntax;
                }
                var uuid = UUID.fromString(args.get(2));
                return disp
                        .flatMapMany(x -> Flux.fromArray(ArrayUtils.toObject(x.getFilter(uuid).getAllAttributes())))
                        .map(String::valueOf)
                        .defaultIfEmpty("Filter has no attributes")
                        .collect(Collectors.joining(" ", "`", " `"))
                        .zipWith(m.getChannel())
                        .flatMap(x -> x.getT2().createEmbed(e -> {
                            e.setTitle("Attributes for filter " + uuid);
                            e.setDescription(x.getT1());
                        }))
                        .then();
            }
            case "set": {
                // r:filter set uuid attr|all value...
                if (args.size() < 5) {
                    return badSyntax;
                }
                var uuid = UUID.fromString(args.get(2));
                var filter = disp.flatMap(x -> Mono.justOrEmpty(x.getFilter(uuid)))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid filter ID")));
                if (args.get(3).equalsIgnoreCase("all")) {
                    float[] params = new float[args.size() - 4];
                    for (int i = 4; i < args.size(); i++) {
                        params[i - 4] = Float.parseFloat(args.get(i));
                    }
                    return filter.flatMap(x -> {
                        x.setAllAttributes(params);
                        return Mono.just("Attributes set");
                    }).zipWith(m.getChannel())
                            .flatMap(x -> x.getT2().createMessage(x.getT1()))
                            .then();
                } else {
                    var value = Float.parseFloat(args.get(4));
                    var idx = Integer.parseInt(args.get(3));
                    return filter.flatMap(x -> {
                        x.setFloatAttribute(idx, value);
                        return Mono.just("Attribute set");
                    })
                            .defaultIfEmpty("No such filter")
                            .zipWith(m.getChannel())
                            .flatMap(x -> x.getT2().createMessage(x.getT1()))
                            .then();
                }
            }
            default:
                return badSyntax;
        }
    }
}
