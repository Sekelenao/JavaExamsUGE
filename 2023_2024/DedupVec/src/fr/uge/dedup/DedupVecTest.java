package fr.uge.dedup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DedupVecTest {
    @Nested
    public class Q1 {
        @Test
        public void simple() {
            record Point(int x, int y) {
            }
            DedupVec<Point> dedupVec = new DedupVec<Point>();
            dedupVec.add(new Point(1, 2));
            dedupVec.add(new Point(3, 5));

            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertEquals(new Point(1, 2), dedupVec.get(0)),
                    () -> assertEquals(new Point(3, 5), dedupVec.get(1))
            );
        }

        @Test
        public void dedupVecOfIntegers() {
            DedupVec<Integer> dedupVec = new DedupVec<Integer>();
            dedupVec.add(12);
            dedupVec.add(1012);
            dedupVec.add(2048);

            assertAll(
                    () -> assertEquals(3, dedupVec.size()),
                    () -> assertEquals(12, dedupVec.get(0)),
                    () -> assertEquals(1012, dedupVec.get(1)),
                    () -> assertEquals(2048, dedupVec.get(2))
            );
        }

        @Test
        public void dedupVecOfStrings() {
            DedupVec<String> dedupVec = new DedupVec<String>();
            dedupVec.add("foo");
            dedupVec.add("foo");

            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertEquals("foo", dedupVec.get(0)),
                    () -> assertEquals("foo", dedupVec.get(1))
            );
        }

        @Test
        public void dedupVecEmpty() {
            var dedupVec = new DedupVec<>();
            assertEquals(0, dedupVec.size());
        }

        @Test
        public void dedupVecPreconditions() {
            var dedupVec = new DedupVec<>();

            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.add(null)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> dedupVec.get(0)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> dedupVec.get(-1))
            );
        }

        @Test
        public void dedupVecQualityOfImplementation() {
            assertAll(
                    () -> assertTrue(DedupVec.class.accessFlags().contains(AccessFlag.PUBLIC)),
                    () -> assertTrue(DedupVec.class.accessFlags().contains(AccessFlag.FINAL)),
                    () -> assertEquals(1, DedupVec.class.getConstructors().length)
            );
        }
    }

    @Nested
    public class Q2 {
        @Test
        public void dedupVecOfPoints() {
            record Point(int x, int y) {
            }
            var dedupVec = new DedupVec<Point>();
            dedupVec.add(new Point(1, 2));
            dedupVec.add(new Point(3, 5));
            dedupVec.add(new Point(1, 2));

            assertAll(
                    () -> assertEquals(3, dedupVec.size()),
                    () -> assertEquals(new Point(1, 2), dedupVec.get(0)),
                    () -> assertEquals(new Point(3, 5), dedupVec.get(1)),
                    () -> assertEquals(new Point(1, 2), dedupVec.get(2)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(2))
            );
        }

        @Test
        public void dedupVecOfIntegers() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(12);
            dedupVec.add(1012);
            dedupVec.add(1012);

            assertAll(
                    () -> assertEquals(3, dedupVec.size()),
                    () -> assertEquals(12, dedupVec.get(0)),
                    () -> assertEquals(1012, dedupVec.get(1)),
                    () -> assertEquals(1012, dedupVec.get(2)),
                    () -> assertSame(dedupVec.get(1), dedupVec.get(2))
            );
        }

        @Test
        public void dedupVecOfStrings() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add(new String("foo"));
            dedupVec.add(new String("foo"));

            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertEquals("foo", dedupVec.get(0)),
                    () -> assertEquals("foo", dedupVec.get(1)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(1))
            );
        }

        @Test
        public void dedupVecSameValue() {
            record Value(int value) {
            }
            var dedupVec = new DedupVec<Value>();
            IntStream.range(0, 5).forEach(__ -> dedupVec.add(new Value(42)));

            assertAll(
                    () -> assertEquals(5, dedupVec.size()),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(1)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(2)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(3)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(4))
            );
        }

        @Test
        public void dedupVecAllSameValues() {
            record Empty() {
            }
            var dedupVec = new DedupVec<Empty>();
            for (var i = 0; i < 10; i++) {
                dedupVec.add(new Empty());
            }

            var value = dedupVec.get(0);
            for (var i = 0; i < 10; i++) {
                assertSame(value, dedupVec.get(i));
            }
        }

        @Test
        public void dedupVecDoNotStoreDuplicate() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.add(person1);
            dedupVec.add(person2);
            person2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void isAddFastEnough() {
            record Point(int x, int y) {
            }
            var dedupVec = new DedupVec<Point>();

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var i = 0; i < 100_000; i++) {
                    dedupVec.add(new Point(i, i));
                }
            });
        }

        @Test
        public void suddenDeath() {
            var instanceFields = Arrays.stream(DedupVec.class.getDeclaredFields())
                    .filter(f -> !f.accessFlags().contains(AccessFlag.STATIC))
                    .toList();
            assertAll(
                    () -> assertEquals(2, instanceFields.size()),
                    () -> assertTrue(instanceFields.stream().allMatch(f -> f.accessFlags().contains(AccessFlag.PRIVATE))),
                    () -> assertTrue(instanceFields.stream().allMatch(f -> f.accessFlags().contains(AccessFlag.FINAL))),
                    () -> assertTrue(instanceFields.stream().anyMatch(f -> List.class.isAssignableFrom(f.getType()))),
                    () -> assertTrue(instanceFields.stream().anyMatch(f -> Map.class.isAssignableFrom(f.getType())))
            );
        }
    }

    @Nested
    public class Q3 {
        @Test
        public void contains() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(1234);
            dedupVec.add(31);

            assertAll(
                    () -> assertTrue(dedupVec.contains(1234)),
                    () -> assertTrue(dedupVec.contains(31)),
                    () -> assertFalse(dedupVec.contains(12345)),
                    () -> assertFalse(dedupVec.contains(32))
            );
        }

        @Test
        public void containsString() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add("foo");
            dedupVec.add("bar");

            assertAll(
                    () -> assertTrue(dedupVec.contains(new String("foo"))),
                    () -> assertFalse(dedupVec.contains(new String("baz")))
            );
        }

        @Test
        public void containsSignature() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add("foo");
            dedupVec.add("bar");

            assertAll(
                    () -> assertFalse(dedupVec.contains(12)),
                    () -> assertFalse(dedupVec.contains(14L))
            );
        }

        @Test
        public void isContainsFastEnough() {
            record Point(int x, int y) {
            }
            var dedupVec = new DedupVec<Point>();
            new Random(0).ints(100_000, 0, 10).mapToObj(i -> new Point(i, i)).forEach(dedupVec::add);
            dedupVec.add(new Point(13, 13));

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                var golden = new Point(13, 13);
                for (var i = 0; i < 100_000; i++) {
                    assertTrue(dedupVec.contains(golden));
                }
            });
        }

        @Test
        public void isContainsFastEnough2() {
            record Point(int x, int y) {
            }
            var list = new Random(0).ints(100_000).mapToObj(i -> new Point(i, i)).toList();
            var dedupVec = new DedupVec<Point>();
            for (var element : list) {
                dedupVec.add(element);
            }

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var element : list) {
                    assertTrue(dedupVec.contains(element));
                }
            });
        }
    }

    @Nested
    public class Q4 {
        @Test
        public void addAllPoints() {
            record Point(int x, int y) {
            }
            var dedupVec = new DedupVec<Point>();
            dedupVec.add(new Point(1, 2));
            dedupVec.add(new Point(3, 4));
            var dedupVec2 = new DedupVec<Point>();
            dedupVec2.add(new Point(3, 4));
            dedupVec2.add(new Point(5, 6));
            dedupVec.addAll(dedupVec2);

            assertAll(
                    () -> assertEquals(4, dedupVec.size()),
                    () -> assertEquals(new Point(1, 2), dedupVec.get(0)),
                    () -> assertEquals(new Point(3, 4), dedupVec.get(1)),
                    () -> assertEquals(new Point(3, 4), dedupVec.get(2)),
                    () -> assertEquals(new Point(5, 6), dedupVec.get(3)),
                    () -> assertSame(dedupVec.get(1), dedupVec.get(2))
            );
        }

        @Test
        public void addAllIntegers() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(1024);
            dedupVec.add(2048);
            var dedupVec2 = new DedupVec<Integer>();
            dedupVec2.add(8192);
            dedupVec2.add(4096);
            dedupVec2.add(2048);
            dedupVec.addAll(dedupVec2);

            assertAll(
                    () -> assertEquals(5, dedupVec.size()),
                    () -> assertEquals(1024, dedupVec.get(0)),
                    () -> assertEquals(2048, dedupVec.get(1)),
                    () -> assertEquals(8192, dedupVec.get(2)),
                    () -> assertEquals(4096, dedupVec.get(3)),
                    () -> assertEquals(2048, dedupVec.get(4)),
                    () -> assertSame(dedupVec.get(1), dedupVec.get(4))
            );
        }

        @Test
        public void addAllIntegers2() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(1024);
            dedupVec.add(2048);
            var dedupVec2 = new DedupVec<Integer>();
            dedupVec2.add(8192);
            dedupVec2.add(4096);
            dedupVec2.add(2048);
            dedupVec.addAll(dedupVec2);

            assertSame(dedupVec.get(1), dedupVec.get(4));
        }

        @Test
        public void addAllLongs() {
            var dedupVec = new DedupVec<Long>();
            LongStream.range(1_000L, 1_100L).forEach(dedupVec::add);
            var dedupVec2 = new DedupVec<Long>();
            LongStream.range(1_100L, 1_200L).forEach(dedupVec2::add);
            dedupVec.addAll(dedupVec2);

            assertAll(
                    () -> assertEquals(200, dedupVec.size()),
                    () -> IntStream.range(0, 200).forEach(i -> assertEquals(1_000L + i, dedupVec.get(i), "" + i))
            );
        }

        @Test
        public void addAllStrings() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add(new String("foo"));
            dedupVec.add(new String("bar"));
            dedupVec.add(new String("baz"));
            var dedupVec2 = new DedupVec<String>();
            dedupVec2.add(new String("bar"));
            dedupVec.addAll(dedupVec2);

            assertAll(
                    () -> assertEquals(4, dedupVec.size()),
                    () -> assertEquals("foo", dedupVec.get(0)),
                    () -> assertEquals("bar", dedupVec.get(1)),
                    () -> assertEquals("baz", dedupVec.get(2)),
                    () -> assertEquals("bar", dedupVec.get(3)),
                    () -> assertSame(dedupVec.get(1), dedupVec.get(3))
            );
        }

        @Test
        public void addAllStrings2() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add(new String("foo"));
            dedupVec.add(new String("bar"));
            dedupVec.add(new String("baz"));
            var dedupVec2 = new DedupVec<String>();
            dedupVec2.add(new String("bar"));
            dedupVec.addAll(dedupVec2);

            assertSame(dedupVec.get(1), dedupVec.get(3));
        }

        @Test
        public void addAllSameValues() {
            record Empty() {
            }
            var dedupVec = new DedupVec<Empty>();
            var dedupVec2 = new DedupVec<Empty>();
            for (var i = 0; i < 5; i++) {
                dedupVec.add(new Empty());
                dedupVec2.add(new Empty());
            }
            dedupVec.addAll(dedupVec2);

            var value = dedupVec.get(0);
            for (var i = 0; i < dedupVec.size(); i++) {
                assertSame(value, dedupVec.get(i));
            }
        }

        @Test
        public void addAllThenAdd() {
            record Person(String name) {
            }
            var dedupVec = new DedupVec<Person>();
            var dedupVec2 = new DedupVec<Person>();
            var person1 = new Person("Bob");
            dedupVec2.add(person1);
            dedupVec.addAll(dedupVec2);
            dedupVec.add(new Person("Bob"));
            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertSame(person1, dedupVec.get(0)),
                    () -> assertSame(person1, dedupVec.get(1))
            );
        }

        @Test
        public void addAllDoNotStoreDuplicate() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.add(person1);
            var dedupVec2 = new DedupVec<Person>();
            dedupVec2.add(person2);
            dedupVec.addAll(dedupVec2);
            person2 = null;
            dedupVec2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void addAllDoNotStoreDuplicate2() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.add(person1);
            var dedupVec2 = new DedupVec<Person>();
            dedupVec2.add(person2);
            dedupVec2.add(person2);
            dedupVec.addAll(dedupVec2);
            person2 = null;
            dedupVec2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void addAllSignature() {
            var dedupVec = new DedupVec<>();
            dedupVec.add(499);
            var dedupVec2 = new DedupVec<Integer>();
            dedupVec2.add(499);
            dedupVec.addAll(dedupVec2);

            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertEquals(499, dedupVec.get(0)),
                    () -> assertEquals(499, dedupVec.get(1)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(1))
            );
        }

        @Test
        public void addAllPrecondition() {
            assertThrows(NullPointerException.class, () -> new DedupVec<>().addAll(null));
        }
    }

    @Nested
    public class Q5 {
        @Test
        public void newMapFromSet() {
            var set = Set.of("foo", "bar");
            var map = DedupVec.newMapFromSet(set);
            assertEquals(Map.of("foo", "foo", "bar", "bar"), map);
        }

        @Test
        public void newMapFromSetMixedValues() {
            var set = Set.of("foo", 42);
            var map = DedupVec.newMapFromSet(set);
            assertEquals(Map.of("foo", "foo", 42, 42), map);
        }

        @Test
        public void newMapFromSetEmpty() {
            var set = Set.of();
            var map = DedupVec.newMapFromSet(set);
            assertEquals(Map.of(), map);
        }

        @Test
        public void newMapFromSetKeySet() {
            var set = IntStream.range(0, 10).boxed().collect(toSet());
            var map = DedupVec.newMapFromSet(set);
            assertEquals(set, map.keySet());
        }

        @Test
        public void newMapFromSetValues() {
            var set = IntStream.range(0, 10).boxed().collect(toSet());
            var map = DedupVec.newMapFromSet(set);
            assertEquals(set, new HashSet<>(map.values()));
        }

        @Test
        public void newMapFromFixedSetOfInteger() {
            var map = DedupVec.newMapFromSet(Set.of(1, 2, 3));
            assertAll(
                    () -> assertEquals(Set.of(1, 2, 3), map.keySet()),
                    () -> assertEquals(Set.of(1, 2, 3), Set.copyOf(map.values())),
                    () -> assertEquals(Map.of(1, 1, 2, 2, 3, 3), map),
                    () -> assertEquals(Set.of(Map.entry(1, 1), Map.entry(2, 2), Map.entry(3, 3)), map.entrySet())
            );
        }

        @Test
        public void newMapFromFixedSetOfString() {
            var map = DedupVec.newMapFromSet(Set.of("foo", "bar", "baz"));
            assertAll(
                    () -> assertEquals(Set.of("foo", "bar", "baz"), map.keySet()),
                    () -> assertEquals(Set.of("foo", "bar", "baz"), Set.copyOf(map.values())),
                    () -> assertEquals(Map.of("foo", "foo", "bar", "bar", "baz", "baz"), map),
                    () -> assertEquals(Set.of(Map.entry("foo", "foo"), Map.entry("bar", "bar"), Map.entry("baz", "baz")), map.entrySet())
            );
        }

        @Test
        public void newMapFromSetIsAView() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            set.add(42);
            assertEquals(Map.of(42, 42), map);
        }

        @Test
        public void newMapFromSetKeySetIsAView() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var keySet = map.keySet();
            set.add(42);
            assertEquals(set, keySet);
        }

        @Test
        public void newMapFromSetValuesIsAView() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var values = map.values();
            set.add(42);
            assertEquals(List.of(42), List.copyOf(values));
        }

        @Test
        public void newMapFromSetEntrySetIsAView() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var entrySet = map.entrySet();
            set.add(42);
            assertEquals(Set.of(Map.entry(42, 42)), entrySet);
        }

        @Test
        public void newMapFromSetIsNonMutable() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            assertThrows(UnsupportedOperationException.class, () -> map.put(1, 1));
        }

        @Test
        public void newMapFromSetKeySetIsNonMutable() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var keySet = map.keySet();
            assertThrows(UnsupportedOperationException.class, () -> keySet.add(1));
        }

        @Test
        public void newMapFromSetValuesIsNonMutable() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var values = map.values();
            assertThrows(UnsupportedOperationException.class, () -> values.add(1));
        }

        @Test
        public void newMapFromSetEntrySetIsNonMutable() {
            var set = new HashSet<Integer>();
            var map = DedupVec.newMapFromSet(set);
            var entrySet = map.entrySet();
            assertThrows(UnsupportedOperationException.class, () -> entrySet.add(Map.entry(42, 42)));
        }

        @Test
        public void newMapFromSetOrder() {
            var set = Set.of("foo", "bar", "baz", "whizz", "fuzz");
            var map = DedupVec.newMapFromSet(set);
            assertEquals(new ArrayList<>(set), new ArrayList<>(map.keySet()));
        }

        @Test
        public void newMapFromSetValuesOrder() {
            var set = IntStream.range(0, 10).boxed().collect(toSet());
            var map = DedupVec.newMapFromSet(set);
            assertEquals(new ArrayList<>(set), new ArrayList<>(map.values()));
        }

        @Test
        public void newMapFromSetSizeIsFast() {
            var set = IntStream.range(0, 10_000).boxed().collect(toSet());
            var map = DedupVec.newMapFromSet(set);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var i = 0; i < 10_000; i++) {
                    assertEquals(10_000, map.size());
                }
            });
        }

        @Test
        public void newMapFromSetNullChecks() {
            var set = new HashSet<>(Arrays.asList("foo", null));
            var map = DedupVec.newMapFromSet(set);
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> map.get(null)),
                    () -> assertThrows(NullPointerException.class, () -> map.getOrDefault(null, "boom !"))
            );
        }

        @Test
        public void newMapFromSetIsFastEnough() {
            var set = IntStream.range(0, 1_000_000).boxed().collect(toSet());
            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (var i = 0; i < 1_000; i++) {
                    assertNotNull(DedupVec.newMapFromSet(set));
                }
            });
        }

        @Test
        public void newMapFromSetIsAHelperMethod() {
            assertThrows(NoSuchMethodException.class, () -> DedupVec.class.getMethod("newMapFromSet", Set.class));
        }
    }

    /*


    @Nested
    public class Q6 {
        @Test
        public void fromSetOfStrings() {
            var set = Set.of("foo");
            var dedupVec = DedupVec.fromSet(set);

            assertAll(
                    () -> assertEquals(1, dedupVec.size()),
                    () -> assertEquals("foo", dedupVec.get(0))
            );
        }

        @Test
        public void fromSetOfIntegers() {
            var list = List.of(1, 2, 4, 8, 16, -1);
            var set = new LinkedHashSet<>(list);
            var dedupVec = DedupVec.fromSet(set);

            assertAll(
                    () -> assertEquals(6, dedupVec.size()),
                    () -> assertEquals(1, dedupVec.get(0)),
                    () -> assertEquals(2, dedupVec.get(1)),
                    () -> assertEquals(4, dedupVec.get(2)),
                    () -> assertEquals(8, dedupVec.get(3)),
                    () -> assertEquals(16, dedupVec.get(4)),
                    () -> assertEquals(-1, dedupVec.get(5))
            );
        }

        @Test
        public void fromSetThenAdd() {
            var dedupVec = DedupVec.fromSet(Set.of(4242));
            dedupVec.add(4242);

            assertAll(
                    () -> assertEquals(2, dedupVec.size()),
                    () -> assertEquals(4242, dedupVec.get(0)),
                    () -> assertEquals(4242, dedupVec.get(1)),
                    () -> assertSame(dedupVec.get(0), dedupVec.get(1))
            );
        }

        @Test
        public void fromSetWithSetMutatedAfterCreation() {
            var set = new HashSet<Integer>();
            var dedupVec = DedupVec.fromSet(set);
            set.add(1000);

            assertEquals(0, dedupVec.size());
        }

        @Test
        public void fromSetWithSetMutatedAfterCreationNotUsedForDeduplication() {
            record Value(int value) {
            }
            var set = new HashSet<Value>();
            set.add(new Value(11));
            var dedupVec = DedupVec.fromSet(set);
            var anotherValue = new Value(17);
            set.add(anotherValue);
            dedupVec.add(new Value(17));
            assertNotSame(anotherValue, dedupVec.get(1));
        }

        @Test
        public void fromSetSignature() {
            Set<Integer> set = Set.of(1024);
            DedupVec<Object> dedupVec = DedupVec.fromSet(set);

            assertAll(
                    () -> assertEquals(1, dedupVec.size()),
                    () -> assertEquals(1024, dedupVec.get(0)),
                    () -> assertSame(set.iterator().next(), dedupVec.get(0))
            );
        }

        @Test
        public void fromSetPreconditions() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> DedupVec.fromSet(null)),
                    () -> assertThrows(NullPointerException.class, () -> DedupVec.fromSet(Stream.of(null).collect(toSet())))
            );
        }
    }


    @Nested
    public class Q8 {
        @Test
        public void dedupVecIsAList() {
            List<Integer> list = new DedupVec<>();
            list.add(2_000);
            list.add(3_000);
            list.add(2_000);
            assertSame(list.get(0), list.get(2));
        }

        @Test
        public void dedupVecGetAsList() {
            List<Integer> list = new DedupVec<>();
            assertTrue(list.add(2_000));
            assertTrue(list.add(3_000));
            assertTrue(list.add(2_000));

            assertAll(
                    () -> assertEquals(2_000, list.get(0)),
                    () -> assertEquals(3_000, list.get(1)),
                    () -> assertEquals(2_000, list.get(2))
            );
        }

        @Test
        public void dedupVecEquals() {
            var list = new DedupVec<>();
            assertTrue(list.add(1_024));
            assertTrue(list.add(2_048));
            assertTrue(list.add(2_048));

            assertEquals(List.of(1_024, 2_048, 2_048), list);
        }

        @Test
        public void dedupVecEqualsEmpty() {
            var list = new DedupVec<>();
            assertEquals(List.of(), list);
        }

        @Test
        public void dedupVecAddAll() {
            var list = new DedupVec<Integer>();
            assertTrue(list.addAll(List.of(2_000, 3_000, 2_000)));

            assertAll(
                    () -> assertEquals(2_000, list.get(0)),
                    () -> assertEquals(2_000, list.get(2)),
                    () -> assertSame(list.get(0), list.get(2))
            );
        }

        @Test
        public void dedupVecAddAllDedupVec() {
            var dedupVec1 = new DedupVec<Integer>();
            dedupVec1.add(444);
            dedupVec1.add(555);
            var dedupVec2 = new DedupVec<Integer>();
            dedupVec2.add(666);
            dedupVec2.add(777);
            assertTrue(dedupVec1.addAll(dedupVec2));

            assertAll(
                    () -> assertEquals(4, dedupVec1.size()),
                    () -> assertEquals(444, dedupVec1.get(0)),
                    () -> assertEquals(555, dedupVec1.get(1)),
                    () -> assertEquals(666, dedupVec1.get(2)),
                    () -> assertEquals(777, dedupVec1.get(3))
            );
        }

        @Test
        public void dedupVecAddAllDedupVec2() {
            var dedupVec1 = new DedupVec<Integer>();
            dedupVec1.add(444);
            dedupVec1.add(555);
            var dedupVec2 = new DedupVec<Integer>();
            dedupVec2.add(444);
            dedupVec2.add(777);
            assertTrue(dedupVec1.addAll(dedupVec2));

            assertAll(
                    () -> assertEquals(4, dedupVec1.size()),
                    () -> assertEquals(444, dedupVec1.get(0)),
                    () -> assertEquals(555, dedupVec1.get(1)),
                    () -> assertEquals(444, dedupVec1.get(2)),
                    () -> assertEquals(777, dedupVec1.get(3)),
                    () -> assertSame(dedupVec1.get(0), dedupVec1.get(2))
            );
        }

        @Test
        public void dedupVecAddAllReturnValue() {
            var list = new DedupVec<Integer>();
            list.add(1024);
            assertFalse(list.addAll(List.of()));
            assertEquals(1_024, list.get(0));
        }

        @Test
        public void dedupVecSubList() {
            var list = new DedupVec<Integer>();
            list.add(2_000);
            list.add(3_000);
            list.add(2_000);
            assertEquals(List.of(3_000, 2_000), list.subList(1, 3));
        }

        @Test
        public void dedupVecSubListAccessIsFast() {
            var dedupVec = new DedupVec<String>();
            dedupVec.add("foo");
            dedupVec.add("bar");
            var subList = dedupVec.subList(0, 1);
            assertTrue(subList instanceof RandomAccess);
        }
    }


    @Nested
    public class Q9 {
        @Test
        public void getFirst() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(12);
            dedupVec.add(45);

            assertEquals(12, dedupVec.getFirst());
        }

        @Test
        public void getLast() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(12);
            dedupVec.add(45);

            assertEquals(45, dedupVec.getLast());
        }

        @Test
        public void getFirstLastEmpty() {
            var dedupVec = new DedupVec<Integer>();

            assertAll(
                    () -> assertThrows(NoSuchElementException.class, dedupVec::getFirst),
                    () -> assertThrows(NoSuchElementException.class, dedupVec::getLast)
            );
        }

        @Test
        public void addFirst() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 1_000).forEach(dedupVec::addFirst);

            for (var i = 0; i < 1_000; i++) {
                assertEquals(999 - i, dedupVec.get(i));
            }
        }

        @Test
        public void addLast() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 1_000).forEach(dedupVec::addLast);

            for (var i = 0; i < 1_000; i++) {
                assertEquals(i, dedupVec.get(i));
            }
        }

        @Test
        public void addFirstSameValue() {
            record Empty() {
            }
            var dedupVec = new DedupVec<Empty>();
            for (var i = 0; i < 10; i++) {
                dedupVec.addFirst(new Empty());
            }

            var value = dedupVec.get(0);
            for (var i = 0; i < 10; i++) {
                assertSame(value, dedupVec.get(i));
            }
        }

        @Test
        public void addFirstDoNotStoreDuplicate() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.addFirst(person1);
            dedupVec.addFirst(person2);
            person2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void addAllSameValues2() {
            record Empty() {
            }
            var dedupVec = new DedupVec<Empty>();
            dedupVec.addAll(Collections.nCopies(10, new Empty()));

            var value = dedupVec.get(0);
            for (var i = 0; i < 10; i++) {
                assertSame(value, dedupVec.get(i));
            }
        }

        @Test
        public void addAllDoNotStoreDuplicate() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.add(person1);
            dedupVec.addAll(List.of(person2));
            person2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void addAllDoNotStoreDuplicate2() {
            record Person(String name) {
            }
            var person1 = new Person("Bob");
            var person2 = new Person("Bob");
            var ref = new WeakReference<>(person2);
            var dedupVec = new DedupVec<Person>();
            dedupVec.add(person1);
            dedupVec.addAll(List.of(person2, person2));
            person2 = null;
            System.gc();
            assertTrue(ref.refersTo(null));
            Reference.reachabilityFence(dedupVec);
        }

        @Test
        public void dedupVecIsReversible() {
            var list = new DedupVec<>();
            list.add("foo");
            list.add("bar");
            list.add("foo");
            assertEquals(list, list.reversed());
        }

        @Test
        public void reversedAddFirst() {
            var dedupVec = new DedupVec<Integer>();
            var reversed = dedupVec.reversed();
            range(0, 1_000).forEach(reversed::addFirst);

            for (var i = 0; i < 1_000; i++) {
                assertEquals(i, dedupVec.get(i));
            }
        }

        @Test
        public void reversedAddLast() {
            var dedupVec = new DedupVec<Integer>();
            var reversed = dedupVec.reversed();
            range(0, 1_000).forEach(reversed::addLast);

            for (var i = 0; i < 1_000; i++) {
                assertEquals(999 - i, dedupVec.get(i));
            }
        }

        @Test
        public void addInTheMiddle() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            dedupVec.add(5, 333_333);

            assertEquals(List.of(0, 1, 2, 3, 4, 333_333, 5, 6, 7, 8, 9), dedupVec);
        }

        @Test
        public void addInTheMiddleAtLastPosition() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            dedupVec.add(10, 777_777);

            assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 777_777), dedupVec);
        }

        @Test
        public void addWithAListIterator() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            var iterator = dedupVec.listIterator();
            iterator.add(222_222);

            assertEquals(List.of(222_222, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9), dedupVec);
        }

        @Test
        public void addWithAListIteratorAtLastPosition() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            var iterator = dedupVec.listIterator();
            while (iterator.hasNext()) {
                iterator.next();
            }
            iterator.add(999_999);

            assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 999_999), dedupVec);
        }

        @Test
        public void addWithAListIteratorInTheMiddle() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            var iterator = dedupVec.listIterator();
            for (var i = 0; i < 5; i++) {
                iterator.next();
            }
            iterator.add(555_555);

            assertEquals(List.of(0, 1, 2, 3, 4, 555_555, 5, 6, 7, 8, 9), dedupVec);
        }

        @Test
        public void preconditions() {
            var dedupVec = new DedupVec<>();
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.addFirst(null)),
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.addLast(null)),
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.reversed().addFirst(null)),
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.reversed().addLast(null)),
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.reversed().addLast(null)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> dedupVec.add(-1, "")),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> dedupVec.add(1, "")),
                    () -> assertThrows(NullPointerException.class, () -> dedupVec.listIterator().add(null))
            );
        }

        @Test
        public void setAtIndex() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(37);
            assertThrows(UnsupportedOperationException.class, () -> dedupVec.set(0, 101));
        }

        @Test
        public void iteratorSet() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(37);
            var listIterator = dedupVec.listIterator();
            listIterator.next();
            assertThrows(UnsupportedOperationException.class, () -> listIterator.set(101));
        }

        @Test
        public void setSubListAtIndex() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(37);
            var subList = dedupVec.subList(0, 1);
            assertThrows(UnsupportedOperationException.class, () -> subList.set(0, 101));
        }

        @Test
        public void iteratorSubListSet() {
            var dedupVec = new DedupVec<Integer>();
            dedupVec.add(37);
            var subList = dedupVec.subList(0, 1);
            var listIterator = subList.listIterator();
            listIterator.next();
            assertThrows(UnsupportedOperationException.class, () -> listIterator.set(101));
        }

        @Test
        public void removeFirst() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, dedupVec::removeFirst);
        }

        @Test
        public void removeLast() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, dedupVec::removeLast);
        }

        @Test
        public void remove() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, () -> dedupVec.remove(7));
        }

        @Test
        public void removeAll() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, () -> dedupVec.removeAll(List.of(3, 7)));
        }

        @Test
        public void removeIf() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, () -> dedupVec.removeIf(i -> i % 2 == 0));
        }

        @Test
        public void iteratorRemove() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            var iterator = dedupVec.iterator();
            for (var i = 0; i < 4; i++) {
                iterator.next();
            }
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        @Test
        public void subListRemove() {
            var dedupVec = new DedupVec<Integer>();
            range(0, 10).forEach(dedupVec::add);
            assertThrows(UnsupportedOperationException.class, () -> dedupVec.subList(1, 5).remove(1));
        }
    }

     */
}