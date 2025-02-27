package fr.uge.range;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class Range extends AbstractList<Integer> implements Iterable<Integer>, RandomAccess {

    private final int from;

    private final int to;

    private Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static Range of(int from, int to) {
        if(from > to) {
            throw new IllegalArgumentException("Wrong range provided " + from + " > " + to);
        }
        if(to - from < 0) {
            throw new IllegalArgumentException("Range is too large: the size of the range exceeds Integer.MAX_VALUE.");
        }
        return new Range(from, to);
    }

    public int size(){
        return to - from;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {

            private int index = from;

            @Override
            public boolean hasNext() {
                return index < to;
            }

            @Override
            public Integer next() {
                if(!hasNext()) {
                    throw new NoSuchElementException();
                }
                return index++;
            }
        };
    }

    @Override
    public Integer get(int index) {
        if(index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return from + index;
    }

    private static Spliterator.OfInt rangeSpliterator(int from, int to) {
        return new Spliterator.OfInt() {

            private int index = from;

            @Override
            public boolean tryAdvance(IntConsumer action) {
                if(index < to) {
                    action.accept(index++);
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator.OfInt trySplit() {
                var middle = (to - from) / 2 + from;
                if (middle == index) {
                    return null;
                }
                var spliterator = rangeSpliterator(index, middle);
                index = middle;
                return spliterator;
            }

            @Override
            public long estimateSize() {
                return to - index;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL
                        | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.SUBSIZED
                        | Spliterator.SORTED;
            }

            @Override
            public Comparator<? super Integer> getComparator() {
                return null;
            }
        };
    }

    @Override
    public Spliterator<Integer> spliterator() {
        return rangeSpliterator(from, to);
    }

    @Override
    public Stream<Integer> stream() {
        return StreamSupport.stream(rangeSpliterator(from, to), false);
    }

    @Override
    public Stream<Integer> parallelStream() {
        return StreamSupport.stream(rangeSpliterator(from, to), true);
    }

    @SuppressWarnings("preview")
    public <A, B> Gatherer<A, Void, B> times(IndexedFunction<? super A, ? extends B> function){
        Objects.requireNonNull(function);
        return Gatherer.of(
                Gatherer.Integrator.ofGreedy(
                    ((_, element, downstream) -> {
                        for (var index : this){
                            if(downstream.isRejecting()){
                                return false;
                            }
                            downstream.push(function.apply(element, index));
                        }
                        return true;
                    })
                )
        );
    }

    public IntStream intStream() {
        return StreamSupport.intStream(rangeSpliterator(from, to), false);
    }

    @Override
    public boolean contains(Object other) {
        return other instanceof Integer value && value >= from && value < to;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        Objects.requireNonNull(collection);
        if(collection instanceof Range other) {
            return other.from >= from && other.to <= to;
        }
        return collection.stream().allMatch(this::contains);
    }

    @Override
    public int indexOf(Object other) {
        return other instanceof Integer value && (value >= from && value < to) ? value - from : -1;
    }

    @Override
    public int lastIndexOf(Object other) {
        return indexOf(other);
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        if(fromIndex < 0 || toIndex > size()) {
            throw new IndexOutOfBoundsException();
        }
        if(from > to) {
            throw new IllegalArgumentException("Wrong range provided " + from + " > " + to);
        }
        return Range.of(from + fromIndex, from + toIndex);
    }

    public Set<Integer> asSet() {
        return new AbstractSet<>() {

            @Override
            public int size() {
                return Range.this.size();
            }

            @Override
            public Iterator<Integer> iterator() {
                return Range.this.iterator();
            }

            @Override
            public Spliterator<Integer> spliterator() {
                return Range.this.spliterator();
            }

            @Override
            public boolean contains(Object other) {
                return Range.this.contains(other);
            }

            @Override
            public boolean containsAll(Collection<?> collection) {
                return Range.this.containsAll(collection);
            }

        };
    }

    @Override
    public String toString() {
        return IntStream.range(from, to)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
