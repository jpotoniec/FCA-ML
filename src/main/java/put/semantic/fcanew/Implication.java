/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

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

    public OWLSubClassOfAxiom toAxiom(OWLReasoner model) {
        OWLClassExpression subClass = this.getPremises().getClass(model);
        OWLClassExpression superClass = this.getConclusions().getClass(model);
        OWLSubClassOfAxiom a = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
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
