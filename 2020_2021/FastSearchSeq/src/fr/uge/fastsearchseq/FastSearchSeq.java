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

    private void grow(int amount){
        if(array.length == 0){
            array = Arrays.copyOf(array, INITIAL_CAPACITY);
        } else {
            array = Arrays.copyOf(array, Math.max(array.length * 2, amount));
        }
    }

    public void add(T element){
        if(size == array.length){
            grow(1);
        }
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

    public boolean contains(Object element){
        Objects.requireNonNull(element);
        return Arrays.stream(array, 0, size)
                .anyMatch(element::equals);
    }

}
