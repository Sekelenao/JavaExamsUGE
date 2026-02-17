package fr.uge.indexedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class IndexedMap<T> extends AbstractMap<Integer, T> {

    private final List<T> elements;

    private IndexedMap(List<T> elements) {
        this.elements = elements;
        super();
    }

    @SafeVarargs
    public static <E> IndexedMap<E> of(E... elements) {
        Objects.requireNonNull(elements);
        var arraylist = new ArrayList<E>(elements.length);
        for(var element : elements){
            Objects.requireNonNull(element);
            arraylist.add(element);
        }
        return new IndexedMap<>(arraylist);
    }

    @Override
    public int size() {
        return elements.size();
    }

    public T getValueOrDefault(int index, T defaultValue){
        if(index < 0 || index >= elements.size()){
            return defaultValue;
        }
        return elements.get(index);
    }

    private <E> Iterator<E> mappedIterator(IntFunction<E> mapper){
        return new Iterator<>() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < elements.size();
            }

            @Override
            public E next() {
                if(!hasNext()){
                    throw new NoSuchElementException();
                }
                return mapper.apply(index++);
            }

        };
    }

    @Override
    public Set<Integer> keySet() {
        return new AbstractSet<>() {

            @Override
            public Iterator<Integer> iterator() {
                return mappedIterator(i -> i);
            }

            @Override
            public int size() {
                return elements.size();
            }

            @Override
            public boolean contains(Object other) {
                return containsKey(other);
            }
        };
    }

    @Override
    public List<T> values(){
        return Collections.unmodifiableList(elements);
    }

    @Override
    public void forEach(BiConsumer<? super Integer, ? super T> action) {
        Objects.requireNonNull(action);
        for(int i = 0; i < elements.size(); i++){
            action.accept(i, elements.get(i));
        }
    }

    @Override
    public T get(Object key) {
        Objects.requireNonNull(key);
        if(key instanceof Integer index && index >= 0 && index < elements.size()){
            return elements.get(index);
        }
        return null;
    }

    @Override
    public Set<Entry<Integer, T>> entrySet() {
        return new AbstractSet<>() {

            @Override
            public Iterator<Entry<Integer, T>> iterator() {
                return mappedIterator(index -> Map.entry(index, elements.get(index)));
            }

            @Override
            public int size() {
                return elements.size();
            }

            @Override
            public boolean contains(Object other) {
                return other instanceof Map.Entry<?,?> entry
                    && entry.getKey() instanceof Integer index
                    && index >= 0
                    && index < elements.size()
                    && elements.get(index).equals(entry.getValue());
            }

        };
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof Integer index && index >= 0 && index < elements.size();
    }

    private Spliterator<Entry<Integer, T>> indexedSpliterator(int start, int end) {
        return new Spliterator<>() {

            private int index = start;

            @Override
            public boolean tryAdvance(Consumer<? super Entry<Integer, T>> action) {
                Objects.requireNonNull(action);
                if (index < end) {
                    action.accept(Map.entry(index, elements.get(index++)));
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<Entry<Integer, T>> trySplit() {
                var middle = (start + end) >>> 1;
                if(middle == index){
                    return null;
                }
                var spliterator = indexedSpliterator(index, middle);
                index = middle;
                return spliterator;
            }

            @Override
            public long estimateSize() {
                return end - index;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL
                    | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.SUBSIZED;
            }

        };
    }

    public Stream<Entry<Integer, T>> stream(){
        return StreamSupport.stream(indexedSpliterator(0, elements.size()), false);
    }

}
