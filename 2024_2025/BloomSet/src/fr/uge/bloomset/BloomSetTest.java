package fr.uge.bloomset;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class BloomSetTest {

  @Nested
  public class Q1 {
    @Test
    public void addFirstStringElement() {
      var bloomSet = new BloomSet<String>();
      var result = bloomSet.add("test");

      assertTrue(result);
      assertEquals(1, bloomSet.size());
    }

    @Test
    public void addFirstIntegerElement() {
      var bloomSet = new BloomSet<Integer>();
      var result = bloomSet.add(1351);

      assertTrue(result);
      assertEquals(1, bloomSet.size());
    }

    @Test
    public void addDuplicateElement() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("test");
      var result = bloomSet.add("test");

      assertFalse(result);
      assertEquals(1, bloomSet.size());
    }

    @Test
    public void sizeWithSeveralElements() {
      var bloomSet = new BloomSet<String>();
      assertEquals(0, bloomSet.size());

      bloomSet.add("one");
      assertEquals(1, bloomSet.size());

      bloomSet.add("two");
      assertEquals(2, bloomSet.size());

      bloomSet.add("one"); // Duplicate
      assertEquals(2, bloomSet.size());
    }

    @Test
    public void addMultipleStringElements() {
      var bloomSet = new BloomSet<String>();
      assertTrue(bloomSet.add("one"));
      assertTrue(bloomSet.add("two"));
      assertTrue(bloomSet.add("three"));

      assertEquals(3, bloomSet.size());
    }

    @Test
    public void addMultipleIntegerElements() {
      var bloomSet = new BloomSet<Integer>();
      assertTrue(bloomSet.add(1001));
      assertTrue(bloomSet.add(1002));
      assertTrue(bloomSet.add(1003));

      assertEquals(3, bloomSet.size());
    }

    @Test
    public void addWithCustomObjects() {
      record Person(String name, int age) { }

      var bloomSet = new BloomSet<Person>();
      var alice1 = new Person("Alice", 30);
      var bob = new Person("Bob", 25);
      var alice2 = new Person("Alice", 30); // Same as alice1

      assertTrue(bloomSet.add(alice1));
      assertTrue(bloomSet.add(bob));
      assertFalse(bloomSet.add(alice2));

      assertEquals(2, bloomSet.size());
    }

    @Test
    public void addWithDifferentElementWithTheSameHash() {
      record WrongHash(int value, int hash) {
        @Override
        public int hashCode() {
          return hash;
        }

        @Override
        public boolean equals(Object o) {
          return o instanceof WrongHash wrongHashElement && value == wrongHashElement.value;
        }
      }

      var bloomSet = new BloomSet<WrongHash>();
      for (var i = 0; i < 8; i++) {
        bloomSet.add(new WrongHash(i, 42));
      }
      assertEquals(8, bloomSet.size());
    }

    @Test
    public void addNullElement() {
      var bloomSet = new BloomSet<>();
      assertThrows(NullPointerException.class, () -> bloomSet.add(null));
    }

    @Test
    public void qualityOfImplementation() {
      assertAll(
          () -> assertTrue(BloomSet.class.accessFlags().contains(AccessFlag.PUBLIC)),
          () -> assertTrue(BloomSet.class.accessFlags().contains(AccessFlag.FINAL)),
          () -> assertEquals(1, BloomSet.class.getConstructors().length),
          () -> assertTrue(Arrays.stream(BloomSet.class.getDeclaredFields())
              .allMatch(f -> f.accessFlags().contains(AccessFlag.PRIVATE)))
      );
    }

    @Test
    public void bloomSetMustBeCompact() {
      var instanceFields = Arrays.stream(BloomSet.class.getDeclaredFields())
          .filter(f -> !f.accessFlags().contains(AccessFlag.STATIC))
          .toList();
      assertTrue(instanceFields.size() <= 3);
    }
  }

  @Nested
  public class Q2 {
    @Test
    public void containsMultipleStringElements() {
      var bloomSet = new BloomSet<String>();
      assertTrue(bloomSet.add("one"));
      assertTrue(bloomSet.add("two"));
      assertTrue(bloomSet.add("three"));

      assertEquals(3, bloomSet.size());
      assertTrue(bloomSet.contains("one"));
      assertTrue(bloomSet.contains("two"));
      assertTrue(bloomSet.contains("three"));
    }

    @Test
    public void containsMultipleIntegerElements() {
      var bloomSet = new BloomSet<Integer>();
      assertTrue(bloomSet.add(1001));
      assertTrue(bloomSet.add(1002));
      assertTrue(bloomSet.add(1003));

      assertEquals(3, bloomSet.size());
      assertTrue(bloomSet.contains(1001));
      assertTrue(bloomSet.contains(1002));
      assertTrue(bloomSet.contains(1003));
    }

    @Test
    public void containsWithCustomObjects() {
      record Person(String name, int age) { }

      var bloomSet = new BloomSet<Person>();
      var alice1 = new Person("Alice", 30);
      var bob = new Person("Bob", 25);
      var alice2 = new Person("Alice", 30);

      bloomSet.add(alice1);
      bloomSet.add(bob);
      assertTrue(bloomSet.contains(alice2));
    }

    @Test
    public void containsFull() {
      var bloomSet = new BloomSet<Integer>();
      for(var i = 0; i < 8; i++) {
        bloomSet.add(i);
      }

      assertTrue(bloomSet.contains(7));
      assertFalse(bloomSet.contains(-128));
    }

    @Test
    public void containsAddedElements() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("test");

      assertTrue(bloomSet.contains("test"));
    }

    @Test
    public void cContainsNonAddedElements() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("test");

      assertFalse(bloomSet.contains("other"));
    }

    @Test
    public void containsNotElement() {
      var bloomSet = new BloomSet<Integer>();
      for(var i = 0; i < 5; i++) {
        bloomSet.add(i);
      }

      assertTrue(bloomSet.contains(1));
      assertTrue(bloomSet.contains(4));
      assertFalse(bloomSet.contains(-1));
      assertFalse(bloomSet.contains(5));
      assertFalse(bloomSet.contains("diablo"));
    }

    @Test
    public void testContainsNull() {
      var bloomSet = new BloomSet<>();
      assertThrows(NullPointerException.class, () -> bloomSet.contains(null));
    }
  }

  @Nested
  public class Q3 {

    record HashElement(String value, int hashCodeValue, Runnable equalsCalled) {
      @Override
      public int hashCode() {
        return hashCodeValue;
      }

      @Override
      public boolean equals(Object obj) {
        equalsCalled.run();
        if (obj instanceof HashElement hashElement) {
          hashElement.equalsCalled.run();
          return value.equals(hashElement.value);
        }
        return false;
      }

      @Override
      public String toString() {
        return value + " (hash: " + hashCodeValue + ")";
      }
    }

    @Test
    public void addWithHashThatDoesNotOverlap() {
      var bloomSet = new BloomSet<HashElement>();

      bloomSet.add(new HashElement("element", 1 << 7, () -> {}));
      bloomSet.add(new HashElement("other", 1 << 12, Assertions::fail));
    }

    @Test
    public void addAndContainsWithZeroHashCodeElement() {
      record ZeroHashElement(String id) {
        @Override
        public int hashCode() {
          return 0;
        }
      }
      var bloomSet = new BloomSet<ZeroHashElement>();
      var element1 = new ZeroHashElement("a");
      var element2 = new ZeroHashElement("b");

      assertTrue(bloomSet.add(element1));
      assertEquals(1, bloomSet.size());

      assertTrue(bloomSet.add(element2));
      assertEquals(2, bloomSet.size());
    }

    @Test
    public void bloomHashAccumulatesBitsSameValue() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var add = bloomSet.add(new HashElement("element2", 0x00F0, () -> box.equalsCalled = true));
      assertFalse(add);
      assertTrue(box.equalsCalled);
    }

    @Test
    public void bloomHashAccumulatesBitsNotSameValueButSameHash() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var add = bloomSet.add(new HashElement("hello", 0x00F0, () -> box.equalsCalled = true));
      assertTrue(add);
      assertTrue(box.equalsCalled);
    }

    @Test
    public void bloomHashAccumulatesBitsNotSameValueHashOutside() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var add = bloomSet.add(new HashElement("hello", 1 << 16, () -> box.equalsCalled = true));
      assertTrue(add);
      assertFalse(box.equalsCalled);
    }
  }

  @Nested
  public class Q4 {

    record HashElement(String value, int hashCodeValue, Runnable equalsCalled) {
      @Override
      public int hashCode() {
        return hashCodeValue;
      }

      @Override
      public boolean equals(Object obj) {
        equalsCalled.run();
        return obj instanceof HashElement hashElement && value.equals(hashElement.value);
      }

      @Override
      public String toString() {
        return value + " (hash: " + hashCodeValue + ")";
      }
    }

    @Test
    public void containsWithHashThatDoesNotOverlap() {
      var bloomSet = new BloomSet<HashElement>();
      for (var i = 0; i < 8; i++) {
        bloomSet.add(new HashElement("element" + i, 1 << i, () -> {}));
      }

      var otherElement = new HashElement("other", 1 << 20, Assertions::fail);
      assertFalse(bloomSet.contains(otherElement));
    }

    @Test
    public void addAndContainsWithZeroHashCodeElement() {
      record ZeroHashElement(String id) {
        @Override
        public int hashCode() {
          return 0;
        }
      }
      var bloomSet = new BloomSet<ZeroHashElement>();
      var element1 = new ZeroHashElement("a");
      var element2 = new ZeroHashElement("b");

      bloomSet.add(element1);
      assertEquals(1, bloomSet.size());
      assertTrue(bloomSet.contains(element1));
      assertFalse(bloomSet.contains(element2));

      bloomSet.add(element2);
      assertEquals(2, bloomSet.size());
      assertTrue(bloomSet.contains(element2));
    }

    @Test
    public void bloomHashAccumulatesBitsSameValue() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var contains = bloomSet.contains(new HashElement("element2", 0x00F0, () -> box.equalsCalled = true));
      assertTrue(contains);
      assertTrue(box.equalsCalled);
    }

    @Test
    public void bloomHashAccumulatesBitsNotSameValueButSameHash() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var contains = bloomSet.contains(new HashElement("hello", 0x00F0, () -> box.equalsCalled = true));
      assertFalse(contains);
      assertTrue(box.equalsCalled);
    }

    @Test
    public void bloomHashAccumulatesBitsNotSameValueHashOutside() {
      var bloomSet = new BloomSet<HashElement>();
      bloomSet.add(new HashElement("element1", 0b0000_0000_0000_1111, () -> {}));
      bloomSet.add(new HashElement("element2", 0b0000_0000_1111_0000, () -> {}));
      bloomSet.add(new HashElement("element3", 0b0000_1111_0000_0000, () -> {}));
      bloomSet.add(new HashElement("element4", 0b1111_0000_0000_0000, () -> {}));

      var box = new Object() { boolean equalsCalled; };
      var contains = bloomSet.contains(new HashElement("hello", 1 << 16, () -> box.equalsCalled = true));
      assertFalse(contains);
      assertFalse(box.equalsCalled);
    }

    @Test
    public void bloomHashContainsFullHashCollision() {
      var bloomSet = new BloomSet<HashElement>();
      for(var i = 0; i < 8; i++) {
        bloomSet.add(new HashElement("element" + i, 1 << i, () -> {}));
      }

      var element7 = new HashElement("element7", 1 << 7, () -> {});
      assertTrue(bloomSet.contains(element7));

      var other = new HashElement("other", 1 << 7, () -> {});
      assertFalse(bloomSet.contains(other));
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void addStringElementTriggeringExpansion() {
      var bloomSet = new BloomSet<String>();
      for (var i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }

      assertTrue(bloomSet.add("item8"));
      assertEquals(8 + 1, bloomSet.size());
      assertTrue(bloomSet.contains("item8"));

      for (var i = 0; i < 8; i++) {
        assertTrue(bloomSet.contains("item" + i));
      }
    }

    @Test
    public void addDuplicateWhenArrayIsFullDoesNotExpand() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      assertEquals(8, bloomSet.size());

      var result = bloomSet.add("item6");
      assertFalse(result);
      assertEquals(8, bloomSet.size());
    }

    @Test
    public void addMoreIntegerElementsAfterExpansion() {
      var bloomSet = new BloomSet<Integer>();
      for (var i = 0; i < 8 + 5; i++) {
        bloomSet.add(i);
      }

      assertTrue(bloomSet.add(8 + 5));
      assertEquals(8 + 5 + 1, bloomSet.size());
      assertTrue(bloomSet.contains(8 + 5));

      // Verify older items are still present
      assertTrue(bloomSet.contains(0), "Should contain first item");
      assertTrue(bloomSet.contains(8), "Should contain item added during transition"); // CAPACITY-th item is index CAPACITY
    }

    @Test
    public void addDuplicateAfterExpansion() {
      var bloomSet = new BloomSet<String>();
      for (var i = 0; i < 8 + 3; i++) {
        bloomSet.add("key" + i);
      }

      assertFalse(bloomSet.add("key0"));
      assertFalse(bloomSet.add("key8"));
      assertEquals(8 + 3, bloomSet.size());
    }

    @Test
    public void containsAfterExpansion() {
      var bloomSet = new BloomSet<String>();
      for (var i = 0; i < 8 + 10; i++) {
        bloomSet.add("present" + i);
      }

      for (var i = 0; i < 8 + 10; i++) {
        assertTrue(bloomSet.contains("present" + i));
      }

      assertFalse(bloomSet.contains("not_present"));
      assertFalse(bloomSet.contains("present18"));
      assertFalse(bloomSet.contains(101));
    }

    @Test
    public void sizeUpdatesCorrectlyAcrossExpansion() {
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        var bloomSet = new BloomSet<Integer>();
        for (var i = 0; i < 1_000_000; i++) {
          bloomSet.add(i);
          assertEquals(i + 1, bloomSet.size());
        }
        assertEquals(1_000_000, bloomSet.size());
      });
    }

    @Test
    public void containsWorksCorrectlyAcrossExpansion() {
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        var bloomSet = new BloomSet<Integer>();
        for (var i = 0; i < 1_000_000; i++) {
          bloomSet.add(i);
          assertTrue(bloomSet.contains(i));
        }
      });
    }

    @Test
    public void addNullThrowsNPEAfterExpansion() {
      var bloomSet = new BloomSet<Integer>();
      for (var i = 0; i <= 8; i++) {
        bloomSet.add(i);
      }

      assertThrows(NullPointerException.class, () -> bloomSet.add(null));
    }

    @Test
    public void containsNullThrowsNPEAfterExpansion() {
      var bloomSet = new BloomSet<Integer>();
      for (var i = 0; i <= 8; i++) {
        bloomSet.add(i);
      }

      assertThrows(NullPointerException.class, () -> bloomSet.contains(null));
    }
  }

  @Nested
  public class Q6 {
    @Test
    public void newBloomSetOfStringIsASet() {
      Set<String> bloomSet = new BloomSet<String>();
      assertNotNull(bloomSet);
    }

    @Test
    public void newBloomSetOfIntegerIsASet() {
      Set<Integer> bloomSet = new BloomSet<Integer>();
      assertNotNull(bloomSet);
    }

    @Test
    public void newBloomSetIsEmpty() {
      var bloomSet = new BloomSet<>();
      assertTrue(bloomSet.isEmpty());
    }

    @Test
    public void newBloomSetToString() {
      var bloomSet = new BloomSet<String>();
      assertEquals("[]", bloomSet.toString());

      bloomSet.add("test");
      assertEquals("[test]", bloomSet.toString());

      bloomSet.add("another");
      var string = bloomSet.toString();
      assertTrue("[test, another]".equals(string) || "[another, test]".equals(string));
    }

    @Test
    public void toStringAfterExpansion() {
      var bloomSet = new BloomSet<Integer>();
      for (int i = 0; i < 8 + 1; i++) {
        bloomSet.add(i);
      }
      var string = bloomSet.toString();

      assertTrue(string.startsWith("[") && string.endsWith("]"));
      for (int i = 0; i < 9; i++) {
        assertTrue(string.contains("" + i));
      }
    }

    @Test
    public void newBloomSetEquals() {
      var bloomSet1 = new BloomSet<String>();
      bloomSet1.add("one");
      bloomSet1.add("two");

      var bloomSet2 = new BloomSet<String>();
      bloomSet2.add("two");
      bloomSet2.add("one");

      assertEquals(Set.of("one", "two"), bloomSet1);
      assertEquals(Set.of("one", "two"), bloomSet2);
    }

    @Test
    public void newBloomSetHashCode() {
      var bloomSet1 = new BloomSet<String>();
      bloomSet1.add("one");
      bloomSet1.add("two");

      var bloomSet2 = new BloomSet<String>();
      bloomSet2.add("two");
      bloomSet2.add("one");

      assertEquals(Set.of("one", "two").hashCode(), bloomSet1.hashCode());
      assertEquals(Set.of("one", "two").hashCode(), bloomSet2.hashCode());
    }

    @Test
    public void hashCodeAfterExpansion() {
      var bloomSet = new BloomSet<Integer>();
      var referenceSet = new LinkedHashSet<Integer>();
      for (int i = 0; i < 8 + 2; i++) {
        bloomSet.add(i);
        referenceSet.add(i);
      }
      assertEquals(referenceSet.hashCode(), bloomSet.hashCode());
    }

    @Test
    public void newBloomSetAddAll() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("one");

      var toAdd = new ArrayList<String>();
      toAdd.add("two");
      toAdd.add("three");

      assertTrue(bloomSet.addAll(toAdd));
      assertEquals(3, bloomSet.size());
      assertTrue(bloomSet.contains("one"));
      assertTrue(bloomSet.contains("two"));
      assertTrue(bloomSet.contains("three"));
    }

    @Test
    public void newBloomSetContainsAll() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("one");
      bloomSet.add("two");
      bloomSet.add("three");

      assertTrue(bloomSet.containsAll(Set.of("one", "three")));
      assertFalse(bloomSet.containsAll(Set.of("one", "three", "four")));
    }

    @Test
    public void newBloomSetForLoop() {
      var bloomSet = new BloomSet<Integer>();
      for(var i = 0; i < 8; i++) {
        bloomSet.add(i);
      }

      var sum = 0;
      for(var element : bloomSet) {
        sum += element;
      }
      assertEquals(28, sum);
    }

    @Test
    public void newBloomSetLargeSetForLoop() {
      var bloomSet = new BloomSet<Integer>();
      for(var i = 0; i < 10_000; i++) {
        bloomSet.add(i);
      }

      var sum = 0;
      for(var element : bloomSet) {
        sum += element;
      }
      assertEquals(49_995_000, sum);
    }

    @Test
    public void newBloomSetIteratorOrderIfLessThan8() {
      var bloomSet = new BloomSet<String>();
      var elements = List.of("one", "two", "three");
      for (var element : elements) {
        bloomSet.add(element);
      }

      var iterator = bloomSet.iterator();
      for (var element : elements) {
        assertTrue(iterator.hasNext());
        assertEquals(element, iterator.next());
      }

      assertFalse(iterator.hasNext());
    }

    @Test
    public void newBloomSetToArrayOrderIfLessThan8() {
      var bloomSet = new BloomSet<String>();
      var elements = List.of("one", "two", "three");
      for (var element : elements) {
        bloomSet.add(element);
      }

      var array = bloomSet.toArray();
      assertArrayEquals(new Object[] { "one", "two", "three" }, array);
    }

    @Test
    public void newBloomSetToArrayStringOrderIfLessThan8() {
      var bloomSet = new BloomSet<String>();
      var elements = List.of("one", "two", "three");
      for (var element : elements) {
        bloomSet.add(element);
      }

      var array = bloomSet.toArray(new String[0]);
      assertArrayEquals(new Object[] { "one", "two", "three" }, array);
    }

    @Test
    public void newBloomSetToArrayFunctionOrderIfLessThan8() {
      var bloomSet = new BloomSet<String>();
      var elements = List.of("one", "two", "three");
      for (var element : elements) {
        bloomSet.add(element);
      }

      var array = bloomSet.toArray(String[]::new);
      assertArrayEquals(new Object[] { "one", "two", "three" }, array);
    }

    @Test
    public void newBloomSetIteratorNoMoreElements() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("test");

      var iterator = bloomSet.iterator();
      iterator.next(); // Get the first element

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void newBloomSetIteratorEmptySet() {
      var bloomSet = new BloomSet<String>();
      var iterator = bloomSet.iterator();

      assertFalse(iterator.hasNext());
      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void newBloomSetIteratorRemoveUnsupported() {
      var bloomSet = new BloomSet<Integer>();
      bloomSet.add(101);

      var iterator = bloomSet.iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void hashSetBackedIterator() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      // Collect all elements from the iterator
      var set = new LinkedHashSet<String>();
      var iterator = bloomSet.iterator();
      while (iterator.hasNext()) {
        set.add(iterator.next());
      }

      assertEquals(
          Set.of("item0", "item1", "item2", "item3", "item4", "item5", "item6", "item7", "extra item"),
          set);
    }

    @Test
    public void hashSetBackedIteratorRemoveUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      var iterator = bloomSet.iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    public void newBloomSetRemoveUnsupported() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("hello");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.remove("hello"));
    }

    @Test
    public void hashSetBackedRemoveUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.remove("item3"));
    }

    @Test
    public void newBloomSetRemoveAllUnsupported() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("hello");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.removeAll(List.of("hello")));
    }

    @Test
    public void hashSetBackedRemoveAllUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.removeAll(List.of("item3")));
    }

    @Test
    public void newBloomSetRemoveIfUnsupported() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("hello");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.removeIf("hello"::equals));
    }

    @Test
    public void hashSetBackedRemoveIfUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.removeIf("item3"::equals));
    }

    @Test
    public void newBloomSetClearUnsupported() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("hello");

      assertThrows(UnsupportedOperationException.class, bloomSet::clear);
    }

    @Test
    public void hashSetBackedClearUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      assertThrows(UnsupportedOperationException.class, bloomSet::clear);
    }

    @Test
    public void newBloomSetRetainAlUnsupported() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("Bob");
      bloomSet.add("Ana");

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.retainAll(List.of("Ana")));
    }

    @Test
    public void hashSetBackedIteratorRetainAllUnsupported() {
      var bloomSet = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        bloomSet.add("item" + i);
      }
      bloomSet.add("extra item");

      var iterator = bloomSet.iterator();
      iterator.next();

      assertThrows(UnsupportedOperationException.class, () -> bloomSet.retainAll(List.of("item3")));
    }
  }

  @Nested
  public class Q7 {
    @Test
    public void isEmpty() {
      var bloomSet = new BloomSet<>();

      assertTrue(bloomSet.isEmpty());
    }

    @Test
    public void isNotEmpty() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("hello");

      assertFalse(bloomSet.isEmpty());
    }
    
    @Test
    public void isNotEmptyWithAnEmptyString() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("");

      assertFalse(bloomSet.isEmpty());
    }

    @Test
    public void isNotEmptyWithAnEmptyStringBonus() {

      var bloomSet = new BloomSet<>();
      for(var i = 0; i < 10; i++) {
        var zeroHashElement = new Object() {

          @Override
          public int hashCode() {
            return 0;
          }

        };
        bloomSet.add(zeroHashElement);
      }
      assertFalse(bloomSet.isEmpty());
    }

    @Test
    public void hashSetBackedIsNotEmpty() {
      var bloomSet = new BloomSet<Integer>();
      for(var i = 0; i < 8 + 1; i++) {
        bloomSet.add(i);
      }

      assertFalse(bloomSet.isEmpty());
    }
  }

  @Nested
  public class Q8 {
    @Test
    public void equalsWithBloomSet() {
      var bloomSet1 = new BloomSet<String>();
      bloomSet1.add("one");
      bloomSet1.add("two");

      var bloomSet2 = new BloomSet<String>();
      bloomSet2.add("two");
      bloomSet2.add("one");

      assertTrue(bloomSet1.equals(bloomSet2));
      assertTrue(bloomSet2.equals(bloomSet1));
    }

    @Test
    public void notEqualsWithBloomSet() {
      var bloomSet1 = new BloomSet<String>();
      bloomSet1.add("one");
      bloomSet1.add("two");

      var bloomSet2 = new BloomSet<Integer>();
      for (var i = 0; i < 9; i++) {
        bloomSet2.add(i);
      }

      assertFalse(bloomSet1.equals(bloomSet2));
      assertFalse(bloomSet2.equals(bloomSet1));
    }

    @Test
    public void equalityWithRegularSet() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("one");
      bloomSet.add("two");

      assertEquals(bloomSet, Set.of("one", "two"));
    }

    @Test
    public void testEqualityWithString() {
      var bloomSet = new BloomSet<String>();
      bloomSet.add("one");

      assertFalse(bloomSet.equals("hello"));
    }

    @Test
    public void equalityWithNull() {
      var bloomSet = new BloomSet<>();
      bloomSet.add("one");

      assertFalse(bloomSet.equals(null));
    }

    @Test
    public void equalityAfterExpansion() {
      var bloomSet1 = new BloomSet<Integer>();
      for (var i = 0; i < 8 + 1; i++) {
        bloomSet1.add(1 << i);
      }

      var bloomSet2 = new BloomSet<Integer>();
      for (var i = 8; i >= 0; i--) {
        bloomSet2.add(1 << i);
      }

      assertTrue(bloomSet1.equals(bloomSet2));
      assertTrue(bloomSet2.equals(bloomSet1));
    }

    @Test
    public void equalityWithElementWithNotTheSameHash() {
      record WrongHash(int value, int hash) {
        @Override
        public int hashCode() {
          return hash;
        }

        @Override
        public boolean equals(Object o) {
          return o instanceof WrongHash wrongHashElement && value == wrongHashElement.value;
        }
      }

      var bloomSet1 = new BloomSet<WrongHash>();
      for (var i = 0; i < 8; i++) {
        bloomSet1.add(new WrongHash(i, 42));
      }

      var bloomSet2 = new BloomSet<WrongHash>();
      for (var i = 0; i < 8; i++) {
        bloomSet2.add(new WrongHash(i, 101));
      }

      assertFalse(bloomSet1.equals(bloomSet2));
      assertFalse(bloomSet2.equals(bloomSet1));
    }

    @Test
    public void equalityAfterExpansionWithElementWithNotTheSameHash() {
      record WrongHash(int value, int hash) {
        @Override
        public int hashCode() {
          return hash;
        }

        @Override
        public boolean equals(Object o) {
          return o instanceof WrongHash wrongHashElement && value == wrongHashElement.value;
        }
      }

      var bloomSet1 = new BloomSet<WrongHash>();
      for (var i = 0; i < 8 + 1; i++) {
        bloomSet1.add(new WrongHash(i, 42));
      }

      var bloomSet2 = new BloomSet<WrongHash>();
      for (var i = 0; i < 8 + 1; i++) {
        bloomSet2.add(new WrongHash(i, 101));
      }

      assertFalse(bloomSet1.equals(bloomSet2));
      assertFalse(bloomSet2.equals(bloomSet1));
    }

    @Test
    public void equalityNoSideEffect() {
      record CalledElement(int value, Runnable equalsOrHashCodeCalled) {
        @Override
        public int hashCode() {
          equalsOrHashCodeCalled.run();
          return value;
        }

        @Override
        public boolean equals(Object obj) {
          equalsOrHashCodeCalled.run();
          return obj instanceof CalledElement calledElement && value == calledElement.value;
        }
      }

      var equalsOrHashCodeCalled = new Runnable() {
        private boolean armed;

        @Override
        public void run() {
          if (armed) {
            fail();
          }
        }
      };

      var bloomSet1 = new BloomSet<CalledElement>();
      bloomSet1.add(new CalledElement(1, equalsOrHashCodeCalled));
      bloomSet1.add(new CalledElement(2, equalsOrHashCodeCalled));

      var bloomSet2 = new BloomSet<CalledElement>();
      for (var i = 0; i < 8 + 1; i++) {
        bloomSet2.add(new CalledElement(i, equalsOrHashCodeCalled));
      }

      equalsOrHashCodeCalled.armed = true;

      assertFalse(bloomSet1.equals(bloomSet2));
      assertFalse(bloomSet2.equals(bloomSet1));
    }
  }

  @Nested
  public class Q9 {

    @Test
    public void emptySetShouldCreateEmptyStream() {
      var set = new BloomSet<String>();

      assertEquals(0, set.stream().count());
      assertEquals(0, set.parallelStream().count());
    }

    @Test
    public void sequentialStreamShouldProcessAllElements() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var collected = set.stream().toList();

      assertEquals(3, collected.size());
      assertTrue(collected.contains("one"));
      assertTrue(collected.contains("two"));
      assertTrue(collected.contains("three"));
    }

    @Test
    public void parallelStreamShouldProcessAllElements() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var collected = set.parallelStream().toList();

      assertEquals(3, collected.size());
      assertTrue(collected.contains("one"));
      assertTrue(collected.contains("two"));
      assertTrue(collected.contains("three"));
    }

    @Test
    public void streamAfterExpansionShouldWorkCorrectly() {
      var set = new BloomSet<String>();
      for (int i = 0; i < 8 + 2; i++) {
        set.add("element" + i);
      }

      var collected = set.stream().toList();

      assertEquals(8 + 2, collected.size());
      for (int i = 0; i < 8 + 2; i++) {
        assertTrue(collected.contains("element" + i));
      }
    }

    @Test
    public void streamOperationsShouldWorkWithTransformations() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var lengths = set.stream()
          .map(String::length)
          .toList();

      assertEquals(3, lengths.size());
      assertTrue(lengths.contains(3));
      assertTrue(lengths.contains(5));
    }

    @Test
    public void streamOperationsOnSmallSetShouldOptimizeCount() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var size = set.stream()
          .map(_ -> fail())
          .count();

      assertEquals(set.size(), size);
    }

    @Test
    public void streamOperationsOnLargeSetShouldOptimizeCount() {
      var set = new BloomSet<String>();
      for(var i = 0; i < 8 + 2; i++) {
        set.add("item" + i);
      }

      var size = set.stream()
          .map(_ -> fail())
          .count();

      assertEquals(set.size(), size);
    }

    @Test
    public void spliteratorCharacteristicsForSmallSetShouldIncludeNonNull() {
      var set = new BloomSet<String>();
      set.add("test");
      var spliterator = set.spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    public void spliteratorCharacteristicsAfterExpansion() {
      var set = new BloomSet<String>();
      for (var i = 0; i < 8 + 2; i++) {
        set.add("item" + i);
      }
      var spliterator = set.spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
    }

    @Test
    public void spliteratorCharacteristicsForSmallSetShouldIncludeDistinct() {
      var set = new BloomSet<String>();
      set.add("test");
      var spliterator = set.spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT));
    }

    @Test
    public void spliteratorCharacteristicsAfterExpansionShouldIncludeDistinct() {
      var set = new BloomSet<String>();
      for (var i = 0; i < 8 + 2; i++) {
        set.add("item" + i);
      }
      var spliterator = set.spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.DISTINCT));
    }

    @Test
    public void spliteratorCharacteristicsForSmallSetIsOrdered() {
      var set = new BloomSet<String>();
      set.add("test");
      var spliterator = set.spliterator();

      assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void spliteratorCharacteristicsAfterExpansionIsNotOrdered() {
      var set = new BloomSet<String>();
      for (var i = 0; i < 8 + 2; i++) {
        set.add("item" + i);
      }
      var spliterator = set.spliterator();

      assertFalse(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void spliteratorTryAdvanceShouldProcessAllElements() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var spliterator = set.spliterator();
      var elements = new LinkedHashSet<String>();
      while (spliterator.tryAdvance(elements::add)) {
        // keep advancing
      }

      assertEquals(3, elements.size());
      assertTrue(elements.contains("one"));
      assertTrue(elements.contains("two"));
      assertTrue(elements.contains("three"));
    }

    @Test
    public void spliteratorForEachRemaningShouldProcessAllElements() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var spliterator = set.spliterator();
      var elements = new LinkedHashSet<String>();
      spliterator.forEachRemaining(elements::add);

      assertEquals(3, elements.size());
      assertTrue(elements.contains("one"));
      assertTrue(elements.contains("two"));
      assertTrue(elements.contains("three"));
    }

    @Test
    public void spliteratorSmallSetEstimateSizeShouldMatchActualSize() {
      var set = new BloomSet<String>();
      set.add("one");
      set.add("two");
      set.add("three");

      var spliterator = set.spliterator();

      assertEquals(set.size(), spliterator.estimateSize());
    }

    @Test
    public void spliteratorLargeSetEstimateSizeShouldMatchActualSize() {
      var set = new BloomSet<Integer>();
      for(var i = 0; i < 8 + 2; i++) {
        set.add(i);
      }

      var spliterator = set.spliterator();

      assertEquals(set.size(), spliterator.estimateSize());
    }

    @Test
    public void spliteratorTrySplitWithSmallSetShouldReturnNull() {
      var set = new BloomSet<String>();
      set.add("test");

      var spliterator = set.spliterator();
      var split = spliterator.trySplit();

      assertNull(split);
    }

    @Test
    public void spliteratorShouldSplitSmallSetCorrectly() {
      var set = new BloomSet<String>();
      for (int i = 0; i < 8; i++) {
        set.add("element" + i);
      }

      var spliterator = set.spliterator();
      var headSpliterator = spliterator.trySplit();

      var headSet = new LinkedHashSet<>();
      headSpliterator.forEachRemaining(headSet::add);
      var tailSet = new LinkedHashSet<>();
      spliterator.forEachRemaining(tailSet::add);

      assertEquals(4, headSet.size());
      assertEquals(4, tailSet.size());
      assertTrue(Collections.disjoint(headSet, tailSet));
    }

    @Test
    public void spliteratorShouldSplitExpandedSetCorrectly() {
      var set = new BloomSet<String>();
      for (int i = 0; i < 8 + 22; i++) {
        set.add("element" + i);
      }

      var spliterator = set.spliterator();
      var headSpliterator = spliterator.trySplit();

      var headSet = new LinkedHashSet<>();
      headSpliterator.forEachRemaining(headSet::add);
      var tailSet = new LinkedHashSet<>();
      spliterator.forEachRemaining(tailSet::add);

      assertEquals(8 + 22, headSet.size() + tailSet.size());
      assertTrue(Collections.disjoint(headSet, tailSet));
    }

    @Test
    public void sequentialSmallSetProcessingShouldVisitEachElementExactlyOnce() {
      var set = new BloomSet<Integer>();
      for (int i = 0; i < 8; i++) {
        set.add(i);
      }

      var counts = new int[8];

      set.stream().forEach(e -> counts[e]++);

      for (int i = 0; i < 8; i++) {
        assertEquals(1, counts[i]);
      }
    }

    @Test
    public void parallelSmallSetProcessingShouldVisitEachElementExactlyOnce() {
      var set = new BloomSet<Integer>();
      for (int i = 0; i < 8; i++) {
        set.add(i);
      }

      var counts = new AtomicInteger[8];
      Arrays.setAll(counts, _ -> new AtomicInteger(0));

      set.parallelStream().forEach(e -> counts[e].incrementAndGet());

      for (int i = 0; i < 8; i++) {
        assertEquals(1, counts[i].get());
      }
    }

    @Test
    public void sequentialLargeSetProcessingShouldVisitEachElementExactlyOnce() {
      var set = new BloomSet<Integer>();
      for (int i = 0; i < 1_000_000; i++) {
        set.add(i);
      }

      var counts = new int[1_000_000];

      set.stream().forEach(e -> counts[e]++);

      for (int i = 0; i < 1_000_000; i++) {
        assertEquals(1, counts[i]);
      }
    }

    @Test
    public void parallelLargeSetProcessingShouldVisitEachElementExactlyOnce() {
      var set = new BloomSet<Integer>();
      for (int i = 0; i < 1_000_000; i++) {
        set.add(i);
      }

      var counts = new AtomicInteger[1_000_000];
      Arrays.setAll(counts, _ -> new AtomicInteger(0));

      set.parallelStream().forEach(e -> counts[e].incrementAndGet());

      for (int i = 0; i < 1_000_000; i++) {
        assertEquals(1, counts[i].get());
      }
    }

    @Test
    public void parallelLargeSetProcessingUseSeveralThreads() {
      var set = new BloomSet<Integer>();
      for (int i = 0; i < 1_000_000; i++) {
        set.add(i);
      }

      var threads = new CopyOnWriteArraySet<Thread>();

      var sum = set.parallelStream()
          .peek(_ -> threads.add(Thread.currentThread()))
          .mapToInt(v -> v)
          .sum();

      assertTrue(threads.size() > 1);
      assertEquals(1_783_293_664L, sum);
    }
  }

}