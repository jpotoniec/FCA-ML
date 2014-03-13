/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features;

import put.semantic.fcanew.ml.features.values.FeatureValue;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;

/**
 *
 * @author smaug
 */
public interface FeatureCalculator {

    public String getName();

    public FeatureValue compute(Implication impl, OWLReasoner model, PartialContext context);
}
