package lanat.utils;

/**
 * A record that stores two objects.
 * @param <TFirst> The type of the first object.
 * @param <TSecond> The type of the second object.
 */
public record Pair<TFirst, TSecond>(TFirst first, TSecond second) {}