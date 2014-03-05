/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author smaug
 */
public class PartialContext {

    private List<POD> pods;
    private SetOfAttributes attributes;

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

    public void addPOD(POD example) {
        pods.add(example);
    }
}
