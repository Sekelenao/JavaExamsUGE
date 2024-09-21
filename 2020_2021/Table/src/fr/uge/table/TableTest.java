package fr.uge.table;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TableTest {
    // Q1

    @Test
    @Tag("Q1")
    public void ofIntegers() {
        Table<Integer> table = Table.of(2, 3, 4, 5, 7);
        assertEquals(5, table.size());
    }

    @Test
    @Tag("Q1")
    public void ofUser() {
        record User(String name) {
        }
        Table<User> table = Table.of(new User("Ana"), new User("Bob"));
        assertEquals(2, table.size());
    }

    @Test
    @Tag("Q1")
    public void ofEmpty() {
        var table = Table.of();
        assertEquals(0, table.size());
    }

    @Test
    @Tag("Q1")
    public void ofSameValue() {
        Table<Integer> table = Table.of(2, 3, 2, 3);
        assertEquals(4, table.size());
    }

    @Test
    @Tag("Q1")
    public void ofNPE() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> Table.of("foo", null)),
                () -> assertThrows(NullPointerException.class, () -> Table.of((Object[]) null))
        );
    }

    @Test
    @Tag("Q1")
    public void canNotCreateATableWithoutUsingOf() {
        assertEquals(0, Table.class.getConstructors().length);
    }

    // Q2

    @Test
    @Tag("Q2")
    public void newGroupDifferentName() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Ana", 34), new Person("Bob", 54));
        var group = table.groupBy(Person::name, String::compareTo);
        assertEquals(2, group.keySize());
    }

    @Test
    @Tag("Q2")
    public void newGroupSameAge() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Phil", 23), new Person("Elo", 23));
        var group = table.groupBy(Person::age, Integer::compareTo);
        assertEquals(1, group.keySize());
    }

    @Test
    @Tag("Q2")
    public void newGroupOfPets() {
        enum Pet {cat, lion, DOG}
        var table = Table.of(Pet.values());
        var group = table.groupBy(Pet::name, String::compareToIgnoreCase);
        assertEquals(3, group.keySize());
    }

    @Test
    @Tag("Q2")
    public void newGroupNPE() {
        var table = Table.of(1, 2, 3);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> table.groupBy(null, Integer::compareTo)),
                () -> assertThrows(NullPointerException.class, () -> table.groupBy(v -> v, null))
        );
    }

    // Q3

    @Test
    @Tag("Q3")
    public void testToStringPersonByName() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Gogu", 50), new Person("Sia", 50));
        var group = table.groupBy(Person::name, String::compareTo);
        assertEquals("Gogu: [0]\nSia: [1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringPersonByAge() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Gogu", 50), new Person("Sia", 50));
        var group = table.groupBy(Person::age, Integer::compareTo);
        assertEquals("50: [0, 1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringInPersonNameInReverseOrder() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Phil", 23), new Person("Elo", 23));
        var group = table.groupBy(Person::name, reverseOrder(String::compareTo));
        assertEquals("Phil: [0]\nElo: [1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringPoint() {
        record Point(int x, int y) {
        }
        var table = Table.of(new Point(1, 2), new Point(99, 3), new Point(1, 5));
        var group = table.groupBy(Point::x, Integer::compareTo);
        assertEquals("1: [0, 2]\n99: [1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringPets() {
        enum Pet {cat, lion, DOG}
        var table = Table.of(Pet.values());
        var group = table.groupBy(Pet::name, String::compareToIgnoreCase);
        assertEquals("cat: [0]\nDOG: [2]\nlion: [1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringPetByOwner() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana"));
        var group = table.groupBy(Pet::owner, String::compareTo);
        assertEquals("Ana: [1, 2]\nJim: [0]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringPetByName() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana"));
        var group = table.groupBy(Pet::name, String::compareTo);
        assertEquals("brutus: [2]\njulius: [0]\nnero: [1]", group.toString());
    }

    @Test
    @Tag("Q3")
    public void testToStringEmpty() {
        var table = Table.of();
        var group = table.groupBy(__ -> fail(), (_1, _2) -> fail());
        assertEquals("", group.toString());
    }

    // Q4

    @Test
    @Tag("Q4")
    public void dynamic() {
        var table = Table.<String>dynamic();
        table.add("foo");
        table.add("bar");
        table.add("baz");
        var group = table.groupBy(String::length, Integer::compareTo);
        assertAll(
                () -> assertEquals(3, table.size()),
                () -> assertEquals(1, group.keySize())
        );
    }

    @Test
    @Tag("Q4")
    public void dynamicUseAfterCreation() {
        var table = Table.<String>dynamic();
        var group = table.groupBy(String::length, Integer::compareTo);
        table.add("foo");
        table.add("bar");
        table.add("baz");
        assertAll(
                () -> assertEquals(3, table.size()),
                () -> assertEquals(1, group.keySize())
        );
    }

    @Test
    @Tag("Q4")
    public void dynamicPersons() {
        record Person(String name, int age) {
        }
        var table = Table.<Person>dynamic();
        List.of(new Person("Ana", 34), new Person("Ana", 32)).forEach(table::add);
        var group = table.groupBy(Person::name, String::compareTo);
        assertAll(
                () -> assertEquals(2, table.size()),
                () -> assertEquals(1, group.keySize())
        );
    }

    @Test
    @Tag("Q4")
    public void addImmutable() {
        var table = Table.of(1, 2, 6);
        assertThrows(UnsupportedOperationException.class, () -> table.add(4));
    }

    @Test
    @Tag("Q4")
    public void dynamicPersonsSeveralGroups() {
        record Person(String firstName, String lastName, int age) {
        }
        var table = Table.<Person>dynamic();
        List.of(new Person("Gogu", "Yoda", 32), new Person("John", "Rocks", 32)).forEach(table::add);
        var groupFirstName = table.groupBy(Person::firstName, String::compareTo);
        var groupLastName = table.groupBy(Person::firstName, String::compareTo);
        var groupAge = table.groupBy(Person::age, Integer::compareTo);
        assertAll(
                () -> assertEquals(2, table.size()),
                () -> assertEquals(2, groupFirstName.keySize()),
                () -> assertEquals(2, groupLastName.keySize()),
                () -> assertEquals(1, groupAge.keySize())
        );
    }

    @Test
    @Tag("Q4")
    public void dynamicIntegersSeveralGroups() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000).forEach(table::add);
        var groupBy7 = table.groupBy(x -> x % 7, Integer::compareTo);
        var groupBy11 = table.groupBy(x -> x % 11, Integer::compareTo);
        assertAll(
                () -> assertEquals(1_000, table.size()),
                () -> assertEquals(7, groupBy7.keySize()),
                () -> assertEquals(11, groupBy11.keySize())
        );
    }

    @Test
    @Tag("Q4")
    public void groupsSignature() {
        var table = Table.<String>of();
        table.<String>groupBy((Object o) -> fail(), (Object o1, Object o2) -> fail());
        table.<CharSequence>groupBy((Object o) -> o.toString(), (Object o1, Object o2) -> fail());
    }

    @Test
    @Tag("Q4")
    public void addTooSlow() {
        var table = Table.<Integer>dynamic();
        assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> range(0, 1_000_000).forEach(table::add));
    }

    @Test
    @Tag("Q4")
    public void addGroupTooSlow() {
        var table = Table.<Integer>dynamic();
        table.groupBy(v -> v, Integer::compareTo);
        assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> range(0, 1_000_000).forEach(table::add));
    }

    @Test
    @Tag("Q4")
    public void addLotOfGroupsTooSlow() {
        var table = Table.<Integer>dynamic();
        for (var i = 0; i < 1_000; i++) {
            table.groupBy(v -> v, Integer::compareTo);
        }
        assertTimeoutPreemptively(Duration.ofMillis(4_000), () -> range(0, 1_000).forEach(table::add));
    }

    @Test
    @Tag("Q4")
    public void addNPE() {
        var table = Table.<Integer>dynamic();
        assertThrows(NullPointerException.class, () -> table.add(null));
    }

    // Q5

    @Test
    @Tag("Q5")
    public void forEachPetByName() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana"));
        var group = table.groupBy(Pet::name, String::compareTo);
        var pets = new ArrayList<Pet>();
        group.forEach(pets::add);
        assertEquals(List.of(new Pet("brutus", "Ana"), new Pet("julius", "Jim"), new Pet("nero", "Ana")), pets);
    }

    @Test
    @Tag("Q5")
    public void forEachPetByOwner() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana"));
        var group = table.groupBy(Pet::owner, String::compareTo);
        var pets = new ArrayList<Pet>();
        group.forEach(pets::add);
        assertEquals(List.of(new Pet("nero", "Ana"), new Pet("brutus", "Ana"), new Pet("julius", "Jim")), pets);
    }

    @Test
    @Tag("Q5")
    public void forEachPetByOwnerInReverseOrder() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana"));
        var group = table.groupBy(Pet::owner, reverseOrder(String::compareTo));
        var pets = new ArrayList<Pet>();
        group.forEach(pets::add);
        assertEquals(List.of(new Pet("julius", "Jim"), new Pet("nero", "Ana"), new Pet("brutus", "Ana")), pets);
    }

    @Test
    @Tag("Q5")
    public void forEachInts() {
        var table = Table.<Integer>dynamic();
        var group = table.groupBy(x -> x % 7, Integer::compareTo);
        range(0, 20).forEach(table::add);
        var results = new ArrayList<Integer>();
        group.forEach(results::add);
        assertEquals(List.of(0, 7, 14, 1, 8, 15, 2, 9, 16, 3, 10, 17, 4, 11, 18, 5, 12, 19, 6, 13), results);
    }

    @Test
    @Tag("Q5")
    public void forEachRange() {
        var table = Table.of(1, 6, 5, 8, 5);
        var group = table.groupBy(v -> v, Integer::compareTo);
        var results = new ArrayList<Integer>();
        group.forEach(results::add);
        assertEquals(List.of(1, 5, 5, 6, 8), results);
    }

    @Test
    @Tag("Q5")
    public void forEachSignature() {
        var table = Table.<Integer>dynamic();
        var group = table.groupBy(x -> x / 3, Integer::compareTo);
        range(0, 10).forEach(table::add);
        var results = new ArrayList<>();
        group.forEach(results::add);
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), results);
    }

    @Test
    @Tag("Q5")
    public void forEachNPE() {
        var table = Table.of(1, 12, 3);
        var group = table.groupBy(v -> v, Integer::compareTo);
        assertThrows(NullPointerException.class, () -> group.forEach(null));
    }

    // Q6

    @Test
    @Tag("Q6")
    public void groupLookup() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Ana", 34), new Person("Bob", 54), new Person("Gil", 34));
        var group = table.groupBy(Person::age, Integer::compareTo);
        var list = group.lookup(34);
        assertEquals(List.of(new Person("Ana", 34), new Person("Gil", 34)), list);
    }

    @Test
    @Tag("Q6")
    public void groupLookupString() {
        var table = Table.of("foo", "bar", "whizz");
        var group = table.groupBy(String::length, Integer::compareTo);
        List<String> list = group.lookup(3);
        assertEquals(List.of("foo", "bar"), list);
    }

    @Test
    @Tag("Q6")
    public void groupLookupIntegers() {
        var table = Table.of(1, 3, 6, 7, 2);
        var group = table.groupBy(x -> x / 2, Integer::compareTo);
        List<Integer> list = group.lookup(1);
        assertEquals(List.of(3, 2), list);
    }

    @Test
    @Tag("Q6")
    public void groupLookupEmpty() {
        var table = Table.of(1, 3, 6, 7, 3);
        var group = table.groupBy(v -> v, Integer::compareTo);
        assertTrue(group.lookup(-17).isEmpty());
    }

    @Test
    @Tag("Q6")
    public void groupLookupImmutable() {
        record Person(String name, int age) {
        }
        var table = Table.<Person>dynamic();
        var group = table.groupBy(Person::age, Integer::compareTo);
        table.add(new Person("Ana", 34));
        table.add(new Person("Bob", 54));
        assertAll(
                () -> assertThrows(UnsupportedOperationException.class, () -> group.lookup(17).add(new Person("Jane", 1337))),
                () -> assertThrows(UnsupportedOperationException.class, () -> group.lookup(34).add(new Person("Jane", 1337))),
                () -> assertThrows(UnsupportedOperationException.class, () -> group.lookup(54).add(new Person("Jane", 1337)))
        );
    }

    @Test
    @Tag("Q6")
    public void groupLookupIndex() {
        var table = Table.of("foo", "", "baz");
        var group = table.groupBy(String::isEmpty, Boolean::compareTo);
        var list = group.lookup(false);
        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1)),
                () -> assertEquals("foo", list.get(0)),
                () -> assertEquals("baz", list.get(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(2))
        );
    }

    @Test
    @Tag("Q6")
    public void groupLookupIndexWithMutation() {
        var table = Table.<String>dynamic();
        table.add("");
        table.add("foo");
        table.add("bar");
        var group = table.groupBy(String::isEmpty, Boolean::compareTo);
        var list = group.lookup(false);
        table.add("baz");
        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1)),
                () -> assertEquals("foo", list.get(0)),
                () -> assertEquals("bar", list.get(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(2))
        );
    }

    @Test
    @Tag("Q6")
    public void groupLookupSATB() {
        record Person(String name, int age) {
        }
        var table = Table.<Person>dynamic();
        table.add(new Person("Ana", 34));
        table.add(new Person("Bob", 54));
        table.add(new Person("Gil", 34));
        var group = table.groupBy(Person::age, Integer::compareTo);
        var list = group.lookup(34);
        table.add(new Person("Rob", 34));
        assertEquals(List.of(new Person("Ana", 34), new Person("Gil", 34)), list);
    }

    @Test
    @Tag("Q6")
    public void groupLookupImpl() {
        var table = Table.of("foo", "bar");
        var group = table.groupBy(String::length, Integer::compareTo);
        var list = group.lookup(3);
        assertNotEquals(list.getClass().getPackageName(), "java.util");
    }

    @Test
    @Tag("Q6")
    public void groupLookupNPE() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("thunder", "Ana"), new Pet("rex", "Bob"));
        var group = table.groupBy(Pet::name, String::compareTo);
        assertThrows(NullPointerException.class, () -> group.lookup(null));
    }

    // Q7

    @Test
    @Tag("Q7")
    public void newGroup2PersonByName() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Ana", 34), new Person("Bob", 54));
        var group = table.groupBy(Person::name);
        assertEquals(2, group.keySize());
    }

    @Test
    @Tag("Q7")
    public void newGroup2PersonByAge() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Phil", 23), new Person("Elo", 23));
        var group = table.groupBy(Person::age);
        assertEquals(1, group.keySize());
    }

    @Test
    @Tag("Q7")
    public void newGroup2TimeStamp() {
        var table = Table.of(new Timestamp(12));
        var group = table.<Timestamp>groupBy((Timestamp timestamp) -> timestamp);
        assertEquals(1, group.keySize());
    }

    @Test
    @Tag("Q7")
    public void newGroups2Signature() {
        var table = Table.<Integer>of();
        table.<Integer>groupBy((Object o) -> fail());
    }

    @Test
    @Tag("Q7")
    public void newGroup2NPE() {
        var table = Table.of(1, 2, 3);
        assertThrows(NullPointerException.class, () -> table.groupBy(null));
    }

    // Q8

    @Test
    @Tag("Q8")
    public void fullExample() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Gil", 34), new Person("Bob", 54), new Person("Ana", 34));
        var byName = table.groupBy(Person::name, String::compareTo);
        var byAge = table.groupBy(Person::age, Integer::compareTo);
        assertEquals("Ana: [2]\nBob: [1]\nGil: [0]", byName.toString());
        assertEquals("34: [0, 2]\n54: [1]", byAge.toString());
        assertEquals(List.of(new Person("Gil", 34), new Person("Ana", 34)), byAge.lookup(34));
        assertEquals(List.of(new Person("Gil", 34), new Person("Ana", 34), new Person("Bob", 54)), byAge.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void fullDynamicExample() {
        record Person(String name, int age) {
        }
        var table = Table.<Person>dynamic();
        var byName = table.groupBy(Person::name, String::compareTo);
        table.add(new Person("Gil", 34));
        table.add(new Person("Bob", 54));
        table.add(new Person("Ana", 34));
        var byAge = table.groupBy(Person::age, Integer::compareTo);
        assertEquals("Ana: [2]\nBob: [1]\nGil: [0]", byName.toString());
        assertEquals("34: [0, 2]\n54: [1]", byAge.toString());
        assertEquals(List.of(new Person("Gil", 34), new Person("Ana", 34)), byAge.lookup(34));
        assertEquals(List.of(new Person("Gil", 34), new Person("Ana", 34), new Person("Bob", 54)), byAge.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void newGroupStream() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Bob", 54), new Person("Ana", 34));
        var group = table.groupBy(Person::name);
        assertEquals(List.of(new Person("Ana", 34), new Person("Bob", 54)), group.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void newGroupPet() {
        record Pet(String name, String owner) {
        }
        var table = Table.of(new Pet("snoopy", "Joe"), new Pet("rex", "Bo"));
        var group = table.groupBy(Pet::name);
        List<Pet> pets = group.stream().collect(toList());
        assertEquals(List.of(new Pet("rex", "Bo"), new Pet("snoopy", "Joe")), pets);
    }

    @Test
    @Tag("Q8")
    public void newGroupIntegers() {
        var table = Table.of(11, 7, 707);
        var group = table.groupBy(v -> v, Integer::compareTo);
        List<Integer> ints = group.stream().collect(toList());
        assertEquals(List.of(7, 11, 707), ints);
    }

    @Test
    @Tag("Q8")
    public void newGroupStreamOrder() {
        record Person(String name, int age) {
        }
        var table = Table.of(new Person("Gil", 34), new Person("Bob", 54), new Person("Ana", 34));
        var group = table.groupBy(Person::age);
        assertEquals(List.of(new Person("Gil", 34), new Person("Ana", 34), new Person("Bob", 54)), group.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void newGroupParallel() {
        var table = Table.of("foo", "bar");
        var group = table.groupBy(String::length, Integer::compareTo);
        assertFalse(group.stream().isParallel());
    }

    @Test
    @Tag("Q8")
    public void newGroupStreamALot() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000_000).forEach(table::add);
        var group = table.groupBy(x -> x / 10);
        assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> assertEquals(1_000_000, group.stream().count()));
    }

    @Test
    @Tag("Q8")
    public void newGroupOfSpliterator() {
        var table = Table.of(11, 7, 707);
        var group = table.groupBy(v -> v, Integer::compareTo);
        var spliterator = group.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    @Tag("Q8")
    public void newGroupDynamicSpliterator() {
        var table = Table.<Integer>dynamic();
        table.add(747);
        var group = table.groupBy(v -> v, Integer::compareTo);
        var spliterator = group.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    @Tag("Q8")
    public void newGroupParallelStream() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000_000).forEach(table::add);
        var group = table.groupBy(v -> v);
        assertEquals(1_000_000, group.stream().parallel().mapToInt(__ -> 1).sum());
    }

    @Test
    @Tag("Q8")
    public void newGroupParallelStreamALot() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000_000).forEach(table::add);
        var group = table.groupBy(x -> x / 10);
        assertTimeoutPreemptively(Duration.ofMillis(1_000),
                () -> assertEquals(1_000_000, group.stream().parallel().count()));
    }

    @Test
    @Tag("Q8")
    public void newGroupParallelStreamALot2() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000_000).forEach(table::add);
        var group = table.groupBy(x -> x / 10);
        assertTimeoutPreemptively(Duration.ofMillis(1_000),
                () -> assertEquals(1_783_293_664, group.stream().parallel().mapToInt(x -> x).sum()));
    }

    @Test
    @Tag("Q8")
    public void newGroupOfSpliterator2() {
        var table = Table.of(11, 7, 707);
        var group = table.groupBy(v -> v, Integer::compareTo);
        var spliterator = group.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
        assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    @Tag("Q8")
    public void newGroupDynamicSpliterator2() {
        var table = Table.<Integer>dynamic();
        table.add(747);
        var group = table.groupBy(v -> v, Integer::compareTo);
        var spliterator = group.stream().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    @Tag("Q8")
    public void newGroupStreamALot2() {
        var table = Table.<Integer>dynamic();
        range(0, 1_000_000).forEach(table::add);
        var group = table.groupBy(x -> x / 10);
        assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> assertEquals(1_783_293_664, group.stream().mapToInt(x -> x).sum()));
    }

    // Q9
    @Test
    @Tag("Q9")
    public void newGroupStreamSplit() {
        var table = Table.<Integer>dynamic();
        range(0, 10_000).forEach(table::add);
        var group = table.groupBy(x -> x / 10);
        assertNotNull(group.stream().spliterator().trySplit());
    }

    // Q10

    @Test
    @Tag("Q10")
    public void replace() {
        var table = Table.<String>dynamic();
        List.of("foo", "bar", "foo", "whizz").forEach(table::add);
        var group = table.groupBy(String::length, Integer::compareTo);
        table.replace("foo", "baz");
        assertEquals(List.of("baz", "bar", "baz"), group.lookup(3));
    }

    @Test
    @Tag("Q10")
    public void replaceTableOfIntegers() {
        var table = Table.<Integer>dynamic();
        List.of(1, 4, 7).forEach(table::add);
        table.replace(4, 6);
        var group = table.groupBy(x -> x, Integer::compareTo);
        var list = new ArrayList<>();
        group.forEach(list::add);
        assertEquals(List.of(1, 6, 7), list);
    }

    @Test
    @Tag("Q10")
    public void replacePets() {
        record Pet(String name, String owner) {
        }
        var table = Table.<Pet>dynamic();
        List.of(new Pet("snoopy", "Joe"), new Pet("rex", "Bo")).forEach(table::add);
        var group = table.groupBy(Pet::name, String::compareTo);
        table.replace(new Pet("snoopy", "Joe"), new Pet("snoopy", "Jane"));
        assertEquals(List.of(new Pet("rex", "Bo"), new Pet("snoopy", "Jane")), group.stream().collect(toList()));
    }

    @Test
    @Tag("Q10")
    public void replaceTableNonMutable() {
        var table = Table.of(1, 4, 7);
        assertThrows(UnsupportedOperationException.class, () -> table.replace(4, 6));
    }

    @Test
    @Tag("Q10")
    public void replaceInvalidGroup() {
        record Pet(String name, String owner) {
        }
        var table = Table.<Pet>dynamic();
        List.of(new Pet("snoopy", "Joe"), new Pet("rex", "Bo")).forEach(table::add);
        var group = table.groupBy(Pet::name, String::compareTo);
        assertThrows(IllegalStateException.class, () -> table.replace(new Pet("snoopy", "Joe"), new Pet("pywi", "Joe")));
    }

    @Test
    @Tag("Q10")
    public void replaceNPE() {
        var table = Table.<Integer>dynamic();
        table.add(10);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> table.replace(10, null)),
                () -> assertThrows(NullPointerException.class, () -> table.replace(null, 10))
        );
    }

}