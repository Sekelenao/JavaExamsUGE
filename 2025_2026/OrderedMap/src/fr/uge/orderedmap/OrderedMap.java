package fr.uge.orderedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

public final class OrderedMap<K, V> extends AbstractMap<K, V> {

    private final Map.Entry<K, V>[] entries;

    private OrderedMap(Map.Entry<K, V>[] entries) {
        this.entries = entries;
        super();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> OrderedMap<K, V> of(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map);
        var nextEmptyIndex = 0;
        var array = (Map.Entry<K, V>[]) new Map.Entry<?, ?>[map.size()];
        for (var entry : map.entrySet()) {
            var key = Objects.requireNonNull(entry.getKey());
            var value = entry.getValue();
            array[nextEmptyIndex++] = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
        return new OrderedMap<>(array);
    }

    @Override
    public int size() {
        return entries.length;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {

        return new AbstractSet<>() {

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<>() {

                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < entries.length;
                    }

                    @Override
                    public Entry<K, V> next() {
                        if(!hasNext()){
                            throw new NoSuchElementException();
                        }
                        return entries[index++];
                    }

                };
            }

            @Override
            public int size() {
                return entries.length;
            }

            @Override
            public Spliterator<Entry<K, V>> spliterator() {
                return Spliterators.spliterator(entries, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
            }

        };

    }

    public static int[] indexArray(Map.Entry<?, ?>[] entries) {
        Objects.requireNonNull(entries);
        var arraySize = entries.length;
        var indexArraySize = arraySize * 2;
        var indexArray = new int[indexArraySize];
        for (var i = 0; i < arraySize; i++) {
            var targetIndex = Math.floorMod(entries[i].getKey().hashCode(), indexArraySize);
            while (indexArray[targetIndex] != 0) {
                targetIndex = Math.floorMod((targetIndex + 1), indexArray.length);
            }
            indexArray[targetIndex] = i + 1;
        }
        return indexArray;
    }

}
