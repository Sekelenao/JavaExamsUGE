package fr.uge.policy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PolicyTest {

    @Test
    @Tag("Q1")
    public void allowExample() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("fob");
        policy.allow("bar");
        policy.allow("baz");
        policy.deny(s -> s.startsWith("f"));
        policy.deny(s -> s.endsWith("zz"));
        assertTrue(policy.allowed("bar"));
        assertFalse(policy.allowed("foo"));
        assertFalse(policy.allowed("white"));
    }

    @Test
    @Tag("Q1")
    public void allowSimpleString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        assertTrue(policy.allowed("foo"));
        assertFalse(policy.allowed("baz"));
    }

    @Test
    @Tag("Q1")
    public void allowSimpleInt() {
        var policy = new Policy<Integer>();
        policy.allow(42);
        policy.allow(10);
        assertTrue(policy.allowed(10));
        assertFalse(policy.allowed(101));
    }

    @Test
    @Tag("Q1")
    public void allowEmpty() {
        var policy = new Policy<String>();
        assertFalse(policy.allowed("foo"));
    }

    @Test
    @Tag("Q1")
    public void allowedSignature() {
        // si vous ne voyez pas pourquoi ce test ne compile pas, commentez le et passer à la suite
        var policy = new Policy<String>();
        var number = 3;
        assertFalse(policy.allowed(number));
    }

    @Test
    @Tag("Q1")
    public void allowedALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(policy::allow);
        });
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(i -> assertTrue(policy.allowed(i)));
        });
    }

    @Test
    @Tag("Q1")
    public void allowNPE() {
        var policy = new Policy<String>();
        assertThrows(NullPointerException.class, () -> policy.allow(null));
    }

    @Test
    @Tag("Q1")
    public void allowedNPE() {
        var policy = new Policy<String>();
        assertThrows(NullPointerException.class, () -> policy.allowed(null));
    }

    @Test
    @Tag("Q2")
    public void denySimpleString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.deny(s -> s.startsWith("f"));
        assertTrue(policy.allowed("bar"));
        assertFalse(policy.allowed("foo"));
    }

    @Test
    @Tag("Q2")
    public void denySimpleInt() {
        var policy = new Policy<Integer>();
        policy.allow(10);
        policy.allow(42);
        policy.deny(i -> i % 10 == 0);
        assertTrue(policy.allowed(42));
        assertFalse(policy.allowed(10));
    }

    @Test
    @Tag("Q2")
    public void denyAnyOrder() {
        var policy = new Policy<String>();
        policy.deny(s -> s.startsWith("f"));
        policy.allow("foo");
        policy.allow("bar");
        assertTrue(policy.allowed("bar"));
        assertFalse(policy.allowed("foo"));
    }

    @Test
    @Tag("Q2")
    public void denyAfterTheFact() {
        var policy = new Policy<Integer>();
        policy.allow(4);
        policy.allow(5);
        assertTrue(policy.allowed(4));
        assertTrue(policy.allowed(5));
        policy.deny(i -> i % 2 == 0);
        assertFalse(policy.allowed(4));
        assertTrue(policy.allowed(5));
    }

    @Test
    @Tag("Q2")
    public void denyALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(i -> {
                policy.deny(v -> v != i);
            });
        });
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(i -> assertFalse(policy.allowed(i)));
        });
    }

    @Test
    @Tag("Q2")
    public void allowAndDenyALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(i -> {
                policy.allow(i);
                policy.deny(v -> v == i);
            });
        });
    }

    @Test
    @Tag("Q2")
    public void denySignature() {
        var policy = new Policy<String>();
        policy.deny((Object o) -> o.toString().equals("fun"));
    }

    @Test
    @Tag("Q2")
    public void denyNPE() {
        var policy = new Policy<String>();
        assertThrows(NullPointerException.class, () -> policy.deny(null));
    }

    @Test
    @Tag("Q3")
    public void asAllDenyFilter() {
        var policy = new Policy<String>();
        policy.deny(s -> s.startsWith("a"));
        policy.deny(s -> s.startsWith("b"));
        var denyFilter = policy.asAllDenyFilter();
        assertTrue(denyFilter.test("abc"));
        assertTrue(denyFilter.test("bob"));
        assertFalse(denyFilter.test("cool"));
    }

    @Test
    @Tag("Q3")
    public void asAllDenyFilterEmpty() {
        var policy = new Policy<Integer>();
        var denyFilter = policy.asAllDenyFilter();
        assertFalse(denyFilter.test(1));
        assertFalse(denyFilter.test(11));
        assertFalse(denyFilter.test(101));
    }

    @Test
    @Tag("Q3")
    public void asAllDenyFilterNonMutable() {
        var policy = new Policy<String>();
        policy.deny(s -> s.startsWith("x"));
        var denyFilter = policy.asAllDenyFilter();
        policy.deny(s -> s.startsWith("y"));
        assertFalse(denyFilter.test("yolo"));
    }

    @Test
    @Tag("Q3")
    public void asAllDenyFilterALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(i -> {
                policy.allow(i);
                policy.deny(v -> v == i);
                policy.asAllDenyFilter();
            });
        });
    }

    @Test
    @Tag("Q4")
    public void forEachExample() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        policy.deny(s -> s.endsWith("r"));
        var list = new ArrayList<String>();
        policy.forEach(list::add);
        assertEquals(List.of("foo", "baz"), list);
    }

    @Test
    @Tag("Q4")
    public void forEachString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        var list = new ArrayList<String>();
        policy.forEach(list::add);
        assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Tag("Q4")
    public void forEachInteger() {
        var policy = new Policy<Integer>();
        policy.allow(747);
        policy.allow(77);
        policy.allow(47);
        policy.allow(7);
        var list = new ArrayList<Integer>();
        policy.forEach(list::add);
        assertEquals(List.of(747, 77, 47, 7), list);
    }

    @Test
    @Tag("Q4")
    public void forEachIntegerWithDeny() {
        var policy = new Policy<Integer>();
        policy.deny(i -> i % 3 == 0);
        range(0, 100).forEach(policy::allow);
        var list = new ArrayList<Integer>();
        policy.forEach(list::add);
        assertEquals(range(0, 100).filter(i -> i % 3 != 0).boxed().collect(toList()), list);
    }

    @Test
    @Tag("Q4")
    public void forEachALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(policy::allow);
        });
        var box = new Object() {
            int counter;
        };
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            policy.forEach(i -> assertEquals(box.counter++, i));
        });
    }

    @Test
    @Tag("Q4")
    public void forEachEmpty() {
        var policy = new Policy<String>();
        policy.forEach((Object o) -> fail());
    }

    @Test
    @Tag("Q4")
    public void forEachNPE() {
        var policy = new Policy<String>();
        assertThrows(NullPointerException.class, () -> policy.forEach(null));
    }

    @Test
    @Tag("Q5")
    public void addAllString() {
        var policy1 = new Policy<String>();
        policy1.allow("foo");
        var policy2 = new Policy<String>();
        policy2.allow("bar");
        policy1.addAll(policy2);
        assertTrue(policy1.allowed("foo"));
        assertTrue(policy1.allowed("bar"));
        assertFalse(policy1.allowed("baz"));
    }

    @Test
    @Tag("Q5")
    public void addAllInteger() {
        var policy1 = new Policy<Integer>();
        policy1.allow(47);
        var policy2 = new Policy<Integer>();
        policy2.allow(747);
        policy1.addAll(policy2);
        assertTrue(policy1.allowed(47));
        assertTrue(policy1.allowed(747));
        assertFalse(policy1.allowed(7));
    }

    @Test
    @Tag("Q5")
    public void addAllStringAndDeny() {
        var policy1 = new Policy<String>();
        policy1.allow("foo");
        policy1.allow("fob");
        policy1.deny("bar"::equals);
        var policy2 = new Policy<String>();
        policy2.allow("bar");
        policy2.allow("baz");
        policy2.deny("foo"::equals);
        policy1.addAll(policy2);
        assertFalse(policy1.allowed("foo"));
        assertFalse(policy1.allowed("bar"));
        assertTrue(policy1.allowed("fob"));
        assertTrue(policy1.allowed("baz"));
        assertFalse(policy1.allowed("zzz"));
    }

    @Test
    @Tag("Q5")
    public void addAllDeny() {
        var policy1 = new Policy<String>();
        policy1.deny(s -> s.startsWith("a"));
        var policy2 = new Policy<String>();
        policy2.deny(s -> s.startsWith("b"));
        policy1.addAll(policy2);
        var denyFilter = policy1.asAllDenyFilter();
        assertTrue(denyFilter.test("abc"));
        assertTrue(denyFilter.test("bob"));
        assertFalse(denyFilter.test("cool"));
    }

    @Test
    @Tag("Q5")
    public void addAllNPE() {
        var policy = new Policy<String>();
        assertThrows(NullPointerException.class, () -> policy.addAll(null));
    }

    @Test
    @Tag("Q6")
    public void forLoopInteger() {
        var policy = new Policy<Integer>();
        range(0, 100).forEach(policy::allow);
        var i = 0;
        for (var value : policy) {
            assertEquals(i++, value);
        }
    }

    @Test
    @Tag("Q6")
    public void forLoopIntegerWithDeny() {
        var policy = new Policy<Integer>();
        policy.deny(i -> i % 3 == 0);
        range(0, 100).forEach(policy::allow);
        var i = 1;
        for (var value : policy) {
            assertEquals(i++, value);
            if (i % 3 == 0) {
                i++;
            }
        }
    }

    @Test
    @Tag("Q6")
    public void forLoopString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        var list = new ArrayList<String>();
        for (var name : policy) {
            list.add(name);
        }
        assertEquals(List.of("foo", "bar", "baz"), list);
    }

    @Test
    @Tag("Q6")
    public void forLoopModificationWhileLoopingAllow() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        assertThrows(ConcurrentModificationException.class, () -> {
            for (var name : policy) {
                policy.allow(name + "2");
            }
        });
    }

    @Test
    @Tag("Q6")
    public void forLoopModificationWhileLoopingDeny() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        assertThrows(ConcurrentModificationException.class, () -> {
            for (var name : policy) {
                policy.deny(__ -> false);
            }
        });
    }

    @Test
    @Tag("Q6")
    public void iteratorOk() {
        var policy = new Policy<String>();
        policy.allow("fuzz");
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("whizz");
        policy.allow("baz");
        policy.allow("jazz");
        policy.deny(s -> s.endsWith("zz"));
        var it = policy.iterator();
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(it.next(), "foo");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(it.next(), "bar");
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(it.next(), "baz");
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test
    @Tag("Q6")
    public void iteratorRemove() {
        var policy = new Policy<String>();
        policy.allow("fuzz");
        policy.allow("foo");
        policy.allow("whizz");
        policy.deny(s -> s.endsWith("zz"));
        var it = policy.iterator();
        assertTrue(it.hasNext());
        assertEquals(it.next(), "foo");
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    @Tag("Q7")
    public void asSetString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        assertEquals(Set.of("foo", "bar", "baz"), policy.asSet());
    }

    @Test
    @Tag("Q7")
    public void asSetIntegerOrder() {
        var policy = new Policy<Integer>();
        range(0, 100).forEach(policy::allow);
        assertEquals(range(0, 100).boxed().collect(toList()), new ArrayList<>(policy.asSet()));
    }

    @Test
    @Tag("Q7")
    public void asSetAsAView() {
        var policy = new Policy<String>();
        var set = policy.asSet();
        policy.allow("foo");
        assertTrue(set.contains("foo"));
        policy.allow("bar");
        assertTrue(set.contains("bar"));
        policy.deny("foo"::equals);
        assertFalse(set.contains("foo"));
        assertTrue(set.contains("bar"));
    }

    @Test
    @Tag("Q7")
    public void asSetSize() {
        var policy = new Policy<String>();
        var set = policy.asSet();
        policy.allow("foo");
        assertEquals(1, set.size());
        policy.allow("bar");
        assertEquals(2, set.size());
        policy.allow("baz");
        assertEquals(3, set.size());
        policy.deny(s -> s.startsWith("b"));
        assertEquals(1, set.size());
        policy.allow("fob");
        assertEquals(2, set.size());
        policy.allow("bob");
        assertEquals(2, set.size());
    }

    @Test
    @Tag("Q7")
    public void asSetModificationWhileLooping() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        var set = policy.asSet();
        assertThrows(ConcurrentModificationException.class, () -> {
            for (var name : set) {
                policy.allow(name + "2");
            }
        });
    }

    @Test
    @Tag("Q7")
    public void asSetContains() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        policy.deny("foo"::equals);
        var set = policy.asSet();
        assertFalse(set.contains("foo"));
        assertFalse(set.contains("fob"));
        assertTrue(set.contains("bar"));
        assertTrue(set.contains("baz"));
    }

    @Test
    @Tag("Q7")
    public void asSetSizeALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000_000).forEach(policy::allow);
        });
        var set = policy.asSet();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 10_000).forEach(__ -> {
                assertEquals(1_000_000, set.size());
            });
        });
    }

    @Test
    @Tag("Q7")
    public void asSetContainsALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000).forEach(policy::allow);
        });
        var set = policy.asSet();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(1_000, 1_000_000).forEach(i -> assertFalse(set.contains(i)));
        });
    }

    @Test
    @Tag("Q7")
    public void asSetContains2ALot() {
        var policy = new Policy<Integer>();
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(0, 1_000).forEach(policy::allow);
        });
        assertTimeoutPreemptively(Duration.ofMillis(2_000), () -> {
            range(1_000, 1_000_000).forEach(i -> {
                var set = policy.asSet();
                assertFalse(set.contains(i));
            });
        });
    }

    @Test
    @Tag("Q7")
    public void asSetSignature() {
        var policy = new Policy<String>();
        Set<String> set = policy.asSet();
    }

    @Test
    @Tag("Q7")
    public void asSetNonMutable() {
        var policy = new Policy<String>();
        var set = policy.asSet();
        assertThrows(UnsupportedOperationException.class, () -> set.add("foo"));
    }

    @Test
    @Tag("Q8")
    public void streamInteger() {
        var policy = new Policy<Integer>();
        policy.deny(i -> i % 2 == 1);
        range(0, 1_000).forEach(policy::allow);
        assertEquals(range(0, 500).boxed().collect(toList()), policy.stream().map(v -> v / 2).collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void streamString() {
        var policy = new Policy<String>();
        policy.allow("foo");
        policy.allow("bar");
        policy.allow("baz");
        policy.deny("bar"::equals);
        assertEquals(List.of("foo", "baz"), policy.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void streamCount() {
        var policy = new Policy<Integer>();
        policy.deny(i -> i % 2 == 1);
        range(0, 1_000).forEach(policy::allow);
        assertEquals(500, policy.stream().count());
    }

    @Test
    @Tag("Q8")
    public void streamForEach() {
        var policy = new Policy<Integer>();
        policy.deny(i -> i % 2 == 1);
        range(0, 1_000).forEach(policy::allow);
        var list = new ArrayList<Integer>();
        policy.stream().forEach(list::add);
        assertEquals(range(0, 1_000).filter(i -> i % 2 == 0).boxed().collect(toList()), list);
    }

    @Test
    @Tag("Q8")
    public void streamSize() {
        var policy = new Policy<Integer>();
        range(0, 1_000).forEach(policy::allow);
        var set = policy.asSet();
        assertEquals(1_000, set.size());
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertEquals(1_000, spliterator.estimateSize());
    }

    @Test
    @Tag("Q8")
    public void streamSizeWithDeny() {
        var policy = new Policy<Integer>();
        range(0, 1_000).forEach(policy::allow);
        policy.deny(i -> i % 2 == 0);
        var set = policy.asSet();
        assertEquals(500, set.size());
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertEquals(500, spliterator.estimateSize());
    }

    @Test
    @Tag("Q8")
    public void streamNoSize() {
        var policy = new Policy<Integer>();
        range(0, 1_000).forEach(policy::allow);
        policy.deny(__ -> false);  // none
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        assertFalse(spliterator.hasCharacteristics(Spliterator.SIZED));
    }

    @Test
    @Tag("Q8")
    public void streamNoSplit() {
        var policy = new Policy<Integer>();
        var stream = policy.stream();
        assertNull(stream.spliterator().trySplit());
    }

    @Test
    @Tag("Q8")
    public void streamKnownSize() {
        var policy = new Policy<Integer>();
        range(0, 10).forEach(policy::allow);
        var set = policy.asSet();
        assertEquals(10, set.size());
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        spliterator.tryAdvance(i -> assertEquals(0, i));
        spliterator.tryAdvance(i -> assertEquals(1, i));
        spliterator.tryAdvance(i -> assertEquals(2, i));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertEquals(7, spliterator.estimateSize());
    }

    @Test
    @Tag("Q8")
    public void streamKnownSize2() {
        var policy = new Policy<Integer>();
        range(0, 10).forEach(policy::allow);
        policy.deny(v -> v % 2 == 0);
        var set = policy.asSet();
        assertEquals(5, set.size());
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertEquals(5, spliterator.estimateSize());
    }

    @Test
    @Tag("Q8")
    public void streamSizeNotKnownAnymore() {
        var policy = new Policy<Integer>();
        range(0, 10).forEach(policy::allow);
        var set = policy.asSet();
        assertEquals(10, set.size());
        var spliterator = policy.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        policy.allow(10);
        var spliterator2 = policy.stream().spliterator();
        assertFalse(spliterator2.hasCharacteristics(Spliterator.SIZED));
    }

    @Test
    @Tag("Q8")
    public void streamSizeNotKnownAnymore2() {
        var policy = new Policy<Integer>();
        range(0, 10).forEach(policy::allow);
        var set = policy.asSet();
        assertEquals(10, set.size());
        var spliterator = policy.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        policy.deny(i -> i != 7);
        var spliterator2 = policy.stream().spliterator();
        assertFalse(spliterator2.hasCharacteristics(Spliterator.SIZED));
    }

    @Test
    @Tag("Q8")
    public void streamCharacteristics() {
        var policy = new Policy<Integer>();
        var stream = policy.stream();
        var spliterator = stream.spliterator();
        assertAll(
                () -> assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED)),
                () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL)),
                () -> assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT))
        );
    }

}