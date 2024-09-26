package fr.uge.fastsearchseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class FastSearchSeq<T> {

    private static final int INITIAL_CAPACITY = 16;

    private T[] array;

    private int size;

    @SuppressWarnings("unchecked")
    public FastSearchSeq() {
        this.array = (T[]) new Object[INITIAL_CAPACITY];
    }

    public void add(T element){
        array[size++] = Objects.requireNonNull(element);
    }

    public int size(){
        return size;
    }

    @Override
    public String toString() {
        return Arrays.stream(array, 0, size)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
