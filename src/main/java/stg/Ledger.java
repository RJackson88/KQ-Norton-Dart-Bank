package stg;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by rickjackson on 3/2/17.
 */
public class Ledger<E> implements List<E> {
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    int modCount = 0;
    int size = 0;
    Node<E> first;
    Node<E> last;
    
    public Ledger() {
        
    }
    
    public Ledger(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    /* Base Linked List Methods */
    
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;
        
        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
    
    Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> n = first;
            
            for (int i = 0; i < index; i++) {
                n = n.next;
            }
            return n;
        } else {
            Node<E> n = last;
            
            for (int i = size - 1; i > index; i--) {
                n = n.prev;
            }
            return n;
        }
    }
    
    /* Modification Operations */
    
    public boolean add(E e) {
        linkLast(e);
        return true;
    }
    
    public void add(int index, E element) {
        checkPositionIndex(index);
        
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
    }
    
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> n = node(index);
        E oldVal = n.item;
        n.item = element;
        return oldVal;
    }
    
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }
    
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> n = first; n != null; n = n.next) {
                if (n.item == null) {
                    unlink(n);
                    return true;
                }
            }
        } else {
            for (Node<E> n = first; n != null; n = n.next) {
                if (o.equals(n.item)) {
                    unlink(n);
                    return true;
                }
            }
        }
        return false;
    }
    
    /* Bulk Operations */
    
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        
        if (c.size() == 0) {
            return false;
        }
        Node<E> pred;
        Node<E> succ;
        
        if (index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }
        
        for (E e : c) {
            Node<E> newNode = new Node<>(pred, e, null);
            
            if (pred == null) {
                first = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;
        }
        
        if (succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }
        size += c.size();
        modCount++;
        return true;
    }
    
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> itr = iterator();
        
        while (itr.hasNext()) {
            if (c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> itr = iterator();
        
        while (itr.hasNext()) {
            if (!c.contains(itr.next())) {
                itr.remove();
                modified = true;
            }
        }
        return modified;
    }
    
    public void clear() {
        for (Node<E> n = first; n != null; ) {
            Node<E> next = n.next;
            n.item = null;
            n.next = null;
            n.prev = null;
            n = next;
        }
        last = null;
        first = last;
        size = 0;
        modCount++;
    }
    
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (Node<E> n = first; n != null; n = n.next)
            arr[i++] = n.item;
        return arr;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i = 0;
        Object[] arr = a;
        
        for (Node<E> n = first; n != null; n = n.next) {
            arr[i++] = n.item;
        }
        
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T[] finishToArray(T[] arr, Iterator<?> itr) {
        int i = arr.length;
        
        while (itr.hasNext()) {
            int cap = arr.length;
            
            if (i == cap) {
                int newCap = cap + (cap >> 1) + 1;
                
                if (newCap - MAX_ARRAY_SIZE > 0) {
                    newCap = hugeCapacity(cap + 1);
                }
                arr = Arrays.copyOf(arr, newCap);
            }
            arr[i++] = (T) itr.next();
        }
        return (i == arr.length) ? arr : Arrays.copyOf(arr, i);
    }
    
    /* Linking Operations */
    
    private void linkFirst(E e) {
        final Node<E> f = this.first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }
        size++;
        modCount++;
    }
    
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
        modCount++;
    }
    
    void linkBefore(E e, Node<E> succ) {
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        
        if (pred == null) {
            first = newNode;
        } else {
            pred.next = newNode;
        }
        size++;
        modCount++;
    }
    
    private E unlinkFirst(Node<E> f) {
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null;
        first = next;
        
        if (next == null) {
            last = null;
        } else {
            next.prev = null;
        }
        size--;
        modCount++;
        return element;
    }
    
    public E unlinkLast(Node<E> l) {
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null;
        last = prev;
        
        if (prev == null) {
            first = null;
        } else {
            prev.next = null;
        }
        size--;
        modCount++;
        return element;
    }
    
    E unlink(Node<E> n) {
        final E element = n.item;
        final Node<E> next = n.next;
        final Node<E> prev = n.prev;
        
        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            n.prev = null;
        }
        
        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            n.next = null;
        }
        n.item = null;
        size--;
        modCount++;
        return element;
    }
    
    public E getFirst() {
        final Node<E> f = first;
        
        if (f == null) {
            throw new NoSuchElementException();
        }
        return f.item;
    }
    
    public E getLast() {
        final Node<E> l = last;
        
        if (l == null) {
            throw new NoSuchElementException();
        }
        return l.item;
    }
    
    public E removeFirst() {
        final Node<E> f = first;
        
        if (f == null) {
            throw new NoSuchElementException();
        }
        return unlinkFirst(f);
    }
    
    public E removeLast() {
        final Node<E> l = last;
        
        if (l == null) {
            throw new NoSuchElementException();
        }
        return unlinkLast(l);
    }
    
    public void addFirst(E e) {
        linkFirst(e);
    }
    
    public void addLast(E e) {
        linkLast(e);
    }
    
    /* Query Operations */
    
    public int size() {
        return size;
    }
    
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError("Required array size too large");
        }
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE
                                              : MAX_ARRAY_SIZE;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }
    
    public int indexOf(Object o) {
        int index = 0;
        
        if (o == null) {
            for (Node<E> n = first; n != null; n = n.next) {
                if (n.item == null) {
                    return index;
                }
                index++;
            }
        } else {
            for (Node<E> n = first; n != null; n = n.next) {
                if (o.equals(n.item)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }
    
    public int lastIndexOf(Object o) {
        int index = size;
        
        if (o == null) {
            for (Node<E> n = last; n != null; n = n.prev) {
                index--;
                
                if (n.item == null) {
                    return index;
                }
            }
        } else {
            for (Node<E> n = last; n != null; n = n.prev) {
                index--;
                
                if (o.equals(n.item)) {
                    return index;
                }
            }
        }
        return -1;
    }
    
    /* Queue Operations */
    
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }
    
    public E element() {
        return getFirst();
    }
    
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }
    
    public E remove() {
        return removeFirst();
    }
    
    public boolean offer(E e) {
        return add(e);
    }
    
    // Deque Operations
    
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }
    
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }
    
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }
    
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }
    
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }
    
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }
    
    public void push(E e) {
        addFirst(e);
    }
    
    public E pop() {
        return removeFirst();
    }
    
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }
    
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> n = last; n != null; n = n.prev) {
                if (n.item == null) {
                    unlink(n);
                    return true;
                }
            }
        } else {
            for (Node<E> n = last; n != null; n = n.prev) {
                if (o.equals(n.item)) {
                    unlink(n);
                    return true;
                }
            }
        }
        return false;
    }
    
    /* Iterators */
    
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }
    
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }
    
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }
    
    @Override
    public Spliterator<E> spliterator() {
        return new LedgerSpliterator<E>(this, -1, 0);
    }
    
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess ?
                new RandomAccessSubList<>(this, fromIndex, toIndex) :
                new SubList<>(this, fromIndex, toIndex));
    }
    
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }
    
    private class Itr implements Iterator<E> {
        int cursor = 0;
        int lastRet = -1;
        int expectedModCount = modCount;
        
        public boolean hasNext() {
            return cursor != size;
        }
        
        public E next() {
            checkForComodification();
            
            try {
                int i = cursor;
                E next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }
        
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();
            
            try {
                Ledger.this.remove(lastRet);
                
                if (lastRet < cursor) {
                    cursor--;
                }
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
        
        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        private int expectedModCount = modCount;
        
        ListItr(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }
        
        public boolean hasNext() {
            return nextIndex < size;
        }
        
        public E next() {
            checkForComodification();
            
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }
        
        public boolean hasPrevious() {
            return nextIndex > 0;
        }
        
        public E previous() {
            checkForComodification();
            
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }
        
        public int nextIndex() {
            return nextIndex;
        }
        
        public int previousIndex() {
            return nextIndex - 1;
        }
        
        public void remove() {
            checkForComodification();
            
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            
            if (next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex--;
            }
            lastReturned = null;
            expectedModCount++;
        }
        
        public void set(E e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            checkForComodification();
            lastReturned.item = e;
        }
        
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            
            if (next == null) {
                linkLast(e);
            } else {
                linkBefore(e, next);
            }
            nextIndex++;
            expectedModCount++;
        }
        
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }
        
        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    
    static final class LedgerSpliterator<E> implements Spliterator<E> {
        // batch array size increment
        static final int BATCH_UNIT = 1 << 10;
        // max batch array size;
        static final int MAX_BATCH = 1 << 25;
        // null okay unless traversed
        final Ledger<E> ledger;
        // current node; null until initialized
        Node<E> current;
        // size estimate; -1 until first needed
        int est;
        // initialized when est set
        int expectedModCount;
        // batch size for splits
        int batch;
        
        LedgerSpliterator(Ledger<E> ledger, int est, int expectedModCount) {
            this.ledger = ledger;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }
        
        final int getEst() {
            int s = est;
            final Ledger<E> l = ledger;
            
            if (s < 0) {
                if (l == null) {
                    est = 0;
                    s = est;
                } else {
                    expectedModCount = l.modCount;
                    current = l.first;
                    est = l.size;
                    s = est;
                }
            }
            return s;
        }
        
        public long estimateSize() { return (long) getEst(); }
        
        public Spliterator<E> trySplit() {
            Node<E> node;
            int s = getEst();
            
            if (s > 1 && (node = current) != null) {
                int n = batch + BATCH_UNIT;
                
                if (n > s) {
                    n = s;
                }
                if (n > MAX_BATCH) {
                    n = MAX_BATCH;
                }
                Object[] arr = new Object[n];
                int j = 0;
                
                do {
                    arr[j++] = node.item;
                } while ((node = node.next) != null && j < n);
                current = node;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(
                        arr, 0, j, Spliterator.ORDERED);
            }
            return null;
        }
        
        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> node = current;
            int n = getEst();
            
            if (action == null) {
                throw new NullPointerException();
            }
            if (n > 0 && node != null) {
                current = null;
                est = 0;
                
                do {
                    E e = node.item;
                    node = node.next;
                    action.accept(e);
                } while (node != null & --n > 0);
            }
            if (ledger.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
        
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<E> node = current;
            
            if (getEst() > 0 && node != null) {
                --est;
                E e = node.item;
                current = node.next;
                action.accept(e);
                
                if (ledger.modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }
        
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }
    
    /* Comparison and Hashing */
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        
        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }
    
    public int hashCode() {
        int hashCode = 1;
        
        for (E e : this) {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }
    
    @SuppressWarnings("unchecked")
    private Ledger<E> superClone() {
        try {
            return (Ledger<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
    
    public Object clone() {
        Ledger<E> clone = superClone();
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;
        
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);
        
        return clone;
    }
    
    /* Positional Access Checks */
    
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> itr = listIterator(fromIndex);
        
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            itr.next();
            itr.remove();
        }
    }
    
    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }
    
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }
    
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
    
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }
    
    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
    
    /* String Conversion */
    
    public String toString() {
        Iterator<E> itr = iterator();
        
        if (!itr.hasNext()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        
        for (; ; ) {
            E e = itr.next();
            sb.append((e == this) ? "(this Collection)" : e);
            
            if (!itr.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
}

class SubList<E> extends Ledger<E> {
    private final Ledger<E> ledger;
    private final int offset;
    private int size;
    
    SubList(Ledger<E> ledger, int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > ledger.size()) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        }
        this.ledger = ledger;
        this.offset = fromIndex;
        this.size = toIndex - fromIndex;
        this.modCount = this.ledger.modCount;
    }
    
    public E set(int index, E element) {
        rangeCheck(index);
        checkForComodification();
        return ledger.set(index + offset, element);
    }
    
    public E get(int index) {
        rangeCheck(index);
        checkForComodification();
        return ledger.get(index + offset);
    }
    
    public int size() {
        checkForComodification();
        return size;
    }
    
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        checkForComodification();
        ledger.add(index + offset, element);
        this.modCount = ledger.modCount;
        size++;
    }
    
    public E remove(int index) {
        rangeCheck(index);
        checkForComodification();
        E result = ledger.remove(index + offset);
        this.modCount = ledger.modCount;
        size--;
        return result;
    }
    
    protected void removeRange(int fromIndex, int toIndex) {
        checkForComodification();
        ledger.removeRange(fromIndex + offset, toIndex + offset);
        this.modCount = ledger.modCount;
        size -= (toIndex - fromIndex);
    }
    
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }
    
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        int cSize = c.size();
        
        if (cSize == 0) {
            return false;
        }
        
        checkForComodification();
        ledger.addAll(offset + index, c);
        this.modCount = ledger.modCount;
        size += cSize;
        return true;
    }
    
    public Iterator<E> iterator() {
        return listIterator();
    }
    
    public ListIterator<E> listIterator(final int index) {
        checkForComodification();
        rangeCheckForAdd(index);
        
        return new ListIterator<E>() {
            private final ListIterator<E> i = ledger.listIterator(index
                                                                  + offset);
            
            public boolean hasNext() {
                return nextIndex() < size;
            }
            
            public E next() {
                if (hasNext()) {
                    return i.next();
                } else {
                    throw new NoSuchElementException();
                }
            }
            
            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }
            
            public E previous() {
                if (hasPrevious()) {
                    return i.previous();
                } else {
                    throw new NoSuchElementException();
                }
            }
            
            public int nextIndex() {
                return i.nextIndex() - offset;
            }
            
            public int previousIndex() {
                return i.previousIndex() - offset;
            }
            
            public void remove() {
                i.remove();
                SubList.this.modCount = ledger.modCount;
                size--;
            }
            
            public void set(E e) {
                i.set(e);
            }
            
            public void add(E e) {
                i.add(e);
                SubList.this.modCount = ledger.modCount;
                size++;
            }
        };
    }
    
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<>(this, fromIndex, toIndex);
    }
    
    private void rangeCheck(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }
    
    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }
    
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }
    
    private void checkForComodification() {
        if (this.modCount != ledger.modCount) {
            throw new ConcurrentModificationException();
        }
    }
}

class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
    
    RandomAccessSubList(Ledger<E> ledger, int fromIndex, int toIndex) {
        super(ledger, fromIndex, toIndex);
    }
    
    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }
}
