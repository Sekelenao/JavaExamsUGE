package fr.uge.embed;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.*;

public class EmbedListTest {
    static final class Node {
        private Node next;
        private final int value;

        Node(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return "Node" + value;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    @Nested
    public class Q1 {
        @Test
        public void example() {
            EmbedList<Node> list = new EmbedList<Node>(Node::getNext, Node::setNext);
            list.addFirst(new Node(1));
            list.addFirst(new Node(2));
            list.addFirst(new Node(3));
            assertEquals(3, list.size());
        }

        @Test
        public void example2() {
            class State {
                private State next;
                private final String value;

                State(String value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "State(" + value + ")";
                }

                public State next() {
                    return next;
                }

                public void next(State next) {
                    this.next = next;
                }
            }

            var list = new EmbedList<State>(State::next, State::next);
            list.addFirst(new State("1"));
            list.addFirst(new State("2"));
            list.addFirst(new State("3"));
            list.addFirst(new State("4"));
            assertEquals(4, list.size());
        }

        @Test
        public void emptyList() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertEquals(0, list.size());
        }

        @Test
        @Timeout(2)
        public void embedListALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000_000).forEach(i -> list.addFirst(new Node(i)));
            assertEquals(1_000_000, list.size());
        }

        @Test
        public void publicConstructor() {
            var constructors = EmbedList.class.getConstructors();
            assertEquals(1, constructors.length);
            var constructor = constructors[0];
            assertAll(
                    () -> assertEquals(2, constructor.getParameterCount()),
                    () -> assertTrue(Arrays.stream(constructor.getParameterTypes()).allMatch(type -> type.getPackageName().equals("java.util.function")))
            );
        }

        @Test
        public void allFieldsContainingLambdasAreDeclaredFinal() {
            assertTrue(Arrays.stream(EmbedList.class.getDeclaredFields())
                    .filter(field -> field.getType().getPackageName().equals("java.util.function"))
                    .allMatch(field -> Modifier.isFinal(field.getModifiers())));
        }

        @Test
        public void allFieldsAreDeclaredPrivate() {
            assertTrue(Arrays.stream(EmbedList.class.getDeclaredFields())
                    .allMatch(field -> Modifier.isPrivate(field.getModifiers())));
        }

