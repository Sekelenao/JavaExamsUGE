package fr.uge.intset;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class IntSetTest {

  @Nested
  public class Q1 {

    @Test
    public void testInitiallyAllBitsAreFalse() {
      var intSet = new IntSet();
      for (var i = 0; i < 128; i++) {
        assertFalse(intSet.get(i));
      }
    }

    @Test
    public void testSetAndGetSingleBit() {
      var intSet = new IntSet();
      intSet.set(0);
      assertTrue(intSet.get(0));
      assertFalse(intSet.get(1));
    }

    @Test
    public void testSetAndGetBoundaryBits() {
      var intSet = new IntSet();
      intSet.set(31);
      intSet.set(32);
      intSet.set(127);

      assertTrue(intSet.get(31));
      assertTrue(intSet.get(32));
      assertTrue(intSet.get(127));
      assertFalse(intSet.get(30));
      assertFalse(intSet.get(33));
      assertFalse(intSet.get(126));
    }

    @Test
    public void testSetMultipleBitsInSameInt() {
      var intSet = new IntSet();
      intSet.set(5);
      intSet.set(10);
      intSet.set(5);

      assertTrue(intSet.get(5));
      assertTrue(intSet.get(10));
      assertFalse(intSet.get(6));
    }

    @Test
    public void testAllBitsInSet() {
      var intSet = new IntSet();
      for(var i = 0; i < 128; i++) {
        assertFalse(intSet.get(i));
        intSet.set(i);
        assertTrue(intSet.get(i));
      }
    }

    @Test
    public void testClassShouldBePublicFinal() {
      assertTrue(IntSet.class.accessFlags().contains(AccessFlag.PUBLIC));
      assertTrue(IntSet.class.accessFlags().contains(AccessFlag.FINAL));
    }

    @Test
    public void testClassShouldHaveAPublicConstructor() {
      assertEquals(1, IntSet.class.getConstructors().length);
    }

    @Test
    public void testClassShouldHaveOnlyOneInstanceFieldOfTypeArray() {
      var fields = Arrays.stream(IntSet.class.getDeclaredFields())
          .filter(field -> !field.accessFlags().contains(AccessFlag.STATIC))
          .toList();

      assertEquals(1, fields.size());
      assertTrue(fields.getFirst().getType().isArray());
    }

    @Test
    public void testGetAndSetShouldNotBePublic() {
      var methods = Arrays.stream(IntSet.class.getMethods())
          .map(Method::getName)
          .collect(Collectors.toSet());

      assertFalse(methods.contains("get"));
      assertFalse(methods.contains("set"));
    }

    @Test
    public void testClassConstructorsCallSuperAsLastInstruction() throws IOException {
      var className = "/" + IntSet.class.getName().replace('.', '/') + ".class";
      byte[] data;
      try (var input = IntSet.class.getResourceAsStream(className)) {
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
    public void testAddNewElement() {
      var set = new IntSet();
      var result = set.add(10);
      assertTrue(result);
      assertTrue(set.get(10));
    }

    @Test
    public void testAddExistingElement() {
      var set = new IntSet();
      set.set(10);
      var result = set.add(10);
      assertFalse(result);
    }

    @Test
    public void testAddTwice() {
      var set = new IntSet();
      set.add(10);
      var result = set.add(10);
      assertFalse(result);
    }

    @Test
    public void testAddNegativeElement() {
      var set = new IntSet();
      assertThrows(IllegalArgumentException.class, () -> set.add(-1));
    }

    @Test
    public void testAddBoundaryElements() {
      var set = new IntSet();
      var res1 = set.add(0);
      var res2 = set.add(31);
      var res3 = set.add(32);
      var res4 = set.add(127);

      assertTrue(res1);
      assertTrue(res2);
      assertTrue(res3);
      assertTrue(res4);

      assertTrue(set.get(0));
      assertTrue(set.get(31));
      assertTrue(set.get(32));
      assertTrue(set.get(127));
    }

    @Test
    public void testAddAllBitsInSet() {
      var intSet = new IntSet();
      for(var i = 0; i < 128; i++) {
        assertFalse(intSet.get(i));
        intSet.add(i);
        assertTrue(intSet.get(i));
      }
    }
  }

  @Nested
  public class Q3 {

    @Test
    public void testAddRequiresResizing() {
      var set = new IntSet();
      var added = set.add(1000);
      assertTrue(added);
      assertTrue(set.get(1000));
    }

    @Test
    public void testAddRequiresResizingAtBoundary() {
      var set = new IntSet();
      var added = set.add(128);
      assertTrue(added);
      assertTrue(set.get(128));
    }

    @Test
    public void testAddRequiresLargeResizing() {
      var set = new IntSet();
      var largeValue = 100_000;
      var added = set.add(largeValue);
      assertTrue(added);
      assertTrue(set.get(largeValue));
    }

    @Test
    public void testAddDuplicateAfterResizing() {
      var set = new IntSet();
      set.add(200);
      var addedAgain = set.add(200);
      assertFalse(addedAgain);
    }

    @Test
    public void testAddMultipleValuesTriggeringMultipleResizes() {
      var set = new IntSet();
      set.add(10);
      set.add(200);
      set.add(5_000);

      assertTrue(set.get(10));
      assertTrue(set.get(200));
      assertTrue(set.get(5_000));
      assertFalse(set.get(11));
    }

    @Test
    public void testAddAllBitsInSet() {
      var intSet = new IntSet();
      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          assertTrue(intSet.add(i));
          assertTrue(intSet.get(i));
        }
      });
    }
  }

  @Nested
  public class Q4 {

    @Test
    public void testContainsAddedElement() {
      var set = new IntSet();
      set.add(42);
      var result = set.contains(42);
      assertTrue(result);
    }

    @Test
    public void testContainsNonAddedElement() {
      var set = new IntSet();
      set.add(42);
      var result = set.contains(43);
      assertFalse(result);
    }

    @Test
    public void testContainsValueAtBoundary() {
      var set = new IntSet();
      var result = set.contains(128);
      assertFalse(result);
    }

    @Test
    public void testContainsMaxValue() {
      var set = new IntSet();
      var result = set.contains(Integer.MAX_VALUE);
      assertFalse(result);
    }

    @Test
    public void testContainsNegativeElement() {
      var set = new IntSet();
      assertThrows(IllegalArgumentException.class, () -> set.contains(-1));
    }

    @Test
    public void testContainsAfterResizing() {
      var set = new IntSet();
      var largeValue = 5_000;
      set.add(largeValue); // Triggers array resize

      assertTrue(set.contains(largeValue));
      assertFalse(set.contains(largeValue + 1));
    }

    @Test
    public void testContainsBoundaryValues() {
      var set = new IntSet();
      set.add(0);
      set.add(31);
      set.add(32);
      set.add(127);
      set.add(1024);

      assertTrue(set.contains(0));
      assertTrue(set.contains(31));
      assertTrue(set.contains(32));
      assertTrue(set.contains(127));
      assertTrue(set.contains(1024));

      assertFalse(set.contains(1));
      assertFalse(set.contains(30));
      assertFalse(set.contains(33));
      assertFalse(set.contains(128));
      assertFalse(set.contains(1023));
    }

    @Test
    public void testContainsAllBitsInSet() {
      var intSet = new IntSet();
      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          assertFalse(intSet.contains(i));
          assertTrue(intSet.add(i));
          assertTrue(intSet.contains(i));
        }
      });
    }
  }

  @Nested
  public class Q5 {

    @Test
    public void testStreamEmptySet() {
      var set = new IntSet();
      var count = set.stream().count();
      assertEquals(0L, count);
    }

    @Test
    public void testStreamSingleElement() {
      var set = new IntSet();
      set.add(42);
      var result = set.stream().boxed().toList();
      assertEquals(List.of(42), result);
    }

    @Test
    public void testStreamMultipleElements() {
      var set = new IntSet();
      set.add(100);
      set.add(120);
      set.add(10);
      set.add(50);

      var result = set.stream().boxed().toList();
      assertEquals(List.of(10, 50, 100, 120), result);
    }

    @Test
    public void testStreamFilterAndMap() {
      var set = new IntSet();
      set.add(100);
      set.add(1);
      set.add(10);
      set.add(50);

      var sum = set.stream().filter(i -> i % 2 == 0).map(i -> i *2).sum();
      assertEquals(320, sum);
    }

    @Test
    public void testStreamSort() {
      var set = new IntSet();
      set.add(1);
      set.add(7);
      set.add(3);
      set.add(2);
      set.add(4);

      var list = set.stream().sorted().boxed().toList();
      assertEquals(List.of(1, 2, 3, 4, 7), list);
    }

    @Test
    public void testStreamAcrossBucketBoundaries() {
      var set = new IntSet();
      set.add(0);
      set.add(31);
      set.add(32);
      set.add(63);
      set.add(64);

      var result = set.stream().toArray();
      assertArrayEquals(new int[]{0, 31, 32, 63, 64}, result);
    }

    @Test
    public void testStreamAfterResizing() {
      var set = new IntSet();
      set.add(10);
      set.add(20_000);

      var result = set.stream().toArray();
      assertArrayEquals(new int[]{10, 20000}, result);
    }

    @Test
    public void testStreamSameValueTwice() {
      var set = new IntSet();
      set.add(5);
      set.add(5);

      var result = set.stream().toArray();
      assertArrayEquals(new int[]{5}, result);
    }

    @Test
    public void testStreamCharacteristics() {
      var set = new IntSet();
      var spliterator = set.stream().spliterator();
      assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL));
      assertFalse(spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
    }

    @Test
    public void testStreamLargeSet() {
      var set = new IntSet();
      for(var i = 0; i < 10_000_000; i++) {
        if (i %3 != 0) {
          set.add(i);
        }
      }

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        var sum =  set.stream().mapToLong(l -> l).sum();
        assertEquals(33_333_326_666_667L, sum);
      });
    }
  }

  @Nested
  public class Q6 {

    @Test
    public void testToStringEmptySet() {
      var set = new IntSet();
      var result = set.toString();
      assertEquals("[]", result);
    }

    @Test
    public void testToStringSingleElement() {
      var set = new IntSet();
      set.add(42);
      var result = set.toString();
      assertEquals("[42]", result);
    }

    @Test
    public void testToStringMultipleElementsSorted() {
      var set = new IntSet();
      set.add(30);
      set.add(10);
      set.add(20);
      var result = set.toString();
      assertEquals("[10, 20, 30]", result);
    }

    @Test
    public void testToStringAcrossBucketsAndResizes() {
      var set = new IntSet();
      set.add(0);
      set.add(31);
      set.add(32);
      set.add(128);
      var result = set.toString();
      assertEquals("[0, 31, 32, 128]", result);
    }

    @Test
    public void testToStringIgnoresDuplicates() {
      var set = new IntSet();
      set.add(5);
      set.add(5);
      set.add(5);
      var result = set.toString();
      assertEquals("[5]", result);
    }
  }

  @Nested
  public class Q7 {

    @Test
    public void testBitCountEmptySet() {
      var set = new IntSet();
      assertEquals(0, set.bitCount());
    }

    @Test
    public void testBitCountSingleElement() {
      var set = new IntSet();
      set.add(42);
      assertEquals(1, set.bitCount());
    }

    @Test
    public void testBitCountMultipleElementsSameBucket() {
      var set = new IntSet();
      set.add(1);
      set.add(2);
      set.add(4);
      set.add(8);
      assertEquals(4, set.bitCount());
    }

    @Test
    public void testBitCountMultipleElementsDifferentBuckets() {
      var set = new IntSet();
      set.add(0);
      set.add(31);
      set.add(32);
      set.add(63);
      assertEquals(4, set.bitCount());
    }

    @Test
    public void testBitCountIgnoresDuplicates() {
      var set = new IntSet();
      set.add(10);
      set.add(10);
      set.add(10);
      assertEquals(1, set.bitCount());
    }

    @Test
    public void testBitCountAfterResizing() {
      var set = new IntSet();
      set.add(10);
      set.add(200);
      set.add(500);
      assertEquals(3, set.bitCount());
    }

    @Test
    public void testBitCountLargeNumberOfElements() {
      var set = new IntSet();
      for (var i = 0; i < 10_000_000; i++) {
        set.add(i * 7);
      }

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        assertEquals(10_000_000, set.bitCount());
      });
    }

    @Test
    public void testBitCountShouldNotBePublic() {
      var methods = Arrays.stream(IntSet.class.getMethods())
          .map(Method::getName)
          .collect(Collectors.toSet());

      assertFalse(methods.contains("bitCount"));
    }
  }

  @Nested
  public class Q8 {

    @Test
    public void example() {
      var intSet = new IntSet();
      assertTrue(intSet.add(1));
      assertTrue(intSet.add(12));
      assertFalse(intSet.add(12));

      assertTrue(intSet.contains(12));
      assertFalse(intSet.contains(42));

      assertEquals(13, intSet.stream().sum());

      var set = intSet.asSet();

      assertEquals(2, set.size());
      assertEquals(Set.of(1, 12), set);
    }

    @Test
    public void testAsSetEmpty() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      assertTrue(set.isEmpty());
      assertEquals(0, set.size());
      assertFalse(set.iterator().hasNext());
    }

    @Test
    public void testAsSetSize() {
      var intSet = new IntSet();
      intSet.add(10);
      intSet.add(20);
      intSet.add(20);

      var set = intSet.asSet();
      assertEquals(2, set.size());
    }

    @Test
    public void testAsSetIsAViewSize() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      intSet.add(10);

      assertEquals(1, set.size());
    }

    @Test
    public void testAsSetIsAViewIterator() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      intSet.add(10);

      var iterator = set.iterator();
      assertTrue(iterator.hasNext());
      assertEquals(10, iterator.next());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testAsSetIteratorTraversal() {
      var intSet = new IntSet();
      intSet.add(100);
      intSet.add(5);
      intSet.add(31);
      intSet.add(32);

      var set = intSet.asSet();
      var iterator = set.iterator();

      assertTrue(iterator.hasNext());
      assertEquals(5, iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals(31, iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals(32, iterator.next());
      assertTrue(iterator.hasNext());
      assertEquals(100, iterator.next());
      assertFalse(iterator.hasNext());
    }

    @Test
    public void testAsSetIteratorThrowsExceptionWhenExhausted() {
      var intSet = new IntSet();
      intSet.add(42);
      var set = intSet.asSet();
      var iterator = set.iterator();

      assertEquals(42, iterator.next());

      assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void testAsSetContains() {
      var intSet = new IntSet();
      intSet.add(10);
      intSet.add(1000);
      var set = intSet.asSet();

      assertTrue(set.contains(10));
      assertTrue(set.contains(1000));
      assertFalse(set.contains(11));
      assertFalse(set.contains(0));
    }

    @Test
    public void testAsSetEqualsAndHashCode() {
      var intSet1 = new IntSet();
      intSet1.add(1);
      intSet1.add(2);
      intSet1.add(124);

      var intSet2 = new IntSet();
      intSet2.add(2);
      intSet2.add(124);
      intSet2.add(1);

      var set1 = intSet1.asSet();
      var set2 = intSet2.asSet();

      assertEquals(set1, set2);
      assertEquals(set1.hashCode(), set2.hashCode());
    }

    @Test
    public void testAsSetLargeSet() {
      var intSet = new IntSet();
      for(var i = 0; i < 10_000_000; i++) {
        if (i %3 != 0) {
          intSet.add(i);
        }
      }

      var set = intSet.asSet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        var sum = 0L;
        for(var value : set) {
          sum += value;
        }

        assertEquals(33_333_326_666_667L, sum);
      });
    }
  }

  /*

  @Nested
  public class Q9 {

    @Test
    public void testAsSetAdd() {
      var intSet = new IntSet();

      var set = intSet.asSet();
      assertTrue(set.add(12));

      assertTrue(set.contains(12));
      assertFalse(set.contains(42));
    }

    @Test
    public void testAsSetAddDuplicate() {
      var intSet = new IntSet();

      var set = intSet.asSet();
      assertTrue(set.add(42));
      assertTrue(set.add(12));
      assertFalse(set.add(42));

      assertEquals(2, set.size());
    }

    @Test
    public void testAsSetAddLargeSet() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          if (i %3 != 0) {
            assertTrue(set.add(i));
          }
        }
      });

      assertEquals(6_666_666, set.size());
    }

    @Test
    public void testAsSetAddNullThrowsNPE() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      assertThrows(NullPointerException.class, () -> set.add(null));
    }

    @Test
    public void testAsSetRemove() {
      var intSet = new IntSet();

      var set = intSet.asSet();
      assertTrue(set.add(12));

      assertTrue(set.remove(12));
      assertFalse(set.remove(42));
      assertFalse(set.remove(12));
    }

    @Test
    public void testAsSetRemoveNonExistentValues() {
      var intSet = new IntSet();

      var set = intSet.asSet();
      assertTrue(set.add(12));

      assertFalse(set.remove(3));
      assertFalse(set.remove("Bob"));
    }

    @Test
    public void testAsSetRemoveLargeSet() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          if (i %3 != 0) {
            assertTrue(set.add(i));
          }
        }
      });

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          if (i %3 != 0) {
            assertTrue(set.remove(i));
          }
        }
      });

      assertEquals(0, set.size());
    }

    @Test
    public void testAsSetRemoveNullThrowsNPE() {
      var intSet = new IntSet();
      var set = intSet.asSet();

      assertThrows(NullPointerException.class, () -> set.remove(null));
    }

    @Test
    public void testAsSetContains() {
      var intSet = new IntSet();
      intSet.add(42);

      var set = intSet.asSet();

      assertTrue(set.contains(42));
      assertFalse(set.contains(100));
      assertFalse(set.contains("Bob"));
    }

    @Test
    public void testAsSetContainsLargeSet() {
      var intSet = new IntSet();
      for(var i = 0; i < 10_000_000; i++) {
        if (i %3 != 0) {
          intSet.add(i);
        }
      }

      var set = intSet.asSet();

      assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
        for(var i = 0; i < 10_000_000; i++) {
          if (i %3 != 0) {
            assertTrue(set.contains(i));
          } else  {
            assertFalse(set.contains(i));
          }
        }
      });
    }

    @Test
    public void testRemoveSingleElement() {
      var intSet = new IntSet();
      intSet.add(42);
      var iterator = intSet.asSet().iterator();

      assertTrue(iterator.hasNext());
      assertEquals(42, iterator.next());
      iterator.remove();

      assertFalse(iterator.hasNext());
      assertEquals(0, intSet.bitCount());
      assertFalse(intSet.contains(42));
    }

    @Test
    public void testRemoveAllElements() {
      var intSet = new IntSet();
      intSet.add(10);
      intSet.add(20);
      intSet.add(30);

      var iterator = intSet.asSet().iterator();
      while (iterator.hasNext()) {
        iterator.next();
        iterator.remove();
      }

      assertEquals(0, intSet.bitCount());
      assertTrue(intSet.asSet().isEmpty());
    }

    @Test
    public void testRemoveSpecificElement() {
      var intSet = new IntSet();
      intSet.add(1);
      intSet.add(2);
      intSet.add(3);

      var iterator = intSet.asSet().iterator();
      assertEquals(1, iterator.next());
      assertEquals(2, iterator.next());
      iterator.remove();

      assertEquals(3, iterator.next());
      assertFalse(iterator.hasNext());

      assertTrue(intSet.contains(1));
      assertFalse(intSet.contains(2));
      assertTrue(intSet.contains(3));
    }

    @Test
    public void testRemoveAcrossBucketBoundaries() {
      var intSet = new IntSet();
      intSet.add(31);
      intSet.add(32);

      var iterator = intSet.asSet().iterator();

      assertEquals(31, iterator.next());
      iterator.remove();

      assertEquals(32, iterator.next());
      iterator.remove();

      assertFalse(iterator.hasNext());
      assertEquals(0, intSet.bitCount());
    }

    @Test
    public void testRemoveBeforeNextThrowsException() {
      var intSet = new IntSet();
      intSet.add(10);
      var iterator = intSet.asSet().iterator();

      assertThrows(IllegalStateException.class, iterator::remove);
    }

    @Test
    public void testRemoveTwiceThrowsException() {
      var intSet = new IntSet();
      intSet.add(10);
      var iterator = intSet.asSet().iterator();

      iterator.next();
      iterator.remove();

      assertThrows(IllegalStateException.class, iterator::remove);
    }
  }

   */
}