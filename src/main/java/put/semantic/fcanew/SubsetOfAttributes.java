/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 *
 * @author smaug
 */
public class SubsetOfAttributes extends ReadOnlySubsetOfAttributes {

    public static SubsetOfAttributes intersectionOf(ReadOnlySubsetOfAttributes a, ReadOnlySubsetOfAttributes... other) {
        SubsetOfAttributes result = new SubsetOfAttributes(a);
        for (ReadOnlySubsetOfAttributes b : other) {
            assert result.base == b.base;
            result.indexes.retainAll(b.indexes);
        }
        return result;
    }

    public static SubsetOfAttributes unionOf(ReadOnlySubsetOfAttributes a, ReadOnlySubsetOfAttributes b) {
        assert a.base == b.base;
        SubsetOfAttributes result = new SubsetOfAttributes(a);
        result.indexes.addAll(b.indexes);
        return result;
    }

    public SubsetOfAttributes(SetOfAttributes base) {
        this.base = base;
        this.indexes = new TreeSet<>();
    }

    protected SubsetOfAttributes(SetOfAttributes base, NavigableSet<Integer> indexes) {
        this.base = base;
        this.indexes = new TreeSet<>(indexes);
    }

    public SubsetOfAttributes(ReadOnlySubsetOfAttributes orig) {
        this.base = orig.base;
        this.indexes = new TreeSet<>(orig.indexes);
    }

    public SubsetOfAttributes(SetOfAttributes base, Integer[] attributes) {
        this.base = base;
        this.indexes = new TreeSet<>(Arrays.asList(attributes));
    }

    public SubsetOfAttributes(SetOfAttributes base, Iterable<Attribute> attributes) {
        this.base = base;
        this.indexes = new TreeSet<>();
        for (Attribute a : attributes) {
            this.indexes.add(base.indexOf(a));
        }
    }

    public void removeAll(ReadOnlySubsetOfAttributes other) {
        assert this.base == other.base;
        indexes.removeAll(other.indexes);
    }

    public void addAll(ReadOnlySubsetOfAttributes other) {
        assert this.base == other.base;
        indexes.addAll(other.indexes);
    }

    public void add(int j) {
        indexes.add(j);
    }

    public void add(Attribute a) {
        indexes.add(base.indexOf(a));
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

    public List<? extends SubsetOfAttributes> split() {
        List<SubsetOfAttributes> result = new ArrayList<>();
        for (Integer i : this.indexes) {
            result.add(new SubsetOfAttributes(base, new Integer[]{i}));
        }
        return result;
    }
}