        @Test
        public void preconditions() {
            class Foo {
                Foo getNextFoo() {
                    return null;
                }

                void setNextFoo(Foo next) {
                }
            }
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> new EmbedList<>(Foo::getNextFoo, null)),
                    () -> assertThrows(NullPointerException.class, () -> new EmbedList<>(null, Foo::setNextFoo))
            );
        }
    }

    @Nested
    public class Q2 {
        @Test
        public void example() {
            EmbedList<Node> list = new EmbedList<Node>(Node::getNext, Node::setNext);
            list.addFirst(new Node(1));
            list.addFirst(new Node(2));
            list.addFirst(new Node(3));

            var result = new ArrayList<Integer>();
            list.forEach(node -> result.add(node.value));
            assertEquals(List.of(3, 2, 1), result);
        }

        @Test
        public void example2() {
            class State {
                private State next;
                private final String value;

                State(String value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "State(" + value + ")";
                }

                public State next() {
                    return next;
                }

                public void next(State next) {
                    this.next = next;
                }
            }

            var list = new EmbedList<State>(State::next, State::next);
            list.addFirst(new State("1"));
            list.addFirst(new State("2"));
            list.addFirst(new State("3"));
            list.addFirst(new State("4"));

            var result = new ArrayList<String>();
            list.forEach(state -> result.add(state.value));
            assertEquals(List.of("4", "3", "2", "1"), result);
        }

        @Test
        public void forEach() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(1));
            list.addFirst(new Node(2));
            list.addFirst(new Node(3));

            var list2 = new ArrayList<Integer>();
            list.forEach(link -> list2.add(link.value()));
            assertEquals(List.of(3, 2, 1), list2);
        }

        @Test
        public void forEachSignature() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 10).forEach(i -> list.addFirst(new Node(i)));

            var box = new Object() {
                int sum;
            };
            list.forEach((Object o) -> box.sum++);
            assertEquals(10, box.sum);
        }

        @Test
        @Timeout(2)
        public void forEachALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000_000).forEach(i -> list.addFirst(new Node(i)));

            var box = new Object() {
                int sum;
            };
            list.forEach(link -> box.sum += link.value());
            assertEquals(1_783_293_664, box.sum);
        }

        @Test
        public void precondition() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertThrows(NullPointerException.class, () -> list.forEach(null));
        }
    }


    @Nested
    public class Q3 {
        @Test
        public void example() {
            EmbedList<Node> list = new EmbedList<Node>(Node::getNext, Node::setNext);
            list.addFirst(new Node(1));
            list.addFirst(new Node(2));
            list.addFirst(new Node(3));
            assertEquals(3, list.size());

            var result = new ArrayList<Integer>();
            for (Node node : list) {
                result.add(node.value);
            }
            assertEquals(List.of(3, 2, 1), result);
        }

        @Test
        public void example2() {
            class State {
                private State next;
                private final String value;

                State(String value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "State (" + value + ")";
                }

                public State next() {
                    return next;
                }

                public void next(State next) {
                    this.next = next;
                }
            }

            var list = new EmbedList<State>(State::next, State::next);
            list.addFirst(new State("1"));
            list.addFirst(new State("2"));
            list.addFirst(new State("3"));
            list.addFirst(new State("4"));
            assertEquals(4, list.size());

            var result = new ArrayList<String>();
            for (var state : list) {
                result.add(state.value);
            }
            assertEquals(List.of("4", "3", "2", "1"), result);
        }

        @Test
        @Timeout(2)
        public void forALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000_000).forEach(i -> list.addFirst(new Node(i)));

            var i = 0;
            for (var node : list) {
                assertEquals(999_999 - i++, node.value);
            }
        }

        @Test
        public void iteratorContract() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(42));
            list.addFirst(new Node(21));

            Iterator<Node> iterator = list.iterator();
            assertTrue(iterator.hasNext());
            assertEquals(21, iterator.next().value);
            assertTrue(iterator.hasNext());
            assertEquals(42, iterator.next().value);
            assertFalse(iterator.hasNext());
        }

        @Test
        @Timeout(2)
        public void iteratorALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000_000).forEach(i -> list.addFirst(new Node(i)));

            var iterator = list.iterator();
            for (var i = 0; i < 1_000_000; i++) {
                assertEquals(999_999 - i, iterator.next().value);
            }
        }

        @Test
        public void iteratorRemove() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(42));

            var iterator = list.iterator();
            iterator.next();
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        @Test
        public void iteratorNext() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);

            var iterator = list.iterator();
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }


    @Nested
    public class Q4 {
        @Test
        public void example() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(27));
            list.addFirst(new Node(73));
            list.addFirst(new Node(101));

            assertAll(
                    () -> assertEquals(3, list.size()),
                    () -> assertEquals(101, list.get(0).value),
                    () -> assertEquals(73, list.get(1).value),
                    () -> assertEquals(27, list.get(2).value)
            );
        }

        @Test
        public void sameNode() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(13);
            var node2 = new Node(88);
            list.addFirst(node1);
            list.addFirst(node2);

            assertAll(
                    () -> assertEquals(2, list.size()),
                    () -> assertSame(node2, list.get(0)),
                    () -> assertSame(node1, list.get(1))
            );
        }

        @Test
        public void preconditions() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            assertAll(
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1)),
                    () -> assertThrows(IndexOutOfBoundsException.class, () -> list.get(1))
            );
        }
    }


    @Nested
    public class Q5 {
        @Test
        public void isItAList() {
            List<Node> list = new EmbedList<Node>(Node::getNext, Node::setNext);
            assertAll(
                    () -> assertTrue(list.isEmpty()),
                    () -> assertEquals(0, list.size())
            );
        }

        @Test
        public void testEquals() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(22);
            var node2 = new Node(47);
            list.addFirst(node1);
            list.addFirst(node2);
            assertEquals(List.of(node2, node1), list);
        }

        @Test
        public void testHashCode() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(22);
            var node2 = new Node(47);
            list.addFirst(node1);
            list.addFirst(node2);
            assertEquals(List.of(node2, node1).hashCode(), list.hashCode());
        }

        @Test
        public void testToString() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(22);
            var node2 = new Node(47);
            list.addFirst(node1);
            list.addFirst(node2);
            assertEquals(List.of(node2, node1).toString(), list.toString());
        }

        @Test
        public void indexOf() {
            final class Link {
                private Link link;
                private final int value;

                Link(int value) {
                    this.value = value;
                }

                @Override
                public boolean equals(Object obj) {
                    return obj instanceof Link link && value == link.value;
                }

                @Override
                public int hashCode() {
                    return value;
                }

                Link getLink() {
                    return link;
                }

                void setLink(Link link) {
                    this.link = link;
                }
            }

            var list = new EmbedList<>(Link::getLink, Link::setLink);
            list.addFirst(new Link(1));
            list.addFirst(new Link(10));
            list.addFirst(new Link(1));

            assertEquals(0, list.indexOf(new Link(1)));
        }

        @Test
        @Timeout(2)
        public void indexOfALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 100_000).forEach(i -> list.addFirst(new Node(i)));
            assertEquals(-1, list.indexOf("banzai"));
        }

        @Test
        public void indexOfPrecondition() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertThrows(NullPointerException.class, () -> list.indexOf(null));
        }

        @Test
        public void lastIndexOf() {
            final class Link {
                private Link link;
                private final int value;

                Link(int value) {
                    this.value = value;
                }

                @Override
                public boolean equals(Object obj) {
                    return obj instanceof Link link && value == link.value;
                }

                @Override
                public int hashCode() {
                    return value;
                }

                Link getLink() {
                    return link;
                }

                void setLink(Link link) {
                    this.link = link;
                }
            }

            var list = new EmbedList<>(Link::getLink, Link::setLink);
            list.addFirst(new Link(1));
            list.addFirst(new Link(10));
            list.addFirst(new Link(1));

            assertEquals(2, list.lastIndexOf(new Link(1)));
        }

        @Test
        @Timeout(2)
        public void lastIndexOfALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 100_000).forEach(i -> list.addFirst(new Node(i)));
            assertEquals(-1, list.lastIndexOf("banzai"));
        }

        @Test
        public void lastIndexOfALotPrecondition() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertThrows(NullPointerException.class, () -> list.lastIndexOf(null));
        }

        @Test
        public void contains() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(22);
            var node2 = new Node(47);
            list.addFirst(node1);
            list.addFirst(node2);
            assertAll(
                    () -> assertTrue(list.contains(node1)),
                    () -> assertTrue(list.contains(node2)),
                    () -> assertFalse(list.contains(new Node(99))),
                    () -> assertFalse(list.contains("foo"))
            );
        }

        @Test
        @Timeout(2)
        public void containsALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 100_000).forEach(i -> list.addFirst(new Node(i)));
            assertFalse(list.contains("banzai"));
        }

        @Test
        public void toArray() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(22);
            var node2 = new Node(47);
            list.addFirst(node1);
            list.addFirst(node2);
            assertArrayEquals(new Object[]{node2, node1}, list.toArray());
        }

        @Test
        public void subList() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var nodes = range(0, 10).mapToObj(Node::new).toList();
            nodes.forEach(list::addFirst);
            assertEquals(List.of(nodes.get(7), nodes.get(6)), list.subList(2, 4));
        }
    }


    @Nested
    public class Q6 {
        @Test
        public void unmodifiable() {
            EmbedList<Node> list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(11));
            list.addFirst(new Node(22));
            EmbedList<Node> list2 = list.unmodifiable();

            assertAll(
                    () -> assertEquals(2, list2.size()),
                    () -> assertEquals(22, list2.get(0).value),
                    () -> assertEquals(11, list2.get(1).value),
                    () -> assertThrows(UnsupportedOperationException.class, () -> list2.addFirst(new Node(33)))
            );
        }

        @Test
        public void anUnmodifiableListIsUnmodifiable() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(25));
            var list2 = list.unmodifiable();

            assertSame(list2, list2.unmodifiable());
        }

        @Test
        public void anUnmodifiableListIsAView() {
            class MutableState {
                private MutableState next;
                private String value;

                MutableState(String value) {
                    this.value = value;
                }

                @Override
                public String toString() {
                    return "MutableState (" + value + ")";
                }

                public MutableState getNext() {
                    return next;
                }

                public void setNext(MutableState next) {
                    this.next = next;
                }
            }

            var list = new EmbedList<>(MutableState::getNext, MutableState::setNext);
            list.addFirst(new MutableState("foo"));
            var list2 = list.unmodifiable();
            list.get(0).value = "bar";

            assertEquals("bar", list2.get(0).value);
        }

        @Test
        public void unmodifiableListDoNotMutateTheCurrentList() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(101));
            var list2 = list.unmodifiable();
            list.addFirst(new Node(202));

            assertAll(
                    () -> assertEquals(2, list.size()),
                    () -> assertEquals(202, list.get(0).value),
                    () -> assertEquals(101, list.get(1).value)
            );
        }
    }

    @Nested
    public class Q7 {
        @Test
        public void add() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(45);
            var node2 = new Node(52);
            assertTrue(list.add(node1));
            assertTrue(list.add(node2));

            assertAll(
                    () -> assertEquals(2, list.size()),
                    () -> assertEquals(List.of(node1, node2), list)
            );
        }

        @Test
        public void addAll() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(45);
            var node2 = new Node(52);
            assertTrue(list.addAll(List.of(node1, node2)));

            assertAll(
                    () -> assertEquals(2, list.size()),
                    () -> assertEquals(List.of(node1, node2), list)
            );
        }

        @Test
        public void addAllSet() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var node1 = new Node(45);
            assertTrue(list.addAll(Set.of(node1)));

            assertAll(
                    () -> assertEquals(1, list.size()),
                    () -> assertEquals(List.of(node1), list)
            );
        }

        @Test
        public void addAllEmpty() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertFalse(list.addAll(List.of()));

            assertAll(
                    () -> assertEquals(0, list.size()),
                    () -> assertEquals(List.of(), list)
            );
        }

        @Test
        public void addPrecondition() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertThrows(NullPointerException.class, () -> list.add(null));
        }

        @Test
        public void addAllPreconditions() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> list.addAll(null)),
                    () -> assertThrows(NullPointerException.class, () -> list.addAll(Arrays.asList(null, new Node(0))))
            );
        }

        @Test
        public void addUnmodifiable() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(405));
            var list2 = list.unmodifiable();

            assertThrows(UnsupportedOperationException.class, () -> list2.add(new Node(234)));
        }

        @Test
        public void addAllUnmodifiable() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(405));
            var list2 = list.unmodifiable();

            assertThrows(UnsupportedOperationException.class, () -> list2.addAll(List.of(new Node(234))));
        }

        @Test
        public void addUnmodifiableShouldNotSeePostModifications() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(845));
            var list2 = list.unmodifiable();
            list.add(new Node(321));

            assertThrows(IndexOutOfBoundsException.class, () -> list2.get(1));
        }

        @Test
        public void addUnmodifiableShouldNotSeePostModifications2() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(845));
            var list2 = list.unmodifiable();
            list.add(new Node(321));

            assertEquals(List.of(list.get(0)), list2);
        }

        @Test
        public void addUnmodifiableShouldNotSeePostModifications3() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(845));
            var list2 = list.unmodifiable();
            list.add(new Node(321));

            var result = new ArrayList<Node>();
            list2.forEach(result::add);
            assertEquals(List.of(list.get(0)), result);
        }

        @Test
        public void addUnmodifiableShouldNotSeePostModifications4() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.add(new Node(845));
            var list2 = list.unmodifiable();
            list.add(new Node(321));

            var result = new ArrayList<Node>();
            for (var node : list2) {
                result.add(node);
            }
            assertEquals(List.of(list.get(0)), result);
        }

        @Test
        @Timeout(2)
        public void getFirstIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            var first = new Node(0);
            list.add(first);
            for (var i = 1; i < 100_000; i++) {
                list.add(new Node(i));
                assertEquals(first, list.get(0));
            }
        }

        @Test
        @Timeout(2)
        public void getLastIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            for (var i = 1; i < 100_000; i++) {
                var last = new Node(i);
                list.add(last);
                assertEquals(last, list.get(list.size() - 1));
            }
        }

        @Test
        @Timeout(2)
        public void getFirstUsingAListIteratorIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            var first = new Node(0);
            list.add(first);
            for (var i = 1; i < 100_000; i++) {
                list.add(new Node(i));
                assertEquals(first, list.listIterator(0).next());
            }
        }

        @Test
        @Timeout(2)
        public void getLastUsingAListIteratorIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            for (var i = 1; i < 100_000; i++) {
                var last = new Node(i);
                list.add(last);
                assertEquals(last, list.listIterator(list.size() - 1).next());
            }
        }

        @Test
        @Timeout(2)
        public void getFirstUnmodifiableIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            var first = new Node(0);
            list.add(first);
            for (var i = 1; i < 100_000; i++) {
                list.add(new Node(i));
                assertEquals(first, list.unmodifiable().get(0));
            }
        }

        @Test
        @Timeout(2)
        public void getLastUnmodifiableIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            for (var i = 1; i < 100_000; i++) {
                var last = new Node(i);
                list.add(last);
                assertEquals(last, list.unmodifiable().get(list.size() - 1));
            }
        }

        @Test
        @Timeout(2)
        public void getFirstUnmodifiableUsingAListIteratorIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            var first = new Node(0);
            list.add(first);
            for (var i = 1; i < 100_000; i++) {
                list.add(new Node(i));
                assertEquals(first, list.unmodifiable().listIterator(0).next());
            }
        }

        @Test
        @Timeout(2)
        public void getLastUnmodifiableUsingAListIteratorIsConstantTime() {
            var list = new EmbedList<Node>(Node::getNext, Node::setNext);
            for (var i = 1; i < 100_000; i++) {
                var last = new Node(i);
                list.add(last);
                assertEquals(last, list.unmodifiable().listIterator(list.size() - 1).next());
            }
        }
    }


    @Nested
    public class Q8 {
        @Test
        public void valueStream() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(35));
            list.addFirst(new Node(62));

            assertEquals(List.of(62, 35), list.valueStream(Node::value).toList());
        }

        @Test
        public void valueStreamMap() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000).forEach(i -> list.addFirst(new Node(i)));

            assertEquals(1_000, list.valueStream(Node::value).map(node -> fail()).count());
        }

        @Test
        @Timeout(2)
        public void valueStreamALot() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000_000).forEach(i -> list.addFirst(new Node(i)));

            var box = new Object() {
                int index = 1_000_000;
            };
            list.valueStream(Node::value).forEach(i -> assertEquals(--box.index, i));
        }

        @Test
        public void valueStreamSpliteratorEstimateSizeIsPrecise() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000).forEach(i -> list.addFirst(new Node(i)));

            var spliterator = list.valueStream(node -> node).spliterator();
            var size = 1_000;
            while (spliterator.tryAdvance(i -> {
            })) {
                assertEquals(--size, spliterator.estimateSize());
            }
        }

        @Test
        public void valueStreamCharacteristics() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var spliterator = list.valueStream(node -> node).spliterator();

            assertAll(
                    () -> assertFalse(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void valueStreamNonUnmodifiableCharacteristics() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            var spliterator = list.unmodifiable().valueStream(node -> node).spliterator();
            assertAll(
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.IMMUTABLE)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED)),
                    () -> assertTrue(spliterator.hasCharacteristics(Spliterator.NONNULL))
            );
        }

        @Test
        public void valueStreamSignature() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            list.addFirst(new Node(35));
            list.addFirst(new Node(62));

            assertEquals(List.of("Node62", "Node35"), list.valueStream((Object o) -> "" + o).toList());
        }

        @Test
        public void valueStreamParallelIsParallel() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000).forEach(i -> list.addFirst(new Node(i)));

            assertTrue(list.valueStream(node -> node).parallel().isParallel());
        }

        @Test
        public void valueStreamParallelNotSplittable() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            range(0, 1_000).forEach(i -> list.addFirst(new Node(i)));
            var spliterator =
                    list.valueStream(node -> node)
                            .parallel()
                            .spliterator();

            assertNull(spliterator.trySplit());
        }

        @Test
        public void precondition() {
            var list = new EmbedList<>(Node::getNext, Node::setNext);
            assertThrows(NullPointerException.class, () -> list.valueStream(null));
        }
    }


    @Nested
    public class Q9 {
        static class Data implements EmbedList.Entry<Data> {
            private Data next;
            private final int value;

            Data(int value) {
                this.value = value;
            }

            public int value() {
                return value;
            }

            @Override
            public String toString() {
                return "Data" + value;
            }

            @Override
            public Data getNext() {
                return next;
            }

            @Override
            public void setNext(Data next) {
                this.next = next;
            }
        }

        @Test
        public void example() {
            var list = EmbedList.of(Data.class);
            list.add(new Data(29));
            list.add(new Data(81));

            assertEquals(List.of(29, 81), list.stream().map(Data::value).toList());
        }

        @Test
        public void example2() {
            class Info implements EmbedList.Entry<Info> {
                private Info next;
                private final String value;

                Info(String value) {
                    this.value = value;
                }

                public String value() {
                    return value;
                }

                @Override
                public String toString() {
                    return "Info(" + value + ")";
                }

                @Override
                public Info getNext() {
                    return next;
                }

                @Override
                public void setNext(Info next) {
                    this.next = next;
                }
            }

            var list = EmbedList.of(Info.class);
            list.add(new Info("29"));
            list.add(new Info("81"));

            assertEquals(List.of("29", "81"), list.stream().map(Info::value).toList());
        }

        @Test
        public void erasureIsStillSafe() {
            class FakeEntry implements EmbedList.Entry {
                @Override
                public EmbedList.Entry getNext() {
                    throw new AssertionError();
                }

                @Override
                public void setNext(EmbedList.Entry entry) {
                    throw new AssertionError();
                }
            }

            var list = (List) EmbedList.of(Data.class);
            list.add(new Data(11));
            assertThrows(ClassCastException.class, () -> list.add(new FakeEntry()));
        }

        @Test
        public void precondition() {
            assertThrows(NullPointerException.class, () -> EmbedList.of(null));
        }
    }
}