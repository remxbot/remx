package com.remxbot.bot.rest;

import com.google.common.collect.ImmutableMap;
import com.remxbot.bot.RemxBot;
import com.remxbot.bot.music.filter.ConfigurableFilterFactory;
import com.remxbot.bot.music.filter.impl.AutoGainControlFactory;
import com.remxbot.bot.music.filter.impl.EqualizerConfigurableFactory;
import discord4j.core.object.util.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequestMapping("/guilds/{id}/filters")
@RestController
public class FilterConfigurationInterface {
    private final RemxBot remxBot;

    private Map<String, Supplier<ConfigurableFilterFactory>> factoryFactories =
            ImmutableMap.<String, Supplier<ConfigurableFilterFactory>>builder()
                    .put("equalizer", EqualizerConfigurableFactory::new)
                    .put("autogain", AutoGainControlFactory::new)
                    .build();

    @Autowired
    public FilterConfigurationInterface(RemxBot remxBot) {
        this.remxBot = remxBot;
    }

    /**
     * Obtains filter list assiciated with a guild
     * @param id guild ID
     * @return Flux which will produce the FilterRepresentation (pair of UUID,name) for each filter in guild. If the
     *         guild currently has no audio dispatcher results in an empty flux.
     */
    @GetMapping("/")
    public List<FilterRepresentation> filters(@PathVariable long id) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        return remxBot.getGuildAudioDispatcher(guild)
                .getFilterManager()
                .getFilterMap()
                .entrySet()
                .stream()
                .map(x -> new FilterRepresentation(x.getKey(), x.getValue().getClass().getSimpleName()))
                .collect(Collectors.toList());
    }

    /**
     * Attempts to construct a factory.
     * @param id guild ID to work on
     * @param filterName name of the filter to generate
     * @return UUID of newly inserted factory
     * @throws ResponseStatusException on invalid factories
     */
    @PutMapping("/")
    public UUID addFilter(@PathVariable long id, @RequestBody String filterName) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        if (!factoryFactories.containsKey(filterName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filter name");
        }
        var manager = remxBot.getGuildAudioDispatcher(guild).getFilterManager();
        var uuid = manager.appendFilter(factoryFactories.get(filterName).get());
        manager.updateFilters();
        return uuid;

    }

    /**
     * Deletes a filter
     */
    @DeleteMapping("/{uuid}/")
    public void deleteFilter(@PathVariable long id, @PathVariable UUID uuid) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        var manager = remxBot.getGuildAudioDispatcher(guild).getFilterManager();
        if (!manager.removeFilter(uuid)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid filter ID");
        }
        manager.updateFilters();
    }

    /**
     * Gets values of all parameters
     * @param id guild ID
     * @param uuid filter UUID
     * @return Flux producing values of all parameters associated with the given filter, or 404
     * @see ConfigurableFilterFactory#getAllAttributes()
     */
    @GetMapping("/{uuid}/")
    public float[] allParameters(@PathVariable long id, @PathVariable UUID uuid) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        var filter = remxBot.getGuildAudioDispatcher(guild).getFilterManager().getFilter(uuid);
        if (filter != null) {
            return filter.getAllAttributes();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates all attributes in a filter
     * @param id guild ID
     * @param uuid filter UUID
     * @param values new values
     * @throws IllegalArgumentException if
     *         {@link com.remxbot.bot.music.filter.ConfigurableFilterFactory#setAllAttributes(float[])} throws
     * @throws ResponseStatusException if the UUID is invalid
     * @see com.remxbot.bot.music.filter.ConfigurableFilterFactory#setAllAttributes(float[])
     */
    @PutMapping("/{uuid}/")
    public void setAllParameters(@PathVariable long id, @PathVariable UUID uuid, @RequestBody float[] values) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        var manager = remxBot.getGuildAudioDispatcher(guild).getFilterManager();
        var filter = manager.getFilter(uuid);
        if (filter != null) {
            filter.setAllAttributes(values);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter UUID not mapped");
        }
    }

    /**
     * Gets values of a parameter
     * @param id guild ID
     * @param uuid filter UUID
     * @return The value of the attribute
     * @throws ResponseStatusException if there os no such filter
     * @see com.remxbot.bot.music.filter.ConfigurableFilterFactory#getFloatAttribute(int)
     */
    @GetMapping("/{uuid}/{attribute}")
    public float floatParameter(@PathVariable long id, @PathVariable int attribute, @PathVariable UUID uuid) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        var filter = remxBot.getGuildAudioDispatcher(guild).getFilterManager().getFilter(uuid);
        if (filter != null) {
            return filter.getFloatAttribute(attribute);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates an attribute in a filter
     * @param id guild ID
     * @param uuid filter UUID
     * @param attribute target attribute
     * @param value new value
     * @throws ResponseStatusException if the UUID is invalid or the argument does not pass verification
     * @see com.remxbot.bot.music.filter.ConfigurableFilterFactory#setFloatAttribute(int, float)
     */
    @PutMapping("/{uuid}/{attribute}")
    public void setFloatAttribute(@PathVariable long id, @PathVariable UUID uuid, @PathVariable int attribute,
                                  @RequestBody float value) {
        Snowflake guild = Snowflake.of(id);
        if (!remxBot.hasGuildAudioDispatcher(guild)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unmapped guild");
        }
        var filter = remxBot.getGuildAudioDispatcher(guild).getFilterManager().getFilter(uuid);
        if (filter != null) {
            if (!filter.setFloatAttribute(attribute, value)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Argument verification failed");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter UUID not mapped");
        }
    }

    private class FilterRepresentation {
        public UUID uuid;
        public String className;

        public FilterRepresentation(UUID uuid, String className) {
            this.uuid = uuid;
            this.className = className;
        }
    }
}
