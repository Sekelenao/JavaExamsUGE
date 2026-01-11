package fr.uge.orderedmap;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import module java.base;

import static org.junit.jupiter.api.Assertions.*;

public final class OrderedMapTest {
  @Nested
  public class Q1 {

    @Test
    public void testOfWithEmptyMap() {
      var emptyMap = Map.of();
      var orderedMap = OrderedMap.of(emptyMap);

      assertNotNull(orderedMap);
      assertEquals(0, orderedMap.size());
    }

    @Test
    public void testOfWithSingleEntry() {
      var map = Map.of("key1", "value1");
      var orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(1, orderedMap.size());
    }

    @Test
    public void testOfWithMultipleEntries() {
      var map = Map.of(
          "key1", "value1",
          "key2", "value2",
          "key3", "value3"
      );
      var orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(3, orderedMap.size());
    }

    @Test
    public void testOfWithHashMap() {
      var map = new HashMap<String, Integer>();
      map.put("one", 1);
      map.put("two", 2);
      map.put("three", 3);

      var orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(3, orderedMap.size());
    }

    @Test
    public void testOfWithLinkedHashMap() {
      var map = new LinkedHashMap<String, String>();
      map.put("first", "1st");
      map.put("second", "2nd");
      map.put("third", "3rd");

      var orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(3, orderedMap.size());
    }

    @Test
    public void testOfWithDifferentTypes() {
      var map = Map.of(1, "one", 2, "two");
      OrderedMap<Object, Object> orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(2, orderedMap.size());
    }

    @Test
    public void testOfEncapsulation() {
      var map = new LinkedHashMap<String, String>();
      map.put("alpha", "A");
      map.put("beta", "B");

      var orderedMap = OrderedMap.of(map);

      map.put("gamma", "C");

      assertEquals(2, orderedMap.size());
    }

    @Test
    public void testSizeLargeDataSet() {
      var map = IntStream.range(0, 100_000).boxed()
          .collect(Collectors.toMap(i -> i, i -> i));
      var orderedMap = OrderedMap.of(map);

      assertEquals(100_000, orderedMap.size());
    }

    @Test
    public void testOfWithNullMapThrowsNPE() {
      assertThrows(NullPointerException.class, () -> OrderedMap.of(null));
    }

    @Test
    public void testOfWithNullKeyThrowsNPE() {
      var map = new HashMap<String, String>();
      map.put(null, "value");

      assertThrows(NullPointerException.class, () -> OrderedMap.of(map));
    }

    @Test
    public void testOfWithMultipleNullKeysThrowsNPE() {
      var map = new HashMap<String, Integer>();
      map.put("key", 1);
      map.put(null, 2);
      map.put(null, 3);

      assertThrows(NullPointerException.class, () -> OrderedMap.of(map));
    }

    @Test
    public void testOfWithNullValue() {
      var map = new HashMap<String, String>();
      map.put("key", null);

      var orderedMap = OrderedMap.of(map);

      assertNotNull(orderedMap);
      assertEquals(1, orderedMap.size());
    }

    @Test
    public void testOfOldEntryIsNotReferenced() {
      var map = new HashMap<String, String>();
      map.put("key", "value");
      var entry = map.entrySet().iterator().next();

      var refMap = new WeakReference<>(map);
      var refEntry = new WeakReference<>(entry);

      var orderedMap = OrderedMap.of(map);

      map = null;
      entry = null;
      System.gc();

      assertTrue(refMap.refersTo(null));
      assertTrue(refEntry.refersTo(null));
    }

    @Test
    public void testClassIsPublicFinal() {
      assertTrue(OrderedMap.class.accessFlags().contains(AccessFlag.PUBLIC));
      assertTrue(OrderedMap.class.accessFlags().contains(AccessFlag.FINAL));
    }

    @Test
    public void testClassHasNoPublicConstructor() {
      var constructors = OrderedMap.class.getConstructors();
      assertEquals(0, constructors.length);
    }

    @Test
    public void testOrderableMapFieldsArePrivate() {
      for(var field : OrderedMap.class.getDeclaredFields()) {
        assertTrue(field.accessFlags().contains(AccessFlag.PRIVATE), field.getName());
      }
    }

    @Test
    public void testOrderableMapInstanceFieldsAreArrays() {
      var fields = Arrays.stream(OrderedMap.class.getDeclaredFields())
          .filter(f -> !f.accessFlags().contains(AccessFlag.STATIC))
          .toList();
      for(var field : fields) {
        assertTrue(field.getType().isArray(), field.getName());
      }
    }

    @Test
    public void testOrderablePublicInstanceMethodsAreAllDeclaredInMap() {
      var mapMethods = Arrays.stream(Map.class.getDeclaredMethods())
          .filter(m -> m.accessFlags().contains(AccessFlag.PUBLIC) && !m.accessFlags().contains(AccessFlag.STATIC))
          .map(m -> m.getName() + MethodType.methodType(m.getReturnType(), m.getParameterTypes()).descriptorString())
          .collect(Collectors.toSet());
      var orderedMapMethods = Arrays.stream(OrderedMap.class.getDeclaredMethods())
          .filter(m -> m.accessFlags().contains(AccessFlag.PUBLIC) && !m.accessFlags().contains(AccessFlag.STATIC))
          .map(m -> m.getName() + MethodType.methodType(m.getReturnType(), m.getParameterTypes()).descriptorString())
          .toList();
      for(var method : orderedMapMethods) {
        assertTrue(mapMethods.contains(method), method);
      }
    }

    @Test
    public void classConstructorsCallSuperAsLastInstruction() throws IOException {
      var className = "/" + OrderedMap.class.getName().replace('.', '/') + ".class";
      byte[] data;
      try (var input = OrderedMap.class.getResourceAsStream(className)) {
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
    public void testOrderedMap() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("alpha", 11);
      map.put("beta", 22);
      map.put("gamma", 33);
      map.put("delta", 44);
      var orderedMap = OrderedMap.of(map);

      assertEquals(4, orderedMap.size());
      assertEquals(22, orderedMap.get("beta"));
      assertEquals("[alpha, beta, gamma, delta]", "" + orderedMap.keySet());
      assertEquals("[11, 22, 33, 44]", "" + orderedMap.values());
    }

    @Test
    public void testMapOfString() {
      var map = Map.of("key1", "value1", "key2", "value2");
      Map<String, String> orderedMap = OrderedMap.of(map);

      assertEquals(2, orderedMap.size());
      assertNotNull(orderedMap.entrySet());
    }

    @Test
    public void testMapOfInteger() {
      var map = Map.of(1, "value1", 2, "value2");
      Map<Integer, String> orderedMap = OrderedMap.of(map);

      assertEquals(2, orderedMap.size());
      assertNotNull(orderedMap.entrySet());
    }

    @Test
    public void testIsEmpty() {
      var emptyMap = Map.of();
      var orderedMap = OrderedMap.of(emptyMap);

      assertTrue(orderedMap.isEmpty());

      var nonEmptyMap = Map.of("key", "value");
      var orderedMap2 = OrderedMap.of(nonEmptyMap);

      assertFalse(orderedMap2.isEmpty());
    }

    @Test
    public void testGet() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(1, orderedMap.get("a"));
      assertEquals(2, orderedMap.get("b"));
      assertEquals(3, orderedMap.get("c"));
    }

