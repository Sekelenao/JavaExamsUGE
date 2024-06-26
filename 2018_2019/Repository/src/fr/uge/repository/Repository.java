package fr.uge.repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Repository<T> {

    /*
    Here, we create an inner class so that the Selector has a reference to the instance of the outer class.
    This allows us to avoid storing the Selector in the Repository and vice versa, and also eliminates the need for
    unnecessary getters. It also allows us to make the constructor as well as the selection method private.
     */

    public final class Selector implements Index {

        private final List<Integer> retainedIndices = new ArrayList<>();

        private final Predicate<? super T> filter;

        private int lastSelected;  // To make it lazy

        private Selector(Predicate<? super T> filter) {
            this.filter = Objects.requireNonNull(filter);
            updateSelection();
        }

        private void updateSelection() {
            if(lastSelected != size) {
                IntStream.range(lastSelected, size)
                        .filter(i -> filter.test(array[i]))
                        .forEach(retainedIndices::add);
                lastSelected = size;
            }
        }

        private Index asIndex(){
            return this;
        }

        public int size(){
            updateSelection();
            return retainedIndices.size();
        }

        @Override
        public Iterator<Integer> iterator() {
            updateSelection();
            return retainedIndices.iterator();
        }

        @Override
        public String toString() {
            updateSelection();
            return retainedIndices.stream()
                    .limit((size > 5) ? 4 : 5)
                    .map(Object::toString)
                    .collect(Collectors.joining(", ", "[",
                            (size > 5) ? ", ..., " + array[size-1] + "]" : "]"
                    ));
        }

    }

    @FunctionalInterface
    public interface Index {

        static Index of(List<Integer> indices){
            return Objects.requireNonNull(indices)::iterator;
        }

        Iterator<Integer> iterator();

        default Index and(Index other){
            Objects.requireNonNull(other);
            return () -> Repository.and(this.iterator(), other.iterator());
        }

    }

    public final class Query {

        private Query(){}

        private final ArrayList<Selector> selectors = new ArrayList<>(); // Stock them as Index to avoid casting in stream

        public Query select(Selector selector){
            Objects.requireNonNull(selector);
            selectors.add(selector);
            return this;
        }

        private Iterator<Integer> finalIterator(){
            return switch (selectors.size()){
                case 0 -> IntStream.range(0, size).iterator();
                case 1 -> selectors.getFirst().iterator();
                default -> selectors.stream()
                        .sorted(Comparator.comparingInt(Selector::size))
                        .map(Selector::asIndex)
                        .reduce(Index::and)
                        .orElseThrow()
                        .iterator();
            };
        }

        public Stream<T> toStream(){
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(finalIterator(),
                            Spliterator.NONNULL | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.DISTINCT
                    ), true
            ).map(i -> array[i]);
        }

    }

    @SuppressWarnings("unchecked")
    private T[] array = (T[]) new Object[16];

    private int size;

    public void add(T element) {
        Objects.requireNonNull(element);
        if (size >= array.length) {
            array = Arrays.copyOf(array, size * 2);
        }
        array[size++] = element;
    }

    public Selector addSelector(Predicate<? super T> filter) {
        return new Selector(Objects.requireNonNull(filter));
    }

    public Query createQuery(){
        return new Query();
    }

    static Iterator<Integer> and(Iterator<Integer> it1, Iterator<Integer> it2) {
        Objects.requireNonNull(it1);
        Objects.requireNonNull(it2);
        return new Iterator<>() {

            private Integer future = loadNext();

            private static Integer nextOrNull(Iterator<Integer> it) {
                return it.hasNext() ? it.next() : null;
            }

            private Integer loadNext() {
                var v1 = nextOrNull(it1);
                var v2 = nextOrNull(it2);
                while (v1 != null && v2 != null) {
                    int diff = v1.compareTo(v2);
                    if (diff == 0) return v1;
                    if (diff < 0) v1 = nextOrNull(it1);
                    else v2 = nextOrNull(it2);
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return future != null;
            }

            @Override
            public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                var next = future;
                future = loadNext();
                return next;
            }
        };
    }


}
