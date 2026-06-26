package fr.uge.intset;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import jdk.incubator.vector.*;
import org.jspecify.annotations.NonNull;

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

    private void growIfNecessary(int bitsetIndex) {
        if (bitsetIndex >= bitset.length) {
            bitset = Arrays.copyOf(bitset, Math.max(bitset.length * 2, bitsetIndex + 1));
        }
    }

    public boolean add(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        var bitsetIndex = value >> 5;
        growIfNecessary(bitsetIndex);
        var targetBitMask = 1 << (value & 31);
        if ((bitset[bitsetIndex] & targetBitMask) != 0) {
            return false;
        }
        bitset[bitsetIndex] |= targetBitMask;
        return true;
    }

    public boolean contains(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be positive");
        }
        var bitsetIndex = value >> 5;
        return bitsetIndex < bitset.length && (bitset[bitsetIndex] & (1 << (value & 31))) != 0;
    }

    public Spliterator.OfInt spliterator() {
        return new Spliterator.OfInt() {

            private int index = -1;

            private int bitpos;

            private void moveToNextNonEmptyCell() {
                do { index++; } while (index < bitset.length && bitset[index] == 0);
                if (index < bitset.length) {
                    bitpos = bitset[index];
                }
            }

            @Override
            public boolean tryAdvance(IntConsumer action) {
                if (bitpos == 0) {
                    moveToNextNonEmptyCell();
                }
                if (index >= bitset.length) {
                    return false;
                }
                int bitIndex = Integer.numberOfTrailingZeros(bitpos);
                action.accept((index << 5) + bitIndex);
                bitpos &= (bitpos - 1); // Turn off the rightmost bit (S/O Brian Kernighan)
                return true;
            }

            @Override
            public Spliterator.OfInt trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public int characteristics() {
                return ORDERED | DISTINCT | NONNULL | SORTED;
            }

            @Override
            public Comparator<? super Integer> getComparator() {
                return null;
            }

        };
    }

    public IntStream stream() {
        return StreamSupport.intStream(spliterator(), false);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder("[");
        IntConsumer action = i -> {
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append(i); // StringBuilder is cool because it doesn't allocate a new String :)
        };
        spliterator().forEachRemaining(action);
        return builder.append("]").toString();
    }

    int bitCount() {
        var species = IntVector.SPECIES_PREFERRED;
        int upperBound = species.loopBound(bitset.length);
        var sumVector = IntVector.zero(species);
        int i = 0;
        for (; i < upperBound; i += species.length()) {
            var vector = IntVector.fromArray(species, bitset, i);
            var counts = vector.lanewise(VectorOperators.BIT_COUNT);
            sumVector = sumVector.add(counts);
        }
        int totalBits = sumVector.reduceLanes(VectorOperators.ADD);
        for (; i < bitset.length; i++) {
            totalBits += Integer.bitCount(bitset[i]);
        }
        return totalBits;
    }

    public Set<Integer> asSet() {
        return new AbstractSet<>() {

            @Override
            public @NonNull Iterator<Integer> iterator() {
                return new Iterator<>() {

                    private final Iterator<Integer> iterator = Spliterators.iterator(IntSet.this.spliterator());

                    private int lastReturned = -1;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public @NonNull Integer next() {
                        lastReturned = iterator.next();
                        return lastReturned;
                    }

                    @Override
                    public void remove() {
                        if (lastReturned == -1) {
                            throw new IllegalStateException();
                        }
                        var bitsetIndex = lastReturned >> 5;
                        IntSet.this.bitset[bitsetIndex] &= ~(1 << (lastReturned & 31));
                        lastReturned = -1;
                    }

                };
            }

            @Override
            public int size() {
                return bitCount();
            }

            @Override
            public boolean contains(Object value) {
                return value instanceof Integer integer && integer >= 0 && IntSet.this.contains(integer);
            }

            @Override
            public boolean add(Integer integer) {
                return IntSet.this.add(integer);
            }

            @Override
            public boolean remove(Object value) {
                Objects.requireNonNull(value);
                if (!(value instanceof Integer integer) || integer < 0) {
                    return false;
                }
                var bitsetIndex = integer >> 5;
                if (bitsetIndex >= IntSet.this.bitset.length) {
                    return false;
                }
                var targetBitMask = 1 << (integer & 31);
                if ((IntSet.this.bitset[bitsetIndex] & targetBitMask) == 0) {
                    return false;
                }
                IntSet.this.bitset[bitsetIndex] &= ~targetBitMask;
                return true;
            }

            @Override
            public void clear() {
                Arrays.fill(IntSet.this.bitset, 0);
            }
        };

    }

}
