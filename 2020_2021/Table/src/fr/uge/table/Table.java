package fr.uge.table;

import java.util.*;
import java.util.function.Function;

public final class Table<T> {

    public final class Group<E> {

        private final Map<E, List<Integer>> positions;

        private Group(Comparator<E> comparator, Function<T, E> keySupplier) {
            Objects.requireNonNull(comparator);
            Objects.requireNonNull(keySupplier);
            this.positions = new TreeMap<>(comparator);
            for(var i = 0; i < elements.size(); i++) {
                var key = keySupplier.apply(elements.get(i));
                positions.merge(key, new ArrayList<>(List.of(i)),
                        (l1, l2) -> { l1.addAll(l2); return l1; });
            }
        }

        public int keySize(){
            return positions.size();
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            var sep = "";
            for(var entry : positions.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                builder.append(sep).append(key)
                        .append(": ").append(value);
                sep = "\n";
            }
            return builder.toString();
        }

    }

    private final List<T> elements;

    private final boolean isDynamic;

    private Table(List<T> elements, boolean isDynamic) {
        this.elements = elements;
        this.isDynamic = isDynamic;
    }

    @SafeVarargs
    private static <T> List<T> arrayToList(T... elements) {
        var lst = new ArrayList<T>(elements.length);
        for (var element : elements) {
            Objects.requireNonNull(element);
            lst.add(element);
        }
        return lst;
    }

    public static <T> Table<T> of(T... elements) {
        Objects.requireNonNull(elements);
        return new Table<>(arrayToList(elements), false);
    }

    public static <T> Table<T> dynamic(T... elements) {
        Objects.requireNonNull(elements);
        return new Table<>(arrayToList(elements), true);
    }

    public int size() {
        return elements.size();
    }

    public <E> Group<E> groupBy(Function<T, E> keySupplier, Comparator<E> comparator){
        return new Group<>(Objects.requireNonNull(comparator), Objects.requireNonNull(keySupplier));
    }

}
