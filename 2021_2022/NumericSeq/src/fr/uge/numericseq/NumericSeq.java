package fr.uge.numericseq;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class NumericSeq<T> implements Iterable<T> {

    private static final int INITIAL_CAPACITY = 16;

    private long[] elements;

    private int size;

    private final ToLongFunction<T> into;

    private final LongFunction<T> from;

    private NumericSeq(int size, ToLongFunction<T> into, LongFunction<T> from) {
        this.elements = new long[size];
        this.into = into;
        this.from = from;
    }

    private void grow(int amount){
        if(elements.length == 0){
            elements = Arrays.copyOf(elements, INITIAL_CAPACITY);
        } else {
            elements = Arrays.copyOf(elements, Math.max(elements.length * 2, amount));
        }
    }

    public static NumericSeq<Integer> ints(int... ints) {
        Objects.requireNonNull(ints);
        var ns = new NumericSeq<>(ints.length, Integer::longValue, l -> (int) l);
        for(var l : ints) ns.add(l);
        return ns;
    }

    public static NumericSeq<Long> longs(long... longs) {
        Objects.requireNonNull(longs);
        var ns = new NumericSeq<>(longs.length, s -> s, s -> s);
        for(var l : longs) ns.add(l);
        return ns;
    }

    public static NumericSeq<Double> doubles(double... doubles) {
        Objects.requireNonNull(doubles);
        var ns = new NumericSeq<>(doubles.length, Double::doubleToRawLongBits, Double::longBitsToDouble);
        for(var l : doubles) ns.add(l);
        return ns;
    }

    public void add(T value) {
        Objects.requireNonNull(value);
        if(size == elements.length) {
            grow(1);
        }
        elements[size++] = into.applyAsLong(value);
    }

    public T get(int index) {
        Objects.checkIndex(index, size);
        return from.apply(elements[index]);
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return Arrays.stream(elements)
                .limit(size)
                .mapToObj(from)
                .map(Objects::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private final int maxSize = size;

            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < maxSize;
            }

            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException();
                return from.apply(elements[currentIndex++]);
            }

        };

    }

    public NumericSeq<T> addAll(NumericSeq<T> other) {
        Objects.requireNonNull(other);
        if(elements.length + other.size > elements.length) {
            grow(other.size);
        }
        for (var i = 0; i < other.size; i++) {
            elements[size++] = other.elements[i];
        }
        return this;
    }

    public <E> NumericSeq<E> map(Function<? super T, E> mapper, Supplier<NumericSeq<E>> factory) {
        Objects.requireNonNull(mapper);
        Objects.requireNonNull(factory);
        var numericSeq = factory.get();
        for(var i = 0; i < size; i++) {
            var element = from.apply(elements[i]);
            numericSeq.add(mapper.apply(element));
        }
        return numericSeq;
    }

    public static <E> Collector<E, ?, NumericSeq<E>> toNumericSeq(Supplier<NumericSeq<E>> factory) {
        Objects.requireNonNull(factory);
        return Collector.of(
                factory,
                NumericSeq::add,
                NumericSeq::addAll,
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    private Spliterator<T> customSpliterator(int start, int end, long... array) {
        return new Spliterator<>() {

            private int index = start;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (index < end) {
                    action.accept(from.apply(array[index++]));
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<T> trySplit() {
                if(end - start < 1024){
                    return null;
                }
                var middle = (index + end) >>> 1;
                if (middle == index) {
                    return null;
                }
                var spliterator = customSpliterator(index, middle, array);
                index = middle;
                return spliterator;
            }

            @Override
            public long estimateSize() {
                return end - index;
            }

            @Override
            public int characteristics() { return SIZED | SUBSIZED | ORDERED | NONNULL; }
        };

    }

    public Stream<T> stream() {
        return StreamSupport.stream(customSpliterator(0, size, elements), false);
    }

}
