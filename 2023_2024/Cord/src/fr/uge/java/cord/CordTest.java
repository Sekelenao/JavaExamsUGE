package fr.uge.java.cord;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CordTest {

    @Nested
    class Q1 {
        @Test
        public void cordOfIntegers() {
            Cord<Integer> cord = new Cord<Integer>();
            cord.add(3);
            cord.add(17);

            assertEquals(17, cord.get(0));
            assertEquals(3, cord.get(1));
        }

        @Test
        public void cordOfStrings() {
            Cord<String> cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            cord.add("baz");

            assertEquals("baz", cord.get(0));
            assertEquals("bar", cord.get(1));
            assertEquals("foo", cord.get(2));
        }

        @Test
        public void cordALot() {
            var cord = new Cord<Integer>();
            IntStream.range(0, 100_000).forEach(cord::add);

            for (int i = 0; i < 100_000; i++) {
                assertEquals(99_999 - i, cord.get(i));
            }
        }

        @Test
        public void onePublicConstructor() {
            assertEquals(1, Cord.class.getConstructors().length);
        }

        @Test
        public void qualityOfImplementation() {
            assertTrue(Cord.class.accessFlags().contains(AccessFlag.FINAL));
            assertTrue(Arrays.stream(Cord.class.getDeclaredFields())
                    .filter(f -> f.getType().getPackageName().equals("java.util"))
                    .allMatch(f -> f.accessFlags().contains(AccessFlag.FINAL)));
        }

        @Test
        public void api() {
            var methodNames = Arrays.stream(Cord.class.getDeclaredMethods())
                    .filter(m -> m.accessFlags().contains(AccessFlag.PUBLIC))
                    .map(Method::getName)
                    .toList();
            var allowedMethodNames =
                    Set.of("add", "get", "forEachIndexed", "createChild", "iterator", "attachParent", "indexedElements");
            assertTrue(allowedMethodNames.containsAll(methodNames));
        }

        @Test
        public void cordAddAndGetPreconditions() {
            var cord = new Cord<>();
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> cord.add(null)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> cord.get(0)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> cord.get(-1))
            );
        }
    }


    @Nested
    class Q2 {
        @Test
        public void forEachIndexedOfIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            cord.add(14);
            cord.add(116);

            var array = new int[3];
            cord.forEachIndexed((index, element) -> array[index] = element);
            assertArrayEquals(new int[]{116, 14, 3}, array);
        }

        @Test
        public void forEachIndexedOfStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");

            var array = new String[2];
            cord.forEachIndexed((index, element) -> array[index] = element);
            assertArrayEquals(new String[]{"bar", "foo"}, array);
        }

        @Test
        public void forEachIndexedALot() {
            var cord = new Cord<Integer>();
            IntStream.range(0, 100_000).forEach(i -> cord.add(99_999 - i));

            var indexList = new ArrayList<Integer>();
            var elementList = new ArrayList<Integer>();
            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                cord.forEachIndexed((index, element) -> {
                    indexList.add(index);
                    elementList.add(element);
                });
            });
            var expected = IntStream.range(0, 100_000).boxed().toList();
            assertEquals(expected, indexList);
            assertEquals(expected, elementList);
        }

        @Test
        public void forEachIndexedSignature() throws IllegalAccessException {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.forEachIndexed((int index, Object element) -> {
                assertNotNull(element);
            });
        }

        @Test
        public void forEachIndexedPrecondition() throws IllegalAccessException {
            var cord = new Cord<>();
            assertThrows(NullPointerException.class, () -> cord.forEachIndexed(null));
        }
    }

    @Nested
    class Q3 {
        @Test
        public void createChildOfStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            var child = cord.createChild();
            child.add("baz");

            assertEquals("baz", child.get(0));
            assertEquals("bar", child.get(1));
            assertEquals("foo", child.get(2));
        }

        @Test
        public void createChildOfSIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            var child = cord.createChild();
            child.add(14);
            child.add(116);

            assertEquals(116, child.get(0));
            assertEquals(14, child.get(1));
            assertEquals(3, child.get(2));
        }

        @Test
        public void createChildDelayed() {
            var cord = new Cord<Integer>();
            var child = cord.createChild();
            child.add(2);
            child.add(11);
            cord.add(3);

            assertEquals(11, child.get(0));
            assertEquals(2, child.get(1));
            assertEquals(3, child.get(2));
            assertEquals(3, cord.get(0));
        }

        @Test
        public void createChildSeveralForks() {
            var cord = new Cord<Integer>();
            cord.add(1);
            var child = cord.createChild();
            child.add(2);
            var grandchild = child.createChild();
            grandchild.add(3);

            assertEquals(3, grandchild.get(0));
            assertEquals(2, grandchild.get(1));
            assertEquals(1, grandchild.get(2));
        }

        @Test
        public void createChildSharing() {
            var cord = new Cord<Integer>();
            cord.add(3);
            var child1 = cord.createChild();
            child1.add(1);
            var child2 = cord.createChild();
            child2.add(2);

            assertAll(
                    () -> assertEquals(4, child1.get(0) + child1.get(1)),
                    () -> assertEquals(5, child2.get(0) + child2.get(1))
            );
        }

        @Test
        public void createChildALot() {
            var cord = new Cord<Integer>();
            for (var i = 0; i < 100_000; i++) {
                cord.add(i);
                cord = cord.createChild();
            }

            assertEquals(99_999, cord.get(0));
            assertEquals(0, cord.get(99_999));
        }

        @Test
        public void createChildGetOutOfBounds() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            var child = cord.createChild();
            child.add("baz");

            assertAll(
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> child.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> child.get(3)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> cord.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> cord.get(2))
            );
        }

        @Test
        public void qualityOfImplementation() {
            assertTrue(Cord.class.getDeclaredFields().length <= 2);
        }
    }

    @Nested
    class Q4 {
        @Test
        public void createChildForEachIndexedOfIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            cord.add(14);
            var child = cord.createChild();
            child.add(116);
            child.add(2274);

            var array = new int[4];
            child.forEachIndexed((index, element) -> array[index] = element);
            assertArrayEquals(new int[]{2274, 116, 14, 3}, array);
        }

        @Test
        public void createChildForEachIndexedOfStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            var child = cord.createChild();

            var array = new String[2];
            child.forEachIndexed((index, element) -> array[index] = element);
            assertArrayEquals(new String[]{"bar", "foo"}, array);
        }

        @Test
        public void createChildForEachIndexedSeveralChildren() {
            var cord = new Cord<Integer>();
            cord.add(10);
            var child = cord.createChild();
            child.add(20);
            var grantchild = child.createChild();
            grantchild.add(30);

            var array = new int[3];
            grantchild.forEachIndexed((index, element) -> array[index] = element);
            assertArrayEquals(new int[]{30, 20, 10}, array);
        }

        @Test
        public void createChildForEachIndexedALot() {
            var cord = new Cord<Integer>();
            for (var i = 0; i < 100_000; i++) {
                cord.add(i);
                cord = cord.createChild();
            }

            var box = new Object() {
                int index;
                int element;
            };
            cord.forEachIndexed((index, element) -> {
                assertEquals(box.index++, index);
                assertEquals(99_999 - box.element++, element);
            });
        }

        @Test
        public void createChildForEachIndexedALot2() {
            var cord = new Cord<Integer>();
            for (var i = 0; i < 1_000; i++) {
                cord = cord.createChild();
                for (var j = 0; j < 10_000; j++) {
                    cord.add(9_999 - j);
                }
            }
            var finalCord = cord;

            var indexList = new ArrayList<Integer>();
            var elementList = new ArrayList<Integer>();
            assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
                finalCord.forEachIndexed((index, element) -> {
                    indexList.add(index);
                    elementList.add(element);
                });
            });
            assertEquals(IntStream.range(0, 10_000_000).boxed().toList(), indexList);
            assertEquals(IntStream.range(0, 10_000_000).map(i -> i % 10_000).boxed().toList(), elementList);
        }

        @Test
        public void forEachIndexedPrecondition() throws IllegalAccessException {
            var cord = new Cord<>();
            assertThrows(NullPointerException.class, () -> cord.forEachIndexed(null));
        }
    }

    @Nested
    class Q5 {
        @Test
        public void loopOverIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            cord.add(14);

            var list = new ArrayList<Integer>();
            for (var element : cord) {
                list.add(element);
            }
            assertEquals(List.of(14, 3), list);
        }

        @Test
        public void loopOverStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            cord.add("baz");

            var list = new ArrayList<String>();
            for (var element : cord) {
                list.add(element);
            }
            assertEquals(List.of("baz", "bar", "foo"), list);
        }

        @Test
        public void createChildLoopOverIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            var child = cord.createChild();
            child.add(14);
            child.add(116);

            var list = new ArrayList<Integer>();
            for (var element : child) {
                list.add(element);
            }
            assertEquals(List.of(116, 14, 3), list);
        }

        @Test
        public void createChildLoopOverStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            var child = cord.createChild();
            child.add("baz");

            var list = new ArrayList<String>();
            for (var element : child) {
                list.add(element);
            }
            assertEquals(List.of("baz", "bar", "foo"), list);
        }

        @Test
        public void emptyCordLoopOverIntegers() {
            var cord = new Cord<Integer>();
            var child = cord.createChild();
            child.add(3);
            child.add(14);

            var list = new ArrayList<Integer>();
            for (var element : child) {
                list.add(element);
            }
            assertEquals(List.of(14, 3), list);
        }

        @Test
        public void emptyCordsAllAlongLoop() {
            var cord = new Cord<>();
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            for (var element : cord) {
                fail();
            }
        }

        @Test
        public void createChildCordWithOneElementPerCordLoop() {
            var cord = new Cord<Integer>();
            for (int i = 0; i < 100_000; i++) {
                cord.add(100_000 - i);
                cord = cord.createChild();
            }
            cord.add(0);

            var list = new ArrayList<Integer>();
            for (var element : cord) {
                list.add(element);
            }
            assertEquals(IntStream.rangeClosed(0, 100_000).boxed().toList(), list);
        }

        @Test
        public void createChildCordWithOneElementPerCordIteratorNext() {
            var cord = new Cord<Integer>();
            for (int i = 0; i < 100_000; i++) {
                cord.add(100_000 - i);
                cord = cord.createChild();
            }
            cord.add(0);

            var iterator = cord.iterator();
            for (int i = 0; i <= 100_000; i++) {
                assertEquals(i, iterator.next());
            }
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        public void createChildCordWithOneElementPerCordIteratorHasNext() {
            var cord = new Cord<Integer>();
            for (int i = 0; i < 100_000; i++) {
                cord.add(100_000 - i);
                cord = cord.createChild();
            }
            cord.add(0);

            var iterator = cord.iterator();
            for (int i = 0; i <= 100_000; i++) {
                assertTrue(iterator.hasNext());
                assertTrue(iterator.hasNext());
                assertEquals(i, iterator.next());
            }
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        public void cordIteratorRemove() {
            var cord = new Cord<Integer>();
            cord.add(134);

            var iterator = cord.iterator();
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }
    }


    @Nested
    class Q6 {
        @Test
        public void emptyCordsAllAlongIteratorNext() {
            var cord = new Cord<>();
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            assertThrows(NoSuchElementException.class, cord.iterator()::next);
        }

        @Test
        public void emptyCordsAllAlongIteratorHasNext() {
            var cord = new Cord<>();
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            var iterator = cord.iterator();
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
        }

        @Test
        public void emptyCordLoopOverStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            cord.add("bar");
            cord.add("baz");
            var child = cord.createChild();

            var list = new ArrayList<String>();
            for (var element : child) {
                list.add(element);
            }
            assertEquals(List.of("baz", "bar", "foo"), list);
        }

        @Test
        public void emptyCordsAlmostAllAlongIteratorNext() {
            var cord = new Cord<>();
            cord.add(42);
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            var iterator = cord.iterator();
            assertEquals(42, iterator.next());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        public void emptyCordsAlmostAllAlongIteratorHasNext() {
            var cord = new Cord<>();
            cord.add(42);
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            var iterator = cord.iterator();
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertEquals(42, iterator.next());
            assertFalse(iterator.hasNext());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        public void createChildCordWithOneElementPerCordButTheLastOneLoop() {
            var cord = new Cord<Integer>();
            for (int i = 0; i < 100_000; i++) {
                cord.add(99_999 - i);
                cord = cord.createChild();
            }

            var list = new ArrayList<Integer>();
            for (var element : cord) {
                list.add(element);
            }
            assertEquals(IntStream.range(0, 100_000).boxed().toList(), list);
        }
    }

    @Nested
    class Q7 {
        @Test
        public void attachParentIntegers() {
            var cord = new Cord<Integer>();
            cord.add(3);
            cord.add(14);
            var child = new Cord<Integer>();
            child.add(116);
            child.attachParent(cord);

            assertEquals(116, child.get(0));
            assertEquals(14, child.get(1));
            assertEquals(3, child.get(2));
            assertEquals(14, cord.get(0));
            assertEquals(3, cord.get(1));
        }

        @Test
        public void attachParentStrings() {
            var cord = new Cord<String>();
            cord.add("foo");
            var child = new Cord<String>();
            child.add("bar");
            child.add("baz");
            child.attachParent(cord);

            assertEquals("baz", child.get(0));
            assertEquals("bar", child.get(1));
            assertEquals("foo", child.get(2));
            assertEquals("foo", cord.get(0));
        }

        @Test
        public void attachParentToAnotherCordSignature() {
            var cord = new Cord<String>();
            cord.add("foo");
            var child = new Cord<>();
            child.attachParent(cord);

            assertEquals("foo", child.get(0));
        }

        @Test
        public void canNotAttachParentTwice() {
            var cord = new Cord<>();
            cord.attachParent(new Cord<>());

            assertThrows(IllegalStateException.class, () -> cord.attachParent(new Cord<>()));
        }

        @Test
        public void canNotAttachParentTwice2() {
            var cord = new Cord<>();
            var child = cord.createChild();
            assertThrows(IllegalStateException.class, () -> child.attachParent(new Cord<>()));
        }

        @Test
        public void attachParentPrecondition() {
            var cord = new Cord<>();
            assertThrows(NullPointerException.class, () -> cord.attachParent(null));
        }
    }


    @Nested
    class Q8 {
        @Test
        public void detectCycle() {
            var cord = new Cord<>();
            var child = cord.createChild();
            assertThrows(IllegalStateException.class, () -> cord.attachParent(child));
        }

        @Test
        public void detectCycle2() {
            var cord = new Cord<>();
            var child = cord.createChild();
            var grandchild = child.createChild();
            assertThrows(IllegalStateException.class, () -> cord.attachParent(grandchild));
        }

        @Test
        public void detectAutoCycle() {
            var cord = new Cord<>();
            assertThrows(IllegalStateException.class, () -> cord.attachParent(cord));
        }
    }

    @Nested
    class Q9 {
        @Test
        public void indexedElements() {
            var cord = new Cord<Integer>();
            cord.add(3);
            cord.add(14);

            var list = cord.indexedElements().map(e -> e.element()).toList();
            assertEquals(List.of(14, 3), list);
        }

        @Test
        public void indexedElementsIndex() {
            var cord = new Cord<String>();
            IntStream.range(0, 10).forEach(i -> cord.add("" + i));

            var list = cord.indexedElements().map(e -> e.index()).toList();
            assertEquals(IntStream.range(0, 10).boxed().toList(), list);
        }

        @Test
        public void createChildrenIndexedElementsMostlyEmpty() {
            var cord = new Cord<Integer>();
            cord.add(42);
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            assertEquals(42, cord.indexedElements().findFirst().orElseThrow().element());
        }

        @Test
        public void createChildrenIndexedElementsEmptyCordsAllAlong() {
            var cord = new Cord<Integer>();
            for (int i = 0; i < 100_000; i++) {
                cord = cord.createChild();
            }

            assertTrue(cord.indexedElements().findFirst().isEmpty());
            assertEquals(0, cord.indexedElements().count());
        }

        @Test
        public void indexedElementsLazyCreation() {
            var cord = new Cord<Integer>();
            IntStream.range(0, 100_000).forEach(cord::add);

            assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
                for (int i = 0; i < 100_000; i++) {
                    assertNotNull(cord.indexedElements());
                }
            });
        }
    }

}

