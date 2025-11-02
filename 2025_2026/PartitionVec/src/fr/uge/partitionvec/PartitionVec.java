package fr.uge.partitionvec;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PartitionVec<T> implements Collection<T> {

    private T[] values;

    private int nextEmptyIndex;

    private int version;

    @SuppressWarnings("unchecked")
    public boolean add(T element){
        Objects.requireNonNull(element);
        if (values == null) {
            this.values = (T[]) new Object[4];
        }
        if (nextEmptyIndex == values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }
        values[nextEmptyIndex++] = element;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> other) {
        Objects.requireNonNull(other);
        var changed = false;
        for (var value : other) {
            add(value);
            changed = true;
        }
        return changed;
    }

    @Override
    public int size(){
        return nextEmptyIndex;
    }

    @Override
    public boolean isEmpty() {
        return nextEmptyIndex == 0;
    }

    @Override
    public boolean contains(Object object) {
        if(object == null){
            return false;
        }
        for (int i = 0; i < nextEmptyIndex; i++) {
            if(values[i].equals(object)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> other) {
        Objects.requireNonNull(other);
        var hashset = new HashSet<>(Arrays.asList(values).subList(0, nextEmptyIndex));
        return hashset.containsAll(other);
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private final int version = PartitionVec.this.version;

            private final int size = nextEmptyIndex;

            private int index = 0;

            @Override
            public boolean hasNext() {
                if(version != PartitionVec.this.version){
                    throw new IllegalStateException();
                }
                return index < size;
            }

            @Override
            public T next() {
                if(!hasNext()){
                    throw new NoSuchElementException();
                }
                return values[index++];
            }

        };
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(values, nextEmptyIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A> A[] toArray(A[] array) {
        return (A[]) Arrays.copyOf(values, nextEmptyIndex, array.getClass());
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
