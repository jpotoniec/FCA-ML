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

import org.semanticweb.owlapi.model.OWLOntologyManager;
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
public class ConsistencyCalculator extends SingleFeatureCalculator {

    public ConsistencyCalculator() {
        super("consistent");
    }

    @Override
    public FeatureValue computeSingle(Implication impl, OWLReasoner model, PartialContext context) {
        OWLOntologyManager manager = model.getRootOntology().getOWLOntologyManager();
        OWLSubClassOfAxiom subclassAxiom = impl.toAxiom(model);
        boolean result = true;
        if (!model.getRootOntology().containsAxiom(subclassAxiom, true)) {
            manager.addAxiom(model.getRootOntology(), subclassAxiom);
            model.flush();
            result = model.isConsistent();
            manager.removeAxiom(model.getRootOntology(), subclassAxiom);
            model.flush();
        }
        return new BooleanFeatureValue(result);
    }

}
