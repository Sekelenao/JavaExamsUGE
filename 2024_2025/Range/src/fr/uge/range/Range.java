package fr.uge.range;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private static Spliterator<Integer> rangeSpliterator(int from, int to) {
        return new Spliterator<>() {

            private int index = from;

            @Override
            public boolean tryAdvance(Consumer<? super Integer> action) {
                if(index < to) {
                    action.accept(index++);
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<Integer> trySplit() {
                var middle = from + (to - from) / 2;
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
    public Stream<Integer> parallelStream() {
        return StreamSupport.stream(rangeSpliterator(from, to), true);
    }

    @Override
    public Stream<Integer> stream() {
        return StreamSupport.stream(rangeSpliterator(from, to), false);
    }

    @Override
    public String toString() {
        return IntStream.range(from, to)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
