package fr.uge.configurations;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConfHelper {

    private ConfHelper() {
        throw new AssertionError("You can't instantiate this class");
    }

    @SafeVarargs
    public static String toString(LoggerConf conf, Function<LoggerConf, Optional<?>>... functions){
        Objects.requireNonNull(conf);
        Objects.requireNonNull(functions);
        return Arrays.stream(functions)
                .map(f -> f.apply(conf))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

}
