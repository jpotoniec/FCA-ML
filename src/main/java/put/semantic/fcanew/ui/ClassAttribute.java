/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.KB;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

/**
 *
 * @author smaug
 */
public class ClassAttribute implements Attribute {

    private OWLClassExpression clazz, complement;
    private static DLSyntaxObjectRenderer renderer = new DLSyntaxObjectRenderer();

    public ClassAttribute(OWLClassExpression clazz, OWLReasoner reasoner) {
        this.clazz = clazz;
        this.complement = clazz.getComplementNNF();
    }

    public OWLClassExpression getOntClass() {
        return clazz;
    }

    public OWLClassExpression getComplement() {
        return complement;
    }

    @Override
    public String toString() {
        return renderer.render(clazz);
    }

    protected void describe(KB kb, OWLClass c, Set<OWLLiteral> result, OWLAnnotationProperty p) {
        Set<OWLAnnotation> annotations = c.getAnnotations(kb.getMainOntology(), p);
        for (OWLAnnotation a : annotations) {
            if (a.getValue() instanceof OWLLiteral) {
                OWLLiteral v = (OWLLiteral) a.getValue();
                result.add(v);
            }
        }
    }

    public Set<OWLLiteral> describe(KB kb) {
        if (getOntClass().isAnonymous()) {
            return Collections.EMPTY_SET;
        }
        Set<OWLLiteral> result = new HashSet<>();
        OWLClass c = getOntClass().asOWLClass();
        describe(kb, c, result, kb.getManager().getOWLDataFactory().getRDFSLabel());
        describe(kb, c, result, kb.getManager().getOWLDataFactory().getRDFSComment());
        return result;
    }

    public String describeS(KB kb) {
        Set<OWLLiteral> literals = describe(kb);
        String result = "";
        for (OWLLiteral l : literals) {
            result += l.getLiteral() + System.lineSeparator();
        }
        return result;
    }
}
