package fr.uge.partitionvec;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PartitionVec<T> {

    private T[] values;

    private int nextEmptyIndex;

    private int version;

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

    private List<T> partitionAndReturnLeftPart(Predicate<? super T> predicate){
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

    public List<T> partition(Predicate<? super T> predicate){
        Objects.requireNonNull(predicate);
        final class LeftView extends AbstractList<T> {

            private final List<T> leftPart = partitionAndReturnLeftPart(predicate);

            private final int viewVersion = ++PartitionVec.this.version;

            @Override
            public T get(int index) {
                Objects.checkIndex(index, leftPart.size());
                if(PartitionVec.this.version != viewVersion){
                    throw new IllegalStateException("The PartitionVec has been modified since this view creation");
                }
                return leftPart.get(index);
            }

            @Override
            public int size() {
                return leftPart.size();
            }

        }
        return new LeftView();
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
