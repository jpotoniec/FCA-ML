/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import java.util.Arrays;
import java.util.List;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.features.AbstractFeatureCalculator;
import put.semantic.fcanew.ml.features.values.FeatureValue;

/**
 *
 * @author smaug
 */
public abstract class SingleFeatureCalculator extends AbstractFeatureCalculator {

    public SingleFeatureCalculator(String name) {
        super(name);
    }

    public abstract FeatureValue computeSingle(Implication impl, OWLReasoner model, PartialContext context);

    @Override
    public List<FeatureValue> compute(Implication impl, OWLReasoner model, PartialContext context) {
        return Arrays.asList(computeSingle(impl, model, context));
    }

}
