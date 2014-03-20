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

    protected OWLReasoner model;
    protected int size;

    public RuleCalculator() {
        super(
                "coverage",
                "prevalence",
                "support (cwa)",
                "support (owa)",
                "recall (local support) (cwa)",
                "recall (local support) (owa)",
                "lift (cwa)",
                "lift (owa)"
        );
    }

    protected int size() {
        return size;
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
        this.size = model.getRootOntology().getIndividualsInSignature().size();
        OWLClassExpression p = impl.getPremises().getClass(model);
        OWLClassExpression c = impl.getConclusions().getClass(model);
        OWLClassExpression pc = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(p, c);
        OWLClassExpression pnc = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLObjectIntersectionOf(p, c.getObjectComplementOf());
        double z = support(c);
        double y = support(p);
        double xowa, xcwa;
        xowa = support(pnc);
        xcwa = y - support(pc);
        return transform(
                y,
                z,
                y - xcwa,
                y - xowa,
                (y - xcwa) / z,
                (y - xowa) / z,
                (y - xcwa) / (y * z),
                (y - xowa) / (y * z)
        );
    }

}
