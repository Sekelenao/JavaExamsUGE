package fr.uge.intset;

import java.util.Objects;

public final class IntSet {

    private static final int DEFAULT_CAPACITY = 4;

    private final int[] bitset;

    public IntSet() {
        this.bitset = new int[DEFAULT_CAPACITY];
        super();
    }

    boolean get(int index) {
        Objects.checkIndex(index, bitset.length * 32);
        return (bitset[index >> 5] & (1 << (index & 31))) != 0;
    }

    void set(int index) {
        Objects.checkIndex(index, bitset.length * 32);
        bitset[index >> 5] |= 1 << (index & 31);
    }

    public boolean add(int value){
        if(value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        var bitsetIndex = value >> 5;
        var targetBitMask = 1 << (value & 31);
        if((bitset[bitsetIndex] & targetBitMask) != 0){
            return false;
        }
        bitset[bitsetIndex] |= targetBitMask;
        return true;
    }

}
