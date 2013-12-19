/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.FormatterElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.IntersectionClass;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public abstract class SparqlAttribute implements Attribute {

    protected static Node RDFTYPE = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    protected OntClass clazz;
    protected OntClass notClazz;
    protected String endpoint;
    protected Reasoner kb;

    public SparqlAttribute(Reasoner kb, String endpoint) {
        this.kb = kb;
        this.endpoint = endpoint;
    }

    protected OpBGP getOpForClass(Var var, OntClass clazz) {
        String uri = clazz.getURI();
        BasicPattern pattern = new BasicPattern();
        if (uri != null) {
            pattern.add(new Triple(var, RDFTYPE, Node.createURI(uri)));
        } else if (clazz instanceof IntersectionClass) {
            for (OntClass operand : ((IntersectionClass) clazz).getOperands()) {
                OpBGP bgp = getOpForClass(var, operand);
                pattern.addAll(bgp.getPattern());
            }
        } else {
            return null;
        }
        return new OpBGP(pattern);
    }

    protected static void fetch(Op pattern, Model model, List<String> result) {
        QueryIterator i = Algebra.exec(pattern, model);
        while (i.hasNext()) {
            Binding b = i.nextBinding();
            Node n = b.get(b.vars().next());
            if (n != null && n.isURI()) {
                result.add(n.getURI());
            }
        }
    }

    protected List<String> getExamples(Var var, Op pattern, OntClass clazz) {
        pattern = new OpProject(pattern, Arrays.asList(var));
        pattern = new OpDistinct(pattern);
        pattern = Algebra.optimize(pattern);
        pattern = new OpService(Node.createURI(getSparqlEndpoint()), pattern);
//        System.err.println(OpAsQuery.asQuery(pattern).toString());
        List<String> result = new ArrayList<>();
        fetch(pattern, ModelFactory.createDefaultModel(), result);
        for (Individual i : clazz.getIndividuals()) {
            result.add(i.getURI());
        }
        return result;
    }

    protected Op getPatternForClass(Var var, OntClass clazz) {
        Op pattern = null;
        List<OntClass> classes = new ArrayList<>();
        try {
            classes.addAll(clazz.getSubClasses());
        } catch (RuntimeException ex) {
            System.err.println(ex);
        }
        classes.add(clazz);
        boolean isUnion = false;
        for (OntClass c : classes) {
            Op p = getOpForClass(var, c);
            if (p == null) {
                continue;
            }
            if (pattern == null) {
                pattern = p;
            } else {
                pattern = new OpUnion(pattern, p);
                isUnion = true;
            }
        }
        //well, there is bug in Jena and if later OpJoin is used . in SPARQL query after triple pattern is missing, because dots are added only inside groups
        if (!isUnion) {
            pattern = new OpUnion(pattern, pattern);
        }
        return pattern;
    }

    protected String getSparqlEndpoint() {
        return endpoint;
    }

    @Override
    public OntClass getOntClass() {
        return clazz;
    }    
    
    @Override
    public boolean isSatisfiedBy(Individual a) {
        return a.hasClass(clazz);
    }

    @Override
    public boolean isOppositeSatisfiedBy(Individual a) {
        return a.hasClass(notClazz);
    }
}
