package fr.uge.partitionvec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
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

    public List<T> partition(Predicate<? super T> predicate){
        Objects.requireNonNull(predicate);
        if(values == null){
            return Collections.emptyList();
        }
        int i = 0;
        int limit = nextEmptyIndex;
        while(i < limit){
            var actualElement = values[i];
            if(predicate.test(actualElement)){
                i++;
            } else {
                values[i] = values[--limit];
                values[limit] = actualElement;
            }
        }
        return Arrays.asList(values).subList(0, limit);
    }

}
