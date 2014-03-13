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
import put.semantic.fcanew.ml.features.values.BooleanFeatureValue;
import put.semantic.fcanew.ml.features.values.FeatureValue;

/**
 *
 * @author smaug
 */
public class SatCalculator extends PCAbstractFeatureCalculator {

    public SatCalculator(boolean p, boolean c) {
        super("sat", p, c);
    }

    @Override
    public FeatureValue compute(Implication impl, OWLReasoner model, PartialContext context) {
        OWLClassExpression expr = getClassExpression(impl, model);
        return new BooleanFeatureValue(model.isSatisfiable(expr));
    }

}
