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
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import put.semantic.fcanew.ui.ClassAttribute;

public class POD {

    public static interface PODChangedListener extends EventListener {

        public void podChanged(POD pod);
    }
    private OWLNamedIndividual ind;
    private SetOfAttributes attributes;
    private SubsetOfAttributes p, n;
    private List<PODChangedListener> podChangeListeners = new ArrayList<>();
    private KB kb;

    public POD(OWLNamedIndividual ind, SetOfAttributes attributes, KB kb) {
        this(ind, attributes, kb, new SubsetOfAttributes(attributes), new SubsetOfAttributes(attributes));
    }

    public POD(OWLNamedIndividual ind, SetOfAttributes attributes, KB kb, SubsetOfAttributes p, SubsetOfAttributes n) {
        this.ind = ind;
        this.attributes = attributes;
        this.kb = kb;
        this.p = p;
        this.n = n;
    }

    public ReadOnlySubsetOfAttributes getPositive() {
        return p;
    }

    public ReadOnlySubsetOfAttributes getNegative() {
        return n;
    }

    public OWLNamedIndividual getId() {
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
        OWLClassAssertionAxiom axiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(ex, ind);
        return kb.getReasoner().isEntailed(axiom);
    }

    public void update() {
        p.clear();
        n.clear();
        for (Attribute a : attributes) {
            ClassAttribute attr = (ClassAttribute) a;
            if (hasClass(attr.getOntClass())) {
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
        OWLClassAssertionAxiom complAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        kb.getManager().removeAxiom(kb.getAbox(), complAxiom);
        kb.getManager().addAxiom(kb.getAbox(), normalAxiom);
        kb.getReasoner().flush();
        update();
    }

    public void setNegative(Attribute a) {
        assert a instanceof ClassAttribute;
        ClassAttribute attr = (ClassAttribute) a;
        OWLClassAssertionAxiom complAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        kb.getManager().removeAxiom(kb.getAbox(), normalAxiom);
        kb.getManager().addAxiom(kb.getAbox(), complAxiom);
        kb.getReasoner().flush();
        update();
    }

    public void setUnknown(Attribute a) {
        assert a instanceof ClassAttribute;
        ClassAttribute attr = (ClassAttribute) a;
        OWLClassAssertionAxiom complAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getComplement(), ind);
        OWLClassAssertionAxiom normalAxiom = kb.getManager().getOWLDataFactory().getOWLClassAssertionAxiom(attr.getOntClass(), ind);
        kb.getManager().removeAxiom(kb.getAbox(), complAxiom);
        kb.getManager().removeAxiom(kb.getAbox(), normalAxiom);
        kb.getReasoner().flush();
        update();
    }
}
