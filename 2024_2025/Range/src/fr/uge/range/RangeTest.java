package fr.uge.range;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("preview")
public class RangeTest {

    @Nested
    public class Q1 {
        @Test
        public void simpleRange() {
            var range = Range.of(2, 5);
            assertAll(
                    () -> assertEquals(3, range.size()),
                    () -> assertEquals("[2, 3, 4]", "" + range)
            );
        }

        @Test
        public void negativeRange() {
            var range = Range.of(-3, 1);
            assertAll(
                    () -> assertEquals(4, range.size()),
                    () -> assertEquals("[-3, -2, -1, 0]", "" + range)
            );
        }

        @Test
        public void negativeRange2() {
            var range = Range.of(-6, -1);
            assertAll(
                    () -> assertEquals(5, range.size()),
                    () -> assertEquals("[-6, -5, -4, -3, -2]", "" + range)
            );
        }

        @Test
        public void emptyRange() {
            var range = Range.of(3, 3);
            assertAll(
                    () -> assertEquals(0, range.size()),
                    () -> assertEquals("[]", "" + range)
            );
        }

        @Test
        public void bigRange() {
            var range = Range.of(0, Integer.MAX_VALUE);
            assertEquals(Integer.MAX_VALUE, range.size());
        }

        @Test
        public void badRange() {
            assertThrows(IllegalArgumentException.class, () -> Range.of(2, 1));
        }

        @Test
        public void badRange2() {
            assertThrows(IllegalArgumentException.class, () -> Range.of(-1, -2));
        }

        @Test
        public void rangeTooBig() {
            assertThrows(IllegalArgumentException.class, () -> Range.of(Integer.MIN_VALUE, Integer.MAX_VALUE));
        }

        @Test
        public void rangeTooBig2() {
            assertThrows(IllegalArgumentException.class, () -> Range.of(Integer.MIN_VALUE, 0));
        }

        @Test
        public void rangeTooBig3() {
            assertThrows(IllegalArgumentException.class, () -> Range.of(-1, Integer.MAX_VALUE));
        }

        @Test
        public void qualityOfImplementation() {
            assertAll(
                    () -> assertTrue(Range.class.accessFlags().contains(AccessFlag.PUBLIC)),
                    () -> assertTrue(Range.class.accessFlags().contains(AccessFlag.FINAL)),
                    () -> assertEquals(0, Range.class.getConstructors().length),
                    () -> assertEquals(2, Range.class.getDeclaredFields().length),
                    () -> assertTrue(Arrays.stream(Range.class.getDeclaredFields()).allMatch(f -> f.accessFlags().contains(AccessFlag.PRIVATE))),
                    () -> assertTrue(Arrays.stream(Range.class.getDeclaredFields()).allMatch(f -> f.accessFlags().contains(AccessFlag.FINAL))),
                    () -> assertTrue(Arrays.stream(Range.class.getDeclaredFields()).allMatch(f -> f.getType().isPrimitive()))
            );
        }
    }

    @Nested
    public class Q2 {
        @Test
        public void rangeLoop() {
            var range = Range.of(2, 5);
            var sum = 0;
            for (var value : range) {
                sum += value;
            }
            assertEquals(2 + 3 + 4, sum);
        }

        @Test
        public void negativeRangeLoop() {
            var range = Range.of(-2, 3);
            var sum = 0;
            for (var value : range) {
                sum += value;
            }
            assertEquals(-2 + -1 + 0 + 1 + 2, sum);
        }

        @Test
        public void negativeRangeLoop2() {
            var range = Range.of(-4, -1);
            var sum = 0;
            for (var value : range) {
                sum += value;
            }
            assertEquals(-4 + -3 + -2, sum);
        }

        @Test
        public void emptyeRangeLoop2() {
            var range = Range.of(3, 3);
            var sum = 0;
            for (var value : range) {
                sum += value;
            }
            assertEquals(0, sum);
        }

        @Test
        public void rangeWhileIterator() {
            var range = Range.of(2, 5);
            var iterator = range.iterator();
            var sum = 0;
            while (iterator.hasNext()) {
                var value = iterator.next();
                sum += value;
            }
            assertEquals(2 + 3 + 4, sum);
        }

