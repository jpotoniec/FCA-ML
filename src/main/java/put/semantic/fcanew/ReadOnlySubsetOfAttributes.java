/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.ui.ClassAttribute;

/**
 *
 * @author smaug
 */
public abstract class ReadOnlySubsetOfAttributes implements Iterable<Attribute> {

    protected SetOfAttributes base;
    protected NavigableSet<Integer> indexes;

    public boolean containsAny(ReadOnlySubsetOfAttributes other) {
        assert this.base == other.base;
        for (Integer i : other.indexes) {
            if (indexes.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(ReadOnlySubsetOfAttributes other) {
        assert this.base == other.base;
        return indexes.containsAll(other.indexes);
    }

    public SubsetOfAttributes getSubset(int max) {
        return new SubsetOfAttributes(base, indexes.headSet(max, true));
    }

    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public boolean isFull() {
        return indexes.size() == base.size();
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
                throw new UnsupportedOperationException();
            }
        };
    }

    public static boolean less(ReadOnlySubsetOfAttributes a, ReadOnlySubsetOfAttributes b, int i) {
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

    public OWLClassExpression getClass(OWLReasoner model) {
        if (isEmpty()) {
            return model.getTopClassNode().getRepresentativeElement();
        } else {
            Set<OWLClassExpression> classes = new HashSet<>();
            for (Attribute a : this) {
                classes.add(((ClassAttribute) a).getOntClass());
            }
            return model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(classes);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ReadOnlySubsetOfAttributes)) {
            return false;
        }
        ReadOnlySubsetOfAttributes other = (ReadOnlySubsetOfAttributes) obj;
        if (this.base != other.base) {
            return false;
        }
        return this.indexes.equals(other.indexes);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.base);
        hash = 41 * hash + Objects.hashCode(this.indexes);
        return hash;
    }

}
