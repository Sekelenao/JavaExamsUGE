package fr.uge.numericseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public final class NumericSeq<T> {

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

}
