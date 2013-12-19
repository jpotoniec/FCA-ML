/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class ClassAttribute extends SparqlAttribute {

    public ClassAttribute(Reasoner kb, String uri, String endpoint) {
        super(kb, endpoint);
        this.clazz = kb.getClass(uri);
        this.notClazz = kb.createComplementClass(null, clazz);
    }

    @Override
    public String getHumanDescription() {
        return String.format("Does object belong to class %s?", clazz.getLocalName());
    }

    @Override
    public String toString() {
        return clazz.getLocalName();
    }

    protected List<String> getExamples(OntClass clazz) {
        Var var = Var.alloc("x");
        Op pattern = getPatternForClass(var, clazz);
        return getExamples(var, pattern, clazz);
    }

    @Override
    public List<String> getExamples() {
        return getExamples(clazz);
    }

    @Override
    public List<String> getCounterexamples() {
        return getExamples(notClazz);
    }
}
