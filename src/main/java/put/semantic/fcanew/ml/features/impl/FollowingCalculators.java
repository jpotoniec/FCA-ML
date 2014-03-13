/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.features.AbstractFeatureCalculator;
import put.semantic.fcanew.ml.features.values.BooleanFeatureValue;
import put.semantic.fcanew.ml.features.values.FeatureValue;

/**
 *
 * @author smaug
 */
public class FollowingCalculators extends AbstractFeatureCalculator {

    public FollowingCalculators() {
        super("follows from KB");
    }

    @Override
    public FeatureValue compute(Implication impl, OWLReasoner model, PartialContext context) {
        OWLSubClassOfAxiom subclassAxiom = impl.toAxiom(model);
        return new BooleanFeatureValue(model.isEntailed(subclassAxiom));
    }

}
