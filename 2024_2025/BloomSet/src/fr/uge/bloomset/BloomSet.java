package fr.uge.bloomset;

import java.util.Objects;

public final class BloomSet<T> {

    private static final int BLOOM_SET_SIZE = 8;

    private final T[] elements;

    private int bloomHash;

    @SuppressWarnings("unchecked")
    public BloomSet() {
        this.elements = (T[]) new Object[BLOOM_SET_SIZE];
    }

    public boolean add(T element) {
        Objects.requireNonNull(element);
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            bloomHash |= hash;
            while (elements[index] != null){
                index++;
            }
            elements[index] = element;
            return true;
        }
        while (index < BLOOM_SET_SIZE && elements[index] != null){
            if(elements[index++].equals(element)){
                return false;
            }
        }
        elements[index] = element;
        return true;
    }

    public int size(){
        int index = 0;
        while (index < BLOOM_SET_SIZE && elements[index] != null){
            index++;
        }
        return index;
    }

    public boolean contains(Object element){
        Objects.requireNonNull(element);
        int index = 0;
        var hash = element.hashCode();
        if((hash & bloomHash) != hash){
            return false;
        }
        while (index < BLOOM_SET_SIZE && elements[index] != null){
            if(elements[index++].equals(element)){
                return true;
            }
        }
        return false;
    }



}
