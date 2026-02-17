package fr.uge.indexedmap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import module java.base;

import static org.junit.jupiter.api.Assertions.*;

public class IndexedMapTest {

  @Nested
  public class Q1 {

    @Test
    public void testOfCreatesEmptyMap() {
      var map = IndexedMap.of();

      assertEquals(0, map.size());
    }

    @Test
    public void testOfCreatesSingleElementMap() {
      var map = IndexedMap.of("first");

      assertEquals(1, map.size());
    }

    @Test
    public void testOfCreatesMultipleElementMap() {
      var map = IndexedMap.of("first", "second", "third");

      assertEquals(3, map.size());
    }

    @Test
    public void testGetValueOrDefaultReturnsValueForValidKey() {
      var map = IndexedMap.of("apple", "banana", "cherry");

      assertEquals("apple", map.getValueOrDefault(0, "default"));
      assertEquals("banana", map.getValueOrDefault(1, "default"));
      assertEquals("cherry", map.getValueOrDefault(2, "default"));
    }

    @Test
    public void testGetValueOrDefaultReturnsDefaultForNegativeKey() {
      var map = IndexedMap.of("apple", "banana");

      assertEquals("default", map.getValueOrDefault(-1, "default"));
      assertEquals("default", map.getValueOrDefault(-10, "default"));
    }

    @Test
    public void testGetValueOrDefaultReturnsDefaultForKeyEqualToSize() {
      var map = IndexedMap.of("apple", "banana");

      assertEquals("default", map.getValueOrDefault(2, "default"));
    }

    @Test
    public void testGetValueOrDefaultReturnsDefaultForKeyGreaterThanSize() {
      var map = IndexedMap.of("apple", "banana");

      assertEquals("default", map.getValueOrDefault(5, "default"));
      assertEquals("default", map.getValueOrDefault(100, "default"));
    }

    @Test
    public void testOfMutationOfOriginalValues() {
      var values = new String[]{"first", "second", "third"};
      var map = IndexedMap.of(values);
      values[1] = null;

      assertEquals("first", map.getValueOrDefault(0, "default"));
      assertEquals("second", map.getValueOrDefault(1, "default"));
      assertEquals("third", map.getValueOrDefault(2, "default"));
    }

    @Test
    public void testGetValueOrDefaultWithNullDefault() {
      var map = IndexedMap.of("apple", "banana");

      assertNull(map.getValueOrDefault(5, null));
    }

    @Test
    public void testGetValueOrDefaultOnEmptyMap() {
      var map = IndexedMap.of();

      assertEquals("default", map.getValueOrDefault(0, "default"));
    }

    @Test
    public void testOfWithNullElements() {
      assertThrows(NullPointerException.class,
          () -> IndexedMap.of("first", null, "third"));
    }

    @Test
    public void testOfWithOnlyOneElementNull() {
      assertThrows(NullPointerException.class,
          () -> IndexedMap.of(null));
    }

    @Test
    public void testOfWithIntegerType() {
      var map = IndexedMap.of(10, 20, 30);

      assertEquals(3, map.size());
      assertEquals(10, map.getValueOrDefault(0, -1));
      assertEquals(20, map.getValueOrDefault(1, -1));
      assertEquals(-1, map.getValueOrDefault(3, -1));
    }

    @Test
    public void testGetValueOrDefaultBoundaryConditionsAtZero() {
      var map = IndexedMap.of("value");

      assertEquals("value", map.getValueOrDefault(0, "default"));
      assertEquals("default", map.getValueOrDefault(-1, "default"));
    }

    @Test
    public void testGetValueOrDefaultBoundaryConditionsAtMaxIndex() {
      var map = IndexedMap.of("a", "b", "c");

      assertEquals("c", map.getValueOrDefault(2, "default"));
      assertEquals("default", map.getValueOrDefault(3, "default"));
    }

    @Test
    public void testClassShouldBePublicFinal() {
      assertTrue(IndexedMap.class.accessFlags().contains(AccessFlag.PUBLIC));
      assertTrue(IndexedMap.class.accessFlags().contains(AccessFlag.FINAL));
    }

    @Test
    public void testClassShouldNotHaveAPublicConstructor() {
      assertEquals(0, IndexedMap.class.getConstructors().length);
    }

