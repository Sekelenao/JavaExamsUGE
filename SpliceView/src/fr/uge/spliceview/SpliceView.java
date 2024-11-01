package fr.uge.spliceview;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpliceView<T> extends AbstractList<T> {

    private final List<T> list;

    private final T[] array;

    private final int arrayIndex;

    private SpliceView(List<T> list, int index, T[] array) {
        this.list = Objects.requireNonNull(list);
        this.array = Objects.requireNonNull(array);
        this.arrayIndex = index;
    }

    private static void checkPosition(int position, int size) {
        if (position < 0 || position > size) {
            throw new IllegalArgumentException("position out of bounds");
        }
    }

    @SafeVarargs
    public static <E> SpliceView<E> of(List<E> list, int index, E... array) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(array);
        checkPosition(index, list.size());
        return new SpliceView<>(list, index, array);
    }

    @Override
    public int size() {
        return array.length + list.size();
    }

    @Override
    public T get(int index) {
        Objects.checkIndex(index, size());
        if (index < arrayIndex) {                // Array is not on the path
            return list.get(index);
        }
        int cursor = index - arrayIndex;        // We skip the start of the list
        if (cursor < array.length) {             // If cursor is in the array
            return array[cursor];
        }
        return list.get(index - array.length);  // Element is after the array
    }

    @Override
    public String toString() {
        if(array.length == 0) return list.toString();
        return Stream.of(
                list.stream().limit(arrayIndex),
                Stream.concat(
                        Stream.of("@ " + array[0]),
                        Arrays.stream(array).skip(1)
                ),
                list.stream().skip(arrayIndex)
            )
            .flatMap(s -> s)
            .map(String::valueOf)
            .collect(Collectors.joining(", ", "[", "]")
        );
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private final Iterator<T> listIterator = list.iterator();

            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < size();
            }

            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException();
                if (cursor < arrayIndex) {
                    cursor++;
                    return listIterator.next();
                }
                int remaining = cursor - arrayIndex;
                if (remaining < array.length) {
                    cursor++;
                    return array[remaining];
                }
                cursor++;
                return listIterator.next();
            }
        };
    }

    @Override
    public T set(int index, T element) {
        Objects.checkIndex(index, size());
        if (index < arrayIndex) {
            return list.set(index, element);
        }
        int cursor = index - arrayIndex;
        if (cursor < array.length) {
            var old = array[cursor];
            array[cursor] = element;
            return old;
        }
        return list.set(index - array.length, element);
    }

    private static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if(fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size) throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex) throw new IndexOutOfBoundsException("fromIndex > toIndex");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());
        return new AbstractList<>() {

            @Override
            public T get(int index) {
                Objects.checkIndex(index, size());
                return SpliceView.this.get(fromIndex + index);
            }

            @Override
            public int size() {
                return toIndex - fromIndex;
            }

            @Override
            public T set(int index, T element) {
                Objects.checkIndex(index, size());
                return SpliceView.this.set(fromIndex + index, element);
            }

            @Override
            public void add(int index, T element) {
                if(fromIndex < arrayIndex + array.length){
                    throw new UnsupportedOperationException();
                }
                list.add(fromIndex - array.length + index, element);
            }

            @Override
            public T remove(int index) {
                if(fromIndex < arrayIndex + array.length){
                    throw new UnsupportedOperationException();
                }
                return list.remove(fromIndex - array.length + index);
            }

        };
    }
}
