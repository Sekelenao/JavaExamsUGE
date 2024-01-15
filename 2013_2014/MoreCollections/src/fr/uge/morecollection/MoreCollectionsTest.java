package fr.uge.morecollection;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("static-method")
public class MoreCollectionsTest {
  @Test(expected=NullPointerException.class)
  public void testAsMappedListListNull() {
    MoreCollections.asMappedList(null, Function.identity());
  }

  @Test(expected=NullPointerException.class)
  public void testAsMappedListFunctionNull() {
    MoreCollections.asMappedList(new ArrayList<Integer>(), null);
  }
  
  @Test(expected=IllegalStateException.class)
  public void testAsMappedListNotLinded() {
    MoreCollections.asMappedList(new LinkedList<String>(), Function.identity());
  }
  
  @Test(expected=IllegalStateException.class)
  public void testAsMappedListNotLinded2() {
    LinkedList<String> ll = new LinkedList<>();
    ll.add("foo");
    ll.add("bar");
    MoreCollections.asMappedList(ll.subList(0, 1), Function.identity());
  }

  @Test
  public void testAsMappedListIdentity() {
    ArrayList<String> al = new ArrayList<>();
    al.add("hello");
    al.add("world");
    List<String> list = MoreCollections.asMappedList(al, Function.identity());
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("hello", list.get(0));
    assertEquals("world", list.get(1));
  }
  
  @Test
  public void testAsMappedListView() {
    ArrayList<String> al = new ArrayList<>();
    al.add("foo");
    List<String> list = MoreCollections.asMappedList(al, Function.identity());
    al.add("bar");
    assertFalse(list.isEmpty());
    assertEquals(2, list.size());
    assertEquals("foo", list.get(0));
    assertEquals("bar", list.get(1));
  }
  
  @SuppressWarnings("unchecked")
  private Function<Object,Integer> mockTimes(int multipliers) {
    return (Function<Object,Integer>)(Proxy.newProxyInstance(Function.class.getClassLoader(),
        new Class<?>[]{Function.class},
        new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return multipliers * (Integer)args[0];
      }
    }));
  }
  
  @Test
  public void testAsMappedListTimes() {
    List<Integer> l = Arrays.asList(1, 17, 3);
    List<Integer> list = MoreCollections.asMappedList(l, mockTimes(2));
    assertFalse(list.isEmpty());
    assertEquals(3, list.size());
    assertEquals(2, (int)list.get(0));
    assertEquals(34, (int)list.get(1));
    assertEquals(6, (int)list.get(2));
  }
  
  @Test
  public void testAsMappedListIterator() {
    ArrayList<Object> al = new ArrayList<>();
    List<Integer> list = MoreCollections.asMappedList(al, mockTimes(2));
    ListIterator<Integer> it = list.listIterator(0);
    assertFalse(it.hasNext());
  }
  
  @Test
  public void testAsMappedListIterator2() {
    ArrayList<Integer> al = new ArrayList<>();
    al.add(45);
    List<Integer> list = MoreCollections.asMappedList(al.subList(0, 1), mockTimes(2));
    ListIterator<Integer> it = list.listIterator(list.size());
    assertFalse(it.hasNext());
    assertTrue(it.hasPrevious());
    it.previous();
    assertTrue(it.hasNext());
    assertFalse(it.hasPrevious());
  }
  
  @SuppressWarnings("unchecked")
  private Function<Object,String> mockToString() {
    return (Function<Object,String>)(Proxy.newProxyInstance(Function.class.getClassLoader(),
        new Class<?>[]{Function.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return args[0].toString();
          }
        }));
  }
  
  @Test
  public void testAsMappedListEmpty() {
    ArrayList<String> al = new ArrayList<>();
    List<CharSequence> list = MoreCollections.<CharSequence,CharSequence>asMappedList(al, mockToString());
    assertTrue(list.isEmpty());
  }
  
  @Test
  public void testAsMappedMappedList() {
    ArrayList<Double> al = new ArrayList<>();
    MoreCollections.asMappedList(MoreCollections.asMappedList(al, Function.identity()), Function.identity());
  }

  @Test(expected=NullPointerException.class)
  public void testAsMappedMappedCollectionNull() {
    MoreCollections.asMappedCollection(null, Function.identity());
  }
  
  @Test(expected=NullPointerException.class)
  public void testAsMappedMappedCollectionNull2() {
    MoreCollections.asMappedCollection(new ArrayList<String>(), null);
  }
  
  @Test
  public void testAsMappedMappedCollectionIdentity() {
    ArrayList<String> al = new ArrayList<>();
    al.add("hello");
    al.add("world");
    Collection<String> c = MoreCollections.asMappedCollection(al, Function.identity());
    assertFalse(c.isEmpty());
    assertEquals(2, c.size());
    Iterator<String> it = c.iterator();
    assertEquals("hello", it.next());
    assertEquals("world", it.next());
    assertFalse(it.hasNext());
  }
  
  @Test
  public void testAsMappedCollectionView() {
    ArrayList<String> al = new ArrayList<>();
    al.add("foo");
    Collection<String> collection = MoreCollections.asMappedCollection(al, Function.identity());
    al.add("bar");
    assertFalse(collection.isEmpty());
    assertEquals(2, collection.size());
    Iterator<String> iterator = collection.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("foo", iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals("bar", iterator.next());
    assertFalse(iterator.hasNext());
  }
  
  @Test
  public void testAsMappedCollectionHash() {
    HashSet<Object> hash = new HashSet<>();
    MoreCollections.asMappedCollection(hash, Function.identity());
  }
  
  @Test
  public void testAsMappedCollectionTimes() {
    List<Integer> l = Arrays.asList(-4, 8, 9);
    Collection<Integer> c = MoreCollections.asMappedCollection(l, mockTimes(2));
    assertFalse(c.isEmpty());
    assertEquals(3, c.size());
    Iterator<Integer> it = c.iterator();
    assertEquals(-8, (int)it.next());
    assertEquals(16, (int)it.next());
    assertEquals(18, (int)it.next());
    assertFalse(it.hasNext());
  }
  
  @Test
  public void testAsMappedCollectionToString() {
    ArrayList<Object> list = new ArrayList<>();
    Collections.addAll(list,  "foo", "bar", "baz");
    Collection<CharSequence> c = MoreCollections.asMappedCollection(list, mockToString());
    assertFalse(list.isEmpty());
    Iterator<CharSequence> it = c.iterator();
    it.next();
    it.remove();
    assertEquals(2, c.size());
    Iterator<CharSequence> it2 = c.iterator();
    assertEquals("bar", it2.next());
    assertEquals("baz", it2.next());
    assertFalse(it2.hasNext());
  }
  
  @Test
  public void testAsMappedCollectionSerializable() {
    Collection<Object> c = MoreCollections.asMappedCollection(new ArrayList<Object>(), Function.identity());
    assertTrue(c instanceof Serializable);
  }
  
  @Test
  public void testAsMappedCollectionSerializable2() {
    Collection<Object> c = MoreCollections.asMappedCollection(MoreCollections.asMappedList(new ArrayList<Object>(), Function.identity()), Function.identity());
    assertFalse(c instanceof Serializable);
  }

}