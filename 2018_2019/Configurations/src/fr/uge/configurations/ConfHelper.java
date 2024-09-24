package fr.uge.configurations;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConfHelper {

    private ConfHelper() {
        throw new AssertionError("You can't instantiate this class");
    }

    @SafeVarargs
    public static <T> String toString(T instance, Function<? super T, Optional<?>>... getters){
        Objects.requireNonNull(instance);
        Objects.requireNonNull(getters);
        return Arrays.stream(getters)
                .map(f -> f.apply(instance))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Object::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @SafeVarargs
    public static <T> String toExtendedString(T instance, Map.Entry<String, Function<? super T, Optional<?>>>... pairs){
        Objects.requireNonNull(instance);
        Objects.requireNonNull(pairs);
        return Arrays.stream(pairs)
                .map(pair -> Map.entry(pair.getKey(), pair.getValue().apply(instance)))
                .filter(pair -> pair.getValue().isPresent())
                .map(pair -> Map.entry(pair.getKey(), pair.getValue().get()))
                .map(pair -> String.join(": ", pair.getKey(), pair.getValue().toString()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

}
