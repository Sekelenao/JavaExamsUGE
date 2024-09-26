package fr.uge.fastsearchseq;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FastSearchSeq<T> implements Iterable<T> {

    private T[] array;

    private int size;

    private int random = 1;

    @SuppressWarnings("unchecked")
    public FastSearchSeq() {
        this.array = (T[]) new Object[16];
    }

    private void grow(int amount){
        array = Arrays.copyOf(array, Math.max(array.length * 2, amount));
    }

    public void add(T element){
        if(size == array.length){
            grow(1);
        }
        array[size++] = Objects.requireNonNull(element);
    }

    public int size(){
        return size;
    }

    @Override
    public String toString() {
        return Arrays.stream(array, 0, size)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private int logarithm(int number){
        return 31 - Integer.numberOfLeadingZeros(number);
    }

    private int nextRandom() {
        var x = random;
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;
        random = x;
        return x;
    }

    public boolean contains(Object element){
        Objects.requireNonNull(element);
        for(int i = 0; i < size; i++){
            if(array[i].equals(element)){
                var logarithm = logarithm(size);
                if(i > logarithm){
                    int moveTo = nextRandom() % logarithm;
                    var tmp = array[moveTo];
                    array[moveTo] = array[i];
                    array[i] = tmp;
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private final int expectedSize = size;

            private int index = 0;

            private void checkForConcurrentModifications() {
                if(size != expectedSize) throw new ConcurrentModificationException();
            }

            @Override
            public boolean hasNext() {
                checkForConcurrentModifications();
                return index < size;
            }

            @Override
            public T next() {
                checkForConcurrentModifications();
                if(!hasNext()) throw new NoSuchElementException();
                return array[index++];
            }
        };
    }

    @FunctionalInterface
    public interface IndexedConsumer<E> {

        void accept(E value, int index);

        static <E> IndexedConsumer<E> onlyElement(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            return (value, _) -> consumer.accept(value);
        }

    }

    public void forEachIndexed(IndexedConsumer<? super T> consumer){
        Objects.requireNonNull(consumer);
        for(int i = 0; i < size; i++){
            consumer.accept(array[i], i);
        }
    }

}
