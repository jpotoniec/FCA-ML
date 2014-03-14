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

import java.util.List;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.features.AbstractFeatureCalculator;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;

/**
 *
 * @author smaug
 */
public class RuleCalculator extends AbstractFeatureCalculator {

    protected static String getSuffix(boolean owa) {
        return owa ? " (owa)" : "";
    }

    protected boolean owa;
    protected OWLReasoner model;

    public RuleCalculator(boolean owa) {
        super("support" + getSuffix(owa),
                "coverage" + getSuffix(owa),
                "prevalence" + getSuffix(owa),
                "recall (local support)" + getSuffix(owa),
                "lift" + getSuffix(owa)
        );
        this.owa = owa;
    }

    protected int size() {
        return model.getRootOntology().getIndividualsInSignature().size();
    }

    protected int size(OWLClassExpression expr) {
        return model.getInstances(expr, false).getFlattened().size();
    }

    protected double support(OWLClassExpression expr) {
        return ((double) size(expr)) / size();
    }

    @Override
    public List<? extends NumericFeatureValue> compute(Implication impl, OWLReasoner model, PartialContext context) {
        this.model = model;
        OWLClassExpression p = impl.getPremises().getClass(model);
        OWLClassExpression c = impl.getConclusions().getClass(model);
        OWLClassExpression pc = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(p, c);
        OWLClassExpression pnc = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(p, c.getObjectComplementOf());
        double z = support(c);
        double y = support(p);
        double x;
        if (owa) {
            x = support(pnc);
        } else {
            x = y - support(pc);
        }
        return transform(y - x,
                y,
                z,
                (y - x) / z,
                (y - x) / (y * z)
        );
    }

}
