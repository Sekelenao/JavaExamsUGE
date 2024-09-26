package fr.uge.fastsearchseq;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;

import static java.util.Spliterator.NONNULL;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class FastSearchSeqTest {

    @Test
    @Tag("Q1")
    public void addAndSizeStrings() {
        FastSearchSeq<String> seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        assertEquals(2, seq.size());
    }

    @Test
    @Tag("Q1")
    public void addAndSizeIntegers() {
        FastSearchSeq<Integer> seq = new FastSearchSeq<Integer>();
        seq.add(1);
        seq.add(13);
        seq.add(7);
        assertEquals(3, seq.size());
    }

    @Test
    @Tag("Q1")
    public void addAndSizeIntegerSameValues() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(42);
        seq.add(42);
        seq.add(17);
        seq.add(17);
        assertEquals(4, seq.size());
    }

    @Test
    @Tag("Q1")
    public void empty() {
        var seq = new FastSearchSeq<Integer>();
        assertEquals(0, seq.size());
    }

    @Test
    @Tag("Q1")
    public void add16Values() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 16; i++) {
            seq.add(i);
        }
        assertEquals(16, seq.size());
    }

    @Test
    @Tag("Q1")
    public void addPrecondition() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        assertThrows(NullPointerException.class, () -> seq.add(null));
    }

    @Test
    @Tag("Q1")
    public void defaultConstructor16Capacity() throws IllegalAccessException {
        var arrayField =
                Arrays.stream(FastSearchSeq.class.getDeclaredFields())
                        .filter(f -> f.getType().isArray())
                        .findFirst()
                        .orElseThrow();
        arrayField.setAccessible(true);
        var array = arrayField.get(new FastSearchSeq<>());
        assertEquals(16, Array.getLength(array));
    }

    @Test
    @Tag("Q1")
    public void notTooManyFields() {
        var fields = FastSearchSeq.class.getDeclaredFields();
        assertTrue(fields.length <= 3);
        assertTrue(Arrays.stream(fields).map(Field::getType).allMatch(f -> f.isPrimitive() || f.isArray()));
    }

    @Test
    @Tag("Q2")
    public void toStringStringValues() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        assertEquals("foo, bar, baz", seq.toString());
    }

    @Test
    @Tag("Q2")
    public void toStringIntegerValues() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(645);
        seq.add(584);
        assertEquals("645, 584", seq.toString());
    }

    @Test
    @Tag("Q2")
    public void toString8Empty() {
        var seq = new FastSearchSeq<String>();
        assertEquals("", seq.toString());
    }

    @Test
    @Tag("Q2")
    public void toStringWeirdValues() {
        var seq = new FastSearchSeq<String>();
        seq.add("<");
        seq.add("[");
        seq.add(">");
        seq.add("]");
        assertEquals("<, [, >, ]", seq.toString());
    }

    @Test
    @Tag("Q2")
    public void toString8Values() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 8; i++) {
            seq.add(i);
        }
        assertEquals("0, 1, 2, 3, 4, 5, 6, 7", seq.toString());
    }

    @Test
    @Tag("Q3")
    public void toString18Values() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 18; i++) {
            seq.add(i);
        }
        assertEquals(18, seq.size());
        assertEquals("0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17", seq.toString());
    }

    @Test
    @Tag("Q3")
    public void addALotOfValues() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 1_000_000; i++) {
            seq.add(i);
        }
        assertEquals(1_000_000, seq.size());
    }

    @Test
    @Tag("Q3")
    public void tooManyMethods() {
        var set = Set.of("add", "size", "toString", "contains", "iterator", "forEachIndexed", "spliterator", "stream");
        Arrays.stream(FastSearchSeq.class.getMethods())
                .filter(m -> m.getDeclaringClass() == FastSearchSeq.class)
                .filter(m -> !set.contains(m.getName()))
                .forEach(m -> fail("unknown method " + m));
    }

    @Test
    @Tag("Q4")
    public void containsSimple6() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 8; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(6));
        assertFalse(seq.contains(8));
    }

    @Test
    @Tag("Q4")
    public void containsSimple16() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 16; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(5));
        assertFalse(seq.contains(-5));
    }

    @Test
    @Tag("Q4")
    public void containsNotTheSame() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        assertTrue(seq.contains(new String("bar")));
    }

    @Test
    @Tag("Q4")
    public void containsNoFoo() {
        var seq = new FastSearchSeq<String>();
        for (var i = 0; i < 8; i++) {
            seq.add("" + i);
        }
        assertFalse(seq.contains("foo"));
    }

    @Test
    @Tag("Q4")
    public void containsEmpty() {
        var seq = new FastSearchSeq<String>();
        assertFalse(seq.contains("foo"));
    }

    @Test
    @Tag("Q4")
    public void containsOneValue() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        assertTrue(seq.contains("foo"));
    }

    @Test
    @Tag("Q4")
    public void containsPrecondition() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        assertThrows(NullPointerException.class, () -> seq.contains(null));
    }

    @Test
    @Tag("Q4")
    public void containsSignature() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        assertFalse(seq.contains(42));
        assertTrue(seq.contains("foo"));
    }

    @Test
    @Tag("Q5")
    public void containsReorder6() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 8; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(6));
        assertFalse(seq.contains(8));
        assertEquals("6, 1, 2, 3, 4, 5, 0, 7", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorder16() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 16; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(5));
        assertFalse(seq.contains(-5));
        assertEquals("0, 5, 2, 3, 4, 1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorder6Twice() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 8; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(6));
        assertTrue(seq.contains(6));
        assertEquals("6, 1, 2, 3, 4, 5, 0, 7", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorder5Twice() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 16; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(5));
        assertTrue(seq.contains(5));
        assertEquals("0, 5, 2, 3, 4, 1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorder5And6() {
        var seq = new FastSearchSeq<Integer>();
        for (var i = 0; i < 8; i++) {
            seq.add(i);
        }
        assertTrue(seq.contains(5));
        assertTrue(seq.contains(6));
        assertEquals("5, 1, 6, 3, 4, 0, 2, 7", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorderEmpty() {
        var seq = new FastSearchSeq<String>();
        assertFalse(seq.contains("foo"));
        assertEquals("", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorderOneValue() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        assertTrue(seq.contains("foo"));
        assertEquals("foo", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorderTwoValues() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        assertTrue(seq.contains("bar"));
        assertEquals("foo, bar", seq.toString());
    }

    @Test
    @Tag("Q5")
    public void containsReorderThreeValues() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        assertTrue(seq.contains("baz"));
        assertEquals("baz, bar, foo", seq.toString());
    }

    @Test
    @Tag("Q6")
    public void iteratorWithStrings() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        Iterator<String> it = seq.iterator();
        assertTrue(it.hasNext());
        assertEquals("foo", it.next());
        assertTrue(it.hasNext());
        assertEquals("bar", it.next());
        assertTrue(it.hasNext());
        assertEquals("baz", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    @Tag("Q6")
    public void iteratorWithIntegers() {
        var seq = new FastSearchSeq<Integer>();
        range(0, 16).forEach(seq::add);
        Iterator<Integer> it = seq.iterator();
        var i = 0;
        while (it.hasNext()) {
            assertEquals(i++, it.next());
        }
    }

    @Test
    @Tag("Q6")
    public void iteratorLoop() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        for (var element : seq) {
            assertEquals(3, element.length());
        }
    }

    @Test
    @Tag("Q6")
    public void iteratorEmpty() {
        var seq = new FastSearchSeq<Integer>();
        var it = seq.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    @Tag("Q6")
    public void iteratorAfterMutation() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(245);
        seq.add(768);
        var it = seq.iterator();
        seq.add(6868);  // add after iterator creation
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    @Tag("Q6")
    public void iteratorRemove() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        var it = seq.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedSimpleString() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.forEachIndexed((element, index) -> {
            assertEquals("foo", element);
            assertEquals(0, index);
        });
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedSimpleInteger() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(42);
        seq.add(42);
        seq.add(42);
        seq.add(42);
        seq.forEachIndexed((element, __) -> assertEquals(42, element));
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedEmpty() {
        var seq = new FastSearchSeq<Integer>();
        seq.forEachIndexed((_1, _2) -> fail());
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedSeveralStrings() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        seq.forEachIndexed((element, index) -> {
            assertEquals(3, element.length());
            assertTrue(index < 3);
        });
    }

    @Test
    @Tag("Q7")
    public void forEachIndexed16Values() {
        var seq = new FastSearchSeq<Integer>();
        for (int i = 0; i < 16; i++) {
            seq.add(i);
        }
        seq.forEachIndexed((element, index) -> assertEquals(index, element));
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedSignature() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.forEachIndexed((Object o, int index) -> {
            assertAll(
                    () -> assertEquals("foo", o),
                    () -> assertEquals(0, index)
            );
        });
    }

    @Test
    @Tag("Q7")
    public void forEachIndexedPrecondition() {
        var seq = new FastSearchSeq<Integer>();
        assertThrows(NullPointerException.class, () -> seq.forEachIndexed(null));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementSimpleString() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(element -> assertEquals("foo", element)));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementExampleString() {
        var seq = new FastSearchSeq<String>();
        seq.add("j'aime");
        seq.add("le");
        seq.add("Java");
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(System.out::println));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementSimpleInteger() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(42);
        seq.add(42);
        seq.add(42);
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(element -> assertEquals(42, element)));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementEmpty() {
        var seq = new FastSearchSeq<Integer>();
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(__ -> fail()));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementSeveralStrings() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.add("baz");
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(element -> assertEquals(3, element.length())));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElement16Values() {
        var seq = new FastSearchSeq<Integer>();
        for (int i = 0; i < 16; i++) {
            seq.add(i);
        }
        var box = new Object() {
            int counter;
        };
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(element -> assertEquals(box.counter++, element)));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementSignature() {
        var seq = new FastSearchSeq<String>();
        seq.add("foo");
        seq.add("bar");
        seq.forEachIndexed(FastSearchSeq.IndexedConsumer.<String>onlyElement((Object element) -> assertNotNull(element)));
    }

    @Test
    @Tag("Q8")
    public void forEachOnlyElementPrecondition() {
        var seq = new FastSearchSeq<Integer>();
        assertThrows(NullPointerException.class, () -> seq.forEachIndexed(FastSearchSeq.IndexedConsumer.onlyElement(null)));
    }

    @Test
    @Tag("Q9")
    public void streamSimple() {
        var seq = new FastSearchSeq<Integer>();
        range(0, 16).forEach(seq::add);
        assertEquals(120, seq.stream().mapToInt(x -> x).sum());
    }

    @Test
    @Tag("Q9")
    public void streamCount() {
        var seq = new FastSearchSeq<Integer>();
        range(0, 8).forEach(seq::add);
        assertEquals(8, seq.stream().map(__ -> fail()).count());
    }

    @Test
    @Tag("Q9")
    public void streamEmpty() {
        var seq = new FastSearchSeq<String>();
        assertTrue(seq.stream().findFirst().isEmpty());
    }

    @Test
    @Tag("Q9")
    public void streamParallel() {
        var seq = new FastSearchSeq<Integer>();
        range(0, 1_000_000).forEach(seq::add);
        assertFalse(seq.stream().isParallel());
        var threads = new HashSet<Thread>();
        var sum = seq.stream().parallel().peek(__ -> threads.add(Thread.currentThread())).mapToLong(x -> x).sum();
        assertEquals(499999500000L, sum);
        assertTrue(threads.size() > 1);
    }

    @Test
    @Tag("Q9")
    public void streamSplit() {
        var seq = new FastSearchSeq<Integer>();
        range(0, 1_000_000).forEach(seq::add);
        var spliterator = seq.stream().spliterator();
        var spliterator2 = spliterator.trySplit();
        assertEquals(500_000, spliterator.estimateSize());
        assertEquals(500_000, spliterator2.estimateSize());
    }

    @Test
    @Tag("Q9")
    public void streamSATB() {
        var seq = new FastSearchSeq<Integer>();
        seq.add(56);
        seq.add(69);
        var stream = seq.stream();
        seq.add(79); // mutation after creation
        assertEquals(List.of(56, 69), stream.collect(Collectors.toList()));
    }

    @Test
    @Tag("Q9")
    public void streamCharacteristics() {
        var seq = new FastSearchSeq<Integer>();
        var list = new ArrayList<>();
        var expectedCharacteristics = (list.stream().spliterator().characteristics()) & (~Spliterator.ORDERED) | NONNULL;
        assertEquals(expectedCharacteristics, seq.stream().spliterator().characteristics());
    }

}