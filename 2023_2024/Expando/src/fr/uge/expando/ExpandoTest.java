package fr.uge.expando;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpandoTest {
  @Nested
  public class Q1 {
    @Test
    public void simple() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("John", Map.of("age", 32));
      assertAll(
          () -> assertEquals(new Person("John", Map.of("age", 32)), person),
          () -> assertEquals(Map.of("age", 32), person.moreAttributes)
      );
    }

    @Test
    public void nonMutable() {
      record Foo(Map<String, Object> moreAttributes) implements Expando {
        Foo {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Foo.class);
        }
      }

      var map = new LinkedHashMap<String, Object>();
      var foo = new Foo(map);
      map.put("bar", "baz");
      assertAll(
          () -> assertEquals(Map.of(), foo.moreAttributes),
          () -> assertThrows(UnsupportedOperationException.class, () -> foo.moreAttributes().put("bar", "baz"))
      );
    }

    @Test
    public void empty() {
      record Empty(Map<String, Object> moreAttributes) implements Expando {
        Empty {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Empty.class);
        }
      }

      var empty = new Empty(Map.of("name", "John", "age", 32));
      assertAll(
          () -> assertEquals(new Empty(Map.of("name", "John", "age", 32)), empty),
          () -> assertEquals(Map.of("name", "John", "age", 32), empty.moreAttributes)
      );
    }

    @Test
    public void recordWithSeveralComponents() {
      record Several(int k0, int k1, int k2, int k3, int k4, int k5, int k6, int k7, int k8, int k9, Map<String, Object> moreAttributes) implements Expando {
        Several {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Several.class);
        }
      }

      var several = new Several(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, Map.of("other", 100));
      assertAll(
          () -> assertEquals(new Several(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, Map.of("other", 100)), several),
          () -> assertEquals(Map.of("other", 100), several.moreAttributes)
      );
    }

    @Test
    public void aLotOfAttributes() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var map = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> map.put("" + i, i));
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        var attr = new Attr(32, map);
        assertEquals(map, attr.moreAttributes);
      });
    }

    @Test
    public void invalidKeys() {
      record Dog(String name, int age, Map<String, Object> moreAttributes) implements Expando {
        Dog {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Dog.class);
        }
      }

      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> new Dog("scooby", 12, Map.of("name", "Bob"))),
          () -> assertThrows(IllegalArgumentException.class, () -> new Dog("scooby", 12, Map.of("age", 32))),
          () -> assertDoesNotThrow(() -> new Dog("scooby", 12, Map.of())),
          () -> assertDoesNotThrow(() -> new Dog("scooby", 12, Map.of("feet", 4))),
          () -> assertDoesNotThrow(() -> new Dog("scooby", 12, Map.of("feet", 4, "noze", 1)))
      );
    }

    @Test
    public void expandoUtilsQualityOfImplementation() {
      assertEquals(0, ExpandoUtils.class.getConstructors().length);
    }

    @Test
    public void copyAttributesQualityOfImplementation() throws NoSuchMethodException {
      var methods = Arrays.stream(ExpandoUtils.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("copyAttributes"))
          .toList();
      assertEquals(1, methods.size());
      var method = methods.getFirst();
      assertTrue(method.accessFlags().contains(AccessFlag.PUBLIC));
    }

    @Test
    public void copyAttributesPreconditions() {
      record Dog(String name, int age, Map<String, Object> moreAttributes) implements Expando {
        Dog {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Dog.class);
        }
      }

      assertAll(
          () -> assertThrows(NullPointerException.class, () -> ExpandoUtils.copyAttributes(null, Dog.class)),
          () -> assertThrows(NullPointerException.class, () -> ExpandoUtils.copyAttributes(Map.of(), null)),
          () -> assertThrows(NullPointerException.class, () -> ExpandoUtils.copyAttributes(new TreeMap<>() {{ put(null, "value"); }} , Dog.class)),
          () -> assertThrows(NullPointerException.class, () -> ExpandoUtils.copyAttributes(new LinkedHashMap<>() {{ put("key", null); }} , Dog.class))
      );
    }

    @Test
    public void moreAttributesIsAValidKey() {
      record Dog(String name, int age, Map<String, Object> moreAttributes) implements Expando {
        Dog {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Dog.class);
        }
      }

      var dog = new Dog("Krypto", 5, Map.of("moreAttributes", 0));
      assertEquals(Map.of("moreAttributes", 0), dog.moreAttributes);
    }
  }

  @Nested
  public class Q2 {
    @Test
    public void hasACache() {
      var field = Arrays.stream(ExpandoUtils.class.getDeclaredFields())
          .filter(f -> f.getType() == ClassValue.class)
          .findFirst()
          .orElseThrow();

      assertAll(
          () -> assertTrue(field.accessFlags().contains(AccessFlag.STATIC)),
          () -> assertTrue(field.accessFlags().contains(AccessFlag.FINAL)),
          () -> assertTrue(field.getGenericType() instanceof ParameterizedType type &&
              List.of(type.getActualTypeArguments()).toString()
                  .equals("[java.util.Map<java.lang.String, java.lang.reflect.RecordComponent>]"))
      );
    }
  }

  @Nested
  public class Q3 {
    @Test
    public void typeChecking() {
      record Foo(Map<String, Object> moreAttributes) implements Expando {
        Foo {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Foo.class);
        }
      }

      var type = new Foo(Map.of()).getClass();
      assertAll(
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", 3), Foo.class)),
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", "bar"), Foo.class)),
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", LocalDateTime.now()), Foo.class)),
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", 3), type)),
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", "bar"), type)),
          () -> assertNotNull(ExpandoUtils.copyAttributes(Map.of("foo", LocalDateTime.now()), type))
      );
    }

    @Test
    public void shouldNotCompile() {
      record InvalidRecord(Map<String, Object> moreAttributes) {}
      class InvalidClass implements Expando {
        @Override
        public Map<String, Object> moreAttributes() {
          return Map.of();
        }
      }

      //ExpandoUtils.copyAttributes(Map.of(), InvalidRecord.class);  // should not compîle, does not implement Expando
      //ExpandoUtils.copyAttributes(Map.of(), InvalidClass.class);   // should not compile, not a record
    }
  }

  @Nested
  public class Q4 {
    @Test
    public void invoke() throws NoSuchMethodException {
      record Foo(int x, Map<String, Object> moreAttributes) implements Expando { }

      var getter = Foo.class.getMethod("x");
      assertEquals(3, ExpandoUtils.invoke(getter, new Foo(3, Map.of())));
    }

    @Test
    public void invokeShouldPropagateRuntimeException() throws NoSuchMethodException {
      record Foo(int x, String s, Map<String, Object> moreAttributes) implements Expando {
        public int x() {
          s.toString();  // should throw a NPE
          return 3;
        }
      }

      var getter = Foo.class.getMethod("x");
      assertThrows(NullPointerException.class, () -> ExpandoUtils.invoke(getter, new Foo(0, null, Map.of())));
    }

    @Test
    public void invokeShouldPropagateError() throws NoSuchMethodException {
      record Foo(String s, Map<String, Object> moreAttributes) implements Expando {
        public String s() {
          return s();  // call itself
        }
      }

      var getter = Foo.class.getMethod("s");
      assertThrows(StackOverflowError.class, () -> ExpandoUtils.invoke(getter, new Foo("foo", Map.of())));
    }

    @Test
    public void invokeShouldThrowAnAssertionErrorForAnException() throws NoSuchMethodException {
      record Foo(String s, Map<String, Object> moreAttributes) implements Expando {
        public int x() throws IOException {
          throw new IOException();
        }
      }

      var getter = Foo.class.getMethod("x");
      assertThrows(AssertionError.class, () -> ExpandoUtils.invoke(getter, new Foo("foo", Map.of())));
    }

    @Test
    public void invokeIllegalAccessShouldPropagateAnIllegalAccessError() throws NoSuchMethodException {
      record Foo(String s, Map<String, Object> moreAttributes) implements Expando {
        private int x() {
          return 42;
        }
      }

      var privateMethod = Foo.class.getDeclaredMethod("x");
      assertThrows(IllegalAccessError.class, () -> ExpandoUtils.invoke(privateMethod, new Foo("foo", Map.of())));
    }

    @Test
    public void invokeQualityOfImplementation() {
      var methods = Arrays.stream(ExpandoUtils.class.getDeclaredMethods())
          .filter(m -> m.getName().equals("invoke"))
          .toList();
      assertEquals(1, methods.size());
      var method = methods.getFirst();
      assertFalse(method.accessFlags().contains(AccessFlag.PUBLIC));
    }
  }

  @Nested
  public class Q5 {
    @Test
    public void personAsMap() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var jane = new Person("Jane", Map.of("age", 34));
      assertEquals(Map.of("name", "Jane", "age", 34), jane.asMap());
    }

    @Test
    public void catAsMap() {
      record Cat(String name, int age, Map<String, Object> moreAttributes) implements Expando {
        Cat {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Cat.class);
        }
      }

      var garfield = new Cat("Garfield", 12, Map.of("width", "a lot"));
      assertEquals(Map.of("name", "Garfield", "age", 12, "width", "a lot"), garfield.asMap());
    }

    @Test
    public void personEntrySet() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var jane = new Person("Jane", Map.of("age", 34, "eye", "green"));
      var map = jane.asMap();
      assertAll(
          () -> assertEquals(3, map.size()),
          () -> assertEquals(Set.of(Map.entry("name", "Jane"), Map.entry("age", 34), Map.entry("eye", "green")),
              map.entrySet())
      );
    }

    @Test
    public void personGet() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var jane = new Person("Jane", Map.of("age", 34, "eye", "green"));
      var map = jane.asMap();
      assertAll(
          () -> assertEquals("Jane", map.get("name")),
          () -> assertEquals(34, map.get("age")),
          () -> assertEquals("green", map.get("eye")),
          () -> assertNull(map.get("foo"))
      );
    }

    @Test
    public void personGetOrDefault() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var jane = new Person("Jane", Map.of("age", 34, "eye", "green"));
      var map = jane.asMap();
      assertAll(
          () -> assertEquals("Jane", map.getOrDefault("name", "")),
          () -> assertEquals(34, map.getOrDefault("age", "")),
          () -> assertEquals("green", map.getOrDefault("eye", "")),
          () -> assertEquals("", map.getOrDefault("foo", ""))
      );
    }

    @Test
    public void personContainsKey() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var jane = new Person("Jane", Map.of("age", 34, "eye", "green"));
      var map = jane.asMap();
      assertAll(
          () -> assertTrue(map.containsKey("name")),
          () -> assertTrue(map.containsKey("age")),
          () -> assertTrue(map.containsKey("eye")),
          () -> assertFalse(map.containsKey("foo"))
      );
    }

    @Test
    public void personAsMapIsUnmodifiable() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var ethan = new Person("Ethan", Map.of("age", 58, "run", "fast"));
      var map = ethan.asMap();
      assertAll(
          () -> assertThrows(UnsupportedOperationException.class, () -> map.put("foo", "bar")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.remove("age")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.replace("run", "slow")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.putAll(Map.of("foo", "bar"))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().removeAll(List.of("age"))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((s, o) -> "boom")),
          () -> assertThrows(UnsupportedOperationException.class, map::clear),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.entrySet().remove(Map.entry("age", 58))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.entrySet().add(Map.entry("age", 12))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.entrySet().addAll(List.of(Map.entry("age", 12)))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().remove()),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.entrySet().clear()),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().remove("age")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().add("age")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().addAll(List.of("age"))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().iterator().remove()),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.keySet().clear()),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.values().remove("Ethan")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.values().add("Ethan")),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.values().addAll(List.of("Ethan"))),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.values().iterator().remove()),
          () -> assertThrows(UnsupportedOperationException.class, () -> map.values().clear())
      );
    }

    @Test
    public void attrAsMapSizeIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(1_000_001, attr.asMap().size());
        }
      });
    }
  }

  @Nested
  public class Q6 {
    @Test
    public void attrAsMapGetIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(i, attr.asMap().get("" + i));
        }
      });
    }

    @Test
    public void attrAsMapGetOrDefaultIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(i, attr.asMap().getOrDefault("" + i, ""));
        }
      });
    }

    @Test
    public void attrAsMapContainsKeyIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertTrue(attr.asMap().containsKey("" + i));
        }
      });
    }

    @Test
    public void asMapGetGetOrDefaultContainsKeyPreconditions() {
      record NoAttr(Map<String, Object> moreAttributes) implements Expando {
        NoAttr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, NoAttr.class);
        }
      }

      assertAll(
          () -> assertThrows(NullPointerException.class, () -> new NoAttr(Map.of()).asMap().get(null)),
          () -> assertThrows(NullPointerException.class, () -> new NoAttr(Map.of()).asMap().getOrDefault(null, "")),
          () -> assertThrows(NullPointerException.class, () -> new NoAttr(Map.of()).asMap().containsKey(null))
      );
    }
  }

  @Nested
  public class Q7 {
    @Test
    public void attrAsMapForEachIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        attr.asMap().forEach((key, value) -> {
          if (key.equals("id")) {
            return;
          }
          assertEquals(key, "" + value);
        });
      });
    }

    @Test
    public void attrAsMapForEachPreconditions() {
      record NoAttr(Map<String, Object> moreAttributes) implements Expando {
        NoAttr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, NoAttr.class);
        }
      }

      assertThrows(NullPointerException.class, () -> new NoAttr(Map.of()).asMap().forEach(null));
    }
  }

  @Nested
  public class Q8 {
    @Test
    public void stream() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", Map.of("phone", "776-2323"));
      var keys = person.asMap().entrySet().stream().map(Map.Entry::getKey).collect(toSet());
      assertEquals(Set.of("name", "phone"), keys);
    }

    @Test
    public void streamEntries() {
      record Foo(int id1, int id2, int id3, int id4, int id5, Map<String, Object> moreAttributes) implements Expando {
        Foo { moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Foo.class); }
      }

      var foo = new Foo(10, 11, 12, 13, 14, Map.of("id0", 9, "id6", 15));
      var firsts = foo.asMap().entrySet().stream().collect(toSet());
      assertEquals(
          Set.of(Map.entry("id0", 9), Map.entry("id1", 10), Map.entry("id2", 11),Map.entry("id3", 12), Map.entry("id4", 13), Map.entry("id5", 14), Map.entry("id6", 15)),
          firsts);
    }

    @Test
    public void streamCountIsFast() {
      record Attr(int id, Map<String, Object> moreAttributes) implements Expando {
        Attr {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Attr.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new Attr(42, attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        for(var i = 0; i < 1_000_000; i++) {
          assertEquals(1_000_001, attr.asMap().entrySet().stream().count());
        }
      });
    }

    @Test
    public void streamALotOfAttributes() {
      record AttrOnly(Map<String, Object> moreAttributes) implements Expando {
        AttrOnly {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, AttrOnly.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new AttrOnly(attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        var set = attr.asMap().entrySet().stream().collect(toSet());
        assertEquals(attributes.entrySet(), set);
      });
    }

    @Test
    public void streamParallelUsesSeveralThreads() {
      record AttrOnly(Map<String, Object> moreAttributes) implements Expando {
        AttrOnly {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, AttrOnly.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_024).forEach(i -> attributes.put("" + i, i));
      var attr = new AttrOnly(attributes);
      var threads = new CopyOnWriteArraySet<Thread>();
      assertTimeoutPreemptively(Duration.ofMillis(1_000), () -> {
        var list = attr.asMap().entrySet().stream().parallel()
            .peek(__ -> threads.add(Thread.currentThread()))
            .toList();
        assertEquals(1_024, list.size());
      });
      assertTrue(threads.size() > 1);
    }

    @Test
    public void streamParallelALotOfAttributes() {
      record AttrOnly(Map<String, Object> moreAttributes) implements Expando {
        AttrOnly {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, AttrOnly.class);
        }
      }

      var attributes = new HashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new AttrOnly(attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        var set = attr.asMap().entrySet().stream().parallel().collect(toSet());
        assertEquals(attributes.entrySet(), set);
      });
    }

    @Test
    public void streamCharacteristics() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", Map.of("phone", "776-2323"));
      var spliterator = person.asMap().entrySet().stream().spliterator();
      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(IMMUTABLE)),
          () -> assertTrue(spliterator.hasCharacteristics(DISTINCT)),
          () -> assertTrue(spliterator.hasCharacteristics(NONNULL))
      );
    }

    @Test
    public void spliteratorCharacteristics() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", Map.of("phone", "776-2323"));
      var spliterator = person.asMap().entrySet().spliterator();
      assertAll(
          () -> assertTrue(spliterator.hasCharacteristics(IMMUTABLE)),
          () -> assertTrue(spliterator.hasCharacteristics(DISTINCT)),
          () -> assertTrue(spliterator.hasCharacteristics(NONNULL))
      );
    }
  }

  /*

  @Nested
  public class Q9 {
    @Test
    public void noCopy() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      Map<String, Object> map = Map.of("age", 12);
      var person = new Person("John", map);
      assertSame(map, person.moreAttributes);
    }

    @Test
    public void asMapFirsts() {
      record Foo(int id1, int id2, int id3, int id4, int id5, Map<String, Object> moreAttributes) implements Expando {
        Foo { moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Foo.class); }
      }

      var foo = new Foo(10, 11, 12, 13, 14, Map.of("id0", 9));
      var firsts = new ArrayList<>(foo.asMap().entrySet());
      assertEquals(
          List.of(Map.entry("id1", 10), Map.entry("id2", 11),Map.entry("id3", 12), Map.entry("id4", 13), Map.entry("id5", 14), Map.entry("id0", 9)),
          firsts);
    }

    @Test
    public void stream() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", Map.of("phone", "776-2323"));
      var keys = person.asMap().entrySet().stream().map(Map.Entry::getKey).toList();
      assertEquals(List.of("name", "phone"), keys);
    }

    @Test
    public void streamFirst() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", Map.of("phone", "776-2323"));
      var first = person.asMap().entrySet().stream().findFirst().orElseThrow();
      assertEquals(Map.entry("name", "Bruce"), first);
    }

    @Test
    public void streamFirsts() {
      record Foo(int id1, int id2, int id3, int id4, int id5, Map<String, Object> moreAttributes) implements Expando {
        Foo { moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Foo.class); }
      }

      var foo = new Foo(10, 11, 12, 13, 14, Map.of("id0", 9, "id6", 15));
      var firsts = foo.asMap().entrySet().stream().limit(5).toList();
      assertEquals(
          List.of(Map.entry("id1", 10), Map.entry("id2", 11),Map.entry("id3", 12), Map.entry("id4", 13), Map.entry("id5", 14)),
          firsts);
    }

    @Test
    public void streamALotOfAttributes() {
      record AttrOnly(Map<String, Object> moreAttributes) implements Expando {
        AttrOnly {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, AttrOnly.class);
        }
      }

      var attributes = new LinkedHashMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new AttrOnly(attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        var list = attr.asMap().entrySet().stream().toList();
        assertEquals(new ArrayList<>(attributes.entrySet()), list);
      });
    }

    @Test
    public void streamParallelALotOfAttributes() {
      record AttrOnly(Map<String, Object> moreAttributes) implements Expando {
        AttrOnly {
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, AttrOnly.class);
        }
      }

      var attributes = new TreeMap<String, Object>();
      range(0, 1_000_000).forEach(i -> attributes.put("" + i, i));
      var attr = new AttrOnly(attributes);
      assertTimeoutPreemptively(Duration.ofMillis(3_000), () -> {
        var list = attr.asMap().entrySet().stream().parallel().toList();
        assertEquals(new ArrayList<>(attributes.entrySet()), list);
      });
    }

    @Test
    public void streamCharacteristics() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", new ConcurrentSkipListMap<>(Map.of("phone", "776-2323")));
      var spliterator = person.asMap().entrySet().stream().spliterator();
      assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }

    @Test
    public void spliteratorCharacteristics() {
      record Person(String name, Map<String, Object> moreAttributes) implements Expando {
        Person {
          Objects.requireNonNull(name);
          moreAttributes = ExpandoUtils.copyAttributes(moreAttributes, Person.class);
        }
      }

      var person = new Person("Bruce", new ConcurrentSkipListMap<>(Map.of("phone", "776-2323")));
      var spliterator = person.asMap().entrySet().spliterator();
      assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
    }
  }

  */

}