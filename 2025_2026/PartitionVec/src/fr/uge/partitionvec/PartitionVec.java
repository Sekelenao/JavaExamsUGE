package fr.uge.partitionvec;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PartitionVec<T> {

    private T[] values;

    private int nextEmptyIndex;

    @SuppressWarnings("unchecked")
    public void add(T element){
        Objects.requireNonNull(element);
        if (values == null) {
            this.values = (T[]) new Object[4];
        }
        if (nextEmptyIndex == values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }
        values[nextEmptyIndex++] = element;
    }

    public int size(){
        return nextEmptyIndex;
    }

    @Override
    public String toString() {
        if(values == null){
            return "[]";
        }
        return Arrays.stream(values, 0, nextEmptyIndex)
            .map(String::valueOf)
            .collect(Collectors.joining(", ", "[", "]"));
    }

}
