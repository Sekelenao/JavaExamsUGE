package fr.uge.configurations;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConfHelper {

    private ConfHelper() {
        throw new AssertionError("You can't instantiate this class");
    }

    @SafeVarargs
    private static <T> void checkForNulls(T... objects) {
        for (var obj : objects) Objects.requireNonNull(obj);
    }

    @SafeVarargs
    public static <T> String toString(T instance, Function<? super T, Optional<?>>... getters){
        Objects.requireNonNull(instance);
        Objects.requireNonNull(getters);
        checkForNulls(getters);
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
        checkForNulls(pairs);
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
        checkForNulls(setters);
        var totalCombinations = 1 << setters.length;
        var configurations = new HashSet<T>();
        configurations.add(supplier.get());
        for (int i = 1; i < totalCombinations; i++) {
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

    @SafeVarargs
    public static <T> Stream<T> generateAllAsStream(Supplier<T> supplier, UnaryOperator<T>... setters) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(setters);
        checkForNulls(setters);
        int totalCombinations = 1 << setters.length;
        var configurations = Stream.of(supplier.get());
        for (int i = 1; i < totalCombinations; i++) {
            var currentConfig = supplier.get();
            for (int setterIndex = 0; setterIndex < setters.length; setterIndex++) {
                if ((i & (1 << setterIndex)) != 0) {
                    currentConfig = setters[setterIndex].apply(currentConfig);
                }
            }
            configurations = Stream.concat(configurations, Stream.of(currentConfig));
        }
        return configurations;
    }


}
