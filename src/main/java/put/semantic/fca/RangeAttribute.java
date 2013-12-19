/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.OntObjectProperty;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class RangeAttribute implements Attribute {

    private OntObjectProperty property;
    private OntClass range;
    private OntClass notRange;
    private OntClass clazz;

    public RangeAttribute(Reasoner kb, String pURI, String dURI) {
        property = kb.getObjectProperty(pURI);        
        range = kb.getClass(dURI);
        notDomain = kb.createComplementClass(null, domain);
        clazz=kb.createUniversalRestriction(null, property, range);
    }
    
    @Override
    public String getHumanDescription() {
        return String.format("Is %s domain for property %s?", range.getLocalName(), property.getLocalName());
    }

    @Override
    public OntClass getOntClass(Reasoner kb) {
        return clazz;
    }

    @Override
    public List<String> getExamples() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getCounterexamples() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
