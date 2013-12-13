/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.OntObjectProperty;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class DomainAttribute implements Attribute {

    private OntObjectProperty property;
    private OntClass domain;
    private OntClass notDomain;

    public DomainAttribute(Reasoner kb, String pURI, String dURI) {
        property = kb.getObjectProperty(pURI);
        domain = kb.getClass(dURI);
        notDomain = kb.createComplementClass(null, domain);
    }

    @Override
    public boolean isSatisfiedBy(Individual a) {
        if (a.getObjects(property).isEmpty()) {
            return true;
        }
        return a.hasClass(domain);
    }

    @Override
    public boolean isOppositeSatisfiedBy(Individual a) {
        if (a.getObjects(property).isEmpty()) {
            return true;
        }
        return a.hasClass(notDomain);
    }

    /*
     private boolean check(Individual a, OntClass clazz) {
     for (Individual object : a.getObjects(property)) {
     if (!object.hasClass(clazz)) {
     return false;
     }
     }
     return true;
     }

     @Override
     public boolean isSatisfiedBy(Individual a) {
     return check(a, domain);
     }

     @Override
     public boolean isOppositeSatisfiedBy(Individual a) {
     return check(a, notDomain);
     }
     */

    /*
     @Override
     public boolean isSatisfiedBy(Individual a) {
     for (Individual object : a.getObjects(property)) {
     if (!object.hasClass(domain)) {
     return false;
     }
     }
     return true;
     }

     @Override
     public boolean isOppositeSatisfiedBy(Individual a) {
     for (Individual object : a.getObjects(property)) {
     if (object.hasClass(notDomain)) {
     return true;
     }
     }
     return false;
     }
     */
    @Override
    public String getHumanDescription() {
        return String.format("Is %s domain for property %s?", domain.getLocalName(), property.getLocalName());
    }

    @Override
    public OntClass getOntClass(Reasoner kb) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
