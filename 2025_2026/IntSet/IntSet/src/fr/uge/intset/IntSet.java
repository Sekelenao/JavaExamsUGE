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

}
