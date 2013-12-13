/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import put.semantic.putapi.Descriptor;
import put.semantic.putapi.Individual;
import put.semantic.putapi.IntersectionClass;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;
import put.semantic.putapi.impl.pellet.PelletReasoner;

/**
 *
 * @author smaug
 */
public class ClassAttribute implements Attribute {

    protected OntClass clazz;
    protected OntClass notClazz;
    protected Reasoner kb;
    private String endpoint;

    public ClassAttribute(Reasoner kb, String uri, String endpoint) {
        this.kb = kb;
        this.clazz = kb.getClass(uri);
        this.notClazz = kb.createComplementClass(null, clazz);
        this.endpoint = endpoint;
    }

    public boolean isSatisfiedBy(Individual a) {
        return a.hasClass(clazz);
    }

    public boolean isOppositeSatisfiedBy(Individual a) {
        return a.hasClass(notClazz);
    }

    @Override
    public String getHumanDescription() {
        return String.format("Does object belong to class %s?", clazz.getLocalName());
    }

    @Override
    public String toString() {
        return clazz.getLocalName();
    }

    @Override
    public OntClass getOntClass(Reasoner kb) {
        return clazz;
    }

    protected String getSparqlEndpoint() {
        return endpoint;
    }
    private static Node RDFTYPE = Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    private static Var var = Var.alloc("x");

    private static OpBGP getOpForClass(OntClass clazz) {
        String uri = clazz.getURI();
        BasicPattern pattern = new BasicPattern();
        if (uri != null) {
            pattern.add(new Triple(var, RDFTYPE, Node.createURI(uri)));
        } else if (clazz instanceof IntersectionClass) {
            for (OntClass operand : ((IntersectionClass) clazz).getOperands()) {
                OpBGP bgp = getOpForClass(operand);
                pattern.addAll(bgp.getPattern());
            }
        }
        return new OpBGP(pattern);
    }

    private static void fetch(Op pattern, Model model, List<String> result) {
        QueryIterator i = Algebra.exec(pattern, model);
        while (i.hasNext()) {
            Binding b = i.nextBinding();
            Iterator<Var> j = b.vars();
            Node n = b.get(var);
            if (n != null && n.isURI()) {
                result.add(n.getURI());
            }
        }
    }

    private List<String> getExamples(OntClass clazz) {
        Op pattern = null;
        List<OntClass> classes = new ArrayList<>();
        classes.addAll(clazz.getSubClasses());
        classes.add(clazz);
        for (OntClass c : classes) {
            Op p = getOpForClass(c);
            if (pattern == null) {
                pattern = p;
            } else {
                pattern = new OpUnion(pattern, p);
            }
        }
        pattern = new OpProject(pattern, Arrays.asList(var));
        pattern = new OpDistinct(pattern);
        pattern = Algebra.optimize(pattern);
        pattern = new OpService(Node.createURI(getSparqlEndpoint()), pattern);
        List<String> result = new ArrayList<>();
        fetch(pattern, ModelFactory.createDefaultModel(), result);
        for (Individual i : clazz.getIndividuals()) {
            result.add(i.getURI());
        }
        return result;
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
