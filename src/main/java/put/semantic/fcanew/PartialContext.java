/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.ui.ClassAttribute;

/**
 *
 * @author smaug
 */
public class PartialContext {

    public static interface ContextChangedListener extends EventListener {

        public void contextChanged(PartialContext context);
    }
    private List<POD> pods;
    private SetOfAttributes attributes;
    private OWLReasoner model;
    private List<ContextChangedListener> contextChangedListeners = new ArrayList<>();
    private boolean contextChanged = false;
    private POD.PODChangedListener changeListener = new POD.PODChangedListener() {
        @Override
        public void podChanged(POD pod) {
            contextChanged = true;
        }
    };

    public PartialContext(SetOfAttributes attributes, OWLReasoner model) {
        this.pods = new ArrayList<>();
        this.attributes = attributes;
        this.model = model;
    }

    public SetOfAttributes getAttributes() {
        return attributes;
    }

    public List<? extends POD> getPODs() {
        return Collections.unmodifiableList(pods);
    }

    public SubsetOfAttributes K(SubsetOfAttributes p) {
        SubsetOfAttributes m = new SubsetOfAttributes(getAttributes());
        m.fill();
        for (POD pod : getPODs()) {
            if (pod.getPositive().containsAll(p)) {
                m.removeAll(pod.getNegative());
            }
        }
        return m;
    }

    public void addContextChangedListener(ContextChangedListener listener) {
        if (listener != null) {
            contextChangedListeners.add(listener);
        }
    }

    public void removeContextChangedListener(ContextChangedListener listener) {
        contextChangedListeners.remove(listener);
    }

    protected void fireContextChanged() {
        for (ContextChangedListener listener : contextChangedListeners) {
            listener.contextChanged(this);
        }
    }

    public void addPOD(POD example) {
        pods.add(example);
        example.addPODChangedListener(changeListener);
    }

    private OWLClassExpression getClass(ReadOnlySubsetOfAttributes attrs) {
        if (attrs.isEmpty()) {
            return model.getTopClassNode().getRepresentativeElement();
        } else {
            Set<OWLClassExpression> classes = new HashSet<>();
            for (Attribute a : attrs) {
                classes.add(((ClassAttribute) a).getOntClass());
            }
            return model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(classes);
        }
    }

    private void updateModel(Implication implication) {
        OWLClassExpression subClass = getClass(implication.getPremises());
        OWLClassExpression superClass = getClass(implication.getConclusions());
        OWLSubClassOfAxiom a = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
        model.getRootOntology().getOWLOntologyManager().addAxiom(model.getRootOntology(), a);
        model.flush();
    }

    public void update(Implication implication) {
        updateModel(implication);
        contextChanged = false;
        for (POD p : pods) {
            p.update();
        }
        if (contextChanged) {
            fireContextChanged();
        }
    }
}
