package fr.uge.cactuslist;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class CactusListTest {

    // Q1

    @Test
    @Tag("Q1")
    public void testCactusListOfString() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");
        assertEquals(2, cactus.size());
    }

    @Test
    @Tag("Q1")
    public void testCactusListOfInteger() {
        var cactus = new CactusList<Integer>();
        cactus.add(2);
        cactus.add(7);
        cactus.add(-42);
        assertEquals(3, cactus.size());
    }

    @Test
    @Tag("Q1")
    public void testEmptyCactusList() {
        var cactus = new CactusList<>();
        assertEquals(0, cactus.size());
    }

    @Test
    @Tag("Q1")
    public void testCactusListWithCactusList() {
        var cactus1 = new CactusList<String>();
        cactus1.add("foo");
        cactus1.add("foo2");

        var cactus2 = new CactusList<String>();
        cactus2.add("bar");
        cactus2.addCactus(cactus1);
        cactus2.add("baz");

        assertEquals(4, cactus2.size());
    }

    @Test
    @Tag("Q1")
    public void testCactusListSize() {
        var current = new CactusList<String>();
        for (var i = 0; i < 1_000_000; i++) {
            var prevCactus = new CactusList<String>();
            prevCactus.add("foo");
            prevCactus.addCactus(current);
            current = prevCactus;
        }

        assertEquals(1_000_000, current.size());
    }

    @Test
    @Tag("Q1")
    public void testCactusListAddCactusSignature() {
        var cactus1 = new CactusList<String>();
        cactus1.add("foo");

        var cactus2 = new CactusList<CharSequence>();
        cactus2.addCactus(cactus1);

        assertEquals(1, cactus2.size());
    }

    @Test
    @Tag("Q1")
    public void testCactusListConstructor() {
        assertEquals(1, CactusList.class.getConstructors().length);
    }

    @Test
    @Tag("Q1")
    public void testCactusListContract() {
        var cactus = new CactusList<>();
        cactus.add("foo");

        assertAll(
                () -> assertThrows(NullPointerException.class, () -> cactus.add(null)),
                () -> assertThrows(NullPointerException.class, () -> cactus.addCactus(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> cactus.add(new CactusList<>()))
        );
    }

    // Q2

    @Test
    @Tag("Q2")
    public void testCactusListNotFrozenByDefault() {
        var cactus = new CactusList<String>();
        cactus.add("foo");

        assertFalse(cactus.frozen());
    }

    @Test
    @Tag("Q2")
    public void testCactusListAddCactusFrozen() {
        var cactus1 = new CactusList<String>();
        cactus1.add("foo");

        var cactus2 = new CactusList<CharSequence>();
        cactus2.addCactus(cactus1);

        assertTrue(cactus1.frozen());
        assertFalse(cactus2.frozen());
    }

    @Test
    @Tag("Q2")
    public void testFrozenCactusListCannotAddElements() {
        var cactus1 = new CactusList<>();
        var cactus2 = new CactusList<>();
        cactus2.addCactus(cactus1);

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> cactus1.add("foo")),
                () -> assertThrows(IllegalStateException.class, () -> cactus1.addCactus(new CactusList<>()))
        );
    }

    @Test
    @Tag("Q2")
    public void testCactusListCannotBeSelfRefenrencial() {
        var cactus = new CactusList<>();

        assertThrows(IllegalStateException.class, () -> cactus.addCactus(cactus));
    }


    // Q3

    @Test
    @Tag("Q3")
    public void testCactusListOfIntegerForEach() {
        var cactus = new CactusList<Integer>();
        cactus.add(2);
        cactus.add(7);
        cactus.add(-42);

        var result = new ArrayList<Integer>();
        cactus.forEach(result::add);

        assertEquals(result, List.of(2, 7, -42));
    }

    @Test
    @Tag("Q3")
    public void testCactusListWithCactusListForEach() {
        var cactus1 = new CactusList<String>();
        cactus1.add("bar");

        var cactus2 = new CactusList<String>();
        cactus2.add("foo");
        cactus2.addCactus(cactus1);
        cactus2.add("baz");

        var result = new ArrayList<String>();
        cactus2.forEach(result::add);

        assertEquals(result, List.of("foo", "bar", "baz"));
    }

    @Test
    @Tag("Q3")
    public void testCactusListWithADoubleCactusListForEach() {
        var cactus1 = new CactusList<String>();
        cactus1.add("1");

        var cactus2 = new CactusList<String>();
        cactus2.add("2");
        cactus2.add("7");

        var cactus3 = new CactusList<String>();
        cactus3.addCactus(cactus1);
        cactus3.add("--");
        cactus3.addCactus(cactus2);
        cactus3.add("--");

        var result = new ArrayList<String>();
        cactus3.forEach(result::add);

        assertEquals(result, List.of("1", "--", "2", "7", "--"));
    }

    @Test
    @Tag("Q3")
    public void testCactusListWithACactusListInDiamondShapeForEach() {
        var cactus1 = new CactusList<Integer>();
        cactus1.add(48);

        var cactus2 = new CactusList<Integer>();
        cactus2.add(89);
        cactus2.addCactus(cactus1);

        var cactus3 = new CactusList<Integer>();
        cactus3.add(134);
        cactus3.addCactus(cactus2);
        cactus3.add(14);
        cactus3.addCactus(cactus1);

        var result = new ArrayList<Integer>();
        cactus3.forEach(result::add);

        assertEquals(result, List.of(134, 89, 48, 14, 48));
    }

    @Test
    @Tag("Q3")
    public void testCactusListForEachSignature() {
        var cactus = new CactusList<String>();

        cactus.forEach((Object o) -> fail());
    }

    @Test
    @Tag("Q3")
    public void testCactusListForEachNPE() {
        var cactus = new CactusList<String>();

        assertThrows(NullPointerException.class, () -> cactus.forEach(null));
    }


    // Q4

    @Test
    @Tag("Q4")
    public void testCactusListToString() {
        var cactus = new CactusList<Integer>();
        cactus.add(67);
        cactus.add(132);
        cactus.add(44);

        assertEquals("<67, 132, 44>", cactus.toString());
    }

    @Test
    @Tag("Q4")
    public void testCactusListOneElementToString() {
        var cactus = new CactusList<Integer>();
        cactus.add(128);

        assertEquals("<128>", cactus.toString());
    }

    @Test
    @Tag("Q4")
    public void testCactusListSeveralCactusListToString() {
        var cactus1 = new CactusList<String>();
        cactus1.add("bar");

        var cactus2 = new CactusList<String>();
        cactus2.add("foo");
        cactus2.addCactus(cactus1);
        cactus2.add("baz");

        assertEquals("<foo, bar, baz>", cactus2.toString());
    }

    @Test
    @Tag("Q4")
    public void testCactusWithAngleBracketsToString() {
        var cactus1 = new CactusList<String>();
        cactus1.add("[");

        var cactus2 = new CactusList<String>();
        cactus2.add("]");
        cactus2.addCactus(cactus1);

        assertEquals("<], [>", cactus2.toString());
    }

    @Test
    @Tag("Q4")
    public void testCactusListEmptyToString() {
        var cactus = new CactusList<String>();

        assertEquals("<>", cactus.toString());
    }


    // Q5

    @Test
    @Tag("Q5")
    public void testCactusListFromIntegers() {
        CactusList<Integer> cactus = CactusList.from(List.of(1, 89, 77));

        var list = new ArrayList<Integer>();
        cactus.forEach(list::add);

        assertEquals(list, List.of(1, 89, 77));
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromStrings() {
        CactusList<String> cactus = CactusList.from(List.of("foo", "bar"));

        var list = new ArrayList<String>();
        cactus.forEach(list::add);

        assertEquals(list, List.of("foo", "bar"));
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromSize() {
        var cactus = CactusList.from(List.of("whizz", "kidz"));

        assertEquals(2, cactus.size());
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromSideEffect() {
        var list = new ArrayList<>();
        list.add("boom");
        list.add("bam");
        var cactus = CactusList.from(list);
        list.add("biim");

        assertEquals(2, cactus.size());
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromCannotAdd() {
        var cactus = CactusList.from(List.of("a", "b", "c"));

        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> cactus.add("foo")),
                () -> assertThrows(IllegalStateException.class, () -> cactus.addCactus(new CactusList<>()))
        );
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromIsFrozen() {
        var cactus = CactusList.from(List.of("whizz", "kidz"));

        assertTrue(cactus.frozen());
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromDoesNotAllowCactusListAsArgument() {
        var cactus = new CactusList<Integer>();
        cactus.add(3);
        cactus.add(14);

        assertThrows(IllegalArgumentException.class, () -> CactusList.from(List.of(5, cactus, 55)));
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromEmptyForEach() {
        var cactus = CactusList.from(List.of());
        cactus.forEach(element -> fail());
    }

    @Test
    @Tag("Q5")
    public void testCactusListFromSignature() {
        CactusList<Object> cactus = CactusList.from(List.of("foo"));

        assertEquals(1, cactus.size());
    }


    // Q6
    @Test
    @Tag("Q6")
    public void testCactusListGet() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");
        cactus.add("baz");

        assertAll(
                () -> assertEquals("foo", cactus.get(0)),
                () -> assertEquals("bar", cactus.get(1)),
                () -> assertEquals("baz", cactus.get(2))
        );
    }

    @Test
    @Tag("Q6")
    public void testCactusListGetOutOfBounds() {
        var cactus = new CactusList<Integer>();
        cactus.add(187);

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> cactus.get(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> cactus.get(1))
        );
    }

    @Test
    @Tag("Q6")
    public void testCactusListGetWithADiamondShape() {
        var cactus1 = new CactusList<Integer>();
        cactus1.add(48);

        var cactus2 = new CactusList<Integer>();
        cactus2.add(89);
        cactus2.addCactus(cactus1);

        var cactus3 = new CactusList<Integer>();
        cactus3.add(134);
        cactus3.addCactus(cactus2);
        cactus3.add(14);
        cactus3.addCactus(cactus1);

        var result = new ArrayList<Integer>();
        for (var i = 0; i < cactus3.size(); i++) {
            result.add(cactus3.get(i));
        }

        assertEquals(result, List.of(134, 89, 48, 14, 48));
    }

    @Test
    @Tag("Q6")
    public void testCactusListGetALot() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000_000).forEach(cactus::add);

        for (var i = 0; i < cactus.size(); i++) {
            assertEquals(i, cactus.get(i));
        }
    }

    @Test
    @Tag("Q6")
    public void testCactusListGetALot2() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            range(0, 1_000).forEach(j -> innerCactus.add(i * 1_000 + j));
            cactus.addCactus(innerCactus);
        });

        for (var i = 0; i < cactus.size(); i++) {
            assertEquals(i, cactus.get(i));
        }
    }

    @Test
    @Tag("Q6")
    public void testCactusListWithOnlyElementNormalized() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");

        assertTrue(cactus.normalized());
    }

    @Test
    @Tag("Q6")
    public void testCactusListNormalized() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        var cactus2 = new CactusList<String>();
        cactus2.add("cactus");
        cactus.addCactus(cactus2);
        cactus.add("bar");

        assertFalse(cactus.normalized());
    }

    @Test
    @Tag("Q6")
    public void testCactusListFromNormalized() {
        var cactus = CactusList.from(List.of("foo", 3, "bar"));
        assertTrue(cactus.normalized());
    }

    @Test
    @Tag("Q6")
    public void testCactusListNormalizedAtLot() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(cactus::add);

        var cactus2 = new CactusList<Integer>();
        range(0, 1_000).forEach(cactus2::add);

        cactus.addCactus(cactus2);
        assertFalse(cactus.normalized());

        assertEquals(0, cactus.get(0));
        assertTrue(cactus.normalized());
    }

    @Test
    @Tag("Q6")
    public void testCactusListNormalizeNotPublic() {
        assertThrows(NoSuchMethodException.class, () -> CactusList.class.getMethod("normalize"));
    }

    @Test
    @Tag("Q6")
    public void testCactusListNormalizedDontPropagate() {
        var cactus1 = new CactusList<Integer>();
        range(0, 10).forEach(cactus1::add);

        var cactus2 = new CactusList<Integer>();
        range(0, 10).forEach(cactus2::add);

        var cactus3 = new CactusList<Integer>();
        range(0, 10).forEach(cactus3::add);

        cactus2.addCactus(cactus3);
        cactus1.addCactus(cactus2);

        assertFalse(cactus1.normalized());
        assertFalse(cactus2.normalized());
        assertTrue(cactus3.normalized());

        assertEquals(0, cactus1.get(0));

        assertTrue(cactus1.normalized());
        assertFalse(cactus2.normalized());
        assertTrue(cactus3.normalized());
    }


    // Q7

    @Test
    @Tag("Q7")
    public void testCactusListEnhancedFor() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");

        for (var element : cactus) {
            assertEquals(3, element.length());
        }
    }

    @Test
    @Tag("Q7")
    public void testCactusListEnhancedForALot() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000_000).forEach(cactus::add);

        var value = 0;
        for (var element : cactus) {
            assertEquals(value++, element);
        }
    }

    @Test
    @Tag("Q7")
    public void testCactusListEnhancedForALot2() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            range(0, 1_000).forEach(j -> innerCactus.add(i * 1_000 + j));
            cactus.addCactus(innerCactus);
        });

        var value = 0;
        for (var element : cactus) {
            assertEquals(value++, element);
        }
    }

    @Test
    @Tag("Q7")
    public void testCactusListEmptyIteratorHasNext() {
        var cactus = new CactusList<>();

        assertAll(
                () -> assertFalse(cactus.iterator().hasNext()),
                () -> assertThrows(NoSuchElementException.class, () -> cactus.iterator().next())
        );
    }

    @Test
    @Tag("Q7")
    public void testCactusListIteratorALot() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000_000).forEach(cactus::add);

        var value = 0;
        var it = cactus.iterator();
        while (it.hasNext()) {
            assertTrue(it.hasNext());
            assertEquals(value++, it.next());
        }
        assertFalse(it.hasNext());
    }

    @Test
    @Tag("Q7")
    public void testCactusListIteratorALot2() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            range(0, 1_000).forEach(j -> innerCactus.add(i * 1_000 + j));
            cactus.addCactus(innerCactus);
        });

        var value = 0;
        var it = cactus.iterator();
        while (it.hasNext()) {
            assertTrue(it.hasNext());
            assertEquals(value++, it.next());
        }
        assertFalse(it.hasNext());
    }

    @Test
    @Tag("Q7")
    public void testCactusListIteratorCantRemove() {
        var cactus = new CactusList<String>();
        cactus.add("abc");
        cactus.add("it's");
        cactus.add("easy");

        var it = cactus.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    @Tag("Q7")
    public void testCactusListFromIteratorCantRemove() {
        var cactus = CactusList.from(List.of("abc", "it's", "easy"));

        var it = cactus.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    @Tag("Q7")
    public void testCactusListIsAList() {
        var cactus = new CactusList<String>();
        cactus.add("abc");
        cactus.add("it's");
        cactus.add("easy");

        assertEquals(List.of("abc", "it's", "easy"), cactus);
    }

    @Test
    @Tag("Q7")
    public void testCactusListFromIsAList() {
        var cactus = CactusList.from(List.of("abc", "it's", "easy"));

        assertEquals(List.of("abc", "it's", "easy"), cactus);
    }

    @Test
    @Tag("Q7")
    public void testCactusListWithCactusListIsAList() {
        var cactus = new CactusList<String>();
        cactus.add("abc");

        var cactus2 = new CactusList<String>();
        cactus2.add("it's");
        cactus2.add("easy");

        var cactus3 = new CactusList<String>();

        cactus.addCactus(cactus2);
        cactus.addCactus(cactus3);

        assertEquals(List.of("abc", "it's", "easy"), cactus);
    }

    @Test
    @Tag("Q7")
    public void testCactusListIsRandomAccess() {
        var cactus = new CactusList<Integer>();
        cactus.add(2);
        cactus.addCactus(new CactusList<>());
        cactus.add(4);

        assertTrue(RandomAccess.class.isInstance(cactus));
    }


    // Q8

    @Test
    @Tag("Q8")
    public void testCactusListStreamDontUseNormalization() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.addCactus(new CactusList<String>());
        cactus.add("bar");

        var stream = cactus.stream();
        assertFalse(cactus.normalized());
        assertEquals(List.of("foo", "bar"), stream.collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamDontChangeNormalizedStatus() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");

        var stream = cactus.stream();
        assertTrue(cactus.normalized());
        assertEquals(List.of("foo", "bar"), stream.collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamOneElement() {
        var cactus = new CactusList<String>();
        cactus.add("abc");

        var stream = cactus.stream();
        assertEquals("abc", stream.findFirst().orElseThrow());
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamOneElement2() {
        var cactus = new CactusList<Integer>();
        cactus.add(57394);

        var stream = cactus.stream();
        assertEquals(57394, stream.findFirst().orElseThrow());
    }


    @Test
    @Tag("Q8")
    public void testCactusListStreamALot() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000_000).forEach(cactus::add);

        assertEquals(range(0, 1_000_000).boxed().collect(toList()), cactus.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamALot2() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            range(0, 1_000).forEach(j -> innerCactus.add(i * 1_000 + j));
            cactus.addCactus(innerCactus);
        });

        assertEquals(range(0, 1_000_000).boxed().collect(toList()), cactus.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamALotOfEmpty() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            cactus.addCactus(innerCactus);
        });

        assertEquals(List.of(), cactus.stream().collect(toList()));
    }

    @Test
    @Tag("Q8")
    public void testCactusListStreamLimit() {
        var cactus = new CactusList<Integer>();
        range(0, 100).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            innerCactus.add(i);
            cactus.addCactus(innerCactus);
            cactus.add(i);
        });

        assertEquals(List.of(0, 0, 1, 1, 2, 2, 3, 3), cactus.stream().limit(8).collect(toList()));
    }


    @Test
    @Tag("Q8")
    public void testCactusListStreamCharacteristics() {
        var cactus = new CactusList<>();
        assertEquals(336, cactus.stream().spliterator().characteristics());
    }

    // Q9

    @Test
    @Tag("Q9")
    public void testCactusListIteratorDontUseNormalization() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.addCactus(new CactusList<String>());
        cactus.add("bar");

        var it = cactus.iterator();

        var result = new ArrayList<String>();
        it.forEachRemaining(result::add);
        assertFalse(cactus.normalized());
        assertEquals(List.of("foo", "bar"), result);
    }

    @Test
    @Tag("Q9")
    public void testCactusListIteratorDontChangeNormalizedStatus() {
        var cactus = new CactusList<String>();
        cactus.add("foo");
        cactus.add("bar");
        assertTrue(cactus.normalized());

        var it = cactus.iterator();
        assertTrue(cactus.normalized());

        var result = new ArrayList<String>();
        it.forEachRemaining(result::add);
        assertEquals(List.of("foo", "bar"), result);
    }

    @Test
    @Tag("Q9")
    public void testCactusListIteratorALotOfEmpty() {
        var cactus = new CactusList<Integer>();
        range(0, 1_000).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            cactus.addCactus(innerCactus);
        });

        var it = cactus.iterator();
        it.forEachRemaining(element -> fail());
    }

    @Test
    @Tag("Q9")
    public void testCactusListIteratorFirstElements() {
        var cactus = new CactusList<Integer>();
        range(0, 100).forEach(i -> {
            var innerCactus = new CactusList<Integer>();
            innerCactus.add(i);
            cactus.addCactus(innerCactus);
            cactus.add(i);
        });

        var it = cactus.iterator();
        var result = new ArrayList<Integer>();
        var limit = 0;
        while (it.hasNext() && limit++ < 8) {
            result.add(it.next());
        }

        assertEquals(List.of(0, 0, 1, 1, 2, 2, 3, 3), result);
    }
}

