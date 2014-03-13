/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;

/**
 *
 * @author smaug
 */
public class SupportCalculator extends PCAbstractFeatureCalculator {

    public SupportCalculator(boolean p, boolean c) {
        super("support", p, c);
    }

    protected int getSize(OWLReasoner model) {
        return model.getRootOntology().getIndividualsInSignature().size();
    }

    protected int support(OWLClassExpression expr, OWLReasoner model) {
        return model.getInstances(expr, false).getFlattened().size();
    }

    @Override
    public FeatureValue compute(Implication impl, OWLReasoner model, PartialContext context) {
        OWLClassExpression expr = getClassExpression(impl, model);
        double v = support(expr, model);
        return new NumericFeatureValue(v / getSize(model));
    }

}
