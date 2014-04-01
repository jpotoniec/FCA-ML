/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.impl;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import java.util.Arrays;
import java.util.List;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.mappings.Mappings;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ReadOnlySubsetOfAttributes;
import put.semantic.fcanew.ml.features.FeatureCalculator;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;

/**
 *
 * @author smaug
 */
public class EndpointCalculator implements FeatureCalculator {

    private Mappings mappings;

    public static String namify(String endpoint) {
        return endpoint.replaceAll("\\W", "_");
    }

    private boolean isValid(String pattern) {
        return pattern != null && !pattern.isEmpty();
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    private String makePattern(ReadOnlySubsetOfAttributes attrs) {
        String result = "";
        if (attrs.isEmpty()) {
            return null;
        }
        for (Attribute a : attrs) {
            String pattern = mappings.getPattern(a);
            if (isValid(pattern)) {
                if (!result.isEmpty()) {
                    result += ". \n";
                }
                result += pattern;
            } else {
                return null;
            }
        }
        return result;
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("sparql-p", "sparql-c", "sparql-pc");
    }

    protected double count(String pattern) {
        System.err.printf("'%s'\n\n", pattern);
        if (pattern == null) {
            return Double.NaN;
        }
        try {
            pattern = String.format("%s\nselect (count(distinct ?x) as ?c)\nwhere {\n%s\n}", mappings.getPrefixes(), pattern);
            QueryEngineHTTP qe = new QueryEngineHTTP(mappings.getEndpoint(), pattern);
            ResultSet result = qe.execSelect();
            if (result.hasNext()) {
                QuerySolution s = result.next();
                String var = s.varNames().next();
                return s.getLiteral(var).getInt();
            }
        } catch (Throwable ex) {
            System.err.println(pattern);
            ex.printStackTrace();
        }
        return Double.NaN;
    }

    @Override
    public List<? extends FeatureValue> compute(Implication impl, OWLReasoner model, PartialContext context) {
        String pPattern = makePattern(impl.getPremises());
        String cPattern = makePattern(impl.getConclusions());
        double p = count(pPattern);
        double c = count(cPattern);
        double pc = Double.NaN;
        if (pPattern != null && cPattern != null) {
            String pcPattern = String.format("{\n%s\n}\n{\n%s\n}\n", pPattern, cPattern);
            pc = count(pcPattern);
        }
        return Arrays.asList(new NumericFeatureValue(p),
                new NumericFeatureValue(c),
                new NumericFeatureValue(pc)
        );
    }

}
