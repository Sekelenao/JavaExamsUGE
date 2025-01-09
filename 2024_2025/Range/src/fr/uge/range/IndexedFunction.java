package fr.uge.range;

public interface IndexedFunction<A, B> {
    B apply(A a, int index);
}
