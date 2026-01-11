package fr.uge.orderedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public final class OrderedMap<K, V> extends AbstractMap<K, V> {

    private final Map.Entry<K, V>[] entries;

    private int[] indexArray;

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

    @Override
    public V get(Object key) {
        Objects.requireNonNull(key);
        if(entries.length == 0){
            return null;
        }
        if(indexArray == null){
            indexArray = indexArray(entries);
        }
        var hash = Math.floorMod(key.hashCode(), indexArray.length);
        while (indexArray[hash] != 0) {
            var entryIndex = indexArray[hash] - 1;
            var entry = entries[entryIndex];
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
            hash = (hash + 1) % indexArray.length;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        if(entries.length == 0){
            return false;
        }
        if(indexArray == null){
            indexArray = indexArray(entries);
        }
        var hash = Math.floorMod(key.hashCode(), indexArray.length);
        while (indexArray[hash] != 0) {
            var entryIndex = indexArray[hash] - 1;
            var entry = entries[entryIndex];
            if (entry.getKey().equals(key)) {
                return true;
            }
            hash = (hash + 1) % indexArray.length;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<>() {

            @Override
            public Iterator<K> iterator() {
                return new Iterator<>() {

                    private final Iterator<Map.Entry<K, V>> entryIterator = entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return entryIterator.hasNext();
                    }

                    @Override
                    public K next() {
                        if(!hasNext()){
                            throw new NoSuchElementException();
                        }
                        return entryIterator.next().getKey();
                    }
                };
            }

            @Override
            public int size() {
                return entries.length;
            }

            @Override
            public boolean contains(Object object) {
                Objects.requireNonNull(object);
                return OrderedMap.this.containsKey(object);
            }

            private static final class KeySpliterator<K> implements Spliterator<K> {

                private final Spliterator<? extends Map.Entry<K, ?>> entrySpliterator;

                private KeySpliterator(Spliterator<? extends Map.Entry<K, ?>> entrySpliterator){
                    this.entrySpliterator = Objects.requireNonNull(entrySpliterator);
                }

                @Override
                public boolean tryAdvance(Consumer<? super K> action) {
                    return entrySpliterator.tryAdvance(entry -> action.accept(entry.getKey()));
                }

                @Override
                public Spliterator<K> trySplit() {
                    return new KeySpliterator<>(entrySpliterator.trySplit());
                }

                @Override
                public long estimateSize() {
                    return entrySpliterator.estimateSize();
                }

                @Override
                public int characteristics() {
                    return entrySpliterator.characteristics();
                }

            }

            @Override
            public Spliterator<K> spliterator() {
                return new KeySpliterator<>(entrySet().spliterator());
            }

        };
    }
}
