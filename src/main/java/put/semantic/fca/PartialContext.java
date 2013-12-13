/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import put.semantic.putapi.Individual;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class PartialContext {

    private Collection<Attribute> premises;
    private Set<Attribute> notRefuted = new HashSet<>();

    private POD getPOD(Map<String, POD> pods, String key) {
        POD p = pods.get(key);
        if (p == null) {
            p = new POD();
            pods.put(key, p);
        }
        return p;
    }

    public PartialContext(Reasoner kb, Collection<Attribute> attributes, Collection<Attribute> allAttributes) {
        this.premises = attributes;
        Map<String, POD> pods = new HashMap<>();
        for (Attribute a : allAttributes) {
            List<String> examples = a.getExamples();
            for (String ex : examples) {
                getPOD(pods, ex).addPositive(a);
            }
            List<String> counterexamples = a.getCounterexamples();
            for (String ex : counterexamples) {
                getPOD(pods, ex).addNegative(a);
            }
        }
        notRefuted.addAll(allAttributes);
        for (POD pod : pods.values()) {
            if (pod.getPositive().containsAll(attributes)) {
                notRefuted.removeAll(pod.getNegative());
            }
        }
        notRefuted.removeAll(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PartialContext)) {
            return false;
        }
        return notRefuted.equals(((PartialContext) obj).notRefuted);
    }

    @Override
    public int hashCode() {
        return notRefuted.hashCode();
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
