/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 *
 * @author smaug
 */
public class SubsetOfAttributes implements Iterable<Attribute> {

    protected SetOfAttributes base;
    protected NavigableSet<Integer> indexes;

    public SubsetOfAttributes(SetOfAttributes base) {
        this.base = base;
        this.indexes = new TreeSet<>();
    }

    protected SubsetOfAttributes(SetOfAttributes base, NavigableSet<Integer> indexes) {
        this.base = base;
        this.indexes = new TreeSet<>(indexes);
    }

    public SubsetOfAttributes(SubsetOfAttributes orig) {
        this.base = orig.base;
        this.indexes = new TreeSet<>(orig.indexes);
    }

    public SubsetOfAttributes(SetOfAttributes base, Integer[] attributes) {
        this.base = base;
        this.indexes = new TreeSet<>(Arrays.asList(attributes));
    }

    @Override
    public Iterator<Attribute> iterator() {
        return new Iterator<Attribute>() {
            private Iterator<Integer> i = indexes.iterator();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public Attribute next() {
                return base.get(i.next());
            }

            @Override
            public void remove() {
                i.remove();
            }
        };
    }

    public static boolean less(SubsetOfAttributes a, SubsetOfAttributes b, int i) {
        if (!(!a.indexes.contains(i) && b.indexes.contains(i))) {
            return false;
        }
        Iterator<Integer> x = a.indexes.iterator();
        Iterator<Integer> y = b.indexes.iterator();
        while (x.hasNext() && y.hasNext()) {
            int p = x.next();
            int q = y.next();
            if (p != q) {
                return q == i;
            }
        }
        if (y.hasNext()) {
            return y.next() == i;
        }
        return false;
    }

    public boolean containsAll(SubsetOfAttributes other) {
        assert this.base == other.base;
        return indexes.containsAll(other.indexes);
    }

    public void removeAll(SubsetOfAttributes other) {
        assert this.base == other.base;
        indexes.removeAll(other.indexes);
    }

    public void addAll(SubsetOfAttributes other) {
        assert this.base == other.base;
        indexes.addAll(other.indexes);
    }

    public SubsetOfAttributes getSubset(int max) {
        return new SubsetOfAttributes(base, indexes.headSet(max, true));
    }

    public void add(int j) {
        indexes.add(j);
    }

    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public boolean isFull() {
        return indexes.size() == base.size();
    }

    public void add(Attribute a) {
        indexes.add(base.indexOf(a));
    }

    public boolean contains(Attribute a) {
        return indexes.contains(base.indexOf(a));
    }

    @Override
    public String toString() {
        String result = "";
        Iterator<Attribute> i = this.iterator();
        while (i.hasNext()) {
            result += i.next();
            if (i.hasNext()) {
                result += ", ";
            }
        }
        return "[" + result + "]";
    }

    public void fill() {
        for (int i = 0; i < base.size(); ++i) {
            this.indexes.add(i);
        }
    }

    public void clear() {
        this.indexes.clear();
    }

    public void remove(Attribute a) {
        indexes.remove(base.indexOf(a));
    }
}
