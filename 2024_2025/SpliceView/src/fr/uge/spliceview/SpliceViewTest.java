package fr.uge.spliceview;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpliceViewTest {
  @Nested
  public class Q1 {
    @Test
    public void spliceViewOfString() {
      SpliceView<String> spliceView = SpliceView.of(List.of("1", "2", "3"), 1, new String[] { "foo" });

      assertAll(
          () -> assertEquals(4, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("2", spliceView.get(2)),
          () -> assertEquals("3", spliceView.get(3))
      );
    }

    @Test
    public void spliceViewOfInteger() {
      SpliceView<Integer> spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[] { 10, 11 });

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(10, spliceView.get(2)),
          () -> assertEquals(11, spliceView.get(3)),
          () -> assertEquals(3, spliceView.get(4))
      );
    }

    @Test
    public void spliceViewFirst() {
      var spliceView = SpliceView.of(List.of(1, 2), 0, new Integer[] { 99 });

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(99, spliceView.get(0)),
          () -> assertEquals(1, spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewLast() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, new Integer[] { 99 });

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(99, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewListEmpty() {
      var spliceView = SpliceView.of(List.of(), 0, new Integer[] { 20, 21 });

      assertAll(
          () -> assertEquals(2, spliceView.size()),
          () -> assertEquals(20, spliceView.get(0)),
          () -> assertEquals(21, spliceView.get(1))
      );
    }

    @Test
    public void spliceViewArrayEmpty() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[0]);

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(3, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewBothEmpty() {
      var spliceView = SpliceView.of(List.of(), 0, new Object[0]);

      assertEquals(0, spliceView.size());
    }

    @Test
    public void spliceViewIsAView() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 2, new Integer[] { 10, 11 });
      list.set(1, -777);

      assertAll(
          () -> assertEquals(6, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(-777, spliceView.get(1)),
          () -> assertEquals(10, spliceView.get(2)),
          () -> assertEquals(11, spliceView.get(3)),
          () -> assertEquals(3, spliceView.get(4)),
          () -> assertEquals(4, spliceView.get(5))
      );
    }

    @Test
    public void spliceViewIsAView2() {
      var array = new Integer[] { 10, 11, 12 };
      var spliceView = SpliceView.of(List.of(1, 2, 3, 4), 3, array);
      array[0] = -777;

      assertAll(
          () -> assertEquals(7, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(3, spliceView.get(2)),
          () -> assertEquals(-777, spliceView.get(3)),
          () -> assertEquals(11, spliceView.get(4)),
          () -> assertEquals(12, spliceView.get(5)),
          () -> assertEquals(4, spliceView.get(6))
      );
    }

    @Test
    public void spliceViewAllowNull() {
      var spliceView = SpliceView.of(Arrays.asList(null, 2), 1, new Integer[] { 10} );

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertNull(spliceView.get(0)),
          () -> assertEquals(10, spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewAllowNull2() {
      var spliceView = SpliceView.of(Stream.of(1, 2).toList(), 1, new Integer[] { null });

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertNull(spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewGetIsCorrectlyTyped() {
      SpliceView<String> spliceView = SpliceView.of(List.of("foo"), 1, new String[] { "bar" });
      String first = spliceView.get(0);

      assertEquals("foo", first);
    }

    @Test
    public void spliceViewGetIsCorrectlyTyped2() {
      SpliceView<Integer> spliceView = SpliceView.of(List.of(1), 0, new Integer[] { 2 });
      Integer first = spliceView.get(0);

      assertEquals(2, first);
    }

    @Test
    public void spliceViewSizeIsFast() {
      var list = IntStream.range(0, 1_000_000).boxed().toList();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var spliceView = SpliceView.of(list, i, new Integer[0]);
          assertEquals(1_000_000, spliceView.size());
        }
      });
    }

    @Test
    public void spliceViewSizeIsFast2() {
      var array = IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var spliceView = SpliceView.of(List.of(), 0, array);
          assertEquals(1_000_000, spliceView.size());
        }
      });
    }

    @Test
    public void spliceViewSizeIsFast3() {
      var list = IntStream.range(0, 1_000_000).boxed().toList();
      var array = IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          var spliceView = SpliceView.of(list, i, array);
          assertEquals(2_000_000, spliceView.size());
        }
      });
    }

    @Test
    public void spliceViewGetIsFast() {
      var list = IntStream.range(0, 1_000_000).boxed().toList();
      var spliceView = SpliceView.of(list, 0, new Integer[0]);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(i, spliceView.get(i));
        }
      });
    }

    @Test
    public void spliceViewGetIsFast2() {
      var array = IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new);
      var spliceView = SpliceView.of(List.of(), 0, array);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(i, spliceView.get(i));
        }
      });
    }

    @Test
    public void spliceViewGetIsFast3() {
      var list = IntStream.range(0, 1_000_000).boxed().toList();
      var array = IntStream.range(1_000_000, 2_000_000).boxed().toArray(Integer[]::new);
      var spliceView = SpliceView.of(list, 1_000_000, array);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 2_000_000; i++) {
          assertEquals(i, spliceView.get(i));
        }
      });
    }

    @Test
    public void spliceViewGetIsFast4() {
      var list = IntStream.range(1_000_000, 2_000_000).boxed().toList();
      var array = IntStream.range(0, 1_000_000).boxed().toArray(Integer[]::new);
      var spliceView = SpliceView.of(list, 0, array);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        for(var i = 0; i < 2_000_000; i++) {
          assertEquals(i, spliceView.get(i));
        }
      });
    }

    @Test
    public void ofPreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class,
              () -> SpliceView.of(null, 0, new Integer[0])),
          () -> assertThrows(NullPointerException.class,
              () -> SpliceView.of(List.of(1), 0, (Integer[]) null)),
          () -> assertThrows(IllegalArgumentException.class,
              () -> SpliceView.of(List.of(1), 2, new Integer[0])),
          () -> assertThrows(IllegalArgumentException.class,
              () -> SpliceView.of(List.of(1), -1, new Integer[0]))
      );
    }

    @Test
    public void getPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, new Integer[] { 10, 11 });
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> spliceView.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> spliceView.get(4))
      );
    }

    @Test
    public void qualityOfImplementation() {
      assertAll(
          () -> assertTrue(SpliceView.class.accessFlags().contains(AccessFlag.PUBLIC)),
          () -> assertTrue(SpliceView.class.accessFlags().contains(AccessFlag.FINAL)),
          () -> assertEquals(0, SpliceView.class.getConstructors().length)
      );
    }
  }

  @Nested
  public class Q2 {
    @Test
    public void listOfString() {
      List<String> spliceView = SpliceView.of(List.of("1", "2", "3"), 1, new String[] { "foo" });

      assertAll(
          () -> assertEquals(4, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("2", spliceView.get(2)),
          () -> assertEquals("3", spliceView.get(3))
      );
    }

    @Test
    public void listOfInteger() {
      List<Integer> spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[] { 10, 11 });

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(10, spliceView.get(2)),
          () -> assertEquals(11, spliceView.get(3)),
          () -> assertEquals(3, spliceView.get(4))
      );
    }

    @Test
    public void spliceViewFirst() {
      var spliceView = SpliceView.of(List.of(1, 2), 0, 99);

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(99, spliceView.get(0)),
          () -> assertEquals(1, spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewLast() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, 99);

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(99, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewListEmpty() {
      var spliceView = SpliceView.of(List.of(), 0, 20, 21);

      assertAll(
          () -> assertEquals(2, spliceView.size()),
          () -> assertEquals(20, spliceView.get(0)),
          () -> assertEquals(21, spliceView.get(1))
      );
    }

    @Test
    public void spliceViewArrayEmpty() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2);

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(3, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewBothEmpty() {
      var spliceView = SpliceView.of(List.of(), 0);

      assertEquals(0, spliceView.size());
    }

    @Test
    public void spliceViewGetIsCorrectlyTyped() {
      List<String> spliceView = SpliceView.of(List.of("foo"), 1, new String[] { "bar" });
      String first = spliceView.get(0);

      assertEquals("foo", first);
    }

    @Test
    public void spliceViewGetIsCorrectlyTyped2() {
      List<Integer> spliceView = SpliceView.of(List.of(1), 0, new Integer[] { 2 });
      Integer first = spliceView.getFirst();

      assertEquals(2, first);
    }

    @Test
    public void ofPreconditions() {
      assertAll(
          () -> assertThrows(NullPointerException.class,
              () -> SpliceView.of(null, 0)),
          () -> assertThrows(NullPointerException.class,
              () -> SpliceView.of(List.of(1), 0, (Integer[]) null)),
          () -> assertThrows(IllegalArgumentException.class,
              () -> SpliceView.of(List.of(1), 2)),
          () -> assertThrows(IllegalArgumentException.class,
              () -> SpliceView.of(List.of(1), -1))
      );
    }

    @Test
    public void getPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, 10, 11);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> spliceView.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class,
              () -> spliceView.get(4))
      );
    }
  }

  @Nested
  public class Q3 {
    @Test
    public void spliceViewOfStringToString() {
      var spliceView= SpliceView.of(List.of("1", "2", "3"), 1, new String[] { "foo" });

      assertEquals("[1, @ foo, 2, 3]", "" + spliceView);
    }

    @Test
    public void spliceViewOfIntegerToString() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[] { 10, 11 });

      assertEquals("[1, 2, @ 10, 11, 3]", "" + spliceView);
    }

    @Test
    public void spliceViewFirstToString() {
      var spliceView = SpliceView.of(List.of(1, 2), 0, new Integer[] { 99 });

      assertEquals("[@ 99, 1, 2]", "" + spliceView);
    }

    @Test
    public void spliceViewLastToString() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, new Integer[] { 99 });

      assertEquals("[1, 2, @ 99]", "" + spliceView);
    }

    @Test
    public void spliceViewListEmptyToString() {
      var spliceView = SpliceView.of(List.of(), 0, new Integer[] { 20, 21 });

      assertEquals("[@ 20, 21]", "" + spliceView);
    }

    @Test
    public void spliceViewArrayEmptyToString() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[0]);

      assertEquals("[1, 2, 3]", "" + spliceView);
    }

    @Test
    public void spliceViewBothEmptyToString() {
      var spliceView = SpliceView.of(List.of(), 0, new Integer[0]);

      assertEquals("[]", "" + spliceView);
    }

    @Test
    public void listOfStringIsAViewToString() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, new String[] { "foo" });
      list.set(0, "777");

      assertEquals("[777, @ foo, 2, 3]", "" + spliceView);
    }

    @Test
    public void listOfStringIsAViewToString2() {
      var array = new String[] { "foo" };
      var spliceView = SpliceView.of(List.of("1", "2", "3"), 2, array);
      array[0] = "777";

      assertEquals("[1, 2, @ 777, 3]", "" + spliceView);
    }

    @Test
    public void spliceViewNullToString() {
      var spliceView = SpliceView.of(List.of("1", "2"), 0, new String[] { "foo" });

      assertEquals("[@ foo, 1, 2]", spliceView.toString());
    }

    @Test
    public void spliceViewNullToString2() {
      var spliceView = SpliceView.of(Arrays.asList("1", null), 0, new String[] { "foo" });

      assertEquals("[@ foo, 1, null]", spliceView.toString());
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void spliceViewLoopOnLinkedListIsFastEnough() {
      var list = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toCollection(LinkedList::new));
      var spliceView = SpliceView.of(list, 500_000, 500_000);
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        var sum = 0L;
        for(var value : spliceView) {
          sum += value;
        }
        assertEquals(500_000_000_000L, sum);
      });
    }
  }

  @Nested
  public class Q6 {
    @Test
    public void listOfStringSet() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, new String[] { "foo" });
      assertEquals("2", spliceView.set(2, "X"));

      assertAll(
          () -> assertEquals(4, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("X", spliceView.get(2)),
          () -> assertEquals("3", spliceView.get(3))
      );
    }

    @Test
    public void listOfIntegerSet() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, new Integer[] { 10, 11 });
      assertEquals(11, spliceView.set(3, 888));

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(10, spliceView.get(2)),
          () -> assertEquals(888, spliceView.get(3)),
          () -> assertEquals(3, spliceView.get(4))
      );
    }

    @Test
    public void spliceViewFirstSet() {
      var spliceView = SpliceView.of(List.of(1, 2), 0, 99);
      assertEquals(99, spliceView.set(0, 888));

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(888, spliceView.get(0)),
          () -> assertEquals(1, spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2))
      );
    }

    @Test
    public void spliceViewLastSet() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, 99);
      assertEquals(99, spliceView.set(2, 888));

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(888, spliceView.get(2))
      );
    }

    @Test
    public void unmodifiableListSet() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, 20, 21);

      assertThrows(UnsupportedOperationException.class, () -> spliceView.set(1, 888));
    }
  }

  @Nested
  public class Q7 {
    @Test
    public void subListLeftPartOfInteger() {
      var spliceView = SpliceView.of(List.of(1, 2, 3), 2, 10, 11);
      List<Integer> subList = spliceView.subList(0, 2);

      assertAll(
          () -> assertEquals(2, subList.size()),
          () -> assertEquals(1, subList.get(0)),
          () -> assertEquals(2, subList.get(1))
      );
    }

    @Test
    public void subListLeftPartOfString() {
      var spliceView = SpliceView.of(List.of("1", "2", "3"), 2, "foo", "bar");
      List<String> subList = spliceView.subList(1, 2);

      assertAll(
          () -> assertEquals(1, subList.size()),
          () -> assertEquals("2", subList.get(0))
      );
    }

    @Test
    public void subListRightPart() {
      var spliceView = SpliceView.of(List.of("1", "2", "3"), 1, "foo", "bar");
      var subList = spliceView.subList(3, 5);

      assertAll(
          () -> assertEquals(2, subList.size()),
          () -> assertEquals("2", subList.get(0)),
          () -> assertEquals("3", subList.get(1))
      );
    }

    @Test
    public void subListMiddlePart() {
      var spliceView = SpliceView.of(List.of("1", "2", "3"), 1, "foo", "bar", "baz");
      var subList = spliceView.subList(1, 3);

      assertAll(
          () -> assertEquals(2, subList.size()),
          () -> assertEquals("foo", subList.get(0)),
          () -> assertEquals("bar", subList.get(1))
      );
    }

    @Test
    public void subList() {
      var spliceView = SpliceView.of(List.of("1", "2", "3"), 1, "foo");
      var subList = spliceView.subList(0, 3);

      assertAll(
          () -> assertEquals(3, subList.size()),
          () -> assertEquals("1", subList.get(0)),
          () -> assertEquals("foo", subList.get(1)),
          () -> assertEquals("2", subList.get(2))
      );
    }

    @Test
    public void subListEmpty() {
      var spliceView = SpliceView.of(List.of(), 0);
      var subList = spliceView.subList(0, 0);

      assertEquals(0, subList.size());
    }

    @Test
    public void subListAddLeftPart() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 2, 888);
      var subList = spliceView.subList(0, 2);

      assertThrows(UnsupportedOperationException.class, () -> subList.add(66));
    }

    @Test
    public void subListAddMiddlePart() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 1, 888, 999);
      var subList = spliceView.subList(1, 2);

      assertThrows(UnsupportedOperationException.class, () -> subList.add(66));
    }

    @Test
    public void subListAddRightPart() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 1, 888, 999);
      var subList = spliceView.subList(4, 5);
      subList.add(66);

      assertAll(
          () -> assertEquals(7, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(888, spliceView.get(1)),
          () -> assertEquals(999, spliceView.get(2)),
          () -> assertEquals(2, spliceView.get(3)),
          () -> assertEquals(3, spliceView.get(4)),
          () -> assertEquals(66, spliceView.get(5)),
          () -> assertEquals(4, spliceView.get(6))
      );
    }

    /*
    @Override
    public void add(int index, T element) {
        if(fromIndex < arrayIndex + array.length){
            throw new UnsupportedOperationException();
        }
        list.add(arrayIndex + index, element);
    }

    @Override
    public T remove(int index) {
        if(fromIndex < arrayIndex + array.length){
            throw new UnsupportedOperationException();
        }
        return list.remove(arrayIndex + index);
    }
     */

    // [1, [888], 2, 3, 66]
    // [3, 66]

    @Test
    public void subListAddRightAtTheEnd() {
      var list = new ArrayList<>(List.of(1, 2, 3));
      var spliceView = SpliceView.of(list, 1, 888);
      var subList = spliceView.subList(4, 4);
      subList.add(66);

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(888, spliceView.get(1)),
          () -> assertEquals(2, spliceView.get(2)),
          () -> assertEquals(3, spliceView.get(3)),
          () -> assertEquals(66, spliceView.get(4))
      );
    }

    @Test
    public void subListAddRightEmpty() {
      var list = new ArrayList<>();
      var spliceView = SpliceView.of(list, 0);
      var subList = spliceView.subList(0, 0);
      subList.add(66);

      assertAll(
          () -> assertEquals(1, spliceView.size()),
          () -> assertEquals(66, spliceView.get(0))
      );
    }

    @Test
    public void subListClearRightPart() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 0, 888);
      var subList = spliceView.subList(1, 3);
      subList.clear();

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals(888, spliceView.get(0)),
          () -> assertEquals(3, spliceView.get(1)),
          () -> assertEquals(4, spliceView.get(2))
      );
    }

    @Test
    public void subListClearRightPart2() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 1, 888);
      var subList = spliceView.subList(2, 5);
      subList.clear();

      assertAll(
          () -> assertEquals(2, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(888, spliceView.get(1))
      );
    }

    @Test
    public void subListRemoveLastRightPart() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 2, 888);
      var subList = spliceView.subList(3, 4);
      subList.removeLast();

      assertAll(
          () -> assertEquals(4, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(888, spliceView.get(2)),
          () -> assertEquals(4, spliceView.get(3))
      );
    }

    @Test
    public void subListSet() {
      var list = new ArrayList<>(List.of(1, 2, 3, 4));
      var spliceView = SpliceView.of(list, 3, 888);
      var subList = spliceView.subList(0, 2);
      subList.set(1, 66);

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(66, spliceView.get(1)),
          () -> assertEquals(3, spliceView.get(2)),
          () -> assertEquals(888, spliceView.get(3)),
          () -> assertEquals(4, spliceView.get(4))
      );
    }

    @Test
    public void subListSet2() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, 888, 999);
      var subList = spliceView.subList(2, 4);
      subList.set(1, 66);

      assertAll(
          () -> assertEquals(4, spliceView.size()),
          () -> assertEquals(1, spliceView.get(0)),
          () -> assertEquals(2, spliceView.get(1)),
          () -> assertEquals(888, spliceView.get(2)),
          () -> assertEquals(66, spliceView.get(3))
      );
    }

    @Test
    public void subListSetFailIfNotMutable() {
      var spliceView = SpliceView.of(List.of(1, 2), 2, 888);
      var subList = spliceView.subList(0, 2);

      assertThrows(UnsupportedOperationException.class, () -> subList.set(0, 66));
    }

    @Test
    public void subListPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, 888, 999);

      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> spliceView.subList(-1, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> spliceView.subList(0, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> spliceView.subList(5, 6))
      );
    }

    @Test
    public void subListGetPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, 888, 999);
      var subList = spliceView.subList(1, 3);

      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.get(2))
      );
    }

    @Test
    public void subListSetPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, 888, 999);
      var subList = spliceView.subList(1, 4);

      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.set(-1, 66)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.set(3, 66))
      );
    }
  }

  @Nested
  public class Q8 {
    @Test
    public void subSubListAdd() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, "foo");
      var subList = spliceView.subList(0, 3);
      var subSubList = subList.subList(2, 3);
      subSubList.add("X");

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("2", spliceView.get(2)),
          () -> assertEquals("X", spliceView.get(3)),
          () -> assertEquals("3", spliceView.get(4))
      );
    }

    @Test
    public void subSubListAdd2() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, "foo");
      var subList = spliceView.subList(0, 4);
      var subSubList = subList.subList(4, 4);
      subSubList.add("X");

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("2", spliceView.get(2)),
          () -> assertEquals("3", spliceView.get(3)),
          () -> assertEquals("X", spliceView.get(4))
      );
    }

    @Test
    public void subSubListAdd3() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, "foo");
      var subList = spliceView.subList(1, 3);
      var subSubList = subList.subList(1, 1);
      subSubList.add("X");

      assertAll(
          () -> assertEquals(5, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("X", spliceView.get(2)),
          () -> assertEquals("2", spliceView.get(3)),
          () -> assertEquals("3", spliceView.get(4))
      );
    }

    @Test
    public void subSubListClear() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, "foo");
      var subList = spliceView.subList(0, 4);
      var subSubList = subList.subList(2, 4);
      subSubList.clear();

      assertAll(
          () -> assertEquals(2, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1))
      );
    }

    @Test
    public void subSubListRemoveLast() {
      var list = new ArrayList<>(List.of("1", "2", "3"));
      var spliceView = SpliceView.of(list, 1, "foo");
      var subList = spliceView.subList(0, 3);
      var subSubList = subList.subList(2, 3);
      subSubList.removeLast();

      assertAll(
          () -> assertEquals(3, spliceView.size()),
          () -> assertEquals("1", spliceView.get(0)),
          () -> assertEquals("foo", spliceView.get(1)),
          () -> assertEquals("3", spliceView.get(2))
      );
    }

    @Test
    public void subListSubListPreconditions() {
      var spliceView = SpliceView.of(List.of(1, 2), 1, 888, 999);
      var subList = spliceView.subList(0, 4);

      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.subList(-1, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.subList(0, -1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subList.subList(5, 6))
      );
    }
  }

}