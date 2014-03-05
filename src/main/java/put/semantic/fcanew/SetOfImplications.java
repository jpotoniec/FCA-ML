/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.HashSet;

/**
 *
 * @author smaug
 */
public class SetOfImplications extends HashSet<Implication> {

    public void closure(SubsetOfAttributes p) {
        boolean changed;
        do {
            changed = false;
            for (Implication i : this) {
                if (p.containsAll(i.getPremises()) && !p.containsAll(i.getConclusions())) {
                    p.addAll(i.getConclusions());
                    changed = true;
                }
            }
        } while (changed);
    }

}
