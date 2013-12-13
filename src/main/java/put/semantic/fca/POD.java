/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author smaug
 */
public class POD {

    private Set<Attribute> positive = new HashSet<>();
    private Set<Attribute> negative = new HashSet<>();

    public void addPositive(Attribute a) {
        positive.add(a);
    }

    public void addNegative(Attribute a) {
        negative.add(a);
    }

    public Set<Attribute> getPositive() {
        return Collections.unmodifiableSet(positive);
    }

    public Set<Attribute> getNegative() {
        return Collections.unmodifiableSet(negative);
    }
}