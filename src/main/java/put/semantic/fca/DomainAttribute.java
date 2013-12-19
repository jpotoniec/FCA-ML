/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpMinus;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Exists;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.OntObjectProperty;
import put.semantic.putapi.Reasoner;
import put.semantic.putapi.Vocabulary;

/**
 *
 * @author smaug
 */
public class DomainAttribute extends SparqlAttribute {

    /*
     * A is a domain of R iff ∃R.⊤⊑A iff ∀R.⊥⊔A     
     */
    private OntObjectProperty property;
    private OntClass domain;
    private OntClass notDomain;

    public DomainAttribute(Reasoner kb, String endpoint, String pURI, String dURI) {
        super(kb, endpoint);
        property = kb.getObjectProperty(pURI);
        domain = kb.getClass(dURI);
        notDomain = kb.createComplementClass(null, domain);
        clazz = kb.createUnionClass(null, new OntClass[]{
                    kb.createUniversalRestriction(null, property, kb.getClass(Vocabulary.Nothing)),
                    domain
                });
        notClazz = kb.createComplementClass(null, clazz);
    }

    @Override
    public String getHumanDescription() {
        return String.format("Is %s domain for property %s?", domain.getLocalName(), property.getLocalName());
    }

    protected OpBGP getPatternForProperty(Var var) {
        BasicPattern p = new BasicPattern();
        //TODO: subproperties
        p.add(new Triple(var, Node.createURI(property.getURI()), Var.ANON));
        return new OpBGP(p);
    }

    private Op getPositive(Var var) {
        Op classOp = getPatternForClass(var, domain);
        Op propOp = getPatternForProperty(var);
        return OpJoin.create(classOp, propOp);
    }

    private Op getNegative(Var var) {
        Op classOp = getPatternForClass(var, notDomain);
        Op propOp = getPatternForProperty(var);
        return OpJoin.create(classOp, propOp);
    }

    @Override
    public List<String> getExamples() {
        Var var = Var.alloc("x");
        Op op = getPositive(var);
        System.err.println("examples:\n" + op);
        return getExamples(var, op);
    }

    @Override
    public List<String> getCounterexamples() {
        Var var = Var.alloc("x");
        Op op = getNegative(var);
        System.err.println("counterexamples:\n" + op);
        return getExamples(var, op);
    }

    @Override
    public String toString() {
        return String.format("∃%s.⊤⊑%s", property.getLocalName(), domain.getLocalName());
    }
}
