package fr.uge.bloomset;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class BloomSet<T> extends AbstractSet<T> {

    private static final int BLOOM_SET_SIZE = 8;

    private Set<T> elementsAsSet;

    private T[] elements;

    private int bloomHash;

    @SuppressWarnings("unchecked")
    public BloomSet() {
        this.elements = (T[]) new Object[BLOOM_SET_SIZE];
    }

    private boolean indexIsNotEmpty(int index){
        return index < BLOOM_SET_SIZE && elements[index] != null;
    }

    private int nextEmptyIndex(){
        int index = 0;
        while (indexIsNotEmpty(index)){
            index++;
        }
        return index;
    }

    private boolean switchImplementationAndAdd(T element){
        elementsAsSet = HashSet.newHashSet(elements.length);
        Collections.addAll(elementsAsSet, elements);
        elements = null;
        return elementsAsSet.add(element);
    }

    private boolean isSetImplementation(){
        return elementsAsSet != null;
    }

    @Override
    public boolean add(T element) {
        Objects.requireNonNull(element);
        if(isSetImplementation()){
            return elementsAsSet.add(element);
        }
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            bloomHash |= hash;
            index = nextEmptyIndex();
            if(index == BLOOM_SET_SIZE){
                return switchImplementationAndAdd(element);
            }
            elements[index] = element;
            return true;
        }
        while (indexIsNotEmpty(index)){
            if(elements[index++].equals(element)){
                return false;
            }
        }
        if(index == BLOOM_SET_SIZE){
            return switchImplementationAndAdd(element);
        }
        elements[index] = element;
        return true;
    }

    @Override
    public int size(){
        if(isSetImplementation()){
            return elementsAsSet.size();
        }
        return nextEmptyIndex();
    }

    @Override
    public boolean contains(Object element){
        Objects.requireNonNull(element);
        if(isSetImplementation()){
            return elementsAsSet.contains(element);
        }
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            return false;
        }
        while (indexIsNotEmpty(index)){
            if(element.equals(elements[index++])){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        if(isSetImplementation()){
            return Collections.unmodifiableSet(elementsAsSet).iterator();
        }
        return new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return indexIsNotEmpty(index);
            }

            @Override
            public T next() {
                if(!hasNext()){
                    throw new NoSuchElementException();
                }
                return elements[index++];
            }

        };
    }

    @Override
    public boolean isEmpty() {
        return bloomHash == 0 && !isSetImplementation() && elements[0] == null;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof BloomSet<?> otherBloomSet){
            if(isSetImplementation() && otherBloomSet.isSetImplementation()){
                return elementsAsSet.equals(otherBloomSet.elementsAsSet);
            }
            if(!isSetImplementation() && !otherBloomSet.isSetImplementation() && bloomHash == otherBloomSet.bloomHash){
                return Arrays.stream(elements).takeWhile(Objects::nonNull).allMatch(otherBloomSet::contains);
            }
            return false;
        }
        if(other instanceof Set<?> otherSet){
            int index = 0;
            while (indexIsNotEmpty(index)){
                if(!otherSet.contains(elements[index++])){
                    return false;
                }
            }
            return otherSet.size() == index;
        }
        return false;
    }

    @Override
    public Spliterator<T> spliterator() {
        return new Spliterator<>() {

            private final Spliterator<T> spliterator = isSetImplementation() ?
                elementsAsSet.spliterator() : Arrays.spliterator(elements, 0, nextEmptyIndex());

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                return spliterator.tryAdvance(action);
            }

            @Override
            public Spliterator<T> trySplit() {
                return spliterator.trySplit();
            }

            @Override
            public long estimateSize() {
                return spliterator.estimateSize();
            }

            @Override
            public int characteristics() {
                var defaultCharacteristics = Spliterator.DISTINCT | Spliterator.NONNULL;
                if(isSetImplementation()){
                    return spliterator.characteristics() | defaultCharacteristics;
                }
                return defaultCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
            }

        };
    }
}
