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
import org.apache.commons.lang.StringUtils;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;
import put.semantic.putapi.Vocabulary;

/**
 *
 * @author smaug
 */
public class Implication {

    private Set<Attribute> premises;
    private Set<Attribute> conclusions;

    public Implication(Set<Attribute> premises, Set<Attribute> conclusions) {
        this.premises = new HashSet<>(premises);
        this.conclusions = new HashSet<>(conclusions);
    }

    public Set<Attribute> getConclusions() {
        return conclusions;
    }

    public Set<Attribute> getPremises() {
        return premises;
    }

    private static OntClass getClass(Reasoner kb, Set<Attribute> set) {
        List<OntClass> operands = new ArrayList<>();
        for (Attribute a : set) {
            operands.add(a.getOntClass(kb));
        }
        if (operands.isEmpty()) {
            return kb.getClass(Vocabulary.Thing);
        } else {
            return kb.createIntersectionClass(null, operands.toArray(new OntClass[0]));
        }
    }

    public OntClass getPremisesClass(Reasoner kb) {
        return getClass(kb, premises);
    }

    public OntClass getConclusionsClass(Reasoner kb) {
        return getClass(kb, conclusions);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Implication)) {
            return false;
        }
        Implication other = (Implication) obj;
        return premises.equals(other.premises) && conclusions.equals(other.conclusions);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.premises);
        hash = 43 * hash + Objects.hashCode(this.conclusions);
        return hash;
    }

    public boolean isSatisfiedBy(Set<Attribute> x) {
        return x.containsAll(premises);
    }

    @Override
    public String toString() {
        return String.format("(%s)->(%s)", StringUtils.join(premises, " "), StringUtils.join(conclusions, " "));
    }
}