    @Test
    public void testClassShouldHaveOnlyOneFieldOfTypeList() {
      var fields = IndexedMap.class.getDeclaredFields();

      assertEquals(1, fields.length);
      assertTrue(List.class.isAssignableFrom(fields[0].getType()));
    }

    @Test
    public void testClassConstructorsCallSuperAsLastInstruction() throws IOException {
      var className = "/" + IndexedMap.class.getName().replace('.', '/') + ".class";
      byte[] data;
      try (var input = IndexedMap.class.getResourceAsStream(className)) {
        data = input.readAllBytes();
      }
      var classModel = ClassFile.of().parse(data);
      var constructors =
          classModel.methods().stream().filter(m -> m.methodName().equalsString("<init>")).toList();
      for (var constructor : constructors) {
        var code =
            constructor.code().orElseThrow(() -> new AssertionError("Constructor has no code"));
        var instructions =
            code.elementStream()
                .flatMap(e -> e instanceof Instruction instruction ? Stream.of(instruction) : null)
                .toList();
        var lastInstruction =
            instructions.get(instructions.size() - 2); // -2 because last is RETURN
        if (!(lastInstruction instanceof InvokeInstruction invokeInstruction)) {
          throw new AssertionError(
              "lastInstruction is neither super() nor this() " + lastInstruction);
        }
        assertAll(
            () -> assertEquals(Opcode.INVOKESPECIAL, invokeInstruction.opcode()),
            () -> assertEquals("<init>", invokeInstruction.name().stringValue())
        );
      }
    }
  }

  @Nested
  public class Q2 {

    @Test
    public void testKeySetSizeForEmptyMap() {
      var map = IndexedMap.of();
      var keys = map.keySet();

      assertEquals(0, keys.size());
    }

    @Test
    public void testKeySetSizeForSingleElementMap() {
      var map = IndexedMap.of("first");
      var keys = map.keySet();

      assertEquals(1, keys.size());
    }

    @Test
    public void testKeySetSizeForMultipleElementMap() {
      var map = IndexedMap.of("a", "b", "c", "d");
      var keys = map.keySet();

      assertEquals(4, keys.size());
    }

    @Test
    public void testKeySetOfString() {
      var map = IndexedMap.of("apple", "banana", "cherry");
      var keys = map.keySet();

      assertEquals(Set.of(0, 1, 2), keys);
    }

    @Test
    public void testKeySetOfIntegers() {
      var map = IndexedMap.of(10, 20, 40, 1000);
      var keys = map.keySet();

      assertEquals(Set.of(0, 1, 2, 3), keys);
    }

    @Test
    public void testKeySetHashCode() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var hashCode = map.keySet().hashCode();

      assertEquals(Set.of(0, 1, 2, 3, 4).hashCode(), hashCode);
    }

    @Test
    public void testKeySetContainsAllValidKeys() {
      var map = IndexedMap.of("a", "b", "c");
      var keys = map.keySet();

      assertTrue(keys.contains(0));
      assertTrue(keys.contains(1));
      assertTrue(keys.contains(2));
    }

    @Test
    public void testKeySetDoesNotContainInvalidKeys() {
      var map = IndexedMap.of("a", "b", "c");
      var keys = map.keySet();

      assertFalse(keys.contains(-99));
      assertFalse(keys.contains(-1));
      assertFalse(keys.contains(3));
      assertFalse(keys.contains(10));
    }

    @Test
    public void testKeySetToArray() {
      var map = IndexedMap.of("a", "b", "c");
      var keys = map.keySet();
      var array = keys.toArray();

      assertEquals(3, array.length);
      assertArrayEquals(new Object[]{0, 1, 2}, array);
    }

    @Test
    public void testKeySetToArrayOfIntegers() {
      var map = IndexedMap.of("a", "b", "c");
      var keys = map.keySet();
      var array = keys.toArray(new Integer[0]);

      assertEquals(3, array.length);
      assertArrayEquals(new Integer[]{0, 1, 2}, array);
    }

    @Test
    public void testKeySetToArrayFunction() {
      var map = IndexedMap.of("a", "b", "c");
      var keys = map.keySet();
      var array = keys.toArray(Integer[]::new);

      assertEquals(3, array.length);
      assertArrayEquals(new Integer[]{0, 1, 2}, array);
    }

    @Test
    public void testKeySetIsEmpty() {
      var emptyMap = IndexedMap.of();

      assertTrue(emptyMap.keySet().isEmpty());
    }

