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
import put.semantic.putapi.Vocabulary;

/**
 *
 * @author smaug
 */
public class NotClassAttribute extends ClassAttribute {
    
    public NotClassAttribute(Reasoner kb, String uri, String endpoint) {
        super(kb, uri, endpoint);
        OntClass tmp = this.clazz;
        this.clazz = this.notClazz;
        this.notClazz = tmp;
    }

    /// all objects which are not stated to belong to given class
    @Override
    public List<String> getExamples() {
        Var var = Var.alloc("x");
        Op pattern = getPatternForClass(var, kb.getClass(Vocabulary.Thing));
        List<String> result = getExamples(var, pattern, kb.getClass(Vocabulary.Thing));
        result.removeAll(getCounterexamples());
        return result;
    }

    @Override
    public List<String> getCounterexamples() {
        return super.getCounterexamples();
    }

    @Override
    public String getHumanDescription() {
        return String.format("Does object not belong to class %s?", notClazz.getLocalName());
    }

    @Override
    public String toString() {
        return "¬" + notClazz.getLocalName();
    }
}