        @Test
        public void rangeIteratorNextInts() {
            var range = Range.of(2, 5);
            var iterator = range.iterator();
            assertEquals(2, iterator.next());
            assertEquals(3, iterator.next());
            assertEquals(4, iterator.next());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        public void rangeLoopALot() {
            var list = Range.of(0, 1_000_000);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                var index = 0;
                for (var value : list) {
                    assertEquals(index++, value);
                }
            });
        }
    }

    @Nested
    public class Q3 {
        @Test
        public void rangeList() {
            var list = Range.of(2, 5);

            assertAll(
                    () -> assertFalse(list.isEmpty()),
                    () -> assertEquals(2, list.getFirst()),
                    () -> assertEquals(4, list.getLast()),
                    () -> assertTrue(list.contains(3)),
                    () -> assertTrue(list.containsAll(List.of(2, 3))),
                    () -> assertArrayEquals(new Object[]{2, 3, 4}, list.toArray()),
                    () -> assertArrayEquals(new Integer[]{2, 3, 4}, list.toArray(Integer[]::new)),
                    () -> assertEquals(1, list.indexOf(3)),
                    () -> assertEquals(0, list.lastIndexOf(2))
            );
        }

        @Test
        public void negativeRangeList() {
            var list = Range.of(-2, 4);

            assertAll(
                    () -> assertFalse(list.isEmpty()),
                    () -> assertEquals(-2, list.getFirst()),
                    () -> assertEquals(3, list.getLast()),
                    () -> assertTrue(list.contains(0)),
                    () -> assertTrue(list.containsAll(List.of(-1, 1))),
                    () -> assertArrayEquals(new Object[]{-2, -1, 0, 1, 2, 3}, list.toArray()),
                    () -> assertArrayEquals(new Integer[]{-2, -1, 0, 1, 2, 3}, list.toArray(Integer[]::new)),
                    () -> assertEquals(5, list.indexOf(3)),
                    () -> assertEquals(4, list.lastIndexOf(2))
            );
        }

        @Test
        public void negativeRangeList2() {
            var list = Range.of(-3, -1);

            assertAll(
                    () -> assertFalse(list.isEmpty()),
                    () -> assertEquals(-3, list.getFirst()),
                    () -> assertEquals(-2, list.getLast()),
                    () -> assertTrue(list.contains(-2)),
                    () -> assertTrue(list.containsAll(List.of(-2, -3))),
                    () -> assertArrayEquals(new Object[]{-3, -2}, list.toArray()),
                    () -> assertArrayEquals(new Integer[]{-3, -2}, list.toArray(Integer[]::new)),
                    () -> assertEquals(0, list.indexOf(-3)),
                    () -> assertEquals(1, list.lastIndexOf(-2))
            );
        }

        @Test
        public void rangeListNotFound() {
            var list = Range.of(1, 5);

            assertAll(
                    () -> assertFalse(list.contains("foo")),
                    () -> assertFalse(list.containsAll(List.of("foo"))),
                    () -> assertEquals(-1, list.indexOf("bar")),
                    () -> assertEquals(-1, list.lastIndexOf("baz"))
            );
        }

        @Test
        public void emptyRangeList() {
            var list = Range.of(3, 3);

            assertAll(
                    () -> assertTrue(list.isEmpty()),
                    () -> assertThrows(NoSuchElementException.class, list::getFirst),
                    () -> assertThrows(NoSuchElementException.class, list::getLast),
                    () -> assertFalse(list.contains(0)),
                    () -> assertFalse(list.containsAll(List.of(0, 1))),
                    () -> assertArrayEquals(new Object[0], list.toArray()),
                    () -> assertArrayEquals(new Integer[0], list.toArray(Integer[]::new)),
                    () -> assertEquals(-1, list.indexOf(3)),
                    () -> assertEquals(-1, list.lastIndexOf(1))
            );
        }

        @Test
        public void rangeNonModifiable() {
            var list = Range.of(3, 5);

            assertAll(
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.add(1)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.addAll(List.of(1, 2))),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.addFirst(1)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.add(0, 1)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.addAll(0, List.of(0, 1))),
                    () -> assertThrows(UnsupportedOperationException.class, list::removeFirst),
                    () -> assertThrows(UnsupportedOperationException.class, list::removeLast),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.remove(3)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.remove((Integer) 3)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.removeAll(List.of(3, 4))),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.removeIf(_ -> true)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.retainAll(List.of(1, 4))),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.set(1, 10)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(x -> x + 1)),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list.sort(null)),
                    () -> assertThrows(UnsupportedOperationException.class, list::clear)
            );
        }

        @Test
        public void rangeListALot() {
            var list = Range.of(0, 1_000_000);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var i = 0; i < list.size(); i++) {
                    assertEquals(i, list.get(i));
                }
            });
        }

        @Test
        public void rangeListALot2() {
            var list = Range.of(1_000_000, 2_000_000);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var i = 0; i < list.size(); i++) {
                    assertEquals(1_000_000 + i, list.get(i));
                }
            });
        }

        @Test
        public void rangeListPrecondition() {
            var list = Range.of(3, 5);

            assertAll(
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(2))
            );
        }

        @Test
        public void qualityOfImplementation() {
            assertTrue(List.class.isAssignableFrom(Range.class));
            assertTrue(RandomAccess.class.isAssignableFrom(Range.class));
        }
    }

    @Nested
    public class Q4 {
        @Test
        public void rangeStream() {
            var range = Range.of(2, 5);

            assertAll(
                    () -> assertEquals(List.of(2, 3, 4), range.stream().toList()),
                    () -> assertEquals(3L, range.stream().count()),
                    () -> assertEquals(2, range.stream().findFirst().orElseThrow()),
                    () -> assertTrue(range.stream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeStream() {
            var range = Range.of(-2, 2);

            assertAll(
                    () -> assertEquals(List.of(-2, -1, 0, 1), range.stream().toList()),
                    () -> assertEquals(4L, range.stream().count()),
                    () -> assertEquals(-2, range.stream().findFirst().orElseThrow()),
                    () -> assertTrue(range.stream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeStream2() {
            var range = Range.of(-6, -1);

            assertAll(
                    () -> assertEquals(List.of(-6, -5, -4, -3, -2), range.stream().toList()),
                    () -> assertEquals(5L, range.stream().count()),
                    () -> assertEquals(-6, range.stream().findFirst().orElseThrow()),
                    () -> assertTrue(range.stream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeStreamMap() {
            var range = Range.of(2, 5);

            assertEquals(3L, range.stream().map(_ -> fail()).count());
        }

        @Test
        public void rangeStreamSorted() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.stream().sorted().toList());
        }

        @Test
        public void rangeStreamSortedAlot() {
            var range = Range.of(0, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                var sum = 0;
                for (var i = 0; i < 1_000; i++) {
                    sum += range.stream().sorted().findFirst().orElseThrow();
                }
                assertEquals(0, sum);
            });
        }

        @Test
        public void rangeStreamDistinct() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.stream().distinct().toList());
        }

        @Test
        public void rangeStreamCharacteristics() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.stream().spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void rangeParallelStream() {
            var range = Range.of(2, 5);

            assertAll(
                    () -> assertEquals(List.of(2, 3, 4), range.parallelStream().toList()),
                    () -> assertEquals(3L, range.parallelStream().count()),
                    () -> assertEquals(2, range.parallelStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.parallelStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeParallelStream() {
            var range = Range.of(-2, 2);

            assertAll(
                    () -> assertEquals(List.of(-2, -1, 0, 1), range.parallelStream().toList()),
                    () -> assertEquals(4L, range.parallelStream().count()),
                    () -> assertEquals(-2, range.parallelStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.parallelStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeParallelStream2() {
            var range = Range.of(-6, -1);

            assertAll(
                    () -> assertEquals(List.of(-6, -5, -4, -3, -2), range.parallelStream().toList()),
                    () -> assertEquals(5L, range.parallelStream().count()),
                    () -> assertEquals(-6, range.parallelStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.parallelStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeParallelStreamMap() {
            var range = Range.of(2, 5);

            assertEquals(3L, range.parallelStream().map(_ -> fail()).count());
        }

        @Test
        public void rangeParallelStreamSorted() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.parallelStream().sorted().toList());
        }

        @Test
        public void rangeParallelStreamDistinct() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.parallelStream().distinct().toList());
        }

        @Test
        public void rangeParallelStreamCharacteristics() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.parallelStream().spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void rangeParallelStreamUseSeveralThreads() {
            var range = Range.of(0, 1_000_000);
            var set = new CopyOnWriteArraySet<Thread>();
            var list = range.parallelStream().peek(_ -> set.add(Thread.currentThread())).map(v -> v * 2).toList();

            assertAll(
                    () -> assertEquals(1_000_000, list.size()),
                    () -> assertTrue(set.size() > 1)
            );
        }

        @Test
        public void rangeParallelStreamSplit() {
            var range = Range.of(0, 10);

            var spliterator = range.parallelStream().spliterator();
            var spliterator2 = spliterator.trySplit();
            assertAll(
                    () -> assertEquals(5L, spliterator.estimateSize()),
                    () -> assertEquals(5L, spliterator2.estimateSize())
            );
        }

        @Test
        public void rangeParallelStreamSplit2() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.parallelStream().spliterator();
            var spliterator2 = spliterator.trySplit();
            assertAll(
                    () -> assertEquals(1073741824L, spliterator.estimateSize()),
                    () -> assertEquals(1073741823L, spliterator2.estimateSize())
            );
        }

        @Test
        public void rangeParallelStreamSplitOneValue() {
            var range = Range.of(0, 1);
            var spliterator = range.parallelStream().spliterator();

            assertNull(spliterator.trySplit());
        }

        @Test
        public void rangeParallelStreamSplitOrder() {
            var range = Range.of(0, 10);

            var spliterator = range.parallelStream().spliterator();
            var spliterator2 = spliterator.trySplit();

            var box = new Object() {
                int v1;
                int v2;
            };
            spliterator.tryAdvance(i -> box.v1 = i);
            spliterator2.tryAdvance(i -> box.v2 = i);

            assertAll(
                    () -> assertEquals(5, box.v1),
                    () -> assertEquals(0, box.v2)
            );
        }
    }

    @Nested
    public class Q5 {
        @Test
        public void rangeTimes() {
            var list = Stream.of("foo", "bar").gather(Range.of(0, 3).times(String::charAt)).toList();

            assertEquals(List.of('f', 'o', 'o', 'b', 'a', 'r'), list);
        }

        @Test
        public void rangeTimes2() {
            var list = Stream.of("foo", "bar").gather(Range.of(0, 3).times(String::substring)).toList();

            assertEquals(List.of("foo", "oo", "o", "bar", "ar", "r"), list);
        }

        @Test
        public void rangeTimesLimit() {
            var list = Stream.of("foo", "bar").gather(Range.of(0, 3).times(String::charAt)).limit(4).toList();

            assertEquals(List.of('f', 'o', 'o', 'b'), list);
        }

        @Test
        public void rangeTimesEmpty() {
            class A {
                int identity(int index) {
                    throw new AssertionError();
                }
            }

            var list = Stream.<A>empty().gather(Range.of(0, 3).times(A::identity)).toList();

            assertEquals(List.of(), list);
        }

        @Test
        public void rangeTimesCount() {
            class A {
                int counter;

                int identity(int index) {
                    counter++;
                    return index;
                }
            }

            var a = new A();
            var list = Stream.of(a).gather(Range.of(0, 100).times(A::identity)).limit(10).toList();

            assertAll(
                    () -> assertEquals(10, a.counter),
                    () -> assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), list)
            );
        }

        @Test
        public void rangeTimesParallel() {
            var set = new CopyOnWriteArraySet<Thread>();

            class A {
                int identity(int index) {
                    set.add(Thread.currentThread());
                    return index;
                }
            }

            var list = List.of(new A(), new A(), new A(), new A())
                    .parallelStream()
                    .gather(Range.of(0, 1_000_000).times(A::identity))
                    .toList();

            assertAll(
                    () -> assertEquals(4_000_000, list.size()),
                    () -> assertTrue(set.size() > 1)
            );
        }

        @Test
        public void rangeTimesExample() {
            record Pair(int v1, int v2) {
            }
            var list = Range.of(0, 2).stream().gather(Range.of(0, 2).times(Pair::new)).toList();

            assertEquals(List.of(new Pair(0, 0), new Pair(0, 1), new Pair(1, 0), new Pair(1, 1)), list);
        }

        @Test
        public void qualityOfImplementation() {
            var method = Arrays.stream(Range.class.getMethods())
                    .filter(m -> m.getName().equals("times"))
                    .findFirst().orElseThrow();
            var funType = (ParameterizedType) method.getGenericParameterTypes()[0];
            var typeArgument = funType.getActualTypeArguments();
            System.out.println(Arrays.toString(typeArgument));
            assertAll(
                    () -> assertTrue(((Class<?>) funType.getRawType()).accessFlags().contains(AccessFlag.PUBLIC)),
                    () -> assertEquals(2, typeArgument.length),
                    () -> assertTrue(Arrays.stream(typeArgument).noneMatch(t -> t instanceof TypeVariable<?>))
            );
        }
    }

    /*


    @Nested
    public class Q6 {
        @Test
        public void rangeIntStream() {
            var range = Range.of(2, 5);

            assertAll(
                    () -> assertEquals(List.of(2, 3, 4), range.intStream().boxed().toList()),
                    () -> assertEquals(3L, range.intStream().count()),
                    () -> assertEquals(2, range.intStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeIntStream() {
            var range = Range.of(-2, 2);

            assertAll(
                    () -> assertEquals(List.of(-2, -1, 0, 1), range.intStream().boxed().toList()),
                    () -> assertEquals(4L, range.intStream().count()),
                    () -> assertEquals(-2, range.intStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeIntStream2() {
            var range = Range.of(-6, -1);

            assertAll(
                    () -> assertEquals(List.of(-6, -5, -4, -3, -2), range.intStream().boxed().toList()),
                    () -> assertEquals(5L, range.intStream().count()),
                    () -> assertEquals(-6, range.intStream().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeIntStreamMap() {
            var range = Range.of(2, 5);

            assertEquals(3L, range.intStream().map(_ -> fail()).count());
        }

        @Test
        public void rangeIntStreamSorted() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.intStream().sorted().boxed().toList());
        }

        @Test
        public void rangeIntStreamSortedAlot() {
            var range = Range.of(0, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                var sum = 0;
                for (var i = 0; i < 1_000; i++) {
                    sum += range.intStream().sorted().findFirst().orElseThrow();
                }
                assertEquals(0, sum);
            });
        }

        @Test
        public void rangeIntStreamDistinct() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.intStream().distinct().boxed().toList());
        }

        @Test
        public void rangeIntStreamCharacteristics() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.intStream().spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void rangeParallelIntStream() {
            var range = Range.of(2, 5);

            assertAll(
                    () -> assertEquals(List.of(2, 3, 4), range.intStream().parallel().boxed().toList()),
                    () -> assertEquals(3L, range.intStream().parallel().count()),
                    () -> assertEquals(2, range.intStream().parallel().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().parallel().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeParallelIntStream() {
            var range = Range.of(-2, 2);

            assertAll(
                    () -> assertEquals(List.of(-2, -1, 0, 1), range.intStream().parallel().boxed().toList()),
                    () -> assertEquals(4L, range.intStream().parallel().count()),
                    () -> assertEquals(-2, range.intStream().parallel().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().parallel().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeNegativeParallelIntStream2() {
            var range = Range.of(-6, -1);

            assertAll(
                    () -> assertEquals(List.of(-6, -5, -4, -3, -2), range.intStream().parallel().boxed().toList()),
                    () -> assertEquals(5L, range.intStream().parallel().count()),
                    () -> assertEquals(-6, range.intStream().parallel().findFirst().orElseThrow()),
                    () -> assertTrue(range.intStream().parallel().allMatch(Objects::nonNull))
            );
        }

        @Test
        public void rangeParallelIntStreamMap() {
            var range = Range.of(2, 5);

            assertEquals(3L, range.intStream().parallel().map(_ -> fail()).count());
        }

        @Test
        public void rangeParallelIntStreamSorted() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.intStream().parallel().sorted().boxed().toList());
        }

        @Test
        public void rangeParallelIntStreamDistinct() {
            var range = Range.of(5, 10);

            assertEquals(List.of(5, 6, 7, 8, 9), range.intStream().parallel().distinct().boxed().toList());
        }

        @Test
        public void rangeParallelIntStreamCharacteristics() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.intStream().parallel().spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void rangeParallelIntStreamUseSeveralThreads() {
            var range = Range.of(0, 1_000_000);
            var set = new CopyOnWriteArraySet<Thread>();
            var list = range.intStream().parallel()
                    .peek(_ -> set.add(Thread.currentThread())).mapToObj(v -> v * 2)
                    .toList();

            assertAll(
                    () -> assertEquals(1_000_000, list.size()),
                    () -> assertTrue(set.size() > 1)
            );
        }

        @Test
        public void rangeParallelIntStreamSplit2() {
            var range = Range.of(0, 10);

            var spliterator = range.intStream().parallel().spliterator();
            var spliterator2 = spliterator.trySplit();
            assertAll(
                    () -> assertEquals(5L, spliterator.estimateSize()),
                    () -> assertEquals(5L, spliterator2.estimateSize())
            );
        }

        @Test
        public void rangeParallelIntStreamSplit() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.intStream().parallel().spliterator();
            var spliterator2 = spliterator.trySplit();
            assertAll(
                    () -> assertEquals(1073741824L, spliterator.estimateSize()),
                    () -> assertEquals(1073741823L, spliterator2.estimateSize())
            );
        }

        @Test
        public void rangeParallelIntStreamSplitOrder() {
            var range = Range.of(0, 10);

            var spliterator = range.intStream().parallel().spliterator();
            var spliterator2 = spliterator.trySplit();

            var box = new Object() {
                int v1;
                int v2;
            };
            spliterator.tryAdvance((int i) -> box.v1 = i);
            spliterator2.tryAdvance((int i) -> box.v2 = i);

            assertAll(
                    () -> assertEquals(5, box.v1),
                    () -> assertEquals(0, box.v2)
            );
        }

        @Test
        public void qualityOfImplementation() throws NoSuchMethodException {
            var method = Range.class.getMethod("intStream");
            assertSame(IntStream.class, method.getReturnType());
        }
    }


    @Nested
    public class Q7 {
        @Test
        public void rangeContainsALot() {
            var range = Range.of(0, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.contains(i) ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeContainsALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.contains(i) ? 1 : 0;
                }
                assertEquals(999_998, sum);
            });
        }

        @Test
        public void rangeIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_998, sum);
            });
        }

        @Test
        public void rangeLastIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeLastIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += range.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_998, sum);
            });
        }
    }


    @Nested
    public class Q8 {
        @Test
        public void rangeSubListContainsALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.contains(i) ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListContainsALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.contains(i) ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubListIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubListLastIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListLastIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubListSubListContainsALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE).subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.contains(i) ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListSubListContainsALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE - 2).subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.contains(i) ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubListSubListIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE).subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListSubListIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE - 2).subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.indexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubListSubListLastIndexOfALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE).subList(0, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeSubListSubListLastIndexOfALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var list = range.subList(0, Integer.MAX_VALUE - 2).subList(2, Integer.MAX_VALUE / 2);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += list.lastIndexOf(i) != -1 ? 1 : 0;
                }
                assertEquals(999_996, sum);
            });
        }

        @Test
        public void rangeSubList() {
            var range = Range.of(2, 5);
            var list = range.subList(0, 3);

            assertAll(
                    () -> assertEquals(3, list.size()),
                    () -> assertEquals(List.of(2, 3, 4), list),
                    () -> assertEquals(2, list.getFirst()),
                    () -> assertEquals(4, list.getLast()),
                    () -> assertEquals(2, list.get(0)),
                    () -> assertEquals(3, list.get(1)),
                    () -> assertEquals(4, list.get(2))
            );
        }

        @Test
        public void rangeSubList2() {
            var range = Range.of(2, 5);
            var list = range.subList(1, 3);

            assertAll(
                    () -> assertEquals(2, list.size()),
                    () -> assertEquals(List.of(3, 4), list),
                    () -> assertEquals(3, list.getFirst()),
                    () -> assertEquals(4, list.getLast()),
                    () -> assertEquals(3, list.get(0)),
                    () -> assertEquals(4, list.get(1))
            );
        }

        @Test
        public void rangeSubListEmpty() {
            var range = Range.of(3, 3);
            var list = range.subList(0, 0);

            assertAll(
                    () -> assertEquals(0, list.size()),
                    () -> assertEquals(List.of(), list),
                    () -> assertThrows(NoSuchElementException.class, () -> list.getFirst()),
                    () -> assertThrows(NoSuchElementException.class, () -> list.getLast()),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(0)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(1))
            );
        }

        @Test
        public void rangeSubListPreconditions() {
            var range = Range.of(2, 5);
            var range2 = Range.of(3, 3);

            assertAll(
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> range.subList(0, 4)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> range.subList(-1, 1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> range2.subList(0, 1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> range2.subList(-1, 1))
            );
        }
    }


    @Nested
    public class Q9 {
        @Test
        public void rangeAsSet() {
            var range = Range.of(2, 5);
            var set = range.asSet();

            assertEquals(Set.of(2, 3, 4), set);
        }

        @Test
        public void rangeAsSetNegative() {
            var range = Range.of(-2, 2);
            var set = range.asSet();

            assertEquals(Set.of(-2, -1, 0, 1), set);
        }

        @Test
        public void rangeAsSetNegative2() {
            var range = Range.of(-5, -2);
            var set = range.asSet();

            assertEquals(Set.of(-5, -4, -3), set);
        }

        @Test
        public void rangeAsSetEmpty() {
            var range = Range.of(3, 3);
            var set = range.asSet();

            assertEquals(Set.of(), set);
        }

        @Test
        public void rangeAsSetIsNotAList() {
            var range = Range.of(3, 5);
            var set = range.asSet();

            assertNotEquals(List.of(3, 4), set);
        }

        @Test
        public void rangeAsSetIsOrdered() {
            var range = Range.of(10, 16);
            var set = range.asSet();

            assertEquals(List.of(10, 11, 12, 13, 14, 15), List.copyOf(set));
        }

        @Test
        public void rangeAsSetContainsALot() {
            var range = Range.of(0, Integer.MAX_VALUE);
            var set = range.asSet();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += set.contains(i) ? 1 : 0;
                }
                assertEquals(1_000_000, sum);
            });
        }

        @Test
        public void rangeAsSetContainsALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var set = range.asSet();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += set.contains(i) ? 1 : 0;
                }
                assertEquals(999_998, sum);
            });
        }

        @Test
        public void rangeAsSetContainsAllWithARangeALot() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var set = range.asSet();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 2; i < 1_000_000; i++) {
                    sum += set.containsAll(Range.of(2, i)) ? 1 : 0;
                }
                assertEquals(999_998, sum);
            });
        }

        @Test
        public void rangeAsSetContainsAllWithARangeALot2() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var set = range.asSet();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                int sum = 0;
                for (var i = 0; i < 1_000_000; i++) {
                    sum += set.containsAll(Range.of(0, i)) ? 1 : 0;
                }
                assertEquals(0, sum);
            });
        }

        @Test
        public void rangeAsSetContainsAllWithARangeALot3() {
            var range = Range.of(2, Integer.MAX_VALUE);
            var set = range.asSet();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                assertTrue(set.containsAll(range));
            });
        }

        @Test
        public void rangeAsSetStreamCharacteristics() {
            var range = Range.of(0, Integer.MAX_VALUE);

            var spliterator = range.asSet().stream().spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }
    }

     */
}