/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

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
public class ImplicationShapeCalculator extends AbstractFeatureCalculator {

    public ImplicationShapeCalculator() {
        super("size: premises (abs)", "size: premises (rel)", "size: conclusions (abs)", "size: conclusions (rel)", "size: whole (abs)", "size: whole (rel)");
    }

    @Override
    public List<? extends FeatureValue> compute(Implication impl, OWLReasoner model, PartialContext context) {
        double all = context.getAttributes().size();
        double p = impl.getPremises().size();
        double c = impl.getConclusions().size();
        return transform(p, p / all, c, c / all, p + c, (p + c) / all);
    }

}
