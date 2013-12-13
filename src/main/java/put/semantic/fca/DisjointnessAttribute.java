/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class DisjointnessAttribute implements Attribute {

    private Set<OntClass> classes = new HashSet<>();

    public DisjointnessAttribute(Reasoner kb, String uri1, String uri2) {
        classes.add(kb.getClass(uri1));
        classes.add(kb.getClass(uri2));
    }

    private int count(Individual a) {
        int n = 0;
        for (OntClass c : classes) {
            if (a.hasClass(c)) {
                ++n;
            }
        }
        return n;
    }

    @Override
    public boolean isSatisfiedBy(Individual a) {
        return count(a) == 1;
    }

    @Override
    public boolean isOppositeSatisfiedBy(Individual a) {
        return count(a) > 1;
    }

    @Override
    public String getHumanDescription() {
        StringBuilder b = new StringBuilder();
        for (OntClass c : classes) {
            b.append(c.getLocalName()).append(" ");
        }
        return String.format("Are %sdisjoint?", b.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DisjointnessAttribute)) {
            return false;
        }
        DisjointnessAttribute other = (DisjointnessAttribute) obj;
        return classes.equals(other.classes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(classes);
    }

    @Override
    public String toString() {
        boolean cap = false;
        StringBuilder result = new StringBuilder();
        for (OntClass c : classes) {
            if (cap) {
                result.append("⊔");
            }
            result.append("¬").append(c.getLocalName());
            cap = true;
        }
//        result.append("=⊥");
        return result.toString();
    }

    @Override
    public OntClass getOntClass(Reasoner kb) {
//        assert this.classes.size() == 2;
//        OntClass[] classes = this.classes.toArray(new OntClass[0]);
//        OntClass[] notClasses = new OntClass[classes.length];
//        for (int i = 0; i < classes.length; ++i) {
//            notClasses[i] = kb.createComplementClass(null, classes[i]);
//        }
        List<OntClass> not = new ArrayList<>();
        for (OntClass c : classes) {
            not.add(kb.createComplementClass(null, c));
        }
        return kb.createUnionClass(null, not.toArray(new OntClass[0]));
//        return kb.createUnionClass(null, new OntClass[]{
//                    kb.createIntersectionClass(null, new OntClass[]{classes[0], notClasses[1]}),
//                    kb.createIntersectionClass(null, new OntClass[]{notClasses[0], classes[1]})
//                });
    }
}
