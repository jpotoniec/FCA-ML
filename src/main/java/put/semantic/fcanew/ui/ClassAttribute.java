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

    public ClassAttribute(OWLClassExpression clazz, OWLReasoner reasoner) {
        this.clazz = clazz;
//        getReasoner().getInstances(getFactory().getOWLObjectComplementOf(attribute), false).getFlattened()
//        this.complement = clazz.getObjectComplementOf();
        this.complement = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectComplementOf(clazz);
    }

    public OWLClassExpression getOntClass() {
        return clazz;
    }

    public OWLClassExpression getComplement() {
        return complement;
    }

    @Override
    public String toString() {
        if (!clazz.isAnonymous()) {
            return clazz.asOWLClass().getIRI().getFragment();
        }
        return new DLSyntaxObjectRenderer().render(clazz);
    }
}
