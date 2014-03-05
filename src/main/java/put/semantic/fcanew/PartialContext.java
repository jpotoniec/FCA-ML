/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

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
    private List<ContextChangedListener> contextChangedListeners = new ArrayList<>();
    private boolean contextChanged = false;
    private POD.PODChangedListener changeListener = new POD.PODChangedListener() {
        @Override
        public void podChanged(POD pod) {
            contextChanged = true;
        }
    };

    public PartialContext(SetOfAttributes attributes) {
        this.pods = new ArrayList<>();
        this.attributes = attributes;
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

    public void update(Implication implication) {
        for (POD p : pods) {
            if (p.getPositive().containsAll(implication.getPremises())) {
                p.getPositive().addAll(implication.getConclusions());
            }
        }
        fireContextChanged();
    }
}
