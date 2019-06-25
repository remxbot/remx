package com.remxbot.bot.music.filter;

import com.remxbot.bot.music.GuildAudioDispatcher;
import com.remxbot.bot.util.ResourceLock;
import com.remxbot.bot.util.UUIDTools;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages filter chains on a per-player basis
 */
public class FilterChainManager {
    final private Map<UUID, ConfigurableFilterFactory> filters = new LinkedHashMap<>();
    final private GuildAudioDispatcher dispatcher;
    final private Lock lock = new ReentrantLock();

    /**
     * @param dispatcher dispatcher holding the player that this manager manages
     */
    public FilterChainManager(GuildAudioDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * @param filter filter to add to the end of the chain
     * @return index of the newly added filter
     */
    public UUID appendFilter(ConfigurableFilterFactory filter) {
        try (var locked = new ResourceLock(lock)) {
            UUID id = UUIDTools.ensureUniqueRandom(filters::containsKey);
            filters.put(id, filter);
            updateFilters();
            return id;
        }
    }

    /**
     * Removes a filter based on it's UUID. Do not forget to call {@link #updateFilters()} after sets of invocations.
     * @param id the UUID of the filter to get rid of
     * @return true if the filter was removed
     */
    public boolean removeFilter(UUID id) {
        try (var locked = new ResourceLock(lock)) {
            return filters.remove(id) != null;
        }
    }

    /**
     * @param id the UUID of the wanted filter
     * @return the wanted filter, or null
    */
    public ConfigurableFilterFactory getFilter(UUID id) {
        return filters.get(id);
    }

    /**
     * Called when the filter chain changes
     */
    public void updateFilters() {
        try (var locked = new ResourceLock(lock)) {
            if (filters.isEmpty()) {
                dispatcher.getPlayer().setFilterFactory(null);
            } else {
                dispatcher.getPlayer().setFilterFactory(new ProxyFilterFactory(new LinkedList<>(filters.values())));
            }
        }
    }
}
