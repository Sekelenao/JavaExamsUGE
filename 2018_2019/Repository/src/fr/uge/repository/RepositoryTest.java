package fr.uge.repository;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class RepositoryTest {
  @Test @Tag("Q1")
  public void testIteratorAndHasNext() {
    assertAll(
      () -> assertFalse(Repository.and(List.<Integer>of().iterator(), List.<Integer>of().iterator()).hasNext()),
      () -> assertFalse(Repository.and(List.<Integer>of().iterator(), List.of(1).iterator()).hasNext()),
      () -> assertFalse(Repository.and(List.<Integer>of(1).iterator(), List.<Integer>of().iterator()).hasNext()),
      () -> assertFalse(Repository.and(List.of(1).iterator(), List.of(2).iterator()).hasNext()),
      () -> assertTrue(Repository.and(List.of(3).iterator(), List.of(3).iterator()).hasNext()),
      () -> assertFalse(Repository.and(List.of(1, 2, 3).iterator(), List.of(4, 5, 6).iterator()).hasNext()),
      () -> assertTrue(Repository.and(List.of(1, 2, 3).iterator(), List.of(-5, -4, -1, 3).iterator()).hasNext())
      );
  }
  
  @Test @Tag("Q1")
  public void testIteratorAndNext() {
    assertAll(
      () -> assertThrows(NoSuchElementException.class, () -> Repository.and(List.<Integer>of().iterator(), List.<Integer>of().iterator()).next()),
      () -> assertThrows(NoSuchElementException.class, () -> Repository.and(List.<Integer>of().iterator(), List.of(1).iterator()).next()),
      () -> assertThrows(NoSuchElementException.class, () -> Repository.and(List.of(1).iterator(), List.of(2).iterator()).next()),
      () -> assertEquals(3, (int)Repository.and(List.of(3).iterator(), List.of(3).iterator()).next()),
      () -> assertThrows(NoSuchElementException.class, () -> Repository.and(List.of(1, 2, 3).iterator(), List.of(4, 5, 6).iterator()).next()),
      () -> assertEquals(3, (int)Repository.and(List.of(1, 2, 3).iterator(), List.of(-5, -4, -1, 3).iterator()).next())
      );
  }
  @Test @Tag("Q1")
  public void testIteratorWith1000Elements() {
    var it = Repository.and(IntStream.range(0, 1_000).filter(i -> i % 7 == 0).iterator(), IntStream.range(0, 1_000).filter(i -> i % 13 == 0).iterator());
    var list = new ArrayList<Integer>();
    it.forEachRemaining(list::add);
    assertEquals(IntStream.range(0, 1_000).filter(i -> i % 7 == 0 && i % 13 == 0).boxed().collect(toList()), list);
  }
  @Test @Tag("Q1")
  public void testIteratorALot() {
    var it = Repository.and(IntStream.range(0, 1_000_000).filter(i -> i % 7 == 0).iterator(), IntStream.range(0, 1_000_000).filter(i -> i % 13 == 0).iterator());
    var list = new ArrayList<Integer>();
    it.forEachRemaining(list::add);
    assertEquals(IntStream.range(0, 1_000_000).filter(i -> i % 7 == 0 && i % 13 == 0).boxed().collect(toList()), list);
  }

  @Test @Tag("Q2")
  public void testAddSelectorEmpty() {
    var repository = new Repository<Integer>();
    var selector = repository.addSelector(__ -> true);
    assertEquals("[]", selector.toString());
  }
  @Test @Tag("Q2")
  public void testAddSelectorFirst() {
    var repository = new Repository<String>();
    var selector = repository.addSelector(s -> s.length() == 3);
    repository.add("dad");
    repository.add("franck");
    repository.add("bob");
    assertEquals("[0, 2]", selector.toString());
  }
  @Test @Tag("Q2")
  public void testAddSelectorLast() {
    var repository = new Repository<String>();
    repository.add("dad");
    repository.add("franck");
    repository.add("bob");
    var selector = repository.addSelector(s -> s.length() == 3);
    assertEquals("[0, 2]", selector.toString());
  }
  @Test @Tag("Q2")
  public void testAddSelectorMiddle() {
    var repository = new Repository<String>();
    repository.add("dad");
    repository.add("franck");
    var selector = repository.addSelector(s -> s.length() == 3);
    repository.add("bob");
    assertEquals("[0, 2]", selector.toString());
  }
  @Test @Tag("Q2")
  public void testAddSelectorExplicitTypedLambda() {
    var repository = new Repository<String>();
    var selector = repository.addSelector((Object o) -> o != null);
    assertEquals("[]", selector.toString());
  }
  @Test @Tag("Q2")
  public void testAddNull() {
    assertThrows(NullPointerException.class, () -> new Repository<>().add(null));
  }
  @Test @Tag("Q2")
  public void testAddSelectorNull() {
    assertThrows(NullPointerException.class, () -> new Repository<>().addSelector(null));
  }

  @Test @Tag("Q3")
  public void testAddSelectorToString() {
    var repository = new Repository<Integer>();
    var selector = repository.addSelector(__ -> true);
    assertEquals("[]", selector.toString());
    repository.add(0);
    assertEquals("[0]", selector.toString());
    repository.add(1);
    assertEquals("[0, 1]", selector.toString());
    repository.add(2);
    assertEquals("[0, 1, 2]", selector.toString());
    repository.add(3);
    assertEquals("[0, 1, 2, 3]", selector.toString());
    repository.add(4);
    assertEquals("[0, 1, 2, 3, 4]", selector.toString());
    repository.add(5);
    assertEquals("[0, 1, 2, 3, ..., 5]", selector.toString());
    repository.add(6);
    assertEquals("[0, 1, 2, 3, ..., 6]", selector.toString());
  }

  @Test @Tag("Q4")
  public void testIndexOf() {
    var index = Repository.Index.of(List.of(1, 2, 3));
    var list = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list::add);
    assertEquals(List.of(1, 2, 3), list);
  }
  @Test @Tag("Q4")
  public void testIndexOfTwoIterators() {
    var index = Repository.Index.of(List.of(2, 4, 8));
    var list1 = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list1::add);
    var list2 = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list2::add);
    assertEquals(list1, list2);
  }
  @Test @Tag("Q4")
  public void testIndexOfEmpty() {
    var index = Repository.Index.of(List.of());
    index.iterator().forEachRemaining(__ -> fail("empty"));
  }
  @Test @Tag("Q4")
  public void testIndexOfnull() {
    assertThrows(NullPointerException.class, () -> Repository.Index.of(null));
  }
  @Test @Tag("Q4")
  public void testIndexAnd() {
    var index1 = Repository.Index.of(List.of(1, 2, 3, 4));
    var index2 = Repository.Index.of(List.of(2, 4, 7));
    var index = index1.and(index2);
    var list = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list::add);
    assertEquals(List.of(2, 4), list);
  }
  @Test @Tag("Q4")
  public void testIndexAndTwoIterators() {
    var index = Repository.Index.of(List.of(2, 4, 8)).and(Repository.Index.of(List.of(2, 4, 6, 8, 10)));
    var list1 = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list1::add);
    var list2 = new ArrayList<Integer>();
    index.iterator().forEachRemaining(list2::add);
    assertEquals(list1, list2);
  }
  @Test @Tag("Q4")
  public void testIndexAndNull() {
    var index = Repository.Index.of(List.of());
    assertThrows(NullPointerException.class, () -> index.and(null));
  }

  @Test @Tag("Q5")
  public void testQueryToStream() {
    var repository = new Repository<String>();
    repository.add("foo");
    repository.add("baz");
    repository.add("booz");
    var query = repository.createQuery();
    assertEquals(List.of("foo", "baz", "booz"), query.toStream().collect(toList()));
  }
  @Test @Tag("Q5")
  public void testQueryToStreamEmpty() {
    var repository = new Repository<String>();
    var query = repository.createQuery();
    assertEquals(0, query.toStream().count());
  }
  @Test @Tag("Q5")
  public void testQueryToStreamALot() {
    var repository = new Repository<Integer>();
    IntStream.range(0, 1_000_000).forEach(repository::add);
    var query = repository.createQuery();
    assertEquals(1_000_000, query.toStream().count());
  }
  
  @Test @Tag("Q6")
  public void testQuerySelector() {
    var repository = new Repository<String>();
    repository.add("foo");
    repository.add("baz");
    repository.add("booz");
    var selector = repository.addSelector(s -> s.contains("oo"));
    var query = repository.createQuery();
    assertTimeout(Duration.ofMillis(3_000),
    		() -> assertEquals(List.of("foo", "booz"), query.select(selector).toStream().collect(toList())));
  }
  @Test @Tag("Q6")
  public void testQuerySelectorNull() {
    var repository = new Repository<String>();
    var query = repository.createQuery();
    assertThrows(NullPointerException.class, () -> query.select(null));
  }
  @Test @Tag("Q6")
  public void testQuerySeveralSelectors() {
    var repository = new Repository<Integer>();
    IntStream.range(0, 1_000_000).forEach(repository::add);
    var selector7 = repository.addSelector(i -> i % 7 == 0);
    var selector13 = repository.addSelector(i -> i % 13 == 0);
    var selector17 = repository.addSelector(i -> i % 17 == 0);
    var query = repository.createQuery();
    var expected = IntStream.range(0, 1_000_000).filter(i -> i % 7 == 0 && i % 13 == 0 && i % 17 == 0).boxed().collect(toList());
    assertTimeout(Duration.ofMillis(3_000),
    		() -> assertEquals(expected, query.select(selector7).select(selector13).select(selector17).toStream().collect(toList())));
  }

  @Test @Tag("Q7")
  public void testQuerySelectorALot() {
    var repository = new Repository<Integer>();
    IntStream.range(0, 10_000_000).forEach(repository::add);
    var selector = repository.addSelector(i -> i == 777);
    var query = repository.createQuery();
    assertTimeout(Duration.ofMillis(30), () -> assertEquals(1, query.select(selector).toStream().count()));
  }
  @Test @Tag("Q7")
  public void testQuerySeveralSelectorsFastEnough() {
    var repository = new Repository<Integer>();
    IntStream.range(0, 10_000_000).forEach(repository::add);
    var selector7 = repository.addSelector(i -> i % 7 == 0);
    var selector13 = repository.addSelector(i -> i % 13 == 0);
    var selector21 = repository.addSelector(i -> i == 21);
    var query = repository.createQuery();
    assertTimeout(Duration.ofMillis(30), () -> assertEquals(List.<Integer>of(), query.select(selector7).select(selector13).select(selector21).toStream().collect(toList())));
  }

}