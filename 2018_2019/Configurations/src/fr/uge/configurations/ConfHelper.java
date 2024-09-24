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
    public static <T> String toString(T instance, Function<T, Optional<?>>... getters){
        Objects.requireNonNull(instance);
        Objects.requireNonNull(getters);
        return Arrays.stream(getters)
                .map(f -> f.apply(instance))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

}
