import java.util.*;

public class NonBlockingArrayList<E> extends ArrayList<E> {


    final transient Object lock = new Object();
    private transient volatile Object[] array;

   
    final Object[] getArray() {
        return array;
    }
    
    final void setArray(Object[] a) {
        array = a;
    }

    public  NonBlockingArrayList() {
        setArray(new Object[0]);
    }

    public  NonBlockingArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }

   
    public int size() {
        return getArray().length;
    }

    
    public boolean isEmpty() {
        return size() == 0;
    }

    private static int indexOfRange(Object o, Object[] es, int from, int to) {
        if (o == null) {
            for (int i = from; i < to; i++)
                if (es[i] == null)
                    return i;
        } else {
            for (int i = from; i < to; i++)
                if (o.equals(es[i]))
                    return i;
        }
        return -1;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @SuppressWarnings("unchecked")
    static <E> E elementAt(Object[] a, int index) {
        return (E) a[index];
    }


    public E get(int index) {
        return elementAt(getArray(), index);
    }
    
    public E set(int index, E element) {
        synchronized (lock) {
            Object[] es = getArray();
            E oldValue = elementAt(es, index);

            if (oldValue != element) {
                es = es.clone();
                es[index] = element;
            }
            setArray(es);
            return oldValue;
        }
    }

    public boolean add(E e) {
        synchronized (lock) {
            Object[] es = getArray();
            int len = es.length;
            es = Arrays.copyOf(es, len + 1);
            es[len] = e;
            setArray(es);
            return true;
        }
    }

    public void add(int index, E element) {
        synchronized (lock) {
            Object[] es = getArray();
            int len = es.length;
            if (index > len || index < 0)
                return;
            Object[] newElements;
            int numMoved = len - index;
            if (numMoved == 0)
                newElements = Arrays.copyOf(es, len + 1);
            else {
                newElements = new Object[len + 1];
                System.arraycopy(es, 0, newElements, 0, index);
                System.arraycopy(es, index, newElements, index + 1,
                        numMoved);
            }
            newElements[index] = element;
            setArray(newElements);
        }
    }

    public E remove(int index) {
        synchronized (lock) {
            Object[] es = getArray();
            int len = es.length;
            E oldValue = elementAt(es, index);
            int numMoved = len - index - 1;
            Object[] newElements;
            if (numMoved == 0)
                newElements = Arrays.copyOf(es, len - 1);
            else {
                newElements = new Object[len - 1];
                System.arraycopy(es, 0, newElements, 0, index);
                System.arraycopy(es, index + 1, newElements, index,
                        numMoved);
            }
            setArray(newElements);
            return oldValue;
        }
    }

    public boolean remove(Object o) {
        Object[] snapshot = getArray();
        int index = indexOfRange(o, snapshot, 0, snapshot.length);
        return index >= 0 && remove(o, snapshot, index);
    }

    private boolean remove(Object o, Object[] snapshot, int index) {
        synchronized (lock) {
            Object[] current = getArray();
            int len = current.length;
            if (snapshot != current) findIndex: {
                int prefix = Math.min(index, len);
                for (int i = 0; i < prefix; i++) {
                    if (current[i] != snapshot[i]
                            && Objects.equals(o, current[i])) {
                        index = i;
                        break findIndex;
                    }
                }
                if (index >= len)
                    return false;
                if (current[index] == o)
                    break findIndex;
                index = indexOfRange(o, current, index, len);
                if (index < 0)
                    return false;
            }
            Object[] newElements = new Object[len - 1];
            System.arraycopy(current, 0, newElements, 0, index);
            System.arraycopy(current, index + 1,
                    newElements, index,
                    len - index - 1);
            setArray(newElements);
            return true;
        }
    }

    public void clear() {
        synchronized (lock) {
            setArray(new Object[0]);
        }
    }

    public Iterator<E> iterator() {
        return new   NonBlockingArrayList.COWIterator<E>(getArray(), 0);
    }

    public ListIterator<E> listIterator() {
        return new   NonBlockingArrayList.COWIterator<E>(getArray(), 0);
    }

    public ListIterator<E> listIterator(int index) {
        Object[] es = getArray();
        int len = es.length;
        if (index < 0 || index > len)
            return null;

        return new   NonBlockingArrayList.COWIterator<E>(es, index);
    }



    static final class COWIterator<E> implements ListIterator<E> {
        private final Object[] snapshot;
        private int cursor;

        COWIterator(Object[] es, int initialCursor) {
            cursor = initialCursor;
            snapshot = es;
        }

        public boolean hasNext() {
            return cursor < snapshot.length;
        }

        public boolean hasPrevious() {
            return cursor > 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (! hasNext())
                throw new NoSuchElementException();
            return (E) snapshot[cursor++];
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            if (! hasPrevious())
                throw new NoSuchElementException();
            return (E) snapshot[--cursor];
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        public void set(E e) {
            throw new UnsupportedOperationException();
        }
        public void add(E e) {
            throw new UnsupportedOperationException();
        }

    }


}