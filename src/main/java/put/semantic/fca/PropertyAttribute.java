/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.vocabulary.VocabTestQuery;
import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntObjectProperty;
import put.semantic.putapi.Reasoner;
import put.semantic.putapi.Vocabulary;

/**
 *
 * @author smaug
 */
public class PropertyAttribute extends SparqlAttribute {

    private OntObjectProperty p;

    public PropertyAttribute(Reasoner kb, String endpoint, String puri) {
        super(kb, endpoint);
        p = kb.getObjectProperty(puri);
        this.clazz = kb.createExistentialRestriction(null, p, kb.getClass(Vocabulary.Thing));
        this.notClazz = kb.createComplementClass(null, clazz);
    }

    @Override
    public String getHumanDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getExamples() {
        Var var = Var.alloc("x");
        Op pattern = getPatternForProperty(var, p);
        return getExamples(var, pattern, clazz);
    }

    @Override
    public List<String> getCounterexamples() {
        Var var = Var.alloc("x");
        Op pattern = getPatternForClass(var, kb.getClass(Vocabulary.Thing));
        List<String> result = getExamples(var, pattern, kb.getClass(Vocabulary.Thing));
        result.removeAll(getExamples());
        return result;
    }

    protected static Op getPatternForProperty(Var var, OntObjectProperty p) {
        BasicPattern pattern = new BasicPattern();
        pattern.add(new Triple(var, Node.createURI(p.getURI()), Var.ANON));
        return new OpBGP(pattern);
    }

    @Override
    public String toString() {
        return String.format("∃%s.⊤", p.getLocalName());
    }
}
