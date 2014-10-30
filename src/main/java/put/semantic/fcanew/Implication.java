/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.ui.ClassAttribute;

/**
 *
 * @author smaug
 */
public class Implication {

    protected SubsetOfAttributes premises, conclusions;

    public Implication(SubsetOfAttributes premises, SubsetOfAttributes conclusions) {
        this.premises = premises;
        this.conclusions = conclusions;
        conclusions.removeAll(premises);
    }

    public ReadOnlySubsetOfAttributes getPremises() {
        return premises;
    }

    public ReadOnlySubsetOfAttributes getConclusions() {
        return conclusions;
    }

    public boolean isTrivial() {
        return conclusions.isEmpty();
    }

    @Override
    public String toString() {
        return premises.toString() + "->" + conclusions.toString();
    }

    public Map<String, String> describe(KB kb) {
        Map<String, String> result = new TreeMap<>();
        for (Attribute a : this.getPremises()) {
            result.put(a.toString(), ((ClassAttribute) a).describeS(kb));
        }
        for (Attribute a : this.getConclusions()) {
            result.put(a.toString(), ((ClassAttribute) a).describeS(kb));
        }
        return result;
    }

    public OWLSubClassOfAxiom toAxiom(OWLDataFactory factory) {
        OWLClassExpression subClass = this.getPremises().getClass(factory);
        OWLClassExpression superClass = this.getConclusions().getClass(factory);
        OWLSubClassOfAxiom a = factory.getOWLSubClassOfAxiom(subClass, superClass);
        return a;
    }

    public boolean isRefutedBy(PartialContext context) {
        for (POD p : context.getPODs()) {
            if (isRefutedBy(p)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRefutedBy(POD p) {
        return p.getPositive().containsAll(premises) && p.getNegative().containsAny(conclusions);
    }
}
