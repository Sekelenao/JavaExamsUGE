package fr.uge.partitionvec;

import java.util.AbstractList;
import java.util.Arrays;
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

    private int partitionAndReturnLimit(Predicate<? super T> predicate){
        if(values == null){
            return 0;
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
        return limit;
    }

    public interface PartitionView<T> extends List<T> {
        PartitionView<T> otherPartition();
    }

    public PartitionView<T> partition(Predicate<? super T> predicate){
        Objects.requireNonNull(predicate);
        var vec = PartitionVec.this;
        final class PartitionViewImpl extends AbstractList<T> implements PartitionVec.PartitionView<T> {

            private final int start;

            private final int end;

            private final int max;

            private final int currentViewVersion;

            private PartitionViewImpl(int start, int end, int max, int version){
                this.currentViewVersion = version;
                this.start = start;
                this.end = end;
                this.max = max;
            }

            private PartitionViewImpl childViewWithFollowingBounds(int start, int end){
                return new PartitionViewImpl(start, end, max, currentViewVersion);
            }

            private void checkVersion(){
                if(vec.version != currentViewVersion){
                    throw new IllegalStateException("The PartitionVec has been modified since this view creation");
                }
            }

            @Override
            public T get(int index) {
                Objects.checkIndex(index, size());
                checkVersion();
                return vec.values[start + index];
            }

            @Override
            public int size() {
                return end - start;
            }

            public PartitionVec.PartitionView<T> otherPartition(){
                checkVersion();
                if(start == 0){
                    return childViewWithFollowingBounds(end, max);
                }
                return childViewWithFollowingBounds(0, start);
            }

        }
        return new PartitionViewImpl(0, partitionAndReturnLimit(predicate), vec.nextEmptyIndex, ++vec.version);
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
