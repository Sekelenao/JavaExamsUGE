package fr.uge.bloomset;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

public final class BloomSet<T> extends AbstractSet<T> {

    private static final int BLOOM_SET_SIZE = 8;

    private Set<T> elementsAsSet;

    private T[] elements;

    private int bloomHash;

    @SuppressWarnings("unchecked")
    public BloomSet() {
        this.elements = (T[]) new Object[BLOOM_SET_SIZE];
    }

    private boolean isNotValidIndexForNextElement(int index){
        return index < BLOOM_SET_SIZE && elements[index] != null;
    }

    private int findNextIndex(){
        int index = 0;
        while (isNotValidIndexForNextElement(index)){
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

    public boolean add(T element) {
        Objects.requireNonNull(element);
        if(elementsAsSet != null){
            return elementsAsSet.add(element);
        }
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            bloomHash |= hash;
            index = findNextIndex();
            if(index == BLOOM_SET_SIZE){
                return switchImplementationAndAdd(element);
            }
            elements[index] = element;
            return true;
        }
        while (isNotValidIndexForNextElement(index)){
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

    public int size(){
        if(elementsAsSet != null){
            return elementsAsSet.size();
        }
        return findNextIndex();
    }

    public boolean contains(Object element){
        Objects.requireNonNull(element);
        if(elementsAsSet != null){
            return elementsAsSet.contains(element);
        }
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            return false;
        }
        while (isNotValidIndexForNextElement(index)){
            if(element.equals(elements[index++])){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        if(elementsAsSet != null){
            return Collections.unmodifiableSet(elementsAsSet).iterator();
        }
        return new Iterator<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return isNotValidIndexForNextElement(index);
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

}
