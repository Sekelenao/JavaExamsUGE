package fr.uge.bloomset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class BloomSet<T> {

    private static final int BLOOM_SET_SIZE = 8;

    private Set<T> elementsAsSet;

    private T[] elements;

    private int bloomHash;

    @SuppressWarnings("unchecked")
    public BloomSet() {
        this.elements = (T[]) new Object[BLOOM_SET_SIZE];
    }

    private int findNextIndex(){
        int index = 0;
        while (index < BLOOM_SET_SIZE && elements[index] != null){
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
        while (index < BLOOM_SET_SIZE && elements[index] != null){
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
        while (index < BLOOM_SET_SIZE && elements[index] != null){
            if(element.equals(elements[index++])){
                return true;
            }
        }
        return false;
    }



}
