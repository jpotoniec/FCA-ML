/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import put.semantic.putapi.Individual;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class PartialContext {

    private Set<POD> pods = new HashSet<>();
    private Collection<Attribute> premises;
    private Set<Attribute> notRefuted = new HashSet<>();

    public PartialContext(Reasoner kb, Collection<Attribute> attributes, Collection<Attribute> allAttributes) {
        this.premises = attributes;
        for (Individual i : kb.getIndividuals()) {
            pods.add(new POD(kb, i, allAttributes));
        }
        notRefuted.addAll(allAttributes);
        for (POD pod : pods) {
            if (pod.getPositive().containsAll(attributes)) {
                notRefuted.removeAll(pod.getNegative());
            }
        }
        notRefuted.removeAll(attributes);
    }

    public Set<POD> getDescriptions() {
        return Collections.unmodifiableSet(pods);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartialContext)) {
            return false;
        }
        return pods.equals(((PartialContext) obj).pods);
    }

    @Override
    public int hashCode() {
        return pods.hashCode();
    }

    public Set<Attribute> getNotRefuted() {
        return Collections.unmodifiableSet(notRefuted);
    }

    @Override
    public String toString() {
        String i = "", t = "";
        boolean and = false;
        for (Attribute attr : premises) {
            if (and) {
                i += " and ";
            }
            i += attr;
            and = true;
        }
        and = false;
        for (Attribute attr : notRefuted) {
            if (and) {
                t += " and ";
            }
            t += attr;
            and = true;
        }
        return String.format("if %s, then %s", i, t);
    }
}
