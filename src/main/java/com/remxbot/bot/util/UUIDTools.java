package com.remxbot.bot.util;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Utility class for working with UUIDs
 */
public class UUIDTools {
    /**
     * Generates random UUIDs until it finds a unique one. Relies on functioning randomness source
     * @param contains Predicate that returns true if the UUID is taken
     * @return a unique UUID
     */
    public static UUID ensureUniqueRandom(Predicate<UUID> contains) {
        UUID id;
        while (contains.test(id = UUID.randomUUID()));
        return id;
    }
}
