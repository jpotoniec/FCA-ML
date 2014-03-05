/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.ui.ClassAttribute;

public class POD {

    public static interface PODChangedListener extends EventListener {

        public void podChanged(POD pod);
    }
    private OWLNamedIndividual ind;
    private SetOfAttributes attributes;
    private SubsetOfAttributes p, n;
    private List<PODChangedListener> podChangeListeners = new ArrayList<>();
    private OWLReasoner reasoner;

    public POD(OWLNamedIndividual ind, SetOfAttributes attributes, OWLReasoner reasoner) {
        this(ind, attributes, reasoner, new SubsetOfAttributes(attributes), new SubsetOfAttributes(attributes));
    }

    public POD(OWLNamedIndividual ind, SetOfAttributes attributes, OWLReasoner reasoner, SubsetOfAttributes p, SubsetOfAttributes n) {
        this.ind = ind;
        this.attributes = attributes;
        this.reasoner = reasoner;
        this.p = p;
        this.n = n;
    }

    public ReadOnlySubsetOfAttributes getPositive() {
        return p;
    }

    public ReadOnlySubsetOfAttributes getNegative() {
        return n;
    }

    public OWLIndividual getId() {
        return ind;
    }

    public void addPODChangedListener(PODChangedListener listener) {
        if (listener != null) {
            podChangeListeners.add(listener);
        }
    }

    public void removePODChangedListener(PODChangedListener listener) {
        podChangeListeners.remove(listener);
    }

    protected void firePODChanged() {
        for (PODChangedListener listener : podChangeListeners) {
            listener.podChanged(this);
        }
    }

    @Override
    public String toString() {
        return ind.getIRI().getFragment();
    }

    private boolean hasClass(OWLClassExpression ex) {
        OWLClassAssertionAxiom axiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(ex, ind);
        return reasoner.isEntailed(axiom);
    }

    public void update() {
        p.clear();
        n.clear();
        for (Attribute a : attributes) {
            ClassAttribute attr = (ClassAttribute) a;
            if(hasClass(attr.getOntClass())) {
                p.add(attr);
            } else if (hasClass(attr.getComplement())) {
                n.add(attr);
            }
        }
        firePODChanged();
    }

    public void setPositive(Attribute a) {
        assert a instanceof ClassAttribute;
        ClassAttribute attr = (ClassAttribute) a;
        OWLClassAssertionAxiom complAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        reasoner.getRootOntology().getOWLOntologyManager().removeAxiom(reasoner.getRootOntology(), complAxiom);
        reasoner.getRootOntology().getOWLOntologyManager().addAxiom(reasoner.getRootOntology(), normalAxiom);
        reasoner.flush();
        update();
    }

    public void setNegative(Attribute a) {
        assert a instanceof ClassAttribute;
        ClassAttribute attr = (ClassAttribute) a;
        OWLClassAssertionAxiom complAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        reasoner.getRootOntology().getOWLOntologyManager().addAxiom(reasoner.getRootOntology(), complAxiom);
        reasoner.getRootOntology().getOWLOntologyManager().removeAxiom(reasoner.getRootOntology(), normalAxiom);
        reasoner.flush();
        update();
    }

    public void setUnknown(Attribute a) {
        assert a instanceof ClassAttribute;
        ClassAttribute attr = (ClassAttribute) a;
        OWLClassAssertionAxiom complAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        reasoner.getRootOntology().getOWLOntologyManager().removeAxiom(reasoner.getRootOntology(), complAxiom);
        reasoner.getRootOntology().getOWLOntologyManager().removeAxiom(reasoner.getRootOntology(), normalAxiom);
        reasoner.flush();
        update();
    }
}