    @Test
    public void testKeySetIsNotEmpty() {
      var nonEmptyMap = IndexedMap.of("a");

      assertFalse(nonEmptyMap.keySet().isEmpty());
    }

    @Test
    public void testKeySetIsUnmodifiable() {
      var map = IndexedMap.of("a", "b");
      var keySet = map.keySet();

      assertThrows(UnsupportedOperationException.class, () -> keySet.add(0));
      assertThrows(UnsupportedOperationException.class, () -> keySet.remove(0));
      assertThrows(UnsupportedOperationException.class, keySet::clear);
    }

    @Test
    public void testKeySetIteratorHasNextOnEmptyMap() {
      var map = IndexedMap.of();
      var iterator = map.keySet().iterator();

      assertFalse(iterator.hasNext());
    }

    @Test
    public void testKeySetIteratorNextOnEmptyMapThrowsException() {
      var map = IndexedMap.of();
      var iterator = map.keySet().iterator();

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testKeySetIteratorRemoveThrowsException() {
      var map = IndexedMap.of("a", "b");
      var iterator = map.keySet().iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void testKeySetIteratorForSingleElement() {
      var map = IndexedMap.of("first");
      var iterator = map.keySet().iterator();

      assertTrue(iterator.hasNext());
      assertEquals(0, iterator.next());
      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testKeySetIteratorForMultipleElements() {
      var map = IndexedMap.of("a", "b", "c");
      var iterator = map.keySet().iterator();

      assertTrue(iterator.hasNext());
      assertEquals(0, iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals(1, iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals(2, iterator.next());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testKeySetIteratorThrowsExceptionWhenExhausted() {
      var map = IndexedMap.of("a", "b");
      var iterator = map.keySet().iterator();

      iterator.next();
      iterator.next();

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testKeySetIteratorCanBeCalledMultipleTimes() {
      var map = IndexedMap.of("a", "b");
      var keys = map.keySet();
      var iterator1 = keys.iterator();
      var iterator2 = keys.iterator();

      assertEquals(0, iterator1.next());
      assertEquals(0, iterator2.next());
      assertEquals(1, iterator1.next());
      assertEquals(1, iterator2.next());
    }

    @Test
    public void testKeySetIteratorMultipleHasNextCallsDoNotAdvance() {
      var map = IndexedMap.of("a", "b");
      var iterator = map.keySet().iterator();

      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      assertEquals(0, iterator.next());
    }

    @Test
    public void testKeySetFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        var sum = 0;
        for(var i = 0; i < 10_000; i++) {
          sum += map.keySet().iterator().next();
        }
        assertEquals(0, sum);
      });
    }
  }

  @Nested
  public class Q3 {

    @Test
    public void testValuesReturnsEmptyListForEmptyMap() {
      var map = IndexedMap.of();
      var values = map.values();

      assertEquals(0, values.size());
      assertTrue(values.isEmpty());
    }

    @Test
    public void testValuesReturnsSingleElementList() {
      var map = IndexedMap.of("first");
      var values = map.values();

      assertEquals(1, values.size());
      assertEquals("first", values.getFirst());
    }

    @Test
    public void testValuesReturnsMultipleElements() {
      var map = IndexedMap.of("a", "b", "c");
      var values = map.values();

      assertEquals(3, values.size());
      assertEquals("a", values.get(0));
      assertEquals("b", values.get(1));
      assertEquals("c", values.get(2));
    }

    @Test
    public void testValuesOfString() {
      var map = IndexedMap.of("apple", "banana", "cherry");
      var values = map.values();

      assertEquals(List.of("apple", "banana", "cherry"), values);
    }

    @Test
    public void testValuesOfIntegers() {
      var map = IndexedMap.of(10, 20, 40, 1000);
      var values = map.values();

      assertEquals(List.of(10, 20, 40, 1000), values);
    }

    @Test
    public void testValuesReturnsSameListOnMultipleCalls() {
      var map = IndexedMap.of("a", "b", "c");
      var values1 = map.values();
      var values2 = map.values();

      assertEquals(values1, values2);
    }

    @Test
    public void testValuesListIsUnmodifiable() {
      var map = IndexedMap.of("a", "b", "c");
      var values = map.values();

      assertThrows(UnsupportedOperationException.class, () -> values.set(0, "x"));
      assertThrows(UnsupportedOperationException.class, () -> values.add("d"));
      assertThrows(UnsupportedOperationException.class, () -> values.remove(1));
      assertThrows(UnsupportedOperationException.class, values::removeFirst);
      assertThrows(UnsupportedOperationException.class, values::clear);
      assertThrows(UnsupportedOperationException.class, values::removeLast);
    }

    @Test
    public void testValuesContainsAllElements() {
      var map = IndexedMap.of("apple", "banana", "cherry");
      var values = map.values();

      assertTrue(values.contains("apple"));
      assertTrue(values.contains("banana"));
      assertTrue(values.contains("cherry"));
      assertFalse(values.contains("date"));
    }

    @Test
    public void testValuesWithIntegerType() {
      var map = IndexedMap.of(10, 20, 30);
      var values = map.values();

      assertEquals(3, values.size());
      assertEquals(10, values.get(0));
      assertEquals(20, values.get(1));
      assertEquals(30, values.get(2));
    }

    @Test
    public void testValuesIterator() {
      var map = IndexedMap.of("x", "y", "z");
      var values = map.values();
      var iterator = values.iterator();

      assertTrue(iterator.hasNext());
      assertEquals("x", iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals("y", iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals("z", iterator.next());
      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testValuesIteratorNextEmptyMapThrowsException() {
      var map = IndexedMap.of();
      var values = map.values();
      var iterator = values.iterator();

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testValuesIteratorRemoveThrowsException() {
      var map = IndexedMap.of("x", "y", "z");
      var values = map.values();
      var iterator = values.iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void testValuesIndexOf() {
      var map = IndexedMap.of("a", "b", "c", "b");
      var values = map.values();

      assertEquals(0, values.indexOf("a"));
      assertEquals(1, values.indexOf("b"));
      assertEquals(2, values.indexOf("c"));
      assertEquals(-1, values.indexOf("d"));
    }

    @Test
    public void testValuesLastIndexOf() {
      var map = IndexedMap.of("a", "b", "c", "b");
      var values = map.values();

      assertEquals(0, values.lastIndexOf("a"));
      assertEquals(3, values.lastIndexOf("b"));
      assertEquals(2, values.lastIndexOf("c"));
      assertEquals(-1, values.lastIndexOf("d"));
    }

    @Test
    public void testValuesToArray() {
      var map = IndexedMap.of("a", "b", "c");
      var values = map.values();
      var array = values.toArray();

      assertArrayEquals(new Object[]{"a", "b", "c"}, array);
    }

    @Test
    public void testValuesToArrayOfString() {
      var map = IndexedMap.of("a", "b", "c");
      var values = map.values();
      var array = values.toArray(new String[0]);

      assertArrayEquals(new String[]{"a", "b", "c"}, array);
    }

    @Test
    public void testValuesToArrayFunction() {
      var map = IndexedMap.of("a", "b", "c");
      var values = map.values();
      var array = values.toArray(String[]::new);

      assertArrayEquals(new String[]{"a", "b", "c"}, array);
    }

    @Test
    public void testValuesFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        var sum = new Random(0)
            .ints(10_000, 0, 1_000_000)
            .mapToLong(i -> map.values().get(i))
            .sum();
        assertEquals(4_977_017_971L, sum);
      });
    }
  }

  @Nested
  public class Q4 {

    @Test
    public void testForEachWithIntegerType() {
      var map = IndexedMap.of(10, 20, 30);
      var list = new ArrayList<Integer>();
      map.forEach(list::add);

      assertEquals(List.of(10, 20, 30), list);
    }

    @Test
    public void testForEachOnSingleElementMap() {
      var map = IndexedMap.of("first");
      var results = new HashMap<Integer, String>();
      map.forEach(results::putIfAbsent);

      assertEquals(Map.of(0, "first"), results);
    }

    @Test
    public void testForEachCanBuildMap() {
      var map = IndexedMap.of("a", "b", "a");
      var resultMap = new HashMap<Integer, String>();
      map.forEach(resultMap::putIfAbsent);

      assertEquals(Map.of(0, "a", 1, "b", 2, "a"), resultMap);
    }

    @Test
    public void testForEachWithLargeMap() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new));

      map.forEach(Assertions::assertEquals);
    }

    @Test
    public void testForEachThrowsNullPointerExceptionForNullAction() {
      var map = IndexedMap.of("a", "b", "c");

      assertThrows(NullPointerException.class, () -> map.forEach(null));
    }
  }

  @Nested
  public class Q5 {

    @Test
    public void testMapSize() {
      Map<?, ?> map = IndexedMap.of("a", "b", "c");

      assertEquals(3, map.size());
    }

    @Test
    public void testMapIsEmpty() {
      Map<?, ?> map = IndexedMap.of();

      assertTrue(map.isEmpty());
    }

    @Test
    public void testMapIsNotEmpty() {
      Map<?, ?> map = IndexedMap.of("a", "b", "c");

      assertFalse(map.isEmpty());
    }

    @Test
    public void testMapGetKeysExist() {
      var map = IndexedMap.of("a", "b", "c");

      assertEquals("a", map.get(0));
      assertEquals("b", map.get(1));
      assertEquals("c", map.get(2));
    }

    @Test
    public void testMapGetInvalidKeys() {
      var map = IndexedMap.of("a", "b", "c");

      assertNull(map.get("a"));
      assertNull(map.get(-1));
      assertNull(map.get(3));
    }

    @Test
    public void testMapContainsKey() {
      var map = IndexedMap.of("a", "b", "c");

      assertTrue(map.containsKey(0));
      assertTrue(map.containsKey(1));
      assertTrue(map.containsKey(2));
    }

    @Test
    public void testMapContainsKeyInvalidKeys() {
      var map = IndexedMap.of("a", "b", "c");

      assertFalse(map.containsKey("a"));
      assertFalse(map.containsKey(-1));
      assertFalse(map.containsKey(3));
    }

    @Test
    public void testMapValues() {
      Map<?,?> map = IndexedMap.of("a", "b", "c");
      var values = map.values();

      assertEquals(List.of("a", "b", "c"), values);
    }

    @Test
    public void testMapContainsValueWorks() {
      var map = IndexedMap.of("apple", "banana", "cherry");

      assertTrue(map.containsValue("apple"));
      assertTrue(map.containsValue("banana"));
      assertTrue(map.containsValue("cherry"));
      assertFalse(map.containsValue("date"));
    }

    @Test
    public void testMapEntrySetSizeForSingleElementMap() {
      Map<?,?> map = IndexedMap.of("first");
      var entries = map.entrySet();

      assertEquals(1, entries.size());
    }

    @Test
    public void testMapEntrySetSizeForMultipleElementMap() {
      var map = IndexedMap.of("a", "b", "c", "d");
      var entries = map.entrySet();

      assertEquals(4, entries.size());
    }

    @Test
    public void testMapEntrySetIsEmpty() {
      var map = IndexedMap.of();

      assertTrue(map.entrySet().isEmpty());
    }

    @Test
    public void testMapEntrySetIsNotEmpty() {
      var map = IndexedMap.of("a");

      assertFalse(map.entrySet().isEmpty());
    }

    @Test
    public void testMapEntrySetWithIntegerType() {
      var map = IndexedMap.of(10, 20, 30);
      var entries = new ArrayList<>(map.entrySet());

      assertEquals(3, entries.size());
      assertEquals(0, entries.get(0).getKey());
      assertEquals(1, entries.get(1).getKey());
      assertEquals(2, entries.get(2).getKey());
      assertEquals(10, entries.get(0).getValue());
      assertEquals(20, entries.get(1).getValue());
      assertEquals(30, entries.get(2).getValue());
    }

    @Test
    public void testMapEntrySetContains() {
      var map = IndexedMap.of("a", "b");
      var entrySet = map.entrySet();

      assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>(0, "a")));
      assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>(1, "b")));
      assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>(2, "c")));
      assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>(-1, "a")));
      assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>("a", 0)));
      assertFalse(entrySet.contains("a"));
      assertFalse(entrySet.contains(0));
    }

    @Test
    public void testMapEntrySetIteratorForSingleElement() {
      var map = IndexedMap.of("first");
      var iterator = map.entrySet().iterator();

      assertTrue(iterator.hasNext());
      var entry = iterator.next();
      assertEquals(0, entry.getKey());
      assertEquals("first", entry.getValue());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testMapEntrySetIteratorForMultipleElements() {
      var map = IndexedMap.of("a", "b", "c");
      var iterator = map.entrySet().iterator();

      assertTrue(iterator.hasNext());
      var entry1 = iterator.next();
      assertEquals(0, entry1.getKey());
      assertEquals("a", entry1.getValue());

      assertTrue(iterator.hasNext());
      var entry2 = iterator.next();
      assertEquals(1, entry2.getKey());
      assertEquals("b", entry2.getValue());

      assertTrue(iterator.hasNext());
      var entry3 = iterator.next();
      assertEquals(2, entry3.getKey());
      assertEquals("c", entry3.getValue());

      assertFalse(iterator.hasNext());
    }

    @Test
    public void testMapEntrySetIteratorThrowsExceptionWhenExhausted() {
      var map = IndexedMap.of("a", "b");
      var iterator = map.entrySet().iterator();

      iterator.next();
      iterator.next();

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testMapEntrySetIteratorOnEmptyMap() {
      var map = IndexedMap.of();
      var iterator = map.entrySet().iterator();

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testMapEntrySetIteratorRemove() {
      var map = IndexedMap.of("a", "b", "c");
      var iterator = map.entrySet().iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void testMapEntrySetIteratorCanBeCalledMultipleTimes() {
      var map = IndexedMap.of("a", "b");
      var entries = map.entrySet();
      var iterator1 = entries.iterator();
      var iterator2 = entries.iterator();

      var entry1 = iterator1.next();
      var entry2 = iterator2.next();

      assertEquals(entry1.getKey(), entry2.getKey());
      assertEquals(entry1.getValue(), entry2.getValue());
    }

    @Test
    public void testMapEntrySetEntriesAreUnmodifiable() {
      var map = IndexedMap.of("a", "b", "c");
      var iterator = map.entrySet().iterator();
      var entry = iterator.next();

      assertThrows(UnsupportedOperationException.class, () -> entry.setValue("x"));
    }

    @Test
    public void testMapEntrySetIteratorMultipleHasNextCallsDoNotAdvanceIterator() {
      var map = IndexedMap.of("a", "b");
      var iterator = map.entrySet().iterator();

      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      var entry1 = iterator.next();
      assertEquals(0, entry1.getKey());
      assertEquals("a", entry1.getValue());
      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      assertTrue(iterator.hasNext());
      var entry2 = iterator.next();
      assertEquals(1, entry2.getKey());
      assertEquals("b", entry2.getValue());
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testEntrySetFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        var sum = 0;
        for(var i = 0; i < 10_000; i++) {
          sum += map.entrySet().iterator().next().getValue();
        }
        assertEquals(0, sum);
      });
    }
  }

  @Nested
  public class Q6 {

    @Test
    public void testMapGetFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals("" + i, map.get(i));
        }
      });
    }

    @Test
    public void testMapGetNotFoundFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 1_000_000; i < 2_000_000; i++) {
          assertNull(map.get(i));
        }
      });
    }

    @Test
    public void testMapOrDefaultGetFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals("" + i, map.get(i));
        }
      });
    }

    @Test
    public void testMapOrDefaultNotFoundFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = -1_000_000; i < -1; i++) {
          assertEquals("default", map.getOrDefault(i, "default"));
        }
      });
    }
  }


  @Nested
  public class Q7 {
    @Test
    public void testMapContainsKeyFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertTrue(map.containsKey(i));
        }
      });
    }

    @Test
    public void testMapContainsKeyNotFoundFastEnough() {
      Map<Integer, String> map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 1_000_000; i < 2_000_000; i++) {
          assertFalse(map.containsKey(i));
        }
      });
    }

    @Test
    public void testMapKeySetContainsFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));
      var keySet = map.keySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertTrue(keySet.contains(i));
        }
      });
    }

    @Test
    public void testMapKeySetContainsNotFoundFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));
      var keySet = map.keySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = -1_000_000; i < -1; i++) {
          assertFalse(keySet.contains(i));
        }
      });
    }

    @Test
    public void testMapEntrySetContainsFastEnough() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));
      var entrySet = map.entrySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertTrue(entrySet.contains(new AbstractMap.SimpleEntry<>(i, "" + i)));
        }
      });
    }

    @Test
    public void testMapEntrySetContainsBadKeyFastEnough() {
      var map = IndexedMap.of(IntStream.range(1, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));
      var entrySet = map.entrySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 1; i < 1_000_000; i++) {
          assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>("" + i, "" + i)));
        }
      });
    }

    @Test
    public void testMapEntrySetContainsBadValueFastEnough() {
      var map = IndexedMap.of(IntStream.range(1, 1_000_000)
          .mapToObj(i -> "" + i)
          .toArray(String[]::new));
      var entrySet = map.entrySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 1; i < 1_000_000; i++) {
          assertFalse(entrySet.contains(new AbstractMap.SimpleEntry<>(i, "" + -i)));
        }
      });
    }
  }

  @Nested
  public class Q8 {

    @Test
    public void testStreamOnEmptyMap() {
      var map = IndexedMap.of();
      var count = map.stream().count();

      assertEquals(0, count);
    }

    @Test
    public void testStreamOnSingleElementMap() {
      var map = IndexedMap.of("first");
      var entries = map.stream().toList();

      assertEquals(1, entries.size());
      assertEquals(0, entries.getFirst().getKey());
      assertEquals("first", entries.getFirst().getValue());
    }

    @Test
    public void testStreamOnMultipleElementMap() {
      var map = IndexedMap.of("a", "b", "c");
      var entries = map.stream().toList();

      assertEquals(3, entries.size());
      assertEquals(0, entries.get(0).getKey());
      assertEquals(1, entries.get(1).getKey());
      assertEquals(2, entries.get(2).getKey());
      assertEquals("a", entries.get(0).getValue());
      assertEquals("b", entries.get(1).getValue());
      assertEquals("c", entries.get(2).getValue());
    }

    @Test
    public void testStreamMapToList() {
      var map = IndexedMap.of("apple", "banana", "cherry");
      var list = map.stream()
          .map(e -> "" + e)
          .toList();

      assertEquals(List.of("0=apple", "1=banana", "2=cherry"), list);
    }

    @Test
    public void testStreamMaintainsOrder() {
      var map = IndexedMap.of("first", "second", "third", "fourth");
      var values = map.stream()
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of("first", "second", "third", "fourth"), values);
    }

    @Test
    public void testStreamFilter() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var evenIndices = map.stream()
          .filter(entry -> entry.getKey() % 2 == 0)
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of("a", "c", "e"), evenIndices);
    }

    @Test
    public void testStreamMap() {
      var map = IndexedMap.of("a", "b", "c");
      var transformed = map.stream()
          .map(entry -> entry.getKey() + ":" + entry.getValue())
          .toList();

      assertEquals(List.of("0:a", "1:b", "2:c"), transformed);
    }

    @Test
    public void testStreamMapThenCount() {
      var map = IndexedMap.of("a", "b", "c");
      var count = map.stream()
          .map(_ -> fail())
          .count();

      assertEquals(3L, count);
    }

    @Test
    public void testStreamCount() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var count = map.stream().count();

      assertEquals(5L, count);
    }

    @Test
    public void testStreamForEach() {
      var map = IndexedMap.of("x", "y", "z");
      var results = new ArrayList<String>();

      map.stream().forEach(entry ->
          results.add(entry.getKey() + "=" + entry.getValue()));

      assertEquals(List.of("0=x", "1=y", "2=z"), results);
    }

    @Test
    public void testStreamWithIntegerType() {
      var map = IndexedMap.of(10, 20, 30, 40);
      var sum = map.stream()
          .mapToInt(e -> e.getValue())
          .sum();

      assertEquals(100, sum);
    }

    @Test
    public void testStreamCollectToMap() {
      var map = IndexedMap.of("a", "b", "c");
      var resultMap = map.stream()
          .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

      assertEquals(Map.of(0,  "a", 1, "b", 2, "c"), resultMap);
    }

    @Test
    public void testStreamFindFirst() {
      var map = IndexedMap.of("a", "b", "c");
      var first = map.stream().findFirst();

      assertTrue(first.isPresent());
      assertEquals(Map.entry(0, "a"), first.orElseThrow());
    }

    @Test
    public void testStreamFindFirstOnEmptyMap() {
      var map = IndexedMap.of();
      var first = map.stream().findFirst();

      assertTrue(first.isEmpty());
    }

    @Test
    public void testStreamAllMatch() {
      var map = IndexedMap.of("a", "b", "c");
      var allMatch = map.stream()
          .allMatch(entry -> entry.getKey() >= 0);

      assertTrue(allMatch);
    }

    @Test
    public void testStreamAnyMatch() {
      var map = IndexedMap.of("a", "b", "c");
      var anyMatch = map.stream()
          .anyMatch(entry -> entry.getValue().equals("b"));

      assertTrue(anyMatch);
    }

    @Test
    public void testStreamNoneMatch() {
      var map = IndexedMap.of("a", "b", "c");
      var noneMatch = map.stream()
          .noneMatch(entry -> entry.getValue().equals("d"));

      assertTrue(noneMatch);
    }

    @Test
    public void testStreamLimit() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var limited = map.stream()
          .limit(3)
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of("a", "b", "c"), limited);
    }

    @Test
    public void testStreamSkip() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var skipped = map.stream()
          .skip(2)
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of("c", "d", "e"), skipped);
    }

    @Test
    public void testSpliteratorCharacteristics() {
      var map = IndexedMap.of("a", "b", "c");
      var spliterator = map.stream().spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
      assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
      assertFalse(spliterator.hasCharacteristics(Spliterator.SORTED));
    }
  }


  @Nested
  public class Q9 {

    @Test
    public void testParallelStreamOnEmptyMap() {
      var map = IndexedMap.of();
      var count = map.stream().parallel().count();

      assertEquals(0, count);
    }

    @Test
    public void testParallelStreamOnSingleElement() {
      var map = IndexedMap.of("first");
      var count = map.stream().parallel().count();

      assertEquals(1, count);
    }

    @Test
    public void testParallelStreamCountsCorrectly() {
      var map = IndexedMap.of("a", "b", "c", "d", "e", "f", "g", "h");
      var count = map.stream().parallel().count();

      assertEquals(8, count);
    }

    @Test
    public void testParallelStreamCollectsAllElements() {
      var map = IndexedMap.of("a", "b", "c", "d", "e", "f", "g", "h");
      var values = map.stream()
          .parallel()
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h"), values);
    }

    @Test
    public void testParallelStreamSum() {
      var map = IndexedMap.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
      var sum = map.stream()
          .parallel()
          .mapToInt(e -> e.getValue())
          .sum();

      assertEquals(55, sum);
    }

    @Test
    public void testParallelStreamMapThenCount() {
      var map = IndexedMap.of("a", "b", "c");
      var count = map.stream()
          .parallel()
          .map(_ -> fail())
          .count();

      assertEquals(3L, count);
    }

    @Test
    public void testTrySplitReturnsNullWhenEmpty() {
      var map = IndexedMap.of();
      var spliterator = map.stream().spliterator();
      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testTrySplitReturnsNullWhenCannotSplit() {
      var map = IndexedMap.of("a");
      var spliterator = map.stream().spliterator();
      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testTrySplitCreatesIndependentSpliterators() {
      var map = IndexedMap.of("a", "b", "c", "d", "e", "f");
      var spliterator = map.stream().spliterator();
      var split = spliterator.trySplit();

      var mainEntries = new ArrayList<>();
      var splitEntries = new ArrayList<>();
      spliterator.forEachRemaining(mainEntries::add);
      split.forEachRemaining(splitEntries::add);

      var totalSize = mainEntries.size() + splitEntries.size();
      assertEquals(6, totalSize);
    }

    @Test
    public void testTrySplitDividesSpliterator() {
      var map = IndexedMap.of("a", "b", "c", "d");
      var spliterator = map.stream().spliterator();

      var split = spliterator.trySplit();

      assertEquals(2, split.estimateSize());
      assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testSpliteratorEstimateSizeAfterAdvance() {
      var map = IndexedMap.of("a", "b", "c", "d", "e");
      var spliterator = map.stream().spliterator();

      spliterator.tryAdvance(_ -> {});
      spliterator.tryAdvance(_ -> {});
      assertEquals(3, spliterator.estimateSize());
    }

    @Test
    public void testParallelStreamWithFilter() {
      var map = IndexedMap.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
      var evenValues = map.stream()
          .parallel()
          .filter(entry -> entry.getValue() % 2 == 0)
          .map(e -> e.getValue())
          .toList();

      assertEquals(List.of(2, 4, 6, 8, 10), evenValues);
    }

    @Test
    public void testParallelStreamMaintainsAllElements() {
      var map = IndexedMap.of(IntStream.range(0, 1_000_000).mapToObj(i -> "e" + i).toArray(String[]::new));
      var count = map.stream().parallel().count();

      assertEquals(1_000_000, count);
    }
  }

}