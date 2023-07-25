package de.xahrie.trues.api.datatypes.collections;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.xahrie.trues.api.database.query.Id;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class SortedList<E> extends AbstractList<E> {
  private final Comparator<E> comparator;
  private Set<E> data;

  @NotNull
  @Contract(" -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted() {
    return new SortedList<>(new LinkedHashSet<E>(), Comparator.naturalOrder());
  }

  @NotNull
  @Contract("_ -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted(@NotNull Stream<? extends E> c) {
    return new SortedList<>(c.toList(), Comparator.naturalOrder());
  }

  @NotNull
  @Contract("_ -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted(@NotNull Collection<? extends E> c) {
    return new SortedList<>(c, Comparator.naturalOrder());
  }

  @NotNull
  @Contract("_ -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted(Comparator<E> comparator) {
    return new SortedList<>(new LinkedHashSet<>(), comparator);
  }

  @NotNull
  @Contract("_, _ -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted(@NotNull Collection<? extends E> c, Comparator<E> comparator) {
    return new SortedList<E>(c, comparator);
  }

  @NotNull
  @Contract("_, _ -> new")
  public static <E extends Comparable<E>> SortedList<E> sorted(@NotNull Stream<? extends E> c, Comparator<E> comparator) {
    return new SortedList<E>(c.toList(), comparator);
  }

  @NotNull
  @Contract(" -> new")
  public static <E> SortedList<E> of() {
    return new SortedList<>(new LinkedHashSet<>(), null);
  }

  @NotNull
  @Contract("_ -> new")
  @SafeVarargs
  public static <E> SortedList<E> of(E... elements) {
    return SortedList.of(List.of(elements));
  }

  @NotNull
  @Contract("_ -> new")
  public static <E> SortedList<E> of(@NotNull Stream<? extends E> c) {
    return new SortedList<>(c.toList(), null);
  }

  @NotNull
  @Contract("_ -> new")
  public static <E> SortedList<E> of(@NotNull Collection<? extends E> c) {
    return new SortedList<>(c, null);
  }

  private SortedList(Collection<? extends E> collection, Comparator<E> comparator) {
    this.data = collection == null ? new LinkedHashSet<>() : new LinkedHashSet<>(collection);
    this.comparator = comparator;
    if (comparator != null) sort();
  }

  @Override
  public void add(int index, E element) {
    data.add(element);
    if (comparator != null) sort();
  }

  @Override
  public boolean remove(Object o) {
    return data.remove(o);
  }

  @Override
  public E get(int index) {
    return data.stream().toList().get(index);
  }

  public E getOr(int index, E defaultValue) {
    return index >= size() ? defaultValue : get(index);
  }

  @Override
  public int size() {
    return data.size();
  }

  public void sort() {
    if (size() == 0) return;
    if (comparator != null) this.data = data.stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new));
    else if (get(0) instanceof Comparable) this.data = new LinkedHashSet<>(new TreeSet<>(data));
    else if (get(0) instanceof Id) {
      this.data = data.stream().sorted(Comparator.comparingInt(o -> ((Id) o).getId())).collect(Collectors.toCollection(LinkedHashSet::new));
    }
  }

  public SortedList<E> reverse() {
    final ArrayList<E> list = new ArrayList<>(data);
    Collections.reverse(list);
    this.data = new LinkedHashSet<>(list);
    return this;
  }

  @Override
  public void clear() {
    data.clear();
  }
}
