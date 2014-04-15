/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import java.util.List;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.features.AbstractFeatureCalculator;
import put.semantic.fcanew.ml.features.values.BooleanFeatureValue;

/**
 *
 * @author smaug
 */
public class SatCalculator extends AbstractFeatureCalculator {

    public SatCalculator() {
        super("sat",
                "sat (premises)",
                "sat (conclusions)"
        );
    }

    @Override
    public List<? extends BooleanFeatureValue> compute(Implication impl, OWLReasoner model, PartialContext context) {
        OWLClassExpression p = impl.getPremises().getClass(model.getRootOntology().getOWLOntologyManager().getOWLDataFactory());
        OWLClassExpression c = impl.getConclusions().getClass(model.getRootOntology().getOWLOntologyManager().getOWLDataFactory());
        OWLClassExpression pc = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(p, c);
        return transform(model.isSatisfiable(pc),
                model.isSatisfiable(p),
                model.isSatisfiable(c));
    }

}
