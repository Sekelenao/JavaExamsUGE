package fr.uge.fastsearchseq;

import java.util.Objects;

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

}
