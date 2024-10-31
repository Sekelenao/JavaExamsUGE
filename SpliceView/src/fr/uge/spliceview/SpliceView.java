package fr.uge.spliceview;

import java.util.List;
import java.util.Objects;

public final class SpliceView<T> {

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

    public static <E> SpliceView<E> of(List<E> list, int index, E[] array) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(array);
        checkPosition(index, list.size());
        return new SpliceView<>(list, index, array);
    }

    public int size(){
        return array.length + list.size();
    }

    // 4
    // [1, 2, {1, 4}, 6]

    public T get(int index) {
        Objects.checkIndex(index, size());
        if(index < arrayIndex) {                // Array is not on the path
            return list.get(index);
        }
        int cursor = index - arrayIndex;        // We skip the start of the list
        if(cursor < array.length) {             // If cursor is in the array
            return array[cursor];
        }
        return list.get(index - array.length);  // Element is after the array
    }

}
