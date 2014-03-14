/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
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
    private List<ProgressListener> progessListeners = new ArrayList<ProgressListener>();

    public PartialContext(SetOfAttributes attributes, OWLReasoner model) {
        this.pods = new ArrayList<>();
        this.attributes = attributes;
        this.model = model;
        addProgressListener(new ProgressListener() {

            int max = 0;

            @Override
            public void reset(int max) {
                this.max = max;
            }

            @Override
            public void update(int status) {
                System.err.printf("Attribute %d/%d\n", status, max);
            }
        });
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

    private void updateModel(Implication implication) {
        OWLSubClassOfAxiom a = implication.toAxiom(model);
        model.getRootOntology().getOWLOntologyManager().addAxiom(model.getRootOntology(), a);
        model.flush();
    }

    public void update(Implication implication) {
        updateModel(implication);
        updateContext();
        fireContextChanged();
    }

    public void updateContext() {
        model.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.CLASS_HIERARCHY, InferenceType.OBJECT_PROPERTY_ASSERTIONS, InferenceType.OBJECT_PROPERTY_HIERARCHY);
        Map<OWLNamedIndividual, SubsetOfAttributes> p = new HashMap<>();
        Map<OWLNamedIndividual, SubsetOfAttributes> n = new HashMap<>();
        Set<OWLNamedIndividual> individuals = model.getRootOntology().getIndividualsInSignature(true);
        for (OWLNamedIndividual i : individuals) {
            p.put(i, new SubsetOfAttributes(getAttributes()));
            n.put(i, new SubsetOfAttributes(getAttributes()));
        }
        fireProgressListenerReset(getAttributes().size());
        for (int x = 0; x < getAttributes().size(); ++x) {
            fireProgressUpdate(x + 1);
            ClassAttribute attr = (ClassAttribute) getAttributes().get(x);
            Set<OWLNamedIndividual> instances;
            instances = model.getInstances(attr.getOntClass(), false).getFlattened();
            for (OWLNamedIndividual i : instances) {
                SubsetOfAttributes attrs = p.get(i);
                if (attrs != null) {
                    attrs.add(x);
                }
            }
            instances = model.getInstances(attr.getComplement(), false).getFlattened();
            for (OWLNamedIndividual i : instances) {
                SubsetOfAttributes attrs = n.get(i);
                if (attrs != null) {
                    attrs.add(x);
                }
            }
        }
        pods.clear();
        for (OWLNamedIndividual i : p.keySet()) {
            POD pod = new POD(i, getAttributes(), model, p.get(i), n.get(i));
            addPOD(pod);
        }
    }

    public void addProgressListener(ProgressListener l) {
        if (l != null) {
            progessListeners.add(l);
        }
    }

    public void removeProgressListener(ProgressListener l) {
        progessListeners.remove(l);
    }

    protected void fireProgressListenerReset(int max) {
        for (ProgressListener l : progessListeners) {
            l.reset(max);
        }
    }

    protected void fireProgressUpdate(int status) {
        for (ProgressListener l : progessListeners) {
            l.update(status);
        }
    }
}
