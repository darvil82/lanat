package lanat.utils;

import org.jetbrains.annotations.Nullable;

public record Pair<TFirst, TSecond>(@Nullable TFirst first, @Nullable TSecond second) {}