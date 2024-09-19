package fr.uge.numericseq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NumericSeqTest {

    // Q1
    @Nested
    class Q1 {

        @Test
        @Tag("Q1")
        public void longs() {
            var seq = NumericSeq.longs();
            seq.add(1L);
            seq.add(42L);
            seq.add(747L);
            assertAll(() -> assertEquals(3, seq.size()), () -> assertEquals(1L, seq.get(0)),
                    () -> assertEquals(42L, seq.get(1)), () -> assertEquals(747L, seq.get(2)));
        }

        @Test
        @Tag("Q1")
        public void longTyping() {
            NumericSeq<Long> seq = NumericSeq.longs();
            assertNotNull(seq);
        }

        @Test
        @Tag("Q1")
        public void emptyLongs() {
            var seq = NumericSeq.longs();
            assertEquals(0, seq.size());
        }

        @Test
        @Tag("Q1")
        public void getOutOfBounds() {
            var seq = NumericSeq.longs();
            seq.add(1L);
            seq.add(42L);
            assertAll(() -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> seq.get(2)));
        }

        @Test
        @Tag("Q1")
        public void addPrecondition() {
            var seq = NumericSeq.longs();
            assertThrows(NullPointerException.class, () -> seq.add(null));
        }

        @Test
        @Tag("Q1")
        public void noPublicConstructor() {
            assertEquals(0, NumericSeq.class.getConstructors().length);
        }
    }

    // Q2
    @Nested
    class Q2 {

        @Test
        @Tag("Q2")
        public void longsResizeable() {
            var seq = NumericSeq.longs();
            for (var i = 0L; i < 17L; i++) {
                seq.add(i);
            }
            assertEquals(17, seq.size());
        }

        @Test
        @Timeout(1)
        @Tag("Q2")
        public void longsALotOfValues() {
            var seq = NumericSeq.longs();
            LongStream.range(0, 1_000_000).forEach(seq::add);
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, seq.get(i))));
        }
    }

    // Q3
    @Nested
    class Q3 {

        @Test
        @Tag("Q3")
        public void longsFormat() {
            var seq = NumericSeq.longs();
            seq.add(1L);
            seq.add(42L);
            seq.add(747L);
            assertEquals("[1, 42, 747]", "" + seq);
        }

        @Test
        @Tag("Q3")
        public void longsToStringOneValue() {
            var seq = NumericSeq.longs();
            seq.add(727L);
            assertEquals("[727]", "" + seq);
        }

        @Test
        @Tag("Q3")
        public void longsEmptyFormat() {
            var seq = NumericSeq.longs();
            assertEquals("[]", "" + seq);
        }
    }

    // Q4
    @Nested
    class Q4 {
        @Test
        @Tag("Q4")
        public void intsWithValues() {
            var seq = NumericSeq.ints(1, 42, 747);
            assertAll(() -> assertEquals(3, seq.size()), () -> assertEquals(1, seq.get(0)),
                    () -> assertEquals(42, seq.get(1)), () -> assertEquals(747, seq.get(2)));
        }

        @Test
        @Tag("Q4")
        public void longsWithValues() {
            var seq = NumericSeq.longs(1L, 42L, 747L);
            assertAll(() -> assertEquals(3, seq.size()), () -> assertEquals(1L, seq.get(0)),
                    () -> assertEquals(42L, seq.get(1)), () -> assertEquals(747L, seq.get(2)));
        }

        @Test
        @Tag("Q4")
        public void doublesWithValues() {
            var seq = NumericSeq.doubles(2., 256., 16.);
            assertAll(() -> assertEquals(3, seq.size()), () -> assertEquals(2., seq.get(0)),
                    () -> assertEquals(256., seq.get(1)), () -> assertEquals(16., seq.get(2)));
        }

        @Test
        @Tag("Q4")
        public void intsWithValuesToString() {
            var seq = NumericSeq.ints(1, 42, 747);
            assertEquals("[1, 42, 747]", seq.toString());
        }

        @Test
        @Tag("Q4")
        public void longsWithValuesToString() {
            var seq = NumericSeq.longs(1L, 42L, 747L);
            assertEquals("[1, 42, 747]", seq.toString());
        }

        @Test
        @Tag("Q4")
        public void doublesWithValuesToString() {
            var seq = NumericSeq.doubles(2., 256., 16.);
            assertEquals("[2.0, 256.0, 16.0]", seq.toString());
        }

        @Test
        @Tag("Q4")
        public void intsOrLongsOrDoublesWithValuesTyping() {
            NumericSeq<Integer> intSeq = NumericSeq.ints(42);
            NumericSeq<Long> longSeq = NumericSeq.longs(42L);
            NumericSeq<Double> doubleSeq = NumericSeq.doubles(256.);
            assertAll(() -> assertNotNull(intSeq), () -> assertNotNull(longSeq), () -> assertNotNull(doubleSeq));
        }

        @Test
        @Tag("Q4")
        public void intsWithValuesAdd() {
            var seq = NumericSeq.ints(1, 42);
            seq.add(0);
            seq.add(-31);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(1, seq.get(0)),
                    () -> assertEquals(42, seq.get(1)), () -> assertEquals(0, seq.get(2)), () -> assertEquals(-31, seq.get(3)));
        }

        @Test
        @Tag("Q4")
        public void longsWithValuesAdd() {
            var seq = NumericSeq.longs(1L, 42L);
            seq.add(0L);
            seq.add(-31L);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(1L, seq.get(0)),
                    () -> assertEquals(42L, seq.get(1)), () -> assertEquals(0L, seq.get(2)),
                    () -> assertEquals(-31L, seq.get(3)));
        }

        @Test
        @Tag("Q4")
        public void doubleWithValuesAdd() {
            var seq = NumericSeq.doubles(2., 16.);
            seq.add(0.);
            seq.add(-32.3);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(2., seq.get(0)),
                    () -> assertEquals(16., seq.get(1)), () -> assertEquals(0., seq.get(2)),
                    () -> assertEquals(-32.3, seq.get(3)));
        }

        @Test
        @Tag("Q4")
        public void intsWithValuesBig() {
            var seq = NumericSeq.ints(IntStream.range(0, 1_000_000).toArray());
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, seq.get(i))));
        }

        @Test
        @Tag("Q4")
        public void longsWithValuesBig() {
            var seq = NumericSeq.longs(LongStream.range(0, 1_000_000).toArray());
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((long) i, seq.get(i))));
        }

        @Test
        @Tag("Q4")
        public void doublesWithValuesBig() {
            var seq = NumericSeq.doubles(IntStream.range(0, 1_000_000).mapToDouble(i -> i).toArray());
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((double) i, seq.get(i))));
        }

        @Test
        @Tag("Q4")
        public void longsSideMutation() {
            var array = new long[]{12L, 80L, 128L};
            var seq = NumericSeq.longs(array);
            array[1] = 64L;
            assertEquals(80L, seq.get(1));
        }

        @Test
        @Tag("Q4")
        public void onlyOneArray() {
            assertTrue(Arrays.stream(NumericSeq.class.getDeclaredFields())
                    .noneMatch(field -> field.getType().isArray() && field.getType() != long[].class));
        }

        @Test
        @Tag("Q4")
        public void intsOrLongsOrDoublesWithValuesPrecondition() {
            assertAll(() -> assertThrows(NullPointerException.class, () -> NumericSeq.ints(null)),
                    () -> assertThrows(NullPointerException.class, () -> NumericSeq.longs(null)),
                    () -> assertThrows(NullPointerException.class, () -> NumericSeq.doubles(null)));
        }
    }

    /*

    // Q5
    @Nested
    class Q5 {
        @Test
        @Tag("Q5")
        public void longsFor() {
            var seq = NumericSeq.longs();
            seq.add(23L);
            seq.add(99L);
            var list = new ArrayList<Long>();
            for (var value : seq) {
                list.add(value);
            }
            assertEquals(List.of(23L, 99L), list);
        }

        @Test
        @Tag("Q5")
        public void emptyFor() {
            var seq = NumericSeq.longs();
            for (var value : seq) {
                fail();
            }
        }

        @Test
        @Tag("Q5")
        public void typedIntsWithValuesFor() {
            var seq = NumericSeq.ints(23, 99, -14, 66);
            var list = new ArrayList<Integer>();
            for (int value : seq) {
                list.add(value);
            }
            assertEquals(List.of(23, 99, -14, 66), list);
        }

        @Test
        @Tag("Q5")
        public void typedLongsWithValuesFor() {
            var seq = NumericSeq.longs(23L, 99L, -14L, 66L);
            var list = new ArrayList<Long>();
            for (long value : seq) {
                list.add(value);
            }
            assertEquals(List.of(23L, 99L, -14L, 66L), list);
        }

        @Test
        @Tag("Q5")
        public void typedDoublesWithValuesFor() {
            var seq = NumericSeq.doubles(23.5, 99.6, -14.8, 66.6);
            var list = new ArrayList<Double>();
            for (double value : seq) {
                list.add(value);
            }
            assertEquals(List.of(23.5, 99.6, -14.8, 66.6), list);
        }

        @Test
        @Timeout(1)
        @Tag("Q5")
        public void intsForBig() {
            var seq = NumericSeq.ints();
            IntStream.range(0, 1_000_000).forEach(seq::add);
            var sum = 0;
            for (var value : seq) {
                sum += value;
            }
            assertEquals(IntStream.range(0, 1_000_000).sum(), sum);
        }

        @Test
        @Timeout(1)
        @Tag("Q5")
        public void longsForBig() {
            var seq = NumericSeq.longs();
            LongStream.range(0, 1_000_000).forEach(seq::add);
            var sum = 0L;
            for (var value : seq) {
                sum += value;
            }
            assertEquals(LongStream.range(0, 1_000_000).sum(), sum);
        }

        @Test
        @Timeout(1)
        @Tag("Q5")
        public void doublesForBig() {
            var seq = NumericSeq.doubles();
            IntStream.range(0, 1_000_000).mapToDouble(i -> i).forEach(seq::add);
            var sum = 0.;
            for (var value : seq) {
                sum += value;
            }
            assertEquals(IntStream.range(0, 1_000_000).mapToDouble(i -> i).sum(), sum);
        }

        @Test
        @Tag("Q5")
        public void intsForWithAdd() {
            var seq = NumericSeq.ints();
            seq.add(3);
            var list = new ArrayList<Integer>();
            for (var value : seq) {
                list.add(value);
                seq.add(10);
            }
            assertAll(() -> assertEquals(List.of(3), list), () -> assertEquals(2, seq.size()),
                    () -> assertEquals(3, seq.get(0)), () -> assertEquals(10, seq.get(1)));
        }

        @Test
        @Tag("Q5")
        public void longsForWithAdd() {
            var seq = NumericSeq.longs();
            seq.add(3L);
            var list = new ArrayList<Long>();
            for (var value : seq) {
                list.add(value);
                seq.add(10L);
            }
            assertAll(() -> assertEquals(List.of(3L), list), () -> assertEquals(2, seq.size()),
                    () -> assertEquals(3L, seq.get(0)), () -> assertEquals(10L, seq.get(1)));
        }

        @Test
        @Tag("Q5")
        public void doublesForWithAdd() {
            var seq = NumericSeq.doubles();
            seq.add(3.0);
            var list = new ArrayList<Double>();
            for (var value : seq) {
                list.add(value);
                seq.add(10.0);
            }
            assertAll(() -> assertEquals(List.of(3.0), list), () -> assertEquals(2, seq.size()),
                    () -> assertEquals(3.0, seq.get(0)), () -> assertEquals(10.0, seq.get(1)));
        }

        @Test
        @Tag("Q5")
        public void iteratorIntsWithValuesFor() {
            var seq = NumericSeq.ints(23, 99, -14, 66);
            var iterator = seq.iterator();
            assertEquals(23, iterator.next());
            assertEquals(99, iterator.next());
            assertEquals(-14, iterator.next());
            assertEquals(66, iterator.next());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @Tag("Q5")
        public void iteratorLongsWithValuesFor() {
            var seq = NumericSeq.longs(23L, 99L, -14L, 66L);
            var iterator = seq.iterator();
            assertEquals(23L, iterator.next());
            assertEquals(99L, iterator.next());
            assertEquals(-14L, iterator.next());
            assertEquals(66L, iterator.next());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @Tag("Q5")
        public void iteratorDoublesWithValuesFor() {
            var seq = NumericSeq.doubles(23., 99., -14., 66.);
            var iterator = seq.iterator();
            assertEquals(23., iterator.next());
            assertEquals(99., iterator.next());
            assertEquals(-14., iterator.next());
            assertEquals(66., iterator.next());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        @Tag("Q5")
        public void iteratorIntsWithValuesSeveralHasNext() {
            var seq = NumericSeq.ints();
            seq.add(314);
            seq.add(17);
            var iterator = seq.iterator();
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(314, iterator.next());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(17, iterator.next());
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }

        @Test
        @Tag("Q5")
        public void iteratorLongsWithValuesSeveralHasNext() {
            var seq = NumericSeq.longs();
            seq.add(314L);
            seq.add(17L);
            var iterator = seq.iterator();
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(314L, iterator.next());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(17L, iterator.next());
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }

        @Test
        @Tag("Q5")
        public void iteratorDoublesWithValuesSeveralHasNext() {
            var seq = NumericSeq.doubles();
            seq.add(314.);
            seq.add(17.);
            var iterator = seq.iterator();
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(314., iterator.next());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(17., iterator.next());
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }

        @Test
        @Tag("Q5")
        public void iteratorIntsRemove() {
            var seq = NumericSeq.ints(12);
            var iterator = seq.iterator();
            assertEquals(12, iterator.next());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        @Test
        @Tag("Q5")
        public void iteratorLongsRemove() {
            var seq = NumericSeq.longs(12L);
            var iterator = seq.iterator();
            assertEquals(12L, iterator.next());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        @Test
        @Tag("Q5")
        public void iteratorDoublesRemove() {
            var seq = NumericSeq.doubles(12.);
            var iterator = seq.iterator();
            assertEquals(12., iterator.next());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }
    }

    // Q6
    @Nested
    class Q6 {
        @Test
        @Tag("Q6")
        public void addAll() {
            var seq = NumericSeq.longs();
            seq.add(44L);
            seq.add(666L);
            var seq2 = NumericSeq.longs();
            seq2.add(77L);
            seq2.add(666L);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(44L, seq.get(0)),
                    () -> assertEquals(666L, seq.get(1)), () -> assertEquals(77L, seq.get(2)),
                    () -> assertEquals(666L, seq.get(3)));
        }

        @Test
        @Tag("Q6")
        public void addAllInts() {
            var seq = NumericSeq.ints(44, 666);
            var seq2 = NumericSeq.ints(77, 666);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(44, seq.get(0)),
                    () -> assertEquals(666, seq.get(1)), () -> assertEquals(77, seq.get(2)), () -> assertEquals(666, seq.get(3)));
        }

        @Test
        @Tag("Q6")
        public void addAllLongs() {
            var seq = NumericSeq.longs(44, 666);
            var seq2 = NumericSeq.longs(77, 666);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(44L, seq.get(0)),
                    () -> assertEquals(666L, seq.get(1)), () -> assertEquals(77L, seq.get(2)),
                    () -> assertEquals(666L, seq.get(3)));
        }

        @Test
        @Tag("Q6")
        public void addAllDoubles() {
            var seq = NumericSeq.doubles(44., 666.);
            var seq2 = NumericSeq.doubles(77., 666.);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(4, seq.size()), () -> assertEquals(44., seq.get(0)),
                    () -> assertEquals(666., seq.get(1)), () -> assertEquals(77., seq.get(2)),
                    () -> assertEquals(666., seq.get(3)));
        }

        @Test
        @Tag("Q6")
        public void addAllIntsEmpty() {
            var seq = NumericSeq.ints(32);
            var seq2 = NumericSeq.ints();
            seq.addAll(seq2);
            assertAll(() -> assertEquals(1, seq.size()), () -> assertEquals(32, seq.get(0)));
        }

        @Test
        @Tag("Q6")
        public void addAllLongsEmpty() {
            var seq = NumericSeq.longs(32L);
            var seq2 = NumericSeq.longs();
            seq.addAll(seq2);
            assertAll(() -> assertEquals(1, seq.size()), () -> assertEquals(32L, seq.get(0)));
        }

        @Test
        @Tag("Q6")
        public void addAllDoublesEmpty() {
            var seq = NumericSeq.doubles(32.);
            var seq2 = NumericSeq.doubles();
            seq.addAll(seq2);
            assertAll(() -> assertEquals(1, seq.size()), () -> assertEquals(32., seq.get(0)));
        }

        @Test
        @Timeout(1)
        @Tag("Q6")
        public void addAllIntsBig() {
            var seq = NumericSeq.ints();
            IntStream.range(0, 1_000_000).forEach(seq::add);
            var seq2 = NumericSeq.ints();
            IntStream.range(1_000_000, 2_000_000).forEach(seq2::add);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(2_000_000, seq.size()),
                    () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals(i, seq.get(i))));
        }

        @Test
        @Timeout(1)
        @Tag("Q6")
        public void addAllLongsBig() {
            var seq = NumericSeq.longs();
            LongStream.range(0, 1_000_000).forEach(seq::add);
            var seq2 = NumericSeq.longs();
            LongStream.range(1_000_000, 2_000_000).forEach(seq2::add);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(2_000_000, seq.size()),
                    () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals((long) i, seq.get(i))));
        }

        @Test
        @Timeout(1)
        @Tag("Q6")
        public void addAllDoublesBig() {
            var seq = NumericSeq.doubles();
            IntStream.range(0, 1_000_000).mapToDouble(i -> i).forEach(seq::add);
            var seq2 = NumericSeq.doubles();
            IntStream.range(1_000_000, 2_000_000).mapToDouble(i -> i).forEach(seq2::add);
            seq.addAll(seq2);
            assertAll(() -> assertEquals(2_000_000, seq.size()),
                    () -> IntStream.range(0, 2_000_000).forEach(i -> assertEquals((double) i, seq.get(i))));
        }

        @Test
        @Tag("Q6")
        public void addAllPrecondition() {
            var seq = NumericSeq.longs(65L, 67L);
            assertThrows(NullPointerException.class, () -> seq.addAll(null));
        }
    }

    // Q7
    @Nested
    class Q7 {
        @Test
        @Tag("Q7")
        public void map() {
            var seq = NumericSeq.longs();
            seq.add(45L);
            seq.add(37L);
            NumericSeq<Double> seq2 = seq.map(l -> (double) l, NumericSeq::doubles);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(45., seq2.get(0)),
                    () -> assertEquals(37., seq2.get(1)));
        }

        @Test
        @Tag("Q7")
        public void mapOfInts() {
            var seq = NumericSeq.ints(45, 37);
            NumericSeq<Integer> seq2 = seq.map(i -> 2 * i, NumericSeq::ints);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(90, seq2.get(0)),
                    () -> assertEquals(74, seq2.get(1)), () -> assertNotSame(seq, seq2));
        }

        @Test
        @Tag("Q7")
        public void mapOfLongs() {
            var seq = NumericSeq.longs(45L, 37L);
            NumericSeq<Long> seq2 = seq.map(l -> 2L * l, NumericSeq::longs);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(90L, seq2.get(0)),
                    () -> assertEquals(74L, seq2.get(1)), () -> assertNotSame(seq, seq2));
        }

        @Test
        @Tag("Q7")
        public void mapOfDoubles() {
            var seq = NumericSeq.doubles(45., 37.);
            NumericSeq<Double> seq2 = seq.map(d -> 2.0 * d, NumericSeq::doubles);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(90., seq2.get(0)),
                    () -> assertEquals(74., seq2.get(1)), () -> assertNotSame(seq, seq2));
        }

        @Test
        @Tag("Q7")
        public void mapOfIntsToOtherPrimitives() {
            var seq = NumericSeq.ints(45, 37);
            var seq2 = seq.map(i -> (long) i, NumericSeq::longs);
            var seq3 = seq.map(i -> (double) i, NumericSeq::doubles);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(45L, seq2.get(0)),
                    () -> assertEquals(37L, seq2.get(1)), () -> assertEquals(2, seq3.size()),
                    () -> assertEquals(45., seq3.get(0)), () -> assertEquals(37., seq3.get(1)));
        }

        @Test
        @Tag("Q7")
        public void mapOfLongsToOtherPrimitives() {
            var seq = NumericSeq.longs(45L, 37L);
            var seq2 = seq.map(l -> (int) (long) l, NumericSeq::ints);
            var seq3 = seq.map(l -> (double) l, NumericSeq::doubles);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(45, seq2.get(0)),
                    () -> assertEquals(37, seq2.get(1)), () -> assertEquals(2, seq3.size()), () -> assertEquals(45., seq3.get(0)),
                    () -> assertEquals(37., seq3.get(1)));
        }

        @Test
        @Tag("Q7")
        public void mapOfDoublesToOtherPrimitives() {
            var seq = NumericSeq.doubles(45., 37.);
            var seq2 = seq.map(d -> (int) (double) d, NumericSeq::ints);
            var seq3 = seq.map(d -> (long) (double) d, NumericSeq::longs);
            assertAll(() -> assertEquals(2, seq2.size()), () -> assertEquals(45, seq2.get(0)),
                    () -> assertEquals(37, seq2.get(1)), () -> assertEquals(2, seq3.size()), () -> assertEquals(45L, seq3.get(0)),
                    () -> assertEquals(37L, seq3.get(1)));
        }

        @Test
        @Tag("Q7")
        public void mapNotTheSame() {
            var seq = NumericSeq.longs(42L);
            var seq2 = seq.map((Object o) -> 42, NumericSeq::ints);
            assertNotSame(seq, seq2);
        }

        @Test
        @Tag("Q7")
        public void mapPreconditions() {
            var seq = NumericSeq.longs();
            assertAll(() -> assertThrows(NullPointerException.class, () -> seq.map(null, NumericSeq::ints)),
                    () -> assertThrows(NullPointerException.class, () -> seq.map(i -> i, null)));
        }
    }

    // Q8
    @Nested
    class Q8 {
        @Test
        @Tag("Q8")
        public void toNumericSeq() {
            var seq = IntStream.range(0, 10).boxed().collect(NumericSeq.toNumericSeq(NumericSeq::ints));
            assertAll(() -> assertEquals(10, seq.size()),
                    () -> IntStream.range(0, 10).forEach(i -> assertEquals(i, seq.get(i))));
        }

        @Test
        @Tag("Q8")
        public void toNumericSeqMutable() {
            var seq = Stream.of(12L, 45L).collect(NumericSeq.toNumericSeq(NumericSeq::longs));
            seq.add(99L);
            assertAll(() -> assertEquals(3, seq.size()), () -> assertEquals(12L, seq.get(0)),
                    () -> assertEquals(45L, seq.get(1)), () -> assertEquals(99L, seq.get(2)));
        }

        @Test
        @Tag("Q8")
        public void toNumericSeqParallelInts() {
            var seq = IntStream.range(0, 1_000_000).parallel().boxed().collect(NumericSeq.toNumericSeq(NumericSeq::ints));
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals(i, seq.get(i))));
        }

        @Test
        @Tag("Q8")
        public void toNumericSeqParallelLongs() {
            var seq = LongStream.range(0, 1_000_000).parallel().boxed().collect(NumericSeq.toNumericSeq(NumericSeq::longs));
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((long) i, seq.get(i))));
        }

        @Test
        @Tag("Q8")
        public void toNumericSeqParallelDoubles() {
            var seq = IntStream.range(0, 1_000_000).parallel().mapToObj(i -> (double) i)
                    .collect(NumericSeq.toNumericSeq(NumericSeq::doubles));
            assertAll(() -> assertEquals(1_000_000, seq.size()),
                    () -> IntStream.range(0, 1_000_000).forEach(i -> assertEquals((double) i, seq.get(i))));
        }

        @Test
        @Tag("Q8")
        public void toNumericSeqPreconditions() {
            assertThrows(NullPointerException.class, () -> NumericSeq.toNumericSeq(null));
        }

        @Test
        @Tag("Q8")
        public void toNumericVersusNull() {
            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> Stream.of(12, null).collect(NumericSeq.toNumericSeq(NumericSeq::ints))),
                    () -> assertThrows(NullPointerException.class,
                            () -> Stream.of(12L, null).collect(NumericSeq.toNumericSeq(NumericSeq::longs))),
                    () -> assertThrows(NullPointerException.class,
                            () -> Stream.of(12., null).collect(NumericSeq.toNumericSeq(NumericSeq::doubles))));
        }
    }

    // Q9
    @Nested
    class Q9 {
        @Test
        @Tag("Q9")
        public void stream() {
            var seq = NumericSeq.longs();
            seq.add(12L);
            seq.add(1L);
            assertEquals(List.of(12L, 1L), seq.stream().toList());
        }

        @Test
        @Tag("Q9")
        public void streamCount() {
            var seq = NumericSeq.ints(2, 3, 4);
            assertEquals(3, seq.stream().map(__ -> fail()).count());
        }

        @Test
        @Tag("Q9")
        public void streamParallel() {
            var seq = IntStream.range(0, 1_000_000).boxed().collect(NumericSeq.toNumericSeq(NumericSeq::ints));
            var thread = Thread.currentThread();
            var otherThreadCount = seq.stream().parallel().mapToInt(__ -> thread != Thread.currentThread() ? 1 : 0).sum();
            assertNotEquals(0, otherThreadCount);
        }

        @Test
        @Tag("Q9")
        public void streamMutation() {
            var seq = NumericSeq.doubles();
            seq.add(32.);
            var stream = seq.stream();
            seq.add(64.);
            assertEquals(List.of(32.), stream.toList());
        }

        @Test
        @Tag("Q9")
        public void streamDontSplitIfNotEnoughElements() {
            var seq = NumericSeq.ints();
            IntStream.range(0, 512).forEach(seq::add);
            assertNull(seq.stream().spliterator().trySplit());
        }

        @Test
        @Tag("Q9")
        public void streamSplitIfEnoughElements() {
            var seq = NumericSeq.ints();
            IntStream.range(0, 2_048).forEach(seq::add);
            assertNotNull(seq.stream().spliterator().trySplit());
        }

        @Test
        @Tag("Q9")
        public void streamNotParallelByDefault() {
            var stream = NumericSeq.longs(200L).stream();
            assertFalse(stream.isParallel());
        }

        @Test
        @Tag("Q9")
        public void streamCharacteristics() {
            var spliterator = NumericSeq.longs().stream().spliterator();
            assertAll(() -> spliterator.hasCharacteristics(Spliterator.NONNULL),
                    () -> spliterator.hasCharacteristics(Spliterator.ORDERED),
                    () -> spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
        }
    }
     */
}