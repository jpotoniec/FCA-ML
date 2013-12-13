/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class ClassAttribute implements Attribute {

    protected OntClass clazz;
    protected OntClass notClazz;

    public ClassAttribute(Reasoner kb, String uri) {
        this.clazz = kb.getClass(uri);
        this.notClazz = kb.createComplementClass(null, clazz);
    }

    @Override
    public boolean isSatisfiedBy(Individual a) {
        return a.hasClass(clazz);
    }

    @Override
    public boolean isOppositeSatisfiedBy(Individual a) {
        return a.hasClass(notClazz);
    }

    @Override
    public String getHumanDescription() {
        return String.format("Does object belong to class %s?", clazz.getLocalName());
    }

    @Override
    public String toString() {
        return clazz.getLocalName();
    }        

    @Override
    public OntClass getOntClass(Reasoner kb) {
        return clazz;
    }
}