    @Test
    public void testGetWithMutation() {
      var map = new HashMap<Integer, String>();
      map.put(100, "value");
      var entry = map.entrySet().iterator().next();

      var orderedMap = OrderedMap.of(map);
      entry.setValue("newValue");

      assertEquals(1, orderedMap.size());
      assertEquals("value", orderedMap.get(100));
    }

    @Test
    public void testGetValueWithNull() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.get("key"));
    }

    @Test
    public void testGetValueOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.get("any"));
    }

    @Test
    public void testGetOrDefault() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(1, orderedMap.getOrDefault("a", 0));
      assertEquals(2, orderedMap.getOrDefault("b", 0));
      assertEquals(3, orderedMap.getOrDefault("c", 0));
    }

    @Test
    public void testGetOrDefaultValueWithNull() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.getOrDefault("key", "value"));
    }

    @Test
    public void testGetOrDefaultValueOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertEquals("default", orderedMap.getOrDefault("any", "default"));
    }

    @Test
    public void testContainsValue() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsValue(1));
      assertTrue(orderedMap.containsValue(2));
      assertTrue(orderedMap.containsValue(3));
      assertFalse(orderedMap.containsValue(4));
    }

    @Test
    public void testContainsValueWithNull() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsValue(null));
      assertFalse(orderedMap.containsValue("value"));
    }

    @Test
    public void testContainsValueOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertFalse(orderedMap.containsValue("any"));
    }

    @Test
    public void testKeySet() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);

      var keySet = orderedMap.keySet();

      assertEquals(3, keySet.size());
      assertTrue(keySet.contains("a"));
      assertTrue(keySet.contains("b"));
      assertTrue(keySet.contains("c"));
      assertFalse(keySet.contains("d"));
    }

    @Test
    public void testKeySetOrder() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("first", 1);
      map.put("second", 2);
      map.put("third", 3);
      var orderedMap = OrderedMap.of(map);

      var keys = List.copyOf(orderedMap.keySet());

      assertEquals(List.of("first", "second", "third"), keys);
    }

    @Test
    public void testKeySetOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      var keySet = orderedMap.keySet();

      assertTrue(keySet.isEmpty());
      assertEquals(0, keySet.size());
    }

    @Test
    public void testValues() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      var values = orderedMap.values();

      assertEquals(3, values.size());
      assertTrue(values.contains(1));
      assertTrue(values.contains(2));
      assertTrue(values.contains(3));
      assertFalse(values.contains(4));
    }

    @Test
    public void testValuesWithNull() {
      var map = new HashMap<String, String>();
      map.put("key1", null);
      map.put("key2", "value");
      var orderedMap = OrderedMap.of(map);

      var values = orderedMap.values();

      assertEquals(2, values.size());
      assertTrue(values.contains(null));
      assertTrue(values.contains("value"));
    }

    @Test
    public void testValuesOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      var values = orderedMap.values();

      assertTrue(values.isEmpty());
      assertEquals(0, values.size());
    }

    @Test
    public void testEqualsWithSameMap() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);

      assertEquals(orderedMap, orderedMap);
    }

    @Test
    public void testEqualsWithEqualMaps() {
      var map1 = new LinkedHashMap<String, Integer>();
      map1.put("a", 1);
      map1.put("b", 2);
      var orderedMap1 = OrderedMap.of(map1);

      var map2 = new LinkedHashMap<String, Integer>();
      map2.put("a", 1);
      map2.put("b", 2);
      var orderedMap2 = OrderedMap.of(map2);

      assertEquals(orderedMap1, orderedMap2);
    }

    @Test
    public void testEqualsWithDifferentMaps() {
      var map1 = Map.of("a", 1, "b", 2);
      var orderedMap1 = OrderedMap.of(map1);

      var map2 = Map.of("a", 1, "c", 3);
      var orderedMap2 = OrderedMap.of(map2);

      assertNotEquals(orderedMap1, orderedMap2);
    }

    @Test
    public void testEqualsWithRegularMap() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      var orderedMap = OrderedMap.of(map);

      var regularMap = new HashMap<String, Integer>();
      regularMap.put("a", 1);
      regularMap.put("b", 2);

      assertTrue(orderedMap.equals(regularMap));
    }

    @Test
    public void testEqualsWithNull() {
      var map = Map.of("a", 1);
      var orderedMap = OrderedMap.of(map);

      assertFalse(orderedMap.equals(null));
    }

    @Test
    public void testHashCodeConsistency() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);

      var hashCode1 = orderedMap.hashCode();
      var hashCode2 = orderedMap.hashCode();

      assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testHashCodeEqualMaps() {
      var map1 = new LinkedHashMap<String, Integer>();
      map1.put("a", 1);
      map1.put("b", 2);
      var orderedMap1 = OrderedMap.of(map1);

      var map2 = Map.of("a", 1, "b", 2);
      var orderedMap2 = OrderedMap.of(map2);

      assertEquals(orderedMap1.hashCode(), orderedMap2.hashCode());
    }

    @Test
    public void testToString() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      var orderedMap = OrderedMap.of(map);

      assertEquals("{a=1, b=2}", orderedMap.toString());
    }

    @Test
    public void testToStringEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      var string = orderedMap.toString();

      assertEquals("{}", string);
    }

    @Test
    public void testPutThrowsUnsupportedOperationException() {
      var map = Map.of("a", 1);
      var orderedMap = OrderedMap.of(map);

      assertThrows(UnsupportedOperationException.class, () ->
          orderedMap.put("b", 2));
    }

    @Test
    public void testRemoveThrowsUnsupportedOperationException() {
      var map = Map.of("a", 1);
      var orderedMap = OrderedMap.of(map);

      assertThrows(UnsupportedOperationException.class, () ->
          orderedMap.remove("a"));
    }

    @Test
    public void testPutAllThrowsUnsupportedOperationException() {
      var map = Map.of("a", 1);
      var orderedMap = OrderedMap.of(map);

      assertThrows(UnsupportedOperationException.class, () ->
          orderedMap.putAll(Map.of("b", 2)));
    }

    @Test
    public void testClearThrowsUnsupportedOperationException() {
      var map = Map.of("a", 1);
      var orderedMap = OrderedMap.of(map);

      assertThrows(UnsupportedOperationException.class, orderedMap::clear);
    }

    @Test
    public void testKeySetSize() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertEquals(3, keySet.size());
    }

    @Test
    public void testKeySetSizeEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertEquals(0, keySet.size());
    }

    @Test
    public void testKeySetIterator() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var keys = new ArrayList<String>();
      for (var key : keySet) {
        keys.add(key);
      }

      assertEquals(3, keys.size());
      assertTrue(keys.contains("a"));
      assertTrue(keys.contains("b"));
      assertTrue(keys.contains("c"));
    }

    @Test
    public void testKeySetIteratorOrder() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("first", 1);
      map.put("second", 2);
      map.put("third", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var keys = new ArrayList<String>();
      for (var key : keySet) {
        keys.add(key);
      }

      assertEquals(List.of("first", "second", "third"), keys);
    }

    @Test
    public void testKeySetContainsExistingKey() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertTrue(keySet.contains("a"));
      assertTrue(keySet.contains("b"));
      assertTrue(keySet.contains("c"));
    }

    @Test
    public void testKeySetContainsNonExistentKey() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertFalse(keySet.contains("z"));
      assertFalse(keySet.contains("missing"));
    }

    @Test
    public void testKeySetContainsOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertFalse(keySet.contains("any"));
    }

    @Test
    public void testKeySetContainsWithDifferentTypes() {
      var map = Map.of(1, "one", "two", 2, 3.0, "three");
      OrderedMap<Object, Object> orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertTrue(keySet.contains(1));
      assertTrue(keySet.contains("two"));
      assertTrue(keySet.contains(3.0));
      assertFalse(keySet.contains(4));
    }

    @Test
    public void testKeySetIsEmpty() {
      var emptyMap = Map.of();
      var orderedMap = OrderedMap.of(emptyMap);
      var keySet = orderedMap.keySet();

      assertTrue(keySet.isEmpty());

      var nonEmptyMap = Map.of("key", "value");
      var orderedMap2 = OrderedMap.of(nonEmptyMap);
      var keySet2 = orderedMap2.keySet();

      assertFalse(keySet2.isEmpty());
    }

    @Test
    public void testKeySetToArray() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var array = keySet.toArray();

      assertSame(Object[].class, array.getClass());
      assertEquals(3, array.length);
      var list = List.of(array);
      assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    public void testKeySetToArrayWithTyper() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var array = keySet.toArray(new String[0]);

      assertSame(String[].class, array.getClass());
      assertEquals(3, array.length);
      var list = List.of(array);
      assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    public void testKeySetToArrayWithFunction() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var array = keySet.toArray(String[]::new);

      assertSame(String[].class, array.getClass());
      assertEquals(3, array.length);
      var list = List.of(array);
      assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    public void testKeySetForEach() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var keys = new ArrayList<String>();
      keySet.forEach(keys::add);

      assertEquals(List.of("a", "b", "c"), keys);
    }

    @Test
    public void testKeySetMultipleCalls() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);

      var keySet1 = orderedMap.keySet();
      var keySet2 = orderedMap.keySet();

      assertEquals(keySet1.size(), keySet2.size());
      assertTrue(keySet1.contains("a"));
      assertTrue(keySet2.contains("a"));
    }

    @Test
    public void testKeySetContainsAll() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertTrue(keySet.containsAll(List.of("a", "b")));
      assertTrue(keySet.containsAll(List.of("a", "b", "c")));
      assertFalse(keySet.containsAll(List.of("a", "b", "d")));
    }

    @Test
    public void testKeySetLargeDataSet() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertEquals(1_000_000, keySet.size());
      assertTrue(keySet.contains(0));
      assertTrue(keySet.contains(500));
      assertTrue(keySet.contains(999));
      assertTrue(keySet.contains(99_977));
      assertFalse(keySet.contains(1_000_000));
      assertFalse(keySet.contains(-1));
    }

    @Test
    public void testKeySetIteratorHasNext() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();
      var iterator = keySet.iterator();

      assertTrue(iterator.hasNext());
      assertEquals("a", iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals("b", iterator.next());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testKeySetIteratorOnEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();
      var iterator = keySet.iterator();

      assertFalse(iterator.hasNext());
    }

    @Test
    public void testEntrySetSize() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);
      var entrySet = orderedMap.entrySet();

      assertEquals(3, entrySet.size());
    }

    @Test
    public void testEntrySetSizeEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var entrySet = orderedMap.entrySet();

      assertEquals(0, entrySet.size());
    }

    @Test
    public void testEntrySetForEachLoop() {
      var map = new LinkedHashMap<String, String>();
      map.put("x", "X");
      map.put("y", "Y");
      map.put("z", "Z");
      var orderedMap = OrderedMap.of(map);

      for (var entry : orderedMap.entrySet()) {
        var key = entry.getKey();
        var value = entry.getValue();
        assertEquals(value, key.toUpperCase(Locale.ROOT));
      }
    }

    @Test
    public void testEntrySetWithNullValues() {
      var map = new HashMap<String, String>();
      map.put("key1", null);
      var orderedMap = OrderedMap.of(map);
      var entrySet = orderedMap.entrySet();

      for (var entry : entrySet) {
        assertNull(entry.getValue());
      }
    }

    @Test
    public void testEntrySetIterationConsistentAcrossMultipleIterations() {
      var map = new LinkedHashMap<Integer, String>();
      map.put(1, "one");
      map.put(2, "two");
      map.put(3, "three");
      var orderedMap = OrderedMap.of(map);

      var firstIteration = new ArrayList<Integer>();
      for (var entry : orderedMap.entrySet()) {
        firstIteration.add(entry.getKey());
      }

      var secondIteration = new ArrayList<Integer>();
      for (var entry : orderedMap.entrySet()) {
        secondIteration.add(entry.getKey());
      }

      assertEquals(firstIteration, secondIteration);
    }

    @Test
    public void testEntrySetOrderMatchesOriginalMap() {
      var map = new LinkedHashMap<String, String>();
      map.put("alpha", "A");
      map.put("beta", "B");
      map.put("gamma", "C");
      map.put("delta", "D");
      var orderedMap = OrderedMap.of(map);

      var keys = new ArrayList<String>();
      for (var entry : orderedMap.entrySet()) {
        keys.add(entry.getKey());
      }

      assertEquals(List.copyOf(map.keySet()), keys);
    }

    @Test
    public void testEntrySetIteratorHasNext() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var iterator = orderedMap.entrySet().iterator();

      assertTrue(iterator.hasNext());
      assertEquals(Map.entry("key", "value"), iterator.next());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testEntrySetIteratorNext() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("one", 1);
      map.put("two", 2);
      map.put("three", 3);
      var orderedMap = OrderedMap.of(map);
      var iterator = orderedMap.entrySet().iterator();

      var entries = new ArrayList<Map.Entry<String, Integer>>();
      while (iterator.hasNext()) {
        entries.add(iterator.next());
      }

      assertEquals(
          List.of(Map.entry("one", 1), Map.entry("two", 2), Map.entry("three", 3)),
          entries);
    }

    @Test
    public void testEntrySetIteratorMultipleCalls() {
      var map = new LinkedHashMap<String, Integer>() {{
        put("a", 1);
        put("b", 2);
      }};
      var orderedMap = OrderedMap.of(map);

      var iterator1 = orderedMap.entrySet().iterator();
      var iterator2 = orderedMap.entrySet().iterator();

      assertTrue(iterator1.hasNext());
      assertTrue(iterator2.hasNext());
      assertEquals(Map.entry("a", 1), iterator1.next());
      assertEquals(Map.entry("a", 1), iterator2.next());
      assertTrue(iterator1.hasNext());
      assertTrue(iterator2.hasNext());
      assertEquals(Map.entry("b", 2), iterator1.next());
      assertEquals(Map.entry("b", 2), iterator2.next());
      assertFalse(iterator1.hasNext());
      assertFalse(iterator2.hasNext());
    }

    @Test
    public void testEntrySetIteratorNextOnEmptyThrowsNoSuchElementException() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var iterator = orderedMap.entrySet().iterator();

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testEntrySetIteratorNextThrowsNoSuchElementException() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var iterator = orderedMap.entrySet().iterator();

      iterator.next();
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testEntrySetIteratorRemoveThrowsUnsupportedOperationException() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var iterator = orderedMap.entrySet().iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
  }

  @Nested
  public class Q3 {

    @Test
    public void testEntrySetStreamSum() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);

      var sum = orderedMap.entrySet().stream()
          .mapToInt(Map.Entry::getValue)
          .sum();

      assertEquals(6, sum);
    }

    @Test
    public void testEntrySetStreamPreservesOrder() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("first", 1);
      map.put("second", 2);
      map.put("third", 3);
      var orderedMap = OrderedMap.of(map);

      var keys = orderedMap.entrySet().stream()
          .map(Map.Entry::getKey)
          .toList();

      assertEquals(List.of("first", "second", "third"), keys);
    }

    @Test
    public void testEntrySetOrderWithStream() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("foo", 10);
      map.put("bar", 20);
      map.put("baz", 30);
      map.put("whizz", 30);
      var orderedMap = OrderedMap.of(map);

      var keys = orderedMap.entrySet().stream()
          .map(Map.Entry::getKey)
          .toList();

      assertEquals(List.of("foo", "bar", "baz", "whizz"), keys);
    }

    @Test
    public void testEntrySetStreamCount() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      var count = orderedMap.entrySet().stream()
          .peek(_ -> fail())
          .count();

      assertEquals(3, count);
    }

    @Test
    public void testEntrySetOrderWithLargeDataSet() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, i * 10));
      var orderedMap = OrderedMap.of(map);

      assertEquals(IntStream.range(0, 1_000_000).boxed().toList(),
          orderedMap.entrySet().stream().map(Map.Entry::getKey).toList());
      assertEquals(IntStream.range(0, 1_000_000).mapToObj(i -> i * 10).toList(),
          orderedMap.entrySet().stream().map(Map.Entry::getValue).toList());
    }

    @Test
    public void testEntrySetSpliteratorTryAdvance() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var entries = new ArrayList<Map.Entry<String, Integer>>();
      spliterator.tryAdvance(entries::add);
      spliterator.tryAdvance(entries::add);
      spliterator.tryAdvance(entries::add);

      assertEquals(3, entries.size());
      assertEquals("a", entries.get(0).getKey());
      assertEquals("b", entries.get(1).getKey());
      assertEquals("c", entries.get(2).getKey());
    }

    @Test
    public void testEntrySetSpliteratorTryAdvanceReturnsFalseWhenExhausted() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      assertTrue(spliterator.tryAdvance(_ -> {}));
      assertFalse(spliterator.tryAdvance(_ -> {}));
    }

    @Test
    public void testEntrySetSpliteratorTryAdvanceOnEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      assertFalse(spliterator.tryAdvance(_ -> {}));
    }

    @Test
    public void testEntrySetSpliteratorCharacteristics() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL)),
          () -> assertFalse(spliterator.hasCharacteristics(Spliterator.CONCURRENT)),
          () -> assertFalse(spliterator.hasCharacteristics(Spliterator.SORTED))
      );
    }

    @Test
    public void testEntrySetSpliteratorEstimateSize() {
      var map = Map.of("a", 1, "b", 2, "c", 3, "d", 4);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      assertEquals(4, spliterator.estimateSize());
      spliterator.tryAdvance(_ -> {});
      assertEquals(3, spliterator.estimateSize());
      spliterator.tryAdvance(_ -> {});
      assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testEntrySetSpliteratorEstimateSizeEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      assertEquals(0, spliterator.estimateSize());
    }

    @Test
    public void testEntrySetSpliteratorForEachRemaining() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("x", 10);
      map.put("y", 20);
      map.put("z", 30);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var entries = new ArrayList<Map.Entry<String, Integer>>();
      spliterator.forEachRemaining(entries::add);

      assertEquals(3, entries.size());
      assertEquals("x", entries.get(0).getKey());
      assertEquals(10, entries.get(0).getValue());
      assertEquals("y", entries.get(1).getKey());
      assertEquals(20, entries.get(1).getValue());
      assertEquals("z", entries.get(2).getKey());
      assertEquals(30, entries.get(2).getValue());
    }
  }

  @Nested
  public class Q4 {
    @SafeVarargs
    private static <T> T[] fromEntries(T... entries) {
      return entries;
    }

    @Test
    public void testIndexArraySizeIsDoubleEntries() {
      var entries = fromEntries(
          Map.entry("a", 1),
          Map.entry("b", 2),
          Map.entry("c", 3)
      );
      var indexArray = OrderedMap.indexArray(entries);

      assertEquals(6, indexArray.length);
    }

    @Test
    public void testIndexArrayWithSingleEntry() {
      var entries = fromEntries(Map.entry("key", "value"));
      var indexArray = OrderedMap.indexArray(entries);

      assertEquals(2, indexArray.length);
      assertTrue(indexArray[0] == 1 || indexArray[1] == 1);
    }

    @Test
    public void testIndexArrayContainsAllIndices() {
      var entries = fromEntries(
          Map.entry("a", 10),
          Map.entry("b", 20),
          Map.entry("c", 30)
      );

      var indexArray = OrderedMap.indexArray(entries);

      var foundIndices = new HashSet<Integer>();
      for (var index : indexArray) {
        if (index != 0) {
          foundIndices.add(index);
        }
      }

      assertEquals(Set.of(1, 2, 3), foundIndices);
    }

    @Test
    public void testIndexArrayWithCollisions() {
      var entries = fromEntries(
          Map.entry("alpha", 11),
          Map.entry("beta", 22),
          Map.entry("gamma", 33),
          Map.entry("delta", 44)
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{2, 4, 0, 0, 0, 0, 1, 3}, indexArray);
    }

    @Test
    public void testIndexArrayWithCollisionsOfHashCode() {
      var entries = fromEntries(
          Map.entry("FB", 10),
          Map.entry("Ea", 20),
          Map.entry("Aa", 30),
          Map.entry("BB", 40)
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{3, 4, 0, 0, 1, 2, 0, 0}, indexArray);
    }

    @Test
    public void testIndexArrayLinearProbing() {
      var entries = fromEntries(
          Map.entry(0, "zero"),
          Map.entry(8, "eight"),
          Map.entry(16, "sixteen"),
          Map.entry(32, "thirty-two")
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{1, 2, 3, 4, 0, 0, 0, 0}, indexArray);
    }

    @Test
    public void testIndexArrayLinearProbing2() {
      var entries = fromEntries(
          Map.entry(7, "seven"),
          Map.entry(15, "fifteen"),
          Map.entry(31, "thirty-one"),
          Map.entry(39, "thirty-nine")
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{2, 3, 4, 0, 0, 0, 0, 1}, indexArray);
    }

    @Test
    public void testIndexArrayNoOrder() {
      var entries = fromEntries(
          Map.entry("baz", 100),
          Map.entry("bar", 200),
          Map.entry("foo", 300)
      );
      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{3, 0, 0, 2, 0, 1}, indexArray);
    }

    @Test
    public void testIndexArrayAllEntriesFindable() {
      var entries = fromEntries(
          Map.entry("alpha", 11),
          Map.entry("beta", 22),
          Map.entry("gamma", 33),
          Map.entry("delta", 44)
      );

      var indexArray = OrderedMap.indexArray(entries);

      var set = Arrays.stream(indexArray).boxed().collect(Collectors.toSet());
      assertEquals(Set.of(0, 1, 2, 3, 4), set);
    }

    @Test
    public void testIndexArrayHandlesNegativeHashCodes() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return -5;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return -10;
        }
      };
      var entries = fromEntries(
          Map.entry(key1, "negative1"),
          Map.entry(key2, "negative2")
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{0, 0, 2, 1}, indexArray);
    }

    @Test
    public void testIndexArrayHandlesIntegerLimitValuesHashCodes() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return Integer.MIN_VALUE;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return Integer.MAX_VALUE;
        }
      };
      var entries = fromEntries(
          Map.entry(key1, "minValue"),
          Map.entry(key2, "maxValue")
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{1, 0, 0, 2}, indexArray);
    }

    @Test
    public void testIndexArrayWithIdenticalHashCodes() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var key3 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var entries = fromEntries(
          Map.entry(key1, "same1"),
          Map.entry(key2, "same2"),
          Map.entry(key3, "same3")
      );

      var indexArray = OrderedMap.indexArray(entries);

      assertArrayEquals(new int[]{3, 0, 0, 0, 1, 2}, indexArray);
    }

    @Test
    public void testIndexArrayEqualsShouldNotBeCalled() {
      var key1 = new Object() {
        @Override
        public boolean equals(Object obj) {
          throw new AssertionError("Should not be called");
        }
      };
      var key2 = new Object() {
        @Override
        public boolean equals(Object obj) {
          throw new AssertionError("Should not be called");
        }
      };
      var entries = fromEntries(
          Map.entry(key1, "key1"),
          Map.entry(key2, "key2")
      );

      assertNotNull(OrderedMap.indexArray(entries));
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void testGetWithExistingKey() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(1, orderedMap.get("a"));
      assertEquals(2, orderedMap.get("b"));
      assertEquals(3, orderedMap.get("c"));
    }

    @Test
    public void testGetWithNonExistentKey() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.get("z"));
      assertNull(orderedMap.get("missing"));
    }

    @Test
    public void testGetWithNullKeyThrowsNPE() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);

      assertThrows(NullPointerException.class, () -> orderedMap.get(null));
    }

    @Test
    public void testGetOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.get("any"));
    }

    @Test
    public void testGetWithNullValue() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.get("key"));
    }

    @Test
    public void testGetWithDifferentTypes() {
      var map = Map.of(1, "one", "two", 2, 3.0, "three");
      OrderedMap<Object, Object> orderedMap = OrderedMap.of(map);

      assertEquals("one", orderedMap.get(1));
      assertEquals(2, orderedMap.get("two"));
      assertEquals("three", orderedMap.get(3.0));
      assertNull(orderedMap.get(4));
    }

    @Test
    public void testGetWithCollisions() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var map = new LinkedHashMap<>();
      map.put(key1, "value1");
      map.put(key2, "value2");
      var orderedMap = OrderedMap.of(map);

      assertEquals("value1", orderedMap.get(key1));
      assertEquals("value2", orderedMap.get(key2));
    }

    @Test
    public void testGetFastEnough() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for (var i = 0; i < 1_000_000; i++) {
          assertEquals("value" + i, orderedMap.get(i));
        }
      });
    }


    @Test
    public void testGetOrDefault() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("alpha", 11);
      map.put("beta", 22);
      map.put("gamma", 33);
      map.put("delta", 44);
      var orderedMap = OrderedMap.of(map);

      assertEquals(11, orderedMap.getOrDefault("alpha", 0));
      assertEquals(22, orderedMap.getOrDefault("beta", 0));
      assertEquals(33, orderedMap.getOrDefault("gamma", 0));
      assertEquals(44, orderedMap.getOrDefault("delta", 0));
    }

    @Test
    public void testGetOrDefaultWithExistingKey() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(2, orderedMap.getOrDefault("b", 999));
    }

    @Test
    public void testGetOrDefaultWithNonExistentKey() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(999, orderedMap.getOrDefault("z", 999));
    }

    @Test
    public void testGetOrDefaultWithNullDefaultValue() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);

      assertEquals("value", orderedMap.getOrDefault("key", null));
      assertNull(orderedMap.getOrDefault("missing", null));
    }

    @Test
    public void testGetOrDefaultWithNullKeyThrowsNPE() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);

      assertThrows(NullPointerException.class, () ->
          orderedMap.getOrDefault(null, "default"));
    }

    @Test
    public void testGetOrDefaultWithNullValue() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertNull(orderedMap.getOrDefault("key", "default"));
    }

    @Test
    public void testGetOrDefaultOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertEquals(42, orderedMap.getOrDefault("any", 42));
    }

    @Test
    public void testGetOrDefaultMultipleKeys() {
      var map = Map.of("one", 1, "two", 2, "three", 3, "four", 4);
      var orderedMap = OrderedMap.of(map);

      assertEquals(1, orderedMap.getOrDefault("one", 0));
      assertEquals(2, orderedMap.getOrDefault("two", 0));
      assertEquals(3, orderedMap.getOrDefault("three", 0));
      assertEquals(4, orderedMap.getOrDefault("four", 0));
      assertEquals(0, orderedMap.getOrDefault("five", 0));
    }

    @Test
    public void testGetOrDefaultWithCollisions() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return 42;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return 42;
        }
      };
      var map = new LinkedHashMap<>();
      map.put(key1, "value1");
      map.put(key2, "value2");
      var orderedMap = OrderedMap.of(map);

      assertEquals("value1", orderedMap.getOrDefault(key1, "default"));
      assertEquals("value2", orderedMap.getOrDefault(key2, "default"));
    }

    @Test
    public void testGetOrDefaultWithDifferentTypes() {
      var map = Map.of(1, "one", "two", 2, 3.0, "three");
      OrderedMap<Object, Object> orderedMap = OrderedMap.of(map);

      assertEquals("one", orderedMap.getOrDefault(1, "default"));
      assertEquals(2, orderedMap.getOrDefault("two", "default"));
      assertEquals("three", orderedMap.getOrDefault(3.0, "default"));
      assertEquals("default", orderedMap.getOrDefault(4, "default"));
    }

    @Test
    public void testGetOrDefaultReusesIndexArray() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertEquals(1, orderedMap.getOrDefault("a", 0));
      assertEquals(2, orderedMap.getOrDefault("b", 0));
      assertEquals(3, orderedMap.getOrDefault("c", 0));
    }

    @Test
    public void testGetOrDefaultWithStringKeys() {
      var map = Map.of("apple", "red", "banana", "yellow", "grape", "purple");
      var orderedMap = OrderedMap.of(map);

      assertEquals("red", orderedMap.getOrDefault("apple", "unknown"));
      assertEquals("yellow", orderedMap.getOrDefault("banana", "unknown"));
      assertEquals("purple", orderedMap.getOrDefault("grape", "unknown"));
      assertEquals("unknown", orderedMap.getOrDefault("orange", "unknown"));
    }

    @Test
    public void testGetOrDefaultWithIntegerKeys() {
      var map = Map.of(10, "ten", 20, "twenty", 30, "thirty");
      var orderedMap = OrderedMap.of(map);

      assertEquals("ten", orderedMap.getOrDefault(10, "default"));
      assertEquals("twenty", orderedMap.getOrDefault(20, "default"));
      assertEquals("default", orderedMap.getOrDefault(40, "default"));
    }

    @Test
    public void testGetOrDefaultLargeDataSet() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        assertEquals("value0", orderedMap.getOrDefault(0, "default"));
        assertEquals("value500", orderedMap.getOrDefault(500, "default"));
        assertEquals("value9997", orderedMap.getOrDefault(9997, "default"));
        assertEquals("default", orderedMap.getOrDefault(1_000_000, "default"));
        assertEquals("default", orderedMap.getOrDefault(-1, "default"));
      });
    }

    @Test
    public void testGetOrDefaultFastEnough() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for (var i = 0; i < 1_000_000; i++) {
          assertEquals("value" + i, orderedMap.getOrDefault(i, "default"));
        }
      });
    }

    @Test
    public void testGetOrDefaultWithNegativeHashCode() {
      var key = new Object() {
        @Override
        public int hashCode() {
          return -42;
        }
      };
      var map = new LinkedHashMap<>();
      map.put(key, "negative");
      var orderedMap = OrderedMap.of(map);

      assertEquals("negative", orderedMap.getOrDefault(key, "default"));
    }

    @Test
    public void testGetOrDefaultConsistency() {
      var map = Map.of("x", 100, "y", 200, "z", 300);
      var orderedMap = OrderedMap.of(map);

      for (var i = 0; i < 10; i++) {
        assertEquals(100, orderedMap.getOrDefault("x", 0));
        assertEquals(200, orderedMap.getOrDefault("y", 0));
        assertEquals(300, orderedMap.getOrDefault("z", 0));
        assertEquals(0, orderedMap.getOrDefault("w", 0));
      }
    }

    @Test
    public void testIndexArrayNotComputedTooEarly() throws IllegalAccessException {
      var arrayField = Arrays.stream(OrderedMap.class.getDeclaredFields())
          .filter(field -> field.getType() == int[].class)
          .findFirst().orElseThrow();
      arrayField.setAccessible(true);

      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);
      assertNull(arrayField.get(orderedMap));
    }

    @Test
    public void testIndexArrayNotComputedWhenUsingALoop() throws IllegalAccessException {
      var arrayField = Arrays.stream(OrderedMap.class.getDeclaredFields())
          .filter(field -> field.getType() == int[].class)
          .findFirst().orElseThrow();
      arrayField.setAccessible(true);

      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      var sum = 0;
      for(var entry : map.entrySet()) {
        sum += entry.getValue();
      }
      assertEquals(6, sum);

      assertNull(arrayField.get(orderedMap));
    }

    @Test
    public void testIndexArrayNotComputedWhenUsingAStream() throws IllegalAccessException {
      var arrayField = Arrays.stream(OrderedMap.class.getDeclaredFields())
          .filter(field -> field.getType() == int[].class)
          .findFirst().orElseThrow();
      arrayField.setAccessible(true);

      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      var sum = map.entrySet().stream().mapToInt(Map.Entry::getValue).sum();
      assertEquals(6, sum);

      assertNull(arrayField.get(orderedMap));
    }
  }

  @Nested
  public class Q6 {

    @Test
    public void testContainsKeyWithExistingKey() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsKey("a"));
      assertTrue(orderedMap.containsKey("b"));
      assertTrue(orderedMap.containsKey("c"));
    }

    @Test
    public void testContainsKeyWithNonExistentKey() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);

      assertFalse(orderedMap.containsKey("z"));
      assertFalse(orderedMap.containsKey("missing"));
    }

    @Test
    public void testContainsKeyWithNullKeyThrowsNPE() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);

      assertThrows(NullPointerException.class, () -> orderedMap.containsKey(null));
    }

    @Test
    public void testContainsKeyOnEmptyMap() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);

      assertFalse(orderedMap.containsKey("any"));
    }

    @Test
    public void testContainsKeyWithNullValue() {
      var map = new HashMap<String, String>();
      map.put("key", null);
      var orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsKey("key"));
    }

    @Test
    public void testContainsKeyWithDifferentTypes() {
      var map = Map.of(1, "one", "two", 2, 3.0, "three");
      OrderedMap<Object, Object> orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsKey(1));
      assertTrue(orderedMap.containsKey("two"));
      assertTrue(orderedMap.containsKey(3.0));
      assertFalse(orderedMap.containsKey(4));
    }

    @Test
    public void testContainsKeyWithCollisions() {
      var key1 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var key2 = new Object() {
        @Override
        public int hashCode() {
          return 40;
        }
      };
      var map = new LinkedHashMap<>();
      map.put(key1, "value1");
      map.put(key2, "value2");
      var orderedMap = OrderedMap.of(map);

      assertTrue(orderedMap.containsKey(key1));
      assertTrue(orderedMap.containsKey(key2));
    }

    @Test
    public void testContainsKeyFastEnough() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for (var i = 0; i < 1_000_000; i++) {
          assertTrue(orderedMap.containsKey(i));
        }
      });
    }
  }

  /*

  @Nested
  public class Q7 {
    @Test
    public void testKeySetContainsWithNullOnEmptyMapThrowsNPE() {
      var map = new HashMap<>();
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertThrows(NullPointerException.class, () -> keySet.contains(null));
    }

    @Test
    public void testKeySetContainsAllWithNullThrowsNPE() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertThrows(NullPointerException.class, () ->
          keySet.containsAll(Arrays.asList("a", null)));
    }

    @Test
    public void testKeySetContainsFastEnough() {
      var map = new LinkedHashMap<Integer, String>();
      IntStream.range(0, 1_000_000).forEach(i -> map.put(i, "value" + i));
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for (var i = 0; i < 1_000_000; i++) {
          assertTrue(keySet.contains(i));
        }
      });
    }

    @Test
    public void testKeySetStream() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("x", 1);
      map.put("y", 2);
      map.put("z", 3);
      var orderedMap = OrderedMap.of(map);
      var keySet = orderedMap.keySet();

      var keys = keySet.stream().toList();

      assertEquals(List.of("x", "y", "z"), keys);
    }

    @Test
    public void testKeySetStreamSum() {
      var map = new LinkedHashMap<Integer, Integer>();
      map.put(1, 10);
      map.put(2, 20);
      map.put(3, 30);
      var orderedMap = OrderedMap.of(map);

      var sum = orderedMap.keySet().stream()
          .mapToInt(k -> k)
          .sum();

      assertEquals(6, sum);
    }

    @Test
    public void testKeySetStreamPreservesOrder() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("first", 1);
      map.put("second", 2);
      map.put("third", 3);
      var orderedMap = OrderedMap.of(map);

      var keys = orderedMap.keySet().stream().toList();

      assertEquals(List.of("first", "second", "third"), keys);
    }

    @Test
    public void testKeySetStreamCount() {
      var map = Map.of("a", 1, "b", 2, "c", 3);
      var orderedMap = OrderedMap.of(map);

      var count = orderedMap.keySet().stream()
          .peek(_ -> fail())
          .count();

      assertEquals(3, count);
    }

    @Test
    public void testKeySetSpliteratorTryAdvance() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("a", 1);
      map.put("b", 2);
      map.put("c", 3);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var list = new ArrayList<String>();
      spliterator.tryAdvance(list::add);
      spliterator.tryAdvance(list::add);
      spliterator.tryAdvance(list::add);

      assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    public void testKeySetSpliteratorTryAdvanceReturnsFalseWhenExhausted() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      assertTrue(spliterator.tryAdvance(_ -> {}));
      assertFalse(spliterator.tryAdvance(_ -> {}));
    }

    @Test
    public void testKeySetSpliteratorTryAdvanceOnEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      assertFalse(spliterator.tryAdvance(_ -> {}));
    }

    @Test
    public void testKeySetSpliteratorCharacteristics() {
      var map = Map.of("a", 1, "b", 2);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL)),
          () -> assertFalse(spliterator.hasCharacteristics(Spliterator.CONCURRENT)),
          () -> assertFalse(spliterator.hasCharacteristics(Spliterator.SORTED))
      );
    }

    @Test
    public void testKeySetSpliteratorEstimateSize() {
      var map = Map.of("a", 1, "b", 2, "c", 3, "d", 4);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      assertEquals(4, spliterator.estimateSize());
      spliterator.tryAdvance(_ -> {});
      assertEquals(3, spliterator.estimateSize());
      spliterator.tryAdvance(_ -> {});
      assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testKeySetSpliteratorEstimateSizeEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      assertEquals(0, spliterator.estimateSize());
    }

    @Test
    public void testKeySetSpliteratorForEachRemaining() {
      var map = new LinkedHashMap<String, Integer>();
      map.put("x", 10);
      map.put("y", 20);
      map.put("z", 30);
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var list = new ArrayList<String>();
      spliterator.forEachRemaining(list::add);

      assertEquals(List.of("x", "y", "z"), list);
    }
  }


  @Nested
  public class Q8 {

    @Test
    public void testEntrySetParallelStream() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 1000).forEach(i -> map.put(i * 2, i));
      var orderedMap = OrderedMap.of(map);

      var sum = orderedMap.entrySet().parallelStream()
          .mapToInt(Map.Entry::getValue)
          .sum();

      assertEquals(499_500, sum);
    }

    @Test
    public void testEntrySetParallelStreamPreservesOrderWithForEachOrdered() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 100).forEach(i -> map.put(i, i));
      var orderedMap = OrderedMap.of(map);

      var keys = new CopyOnWriteArrayList<Integer>();
      orderedMap.entrySet().parallelStream()
          .forEachOrdered(e -> keys.add(e.getKey()));

      assertEquals(100, keys.size());
      for (var i = 0; i < 100; i++) {
        assertEquals(i, keys.get(i));
      }
    }

    @Test
    public void testEntrySetSpliteratorTryAdvanceAfterTrySplit() {
      var map = new LinkedHashMap<Integer, String>();
      for (var i = 0; i < 10; i++) {
        map.put(i, "" + i);
      }
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var split = spliterator.trySplit();

      var leftCount = 0;
      while (split.tryAdvance(_ -> {
      })) {
        leftCount++;
      }

      var rightCount = 0;
      while (spliterator.tryAdvance(_ -> {
      })) {
        rightCount++;
      }

      assertEquals(5, leftCount);
      assertEquals(5, rightCount);
    }

    @Test
    public void testEntrySetSpliteratorTrySplit() {
      var map = new LinkedHashMap<Integer, String>();
      map.put(1, "one");
      map.put(2, "two");
      map.put(3, "three");
      map.put(4, "four");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var split = spliterator.trySplit();

      assertNotNull(split);
      assertEquals(2, split.estimateSize());
      assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testEntrySetSpliteratorTrySplitPreservesOrder() {
      var map = new LinkedHashMap<Integer, String>();
      for (var i = 0; i < 8; i++) {
        map.put(i, "" + i);
      }
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var spliterator2 = spliterator.trySplit();

      var leftEntries = new ArrayList<Integer>();
      spliterator2.forEachRemaining(e -> leftEntries.add(e.getKey()));

      var rightEntries = new ArrayList<Integer>();
      spliterator.forEachRemaining(e -> rightEntries.add(e.getKey()));

      assertEquals(List.of(0, 1, 2, 3), leftEntries);
      assertEquals(List.of(4, 5, 6, 7), rightEntries);
    }

    @Test
    public void testEntrySetSpliteratorTrySplitOnEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testEntrySetSpliteratorTrySplitReturnsNullWhenTooSmall() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testEntrySetSpliteratorMultipleSplits() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 16).forEach(i -> map.put(i, i));
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.entrySet().spliterator();

      var split1 = spliterator.trySplit();
      var split2 = spliterator.trySplit();
      var split3 = split1.trySplit();

      assertNotNull(split1);
      assertNotNull(split2);
      assertNotNull(split3);
    }

    @Test
    public void testKeySetParallelStream() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 1000).forEach(i -> map.put(i, i * 2));
      var orderedMap = OrderedMap.of(map);

      var sum = orderedMap.keySet().parallelStream()
          .mapToInt(v -> v)
          .sum();

      assertEquals(499_500, sum);
    }

    @Test
    public void testKeySetParallelStreamPreservesOrderWithForEachOrdered() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 100).forEach(i -> map.put(i, i));
      var orderedMap = OrderedMap.of(map);

      var keys = new CopyOnWriteArrayList<Integer>();
      orderedMap.keySet().parallelStream()
          .forEachOrdered(keys::add);

      assertEquals(100, keys.size());
      for (var i = 0; i < 100; i++) {
        assertEquals(i, keys.get(i));
      }
    }

    @Test
    public void testKeySetSpliteratorTryAdvanceAfterTrySplit() {
      var map = new LinkedHashMap<Integer, String>();
      for (var i = 0; i < 10; i++) {
        map.put(i, "" + i);
      }
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var split = spliterator.trySplit();

      var leftCount = 0;
      while (split.tryAdvance(_ -> {})) {
        leftCount++;
      }

      var rightCount = 0;
      while (spliterator.tryAdvance(_ -> {})) {
        rightCount++;
      }

      assertEquals(5, leftCount);
      assertEquals(5, rightCount);
    }

    @Test
    public void testKeySetSpliteratorTrySplit() {
      var map = new LinkedHashMap<Integer, String>();
      map.put(1, "one");
      map.put(2, "two");
      map.put(3, "three");
      map.put(4, "four");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var split = spliterator.trySplit();

      assertNotNull(split);
      assertEquals(2, split.estimateSize());
      assertEquals(2, spliterator.estimateSize());
    }

    @Test
    public void testKeySetSpliteratorTrySplitPreservesOrder() {
      var map = new LinkedHashMap<Integer, String>();
      for (var i = 0; i < 8; i++) {
        map.put(i, "" + i);
      }
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var spliterator2 = spliterator.trySplit();

      var leftEntries = new ArrayList<Integer>();
      spliterator2.forEachRemaining(leftEntries::add);

      var rightEntries = new ArrayList<Integer>();
      spliterator.forEachRemaining(rightEntries::add);

      assertEquals(List.of(0, 1, 2, 3), leftEntries);
      assertEquals(List.of(4, 5, 6, 7), rightEntries);
    }

    @Test
    public void testKeySetSpliteratorTrySplitOnEmpty() {
      var map = Map.of();
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testKeySetSpliteratorTrySplitReturnsNullWhenTooSmall() {
      var map = Map.of("key", "value");
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void testKeySetSpliteratorMultipleSplits() {
      var map = new LinkedHashMap<Integer, Integer>();
      IntStream.range(0, 16).forEach(i -> map.put(i, i));
      var orderedMap = OrderedMap.of(map);
      var spliterator = orderedMap.keySet().spliterator();

      var split1 = spliterator.trySplit();
      var split2 = spliterator.trySplit();
      var split3 = split1.trySplit();

      assertNotNull(split1);
      assertNotNull(split2);
      assertNotNull(split3);
    }
  }
   */
}