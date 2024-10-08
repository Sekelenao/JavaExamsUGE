package fr.uge.table;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Table<T> {

    public final class Group<E> {

        private final TreeMap<E, List<Integer>> positions;

        private final Function<? super T, E> keySupplier;

        private Group(Comparator<? super E> comparator, Function<? super T, E> keySupplier) {
            Objects.requireNonNull(comparator);
            Objects.requireNonNull(keySupplier);
            this.positions = new TreeMap<>(comparator);
            this.keySupplier = keySupplier;
            IntStream.range(0, elements.size()).forEach(this::classify);
        }

        private void classify(int index) {
            var key = keySupplier.apply(elements.get(index));
            positions.merge(key, new ArrayList<>(List.of(index)),
                    (l1, l2) -> { l1.addAll(l2); return l1; });
        }

        public int keySize(){
            return positions.size();
        }

        public void forEach(Consumer<? super T> action){
            Objects.requireNonNull(action);
            positions.values().stream()
                    .flatMap(List::stream)
                    .map(elements::get)
                    .forEach(action);
        }

        public List<T> lookup(E key){
            Objects.requireNonNull(key);
            return new AbstractList<>() {

                private final List<Integer> indices = List.copyOf(positions.getOrDefault(key, List.of()));

                @Override
                public T get(int index) {
                    return elements.get(indices.get(index));
                }

                @Override
                public int size() {
                    return indices.size();
                }

                @Override
                public boolean add(T element) {
                    throw new UnsupportedOperationException();
                }

            };

        }

        private Spliterator<T> spliterator(Spliterator<List<Integer>> spliterator) {
            return new Spliterator<>() {

                private final Spliterator<List<Integer>> splt = spliterator;

                private Iterator<Integer> currentIt;

                @Override
                public boolean tryAdvance(Consumer<? super T> action) {
                    if(currentIt == null || !currentIt.hasNext()) {
                        var advanced = splt.tryAdvance(lst -> currentIt = lst.iterator());
                        if(!advanced) return false;
                    }
                    var index = currentIt.next();
                    action.accept(elements.get(index));
                    return true;
                }

                @Override
                public Spliterator<T> trySplit() {
                    return spliterator(splt.trySplit());
                }

                @Override
                public long estimateSize() {
                    return splt.getExactSizeIfKnown();
                }

                @Override
                public int characteristics() {
                    var basics =  NONNULL | ORDERED;
                    if(!isDynamic) basics |= IMMUTABLE;
                    return basics;
                }

            };

        }

        public Stream<T> stream(){
            return StreamSupport.stream(spliterator(positions.values().spliterator()), false);
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

    private final List<Group<?>> groups = new ArrayList<>();

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

    public <E> Group<E> groupBy(Function<? super T, E> keySupplier, Comparator<? super E> comparator){
        Objects.requireNonNull(comparator);
        Objects.requireNonNull(keySupplier);
        var group = new Group<>(comparator, keySupplier);
        groups.add(group);
        return group;
    }

    public <E extends Comparable<? super E>> Group<E> groupBy(Function<? super T, E> keySupplier){
        return groupBy(Objects.requireNonNull(keySupplier), Comparator.naturalOrder());
    }

    public void add(T element){
        Objects.requireNonNull(element);
        if(!isDynamic) throw new UnsupportedOperationException("Not a dynamic table");
        elements.add(element);
        groups.forEach(group -> group.classify(elements.size() - 1));
    }

    public void replace(T oldElement, T newElement){
        Objects.requireNonNull(oldElement);
        Objects.requireNonNull(newElement);
        if(!isDynamic) throw new UnsupportedOperationException("Not a dynamic table");
        for(var group : groups){
            var oldKey = group.keySupplier.apply(oldElement);
            var newKey = group.keySupplier.apply(newElement);
            if(!newKey.equals(oldKey)){
                throw new IllegalStateException("Keys don't match");
            }
        }
        for(var i = 0; i < elements.size(); i++){
            if(elements.get(i).equals(oldElement)){
                elements.set(i, newElement);
            }
        }
    }

}
