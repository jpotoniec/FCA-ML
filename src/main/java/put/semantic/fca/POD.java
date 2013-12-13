/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import put.semantic.putapi.Individual;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class POD {

    private Individual a;
    private Set<Attribute> positive = new HashSet<>();
    private Set<Attribute> negative = new HashSet<>();
    private int pSize;

    public POD(Reasoner kb, Individual a, Collection<Attribute> attributes) {
        this.a = a;
        this.pSize = attributes.size();
        for (Attribute attribute : attributes) {
            if (attribute.isSatisfiedBy(a)) {
                positive.add(attribute);
            } else if (attribute.isOppositeSatisfiedBy(a)) {
                negative.add(attribute);
            }
        }
    }

    @Override
    public String toString() {
        String p = "", n = "";
        for (Attribute attr : positive) {
            p += attr.getHumanDescription() + " ";
        }
        for (Attribute attr : negative) {
            n += attr.getHumanDescription() + " ";
        }
        return String.format("%s: %d/%d out of %d", a.getLocalName(), positive.size(), negative.size(), pSize);
    }

    public Set<Attribute> getPositive() {
        return Collections.unmodifiableSet(positive);
    }

    public Set<Attribute> getNegative() {
        return Collections.unmodifiableSet(negative);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof POD)) {
            return false;
        }
        POD other = (POD) obj;
        return positive.equals(other.positive) && negative.equals(other.negative);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.positive);
        hash = 13 * hash + Objects.hashCode(this.negative);
        return hash;
    }
}