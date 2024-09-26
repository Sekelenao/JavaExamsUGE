package fr.uge.configurations;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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

    @SafeVarargs
    public static <T> Set<T> generateAll(Supplier<T> supplier, UnaryOperator<T>... setters) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(setters);
        var configurations = new HashSet<T>();
        configurations.add(supplier.get());
        for (int i = 1; i < (1 << setters.length); i++) {
            var config = supplier.get();
            for (int setterIndex = 0; setterIndex < setters.length; setterIndex++) {
                if ((i & (1 << setterIndex)) != 0) {
                    config = setters[setterIndex].apply(config);
                }
            }
            configurations.add(config);
        }
        return configurations;
    }

}
