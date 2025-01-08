package fr.uge.range;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Range {

    private final int from;

    private final int to;

    private Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static Range of(int from, int to) {
        if(from > to) {
            throw new IllegalArgumentException("Wrong range provided " + from + " > " + to);
        }
        if(to - from < 0) {
            throw new IllegalArgumentException("Range is too large: the size of the range exceeds Integer.MAX_VALUE.");
        }
        return new Range(from, to);
    }

    public int size(){
        return to - from;
    }

    @Override
    public String toString() {
        return IntStream.range(from, to)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
