package de.subcentral.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

import com.google.common.collect.ImmutableList;

public class CollectionUtil {
    public static enum SearchMode {
        CONTAIN, EQUAL
    };

    public static enum ReplaceMode {
        COMPLETE_LIST, MATCHED_SEQUENCE
    };

    private CollectionUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    @SafeVarargs
    public static <E> List<E> createArrayList(E... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public static <E> Collector<E, ImmutableList.Builder<E>, ImmutableList<E>> collectToImmutableList() {
        return Collector.of(ImmutableList::builder,
                ImmutableList.Builder::add,
                (ImmutableList.Builder<E> builder1, ImmutableList.Builder<E> builder2) -> builder1.addAll(builder2.build()),
                ImmutableList.Builder::build,
                Characteristics.CONCURRENT);
    }

    public static <E> int indexOf(List<E> c, E item, BiPredicate<? super E, ? super E> equalTester) {
        ListIterator<E> iter = c.listIterator();
        while (iter.hasNext()) {
            int index = iter.nextIndex();
            if (equalTester.test(iter.next(), item)) {
                return index;
            }
        }
        return -1;
    }

    public static <E> boolean contains(Collection<E> c, E item, BiPredicate<? super E, ? super E> equalTester) {
        Iterator<E> iter = c.iterator();
        while (iter.hasNext()) {
            if (equalTester.test(iter.next(), item)) {
                return true;
            }
        }
        return false;
    }

    public static <E> boolean containsAllIgnore(Collection<E> c1, Collection<E> c2, Collection<E> elemsToIgnore) {
        if (c1 == c2) {
            return true;
        }
        return copyWithout(c1, elemsToIgnore).containsAll(copyWithout(c2, elemsToIgnore));
    }

    public static boolean equalsIgnoreOrder(Collection<?> c1, Collection<?> c2) {
        if (c1 == c2) {
            return true;
        }
        if (c1.size() != c2.size()) {
            return false;
        }
        return c1.containsAll(c2) && c2.containsAll(c1);
    }

    public static <E> boolean equalsIgnore(Collection<E> c1, Collection<E> c2, Collection<E> elemsToIgnore) {
        if (c1 == c2) {
            return true;
        }
        return copyWithout(c1, elemsToIgnore).equals(copyWithout(c2, elemsToIgnore));
    }

    public static <E> List<E> getCommonElements(Collection<E> c1, Collection<E> c2) {
        List<E> commonElems = new ArrayList<>(c1);
        commonElems.retainAll(c2);
        return commonElems;
    }

    public static <E> boolean containsSequence(List<E> list, List<E> sequence) {
        for (int i = 0; i < list.size() && i + sequence.size() <= list.size(); i++) {
            List<E> sublist = list.subList(i, i + sequence.size());
            if (sublist.equals(sequence)) {
                return true;
            }
        }
        return false;
    }

    public static <E> boolean replaceSequences(List<E> list, List<E> sequence, List<E> replacementSequence, boolean ignoreOrder, boolean onlyReplaceFirst) {
        boolean changed = false;
        for (int i = 0; i < list.size() && i + sequence.size() <= list.size(); i++) {
            List<E> sublist = list.subList(i, i + sequence.size());
            if (ignoreOrder ? CollectionUtil.equalsIgnoreOrder(sublist, sequence) : sublist.equals(sequence)) {
                sublist.clear();
                list.addAll(i, replacementSequence);
                changed = true;
                if (onlyReplaceFirst) {
                    return true;
                }
            }
        }
        return changed;
    }

    public static <E> void transferElementsToHead(List<E> source, List<E> target, Collection<E> elementsToTransfer) {
        // iterate the source in reverse order so that the order in the target is correct
        for (int i = source.size() - 1; i >= 0; i--) {
            E sourceTag = source.get(i);
            if (elementsToTransfer.contains(sourceTag)) {
                target.add(0, sourceTag);
            }
        }
    }

    public static <E> boolean addAllToSortedList(List<E> list, Collection<? extends E> itemsToAdd, Comparator<? super E> comparator, BiPredicate<? super E, ? super E> distincter) {
        boolean changed = false;
        for (E item : itemsToAdd) {
            if (addToSortedList(list, item, comparator, distincter)) {
                changed = true;
            }
        }
        return changed;
    }

    public static <E> boolean addToSortedList(List<E> list, E item, Comparator<? super E> comparator, BiPredicate<? super E, ? super E> distincter) {
        int addIndex = list.size();
        boolean foundAddIndex = false;
        ListIterator<E> iter = list.listIterator();
        while (iter.hasNext()) {
            int currIndex = iter.nextIndex();
            E currItem = iter.next();
            if (distincter != null && distincter.test(currItem, item)) {
                return false;
            }
            if (!foundAddIndex) {
                int comparison = comparator.compare(currItem, item);
                if (comparison > 0) {
                    addIndex = currIndex;
                    // if we don't have to check the remaining items for distinct we can break the loop now
                    if (distincter == null) {
                        break;
                    }
                    foundAddIndex = true;
                }
                else if (comparison == 0) {
                    addIndex = currIndex + 1;
                    // if we don't have to check the remaining items for distinct we can break the loop now
                    if (distincter == null) {
                        break;
                    }
                    foundAddIndex = true;
                }
            }
        }
        list.add(addIndex, item);
        return true;
    }

    public static <E> E setInSortedList(List<E> list, int index, E item, Comparator<? super E> comparator, BiPredicate<? super E, ? super E> distincter) {
        if (distincter != null) {
            ListIterator<E> iter = list.listIterator();
            while (iter.hasNext()) {
                int currIndex = iter.nextIndex();
                E currItem = iter.next();
                if (index != currIndex && distincter.test(currItem, item)) {
                    return null;
                }
            }
        }
        E previousItem = list.set(index, item);
        list.sort(comparator);
        return previousItem;
    }

    public static <E> void updateList(List<E> origList, Collection<? extends E> updateList) {
        updateList(origList, updateList, true, true, true, Objects::equals);
    }

    public static <E> void updateList(List<E> origList, Collection<? extends E> updateList, boolean add, boolean replace, boolean remove) {
        updateList(origList, updateList, add, replace, remove, Objects::equals);
    }

    public static <E> void updateList(List<E> origList, Collection<? extends E> updateList, boolean add, boolean replace, boolean remove, BiPredicate<? super E, ? super E> comparer) {
        updateList(origList, updateList, add, replace, remove, comparer, (List<E> list, Collection<? extends E> itemsToAdd) -> origList.addAll(itemsToAdd));
    }

    public static <E extends Comparable<E>> void updateSortedList(List<E> origList, Collection<? extends E> updateList) {
        updateSortedList(origList, updateList, true, true, true, Objects::equals, ObjectUtil.getDefaultOrdering());
    }

    public static <E extends Comparable<E>> void updateSortedList(List<E> origList, Collection<? extends E> updateList, boolean add, boolean replace, boolean remove) {
        updateSortedList(origList, updateList, add, replace, remove, Objects::equals, ObjectUtil.getDefaultOrdering());
    }

    public static <E> void updateSortedList(List<E> origList,
            Collection<? extends E> updateList,
            boolean add,
            boolean replace,
            boolean remove,
            BiPredicate<? super E, ? super E> equalTester,
            Comparator<? super E> comparator) {
        updateList(origList, updateList, add, replace, remove, equalTester, (List<E> list, Collection<? extends E> itemsToAdd) -> addAllToSortedList(list, itemsToAdd, comparator, null));
    }

    private static <E> void updateList(List<E> origList,
            Collection<? extends E> updateList,
            boolean add,
            boolean replace,
            boolean remove,
            BiPredicate<? super E, ? super E> equalTester,
            BiConsumer<List<E>, Collection<? extends E>> adder) {
        List<E> itemsToRemove = null;
        List<E> itemsToAdd = null;
        for (E update : updateList) {
            boolean origListContainsUpdate = false;
            ListIterator<E> origIter = origList.listIterator();
            while (origIter.hasNext()) {
                E orig = origIter.next();
                if (equalTester.test(orig, update)) {
                    origListContainsUpdate = true;
                    if (remove) {
                        if (itemsToRemove == null) {
                            itemsToRemove = new ArrayList<>(origList);
                        }
                        itemsToRemove.remove(orig);
                    }
                    if (replace) {
                        origIter.set(update);
                    }
                    break;
                }
            }
            if (!origListContainsUpdate && add) {
                if (itemsToAdd == null) {
                    itemsToAdd = new ArrayList<>();
                }
                itemsToAdd.add(update);
            }
        }
        if (remove) {
            if (itemsToRemove != null) {
                origList.removeAll(itemsToRemove);
            }
            else {
                origList.clear();
            }
        }
        if (itemsToAdd != null) {
            adder.accept(origList, itemsToAdd);
        }
    }

    public static <E> boolean replace(List<E> list, List<E> searchElems, List<E> replacement, SearchMode queryMode, ReplaceMode replaceMode, boolean ignoreOrder) {
        switch (queryMode) {
            case CONTAIN:
                switch (replaceMode) {
                    case COMPLETE_LIST:
                        if (ignoreOrder) {
                            if (list.containsAll(searchElems)) {
                                list.clear();
                                list.addAll(replacement);
                                return true;
                            }
                        }
                        else {
                            if (containsSequence(list, searchElems)) {
                                list.clear();
                                list.addAll(replacement);
                                return true;
                            }
                        }
                        break;
                    case MATCHED_SEQUENCE:
                        return replaceSequences(list, searchElems, replacement, ignoreOrder, false);
                    default:
                        throw new AssertionError();
                }
                break;
            case EQUAL:
                if (ignoreOrder) {
                    if (equalsIgnoreOrder(list, searchElems)) {
                        list.clear();
                        list.addAll(replacement);
                        return true;
                    }
                }
                else {
                    if (list.equals(searchElems)) {
                        list.clear();
                        list.addAll(replacement);
                        return true;
                    }
                }
            default:
                throw new AssertionError();
        }
        return false;
    }

    private static <E> List<E> copyWithout(Collection<E> col, Collection<E> elementsToRemove) {
        List<E> list = new ArrayList<>(col);
        list.removeAll(elementsToRemove);
        return list;
    }
}
