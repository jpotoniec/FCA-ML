/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.ml.features.AbstractFeatureCalculator;

/**
 *
 * @author smaug
 */
public abstract class PCAbstractFeatureCalculator extends AbstractFeatureCalculator {

    protected static String getSuffix(boolean p, boolean c) {
        if (p && !c) {
            return " (premises)";
        }
        if (!p && c) {
            return " (conclusions)";
        }
        return "";
    }
    protected boolean p;
    protected boolean c;

    public PCAbstractFeatureCalculator(String name, boolean p, boolean c) {
        super(name + getSuffix(p, c));
        assert p || c;
        this.p = p;
        this.c = c;
    }

    protected OWLClassExpression getClassExpression(Implication impl, OWLReasoner model) {
        OWLClassExpression expr = null;
        if (p && !c) {
            expr = impl.getPremises().getClass(model);
        } else if (!c && p) {
            expr = impl.getConclusions().getClass(model);
        } else {
            expr = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(
                    impl.getPremises().getClass(model),
                    impl.getConclusions().getClass(model));
        }
        assert expr != null;
        return expr;
    }

}
