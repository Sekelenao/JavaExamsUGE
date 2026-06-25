package fr.uge.intset;

import java.util.Arrays;
import java.util.Objects;

public final class IntSet {

    private static final int DEFAULT_CAPACITY = 4;

    private int[] bitset;

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

    private void growIfNecessary(int bitsetIndex){
        if (bitsetIndex >= bitset.length){
            bitset = Arrays.copyOf(bitset, Math.max(bitset.length * 2, bitsetIndex + 1));
        }
    }

    public boolean add(int value){
        if(value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        var bitsetIndex = value >> 5;
        growIfNecessary(bitsetIndex);
        var targetBitMask = 1 << (value & 31);
        if((bitset[bitsetIndex] & targetBitMask) != 0){
            return false;
        }
        bitset[bitsetIndex] |= targetBitMask;
        return true;
    }

    public boolean contains(int value){
        if(value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        var bitsetIndex = value >> 5;
        return bitsetIndex < bitset.length && (bitset[bitsetIndex] & (1 << (value & 31))) != 0;
    }

}
