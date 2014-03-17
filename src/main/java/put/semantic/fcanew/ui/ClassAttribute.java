/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
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
}
