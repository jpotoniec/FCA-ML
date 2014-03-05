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

import com.hp.hpl.jena.ontology.Individual;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import put.semantic.fcanew.ui.ClassAttribute;

public class POD {

    public static interface PODChangedListener extends EventListener {

        public void podChanged(POD pod);
    }
    private Individual ind;
    private SetOfAttributes attributes;
    private SubsetOfAttributes p, n;
    private List<PODChangedListener> podChangeListeners = new ArrayList<>();

    public POD(Individual ind, SetOfAttributes attributes) {
        this.ind = ind;
        this.attributes = attributes;
        this.p = new SubsetOfAttributes(attributes);
        this.n = new SubsetOfAttributes(attributes);
    }

    public SubsetOfAttributes getPositive() {
        return p;
    }

    public SubsetOfAttributes getNegative() {
        return n;
    }

    public Individual getId() {
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
        return ind.getLocalName();
    }

}
