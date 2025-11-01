package fr.uge.partitionvec;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.Instruction;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.reflect.AccessFlag;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class PartitionVecTest {

    @Nested
    public final class Q1 {

        @Test
        public void testConstructor() {
            PartitionVec<String> vec = new PartitionVec<String>();

            assertNotNull(vec);
        }

        @Test
        public void testAddSingleElement() {
            PartitionVec<Integer> vec = new PartitionVec<Integer>();
            vec.add(42);

            assertEquals(1, vec.size());
        }

        @Test
        public void testAddWithDifferentTypes() {
            var vecDouble = new PartitionVec<Double>();
            vecDouble.add(3.14);
            vecDouble.add(2.71);

            assertEquals(2, vecDouble.size());
        }

        @Test
        public void testAddMultipleElements() {
            var vec = new PartitionVec<String>();
            vec.add("first");
            vec.add("second");
            vec.add("third");

            assertEquals(3, vec.size());
        }

        @Test
        public void testAddNullElementThrowsException() {
            var vec = new PartitionVec<String>();
            assertThrows(NullPointerException.class, () -> vec.add(null));
        }

        @Test
        public void testArrayExpansionBeyondInitialCapacity() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);
            vec.add(5);

            assertEquals(5, vec.size());
        }

        @Test
        public void testLargeNumberOfAdditions() {
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                var vec = new PartitionVec<Integer>();
                for (var i = 0; i < 1_000_000; i++) {
                    vec.add(i);
                }

                assertEquals(1_000_000, vec.size());
            });
        }

        @Test
        public void testPartitionClassIsPublicFinal() {
            assertTrue(PartitionVec.class.accessFlags().contains(AccessFlag.PUBLIC));
            assertTrue(PartitionVec.class.accessFlags().contains(AccessFlag.FINAL));
        }

        @Test
        public void testPartitionClassFieldsArePrivate() {
            assertTrue(Arrays.stream(PartitionVec.class.getDeclaredFields())
                .allMatch(field -> field.accessFlags().contains(AccessFlag.PRIVATE)));
        }

        @Test
        public void testPartitionClassOneFieldIsAnArray() {
            assertTrue(Arrays.stream(PartitionVec.class.getDeclaredFields())
                .anyMatch(field -> field.getType().isArray()));
        }

        @Test
        public void testPartitionOnlyOnePublicConstructor() {
            assertEquals(1, PartitionVec.class.getDeclaredConstructors().length);
        }

        @Test
        public void testPartitionConstructorCallSuperAsLastInstruction() throws IOException {
            var className = "/" + PartitionVec.class.getName().replace('.', '/') + ".class";
            byte[] data;
            try (var input = PartitionVec.class.getResourceAsStream(className)) {
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
    public final class Q2 {

        @Test
        public void testToStringEmpty() {
            var vec = new PartitionVec<String>();
            var result = "" + vec;

            assertEquals("[]", result);
        }

        @Test
        public void testToStringSingleElement() {
            var vec = new PartitionVec<String>();
            vec.add("hello");
            var result = "" + vec;

            assertEquals("[hello]", result);
        }

        @Test
        public void testToStringMultipleElements() {
            var vec = new PartitionVec<String>();
            vec.add("first");
            vec.add("second");
            vec.add("third");
            var result = "" + vec;

            assertEquals("[first, second, third]", result);
        }

        @Test
        public void testToStringWithIntegerElements() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            var result = "" + vec;

            assertEquals("[1, 2, 3]", result);
        }

        @Test
        public void testToStringWithDoubleElements() {
            var vec = new PartitionVec<Double>();
            vec.add(3.14);
            vec.add(2.71);
            var result = "" + vec;

            assertEquals("[3.14, 2.71]", result);
        }

        @Test
        public void testToStringAfterArrayExpansion() {
            var vec = new PartitionVec<Integer>();
            for (var i = 0; i < 5; i++) {
                vec.add(i);
            }
            var result = "" + vec;

            assertEquals("[0, 1, 2, 3, 4]", result);
        }

        @Test
        public void testToStringWithSpecialCharacters() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b,c");
            vec.add("d");
            var result = "" + vec;

            assertEquals("[a, b,c, d]", result);
        }

        @Test
        public void testToStringFormat() {
            var vec = new PartitionVec<String>();
            vec.add("x");
            vec.add("y");
            var result = "" + vec;

            assertTrue(result.startsWith("["));
            assertTrue(result.endsWith("]"));
            assertTrue(result.contains(", "));
        }

        @Test
        public void testToStringMultipleCalls() {
            var vec = new PartitionVec<Integer>();
            vec.add(10);
            vec.add(20);
            var result1 = "" + vec;
            var result2 = "" + vec;

            assertEquals(result1, result2);
        }

        @Test
        public void testToStringWithLargeNumberOfElements() {
            var vec = new PartitionVec<Integer>();
            var count = 100;
            for (var i = 0; i < count; i++) {
                vec.add(i);
            }
            var result = "" + vec;

            assertTrue(result.startsWith("["));
            assertTrue(result.endsWith("]"));
            assertTrue(result.contains(", "));
        }

        @Test
        public void testToStringDoesNotIncludeUnusedArrayElements() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            var result = "" + vec;

            assertEquals("[a, b]", result);
            assertFalse(result.contains("null"));
        }
    }

    /*

    @Nested
    public final class Q3 {
        @Test
        public void testPartitionListWithIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            vec.add(3);
            vec.add(7);
            vec.add(4);
            List<Integer> result = vec.partition(n -> n % 2 == 0);

            assertEquals(List.of(2, 4), result);
        }

        @Test
        public void testPartitionListWithStrings() {
            var vec = new PartitionVec<String>();
            vec.add("foo");
            vec.add("bar");
            vec.add("baz");
            vec.add("whizz");
            List<String> result = vec.partition(s -> s.endsWith("z"));

            assertEquals(List.of("whizz", "baz"), result);
        }

        @Test
        public void testPartitionListWithMixedIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(7);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(List.of(2), result);
        }

        @Test
        public void testPartitionListWithOddIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            ;
            vec.add(3);
            vec.add(5);
            vec.add(7);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(List.of(), result);
        }

        @Test
        public void testPartitionListWithEvenIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            ;
            vec.add(6);
            vec.add(4);
            vec.add(8);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(List.of(2, 6, 4, 8), result);
        }


        @Test
        public void testPartitionListEmpty() {
            var vec = new PartitionVec<Integer>();
            var result = vec.partition(n -> true);

            assertEquals(List.of(), result);
        }


        @Test
        public void testPartitionAlgorithmWithIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            vec.add(3);
            vec.add(7);
            vec.add(4);
            vec.partition(n -> n % 2 == 0);

            assertEquals("[2, 4, 7, 3]", "" + vec);
        }

        @Test
        public void testPartitionAlgorithmWithStrings() {
            var vec = new PartitionVec<String>();
            vec.add("foo");
            vec.add("bar");
            vec.add("baz");
            vec.add("whizz");
            vec.partition(s -> s.endsWith("z"));

            assertEquals("[whizz, baz, bar, foo]", "" + vec);
        }

        @Test
        public void testPartitionAlgorithmWithMixedIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(7);
            vec.partition(n -> n % 2 == 0);

            assertEquals("[2, 3, 7, 1]", "" + vec);
        }

        @Test
        public void testPartitionAlgorithmWithOddIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            ;
            vec.add(3);
            vec.add(5);
            vec.add(7);
            vec.partition(n -> n % 2 == 0);

            assertEquals("[3, 5, 7, 1]", "" + vec);
        }

        @Test
        public void testPartitionAlgorithmWithEvenIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            ;
            vec.add(6);
            vec.add(4);
            vec.add(8);
            vec.partition(n -> n % 2 == 0);

            assertEquals("[2, 6, 4, 8]", "" + vec);
        }

        @Test
        public void testPartitionAlgorithmEmptyNotCallThePartitionFunction() {
            var vec = new PartitionVec<Integer>();
            vec.partition(_ -> fail());

            assertEquals("[]", "" + vec);
        }

        @Test
        public void testPartitionThrowsExceptionForNul() {
            var vec = new PartitionVec<String>();

            assertThrows(NullPointerException.class, () -> vec.partition(null));
        }
    }

    @Nested
    public final class Q4 {

        @Test
        public void testPartitionCorrectSize() {
            var vec = new PartitionVec<String>();
            vec.add("apple");
            vec.add("banana");
            vec.add("cherry");
            var result = vec.partition(s -> !s.isEmpty());

            assertEquals(3, result.size());
        }

        @Test
        public void testPartitionCorrectSizeOMixedIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(4);
            vec.add(3);
            vec.add(6);
            vec.add(5);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(2, result.size());
        }

        @Test
        public void testPartitionCorrectElementsAccess() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(3);
            vec.add(5);
            var result = vec.partition(n -> n == 3);

            assertEquals(1, result.size());
            assertEquals(3, result.get(0));

            assertThrows(IndexOutOfBoundsException.class, () -> result.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> result.get(1));
        }

        @Test
        public void testPartitionCorrectElementsAccess2() {
            var vec = new PartitionVec<Integer>();
            vec.add(10);
            vec.add(2);
            vec.add(5);
            vec.add(1);
            vec.add(8);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(3, result.size());
            assertEquals(List.of(10, 2, 8), result);

            assertThrows(IndexOutOfBoundsException.class, () -> result.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> result.get(3));
        }

        @Test
        public void testPartitionCorrectElementsAccessEmpty() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(3);
            vec.add(5);
            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(0, result.size());

            assertThrows(IndexOutOfBoundsException.class, () -> result.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> result.get(0));
        }

        @Test
        public void testPartitionViewIsUnmodifiable() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            vec.add("c");
            var result = vec.partition(_ -> true);

            assertEquals(3, result.size());

            assertAll(() -> {
                assertThrows(UnsupportedOperationException.class, () -> result.add("d"));
                assertThrows(UnsupportedOperationException.class, () -> result.remove(0));
                assertThrows(UnsupportedOperationException.class, () -> result.clear());
                assertThrows(UnsupportedOperationException.class, () -> result.set(0, "x"));
            });
        }

        @Test
        public void testPartitionViewIsNotAffectedBySubsequentVectorSizeChanges() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);

            var result = vec.partition(n -> n % 2 == 0);

            assertEquals(List.of(4, 2), result);

            vec.add(5);  // Should not affect the view

            assertEquals(List.of(4, 2), result);
        }

        @Test
        public void testPartitionViewIsALocalClass() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            var view = vec.partition(_ -> true);

            assertTrue(view.getClass().isLocalClass());
        }

        @Test
        public void testPartitionViewIsHandWritten() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            var view = vec.partition(_ -> true);

            assertNotEquals("java.util", view.getClass().getPackageName());
        }
    }


    @Nested
    public final class Q5 {

        @Test
        public void testPartitionViewThrowsISEAfterSecondPartitionCall() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            vec.add("c");
            var result = vec.partition(s -> s.length() == 1);

            // Second call to partition(), that shuffle the elements
            vec.partition(s -> s.equals("a"));

            assertThrows(IllegalStateException.class, () -> result.get(0));
            assertThrows(IllegalStateException.class, () -> result.get(1));
            assertThrows(IllegalStateException.class, () -> result.get(2));
        }

        @Test
        public void testMultiplePartitionViewsInvalidatedBySubsequentCall() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);

            var resultA = vec.partition(n -> n == 1);
            var resultB = vec.partition(n -> n == 2);
            var resultC = vec.partition(n -> n == 3);

            assertThrows(IllegalStateException.class, resultA::getFirst);

            assertThrows(IllegalStateException.class, resultB::getFirst);

            assertEquals(1, resultC.size());
            assertEquals(3, resultC.getFirst());
        }

        @Test
        public void testInvalidationDoNotInvalidateTheSize() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            var result = vec.partition(n -> true);

            // Invalidate the view.
            vec.partition(_ -> false);

            assertEquals(1, result.size());
        }

        @Test
        public void testBoundChecksIsDoneBeforeInvalidationCheck() {
            var vec = new PartitionVec<Integer>();
            var result = vec.partition(_ -> true);

            vec.partition(n -> false);

            assertThrows(IndexOutOfBoundsException.class, () -> result.get(0));
        }
    }


    @Nested
    public final class Q6 {

        @Test
        public void testOtherPartitionReturnsTheComplementaryElements() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            vec.add(3);
            vec.add(7);
            vec.add(4);

            var evenPartition = vec.partition(n -> n % 2 == 0);
            var oddPartition = evenPartition.otherPartition();

            assertEquals(List.of(2, 4), evenPartition);
            assertEquals(List.of(7, 3), oddPartition);
        }

        @Test
        public void testOtherPartitionCorrectSize() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);
            vec.add(5);

            var evenPartition = vec.partition(n -> n % 2 == 0);
            var oddPartition = evenPartition.otherPartition();

            assertEquals(2, evenPartition.size());
            assertEquals(3, oddPartition.size());
        }

        @Test
        public void testOtherPartitionWithStrings() {
            var vec = new PartitionVec<String>();
            vec.add("foo");
            vec.add("bar");
            vec.add("baz");
            vec.add("whizz");

            var endingWithZ = vec.partition(s -> s.endsWith("z"));
            var notEndingWithZ = endingWithZ.otherPartition();

            assertEquals(List.of("whizz", "baz"), endingWithZ);
            assertEquals(List.of("bar", "foo"), notEndingWithZ);
        }

        @Test
        public void testOtherPartitionCanAccessElements() {
            var vec = new PartitionVec<Integer>();
            vec.add(10);
            vec.add(20);
            vec.add(30);
            vec.add(40);

            var firstPartition = vec.partition(n -> n < 25);
            var secondPartition = firstPartition.otherPartition();

            assertEquals(2, secondPartition.size());
            assertEquals(40, secondPartition.get(0));
            assertEquals(30, secondPartition.get(1));
        }

        @Test
        public void testOtherPartitionIsNotAffectedBySubsequentVectorSizeChanges() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);

            var result = vec.partition(n -> n % 2 == 0);
            var otherResult = result.otherPartition();

            assertEquals(List.of(3, 1), otherResult);

            vec.add(5);  // Should not affect the other partition

            assertEquals(List.of(3, 1), otherResult);
        }

        @Test
        public void testOtherPartitionIsUnmodifiable() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            vec.add("c");

            var firstPartition = vec.partition(s -> s.equals("a"));
            var secondPartition = firstPartition.otherPartition();

            assertThrows(UnsupportedOperationException.class, () -> secondPartition.add("d"));
            assertThrows(UnsupportedOperationException.class, () -> secondPartition.remove(0));
            assertThrows(UnsupportedOperationException.class, () -> secondPartition.clear());
            assertThrows(UnsupportedOperationException.class, () -> secondPartition.set(0, "x"));
        }

        @Test
        public void testOtherPartitionViewIsHandWritten() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            var otherPartition = vec.partition(_ -> true)
                .otherPartition();

            assertNotEquals("java.util", otherPartition.getClass().getPackageName());
        }

        @Test
        public void testOtherPartitionWithEmptyFirstPartition() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(3);
            vec.add(5);

            var evenPartition = vec.partition(n -> n % 2 == 0);
            var oddPartition = evenPartition.otherPartition();

            assertEquals(List.of(), evenPartition);
            assertEquals(List.of(3, 5, 1), oddPartition);
        }

        @Test
        public void testOtherPartitionWithEmptySecondPartition() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            vec.add(4);
            vec.add(6);

            var evenPartition = vec.partition(n -> n % 2 == 0);
            var oddPartition = evenPartition.otherPartition();

            assertEquals(List.of(2, 4, 6), evenPartition);
            assertEquals(List.of(), oddPartition);
        }

        @Test
        public void testOtherPartitionSizeWithLargeCollection() {
            var vec = new PartitionVec<Integer>();
            for (int i = 0; i < 1_000_000; i++) {
                vec.add(i);
            }

            var evenPartition = vec.partition(n -> n % 2 == 0);
            var oddPartition = evenPartition.otherPartition();

            assertEquals(500_000, evenPartition.size());
            assertEquals(500_000, oddPartition.size());
        }

        @Test
        public void testOtherPartitionThrowsCMEAfterModification() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);

            var firstPartition = vec.partition(n -> n % 2 == 0);
            var secondPartition = firstPartition.otherPartition();

            vec.partition(n -> n < 2);

            assertEquals(2, secondPartition.size());
            assertThrows(IllegalStateException.class, secondPartition::getFirst);
        }
    }


    @Nested
    public final class Q7 {
        @Test
        public void testOtherPartitionOfOtherPartitionWithIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            vec.add(3);
            vec.add(7);
            vec.add(4);
            List<Integer> result = vec.partition(n -> n % 2 == 0)
                .otherPartition().otherPartition();

            assertEquals(List.of(2, 4), result);
        }

        @Test
        public void testOtherPartitionOfOtherPartitionWithStrings() {
            var vec = new PartitionVec<String>();
            vec.add("foo");
            vec.add("bar");
            vec.add("baz");
            vec.add("whizz");
            List<String> result = vec.partition(s -> s.endsWith("z"))
                .otherPartition().otherPartition();

            assertEquals(List.of("whizz", "baz"), result);
        }

        @Test
        public void testOtherPartitionOfOtherPartitionWithMixedIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(7);
            var result = vec.partition(n -> n % 2 == 0)
                .otherPartition().otherPartition();

            assertEquals(List.of(2), result);
        }

        @Test
        public void testOtherPartitionOfOtherPartitionWithOddIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            ;
            vec.add(3);
            vec.add(5);
            vec.add(7);
            var result = vec.partition(n -> n % 2 == 0)
                .otherPartition().otherPartition();

            assertEquals(List.of(), result);
        }

        @Test
        public void testOtherPartitionOfOtherPartitionWithEvenIntegers() {
            var vec = new PartitionVec<Integer>();
            vec.add(2);
            ;
            vec.add(6);
            vec.add(4);
            vec.add(8);
            var result = vec.partition(n -> n % 2 == 0)
                .otherPartition().otherPartition();

            assertEquals(List.of(2, 6, 4, 8), result);
        }


        @Test
        public void testOtherPartitionOfOtherPartitionEmpty() {
            var vec = new PartitionVec<Integer>();
            var result = vec.partition(n -> true)
                .otherPartition().otherPartition();

            assertEquals(List.of(), result);
        }

        @Test
        public void testOtherPartitionOfOtherPartitionReturnsFirstPartition() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);

            var firstPartition = vec.partition(n -> n % 2 == 0);
            var secondPartition = firstPartition.otherPartition();
            var backToFirst = secondPartition.otherPartition();

            assertEquals(List.of(4, 2), firstPartition);
            assertEquals(List.of(3, 1), secondPartition);
            assertEquals(List.of(4, 2), backToFirst);
        }

    }


    @Nested
    public final class Q8 {
        @Test
        public void testPartitionVecIsACollection() {
            Collection<String> vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            vec.add("c");

            assertInstanceOf(Collection.class, vec);
        }

        @Test
        public void testIsEmptyWithAnEmptyVec() {
            var vec = new PartitionVec<String>();
            assertTrue(vec.isEmpty());
        }

        @Test
        public void testIsEmptyWithANonEmptyVec() {
            var vec = new PartitionVec<String>();
            vec.add("item");

            assertFalse(vec.isEmpty());
        }

        @Test
        public void testAdd() {
            var vec = new PartitionVec<Integer>();
            assertTrue(vec.add(42));

            assertEquals(Set.of(42), new HashSet<>(vec));
        }

        @Test
        public void testClear() {
            var vec = new PartitionVec<String>();
            vec.add("x");
            vec.add("y");
            assertEquals(2, vec.size());

            assertThrows(UnsupportedOperationException.class, vec::clear);
        }

        @Test
        public void testContainsElementIsPresent() {
            var vec = new PartitionVec<Double>();
            vec.add(3.14);
            vec.add(2.71);

            assertTrue(vec.contains(3.14));
        }

        @Test
        public void testContainsElementIsNotPresent() {
            var vec = new PartitionVec<Double>();
            vec.add(3.14);

            assertFalse(vec.contains(1.0));
        }

        @Test
        public void testToArray() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            var array = vec.toArray();

            assertEquals(2, array.length);
            assertEquals(1, array[0]);
            assertEquals(2, array[1]);
        }

        @Test
        public void testToArrayWithType() {
            var vec = new PartitionVec<>();
            vec.add("a");
            vec.add("b");

            String[] array = vec.toArray(new String[0]);

            assertEquals(2, array.length);
            assertEquals("a", array[0]);
            assertEquals("b", array[1]);
        }

        @Test
        public void testToArrayWithAFunction() {
            var vec = new PartitionVec<>();
            vec.add("a");
            vec.add("b");

            String[] array = vec.toArray(String[]::new);

            assertEquals(2, array.length);
            assertEquals("a", array[0]);
            assertEquals("b", array[1]);
        }

        @Test
        public void testAddAll() {
            var vec = new PartitionVec<Integer>();
            var toAdd = List.of(10, 20, 30);

            assertTrue(vec.addAll(toAdd));

            assertEquals(3, vec.size());
            assertEquals("[10, 20, 30]", "" + vec);
        }

        @Test
        public void testContainsAllWithAllPresent() {
            var vec = new PartitionVec<String>();
            vec.add("x");
            vec.add("y");
            vec.add("z");

            assertTrue(vec.containsAll(List.of("x", "z")));
        }

        @Test
        public void testContainsAllWithNotAllPresent() {
            var vec = new PartitionVec<String>();
            vec.add("x");
            vec.add("y");

            assertFalse(vec.containsAll(List.of("x", "z")));
        }

        @Test
        public void testRemoveAllElements() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);
            vec.add(4);

            assertThrows(UnsupportedOperationException.class,
                () -> vec.removeAll(List.of(2, 4)));
        }

        @Test
        public void testRetainAllElements() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");
            vec.add("c");
            vec.add("d");

            assertThrows(UnsupportedOperationException.class,
                () -> vec.retainAll(List.of("b", "d", "x")));
        }

        @Test
        public void testForEachAllElementsInOrder() {
            var vec = new PartitionVec<String>();
            vec.add("alpha");
            vec.add("beta");
            vec.add("gamma");

            var actual = new ArrayList<String>();
            vec.forEach(actual::add);

            assertEquals(List.of("alpha", "beta", "gamma"), actual);
        }

        @Test
        public void testForEachThatAdd() {
            var vec = new PartitionVec<String>();
            vec.add("alpha");
            vec.add("beta");
            vec.add("gamma");

            vec.forEach(vec::add);

            assertEquals(List.of("alpha", "beta", "gamma", "alpha", "beta", "gamma"), List.copyOf(vec));
        }

        @Test
        public void testForEachEmptyCollection() {
            var vec = new PartitionVec<Integer>();

            vec.forEach(_ -> fail());
        }

        @Test
        public void testForEachFastEnough() {
            var vec = new PartitionVec<Integer>();
            for (int i = 0; i < 1_000_000; i++) {
                vec.add(i);
            }

            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                var box = new Object() {
                    long sum;
                };
                vec.forEach(n -> box.sum += n);
                assertEquals(499_999_500_000L, box.sum);
            });
        }

        @Test
        public void testStreamMapAndReduce() {
            var vec = new PartitionVec<Integer>();
            vec.add(1);
            vec.add(2);
            vec.add(3);

            var result = vec.stream()
                .mapToInt(Integer::intValue)
                .sum();

            assertEquals(6, result);
        }

        @Test
        public void testStreamFastEnough() {
            var vec = new PartitionVec<Integer>();
            for (int i = 0; i < 1_000_000; i++) {
                vec.add(i);
            }

            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                var sum = vec.stream().mapToLong(n -> n).sum();
                assertEquals(499_999_500_000L, sum);
            });
        }

        @Test
        public void testIteratorElements() {
            var vec = new PartitionVec<Integer>();
            vec.add(10);
            vec.add(20);
            vec.add(30);

            var actual = new ArrayList<Integer>();
            for (var element : vec) {
                actual.add(element);
            }

            assertEquals(List.of(10, 20, 30), actual);
        }

        @Test
        public void testIteratorEmpty() {
            var vec = new PartitionVec<String>();
            var it = vec.iterator();
            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        public void testIteratorOneElement() {
            var vec = new PartitionVec<String>();
            vec.add("one");
            var it = vec.iterator();

            assertEquals("one", it.next());

            assertFalse(it.hasNext());
            assertThrows(NoSuchElementException.class, it::next);
        }

        @Test
        public void testIteratorStillWorksAfterAdd() {
            var vec = new PartitionVec<String>();
            vec.add("one");
            vec.add("two");
            vec.add("three");

            var it = vec.iterator();

            vec.add("four");

            var list = new ArrayList<String>();
            while (it.hasNext()) {
                list.add(it.next());
            }
            assertEquals(List.of("one", "two", "three"), list);
        }

        @Test
        public void testIteratorRemoveIsNotSupported() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            var it = vec.iterator();

            it.next();

            assertThrows(UnsupportedOperationException.class, it::remove);
        }

        @Test
        public void testIteratorFastEnough() {
            var vec = new PartitionVec<Integer>();
            for (int i = 0; i < 1_000; i++) {
                vec.add(i);
            }

            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                var sum = 0;
                for (int i = 0; i < 100_000; i++) {
                    sum += vec.iterator().next();
                }
                assertEquals(0, sum);
            });
        }
    }

    @Nested
    public final class Q9 {
        @Test
        public void testPartitionInvalidateIterator() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");

            var iterator = vec.iterator();

            vec.partition(s -> s.equals("a"));

            assertThrows(IllegalStateException.class, iterator::next);
        }

        @Test
        public void testPartitionInvalidateForEach() {
            var vec = new PartitionVec<String>();
            vec.add("a");
            vec.add("b");

            assertThrows(IllegalStateException.class, () -> {
                vec.forEach(_ -> {
                    vec.partition(_ -> false);
                });
            });
        }
    }

     */

}